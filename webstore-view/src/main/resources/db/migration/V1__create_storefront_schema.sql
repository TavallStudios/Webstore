CREATE TABLE site_settings (
    id BIGSERIAL PRIMARY KEY,
    site_name VARCHAR(255) NOT NULL,
    logo_path VARCHAR(500),
    favicon_path VARCHAR(500),
    support_email VARCHAR(255) NOT NULL,
    header_cta_text VARCHAR(255),
    shipping_message VARCHAR(1000),
    return_message VARCHAR(1000),
    guarantee_message VARCHAR(1000),
    brand_palette JSONB NOT NULL DEFAULT '{}'::jsonb,
    typography JSONB NOT NULL DEFAULT '{}'::jsonb,
    social_links JSONB NOT NULL DEFAULT '{}'::jsonb,
    seo_defaults JSONB NOT NULL DEFAULT '{}'::jsonb,
    analytics_settings JSONB NOT NULL DEFAULT '{}'::jsonb,
    payment_settings JSONB NOT NULL DEFAULT '{}'::jsonb,
    review_source JSONB NOT NULL DEFAULT '{}'::jsonb,
    footer_content JSONB NOT NULL DEFAULT '{}'::jsonb,
    announcement_bars JSONB NOT NULL DEFAULT '[]'::jsonb,
    promo_banners JSONB NOT NULL DEFAULT '[]'::jsonb,
    trust_badges JSONB NOT NULL DEFAULT '[]'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE feature_flags (
    id BIGSERIAL PRIMARY KEY,
    flag_key VARCHAR(255) NOT NULL UNIQUE,
    display_name VARCHAR(255) NOT NULL,
    module_name VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    description VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE content_pages (
    id BIGSERIAL PRIMARY KEY,
    slug VARCHAR(255) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    page_type VARCHAR(40) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    layout_configuration JSONB NOT NULL DEFAULT '{}'::jsonb,
    seo_metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE page_sections (
    id BIGSERIAL PRIMARY KEY,
    content_page_id BIGINT NOT NULL REFERENCES content_pages(id) ON DELETE CASCADE,
    section_key VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    section_type VARCHAR(255) NOT NULL,
    placement VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    position INTEGER NOT NULL DEFAULT 0,
    mobile_position INTEGER NOT NULL DEFAULT 0,
    configuration JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    slug VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    tagline VARCHAR(500),
    short_description VARCHAR(1000),
    full_description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    price NUMERIC(12,2) NOT NULL,
    compare_at_price NUMERIC(12,2),
    currency VARCHAR(10) NOT NULL,
    sku VARCHAR(255) NOT NULL UNIQUE,
    barcode VARCHAR(255),
    inventory_tracking BOOLEAN NOT NULL DEFAULT TRUE,
    inventory_quantity INTEGER NOT NULL DEFAULT 0,
    subscription_available BOOLEAN NOT NULL DEFAULT FALSE,
    bundle_eligible BOOLEAN NOT NULL DEFAULT FALSE,
    shipping_profile VARCHAR(255),
    tax_category VARCHAR(255),
    media_assets JSONB NOT NULL DEFAULT '[]'::jsonb,
    badges JSONB NOT NULL DEFAULT '[]'::jsonb,
    highlights JSONB NOT NULL DEFAULT '[]'::jsonb,
    benefits JSONB NOT NULL DEFAULT '[]'::jsonb,
    faq_entries JSONB NOT NULL DEFAULT '[]'::jsonb,
    guarantee_text VARCHAR(1000),
    review_summary JSONB NOT NULL DEFAULT '{}'::jsonb,
    seo_metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    custom_attributes JSONB NOT NULL DEFAULT '{}'::jsonb,
    presentation_metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE product_variants (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    option_group VARCHAR(255) NOT NULL,
    option_value VARCHAR(255) NOT NULL,
    sku VARCHAR(255) NOT NULL,
    price_override NUMERIC(12,2),
    compare_at_price_override NUMERIC(12,2),
    inventory_quantity INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    configuration JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE bundle_offers (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    discount_type VARCHAR(255) NOT NULL,
    discount_value NUMERIC(12,2) NOT NULL,
    bundle_quantity INTEGER NOT NULL DEFAULT 1,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    placement VARCHAR(255),
    configuration JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE subscription_plans (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    interval_label VARCHAR(255) NOT NULL,
    frequency_days INTEGER NOT NULL,
    discount_percent INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    configuration JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE upsell_offers (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    add_on_product_name VARCHAR(255),
    price_delta NUMERIC(12,2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    placement VARCHAR(255),
    configuration JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE promotions (
    id BIGSERIAL PRIMARY KEY,
    offer_code VARCHAR(255),
    display_name VARCHAR(255) NOT NULL,
    promotion_type VARCHAR(40) NOT NULL,
    automatic BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    discount_value NUMERIC(12,2) NOT NULL,
    free_shipping BOOLEAN NOT NULL DEFAULT FALSE,
    configuration JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE shopping_carts (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(40) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    email VARCHAR(255),
    subtotal_amount NUMERIC(12,2) NOT NULL,
    discount_amount NUMERIC(12,2) NOT NULL,
    shipping_amount NUMERIC(12,2) NOT NULL,
    tax_amount NUMERIC(12,2) NOT NULL,
    total_amount NUMERIC(12,2) NOT NULL,
    cart_attributes JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE cart_line_items (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL REFERENCES shopping_carts(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    variant_id BIGINT REFERENCES product_variants(id),
    quantity INTEGER NOT NULL,
    product_name_snapshot VARCHAR(255) NOT NULL,
    sku_snapshot VARCHAR(255) NOT NULL,
    purchase_type VARCHAR(40) NOT NULL,
    unit_price NUMERIC(12,2) NOT NULL,
    line_total NUMERIC(12,2) NOT NULL,
    line_attributes JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE checkout_sessions (
    id BIGSERIAL PRIMARY KEY,
    cart_id BIGINT NOT NULL REFERENCES shopping_carts(id),
    provider VARCHAR(40) NOT NULL,
    status VARCHAR(40) NOT NULL,
    external_reference VARCHAR(255) NOT NULL UNIQUE,
    amount NUMERIC(12,2) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    redirect_url VARCHAR(1000),
    checkout_data JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE customer_orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(255) NOT NULL UNIQUE,
    checkout_session_id BIGINT NOT NULL UNIQUE REFERENCES checkout_sessions(id),
    status VARCHAR(40) NOT NULL,
    payment_status VARCHAR(40) NOT NULL,
    fulfillment_status VARCHAR(40) NOT NULL,
    email VARCHAR(255) NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    subtotal_amount NUMERIC(12,2) NOT NULL,
    discount_amount NUMERIC(12,2) NOT NULL,
    shipping_amount NUMERIC(12,2) NOT NULL,
    tax_amount NUMERIC(12,2) NOT NULL,
    total_amount NUMERIC(12,2) NOT NULL,
    discount_code VARCHAR(255),
    shipping_address JSONB NOT NULL DEFAULT '{}'::jsonb,
    billing_address JSONB NOT NULL DEFAULT '{}'::jsonb,
    order_attributes JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_line_items (
    id BIGSERIAL PRIMARY KEY,
    customer_order_id BIGINT NOT NULL REFERENCES customer_orders(id) ON DELETE CASCADE,
    product_id BIGINT REFERENCES products(id),
    variant_id BIGINT REFERENCES product_variants(id),
    product_name VARCHAR(255) NOT NULL,
    sku VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(12,2) NOT NULL,
    line_total NUMERIC(12,2) NOT NULL,
    line_attributes JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE payment_records (
    id BIGSERIAL PRIMARY KEY,
    customer_order_id BIGINT REFERENCES customer_orders(id),
    checkout_session_id BIGINT NOT NULL REFERENCES checkout_sessions(id),
    provider VARCHAR(40) NOT NULL,
    payment_status VARCHAR(40) NOT NULL,
    external_payment_reference VARCHAR(255) NOT NULL,
    provider_event_id VARCHAR(255),
    amount NUMERIC(12,2) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    provider_payload JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE shipments (
    id BIGSERIAL PRIMARY KEY,
    customer_order_id BIGINT NOT NULL REFERENCES customer_orders(id) ON DELETE CASCADE,
    carrier VARCHAR(255),
    tracking_number VARCHAR(255),
    status VARCHAR(40) NOT NULL,
    shipped_at TIMESTAMPTZ,
    delivered_at TIMESTAMPTZ,
    tracking_payload JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tracking_events (
    id BIGSERIAL PRIMARY KEY,
    shipment_id BIGINT NOT NULL REFERENCES shipments(id) ON DELETE CASCADE,
    event_timestamp TIMESTAMPTZ NOT NULL,
    status VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    message VARCHAR(1000) NOT NULL,
    event_payload JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE webhook_event_logs (
    id BIGSERIAL PRIMARY KEY,
    provider VARCHAR(40) NOT NULL,
    event_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    payload JSONB NOT NULL DEFAULT '{}'::jsonb,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_webhook_event_provider UNIQUE (provider, event_id)
);

CREATE INDEX idx_page_sections_page_position ON page_sections (content_page_id, position);
CREATE INDEX idx_product_variants_product ON product_variants (product_id, sort_order);
CREATE INDEX idx_cart_line_items_cart ON cart_line_items (cart_id);
CREATE INDEX idx_checkout_sessions_external_reference ON checkout_sessions (external_reference);
CREATE INDEX idx_customer_orders_order_number ON customer_orders (order_number);
CREATE INDEX idx_shipments_order ON shipments (customer_order_id);
