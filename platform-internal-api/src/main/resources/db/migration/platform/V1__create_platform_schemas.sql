CREATE SCHEMA IF NOT EXISTS platform_auth;
CREATE SCHEMA IF NOT EXISTS platform_core;
CREATE SCHEMA IF NOT EXISTS platform_sites;
CREATE SCHEMA IF NOT EXISTS platform_audit;

CREATE TABLE platform_auth.platform_users (
    id UUID PRIMARY KEY,
    email VARCHAR(320),
    display_name VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(1000),
    status VARCHAR(40) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMPTZ
);

CREATE TABLE platform_auth.platform_user_roles (
    platform_user_id UUID NOT NULL REFERENCES platform_auth.platform_users(id) ON DELETE CASCADE,
    role VARCHAR(40) NOT NULL,
    PRIMARY KEY (platform_user_id, role)
);

CREATE TABLE platform_auth.provider_identities (
    id UUID PRIMARY KEY,
    platform_user_id UUID NOT NULL REFERENCES platform_auth.platform_users(id) ON DELETE CASCADE,
    provider_type VARCHAR(40) NOT NULL,
    provider_subject VARCHAR(255) NOT NULL,
    email_snapshot VARCHAR(320),
    display_name_snapshot VARCHAR(255),
    avatar_url_snapshot VARCHAR(1000),
    provider_metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMPTZ,
    CONSTRAINT uk_provider_identity UNIQUE (provider_type, provider_subject)
);

