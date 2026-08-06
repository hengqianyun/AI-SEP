package com.shdata.datachain.model;

import java.time.Instant;

public record StreamEventRecord(
    StreamEventType type,
    String subject,
    String actionSummary,
    Instant occurredAt,
    String chainRecordId) {}
