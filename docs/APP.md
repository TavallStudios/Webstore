
You are working inside an existing or new Spring Boot storefront project. Build a reusable, performance-focused single-product storefront engine using:

- Spring Boot, because the backend should remain a structured server-side application with explicit control over routing, rendering, payments, configuration, and business logic.
- Thymeleaf for SSR HTML rendering, because initial page load speed, SEO, and low frontend overhead matter more here than SPA complexity.
- TypeScript/JavaScript only where interactivity is needed, because we want progressive enhancement rather than turning the whole storefront into a client-rendered app for no reason.
- PostgreSQL as the only database, because the commerce layer and flexible configuration layer should live in one durable system instead of split across multiple storage technologies.
- JSONB for flexible/configurable page/content/layout data, because page composition and promotional configuration are flexible, but still benefit from being stored near the rest of the business data.
- Static assets served from the app/server filesystem, because the site should be able to serve images, icons, badges, and marketing media without relying on a bloated external asset management platform.
- Stripe and PayPal for payments, because the storefront should support mainstream purchase flows and customer expectations from day one.
- Cloudflare-friendly caching headers and static asset behavior, because assets should cache cleanly and the site should remain fast under normal edge-cached delivery.
- No MongoDB, because we are intentionally consolidating flexible page/configuration data into PostgreSQL JSONB.
- No external CMS, because this should be a focused storefront engine with lightweight internal admin/configuration tooling rather than a generic content management platform.
- No SPA-first frontend, because the site should render quickly on first request and should not depend on hydration for core browsing and purchase flows.
- No React/Vue unless already present and absolutely required for a tiny isolated widget, because the storefront must remain primarily server-rendered and maintainable.
- No inner/nested classes, because code structure should remain explicit and consistent with the project’s broader conventions.
- Use explicit class, method, and variable names, because vague naming creates brittle code and makes maintenance worse.
- Keep methods focused and readable, because this is a business system, not a contest to see how much logic can be stuffed into one service.
- Do not produce vague abstractions or “demo-only” code, because the goal is an actual engine, not architecture theater.

IMPORTANT ARCHITECTURE DECISION:
We are NOT using MongoDB. Anything previously planned for Mongo must be moved either:
1. into normalized PostgreSQL tables, or
2. into PostgreSQL JSONB columns where the data is flexible, presentation-driven, configuration-oriented, or composed from reusable UI/content blocks.

The business rule is:
- PostgreSQL owns commerce and operational truth, meaning products, orders, carts, customers, payments, inventory, subscriptions, bundles, shipping, and tracking should be modeled relationally when they represent real business entities or operational records.
- JSONB owns flexible rendering/configuration/content structure, meaning homepage section ordering, banner positioning, CTA configuration, trust badge layouts, mobile ordering overrides, or other presentation-focused data should be stored flexibly without pretending it needs a dedicated relational schema for every minor UI variation.
- Do not blur product/business data into random JSON blobs when it should be queryable relational data, because pricing, inventory, subscriptions, and shipment records should not be trapped inside “misc config” blobs that become impossible to reason about later.

PRIMARY GOAL:
Build a reusable single-product storefront engine where:
- the homepage acts like a high-converting landing page / product pitch page, meaning it should emotionally sell the product and guide the user deeper into the funnel without trying to be the final dense purchase interface unless configured to do so.
- the real product page handles pricing, variants, checkout entry, detailed objections, and conversion, meaning the shopper can review actual commercial details and purchase with confidence.
- nearly everything visual and promotional is configurable, because we want to reuse this engine across multiple products or store variants without rewriting templates every time marketing changes its mind.
- we can later reuse the engine for additional products or stores without rewriting core logic, because the point is to build a focused reusable platform, not a one-off hardcoded store.

DO NOT ask broad planning questions.
Inspect the repository, infer structure, and make grounded decisions.
If something is missing, implement the best reasonable default and continue.
Do not stop at a plan only.
Actually create/modify files, schema, templates, services, and configuration.

==================================================
HIGH-LEVEL PRODUCT / STORE BEHAVIOR
==================================================

We want a storefront system inspired by strong single-product conversion sites, but not cloned.

The UX model:
- Homepage:
    - emotionally sells the product, meaning it should lead with a strong promise, strong visuals, and a clear CTA before the user gets dragged into operational detail.
    - large hero section, because this is where the product’s promise, identity, and first impression are established.
    - strong CTA, because the page should immediately give motivated visitors a path toward the product page or direct purchase flow.
    - usually no visible price in the hero by default, because the homepage should lead with problem/benefit/value first unless a specific test/config says price should appear early.
    - acts like a long-form sales page, meaning it can contain benefits, media, social proof, comparisons, guarantees, FAQs, banners, and urgency messaging in a persuasive sequence.
    - supports multiple persuasive sections, because the same product may need different persuasion patterns depending on audience and campaign source.
- Product page:
    - contains actual price, because the product page is where real commercial clarity needs to exist.
    - buy/add-to-cart actions, because this is the page responsible for converting warmed-up users into buyers.
    - variants/options if present, because some products may have size, color, pack quantity, bundle, or subscription-related choices even in a “single-product” store.
    - reviews, FAQs, guarantees, shipping/returns, and comparison logic, because the product page should answer objections and remove purchase anxiety.
    - should be the real conversion page, meaning it should combine purchase functionality with enough proof and clarity to close the deal.
- Both pages should share reusable section rendering and product-backed content, because duplication will become a maintenance nightmare if homepage and product page components diverge without reason.
- Section order should be configurable, because different campaign strategies may require different persuasion sequences.
- Banner positions should be configurable, because “announcement at top”, “as seen on after hero”, or “sale banner before reviews” should be adjustable without code changes.
- “As Seen On”, sale banners, trust strips, and announcement bars must not be hardcoded to one position, because what converts best will vary and the engine must support experimentation and reuse.

EXAMPLE STOREFRONT FLOW:
A clean single-product store should roughly feel like this:
1. User lands on homepage hero with a sharp promise, product image/video, and one main CTA such as “Shop Now” or “See How It Works”.
2. User scrolls into fast proof: trust badges, press strip, feature icons, problem/solution messaging, comparison block, short testimonial highlights.
3. User hits repeated CTA sections that lead to the product page rather than being forced to hunt through navigation.
4. User reaches the product page where price, variants, reviews, FAQ, guarantee, shipping details, bundle offers, subscription offers, and sticky add-to-cart all work cleanly.
5. User adds to cart, optionally accepts an upsell or bundle suggestion, and checks out via Stripe or PayPal.
6. After purchase, the user sees order confirmation, can later track shipment, and can manage subscription links if relevant.

