package com.shdata.datachain.overview;

import java.time.Instant;
import java.util.List;

/** 内存投影快照：空态时 metrics 为 0、列表为空。 */
public record OverviewSnapshot(
    long assetTotal,
    long activeEnterprises,
    long todayAttestations,
    Instant updatedAt,
    List<TrendPointRecord> trendPoints,
    List<TopItemRecord> topItems,
    List<StreamEventRecord> streamEvents,
    List<DistributionSliceRecord> byIndustry,
    List<DistributionSliceRecord> byRegion) {

  public static OverviewSnapshot empty(Instant updatedAt) {
    return new OverviewSnapshot(
        0L, 0L, 0L, updatedAt, List.of(), List.of(), List.of(), List.of(), List.of());
  }
}
