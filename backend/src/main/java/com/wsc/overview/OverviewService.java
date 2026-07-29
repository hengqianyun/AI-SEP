package com.wsc.overview;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class OverviewService {

  private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

  private final OverviewDataStore store;

  public OverviewService(OverviewDataStore store) {
    this.store = store;
  }

  public Map<String, Object> metrics() {
    OverviewSnapshot snap = store.get();
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("assetTotal", snap.assetTotal());
    data.put("activeEnterprises", snap.activeEnterprises());
    data.put("todayAttestations", snap.todayAttestations());
    data.put("updatedAt", snap.updatedAt().toString());
    return data;
  }

  public Map<String, Object> trend(int days) {
    int window = Math.max(1, Math.min(days, 90));
    OverviewSnapshot snap = store.get();
    LocalDate end =
        snap.updatedAt() != null
            ? snap.updatedAt().atZone(ZoneOffset.UTC).toLocalDate()
            : LocalDate.now(ZoneOffset.UTC);

    Map<LocalDate, Integer> byDate =
        snap.trendPoints().stream()
            .collect(
                Collectors.toMap(
                    TrendPointRecord::date, TrendPointRecord::count, Integer::sum, LinkedHashMap::new));

    List<Map<String, Object>> points = new ArrayList<>();
    for (int i = window - 1; i >= 0; i--) {
      LocalDate d = end.minusDays(i);
      Map<String, Object> point = new LinkedHashMap<>();
      point.put("date", ISO_DATE.format(d));
      point.put("count", byDate.getOrDefault(d, 0));
      points.add(point);
    }

    Map<String, Object> data = new LinkedHashMap<>();
    data.put("points", points);
    return data;
  }

  public Map<String, Object> top(int limit) {
    int cap = Math.max(1, Math.min(limit, 10));
    List<Map<String, Object>> items =
        store.get().topItems().stream()
            .sorted(Comparator.comparingInt(TopItemRecord::count).reversed())
            .limit(cap)
            .map(
                t -> {
                  Map<String, Object> row = new LinkedHashMap<>();
                  if (t.productId() != null) {
                    row.put("productId", t.productId());
                  }
                  row.put("productName", t.productName());
                  row.put("count", t.count());
                  return row;
                })
            .toList();
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("items", items);
    return data;
  }

  public Map<String, Object> stream(int limit) {
    int cap = Math.max(1, Math.min(limit <= 0 ? 10 : limit, 100));
    Instant now = Instant.now();
    List<Map<String, Object>> items =
        store.get().streamEvents().stream()
            .sorted(Comparator.comparing(StreamEventRecord::occurredAt).reversed())
            .limit(cap)
            .map(e -> toStreamItem(e, now))
            .toList();
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("items", items);
    return data;
  }

  public Map<String, Object> distribution() {
    OverviewSnapshot snap = store.get();
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("byIndustry", toSlices(snap.byIndustry()));
    data.put("byRegion", toSlices(snap.byRegion()));
    return data;
  }

  private static List<Map<String, Object>> toSlices(List<DistributionSliceRecord> slices) {
    return slices.stream()
        .map(
            s -> {
              Map<String, Object> row = new LinkedHashMap<>();
              row.put("name", s.name());
              row.put("value", s.value());
              return row;
            })
        .toList();
  }

  private static Map<String, Object> toStreamItem(StreamEventRecord e, Instant now) {
    Map<String, Object> row = new LinkedHashMap<>();
    row.put("type", e.type().name());
    row.put("subject", e.subject());
    row.put("actionSummary", e.actionSummary());
    row.put("relativeTime", relativeTime(e.occurredAt(), now));
    row.put("occurredAt", e.occurredAt().toString());
    row.put("chainRecordId", e.chainRecordId());
    return row;
  }

  static String relativeTime(Instant occurredAt, Instant now) {
    Duration d = Duration.between(occurredAt, now);
    if (d.isNegative()) {
      return "刚刚";
    }
    long seconds = d.getSeconds();
    if (seconds < 60) {
      return "刚刚";
    }
    long minutes = seconds / 60;
    if (minutes < 60) {
      return minutes + " 分钟前";
    }
    long hours = minutes / 60;
    if (hours < 24) {
      return hours + " 小时前";
    }
    long days = hours / 24;
    if (days < 30) {
      return days + " 天前";
    }
    return (days / 30) + " 个月前";
  }
}