EXAMPLE HOMEPAGE FEEL:
The homepage should feel like a focused persuasive landing page, not like a cluttered category page or a blog homepage.
A good homepage example shape might be:
- hero with product promise and primary CTA
- trust strip or “as seen on” section placed in a non-disruptive but visible location
- benefits icon row
- image-text section showing the product in context
- testimonial grid or highlighted review
- comparison block showing why this product beats alternatives
- FAQ preview
- repeated CTA
  The exact content, order, visibility, and styling should be configurable.

EXAMPLE PRODUCT PAGE FEEL:
The product page should feel like the real “decision” page:
- product gallery/media at top
- name/tagline/highlights
- actual price and compare-at price if enabled
- subscription toggle if available
- bundle offers if available
- sticky add-to-cart on scroll
- concise review summary near buy box
- shipping / guarantee / returns reassurance
- fuller reviews lower down
- FAQ
- product details/specs/materials
- relevant upsells or add-ons
  This should all be controllable by configuration rather than hardcoded assumptions.

==================================================
WHAT MUST BE CONFIGURABLE
==================================================

Make as much as possible configurable without turning the system into chaos.

At minimum, make these configurable via DB-backed settings/content models:

GLOBAL / SITE CONFIG
- site name, because the engine should support reuse across brands or store instances without code edits.
- brand colors, because design identity should not be hardcoded into templates.
- typography choices if supported, because different products may need different brand presentation while still using a controlled theme system.
- logo paths, because branding assets should be replaceable through configuration.
- favicon, because small identity details still matter and should not require template edits.
- global support email / contact info, because purchase confidence and customer service expectations need editable site-wide contact data.
- social links, because footer/header and trust content may reference brand social presence.
- default SEO metadata, because title templates, descriptions, canonical hints, and social metadata should have configurable defaults.
- analytics script hooks / IDs, because tracking setup often changes between environments and stores.
- global announcement bars, because site-wide notices such as shipping promotions or seasonal notices should be manageable centrally.
- global promo banners, because persistent promotional UI should not be hardcoded into page templates.
- default trust badges, because common trust elements should be reusable across pages.
- header CTA text, because the top navigation CTA may vary by campaign or product type.
- footer content, because legal, informational, and support content should be editable.
- return/refund messaging, because this is policy-oriented content that may change.
- shipping messaging, because thresholds, timelines, and promises vary.
- guarantee messaging, because confidence-building text should be centrally manageable.
- countdown banner enable/disable, because urgency widgets should be optional and configurable.
- free shipping threshold text, because promotional messaging needs to reflect actual business rules.
- global review source text / badges, because stores often want editable “4.8/5 from X customers” style trust copy.
- feature flags for plugin-like features, because modules such as bundles, subscriptions, advanced tracking, or post-purchase upsells should be turn-on/turn-off capable without ripping code apart.

HOMEPAGE CONFIG
- hero layout, because some products may need image-left/text-right while others need video-first or centered hero treatments.
- hero media, because the primary asset could be an image, short looped video, or rotating feature frame.
- hero title/subtitle, because the pitch needs to be editable for campaigns or product variants.
- CTA text and destination, because homepage conversion intent may point to product page, checkout shortcut, or learn-more section.
- optional hidden price behavior, because some homepage flows intentionally avoid early pricing while others want immediate transparency.
- section ordering, because conversion layout should be testable and configurable.
- section enable/disable, because the same base template should allow sections to be turned off rather than deleted from code.
- social proof strip, because some campaigns may want immediate trust reinforcement near the top.
- “as seen on” strip position, because this is useful but should not always sit above the fold.
- benefit icon rows, because quick skim-friendly persuasion blocks should be editable.
- media sections, because demonstration content often changes.
- review sections, because featured proof may vary between campaigns.
- FAQ preview sections, because homepage may show selected objections before sending people deeper.
- comparison sections, because some products win best through contrast against alternatives.
- guarantee sections, because reassurance content is often part of the sell.
- banner placements, because announcement or sale elements should be positionable without code changes.
- urgency blocks, because timers, stock notices, or limited-run messages should be configurable and optional.
- lifestyle sections, because emotional product framing is a big part of this model.
- before/after sections, because certain products benefit from transformation storytelling.
- trust sections, because certifications, guarantees, and ratings often need their own configurable block.
- sticky CTA behavior, because persistent call-to-action logic should be adjustable for desktop and mobile.
- mobile-specific ordering overrides if needed, because a section order that feels good on desktop can feel messy on mobile.

PRODUCT PAGE CONFIG
- gallery layout, because image-first vs media-carousel vs mixed gallery layouts should be selectable.
- price display rules, because some stores want bold pricing first while others emphasize compare-at savings.
- compare-at price display, because sale framing should be configurable.
- subscription toggle display, because recurring purchase options should not appear for products that do not support them.
- bundle offer display, because bundle-selling behavior should be configurable and placement-specific.
- quantity selector display, because some products benefit from multi-unit nudging while others should keep the interface minimal.
- sticky add-to-cart enable/disable, because long-form product pages benefit from persistent purchase access.
- review placement, because trust content placement changes conversion behavior.
- FAQ placement, because objection handling can happen near the buy box or lower on the page depending on strategy.
- shipping/returns placement, because reassurance detail should be placeable near the conversion zone.
- guarantee placement, because stores differ on whether this belongs near pricing or lower in the trust section.
- comparison table placement, because some products need direct comparison at different funnel depths.
- trust badge placement, because icon clutter should be controlled intentionally.
- recommended add-ons / upsells placement, because upsell timing matters.
- post-purchase upsell eligibility, because some offers should only appear after initial purchase creation.
- low-stock messaging rules, because urgency messaging should be configurable and not hardcoded fake scarcity nonsense.
- badge rendering, because labels such as “Best Seller”, “Limited Run”, or “Subscribe & Save” should be flexible.
- highlights / bullets, because concise feature summaries should be configurable.
- spec sections, because technical or ingredient/material detail needs to be renderable without template rewriting.
- ingredients/materials/components sections if relevant, because certain product categories need domain-specific information blocks.
- tabs vs stacked layout for details, because readability and density preferences vary by brand/product.

