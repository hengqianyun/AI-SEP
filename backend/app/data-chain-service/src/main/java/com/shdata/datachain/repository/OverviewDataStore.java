package com.shdata.datachain.repository;

import com.shdata.datachain.entity.OverviewStreamEventEntity;
import com.shdata.datachain.model.DistributionSliceRecord;
import com.shdata.datachain.model.TopItemRecord;
import com.shdata.datachain.model.TrendPointRecord;
import com.shdata.datachain.model.StreamEventType;
import com.shdata.datachain.model.StreamEventRecord;
import com.shdata.datachain.model.OverviewSnapshot;
import com.shdata.datachain.repository.OverviewStreamEventRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 总览数据存储（JPA + 内存混合版）。
 * <p>流事件从 DB 读；指标/趋势/排行仍为内存种子（后续可改为聚合查询）。</p>
 */
@Component
public class OverviewDataStore {

    private final OverviewStreamEventRepository eventRepo;
    private final AtomicReference<OverviewSnapshot> metricSnapshot =
            new AtomicReference<>(seedMetrics(Instant.parse("2026-07-29T06:00:00Z")));

    public OverviewDataStore(OverviewStreamEventRepository eventRepo) {
        this.eventRepo = eventRepo;
    }

    /**
     * 获取总览快照：流事件从 DB，指标/趋势/分布从内存种子。
     */
    @Transactional(readOnly = true)
    public OverviewSnapshot get() {
        OverviewSnapshot metrics = metricSnapshot.get();
        List<StreamEventRecord> stream = eventRepo.findAll().stream()
                .map(this::toStreamEvent)
                .sorted(Comparator.comparing(StreamEventRecord::occurredAt).reversed())
                .toList();
        // 若 DB 有数据则用 DB，否则用内存种子
        if (!stream.isEmpty()) {
            return new OverviewSnapshot(
                    metrics.assetTotal(), metrics.activeEnterprises(), metrics.todayAttestations(),
                    metrics.updatedAt(),
                    metrics.trendPoints(), metrics.topItems(), stream,
                    metrics.byIndustry(), metrics.byRegion());
        }
        return metrics;
    }

    public void clear() {
        metricSnapshot.set(OverviewSnapshot.empty(Instant.parse("2026-07-29T06:00:00Z")));
    }

    public void resetToSeed() {
        metricSnapshot.set(seedMetrics(Instant.parse("2026-07-29T06:00:00Z")));
    }

    public void replace(OverviewSnapshot next) {
        metricSnapshot.set(next);
    }

    private StreamEventRecord toStreamEvent(OverviewStreamEventEntity e) {
        try {
            return new StreamEventRecord(
                    StreamEventType.valueOf(e.getEventType()),
                    e.getSubject(),
                    e.getActionSummary(),
                    e.getOccurredAt(),
                    e.getChainRecordId());
        } catch (IllegalArgumentException ex) {
            return new StreamEventRecord(
                    StreamEventType.CATALOG_REGISTER,
                    e.getSubject(), e.getActionSummary(), e.getOccurredAt(), e.getChainRecordId());
        }
    }

    static OverviewSnapshot seedMetrics(Instant updatedAt) {
        LocalDate today = LocalDate.of(2026, 7, 29);
        List<TrendPointRecord> trend = new ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            int count = (i % 5 == 0) ? 0 : 2 + (i % 7);
            trend.add(new TrendPointRecord(d, count));
        }
        List<TopItemRecord> top = List.of(
                new TopItemRecord("p-01", "企业信用数据集", 42),
                new TopItemRecord("p-02", "供应链发票报告", 35),
                new TopItemRecord("p-03", "物流轨迹接口", 28),
                new TopItemRecord("p-04", "气象观测数据集", 21),
                new TopItemRecord("p-05", "能耗监测报告", 18),
                new TopItemRecord("p-06", "征信评分接口", 15),
                new TopItemRecord("p-07", "海关报关数据集", 12),
                new TopItemRecord("p-08", "医疗影像报告", 9),
                new TopItemRecord("p-09", "农业产量数据集", 6),
                new TopItemRecord("p-10", "其他行业样本", 3));
        List<DistributionSliceRecord> byIndustry = List.of(
                new DistributionSliceRecord("金融", 32),
                new DistributionSliceRecord("制造", 24),
                new DistributionSliceRecord("物流", 18),
                new DistributionSliceRecord("医疗", 14),
                new DistributionSliceRecord("其他", 12));
        List<DistributionSliceRecord> byRegion = List.of(
                new DistributionSliceRecord("华东", 40),
                new DistributionSliceRecord("华南", 25),
                new DistributionSliceRecord("华北", 20),
                new DistributionSliceRecord("西南", 15));
        return new OverviewSnapshot(
                128L, 36L, 7L, updatedAt,
                List.copyOf(trend), top, List.of(), byIndustry, byRegion);
    }

    public static OverviewSnapshot fixtureThreeStreamTypes() {
        return OverviewDataStore.seedMetrics(Instant.parse("2026-07-29T06:00:00Z"));
    }
}
