package com.wsc.overview;

import java.time.Instant;

public record StreamEventRecord(
    StreamEventType type,
    String subject,
    String actionSummary,
    Instant occurredAt,
    String chainRecordId) {}