PRODUCT CONFIG
- slug, because routing and product lookup should be stable and explicit.
- name, because the storefront must render a real product identity.
- short tagline, because the brand promise often appears beside the title.
- short description, because concise summary copy is used in listings, cards, and metadata.
- full description, because the product page needs deeper supporting copy.
- active/inactive, because publishing control matters.
- price, because pricing belongs in relational business data.
- compare-at price, because sale logic and savings display often matter.
- currency, because storefront reuse should not assume one currency forever.
- SKU, because fulfillment, order records, and payment/product mappings need stable identifiers.
- barcode if needed, because some operational integrations may require it.
- inventory tracking, because stock behavior should be explicit.
- inventory quantity, because stock availability must be stored concretely.
- subscription availability, because recurring purchase support is a business rule, not just a UI flag.
- bundle eligibility, because not every product belongs in every bundle.
- shipping profile, because shipping rules must map to actual configured fulfillment behavior.
- tax category, because product tax treatment belongs in structured business data.
- media assets, because products need associated image/video/media references.
- badges, because configurable trust and merchandising labels should render predictably.
- highlights, because concise purchase-driving information should be stored and rendered cleanly.
- benefits, because homepage and product page persuasion sections often reuse these data points.
- FAQs, because objection handling should be product-aware.
- guarantee text, because product-specific promises may differ from the site-wide guarantee default.
- review summary, because buy-box trust copy should be renderable from structured data.
- SEO metadata, because page-level search and social rendering should be tunable.
- custom attributes, because products may need additional domain-specific fields.
- JSONB presentation metadata, because visual presentation preferences may vary without forcing schema churn for every UI nuance.

PROMOTION CONFIG
- discount codes, because code-based offers should be supported as a core promotional tool.
- automatic discounts, because not all promotions should rely on customer-entered codes.
- percentage/fixed/free-shipping offers, because discount logic needs to support common commerce models.
- BOGO-style rules if feasible, because promotion flexibility matters and should be modeled sanely.
- bundle discounts, because bundle pricing is a major single-product upsell pattern.
- subscription discounts, because subscribe-and-save should be configurable rather than hacked into price labels.
- first-order discounts, because acquisition campaigns often need tailored incentives.
- limited-time banners, because urgency/promotional messaging changes frequently.
- promo eligibility rules, because not every offer should apply to every product/cart/customer state.
- promo scheduling windows, because campaigns start and stop.
- announcement scheduling, because site messaging needs timed visibility.

CHECKOUT / ORDER CONFIG
- Stripe enabled/disabled, because payment methods should be environment- and business-configurable.
- PayPal enabled/disabled, because not every deployment wants identical provider support.
- default checkout provider preference, because UI may prioritize one provider while still exposing another.
- allow guest checkout, because friction should be configurable according to store policy.
- cart abandonment capture hooks if feasible, because downstream marketing may want lightweight recovery points.
- shipping thresholds, because free shipping and similar rules are often campaign-driven.
- shipping methods, because checkout needs configurable fulfillment options.
- tax rules or tax provider hooks if applicable, because tax handling needs explicit integration points.
- order status messaging, because customer-facing status language should be editable.
- tracking page behavior, because order tracking UX should be configurable.
- post-purchase upsell offers, because these are part of the revenue model and should be placement-configurable.
- order confirmation content, because post-purchase messaging is part of brand trust.
- customer portal/subscription management links, because recurring billing flows need obvious customer access points.

EXAMPLE CONFIGURABILITY MODEL:
A store owner should be able to do things like:
- move the “As Seen On” strip from just below the hero to below the benefits section without code changes.
- disable the countdown banner during non-sale periods while leaving the feature available.
- change the homepage CTA from “Shop Now” to “See Why It Works”.
- enable subscription on product A but not product B.
- turn off bundle offers entirely for a short campaign.
- add a post-purchase upsell offer for a complementary accessory.
- switch the product page detail layout from stacked sections to tabbed sections.
- update the guarantee copy across the whole site from one admin/settings flow.
  Those are the kinds of configuration goals this engine should actually support.

==================================================
DATA MODEL RULES
==================================================

DO NOT put everything into one garbage JSON column.
Model relational data relationally.
Use JSONB where structure is flexible or presentation-driven.

Create/modify schema and migrations accordingly.

RELATIONAL TABLES TO CREATE OR REWORK
At minimum include well-designed versions of:

- products, because the product itself is a core business entity that needs stable identifiers, prices, statuses, inventory rules, and reporting-friendly structure.
- product_variants, because products may have purchasable choices with their own price, SKU, media, and inventory behavior.
- product_media, because media assets should be associated cleanly with products and variants.
- product_reviews, because review records should be queryable and renderable in structured ways.
- product_faq_entries, because FAQs often belong to products and should be manageable independently.
- product_bundles, because bundles are business-level offers with real pricing logic.
- bundle_items, because bundle composition should be stored relationally rather than hidden in blobs.
- product_upsells, because upsell relationships need clear mappings and eligibility logic.
- subscription_plans, because recurring purchase options should be modeled as first-class configuration/business records.
- carts, because cart lifecycle should be explicit and traceable.
- cart_items, because cart contents are transactional and relational.
- customers, because purchase records and subscriptions need customer linkage.
- addresses, because shipping and billing need proper structure.
- orders, because the order is a central business record.
- order_items, because each purchased unit or bundle component should be represented cleanly.
- payment_transactions, because payment state, provider responses, and reconciliation matter.
- checkout_sessions, because provider handoff and checkout orchestration need their own record layer.
- discount_codes, because promotions should be managed structurally.
- discount_redemptions, because usage tracking and limits matter.
- shipping_profiles, because product shipping behavior should be centrally managed.
- shipping_zones, because location-based fulfillment rules are part of commerce operations.
- shipment_records, because order fulfillment and tracking need explicit linkage.
- tracking_events, because customer-facing and admin-facing tracking history should be queryable.
- inventory_events, because inventory changes should be auditable and traceable.
- site_settings, because global site behavior and theme/configuration need a stable owner table.
- pages, because homepage, product page shells, landing pages, or support pages need publishing and routing identity.
- page_section_instances, because section ordering and config should be stored explicitly per page.
- feature_flags, because modules and optional flows should be toggleable.
- audit_logs if useful, because config changes and admin actions can benefit from traceability.
- webhook_event_logs, because Stripe and PayPal webhook handling should be idempotent and debuggable.

