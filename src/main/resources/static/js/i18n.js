const SUPPORTED_LANGS = ["es", "en", "pt"];
let currentDict = {};
let currentLang = "es";

function getSavedLang() {
    const saved = localStorage.getItem("lang");
    if (saved && SUPPORTED_LANGS.includes(saved)) return saved;

    const browserLang = (navigator.language || "es").slice(0, 2).toLowerCase();
    return SUPPORTED_LANGS.includes(browserLang) ? browserLang : "es";
}

function getCurrentLang() {
    return currentLang;
}

// Traducción nativa con soporte de interpolación {param}
function t(key, params = {}) {
    let text = currentDict[key] || key;
    for (const [k, v] of Object.entries(params)) {
        text = text.replace(new RegExp(`\\{${k}\\}`, "g"), v);
    }
    return text;
}

// Traduce visualmente las categorías almacenadas en BD sin alterar su valor real
function tCat(dbCategory) {
    const key = `cat.${dbCategory}`;
    return currentDict[key] ? currentDict[key] : dbCategory;
}

// Traduce visualmente los estados almacenados en BD sin alterar su valor real
function tStatus(dbStatus) {
    const key = `status.${dbStatus}`;
    return currentDict[key] ? currentDict[key] : dbStatus;
}

function applyStaticTranslations() {
    document.documentElement.lang = currentLang;

    document.querySelectorAll("[data-i18n]").forEach(el => {
        const key = el.getAttribute("data-i18n");
        el.textContent = t(key);
    });

    document.querySelectorAll("[data-i18n-placeholder]").forEach(el => {
        const key = el.getAttribute("data-i18n-placeholder");
        el.setAttribute("placeholder", t(key));
    });
}

async function setLanguage(lang) {
    if (!SUPPORTED_LANGS.includes(lang)) lang = "es";
    try {
        const res = await fetch(`/i18n/${lang}.json`);
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        currentDict = await res.json();
        currentLang = lang;
        localStorage.setItem("lang", lang);

        applyStaticTranslations();
        window.dispatchEvent(new CustomEvent("languageChanged", { detail: lang }));
    } catch (err) {
        console.error("Error cargando idioma:", err);
    }
}

async function initI18n() {
    await setLanguage(getSavedLang());
}