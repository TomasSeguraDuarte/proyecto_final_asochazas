document.addEventListener("DOMContentLoaded", async () => {
    await initI18n();
    renderNavbar("catalogo");

    const paramsUrl = new URLSearchParams(window.location.search);
    const catInicial = paramsUrl.get("categoria") || "";
    if (catInicial) {
        document.getElementById("select-categoria").value = catInicial;
    }

    cargarCatalogo(catInicial, "");

    document.getElementById("form-filtros").addEventListener("submit", (e) => {
        e.preventDefault();
        cargarCatalogo(
            document.getElementById("select-categoria").value,
            document.getElementById("input-busqueda").value.trim()
        );
    });

    document.getElementById("select-categoria").addEventListener("change", () => {
        cargarCatalogo(
            document.getElementById("select-categoria").value,
            document.getElementById("input-busqueda").value.trim()
        );
    });

    window.addEventListener("languageChanged", () => {
        cargarCatalogo(
            document.getElementById("select-categoria").value,
            document.getElementById("input-busqueda").value.trim()
        );
    });
});

async function cargarCatalogo(categoria = "", q = "") {
    const grid = document.getElementById("grid-productos");
    grid.innerHTML = `<div class="text-center py-5 text-muted">${t("catalog.loading")}</div>`;

    try {
        const params = new URLSearchParams();
        if (categoria) params.append("categoria", categoria);
        if (q) params.append("q", q);

        const queryStr = params.toString() ? `?${params.toString()}` : "";
        const productos = await apiFetch(`/productos${queryStr}`);

        if (!productos || productos.length === 0) {
            grid.innerHTML = `
                <div class="col-12 text-center py-5">
                    <i class="bi bi-inbox fs-1 text-muted"></i>
                    <p class="mt-2 text-muted">${t("catalog.empty")}</p>
                </div>
            `;
            return;
        }

        grid.innerHTML = productos.map(p => {
            const catDb = p.categoria ? p.categoria.categoria : "Otros";
            const vendedorNombre = p.vendedor ? p.vendedor.nombre : "USCO";
            const vendedorId = p.vendedor ? p.vendedor.id : "";

            return `
                <div class="col-sm-6 col-md-4 col-lg-3">
                    <div class="card card-producto h-100">
                        <a href="/producto.html?id=${p.id}" class="text-decoration-none">
                            <div class="categoria-banner">
                                <span class="fw-bold small text-uppercase">${escapeHtml(tCat(catDb))}</span>
                                <i class="bi ${getCategoryIcon(catDb)}"></i>
                            </div>
                        </a>
                        <div class="card-body d-flex flex-column">
                            <h5 class="card-title fw-bold mb-1">
                                <a href="/producto.html?id=${p.id}" class="text-decoration-none text-dark">
                                    ${escapeHtml(p.nombre)}
                                </a>
                            </h5>
                            <p class="card-text text-muted small flex-grow-1">
                                ${escapeHtml(p.descripcion.length > 90 ? p.descripcion.substring(0, 90) + "..." : p.descripcion)}
                            </p>
                            <div class="mt-2 pt-2 border-top">
                                <div class="d-flex justify-content-between align-items-center mb-2">
                                    <span class="precio-tag">${formatCOP(p.precio)}</span>
                                    <span class="badge bg-light text-dark border">${t("catalog.stock")}: ${p.stock}</span>
                                </div>
                                <div class="small mb-3">
                                    <a href="/chaza.html?vendedorId=${vendedorId}" class="text-muted text-decoration-none">
                                        <i class="bi bi-shop me-1"></i>${escapeHtml(vendedorNombre)}
                                    </a>
                                </div>
                                <a href="/producto.html?id=${p.id}" class="btn btn-usco w-100 btn-sm">
                                    ${t("catalog.view_btn")}
                                </a>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        }).join("");
    } catch (err) {
        showAlert("alert-catalogo", err.message, "danger");
        grid.innerHTML = "";
    }
}