JSONB COLUMNS SHOULD BE USED FOR THINGS LIKE
- page composition data, because pages need flexible ordered content blocks without forcing a new schema for every section nuance.
- section config data, because hero blocks, trust strips, or comparison blocks may each have different shape requirements.
- hero block config, because hero visual/content combinations should be editable and variant-friendly.
- per-page SEO overrides, because SEO needs flexible page-by-page metadata support.
- flexible banner config, because banners vary in type, timing, content, color, and placement.
- trust badge config, because icon sets and copy often vary by page/campaign.
- CTA config, because calls to action need adjustable labels, destinations, styles, and behaviors.
- product presentation metadata, because visual display preferences can vary independently of core business fields.
- homepage section arrays, because persuasion sequence should be data-driven.
- mobile overrides, because desktop and mobile may need different order/visibility behavior.
- A/B variant config if implemented, because variant-specific rendering should remain flexible.
- arbitrary plugin-style UI settings, because optional storefront modules may need configurable display rules without exploding the relational schema.
- structured but flexible content blocks, because not every marketing block deserves its own permanent table.

GOOD SEPARATION RULES
- Product business truth lives in relational columns/tables, because price, SKU, stock, plan eligibility, and purchase behavior need clear structure.
- Product page rendering config can live in JSONB, because it controls visual placement and block behavior rather than core commercial truth.
- Order/payment/shipment data stays relational, because operational history should be robust, queryable, and consistent.
- Homepage and landing page composition can use JSONB-backed section config, because persuasion layouts need flexibility.
- Site-wide rendering settings can use JSONB where helpful, because theme-like and banner-like settings are configuration-oriented.
- Use JSONB intentionally, not lazily, meaning only use it where flexibility truly beats rigid schema.

INDEXING
- Add proper indexes for slugs, foreign keys, statuses, created_at, updated_at, because storefront lookup and admin operations should not degrade into table scans.
- Add GIN indexes for JSONB columns that will be queried, because flexible config should still remain performant where filters/queries matter.
- Keep query paths realistic and efficient, because there is no prize for “flexible” data that loads like molasses.

MIGRATION REQUIREMENT
If the project already contains Mongo assumptions, remove them and migrate that model into PostgreSQL + JSONB cleanly.
Refactor repository/service logic accordingly.

EXAMPLE DATA SEPARATION:
Good:
- products.price stored as numeric relational data
- products.subscription_available stored as relational boolean
- pages.sections or page_section_instances.config_json storing hero copy, section layout, badge ordering, or sticky CTA rules
  Bad:
- an entire product, its stock, its Stripe ID, and its homepage positioning all stuffed into one JSON blob called content
  Use this example mindset consistently.

==================================================
PAGE / RENDERING ARCHITECTURE
==================================================

Build a section-driven rendering engine using Thymeleaf.

RENDERING RULES
- Pages are SSR first, because fast first paint and SEO matter.
- JavaScript is progressive enhancement only, because the page must remain functional and meaningful without the whole app becoming client-driven.
- Keep HTML lean and readable, because maintainability and performance matter.
- Use Thymeleaf fragments/components for reusable sections, because homepage and product page should share visual/rendering building blocks cleanly.
- Avoid giant monolithic templates, because section-driven rendering is the whole point.

PAGE MODEL
Each page should be able to render from a DB-backed definition roughly like:
- page type, because rendering behavior may differ between homepage, product page, landing page, support page, or order tracking page.
- slug, because route identity matters.
- title, because page labeling and SEO matter.
- SEO metadata, because each page should support search/social tuning.
- product association if applicable, because homepage or product page content often targets a specific product.
- ordered section list, because layout sequence matters to conversion.
- per-section config JSONB, because each section may need flexible settings.
- enable/disable flags, because content should be toggleable without deletion.
- scheduling/publish flags if useful, because drafts and campaign timing may matter.

Implement section types such as:
- hero, because first impression and pitch framing matter.
- announcement bar, because site-wide notices often live above the main page content.
- trust strip, because trust reinforcement is a high-conversion pattern.
- as-seen-on strip, because press/social proof sections are common but should be movable.
- benefits icon row, because skim-friendly value communication is important.
- image-text split, because product storytelling often alternates visual and explanatory content.
- video section, because demonstration content often converts better than static text.
- review grid, because social proof needs flexible rendering options.
- featured testimonial, because a strong single quote/video review can anchor belief.
- FAQ block, because objections should be answerable wherever the flow needs them.
- comparison table, because contrast is persuasive when used well.
- guarantee block, because risk reduction is a major conversion lever.
- shipping block, because logistics reassurance helps conversion.
- sticky CTA, because long pages should keep the purchase path accessible.
- product highlight list, because concise top-level reasons-to-buy should be configurable.
- bundle offer block, because bundle monetization should render cleanly.
- subscription block, because recurring purchase explanation needs a distinct UI.
- upsell block, because complementary offers need page-level placement control.
- before/after block, because certain products need transformation framing.
- countdown block, because urgency can be configurable.
- featured media carousel, because demonstration content should be reusable.
- spec/details block, because product detail depth belongs in structured sections.

Make adding new section types straightforward.

EXAMPLE SECTION CONFIG SHAPES:
A hero section config might look like:
{
"type": "hero",
"enabled": true,
"layout": "media-right",
"headline": "Sleep deeper without headphones",
"subheadline": "A calming bedside sound machine designed for better nightly rest.",
"primaryCta": {
"label": "Shop Now",
"target": "/products/sleep-device"
},
"secondaryCta": {
"label": "See How It Works",
"target": "#how-it-works"
},
"showPrice": false,
"media": {
"kind": "image",
"path": "/assets/products/sleep-device/hero.webp"
}
}

A trust strip config might look like:
{
"type": "trust-strip",
"enabled": true,
"items": [
{ "icon": "truck", "label": "Free shipping over $50" },
{ "icon": "shield", "label": "30-day guarantee" },
{ "icon": "star", "label": "4.8/5 from 2,000+ buyers" }
]
}

A bundle section config might look like:
{
"type": "bundle-offer",
"enabled": true,
"bundleCode": "SLEEP_DUO",
"headline": "Save more with the 2-pack",
"displayStyle": "cards",
"defaultExpanded": true
}

Use examples like these to shape the rendering layer.

==================================================
COMMERCE FEATURES
==================================================

Implement real commerce behavior, not fake mock pages.

CART / CHECKOUT
- add to cart, because the user needs a standard purchase flow that works from homepage CTA shortcuts, product page buy actions, or bundle offers.
- remove from cart, because cart editing is basic storefront behavior.
- update quantity, because users need to adjust purchase intent without friction.
- persistent cart if possible, because returning users should not lose progress unnecessarily.
- guest checkout support, because forcing account creation kills conversions.
- redirect to Stripe checkout/session flow or equivalent integration, because Stripe should handle payment entry cleanly.
- PayPal checkout integration, because some buyers strongly prefer it.
- order confirmation handling, because successful purchase state needs a clear customer-facing result.
- payment success/failure/cancel handling, because checkout return states need robust routing and UX.
- webhook processing for payment confirmation, because actual paid order finalization should rely on provider confirmation rather than wishful thinking.
- robust idempotency around webhooks and order updates, because payment systems love retries and duplicate calls.

