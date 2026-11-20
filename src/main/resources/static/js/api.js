const API_BASE = "/api";
let currentActiveNavPage = "";

function getToken() {
    return localStorage.getItem("token");
}

function getSessionUser() {
    const raw = localStorage.getItem("usuario");
    return raw ? JSON.parse(raw) : null;
}

function saveSession(loginData) {
    localStorage.setItem("token", loginData.token);
    localStorage.setItem("usuario", JSON.stringify({
        id: loginData.id,
        nombre: loginData.nombre,
        correo: loginData.correo,
        rol: loginData.rol
    }));
}

function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("usuario");
    window.location.href = "/index.html";
}

async function apiFetch(endpoint, options = {}) {
    const headers = {
        "Content-Type": "application/json",
        "Accept-Language": typeof getCurrentLang === "function" ? getCurrentLang() : "es",
        ...(options.headers || {})
    };

    const token = getToken();
    if (token) {
        headers["Authorization"] = `Bearer ${token}`;
    }

    const response = await fetch(`${API_BASE}${endpoint}`, {
        ...options,
        headers
    });

    if (response.status === 401) {
        logout();
        throw new Error(t("auth.session_expired"));
    }

    const text = await response.text();
    let data = null;
    try {
        data = text ? JSON.parse(text) : null;
    } catch {
        data = text;
    }

    if (!response.ok) {
        const msg = (data && data.message) || (data && data.error) || `Error ${response.status}`;
        throw new Error(msg);
    }

    return data;
}

function renderNavbar(activePage = currentActiveNavPage) {
    currentActiveNavPage = activePage;
    const container = document.getElementById("navbar-container");
    if (!container) return;

    const user = getSessionUser();
    const lang = typeof getCurrentLang === "function" ? getCurrentLang() : "es";

    let navItems = `
        <li class="nav-item">
            <a class="nav-link ${activePage === 'catalogo' ? 'active' : ''}" href="/index.html">${t("nav.catalog")}</a>
        </li>
    `;

    if (user) {
        if (user.rol === "ADMIN") {
            navItems += `
                <li class="nav-item">
                    <a class="nav-link ${activePage === 'admin' ? 'active' : ''}" href="/admin.html">${t("nav.admin")}</a>
                </li>
            `;
        } else {
            if (user.rol === "VENDEDOR") {
                navItems += `
                    <li class="nav-item">
                        <a class="nav-link ${activePage === 'mi-chaza' ? 'active' : ''}" href="/mi-chaza.html">${t("nav.my_chaza")}</a>
                    </li>
                `;
            }
            navItems += `
                <li class="nav-item">
                    <a class="nav-link ${activePage === 'perfil' ? 'active' : ''}" href="/mi-perfil.html">${t("nav.profile")}</a>
                </li>
            `;
        }
    }

    const langSelector = `
        <select id="lang-selector" aria-label="Idioma" class="form-select form-select-sm bg-dark text-white border-secondary" style="width: auto;" onchange="setLanguage(this.value)">
            <option value="es" ${lang === "es" ? "selected" : ""}>ES</option>
            <option value="en" ${lang === "en" ? "selected" : ""}>EN</option>
            <option value="pt" ${lang === "pt" ? "selected" : ""}>PT</option>
        </select>
    `;

    let rightSection = "";
    if (user) {
        rightSection = `
            <div class="d-flex align-items-center gap-2 text-white">
                ${langSelector}
                <span class="small ms-1">
                    <i class="bi bi-person-circle me-1"></i>
                    <strong>${escapeHtml(user.nombre)}</strong>
                    <span class="badge bg-warning text-dark ms-1">${user.rol}</span>
                </span>
                <button class="btn btn-sm btn-dorado" onclick="logout()">${t("nav.logout")}</button>
            </div>
        `;
    } else {
        rightSection = `
            <div class="d-flex align-items-center gap-2">
                ${langSelector}
                <a href="/login.html" class="btn btn-sm btn-outline-light">${t("nav.login")}</a>
                <a href="/register.html" class="btn btn-sm btn-dorado">${t("nav.register")}</a>
            </div>
        `;
    }

    container.innerHTML = `
        <nav class="navbar navbar-expand-lg navbar-dark navbar-usco shadow-sm">
            <div class="container">
                <a class="navbar-brand" href="/index.html">
                    <i class="bi bi-shop me-2"></i>Aso<span>Chazas</span> USCO
                </a>
                <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navUsco">
                    <span class="navbar-toggler-icon"></span>
                </button>
                <div class="collapse navbar-collapse" id="navUsco">
                    <ul class="navbar-nav me-auto mb-2 mb-lg-0">
                        ${navItems}
                    </ul>
                    ${rightSection}
                </div>
            </div>
        </nav>
    `;
}

window.addEventListener("languageChanged", () => renderNavbar());

function showAlert(containerId, message, type = "danger") {
    const el = document.getElementById(containerId);
    if (!el) return;
    el.innerHTML = `
        <div class="alert alert-${type} alert-dismissible fade show" role="alert">
            ${escapeHtml(message)}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;
}

function formatCOP(valor) {
    const locale = currentLang === "en" ? "en-US" : currentLang === "pt" ? "pt-BR" : "es-CO";
    return new Intl.NumberFormat(locale, {
        style: "currency",
        currency: "COP",
        maximumFractionDigits: 0
    }).format(valor || 0);
}

function getCategoryIcon(categoria) {
    const map = {
        "Comida": "bi-cup-hot-fill",
        "Tecnología": "bi-laptop",
        "Ropa": "bi-bag-heart-fill",
        "Accesorios": "bi-watch",
        "Papelería": "bi-journal-bookmark-fill",
        "Servicios": "bi-tools",
        "Otros": "bi-box-seam-fill"
    };
    return map[categoria] || "bi-tag-fill";
}

function escapeHtml(str) {
    if (str === null || str === undefined) return "";
    return String(str)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;");
}