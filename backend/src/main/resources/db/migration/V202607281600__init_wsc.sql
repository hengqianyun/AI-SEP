-- WSC V1 initial schema (wsc-contracts@1.1.0 / TASK-WSC-001)
-- PostgreSQL 16 + Flyway. Aligns DEC-WSC-001/002/003.

CREATE TABLE industry_category (
    id              VARCHAR(64) PRIMARY KEY,
    name            VARCHAR(256) NOT NULL,
    level           VARCHAR(8)  NOT NULL,
    parent_id       VARCHAR(64) NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_industry_category_level CHECK (level IN ('L1', 'L2')),
    CONSTRAINT fk_industry_category_parent
        FOREIGN KEY (parent_id) REFERENCES industry_category (id)
);

CREATE INDEX idx_industry_category_parent ON industry_category (parent_id);

-- product_type: DATASET | REPORT | API | OTHER
-- OQ-004: OTHER has no required type-specific columns (nullable type_* remain unused)
CREATE TABLE data_product (
    id                      VARCHAR(64) PRIMARY KEY,
    product_code            VARCHAR(128) NOT NULL,
    product_name            VARCHAR(512) NOT NULL,
    product_type            VARCHAR(32)  NOT NULL,
    l2_category_id          VARCHAR(64)  NOT NULL,
    business_category       VARCHAR(256),
    business_sub_category   VARCHAR(256),
    data_source             VARCHAR(64),
    update_frequency        VARCHAR(64),
    delivery_method         VARCHAR(64),
    involves_personal_info  BOOLEAN,
    involves_public_data    BOOLEAN,
    billing_method          VARCHAR(128),
    price                   VARCHAR(128),
    supplier_name           VARCHAR(256),
    supplier_credit_code    VARCHAR(128),
    property_rights_type    VARCHAR(128),
    tags_json               TEXT,
    summary                 TEXT,
    scenario                TEXT,
    -- type-specific nullable columns (unused / ignored for OTHER)
    type_dataset_json       TEXT,
    type_report_json        TEXT,
    type_api_json           TEXT,
    status                  VARCHAR(32) NOT NULL DEFAULT 'LISTED',
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_data_product_code UNIQUE (product_code),
    CONSTRAINT ck_data_product_type CHECK (product_type IN ('DATASET', 'REPORT', 'API', 'OTHER')),
    CONSTRAINT fk_data_product_l2
        FOREIGN KEY (l2_category_id) REFERENCES industry_category (id)
);

CREATE INDEX idx_data_product_l2 ON data_product (l2_category_id);
CREATE INDEX idx_data_product_type ON data_product (product_type);

CREATE TABLE chain_version (
    id              VARCHAR(64) PRIMARY KEY,
    product_id      VARCHAR(64) NOT NULL,
    version_no      INTEGER     NOT NULL,
    metadata_hash   VARCHAR(256) NOT NULL,
    owner_did       VARCHAR(512) NOT NULL,
    cert_owner      VARCHAR(512) NOT NULL,
    ts              TIMESTAMPTZ NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_chain_version_product_no UNIQUE (product_id, version_no),
    CONSTRAINT fk_chain_version_product
        FOREIGN KEY (product_id) REFERENCES data_product (id)
);

CREATE INDEX idx_chain_version_product ON chain_version (product_id);

CREATE TABLE chain_catalog_snapshot (
    id              VARCHAR(64) PRIMARY KEY,
    version_id      VARCHAR(64) NOT NULL,
    snapshot_json   TEXT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_chain_catalog_snapshot_version UNIQUE (version_id),
    CONSTRAINT fk_chain_catalog_snapshot_version
        FOREIGN KEY (version_id) REFERENCES chain_version (id)
);

-- OVW stream: V1 fixture/projection supply (no dependency on unimplemented business APIs)
CREATE TABLE overview_stream_event (
    id                  VARCHAR(64) PRIMARY KEY,
    event_type          VARCHAR(32)  NOT NULL,
    subject             VARCHAR(512) NOT NULL,
    action_summary      VARCHAR(1024) NOT NULL,
    occurred_at         TIMESTAMPTZ NOT NULL,
    chain_record_id     VARCHAR(256) NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_overview_stream_event_type
        CHECK (event_type IN ('CATALOG_REGISTER', 'DATA_REGISTER', 'TRADE_ORDER'))
);

CREATE INDEX idx_overview_stream_occurred ON overview_stream_event (occurred_at DESC);