PRODUCT OPTIONS
- support product variants/options if present, because even a “single-product” store may have legitimate product choice dimensions.
- variant-specific price/media/SKU/inventory if needed, because operational reality often differs per variant.
- clean fallback if a store only has one simple product, because not every storefront needs full variant complexity in the visible UI.

BUNDLES
- support bundle definitions, because bundled quantity/value offers are core to direct-response storefront strategy.
- allow fixed bundles and optional add-on bundles, because stores may want curated multi-pack offers and accessory offers.
- support discounted bundle pricing, because bundle conversion often depends on visible savings.
- render bundles on product page and/or cart, because timing of the bundle pitch affects conversion.
- allow bundle-specific CTA blocks, because bundle offers often need their own persuasive treatment.
- allow bundle config in JSONB presentation layer with relational backing for real bundle data, because the business logic and the display logic are related but not identical.

SUBSCRIPTIONS
- support subscription-capable products/plans, because many single-product stores use repeat-delivery or subscribe-and-save models.
- support one-time purchase vs subscribe-and-save selection, because customers need a clean choice model.
- model subscription plans relationally, because recurring pricing and cadence are core commercial truth.
- integrate with Stripe subscription flow if feasible, because Stripe is a natural path for recurring billing.
- integrate PayPal subscriptions if cleanly possible, otherwise structure code so support can be added without rewrites, because support may be phased.
- expose configurable subscription messaging and discount display, because “save 15% monthly” or similar copy should not be hardcoded.
- support customer-facing subscription management links/portal integration where appropriate, because recurring billing without self-service becomes support hell.

UPSELLS / CROSS-SELLS
- pre-purchase upsells, because the product page may recommend complementary add-ons before cart submission.
- in-cart upsells, because the cart is a natural place to offer accessories or value-adds.
- post-purchase upsells if feasible, because revenue can increase through immediate follow-up offers after payment.
- related add-ons, because a single-product store can still recommend relevant companions.
- configurable placement and display rules, because offer timing matters.
- relational data for eligibility / mappings, because offer relationships are real business rules.
- JSONB for visual display config, because the UI style and placement should stay flexible.

DISCOUNTS / OFFERS
- code-based discounts, because standard promotional campaigns still matter.
- automatic discounts, because frictionless offers convert well.
- bundle discounts, because multi-item value framing is important.
- free shipping threshold messaging, because it nudges average order value.
- compare-at pricing, because sale framing needs structured support.
- sale badges, because visible merchandising signals matter.
- promo scheduling, because campaigns have start/end windows.
- cart-level and product-level discount logic where reasonable, because both patterns are common.

REVIEWS / SOCIAL PROOF
- structured reviews table, because social proof should be storable, searchable, and renderable cleanly.
- featured reviews support, because some reviews deserve homepage or hero-adjacent placement.
- rating summary support, because buy-box trust shorthand is important.
- configurable display blocks, because reviews may appear in multiple layouts.
- review highlights / selected testimonials, because curation matters.
- support both real review records and curated homepage testimonial blocks, because the homepage may use more editorialized proof while product pages use fuller review data.

TRACKING / FULFILLMENT
- order status page, because the user needs a place to confirm current shipment/order state.
- tracking number support, because shipping should link to a concrete trackable record.
- carrier display, because customers need clarity about who is handling fulfillment.
- tracking event history if available, because status progression builds trust.
- shipment records tied to orders, because fulfillment belongs in the core commerce model.
- tracking page rendered in SSR, because order lookup should remain fast and reliable.
- customer can check order/tracking status using order reference + email or another reasonable secure lookup approach, because a full account system should not be mandatory just to view shipping status.

EXAMPLE COMMERCE FLOW:
A real flow might be:
1. User selects one-time purchase or subscribe-and-save.
2. User chooses the default product or a 2-pack bundle.
3. User adds to cart.
4. Cart offers a complementary upsell.
5. User proceeds to checkout and chooses Stripe or PayPal.
6. Checkout session is created and stored.
7. Payment provider returns success.
8. Webhook confirms payment.
9. Order is finalized, inventory event is recorded, shipment placeholder is created.
10. Confirmation page shows order summary and tracking link when available.
    That behavior should be reflected in the engine design.

==================================================
ADMIN / CONFIGURATION EXPERIENCE
==================================================

We do NOT want a bloated CMS, but we do need sane configurability.

Build a lightweight internal admin/configuration layer if one does not exist.

At minimum support:
- editing site settings, because branding, contact info, banners, support copy, theme defaults, and global behavior must be manageable.
- editing products, because titles, prices, statuses, media, badges, FAQs, and product-linked metadata must be manageable without SQL gymnastics.
- editing homepage/page config, because the persuasion flow and section order are core to the store’s effectiveness.
- editing banners/announcement bars, because promotions change constantly.
- editing bundle definitions, because bundled offers are real revenue features.
- editing upsell mappings, because relationships between products/offers should be manageable visually.
- editing subscription plans, because cadence, discount, and availability need admin control.
- editing FAQs/reviews/highlights, because content proof and objection handling need maintenance.
- toggling sections on/off, because not every campaign wants every persuasion block.
- reordering sections, because the visual funnel should be easy to iterate.
- previewing draft vs published config if feasible, because content changes should be safely testable.

This can be:
- simple secured admin Thymeleaf pages, because SSR internal tools are enough here and align with the rest of the stack, or
- pragmatic internal JSON editors/forms, because some highly flexible config structures may need raw or semi-structured editing views.

But it must not become a full separate frontend app unless absolutely necessary.

ADMIN UX PRINCIPLE:
This should feel like an internal commerce/config dashboard, not like a generic CMS and not like a raw database browser. The user should be able to manage product/store behavior with focused screens and practical form flows.

EXAMPLE ADMIN NAVIGATION:
A sane admin sidebar might include:
- Dashboard
- Site Settings
- Pages
- Products
- Bundles
- Upsells
- Subscriptions
- Discounts
- Orders
- Shipments & Tracking
- Reviews & FAQ
- Feature Flags
- Payment Settings
- Analytics / SEO
- Media Assets
  This is not a random wish list. It reflects the actual operational modules this engine needs.

ADMIN DASHBOARD EXAMPLES

1. Dashboard Home
   Purpose:
- Give a quick operational view of store health and recent activity.
  What it should show:
