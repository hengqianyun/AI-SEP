package com.shdata.datachain.overview;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Component;

/**
 * V1 内存 seed/fixture 投影。测试可 {@link #clear()} 验证空态，{@link #resetToSeed()} 恢复。
 */
@Component
public class OverviewDataStore {

  private final AtomicReference<OverviewSnapshot> snapshot =
      new AtomicReference<>(seed(Instant.parse("2026-07-29T06:00:00Z")));

  public OverviewSnapshot get() {
    return snapshot.get();
  }

  public void clear() {
    snapshot.set(OverviewSnapshot.empty(Instant.parse("2026-07-29T06:00:00Z")));
  }

  public void resetToSeed() {
    snapshot.set(seed(Instant.parse("2026-07-29T06:00:00Z")));
  }

  /** 注入自定义 fixture（集成测试三类流等）。 */
  public void replace(OverviewSnapshot next) {
    snapshot.set(next);
  }

  static OverviewSnapshot seed(Instant updatedAt) {
    LocalDate today = LocalDate.of(2026, 7, 29);
    List<TrendPointRecord> trend = new ArrayList<>();
    for (int i = 29; i >= 0; i--) {
      LocalDate d = today.minusDays(i);
      int count = (i % 5 == 0) ? 0 : 2 + (i % 7);
      trend.add(new TrendPointRecord(d, count));
    }

    List<TopItemRecord> top =
        List.of(
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

    // 三类类型；按 occurredAt 倒序（最新在前）
    List<StreamEventRecord> stream =
        List.of(
            new StreamEventRecord(
                StreamEventType.TRADE_ORDER,
                "订单 ORD-20260729-001",
                "交易订单上链存证",
                Instant.parse("2026-07-29T05:45:00Z"),
                "0xstream-trade-001"),
            new StreamEventRecord(
                StreamEventType.DATA_REGISTER,
                "企业「华南数据」",
                "数据登记上链（演示文案）",
                Instant.parse("2026-07-29T04:20:00Z"),
                "0xstream-data-001"),
            new StreamEventRecord(
                StreamEventType.CATALOG_REGISTER,
                "产品「企业信用数据集」",
                "目录登记上链",
                Instant.parse("2026-07-29T02:10:00Z"),
                "0xstream-catalog-001"),
            new StreamEventRecord(
                StreamEventType.CATALOG_REGISTER,
                "产品「供应链发票报告」",
                "目录登记上链",
                Instant.parse("2026-07-28T18:00:00Z"),
                "0xstream-catalog-002"),
            new StreamEventRecord(
                StreamEventType.TRADE_ORDER,
                "订单 ORD-20260728-009",
                "交易订单上链存证",
                Instant.parse("2026-07-28T12:30:00Z"),
                "0xstream-trade-002"),
            new StreamEventRecord(
                StreamEventType.DATA_REGISTER,
                "企业「华北智造」",
                "数据登记上链（演示文案）",
                Instant.parse("2026-07-28T09:00:00Z"),
                "0xstream-data-002"));

    List<StreamEventRecord> sorted =
        stream.stream()
            .sorted(Comparator.comparing(StreamEventRecord::occurredAt).reversed())
            .toList();

    List<DistributionSliceRecord> byIndustry =
        List.of(
            new DistributionSliceRecord("金融", 32),
            new DistributionSliceRecord("制造", 24),
            new DistributionSliceRecord("物流", 18),
            new DistributionSliceRecord("医疗", 14),
            new DistributionSliceRecord("其他", 12));

    List<DistributionSliceRecord> byRegion =
        List.of(
            new DistributionSliceRecord("华东", 40),
            new DistributionSliceRecord("华南", 25),
            new DistributionSliceRecord("华北", 20),
            new DistributionSliceRecord("西南", 15));

    return new OverviewSnapshot(
        128L,
        36L,
        7L,
        updatedAt,
        List.copyOf(trend),
        top,
        sorted,
        byIndustry,
        byRegion);
  }

  /** 测试用：仅三类流、明确倒序。 */
  public static OverviewSnapshot fixtureThreeStreamTypes() {
    Instant updatedAt = Instant.parse("2026-07-29T06:00:00Z");
    LocalDate today = updatedAt.atZone(ZoneOffset.UTC).toLocalDate();
    List<TrendPointRecord> trend = new ArrayList<>();
    for (int i = 2; i >= 0; i--) {
      trend.add(new TrendPointRecord(today.minusDays(i), i + 1));
    }
    List<StreamEventRecord> stream =
        List.of(
            new StreamEventRecord(
                StreamEventType.CATALOG_REGISTER,
                "主体-目录",
                "目录登记",
                Instant.parse("2026-07-29T01:00:00Z"),
                "chain-catalog"),
            new StreamEventRecord(
                StreamEventType.DATA_REGISTER,
                "主体-数据",
                "数据登记",
                Instant.parse("2026-07-29T03:00:00Z"),
                "chain-data"),
            new StreamEventRecord(
                StreamEventType.TRADE_ORDER,
                "主体-订单",
                "交易订单",
                Instant.parse("2026-07-29T05:00:00Z"),
                "chain-trade"));
    return new OverviewSnapshot(
        3L,
        2L,
        1L,
        updatedAt,
        trend,
        List.of(new TopItemRecord("p-1", "演示产品", 3)),
        stream,
        List.of(new DistributionSliceRecord("金融", 100)),
        List.of(new DistributionSliceRecord("华东", 100)));
  }
}
