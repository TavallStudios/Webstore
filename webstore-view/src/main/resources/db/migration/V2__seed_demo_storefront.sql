INSERT INTO site_settings (
    id, site_name, logo_path, favicon_path, support_email, header_cta_text, shipping_message, return_message,
    guarantee_message, brand_palette, typography, social_links, seo_defaults, analytics_settings, payment_settings,
    review_source, footer_content, announcement_bars, promo_banners, trust_badges
) VALUES (
    1,
    'Atlas Bottle',
    '/media/atlas-wordmark.svg',
    '/media/atlas-wordmark.svg',
    'support@atlasbottle.test',
    'Shop Atlas',
    'Free shipping on orders over $50 and carbon-neutral delivery on every order.',
    'Try it for 30 days. If it does not make your routine easier, we refund it.',
    'Backed by a no-drip guarantee and support from real humans.',
    $${
      "background":"#f7efe4",
      "surface":"#fffaf3",
      "ink":"#1d1a16",
      "accent":"#c85f35",
      "accentDeep":"#8c3514",
      "muted":"#6d6258"
    }$$::jsonb,
    $${
      "headline":"Fraunces, Georgia, serif",
      "body":"IBM Plex Sans, Segoe UI, sans-serif",
      "mono":"IBM Plex Mono, Consolas, monospace"
    }$$::jsonb,
    $${
      "instagram":"https://instagram.com/atlasbottle",
      "tiktok":"https://tiktok.com/@atlasbottle",
      "youtube":"https://youtube.com/@atlasbottle"
    }$$::jsonb,
    $${
      "titleTemplate":"%s | Atlas Bottle",
      "description":"A single-product storefront engine demo focused on conversion, clarity, and SSR performance.",
      "openGraphImage":"/media/atlas-og.svg"
    }$$::jsonb,
    $${
      "ga4Id":"G-ATLAS123",
      "metaPixelId":"1234567890",
      "conversionHooks":["purchase","begin_checkout"]
    }$$::jsonb,
    $${
      "stripeEnabled":true,
      "paypalEnabled":true,
      "mode":"sandbox",
      "providerOrder":["STRIPE","PAYPAL"],
      "sandboxNotice":"Providers are wired behind simulated hosted sessions until real credentials are supplied."
    }$$::jsonb,
    $${
      "label":"4.9 / 5 average",
      "source":"from 4,200 verified buyers"
    }$$::jsonb,
    $${
      "columns":[
        {"title":"Support","links":[{"label":"Email us","href":"mailto:support@atlasbottle.test"},{"label":"Track an order","href":"/tracking"}]},
        {"title":"Policies","links":[{"label":"Shipping","href":"#shipping"},{"label":"Returns","href":"#returns"}]}
      ],
      "finePrint":"Atlas Bottle is a demo storefront engine with editable configuration stored in PostgreSQL."
    }$$::jsonb,
    $$[
      {"label":"Spring launch","message":"Save 10% with ATLAS10 through Friday","tone":"accent"}
    ]$$::jsonb,
    $$[
      {"placement":"after-hero","eyebrow":"Limited run","headline":"Copper Clay finish ships this week","body":"Small-batch finish available while inventory lasts."}
    ]$$::jsonb,
    $$[
      {"label":"Free shipping","detail":"Orders over $50"},
      {"label":"30-day returns","detail":"No-hassle trial"},
      {"label":"4.9 rating","detail":"4,200 verified buyers"}
    ]$$::jsonb
);

INSERT INTO feature_flags (id, flag_key, display_name, module_name, enabled, description) VALUES
    (1, 'subscriptions_enabled', 'Subscriptions', 'Offers', TRUE, 'Enables subscribe-and-save offers on product and checkout surfaces.'),
    (2, 'bundles_enabled', 'Bundles', 'Offers', TRUE, 'Enables bundle cards and bundle cart pricing.'),
    (3, 'post_purchase_upsells_enabled', 'Post-purchase upsells', 'Offers', TRUE, 'Keeps upsell placement visible on confirmation flows.'),
    (4, 'countdown_banner_enabled', 'Countdown banner', 'Merchandising', TRUE, 'Displays urgency sections when configured on pages.'),
    (5, 'tracking_lookup_enabled', 'Tracking lookup', 'Operations', TRUE, 'Allows customer order tracking lookup.'),
    (6, 'draft_preview_enabled', 'Draft preview', 'Content', TRUE, 'Allows preview links from the page editor.');