- today’s orders
- revenue snapshot
- recent failed payments or webhook issues
- active promotions
- low-stock products
- pending shipments
- quick links to edit homepage/product page
  Why:
- The admin landing screen should be useful in under 10 seconds, not just decorative nonsense.

2. Site Settings Screen
   Purpose:
- Control global brand and store-wide behavior.
  Example fields:
- site name
- logo
- support email
- footer legal text
- default SEO title template
- guarantee text
- shipping promo text
- free shipping threshold message
- social links
- global trust badges
- analytics IDs
- feature flags
  Flow example:
- admin opens Site Settings
- edits guarantee text and shipping banner copy
- toggles countdown bar off
- saves changes
- preview/published behavior updates accordingly

3. Pages Screen
   Purpose:
- Manage page definitions and section-based rendering.
  Example list view:
- Homepage
- Product Page
- Thank You Page
- Order Tracking Page
- Seasonal Landing Page
  Each row should show:
- slug
- type
- status
- last updated
- associated product if applicable
  Flow example:
- admin opens Homepage
- sees ordered list of sections
- drags FAQ above reviews
- disables “As Seen On” strip
- changes hero CTA text
- previews draft
- publishes

4. Page Editor / Section Editor
   Purpose:
- Edit the actual persuasion flow.
  What it should support:
- reorder sections
- enable/disable sections
- edit section config
- duplicate section
- insert new section type
- preview desktop/mobile visibility
  Example section editor behavior:
- hero section form shows headline, subheadline, media, CTA labels, CTA targets, showPrice toggle
- trust strip form shows icon list, labels, link targets
- comparison section form shows title, column labels, rows, highlighted column
  The page editor should feel like a controlled commerce landing-page composer, not an anything-goes website builder.

5. Products Screen
   Purpose:
- Manage real product data.
  Example list columns:
- product name
- slug
- active status
- price
- compare-at price
- stock quantity
- subscription enabled
- bundle eligible
- updated at
  Product detail screen should support:
- business fields
- media management
- highlights
- badges
- FAQs
- reviews summary
- SEO metadata
- presentation metadata
  Flow example:
- admin edits product price
- changes compare-at price
- uploads new gallery image
- toggles subscription availability on
- attaches shipping profile
- saves

6. Bundles Screen
   Purpose:
- Manage bundle offers cleanly.
  What it should support:
- create a new bundle
- define bundle items
- define bundle discount
- define bundle title/subtitle
- define display style / bundle card messaging
- attach bundle to product page display areas
  Flow example:
- admin creates “2-Pack Save 15%”
- selects product + quantity 2
- sets discount value
- adds persuasive copy
- marks as featured on product page
- saves

7. Upsells Screen
   Purpose:
- Manage pre-purchase, in-cart, and post-purchase offers.
  What it should support:
- define source product
- define upsell target product
- define placement
- define display headline/body
- define offer discount if applicable
- define active dates
  Flow example:
- admin attaches accessory product as an in-cart upsell
- writes “Complete the setup”
- enables only for one-time purchase buyers
- saves

8. Subscriptions Screen
   Purpose:
- Manage recurring purchase plans.
  What it should support:
- plan cadence
- discount percent/fixed discount
- default selected or not
- copy such as “Save 15% monthly”
- Stripe/PayPal mapping identifiers
- product eligibility
  Flow example:
- admin adds monthly and bi-monthly plans
- monthly gives 10% off
- bi-monthly gives 15% off
- sets monthly as default displayed option
- saves

9. Discounts Screen
   Purpose:
- Manage campaign offers.
  What it should support:
- code-based offers
- auto-applied offers
- cart threshold offers
- free shipping thresholds
- active dates
- usage limits
- audience targeting if implemented
  Flow example:
- admin creates SPRING15
- sets 15% off orders over $50
- active for one week
- saves and marks banner as active

10. Orders Screen
    Purpose:
- Review and manage purchase records.
  List view should show:
- order number
- customer
- status
- total
- payment status
- shipment status
- created at
  Order detail should show:
- purchased items
- address
- payment history
- discount usage
- shipment/tracking
- notes/audit trail if present

11. Shipments & Tracking Screen
    Purpose:
- Review shipment state and customer-facing trackability.
  What it should support:
- create/update shipment record
- carrier
- tracking number
- shipment status
- tracking events
- link to order
  Flow example:
- admin opens order shipment
- enters carrier + tracking number
- marks shipped
- customer tracking page now shows active tracking info

12. Reviews & FAQ Screen
    Purpose:
- Manage trust and objection content.
  What it should support:
- approve/reject featured reviews if workflow exists
- edit curated testimonials
- manage FAQ entries by product
- mark FAQ entries as homepage-visible or product-only
  Flow example:
- admin promotes one review to homepage featured testimonial
- edits FAQ wording for shipping timing
- saves

13. Feature Flags Screen
    Purpose:
- Turn optional modules on/off without tearing code apart.
  Possible flags:
- subscriptions enabled
- bundles enabled
- post-purchase upsells enabled
- countdown banner enabled
- tracking lookup enabled
- draft preview enabled
  This should behave like lightweight operational control, not like a graveyard of mystery toggles.

14. Payment Settings Screen
    Purpose:
- Manage payment provider integration settings and status.
  What it should support:
- Stripe enabled
- PayPal enabled
- mode/environment display
- webhook status info if helpful
- provider labels/ordering
- failure diagnostics if safely exposable internally

15. Analytics / SEO Screen
    Purpose:
- Manage marketing metadata and tracking hooks.
  What it should support:
- site-wide SEO defaults
- per-page overrides
- analytics IDs
- conversion scripts/hooks
- open graph defaults
- noindex flags where applicable

16. Media Assets Screen
    Purpose:
- Manage filesystem-backed storefront assets in a practical way.
  What it should support:
- upload
- browse by product/page/global use
- preview
- copy public path
- attach to product or section
  This should remain simple and not become a giant DAM system.

==================================================
PERFORMANCE / DELIVERY REQUIREMENTS
==================================================

The site should feel fast on slower connections.

Implement performance-minded behavior:
- SSR-first pages, because core browsing and purchasing should not wait on frontend hydration.
- minimal JS payload, because most of the site should render and function from server output alone.
- lazy load non-critical media, because below-the-fold performance matters.
- use efficient image handling, because media-heavy storefronts get slow fast when handled lazily.
- support serving static assets from filesystem, because this is part of the deployment model.
- generate/cache correct cache headers for static assets, because Cloudflare and browser caching should work with minimal fuss.
- avoid unnecessary DB calls during page render, because performance dies through death by a thousand queries.
- keep homepage/product page queries efficient, because these are the highest-traffic pages.
- avoid giant all-in-one payloads, because “configurable” should not mean “dump every object on every request”.
- fragment/reuse templates where clean, because repeated rendering logic should stay maintainable.
- design with Cloudflare caching in mind, but do not require a specialized CDN product, because the stack should remain simple and deployable.
- avoid plugin-style bloat, because this is exactly the nonsense we are trying to escape.

