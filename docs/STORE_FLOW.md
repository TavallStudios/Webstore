# What the actual product should feel like
## For non-technical merchants
* answer launch questions
* upload brand/product/media
* connect payments
* choose from a proven storefront preset
* tweak copy, colors, section order, promos
* go live


## For developers/agencies
* same core engine
* plus code mode
* direct HTML/CSS/JS customization
* reusable components
* publish/preview pipeline
* no Liquid nonsense
* no plugin dependency maze

# Client onboarding flow


## Stage 1: Application

Collect:

business/store info
product info
payment-provider intent
domain intent
launch timeline
product assets
policies
Stage 2: Readiness checklist

Require:

at least 1 real product
at least 3 clean images
price set
shipping config set
support email set
refund/return policy set
Stripe or PayPal connected
domain verified or ready
homepage/product page not empty
Stage 3: Provision store

Only after passing readiness:

allocate public store
generate storefront from conversion preset
assign URLs/assets/runtime
enable indexing and checkout
Stage 4: Publish gate

Final checks:

checkout test passes
page speed acceptable
required legal/support pages exist
core sections populated


3.Scroll = persuasion pipeline


In order:

Problem
Solution (your product)
Benefits (not features, stop being a nerd)
Social proof (reviews, UGC, TikTok vids)
Comparison (“why we’re better”)
FAQ (kills objections)

Postgres:

Use Postgres for anything that is:

transactional
relational
queryable in predictable ways
important enough that you don’t want “eh, document shape changed lol” energy

That includes:

orders
payments
customers
product variants
inventory
discounts
shipping rules
audit logs
checkout sessions
probably products too
Mongo:

Use Mongo for anything that is:

layout-heavy
section-based
mutable in structure
basically “page builder data without calling it a page builder”

That includes:

homepage section config
product landing page blocks
testimonial blocks
FAQ sections
trust badge layouts
announcement bars
A/B test content variants
reusable page composition JSON

Postgres tables
products
product_variants
orders
order_items
customers
payments
discount_codes
shipping_zones
inventory_events
Mongo collections
site_themes
page_templates
landing_pages
product_page_content
section_presets
announcement_bars
split_tests

That gives you stru