INSERT INTO content_pages (id, slug, title, page_type, active, layout_configuration, seo_metadata) VALUES
    (1, 'home', 'Atlas Bottle Homepage', 'HOMEPAGE', TRUE,
     $${
       "heroLayout":"media-right",
       "stickyCta":true,
       "mobileSectionOrder":"default"
     }$$::jsonb,
     $${"title":"Atlas Bottle | Daily hydration without the leak chaos"}$$::jsonb),
    (2, 'product-default', 'Atlas Bottle Product Page', 'PRODUCT', TRUE,
     $${
       "galleryLayout":"stacked",
       "stickyAddToCart":true,
       "detailsLayout":"stacked"
     }$$::jsonb,
     $${"title":"Shop Atlas Bottle"}$$::jsonb);

INSERT INTO page_sections (id, content_page_id, section_key, display_name, section_type, placement, active, position, mobile_position, configuration) VALUES
    (1, 1, 'hero', 'Homepage Hero', 'hero', 'top', TRUE, 10, 10,
     $${
       "eyebrow":"Hydration without the drip drama",
       "title":"The insulated bottle built for desks, commutes, and no-spill mornings.",
       "body":"Atlas keeps coffee hot, water cold, and bags dry with a lock-top lid that seals cleanly.",
       "ctaLabel":"Shop the bottle",
       "ctaHref":"/products/atlas-bottle",
       "secondaryLabel":"See how it works",
       "secondaryHref":"#how-it-works",
       "mediaPath":"/media/atlas-bottle-hero.svg"
     }$$::jsonb),
    (2, 1, 'trust-strip', 'Trust Strip', 'trust-strip', 'after-hero', TRUE, 20, 20,
     $${"headline":"Trusted by commuters, parents, and gym bags that stayed dry for once."}$$::jsonb),
    (3, 1, 'benefits', 'Benefits Row', 'benefits', 'body', TRUE, 30, 30,
     $${
       "title":"Fast proof before the details",
       "items":[
         {"title":"Lock-top lid","body":"Flip, lock, toss in your bag."},
         {"title":"18-hour cold hold","body":"Still cold after the afternoon slump."},
         {"title":"Cup-holder ready","body":"Slim enough for real-world commutes."}
       ]
     }$$::jsonb),
    (4, 1, 'lifestyle', 'Lifestyle Story', 'lifestyle', 'body', TRUE, 40, 40,
     $${
       "title":"Designed for the part of your routine that does not need more friction.",
       "body":"Atlas replaces sticky tumbler lids and bulkier flasks with one clean daily carry that looks premium on the desk and survives the backpack.",
       "mediaPath":"/media/atlas-lifestyle.svg"
     }$$::jsonb),
    (5, 1, 'reviews', 'Review Highlights', 'reviews', 'body', TRUE, 50, 50,
     $${
       "title":"People mention the same thing over and over",
       "items":[
         {"quote":"I stopped wrapping my bottle in a tote because this one just seals.","name":"Ariana P."},
         {"quote":"The lock click is weirdly satisfying and it actually fits in my car.","name":"Mika D."},
         {"quote":"Looks nicer than most insulated bottles and holds temp all day.","name":"Jordan T."}
       ]
     }$$::jsonb),
    (6, 1, 'comparison', 'Comparison Block', 'comparison', 'body', TRUE, 60, 60,
     $${
       "title":"Why Atlas wins the desk-to-gym category",
       "rows":[
         {"feature":"Leak-proof lock lid","atlas":"Yes","other":"Usually not"},
         {"feature":"Cup-holder footprint","atlas":"Yes","other":"Bulky"},
         {"feature":"Warm premium finish","atlas":"Yes","other":"Utility only"}
       ]
     }$$::jsonb),
    (7, 1, 'faq-preview', 'FAQ Preview', 'faq-preview', 'body', TRUE, 70, 70,
     $${"title":"Questions people ask before buying","limit":3}$$::jsonb),
    (8, 1, 'cta-repeat', 'Repeat CTA', 'cta', 'footer', TRUE, 80, 80,
     $${
       "title":"Ready for the bottle you stop thinking about once it works?",
       "body":"See pricing, finishes, subscribe-and-save, and bundle options on the product page.",
       "ctaLabel":"Go to the product page",
       "ctaHref":"/products/atlas-bottle"
     }$$::jsonb),
    (9, 2, 'product-proof', 'Product Proof', 'reviews', 'below-buy-box', TRUE, 20, 20,
     $${
       "title":"Verified buyer proof",
       "items":[
         {"quote":"No leaks in a week of train commutes.","name":"Nina R."},
         {"quote":"Bought one, then added the twin pack for my partner.","name":"Caleb F."}
       ]
     }$$::jsonb),
    (10, 2, 'product-faq', 'Product FAQ', 'faq-preview', 'body', TRUE, 30, 30,
     $${"title":"Still deciding? Start here","limit":6}$$::jsonb),
    (11, 2, 'product-specs', 'Specs Section', 'specs', 'body', TRUE, 40, 40,
     $${
       "title":"Materials and daily details",
       "items":[
         {"label":"Capacity","value":"20 oz"},
         {"label":"Body","value":"Double-wall stainless steel"},
         {"label":"Lid","value":"Lock-top seal with soft carry loop"},
         {"label":"Care","value":"Bottle dishwasher safe, lid hand wash"}
       ]
     }$$::jsonb),
    (12, 2, 'product-cta', 'Sticky CTA Backup', 'cta', 'footer', TRUE, 50, 50,
     $${
       "title":"Pick your finish and get it on the way.",
       "body":"Use the bundle cards if you want a desk-plus-gym setup.",
       "ctaLabel":"Jump to buy box",
       "ctaHref":"#buy-box"
     }$$::jsonb);