Do not build a heavy generic page builder runtime.
Build a focused commerce-first rendering engine.

EXAMPLE PERFORMANCE EXPECTATION:
A product page should render usable HTML quickly even on mediocre connections.
A shopper should not have to wait for a giant JS framework to mount before seeing:
- title
- media
- price
- CTA
- reviews summary
- shipping reassurance
  If the page is technically “fancy” but slow and fragile, that is failure.

==================================================
CODE STRUCTURE REQUIREMENTS
==================================================

Prefer a clean layered structure such as:
- controller, because request handling and route logic should be explicit.
- service, because business behavior belongs in focused operational services.
- repository, because persistence concerns should be isolated and testable.
- model/entity, because database representation should be structured and clear.
- dto/view model where useful, because rendering should not always bind raw entities directly.
- template/fragments, because the UI is section-driven and SSR-first.
- static assets, because frontend media/scripts/styles should remain organized.
- migrations, because schema should be reproducible.
- payment integration module/services, because provider logic should be isolated.
- tracking/fulfillment services, because shipment state and customer-facing lookup need focused behavior.
- admin/config services, because internal configuration flows should not be tangled into storefront browsing services.

Use clear names.
Examples of preferred naming style:
- addProductToCart
- removeProductFromCart
- createCheckoutSession
- processStripeWebhookEvent
- processPayPalWebhookEvent
- renderHomepageSections
- renderProductPageSections
- findPublishedPageBySlug
- updateSiteSettings
- registerBundleDefinition
- attachUpsellToProduct
- createSubscriptionPlan

Avoid lazy names like:
- handleThing
- doStuff
- processData
- managePage

No nested classes.
No vague god objects.
No giant service with 40 responsibilities.
No “temporary” fake implementations unless clearly marked and unavoidable.

EXAMPLE PACKAGE SHAPE:
A reasonable package structure might look like:
- storefront.config
- storefront.products
- storefront.pages
- storefront.cart
- storefront.checkout
- storefront.payments.stripe
- storefront.payments.paypal
- storefront.orders
- storefront.shipping
- storefront.tracking
- storefront.admin
- storefront.media
- storefront.features
  Use the repository’s actual conventions if already present, but keep responsibilities this clean.

==================================================
IMPLEMENTATION TASKS
==================================================

Perform the work in this order unless the repo clearly demands a different sequence:

1. Inspect the existing project structure and identify:
    - current Spring Boot setup, because integration should fit the actual app rather than fantasy architecture.
    - current Thymeleaf setup, because rendering changes need to align with existing view infrastructure.
    - current database config, because migrations and repositories need a real target.
    - any Mongo/Mongo-like assumptions, because these must be removed or folded into PostgreSQL/JSONB.
    - current product/order/page models, because refactoring should preserve what is worth keeping.
    - current asset handling, because static file serving and media references need to stay coherent.
    - current payment integrations, because provider work should not be duplicated blindly.

2. Refactor architecture to standardize on PostgreSQL + JSONB:
    - remove Mongo references, because the storage model is being consolidated.
    - redesign entities/tables, because the data model must reflect the new split between relational truth and flexible config.
    - add migrations, because schema changes must be reproducible.
    - update repositories/services, because application logic must actually use the new model.

3. Implement core commerce data model:
    - products, because nothing else works without a real product model.
    - variants, because option support must be clean even if minimal at first.
    - orders, because actual purchases need durable storage.
    - carts, because pre-purchase state matters.
    - checkout sessions, because payment provider orchestration needs traceable records.
    - payments, because status and reconciliation matter.
    - shipping, because fulfillment is part of the store.
    - tracking, because customer trust continues after payment.

4. Implement page/content/config model:
    - site settings, because global behavior must be configurable.
    - pages, because homepage/product page/other views need route-aware page identity.
    - page sections, because composition must be data-driven.
    - JSONB config structures, because flexible rendering depends on them.
    - rendering services, because templates need organized inputs.

5. Build homepage and product page rendering:
    - hero, because the store pitch starts there.
    - trust strip, because proof matters early.
    - benefits, because concise value communication drives scroll.
    - reviews, because social proof supports conversion.
    - FAQ, because objections must be handled.
    - bundle, because higher-value offers matter.
    - subscription, because repeat purchase should be native where applicable.
    - upsell, because add-on monetization should be supported.
    - CTA sections, because the user needs repeated purchase paths.
    - banners, because merchandising and promotional messaging should be manageable.

6. Implement cart and checkout flows:
    - add to cart, because browsing must lead to commerce.
    - cart updates, because cart editing is standard.
    - Stripe, because provider support is required.
    - PayPal, because provider support is required.
    - webhooks, because final order state depends on provider confirmation.
    - order creation/finalization, because paid carts must become real orders.

7. Implement admin/configuration tooling:
    - products, because product management is a core workflow.
    - page sections, because persuasion flow editing is central.
    - site settings, because brand and policy changes must be easy.
    - offers, because promotions drive commerce.
    - bundles, because bundled offers are important.
    - subscriptions, because recurring sales need management.
    - banners, because campaign messaging changes frequently.

8. Implement order tracking and fulfillment views:
    - tracking lookup, because customers need visibility after purchase.
    - shipment records, because fulfillment state matters.
    - tracking events, because shipment history improves trust.
    - order status, because the user needs a coherent view of order progress.

9. Tighten performance and polish:
    - cache headers, because asset delivery should be efficient.
    - media loading, because storefronts are media-heavy.
    - query cleanup, because render paths should be efficient.
    - fragment cleanup, because templates should stay maintainable.
    - TS/JS only where needed, because SSR-first is the rule.

10. Add tests where practical:
- repository tests, because persistence logic should be trustworthy.
- service tests, because business logic deserves coverage.
- payment webhook flow tests, because duplicate/event-driven payment flows can go wrong fast.
- page render sanity tests, because core views should not silently break.
- cart/order flow tests, because the revenue path is not optional.

==================================================
DELIVERABLES
==================================================

