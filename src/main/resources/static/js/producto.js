document.addEventListener("DOMContentLoaded", async () => {
    await initI18n();
    renderNavbar("catalogo");

    const params = new URLSearchParams(window.location.search);
    const id = params.get("id");
    if (!id) {
        window.location.href = "/index.html";
        return;
    }

    cargarDetalleProducto(id);
    window.addEventListener("languageChanged", () => cargarDetalleProducto(id));
});

async function cargarDetalleProducto(productoId) {
    const contenedor = document.getElementById("contenedor-detalle");

    try {
        const p = await apiFetch(`/productos/${productoId}`);
        const catDb = p.categoria ? p.categoria.categoria : "Otros";
        const catTraducida = tCat(catDb);
        const user = getSessionUser();

        document.title = `${p.nombre} - AsoChazas USCO`;
        const breadCat = document.getElementById("bread-categoria");
        breadCat.textContent = catTraducida;
        breadCat.href = `/index.html?categoria=${encodeURIComponent(catDb)}`;
        document.getElementById("bread-nombre").textContent = p.nombre;

        let bloqueAccion = "";
        if (!user) {
            bloqueAccion = `
                <div class="alert alert-warning mb-0">
                    <i class="bi bi-lock-fill me-1"></i>
                    ${t("prod_detail.must_login_prefix")} <a href="/login.html" class="fw-bold">${t("prod_detail.must_login_link")}</a> ${t("prod_detail.must_login_suffix")}
                </div>
            `;
        } else if (user.rol === "ADMIN") {
            bloqueAccion = `
                <div class="alert alert-secondary mb-0">
                    ${t("prod_detail.admin_no_buy")}
                </div>
            `;
        } else if (user.id === p.vendedor.id) {
            bloqueAccion = `
                <div class="alert alert-info d-flex justify-content-between align-items-center mb-0">
                    <span>${t("prod_detail.own_product")}</span>
                    <a href="/mi-chaza.html" class="btn btn-sm btn-usco">${t("prod_detail.manage_chaza")}</a>
                </div>
            `;
        } else if (p.stock <= 0) {
            bloqueAccion = `
                <div class="alert alert-danger mb-0">
                    ${t("prod_detail.out_of_stock")}
                </div>
            `;
        } else {
            bloqueAccion = `
                <form id="form-reservar-producto" class="border rounded p-3 bg-light">
                    <h6 class="fw-bold mb-2">${t("prod_detail.reserve_title")}</h6>
                    <div class="row g-2 align-items-end">
                        <div class="col-sm-4">
                            <label for="input-cantidad" class="form-label small fw-semibold">${t("prod_detail.qty_label")}</label>
                            <input type="number" id="input-cantidad" class="form-control" min="1" max="${p.stock}" value="1" required>
                        </div>
                        <div class="col-sm-8 d-grid">
                            <button type="submit" class="btn btn-usco">
                                <i class="bi bi-chat-dots-fill me-1"></i>${t("prod_detail.reserve_btn")}
                            </button>
                        </div>
                    </div>
                    <div class="form-text mt-2">${t("prod_detail.reserve_help")}</div>
                </form>
            `;
        }

        contenedor.innerHTML = `
            <div class="row g-0">
                <div class="col-md-5 d-flex flex-column justify-content-center align-items-center p-5 text-center"
                     style="background-color: var(--usco-dorado-suave); border-top-left-radius: 8px; border-bottom-left-radius: 8px;">
                    <i class="bi ${getCategoryIcon(catDb)}" style="font-size: 5rem; color: var(--usco-vinotinto);"></i>
                    <span class="badge bg-dark mt-3 text-uppercase">${escapeHtml(catTraducida)}</span>
                </div>
                <div class="col-md-7">
                    <div class="card-body p-4">
                        <div class="d-flex justify-content-between align-items-start gap-2 mb-2">
                            <h2 class="fw-bold mb-0">${escapeHtml(p.nombre)}</h2>
                            <span class="badge bg-success fs-6">${t("catalog.stock")}: ${p.stock}</span>
                        </div>

                        <div class="precio-tag fs-2 mb-3">${formatCOP(p.precio)}</div>

                        <h6 class="fw-bold text-uppercase small text-muted">${t("prod_detail.desc_heading")}</h6>
                        <p class="mb-4">${escapeHtml(p.descripcion)}</p>

                        <div class="card border mb-4">
                            <div class="card-body py-2 px-3 d-flex justify-content-between align-items-center">
                                <div>
                                    <small class="text-muted d-block">${t("prod_detail.published_by")}</small>
                                    <a href="/chaza.html?vendedorId=${p.vendedor.id}" class="fw-bold text-decoration-none" style="color: var(--usco-vinotinto);">
                                        <i class="bi bi-shop me-1"></i>${escapeHtml(p.vendedor.nombre)}
                                    </a>
                                    <div class="small text-muted">
                                        ${p.vendedor.contacto ? `${t("prod_detail.contact_prefix")}: ${escapeHtml(p.vendedor.contacto)}` : escapeHtml(p.vendedor.correo)}
                                    </div>
                                </div>
                                <a href="/chaza.html?vendedorId=${p.vendedor.id}" class="btn btn-sm btn-outline-usco">
                                    ${t("prod_detail.view_chaza")}
                                </a>
                            </div>
                        </div>

                        ${bloqueAccion}
                    </div>
                </div>
            </div>
        `;

        const form = document.getElementById("form-reservar-producto");
        if (form) {
            form.addEventListener("submit", async (e) => {
                e.preventDefault();
                const cantidad = Number(document.getElementById("input-cantidad").value);
                try {
                    await apiFetch("/interacciones", {
                        method: "POST",
                        body: JSON.stringify({ productoId: p.id, cantidad })
                    });
                    showAlert("alert-producto", t("prod_detail.reserve_success"), "success");
                    cargarDetalleProducto(p.id);
                } catch (err) {
                    showAlert("alert-producto", err.message, "danger");
                }
            });
        }

        cargarRelacionados(catDb, p.id);
    } catch (err) {
        contenedor.innerHTML = `<div class="card-body p-4 text-center text-danger">${escapeHtml(err.message)}</div>`;
    }
}

async function cargarRelacionados(categoriaDb, productoActualId) {
    try {
        const lista = await apiFetch(`/productos?categoria=${encodeURIComponent(categoriaDb)}`);
        const relacionados = (lista || []).filter(item => item.id !== productoActualId).slice(0, 4);

        if (relacionados.length === 0) return;

        document.getElementById("seccion-relacionados").classList.remove("d-none");
        document.getElementById("grid-relacionados").innerHTML = relacionados.map(p => `
            <div class="col-sm-6 col-md-3">
                <div class="card card-producto h-100">
                    <div class="categoria-banner py-2">
                        <span class="fw-bold small">${escapeHtml(tCat(categoriaDb))}</span>
                        <i class="bi ${getCategoryIcon(categoriaDb)} fs-5"></i>
                    </div>
                    <div class="card-body d-flex flex-column">
                        <h6 class="fw-bold mb-1">${escapeHtml(p.nombre)}</h6>
                        <div class="precio-tag fs-5 mt-auto mb-2">${formatCOP(p.precio)}</div>
                        <a href="/producto.html?id=${p.id}" class="btn btn-outline-usco btn-sm w-100">${t("prod_detail.view_detail")}</a>
                    </div>
                </div>
            </div>
        `).join("");
    } catch {
    }
}