INSERT INTO products (
    id, slug, name, tagline, short_description, full_description, active, price, compare_at_price, currency, sku,
    barcode, inventory_tracking, inventory_quantity, subscription_available, bundle_eligible, shipping_profile, tax_category,
    media_assets, badges, highlights, benefits, faq_entries, guarantee_text, review_summary, seo_metadata,
    custom_attributes, presentation_metadata
) VALUES (
    1,
    'atlas-bottle',
    'Atlas Bottle',
    'Premium daily bottle with a lock-top seal.',
    'A leak-proof insulated bottle sized for real commutes and clean desks.',
    'Atlas Bottle is a reusable daily carry built around one job: keeping temperature and avoiding leaks without turning into a giant outdoor flask.',
    TRUE,
    42.00,
    52.00,
    'USD',
    'ATLAS-BOTTLE-20OZ',
    '012345678905',
    TRUE,
    128,
    TRUE,
    TRUE,
    'STANDARD',
    'GENERAL_MERCH',
    $$[
      {"path":"/media/atlas-bottle-hero.svg","alt":"Atlas Bottle hero shot"},
      {"path":"/media/atlas-bottle-side.svg","alt":"Atlas Bottle side profile"},
      {"path":"/media/atlas-bottle-lid.svg","alt":"Atlas Bottle lock-top lid close-up"}
    ]$$::jsonb,
    $$["Best Seller","Leak-Proof","Subscribe & Save"]$$::jsonb,
    $$["Leak-proof lock lid","18-hour cold hold","Cup-holder fit","Premium matte finish"]$$::jsonb,
    $$["Stops bag leaks","Looks at home on your desk","Keeps drinks at temp for long stretches"]$$::jsonb,
    $$[
      {"question":"Is it dishwasher safe?","answer":"The bottle is dishwasher safe; hand wash the lid for best seal life.","homepageVisible":true},
      {"question":"Will it fit a standard cup holder?","answer":"Yes. The base was sized for commuter cars and stroller holders.","homepageVisible":true},
      {"question":"Does the lock lid actually stop leaks?","answer":"Yes. The lid uses a seal-and-lock design built for bag carry.","homepageVisible":true},
      {"question":"Can I subscribe and save?","answer":"Yes. Monthly and bi-monthly plans are available.","homepageVisible":false}
    ]$$::jsonb,
    'If it drips in normal use during the first 30 days, we replace it or refund it.',
    $${"rating":"4.9","count":"4200","label":"Verified buyers"}$$::jsonb,
    $${"title":"Atlas Bottle | Leak-proof insulated bottle","description":"Shop Atlas Bottle with bundles, subscriptions, FAQs, and reassurance built in."}$$::jsonb,
    $${"materials":["18/8 stainless steel","BPA-free lid"],"temperatureRetention":{"hot":"8h","cold":"18h"}}$$::jsonb,
    $${"galleryLayout":"stacked","showCompareAtPrice":true,"showQuantitySelector":true,"stickyAddToCart":true}$$::jsonb
);

