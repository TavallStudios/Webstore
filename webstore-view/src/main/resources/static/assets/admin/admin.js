(function () {
    function parseJson(raw, fallback) {
        if (!raw || !raw.trim()) {
            return fallback;
        }
        try {
            return JSON.parse(raw);
        } catch (error) {
            return fallback;
        }
    }

    function writeJson(field, value) {
        if (field) {
            field.value = JSON.stringify(value, null, 2);
        }
    }

    function readText(row, selector) {
        const input = row.querySelector(selector);
        return input ? input.value.trim() : "";
    }

    function readChecked(row, selector) {
        const input = row.querySelector(selector);
        return !!(input && input.checked);
    }

    function linesToLinks(text) {
        return text
            .split(/\r?\n/)
            .map((line) => line.trim())
            .filter(Boolean)
            .map((line) => {
                const [label, href] = line.split("|").map((part) => (part || "").trim());
                return {label, href};
            })
            .filter((entry) => entry.label || entry.href);
    }

    function linksToLines(links) {
        if (!Array.isArray(links)) {
            return "";
        }
        return links.map((link) => `${link.label || ""} | ${link.href || ""}`.trim()).join("\n");
    }

    function formatInstantForInput(value) {
        if (!value) {
            return "";
        }
        const date = new Date(value);
        if (Number.isNaN(date.getTime())) {
            return "";
        }
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, "0");
        const day = String(date.getDate()).padStart(2, "0");
        const hours = String(date.getHours()).padStart(2, "0");
        const minutes = String(date.getMinutes()).padStart(2, "0");
        return `${year}-${month}-${day}T${hours}:${minutes}`;
    }

    function parseInputInstant(value) {
        return value ? new Date(value).toISOString() : new Date().toISOString();
    }

    function initDialogs() {
        document.querySelectorAll("[data-dialog-open]").forEach((button) => {
            button.addEventListener("click", () => {
                const dialog = document.getElementById(button.getAttribute("data-dialog-open"));
                if (dialog && typeof dialog.showModal === "function") {
                    dialog.showModal();
                }
            });
        });

        document.querySelectorAll("[data-dialog-close]").forEach((button) => {
            button.addEventListener("click", () => {
                const dialog = button.closest("dialog");
                if (dialog) {
                    dialog.close();
                }
            });
        });
    }

    function createEditor(options) {
        const container = document.querySelector(options.containerSelector);
        const addButton = document.querySelector(options.addButtonSelector);
        const template = document.querySelector(options.templateSelector);
        if (!container || !template) {
            return {items: () => []};
        }

        function updateEmptyState() {
            const emptyState = container.parentElement.querySelector("[data-empty-state]");
            if (emptyState) {
                emptyState.hidden = container.children.length !== 0;
            }
        }

        function addItem(item) {
            const fragment = template.content.cloneNode(true);
            const row = fragment.firstElementChild;
            options.fillRow(row, item || options.emptyItem());
            container.appendChild(fragment);
        }

        container.addEventListener("click", (event) => {
            const removeButton = event.target.closest("[data-row-remove]");
            if (removeButton) {
                const row = removeButton.closest("[data-editor-row]");
                if (row) {
                    row.remove();
                    updateEmptyState();
                }
            }
        });

        if (addButton) {
            addButton.addEventListener("click", () => {
                addItem(options.emptyItem());
                updateEmptyState();
            });
        }

        (options.initialItems || []).forEach((item) => addItem(item));
        updateEmptyState();

        return {
            items() {
                return Array.from(container.querySelectorAll("[data-editor-row]"))
                    .map((row) => options.readRow(row))
                    .filter((item) => options.keepItem ? options.keepItem(item) : true);
            }
        };
    }

    function initSiteSettingsPage() {
        const form = document.querySelector("#site-settings-form");
        if (!form) {
            return;
        }

        const brandField = form.querySelector("#brandPaletteJson");
        const typographyField = form.querySelector("#typographyJson");
        const socialField = form.querySelector("#socialLinksJson");
        const reviewField = form.querySelector("#reviewSourceJson");
        const footerField = form.querySelector("#footerContentJson");
        const announcementField = form.querySelector("#announcementBarsJson");
        const promoField = form.querySelector("#promoBannersJson");
        const trustField = form.querySelector("#trustBadgesJson");

        const brand = parseJson(brandField.value, {});
        const typography = parseJson(typographyField.value, {});
        const socialLinks = parseJson(socialField.value, {});
        const review = parseJson(reviewField.value, {});
        const footer = parseJson(footerField.value, {});
        const announcements = parseJson(announcementField.value, []);
        const promos = parseJson(promoField.value, []);
        const trustBadges = parseJson(trustField.value, []);

        [
            ["#brand-background", brand.background || "#f7efe4"],
            ["#brand-surface", brand.surface || "#fffaf3"],
            ["#brand-ink", brand.ink || "#1d1a16"],
            ["#brand-accent", brand.accent || "#c85f35"],
            ["#brand-accent-deep", brand.accentDeep || "#8c3514"],
            ["#brand-muted", brand.muted || "#6d6258"],
            ["#font-headline", typography.headline || "Fraunces, Georgia, serif"],
            ["#font-body", typography.body || "IBM Plex Sans, Segoe UI, sans-serif"],
            ["#font-mono", typography.mono || "IBM Plex Mono, Consolas, monospace"],
            ["#review-label", review.label || ""],
            ["#review-source", review.source || ""],
            ["#footer-fine-print", footer.finePrint || ""]
        ].forEach(([selector, value]) => {
            const input = form.querySelector(selector);
            if (input) {
                input.value = value;
            }
        });

        const socialEditor = createEditor({
            containerSelector: "#social-links-list",
            addButtonSelector: "#add-social-link",
            templateSelector: "#social-link-template",
            initialItems: Object.entries(socialLinks).map(([channel, url]) => ({channel, url})),
            emptyItem: () => ({channel: "", url: ""}),
            fillRow(row, item) {
                row.querySelector("[data-field='channel']").value = item.channel || "";
                row.querySelector("[data-field='url']").value = item.url || "";
            },
            readRow(row) {
                return {channel: readText(row, "[data-field='channel']"), url: readText(row, "[data-field='url']")};
            },
            keepItem(item) {
                return item.channel || item.url;
            }
        });

        const footerEditor = createEditor({
            containerSelector: "#footer-columns-list",
            addButtonSelector: "#add-footer-column",
            templateSelector: "#footer-column-template",
            initialItems: Array.isArray(footer.columns) ? footer.columns.map((column) => ({
                title: column.title || "",
                linksText: linksToLines(column.links)
            })) : [],
            emptyItem: () => ({title: "", linksText: ""}),
            fillRow(row, item) {
                row.querySelector("[data-field='title']").value = item.title || "";
                row.querySelector("[data-field='links']").value = item.linksText || "";
            },
            readRow(row) {
                return {title: readText(row, "[data-field='title']"), links: linesToLinks(readText(row, "[data-field='links']"))};
            },
            keepItem(item) {
                return item.title || item.links.length > 0;
            }
        });

        const announcementEditor = createEditor({
            containerSelector: "#announcement-list",
            addButtonSelector: "#add-announcement",
            templateSelector: "#announcement-template",
            initialItems: announcements,
            emptyItem: () => ({label: "", message: "", tone: "accent"}),
            fillRow(row, item) {
                row.querySelector("[data-field='label']").value = item.label || "";
                row.querySelector("[data-field='message']").value = item.message || "";
                row.querySelector("[data-field='tone']").value = item.tone || "accent";
            },
            readRow(row) {
                return {
                    label: readText(row, "[data-field='label']"),
                    message: readText(row, "[data-field='message']"),
                    tone: readText(row, "[data-field='tone']")
                };
            },
            keepItem(item) {
                return item.label || item.message;
            }
        });

        const promoEditor = createEditor({
            containerSelector: "#promo-banner-list",
            addButtonSelector: "#add-promo-banner",
            templateSelector: "#promo-banner-template",
            initialItems: promos,
            emptyItem: () => ({placement: "after-hero", eyebrow: "", headline: "", body: "", ctaLabel: "", ctaHref: ""}),
            fillRow(row, item) {
                row.querySelector("[data-field='placement']").value = item.placement || "after-hero";
                row.querySelector("[data-field='eyebrow']").value = item.eyebrow || "";
                row.querySelector("[data-field='headline']").value = item.headline || "";
                row.querySelector("[data-field='body']").value = item.body || "";
                row.querySelector("[data-field='ctaLabel']").value = item.ctaLabel || "";
                row.querySelector("[data-field='ctaHref']").value = item.ctaHref || "";
            },
            readRow(row) {
                return {
                    placement: readText(row, "[data-field='placement']"),
                    eyebrow: readText(row, "[data-field='eyebrow']"),
                    headline: readText(row, "[data-field='headline']"),
                    body: readText(row, "[data-field='body']"),
                    ctaLabel: readText(row, "[data-field='ctaLabel']"),
                    ctaHref: readText(row, "[data-field='ctaHref']")
                };
            },
            keepItem(item) {
                return item.headline || item.body;
            }
        });

        const trustEditor = createEditor({
            containerSelector: "#trust-badge-list",
            addButtonSelector: "#add-trust-badge",
            templateSelector: "#trust-badge-template",
            initialItems: trustBadges,
            emptyItem: () => ({label: "", detail: ""}),
            fillRow(row, item) {
                row.querySelector("[data-field='label']").value = item.label || "";
                row.querySelector("[data-field='detail']").value = item.detail || "";
            },
            readRow(row) {
                return {label: readText(row, "[data-field='label']"), detail: readText(row, "[data-field='detail']")};
            },
            keepItem(item) {
                return item.label || item.detail;
            }
        });

        form.addEventListener("submit", () => {
            writeJson(brandField, {
                ...brand,
                background: form.querySelector("#brand-background").value,
                surface: form.querySelector("#brand-surface").value,
                ink: form.querySelector("#brand-ink").value,
                accent: form.querySelector("#brand-accent").value,
                accentDeep: form.querySelector("#brand-accent-deep").value,
                muted: form.querySelector("#brand-muted").value
            });
            writeJson(typographyField, {
                ...typography,
                headline: form.querySelector("#font-headline").value.trim(),
                body: form.querySelector("#font-body").value.trim(),
                mono: form.querySelector("#font-mono").value.trim()
            });
            writeJson(socialField, socialEditor.items().reduce((accumulator, item) => {
                if (item.channel) {
                    accumulator[item.channel] = item.url;
                }
                return accumulator;
            }, {}));
            writeJson(reviewField, {
                ...review,
                label: form.querySelector("#review-label").value.trim(),
                source: form.querySelector("#review-source").value.trim()
            });
            writeJson(footerField, {
                ...footer,
                finePrint: form.querySelector("#footer-fine-print").value.trim(),
                columns: footerEditor.items()
            });
            writeJson(announcementField, announcementEditor.items());
            writeJson(promoField, promoEditor.items());
            writeJson(trustField, trustEditor.items());
        });
    }

    function initAnalyticsPage() {
        const form = document.querySelector("#analytics-settings-form");
        if (!form) {
            return;
        }

        const seoField = form.querySelector("#seoDefaultsJson");
        const analyticsField = form.querySelector("#analyticsSettingsJson");
        const seo = parseJson(seoField.value, {});
        const analytics = parseJson(analyticsField.value, {});

        [
            ["#seo-title-template", seo.titleTemplate || "%s | Storefront"],
            ["#seo-description", seo.description || ""],
            ["#seo-og-image", seo.openGraphImage || ""],
            ["#analytics-ga4", analytics.ga4Id || ""],
            ["#analytics-pixel", analytics.metaPixelId || ""],
            ["#analytics-hooks", Array.isArray(analytics.conversionHooks) ? analytics.conversionHooks.join("\n") : ""]
        ].forEach(([selector, value]) => {
            const input = form.querySelector(selector);
            if (input) {
                input.value = value;
            }
        });

        form.addEventListener("submit", () => {
            writeJson(seoField, {
                ...seo,
                titleTemplate: form.querySelector("#seo-title-template").value.trim(),
                description: form.querySelector("#seo-description").value.trim(),
                openGraphImage: form.querySelector("#seo-og-image").value.trim()
            });
            writeJson(analyticsField, {
                ...analytics,
                ga4Id: form.querySelector("#analytics-ga4").value.trim(),
                metaPixelId: form.querySelector("#analytics-pixel").value.trim(),
                conversionHooks: form.querySelector("#analytics-hooks").value.split(/\r?\n/).map((line) => line.trim()).filter(Boolean)
            });
        });
    }

    function initProductFormPage() {
        const form = document.querySelector("#product-form");
        if (!form) {
            return;
        }

        const mediaField = form.querySelector("#mediaAssetsJson");
        const badgesField = form.querySelector("#badgesJson");
        const highlightsField = form.querySelector("#highlightsJson");
        const benefitsField = form.querySelector("#benefitsJson");
        const faqField = form.querySelector("#faqEntriesJson");
        const reviewField = form.querySelector("#reviewSummaryJson");
        const seoField = form.querySelector("#seoMetadataJson");
        const customField = form.querySelector("#customAttributesJson");
        const presentationField = form.querySelector("#presentationMetadataJson");

        const review = parseJson(reviewField.value, {});
        const seo = parseJson(seoField.value, {});
        const presentation = parseJson(presentationField.value, {});

        const mediaEditor = createEditor({
            containerSelector: "#media-assets-list",
            addButtonSelector: "#add-media-asset",
            templateSelector: "#media-asset-template",
            initialItems: parseJson(mediaField.value, []),
            emptyItem: () => ({path: "", alt: ""}),
            fillRow(row, item) {
                row.querySelector("[data-field='path']").value = item.path || "";
                row.querySelector("[data-field='alt']").value = item.alt || "";
            },
            readRow(row) {
                return {path: readText(row, "[data-field='path']"), alt: readText(row, "[data-field='alt']")};
            },
            keepItem(item) {
                return item.path || item.alt;
            }
        });

        const buildStringEditor = (containerSelector, addButtonSelector, hiddenField) => createEditor({
            containerSelector,
            addButtonSelector,
            templateSelector: "#string-item-template",
            initialItems: parseJson(hiddenField.value, []).map((value) => ({value})),
            emptyItem: () => ({value: ""}),
            fillRow(row, item) {
                row.querySelector("[data-field='value']").value = item.value || "";
            },
            readRow(row) {
                return {value: readText(row, "[data-field='value']")};
            },
            keepItem(item) {
                return item.value;
            }
        });

        const badgesEditor = buildStringEditor("#badges-list", "#add-badge", badgesField);
        const highlightsEditor = buildStringEditor("#highlights-list", "#add-highlight", highlightsField);
        const benefitsEditor = buildStringEditor("#benefits-list", "#add-benefit", benefitsField);

        const faqEditor = createEditor({
            containerSelector: "#faq-list",
            addButtonSelector: "#add-faq",
            templateSelector: "#faq-template",
            initialItems: parseJson(faqField.value, []),
            emptyItem: () => ({question: "", answer: "", homepageVisible: false}),
            fillRow(row, item) {
                row.querySelector("[data-field='question']").value = item.question || "";
                row.querySelector("[data-field='answer']").value = item.answer || "";
                row.querySelector("[data-field='homepageVisible']").checked = !!item.homepageVisible;
            },
            readRow(row) {
                return {
                    question: readText(row, "[data-field='question']"),
                    answer: readText(row, "[data-field='answer']"),
                    homepageVisible: readChecked(row, "[data-field='homepageVisible']")
                };
            },
            keepItem(item) {
                return item.question || item.answer;
            }
        });

        const customEditor = createEditor({
            containerSelector: "#custom-attributes-list",
            addButtonSelector: "#add-custom-attribute",
            templateSelector: "#key-value-template",
            initialItems: Object.entries(parseJson(customField.value, {})).map(([key, value]) => ({key, value: String(value ?? "")})),
            emptyItem: () => ({key: "", value: ""}),
            fillRow(row, item) {
                row.querySelector("[data-field='key']").value = item.key || "";
                row.querySelector("[data-field='value']").value = item.value || "";
            },
            readRow(row) {
                return {key: readText(row, "[data-field='key']"), value: readText(row, "[data-field='value']")};
            },
            keepItem(item) {
                return item.key || item.value;
            }
        });

        [
            ["#review-rating", review.rating || ""],
            ["#review-count", review.count || ""],
            ["#review-label", review.label || ""],
            ["#seo-title", seo.title || ""],
            ["#seo-description", seo.description || ""],
            ["#gallery-layout", presentation.galleryLayout || "stacked"]
        ].forEach(([selector, value]) => {
            const input = form.querySelector(selector);
            if (input) {
                input.value = value;
            }
        });

        form.querySelector("#show-compare-price").checked = !!presentation.showCompareAtPrice;
        form.querySelector("#show-quantity-selector").checked = !!presentation.showQuantitySelector;
        form.querySelector("#sticky-add-to-cart").checked = !!presentation.stickyAddToCart;

        form.addEventListener("submit", () => {
            writeJson(mediaField, mediaEditor.items());
            writeJson(badgesField, badgesEditor.items().map((item) => item.value));
            writeJson(highlightsField, highlightsEditor.items().map((item) => item.value));
            writeJson(benefitsField, benefitsEditor.items().map((item) => item.value));
            writeJson(faqField, faqEditor.items());
            writeJson(reviewField, {
                ...review,
                rating: form.querySelector("#review-rating").value.trim(),
                count: form.querySelector("#review-count").value.trim(),
                label: form.querySelector("#review-label").value.trim()
            });
            writeJson(seoField, {
                ...seo,
                title: form.querySelector("#seo-title").value.trim(),
                description: form.querySelector("#seo-description").value.trim()
            });
            writeJson(customField, customEditor.items().reduce((accumulator, item) => {
                if (item.key) {
                    accumulator[item.key] = item.value;
                }
                return accumulator;
            }, {}));
            writeJson(presentationField, {
                ...presentation,
                galleryLayout: form.querySelector("#gallery-layout").value,
                showCompareAtPrice: form.querySelector("#show-compare-price").checked,
                showQuantitySelector: form.querySelector("#show-quantity-selector").checked,
                stickyAddToCart: form.querySelector("#sticky-add-to-cart").checked
            });
        });
    }

    function initOffersPage() {
        document.querySelectorAll("[data-bundle-offer-form]").forEach((form) => {
            form.addEventListener("submit", () => {
                writeJson(form.querySelector("[name='configurationJson']"), {
                    badge: form.querySelector("[data-field='badge']").value.trim()
                });
            });
        });

        document.querySelectorAll("[data-subscription-form]").forEach((form) => {
            form.addEventListener("submit", () => {
                writeJson(form.querySelector("[name='configurationJson']"), {
                    description: form.querySelector("[data-field='description']").value.trim()
                });
            });
        });
    }

    function initPageEditorPage() {
        const form = document.querySelector("#page-section-form");
        if (!form) {
            return;
        }

        const configField = form.querySelector("#configurationJson");
        const baseConfig = parseJson(configField.value, {});
        const typeInput = form.querySelector("#section-type");

        const benefitsEditor = createEditor({
            containerSelector: "#benefit-items-list",
            addButtonSelector: "#add-benefit-item",
            templateSelector: "#benefit-item-template",
            initialItems: Array.isArray(baseConfig.items) && typeInput.value === "benefits" ? baseConfig.items : [],
            emptyItem: () => ({title: "", body: ""}),
            fillRow(row, item) {
                row.querySelector("[data-field='title']").value = item.title || "";
                row.querySelector("[data-field='body']").value = item.body || "";
            },
            readRow(row) {
                return {title: readText(row, "[data-field='title']"), body: readText(row, "[data-field='body']")};
            },
            keepItem(item) {
                return item.title || item.body;
            }
        });

        const reviewsEditor = createEditor({
            containerSelector: "#review-items-list",
            addButtonSelector: "#add-review-item",
            templateSelector: "#review-item-template",
            initialItems: Array.isArray(baseConfig.items) && typeInput.value === "reviews" ? baseConfig.items : [],
            emptyItem: () => ({quote: "", name: ""}),
            fillRow(row, item) {
                row.querySelector("[data-field='quote']").value = item.quote || "";
                row.querySelector("[data-field='name']").value = item.name || "";
            },
            readRow(row) {
                return {quote: readText(row, "[data-field='quote']"), name: readText(row, "[data-field='name']")};
            },
            keepItem(item) {
                return item.quote || item.name;
            }
        });

        const comparisonEditor = createEditor({
            containerSelector: "#comparison-rows-list",
            addButtonSelector: "#add-comparison-row",
            templateSelector: "#comparison-row-template",
            initialItems: Array.isArray(baseConfig.rows) ? baseConfig.rows : [],
            emptyItem: () => ({feature: "", atlas: "", other: ""}),
            fillRow(row, item) {
                row.querySelector("[data-field='feature']").value = item.feature || "";
                row.querySelector("[data-field='atlas']").value = item.atlas || "";
                row.querySelector("[data-field='other']").value = item.other || "";
            },
            readRow(row) {
                return {
                    feature: readText(row, "[data-field='feature']"),
                    atlas: readText(row, "[data-field='atlas']"),
                    other: readText(row, "[data-field='other']")
                };
            },
            keepItem(item) {
                return item.feature || item.atlas || item.other;
            }
        });

        const specsEditor = createEditor({
            containerSelector: "#spec-items-list",
            addButtonSelector: "#add-spec-item",
            templateSelector: "#spec-item-template",
            initialItems: Array.isArray(baseConfig.items) && typeInput.value === "specs" ? baseConfig.items : [],
            emptyItem: () => ({label: "", value: ""}),
            fillRow(row, item) {
                row.querySelector("[data-field='label']").value = item.label || "";
                row.querySelector("[data-field='value']").value = item.value || "";
            },
            readRow(row) {
                return {label: readText(row, "[data-field='label']"), value: readText(row, "[data-field='value']")};
            },
            keepItem(item) {
                return item.label || item.value;
            }
        });

        const customEditor = createEditor({
            containerSelector: "#custom-section-fields-list",
            addButtonSelector: "#add-custom-field",
            templateSelector: "#key-value-template",
            initialItems: Object.entries(baseConfig).map(([key, value]) => ({key, value: String(value ?? "")})),
            emptyItem: () => ({key: "", value: ""}),
            fillRow(row, item) {
                row.querySelector("[data-field='key']").value = item.key || "";
                row.querySelector("[data-field='value']").value = item.value || "";
            },
            readRow(row) {
                return {key: readText(row, "[data-field='key']"), value: readText(row, "[data-field='value']")};
            },
            keepItem(item) {
                return item.key || item.value;
            }
        });

        [
            ["#hero-eyebrow", baseConfig.eyebrow || ""], ["#hero-title", baseConfig.title || ""], ["#hero-body", baseConfig.body || ""],
            ["#hero-cta-label", baseConfig.ctaLabel || ""], ["#hero-cta-href", baseConfig.ctaHref || ""], ["#hero-secondary-label", baseConfig.secondaryLabel || ""],
            ["#hero-secondary-href", baseConfig.secondaryHref || ""], ["#hero-media-path", baseConfig.mediaPath || ""], ["#trust-headline", baseConfig.headline || ""],
            ["#benefits-title", baseConfig.title || ""], ["#lifestyle-title", baseConfig.title || ""], ["#lifestyle-body", baseConfig.body || ""],
            ["#lifestyle-media-path", baseConfig.mediaPath || ""], ["#reviews-title", baseConfig.title || ""], ["#comparison-title", baseConfig.title || ""],
            ["#faq-title", baseConfig.title || ""], ["#faq-limit", baseConfig.limit || 3], ["#cta-title", baseConfig.title || ""],
            ["#cta-body", baseConfig.body || ""], ["#cta-label", baseConfig.ctaLabel || ""], ["#cta-href", baseConfig.ctaHref || ""],
            ["#specs-title", baseConfig.title || ""]
        ].forEach(([selector, value]) => {
            const input = form.querySelector(selector);
            if (input) {
                input.value = value;
            }
        });

        function updatePanels() {
            const type = typeInput.value || "hero";
            form.querySelectorAll("[data-section-panel]").forEach((panel) => {
                panel.classList.toggle("is-active", panel.getAttribute("data-section-panel") === type);
            });
        }

        typeInput.addEventListener("change", updatePanels);
        updatePanels();

        const createNewButton = document.querySelector("#create-new-section");
        if (createNewButton) {
            createNewButton.addEventListener("click", () => {
                form.querySelectorAll("input[type='text'], input[type='number'], textarea").forEach((input) => {
                    if (input.id !== "configurationJson") {
                        input.value = "";
                    }
                });
                form.querySelectorAll("input[type='checkbox']").forEach((input) => {
                    input.checked = input.name === "active";
                });
                form.querySelectorAll("[data-editor-row]").forEach((row) => row.remove());
                form.querySelector("#section-id").value = "";
                form.querySelector("[name='sectionKey']").value = "";
                form.querySelector("[name='displayName']").value = "";
                form.querySelector("[name='placement']").value = "top";
                form.querySelector("[name='position']").value = "0";
                form.querySelector("[name='mobilePosition']").value = "0";
                typeInput.value = "hero";
                updatePanels();
            });
        }

        form.addEventListener("submit", () => {
            let nextConfig = {};
            const type = typeInput.value || "hero";
            if (type === "hero") {
                nextConfig = {
                    eyebrow: form.querySelector("#hero-eyebrow").value.trim(),
                    title: form.querySelector("#hero-title").value.trim(),
                    body: form.querySelector("#hero-body").value.trim(),
                    ctaLabel: form.querySelector("#hero-cta-label").value.trim(),
                    ctaHref: form.querySelector("#hero-cta-href").value.trim(),
                    secondaryLabel: form.querySelector("#hero-secondary-label").value.trim(),
                    secondaryHref: form.querySelector("#hero-secondary-href").value.trim(),
                    mediaPath: form.querySelector("#hero-media-path").value.trim()
                };
            } else if (type === "trust-strip") {
                nextConfig = {headline: form.querySelector("#trust-headline").value.trim()};
            } else if (type === "benefits") {
                nextConfig = {title: form.querySelector("#benefits-title").value.trim(), items: benefitsEditor.items()};
            } else if (type === "lifestyle") {
                nextConfig = {
                    title: form.querySelector("#lifestyle-title").value.trim(),
                    body: form.querySelector("#lifestyle-body").value.trim(),
                    mediaPath: form.querySelector("#lifestyle-media-path").value.trim()
                };
            } else if (type === "reviews") {
                nextConfig = {title: form.querySelector("#reviews-title").value.trim(), items: reviewsEditor.items()};
            } else if (type === "comparison") {
                nextConfig = {title: form.querySelector("#comparison-title").value.trim(), rows: comparisonEditor.items()};
            } else if (type === "faq-preview") {
                nextConfig = {title: form.querySelector("#faq-title").value.trim(), limit: Number(form.querySelector("#faq-limit").value || 3)};
            } else if (type === "cta") {
                nextConfig = {
                    title: form.querySelector("#cta-title").value.trim(),
                    body: form.querySelector("#cta-body").value.trim(),
                    ctaLabel: form.querySelector("#cta-label").value.trim(),
                    ctaHref: form.querySelector("#cta-href").value.trim()
                };
            } else if (type === "specs") {
                nextConfig = {title: form.querySelector("#specs-title").value.trim(), items: specsEditor.items()};
            } else {
                nextConfig = customEditor.items().reduce((accumulator, item) => {
                    if (item.key) {
                        accumulator[item.key] = item.value;
                    }
                    return accumulator;
                }, {});
            }
            writeJson(configField, nextConfig);
        });
    }

    function initOrderDetailPage() {
        const form = document.querySelector("#shipment-form");
        if (!form) {
            return;
        }

        const eventsField = form.querySelector("#trackingEventsJson");
        const editor = createEditor({
            containerSelector: "#tracking-events-list",
            addButtonSelector: "#add-tracking-event",
            templateSelector: "#tracking-event-template",
            initialItems: parseJson(eventsField.value, []),
            emptyItem: () => ({eventTimestamp: new Date().toISOString(), status: "Shipment update", location: "", message: ""}),
            fillRow(row, item) {
                row.querySelector("[data-field='eventTimestamp']").value = formatInstantForInput(item.eventTimestamp);
                row.querySelector("[data-field='status']").value = item.status || "";
                row.querySelector("[data-field='location']").value = item.location || "";
                row.querySelector("[data-field='message']").value = item.message || "";
            },
            readRow(row) {
                return {
                    eventTimestamp: parseInputInstant(readText(row, "[data-field='eventTimestamp']")),
                    status: readText(row, "[data-field='status']"),
                    location: readText(row, "[data-field='location']"),
                    message: readText(row, "[data-field='message']")
                };
            },
            keepItem(item) {
                return item.status || item.location || item.message;
            }
        });

        form.addEventListener("submit", () => {
            writeJson(eventsField, editor.items());
        });
    }

    document.addEventListener("DOMContentLoaded", () => {
        initDialogs();
        initSiteSettingsPage();
        initAnalyticsPage();
        initProductFormPage();
        initOffersPage();
        initPageEditorPage();
        initOrderDetailPage();
    });
})();
