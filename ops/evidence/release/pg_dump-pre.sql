--
-- PostgreSQL database dump
--

\restrict QWPRc0hdmqbBVbet9kn6ljUnnjbNKCypen7YQjhx4rdWA8d1toPUbRJP12aYpnv

-- Dumped from database version 16.14 (Debian 16.14-1.pgdg13+1)
-- Dumped by pg_dump version 16.14 (Debian 16.14-1.pgdg13+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: chain_catalog_snapshot; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.chain_catalog_snapshot (
    id character varying(64) NOT NULL,
    version_id character varying(64) NOT NULL,
    snapshot_json text NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: chain_version; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.chain_version (
    id character varying(64) NOT NULL,
    product_id character varying(64) NOT NULL,
    version_no integer NOT NULL,
    metadata_hash character varying(256) NOT NULL,
    owner_did character varying(512) NOT NULL,
    cert_owner character varying(512) NOT NULL,
    ts timestamp with time zone NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: data_product; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.data_product (
    id character varying(64) NOT NULL,
    product_code character varying(128) NOT NULL,
    product_name character varying(512) NOT NULL,
    product_type character varying(32) NOT NULL,
    l2_category_id character varying(64) NOT NULL,
    business_category character varying(256),
    business_sub_category character varying(256),
    data_source character varying(64),
    update_frequency character varying(64),
    delivery_method character varying(64),
    involves_personal_info boolean,
    involves_public_data boolean,
    billing_method character varying(128),
    price character varying(128),
    supplier_name character varying(256),
    supplier_credit_code character varying(128),
    property_rights_type character varying(128),
    tags_json text,
    summary text,
    scenario text,
    type_dataset_json text,
    type_report_json text,
    type_api_json text,
    status character varying(32) DEFAULT 'LISTED'::character varying NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT ck_data_product_type CHECK (((product_type)::text = ANY ((ARRAY['DATASET'::character varying, 'REPORT'::character varying, 'API'::character varying, 'OTHER'::character varying])::text[])))
);


--
-- Name: flyway_schema_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


--
-- Name: industry_category; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.industry_category (
    id character varying(64) NOT NULL,
    name character varying(256) NOT NULL,
    level character varying(8) NOT NULL,
    parent_id character varying(64),
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    updated_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT ck_industry_category_level CHECK (((level)::text = ANY ((ARRAY['L1'::character varying, 'L2'::character varying])::text[])))
);


--
-- Name: overview_stream_event; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.overview_stream_event (
    id character varying(64) NOT NULL,
    event_type character varying(32) NOT NULL,
    subject character varying(512) NOT NULL,
    action_summary character varying(1024) NOT NULL,
    occurred_at timestamp with time zone NOT NULL,
    chain_record_id character varying(256) NOT NULL,
    created_at timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT ck_overview_stream_event_type CHECK (((event_type)::text = ANY ((ARRAY['CATALOG_REGISTER'::character varying, 'DATA_REGISTER'::character varying, 'TRADE_ORDER'::character varying])::text[])))
);


--
-- Data for Name: chain_catalog_snapshot; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.chain_catalog_snapshot (id, version_id, snapshot_json, created_at) FROM stdin;
\.


--
-- Data for Name: chain_version; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.chain_version (id, product_id, version_no, metadata_hash, owner_did, cert_owner, ts, created_at) FROM stdin;
\.


--
-- Data for Name: data_product; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.data_product (id, product_code, product_name, product_type, l2_category_id, business_category, business_sub_category, data_source, update_frequency, delivery_method, involves_personal_info, involves_public_data, billing_method, price, supplier_name, supplier_credit_code, property_rights_type, tags_json, summary, scenario, type_dataset_json, type_report_json, type_api_json, status, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: flyway_schema_history; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) FROM stdin;
1	202607281600	init wsc	SQL	V202607281600__init_wsc.sql	120320878	wsc	2026-07-30 03:27:05.82064	117	t
\.


--
-- Data for Name: industry_category; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.industry_category (id, name, level, parent_id, created_at, updated_at) FROM stdin;
\.


--
-- Data for Name: overview_stream_event; Type: TABLE DATA; Schema: public; Owner: -
--

COPY public.overview_stream_event (id, event_type, subject, action_summary, occurred_at, chain_record_id, created_at) FROM stdin;
\.


--
-- Name: chain_catalog_snapshot chain_catalog_snapshot_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chain_catalog_snapshot
    ADD CONSTRAINT chain_catalog_snapshot_pkey PRIMARY KEY (id);


--
-- Name: chain_version chain_version_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chain_version
    ADD CONSTRAINT chain_version_pkey PRIMARY KEY (id);


--
-- Name: data_product data_product_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_product
    ADD CONSTRAINT data_product_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: industry_category industry_category_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.industry_category
    ADD CONSTRAINT industry_category_pkey PRIMARY KEY (id);


--
-- Name: overview_stream_event overview_stream_event_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.overview_stream_event
    ADD CONSTRAINT overview_stream_event_pkey PRIMARY KEY (id);


--
-- Name: chain_catalog_snapshot uq_chain_catalog_snapshot_version; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chain_catalog_snapshot
    ADD CONSTRAINT uq_chain_catalog_snapshot_version UNIQUE (version_id);


--
-- Name: chain_version uq_chain_version_product_no; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chain_version
    ADD CONSTRAINT uq_chain_version_product_no UNIQUE (product_id, version_no);


--
-- Name: data_product uq_data_product_code; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_product
    ADD CONSTRAINT uq_data_product_code UNIQUE (product_code);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- Name: idx_chain_version_product; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_chain_version_product ON public.chain_version USING btree (product_id);


--
-- Name: idx_data_product_l2; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_data_product_l2 ON public.data_product USING btree (l2_category_id);


--
-- Name: idx_data_product_type; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_data_product_type ON public.data_product USING btree (product_type);


--
-- Name: idx_industry_category_parent; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_industry_category_parent ON public.industry_category USING btree (parent_id);


--
-- Name: idx_overview_stream_occurred; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_overview_stream_occurred ON public.overview_stream_event USING btree (occurred_at DESC);


--
-- Name: chain_catalog_snapshot fk_chain_catalog_snapshot_version; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chain_catalog_snapshot
    ADD CONSTRAINT fk_chain_catalog_snapshot_version FOREIGN KEY (version_id) REFERENCES public.chain_version(id);


--
-- Name: chain_version fk_chain_version_product; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.chain_version
    ADD CONSTRAINT fk_chain_version_product FOREIGN KEY (product_id) REFERENCES public.data_product(id);


--
-- Name: data_product fk_data_product_l2; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.data_product
    ADD CONSTRAINT fk_data_product_l2 FOREIGN KEY (l2_category_id) REFERENCES public.industry_category(id);


--
-- Name: industry_category fk_industry_category_parent; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.industry_category
    ADD CONSTRAINT fk_industry_category_parent FOREIGN KEY (parent_id) REFERENCES public.industry_category(id);


--
-- PostgreSQL database dump complete
--

\unrestrict QWPRc0hdmqbBVbet9kn6ljUnnjbNKCypen7YQjhx4rdWA8d1toPUbRJP12aYpnv