Produce actual code and project changes, including:
- schema migrations, because the data model must be reproducible.
- entities/models, because structured data access is required.
- repositories, because persistence must be cleanly implemented.
- services, because business behavior belongs in focused layers.
- controllers, because routes and request behavior must exist.
- Thymeleaf templates, because storefront pages and admin views need real SSR output.
- Thymeleaf fragments, because section-driven UI should be reusable.
- TS/JS files for interactivity, because sticky CTA, cart behavior, small toggles, and similar behavior may need enhancement.
- payment integration code, because checkout must function.
- admin/config pages, because configurability is part of the requirement.
- tracking/order lookup pages, because post-purchase flows matter.
- configuration classes, because environment/provider settings need clean wiring.
- seed/sample data where helpful, because the engine should be demonstrable and testable.
- comments only where genuinely useful, because clutter is not documentation.

Also provide:
- a concise implementation summary, because the final output should explain what changed.
- a concise list of major schema decisions, because the data model is a major part of the architecture.
- a concise list of any assumptions made, because hidden assumptions create surprises later.
- a concise list of any items intentionally stubbed and why, because missing credentials or external provider details may require partial scaffolding.

IMPORTANT OUTPUT STYLE RULE:
Whenever you summarize or explain something in your final output, do not use naked bullet points with no explanation.
Every listed item should include a brief explanation of why it exists, what changed, or what decision was made.
Do not return shallow one-line lists that leave the reasoning implied.

==================================================
ACCEPTANCE CRITERIA
==================================================

The implementation is successful when all of the following are true:

- Mongo is fully removed from the architecture for this storefront scope, because the storage model has been intentionally consolidated.
- PostgreSQL + JSONB cleanly handle both commerce and flexible page content, because that is the chosen architecture.
- We have a reusable single-product storefront engine, because the goal is reuse rather than a one-off product page.
- Homepage and product page are separate but share reusable rendering components, because funnel separation matters but duplication should not grow uncontrolled.
- Page sections are DB-configurable, because persuasion flow should not require code edits.
- Site banners/trust strips/CTA blocks are configurable, because merchandising should be manageable.
- Products, bundles, subscriptions, and upsells are modeled sanely, because revenue features need real structure.
- Cart and checkout function through Stripe and PayPal, because purchase flow must actually work.
- Orders and tracking data are stored and viewable, because post-purchase operations matter.
- Admin/config tooling exists in a lightweight but practical form, because the store must be operable.
- The site is SSR-first and feels fast, because performance is a major product requirement.
- The code is maintainable and not a spaghetti plugin graveyard, because we are replacing exactly that kind of nonsense.

ACCEPTANCE EXAMPLE:
If a non-developer admin can:
- update the homepage hero
- reorder sections
- change a banner
- adjust a bundle offer
- enable subscriptions
- review an order
- add tracking info
  without touching code, then the engine is behaving correctly.
  If every small marketing change still requires template edits, the implementation is not complete.

==================================================
STYLE / DECISION RULES
==================================================

Whenever you face a decision:
- choose maintainability over novelty, because this is a real store system and not a playground for cleverness.
- choose SSR over unnecessary frontend complexity, because speed and robustness matter more than framework fashion.
- choose relational modeling for core business data, because the truth of commerce needs structure.
- choose JSONB for flexible page/configuration content, because layout/content flexibility should remain practical.
- choose focused abstractions over framework cosplay, because fake elegance is still bad architecture.
- choose implementation over endless planning, because the prompt exists to produce a working engine.

Do not give me a toy scaffold.
Do not give me a blog post.
Do not give me a fake “future architecture”.
Actually implement the engine and wire the major flows together.
If some integrations require credentials not present in repo, scaffold them properly, wire config, and leave clean integration points without breaking the rest of the system.

==================================================
ADDITIONAL VISUAL / UX EXAMPLES
==================================================

Use these examples to guide the feel of the storefront and admin UI.

EXAMPLE 1: CLEAN SINGLE-PRODUCT HOMEPAGE
The page opens with a large hero containing:
- a sharp value-focused headline
- a short supporting line
- a product image or demo video
- one strong CTA
  Below that:
- a trust strip with shipping/guarantee/rating
- a benefits row with three to five concise icons
- an image-text section showing the product in use
- a short quote/testimonial with face/name if available
- a comparison block showing “why this instead of alternatives”
- a FAQ preview with 3-5 common objections
- repeated CTA
  This should feel focused, premium, and easy to scan.

EXAMPLE 2: PRODUCT PAGE WITH HIGHER CONVERSION FRICTION HANDLING
At top:
- gallery
- title
- tagline
- price
- compare-at price if relevant
- variant selector if relevant
- one-time vs subscribe-and-save
- add to cart
  Nearby:
- review summary
- guarantee snippet
- shipping snippet
  Further down:
- full testimonials
- FAQ
- comparison section
- specs/materials
- related add-ons
- bundle cards
  This should feel like the page that closes the sale.

EXAMPLE 3: ADMIN PAGE EDITOR EXPERIENCE
The page editor should feel like:
- left sidebar with sections in order
- main panel showing selected section form fields
- top actions for preview / publish / save draft
- simple up/down drag or reorder tools
- section enable/disable toggle
- add section modal listing available section types
  This does not need to be sexy. It needs to be usable and clear.

EXAMPLE 4: PRODUCT EDITOR EXPERIENCE
The product editor might contain tabs or panels such as:
- General
- Pricing
- Inventory
- Media
- Reviews & FAQ
- Presentation
- SEO
- Offers
  Each panel should edit the corresponding data cleanly rather than dumping every field into one monster form.

==================================================
DO NOT MAKE THESE MISTAKES
==================================================

- Do not model the entire storefront as one giant JSON document, because that destroys queryability and maintainability.
- Do not hardcode homepage section order, because configurability is central.
- Do not make banners or trust strips static in templates, because they need placement control.
- Do not build a generic website builder, because this is a commerce-focused engine.
- Do not hide price/inventory/order/subscription truth inside page config JSON, because those are business entities.
- Do not require a frontend framework for basic admin or storefront functionality, because SSR is sufficient here.
- Do not make admin screens look like raw database tables with no flow, because the system needs real operational usability.
- Do not implement “feature flags” as random booleans with no organization, because they should remain understandable.
- Do not leave webhook handling non-idempotent, because payment providers retry and chaos follows.
- Do not write summaries with unexplained bullet points, because vague output is part of what causes sloppy implementation.

FINAL REMINDER:
This is not “a store page.”
This is a reusable, SSR-first, PostgreSQL + JSONB single-product commerce engine with lightweight internal admin tooling, configurable persuasion flows, payment integration, bundle/subscription/upsell support, and post-purchase tracking behavior.
Build it like an actual product.