INSERT INTO product_variants (id, product_id, option_group, option_value, sku, price_override, compare_at_price_override, inventory_quantity, active, sort_order, configuration) VALUES
    (1, 1, 'Finish', 'Copper Clay', 'ATLAS-COPPER', 42.00, 52.00, 36, TRUE, 10, $${
      "swatch":"#ab5a3b"
    }$$::jsonb),
    (2, 1, 'Finish', 'Midnight Graphite', 'ATLAS-GRAPHITE', 44.00, 54.00, 92, TRUE, 20, $${
      "swatch":"#2b2f33"
    }$$::jsonb);

INSERT INTO bundle_offers (id, product_id, name, description, discount_type, discount_value, bundle_quantity, active, placement, configuration) VALUES
    (1, 1, 'Twin Pack', 'Two bottles for the desk-and-gym setup.', 'PERCENTAGE', 12.00, 2, TRUE, 'buy-box', $${
      "badge":"Most popular"
    }$$::jsonb),
    (2, 1, 'Team Shelf', 'Three bottles for home, office, and backup.', 'PERCENTAGE', 18.00, 3, TRUE, 'below-buy-box', $${
      "badge":"Best value"
    }$$::jsonb);

INSERT INTO subscription_plans (id, product_id, name, interval_label, frequency_days, discount_percent, active, configuration) VALUES
    (1, 1, 'Monthly refresh', 'Every 30 days', 30, 8, TRUE, $${
      "description":"Ideal if you rotate bottles or gift them regularly."
    }$$::jsonb),
    (2, 1, 'Bi-monthly refresh', 'Every 60 days', 60, 10, TRUE, $${
      "description":"Best for slower replacement cycles."
    }$$::jsonb);

INSERT INTO upsell_offers (id, product_id, title, description, add_on_product_name, price_delta, active, placement, configuration) VALUES
    (1, 1, 'Travel cleaning kit', 'Pocket brush and drying stand for the lid assembly.', 'Atlas Cleaning Kit', 9.00, TRUE, 'post-buy-box', $${
      "cta":"Add the cleaning kit"
    }$$::jsonb);

INSERT INTO promotions (id, offer_code, display_name, promotion_type, automatic, active, discount_value, free_shipping, configuration) VALUES
    (1, 'ATLAS10', 'Spring launch 10% off', 'PERCENTAGE', FALSE, TRUE, 10.00, FALSE, $${
      "appliesTo":"product"
    }$$::jsonb),
    (2, NULL, 'Bundle free shipping', 'FREE_SHIPPING', TRUE, TRUE, 0.00, TRUE, $${
      "minimumQuantity":2
    }$$::jsonb);

INSERT INTO shopping_carts (id, token, status, currency, email, subtotal_amount, discount_amount, shipping_amount, tax_amount, total_amount, cart_attributes) VALUES
    (1, 'seed-cart', 'CONVERTED', 'USD', 'customer@example.com', 84.00, 8.40, 0.00, 0.00, 75.60, $${
      "source":"seed"
    }$$::jsonb);

INSERT INTO cart_line_items (id, cart_id, product_id, variant_id, quantity, product_name_snapshot, sku_snapshot, purchase_type, unit_price, line_total, line_attributes) VALUES
    (1, 1, 1, 1, 2, 'Atlas Bottle', 'ATLAS-COPPER', 'ONE_TIME', 42.00, 84.00, $${
      "bundleOfferId":1
    }$$::jsonb);

INSERT INTO checkout_sessions (id, cart_id, provider, status, external_reference, amount, currency, redirect_url, checkout_data) VALUES
    (1, 1, 'STRIPE', 'COMPLETED', 'seed-checkout-1', 75.60, 'USD', '/checkout/sessions/seed-checkout-1', $${
      "customerName":"Morgan Lee",
      "email":"customer@example.com",
      "addressLine1":"44 Market Street",
      "city":"Oakland",
      "state":"CA",
      "postalCode":"94607",
      "country":"US",
      "discountCode":"ATLAS10"
    }$$::jsonb);