CREATE TABLE platform_core.tenant_accounts (
    id UUID PRIMARY KEY,
    slug VARCHAR(255) NOT NULL UNIQUE,
    display_name VARCHAR(255) NOT NULL,
    status VARCHAR(40) NOT NULL,
    settings JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE platform_core.tenant_memberships (
    id UUID PRIMARY KEY,
    tenant_account_id UUID NOT NULL REFERENCES platform_core.tenant_accounts(id) ON DELETE CASCADE,
    platform_user_id UUID NOT NULL REFERENCES platform_auth.platform_users(id) ON DELETE CASCADE,
    role VARCHAR(40) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_tenant_membership UNIQUE (tenant_account_id, platform_user_id)
);

CREATE TABLE platform_core.tenant_onboarding_states (
    id UUID PRIMARY KEY,
    tenant_account_id UUID NOT NULL REFERENCES platform_core.tenant_accounts(id) ON DELETE CASCADE,
    readiness_state VARCHAR(40) NOT NULL,
    current_step VARCHAR(255) NOT NULL,
    draft_data JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMPTZ,
    CONSTRAINT uk_tenant_onboarding UNIQUE (tenant_account_id)
);

CREATE TABLE platform_sites.tenant_sites (
    id UUID PRIMARY KEY,
    tenant_account_id UUID NOT NULL REFERENCES platform_core.tenant_accounts(id) ON DELETE CASCADE,
    created_by_user_id UUID NOT NULL REFERENCES platform_auth.platform_users(id),
    slug VARCHAR(255) NOT NULL,
    site_name VARCHAR(255) NOT NULL,
    summary VARCHAR(2000),
    lifecycle_state VARCHAR(40) NOT NULL,
    readiness_state VARCHAR(40) NOT NULL,
    site_configuration JSONB NOT NULL DEFAULT '{}'::jsonb,
    current_publication_id UUID,
    last_error_message VARCHAR(4000),
    launched_at TIMESTAMPTZ,
    destroyed_at TIMESTAMPTZ,
    last_status_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_tenant_site_slug UNIQUE (tenant_account_id, slug)
);

CREATE TABLE platform_sites.site_runtime_definitions (
    id UUID PRIMARY KEY,
    tenant_site_id UUID NOT NULL REFERENCES platform_sites.tenant_sites(id) ON DELETE CASCADE,
    runtime_identifier VARCHAR(255),
    runtime_namespace VARCHAR(255) NOT NULL,
    virtual_machine_name VARCHAR(255) NOT NULL,
    service_name VARCHAR(255) NOT NULL,
    ingress_name VARCHAR(255),
    desired_cpu_cores INTEGER NOT NULL,
    desired_memory_mib INTEGER NOT NULL,
    desired_storage_gib INTEGER NOT NULL,
    desired_config JSONB NOT NULL DEFAULT '{}'::jsonb,
    actual_config JSONB NOT NULL DEFAULT '{}'::jsonb,
    status_payload JSONB NOT NULL DEFAULT '{}'::jsonb,
    last_synchronized_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_site_runtime_definition UNIQUE (tenant_site_id)
);

CREATE TABLE platform_sites.site_domains (
    id UUID PRIMARY KEY,
    tenant_site_id UUID NOT NULL REFERENCES platform_sites.tenant_sites(id) ON DELETE CASCADE,
    host VARCHAR(320) NOT NULL UNIQUE,
    domain_status VARCHAR(40) NOT NULL,
    primary_domain BOOLEAN NOT NULL DEFAULT FALSE,
    routing_config JSONB NOT NULL DEFAULT '{}'::jsonb,
    verified_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE platform_sites.site_publications (
    id UUID PRIMARY KEY,
    tenant_site_id UUID NOT NULL REFERENCES platform_sites.tenant_sites(id) ON DELETE CASCADE,
    created_by_user_id UUID NOT NULL REFERENCES platform_auth.platform_users(id),
    version_label VARCHAR(255) NOT NULL,
    build_reference VARCHAR(255) NOT NULL,
    publication_status VARCHAR(40) NOT NULL,
    runtime_config_snapshot JSONB NOT NULL DEFAULT '{}'::jsonb,
    store_config_snapshot JSONB NOT NULL DEFAULT '{}'::jsonb,
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    published_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE platform_sites.tenant_sites
    ADD CONSTRAINT fk_tenant_sites_current_publication
        FOREIGN KEY (current_publication_id) REFERENCES platform_sites.site_publications(id);

CREATE TABLE platform_sites.orchestration_jobs (
    id UUID PRIMARY KEY,
    tenant_site_id UUID NOT NULL REFERENCES platform_sites.tenant_sites(id) ON DELETE CASCADE,
    created_by_user_id UUID NOT NULL REFERENCES platform_auth.platform_users(id),
    job_type VARCHAR(60) NOT NULL,
    job_status VARCHAR(40) NOT NULL,
    request_payload JSONB NOT NULL DEFAULT '{}'::jsonb,
    result_payload JSONB NOT NULL DEFAULT '{}'::jsonb,
    retry_count INTEGER NOT NULL DEFAULT 0,
    last_error_message VARCHAR(4000),
    queued_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMPTZ,
    finished_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE platform_sites.site_deployment_records (
    id UUID PRIMARY KEY,
    tenant_site_id UUID NOT NULL REFERENCES platform_sites.tenant_sites(id) ON DELETE CASCADE,
    site_publication_id UUID REFERENCES platform_sites.site_publications(id),
    orchestration_job_id UUID REFERENCES platform_sites.orchestration_jobs(id),
    triggered_by_user_id UUID NOT NULL REFERENCES platform_auth.platform_users(id),
    action_type VARCHAR(60) NOT NULL,
    deployment_status VARCHAR(40) NOT NULL,
    runtime_spec_snapshot JSONB NOT NULL DEFAULT '{}'::jsonb,
    result_payload JSONB NOT NULL DEFAULT '{}'::jsonb,
    error_message VARCHAR(4000),
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE platform_sites.site_status_history (
    id UUID PRIMARY KEY,
    tenant_site_id UUID NOT NULL REFERENCES platform_sites.tenant_sites(id) ON DELETE CASCADE,
    previous_state VARCHAR(40) NOT NULL,
    new_state VARCHAR(40) NOT NULL,
    transition_reason VARCHAR(255) NOT NULL,
    transition_source VARCHAR(255) NOT NULL,
    details JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE platform_sites.runtime_mutation_history (
    id UUID PRIMARY KEY,
    tenant_site_id UUID NOT NULL REFERENCES platform_sites.tenant_sites(id) ON DELETE CASCADE,
    requested_by_user_id UUID NOT NULL REFERENCES platform_auth.platform_users(id),
    previous_resources JSONB NOT NULL DEFAULT '{}'::jsonb,
    requested_resources JSONB NOT NULL DEFAULT '{}'::jsonb,
    mutation_status VARCHAR(40) NOT NULL,
    result_payload JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE platform_audit.admin_action_history (
    id UUID PRIMARY KEY,
    admin_user_id UUID NOT NULL REFERENCES platform_auth.platform_users(id),
    tenant_account_id UUID REFERENCES platform_core.tenant_accounts(id),
    tenant_site_id UUID REFERENCES platform_sites.tenant_sites(id),
    action_type VARCHAR(60) NOT NULL,
    before_state_summary JSONB NOT NULL DEFAULT '{}'::jsonb,
    after_state_summary JSONB NOT NULL DEFAULT '{}'::jsonb,
    action_details JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE platform_audit.audit_log_entries (
    id UUID PRIMARY KEY,
    actor_user_id UUID REFERENCES platform_auth.platform_users(id),
    subject_type VARCHAR(120) NOT NULL,
    subject_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    event_summary VARCHAR(1000) NOT NULL,
    payload JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_platform_users_email ON platform_auth.platform_users ((lower(email))) WHERE email IS NOT NULL;
CREATE INDEX idx_provider_identities_user ON platform_auth.provider_identities (platform_user_id);
CREATE INDEX idx_provider_identities_metadata_gin ON platform_auth.provider_identities USING GIN (provider_metadata);
CREATE INDEX idx_tenant_memberships_user ON platform_core.tenant_memberships (platform_user_id);
CREATE INDEX idx_tenant_onboarding_state_readiness ON platform_core.tenant_onboarding_states (readiness_state, updated_at DESC);
CREATE INDEX idx_tenant_sites_account_state ON platform_sites.tenant_sites (tenant_account_id, lifecycle_state, updated_at DESC);
CREATE INDEX idx_tenant_sites_updated ON platform_sites.tenant_sites (updated_at DESC);
CREATE INDEX idx_site_runtime_status_payload_gin ON platform_sites.site_runtime_definitions USING GIN (status_payload);
CREATE INDEX idx_site_domains_site ON platform_sites.site_domains (tenant_site_id, primary_domain DESC);
CREATE INDEX idx_site_publications_site_status ON platform_sites.site_publications (tenant_site_id, publication_status, created_at DESC);
CREATE INDEX idx_orchestration_jobs_status ON platform_sites.orchestration_jobs (job_status, queued_at DESC);
CREATE INDEX idx_orchestration_jobs_request_gin ON platform_sites.orchestration_jobs USING GIN (request_payload);
CREATE INDEX idx_site_deployments_site ON platform_sites.site_deployment_records (tenant_site_id, created_at DESC);
CREATE INDEX idx_site_status_history_site ON platform_sites.site_status_history (tenant_site_id, created_at DESC);
CREATE INDEX idx_runtime_mutation_history_site ON platform_sites.runtime_mutation_history (tenant_site_id, created_at DESC);
CREATE INDEX idx_admin_action_history_site ON platform_audit.admin_action_history (tenant_site_id, created_at DESC);
CREATE INDEX idx_admin_action_history_tenant ON platform_audit.admin_action_history (tenant_account_id, created_at DESC);
CREATE INDEX idx_audit_log_subject ON platform_audit.audit_log_entries (subject_type, subject_id, created_at DESC);
