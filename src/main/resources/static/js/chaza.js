document.addEventListener("DOMContentLoaded", async () => {
    await initI18n();
    renderNavbar("catalogo");

    const params = new URLSearchParams(window.location.search);
    const vendedorId = Number(params.get("vendedorId"));
    if (!vendedorId) {
        window.location.href = "/index.html";
        return;
    }

    cargarChaza(vendedorId);
    window.addEventListener("languageChanged", () => cargarChaza(vendedorId));
});

async function cargarChaza(vendedorId) {
    const grid = document.getElementById("grid-chaza-productos");

    try {
        const todos = await apiFetch("/productos");
        const productosVendedor = (todos || []).filter(p => p.vendedor && p.vendedor.id === vendedorId);

        document.getElementById("chaza-total").textContent = productosVendedor.length;

        if (productosVendedor.length === 0) {
            document.getElementById("chaza-nombre").textContent = t("chaza.empty_title");
            document.getElementById("chaza-contacto").textContent = t("chaza.empty_subtitle");
            grid.innerHTML = `
                <div class="col-12 text-center py-5">
                    <i class="bi bi-shop-window fs-1 text-muted"></i>
                    <p class="mt-2 text-muted">${t("chaza.empty_grid")}</p>
                </div>
            `;
            return;
        }

        const vendedor = productosVendedor[0].vendedor;
        document.title = `${t("chaza.title_prefix")} ${vendedor.nombre} - AsoChazas USCO`;
        document.getElementById("chaza-nombre").textContent = `${t("chaza.title_prefix")} ${vendedor.nombre}`;
        document.getElementById("chaza-contacto").textContent =
            vendedor.contacto ? `${t("chaza.direct_contact")}: ${vendedor.contacto} • ${vendedor.correo}` : vendedor.correo;

        grid.innerHTML = productosVendedor.map(p => {
            const catDb = p.categoria ? p.categoria.categoria : "Otros";
            return `
                <div class="col-sm-6 col-md-4 col-lg-3">
                    <div class="card card-producto h-100">
                        <div class="categoria-banner">
                            <span class="fw-bold small text-uppercase">${escapeHtml(tCat(catDb))}</span>
                            <i class="bi ${getCategoryIcon(catDb)}"></i>
                        </div>
                        <div class="card-body d-flex flex-column">
                            <h5 class="card-title fw-bold mb-1">${escapeHtml(p.nombre)}</h5>
                            <p class="card-text text-muted small flex-grow-1">
                                ${escapeHtml(p.descripcion.length > 90 ? p.descripcion.substring(0, 90) + "..." : p.descripcion)}
                            </p>
                            <div class="mt-2 pt-2 border-top">
                                <div class="d-flex justify-content-between align-items-center mb-3">
                                    <span class="precio-tag">${formatCOP(p.precio)}</span>
                                    <span class="badge bg-light text-dark border">${t("catalog.stock")}: ${p.stock}</span>
                                </div>
                                <a href="/producto.html?id=${p.id}" class="btn btn-usco w-100 btn-sm">
                                    ${t("chaza.view_reserve")}
                                </a>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        }).join("");
    } catch (err) {
        showAlert("alert-chaza", err.message, "danger");
        grid.innerHTML = "";
    }
}