INSERT INTO customer_orders (id, order_number, checkout_session_id, status, payment_status, fulfillment_status, email, customer_name, currency, subtotal_amount, discount_amount, shipping_amount, tax_amount, total_amount, discount_code, shipping_address, billing_address, order_attributes) VALUES
    (1, 'WS-100001', 1, 'FULFILLED', 'PAID', 'DELIVERED', 'customer@example.com', 'Morgan Lee', 'USD', 84.00, 8.40, 0.00, 0.00, 75.60, 'ATLAS10',
     $${
       "addressLine1":"44 Market Street",
       "city":"Oakland",
       "state":"CA",
       "postalCode":"94607",
       "country":"US"
     }$$::jsonb,
     $${
       "addressLine1":"44 Market Street",
       "city":"Oakland",
       "state":"CA",
       "postalCode":"94607",
       "country":"US"
     }$$::jsonb,
     $${
       "notes":"Seeded order for tracking and admin demos."
     }$$::jsonb);

INSERT INTO order_line_items (id, customer_order_id, product_id, variant_id, product_name, sku, quantity, unit_price, line_total, line_attributes) VALUES
    (1, 1, 1, 1, 'Atlas Bottle', 'ATLAS-COPPER', 2, 42.00, 84.00, $${
      "bundleOfferId":1
    }$$::jsonb);

INSERT INTO payment_records (id, customer_order_id, checkout_session_id, provider, payment_status, external_payment_reference, provider_event_id, amount, currency, provider_payload) VALUES
    (1, 1, 1, 'STRIPE', 'PAID', 'pi_seed_001', 'evt_seed_checkout_paid', 75.60, 'USD', $${
      "mode":"sandbox",
      "status":"paid"
    }$$::jsonb);

INSERT INTO shipments (id, customer_order_id, carrier, tracking_number, status, shipped_at, delivered_at, tracking_payload) VALUES
    (1, 1, 'UPS', '1Z999AA10123456784', 'DELIVERED', NOW() - INTERVAL '5 days', NOW() - INTERVAL '2 days', $${
      "carrierUrl":"https://wwwapps.ups.com/WebTracking/track?track=yes&trackNums=1Z999AA10123456784"
    }$$::jsonb);

INSERT INTO tracking_events (id, shipment_id, event_timestamp, status, location, message, event_payload) VALUES
    (1, 1, NOW() - INTERVAL '5 days', 'Label created', 'Oakland, CA', 'Shipping label created.', '{}'::jsonb),
    (2, 1, NOW() - INTERVAL '4 days', 'In transit', 'Sacramento, CA', 'Package departed sorting hub.', '{}'::jsonb),
    (3, 1, NOW() - INTERVAL '2 days', 'Delivered', 'Oakland, CA', 'Delivered to front desk.', '{}'::jsonb);

SELECT setval('site_settings_id_seq', 1, TRUE);
SELECT setval('feature_flags_id_seq', 6, TRUE);
SELECT setval('content_pages_id_seq', 2, TRUE);
SELECT setval('page_sections_id_seq', 12, TRUE);
SELECT setval('products_id_seq', 1, TRUE);
SELECT setval('product_variants_id_seq', 2, TRUE);
SELECT setval('bundle_offers_id_seq', 2, TRUE);
SELECT setval('subscription_plans_id_seq', 2, TRUE);
SELECT setval('upsell_offers_id_seq', 1, TRUE);
SELECT setval('promotions_id_seq', 2, TRUE);
SELECT setval('shopping_carts_id_seq', 1, TRUE);
SELECT setval('cart_line_items_id_seq', 1, TRUE);
SELECT setval('checkout_sessions_id_seq', 1, TRUE);
SELECT setval('customer_orders_id_seq', 1, TRUE);
SELECT setval('order_line_items_id_seq', 1, TRUE);
SELECT setval('payment_records_id_seq', 1, TRUE);
SELECT setval('shipments_id_seq', 1, TRUE);
SELECT setval('tracking_events_id_seq', 3, TRUE);
