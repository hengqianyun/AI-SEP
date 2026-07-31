-- WSC V1.1 schema (wsc-contracts@2.0.0 / TASK-WSC-101)
-- MySQL 8 + Flyway. Three-level industry tree; products hang on L3.
-- Sole authority path: sql/migration (DEC-WSC-005). Do NOT use db/migration.

CREATE TABLE industry_category (
    id              VARCHAR(64)  NOT NULL PRIMARY KEY,
    name            VARCHAR(256) NOT NULL,
    level           VARCHAR(8)   NOT NULL,
    parent_id       VARCHAR(64)  NULL,
    created_at      TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at      TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    CONSTRAINT ck_industry_category_level CHECK (level IN ('L1', 'L2', 'L3')),
    CONSTRAINT fk_industry_category_parent
        FOREIGN KEY (parent_id) REFERENCES industry_category (id)
);

CREATE INDEX idx_industry_category_parent ON industry_category (parent_id);
CREATE INDEX idx_industry_category_level ON industry_category (level);

-- product_type: DATASET | REPORT | API | OTHER
-- OQ-004: OTHER has no required type-specific columns
-- Maintenance entry ≡ data product (same id / product_code)
CREATE TABLE data_product (
    id                      VARCHAR(64)  NOT NULL PRIMARY KEY,
    product_code            VARCHAR(128) NOT NULL,
    product_name            VARCHAR(512) NOT NULL,
    product_type            VARCHAR(32)  NOT NULL,
    l3_category_id          VARCHAR(64)  NULL,
    maintenance_status      VARCHAR(32)  NOT NULL DEFAULT 'PENDING',
    business_category       VARCHAR(256) NULL,
    business_sub_category   VARCHAR(256) NULL,
    data_source             VARCHAR(64)  NULL,
    update_frequency        VARCHAR(64)  NULL,
    delivery_method         VARCHAR(64)  NULL,
    involves_personal_info  TINYINT(1)   NULL,
    involves_public_data    TINYINT(1)   NULL,
    billing_method          VARCHAR(128) NULL,
    price                   VARCHAR(128) NULL,
    supplier_name           VARCHAR(256) NULL,
    supplier_credit_code    VARCHAR(128) NULL,
    property_rights_type    VARCHAR(128) NULL,
    tags_json               TEXT         NULL,
    summary                 TEXT         NULL,
    scenario                TEXT         NULL,
    type_dataset_json       TEXT         NULL,
    type_report_json        TEXT         NULL,
    type_api_json           TEXT         NULL,
    status                  VARCHAR(32)  NOT NULL DEFAULT 'LISTED',
    created_at              TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at              TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    CONSTRAINT uq_data_product_code UNIQUE (product_code),
    CONSTRAINT ck_data_product_type CHECK (product_type IN ('DATASET', 'REPORT', 'API', 'OTHER')),
    CONSTRAINT ck_data_product_maint CHECK (maintenance_status IN ('PENDING', 'MAINTAINED')),
    CONSTRAINT fk_data_product_l3
        FOREIGN KEY (l3_category_id) REFERENCES industry_category (id)
);

CREATE INDEX idx_data_product_l3 ON data_product (l3_category_id);
CREATE INDEX idx_data_product_type ON data_product (product_type);
CREATE INDEX idx_data_product_maint ON data_product (maintenance_status);

CREATE TABLE chain_version (
    id              VARCHAR(64)  NOT NULL PRIMARY KEY,
    product_id      VARCHAR(64)  NOT NULL,
    version_no      INT          NOT NULL,
    metadata_hash   VARCHAR(256) NOT NULL,
    owner_did       VARCHAR(512) NOT NULL,
    cert_owner      VARCHAR(512) NOT NULL,
    ts              TIMESTAMP(3) NOT NULL,
    created_at      TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT uq_chain_version_product_no UNIQUE (product_id, version_no),
    CONSTRAINT fk_chain_version_product
        FOREIGN KEY (product_id) REFERENCES data_product (id)
);

CREATE INDEX idx_chain_version_product ON chain_version (product_id);

CREATE TABLE chain_catalog_snapshot (
    id              VARCHAR(64)  NOT NULL PRIMARY KEY,
    version_id      VARCHAR(64)  NOT NULL,
    snapshot_json   TEXT         NOT NULL,
    created_at      TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT uq_chain_catalog_snapshot_version UNIQUE (version_id),
    CONSTRAINT fk_chain_catalog_snapshot_version
        FOREIGN KEY (version_id) REFERENCES chain_version (id)
);

-- OVW stream projection (no new business metrics in TASK-WSC-101)
CREATE TABLE overview_stream_event (
    id                  VARCHAR(64)   NOT NULL PRIMARY KEY,
    event_type          VARCHAR(32)   NOT NULL,
    subject             VARCHAR(512)  NOT NULL,
    action_summary      VARCHAR(1024) NOT NULL,
    occurred_at         TIMESTAMP(3)  NOT NULL,
    chain_record_id     VARCHAR(256)  NOT NULL,
    created_at          TIMESTAMP(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT ck_overview_stream_event_type
        CHECK (event_type IN ('CATALOG_REGISTER', 'DATA_REGISTER', 'TRADE_ORDER'))
);

CREATE INDEX idx_overview_stream_occurred ON overview_stream_event (occurred_at);

-- Import error reports (TTL ≤24h enforced by application; whitelist fields only)
CREATE TABLE import_error_report (
    id              VARCHAR(64)  NOT NULL PRIMARY KEY,
    created_at      TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    expires_at      TIMESTAMP(3) NOT NULL,
    success_count   INT          NOT NULL DEFAULT 0,
    failure_count   INT          NOT NULL DEFAULT 0,
    rows_json       TEXT         NOT NULL,
    created_by      VARCHAR(64)  NULL
);

CREATE INDEX idx_import_error_report_expires ON import_error_report (expires_at);
