let modalEditProd = null;

document.addEventListener("DOMContentLoaded", async () => {
    if (!getToken()) {
        window.location.href = "/login.html";
        return;
    }

    await initI18n();

    try {
        const perfil = await apiFetch("/auth/me");
        if (perfil.rol.rol !== "VENDEDOR") {
            window.location.href = "/mi-perfil.html";
            return;
        }

        localStorage.setItem("usuario", JSON.stringify({
            id: perfil.id,
            nombre: perfil.nombre,
            correo: perfil.correo,
            rol: perfil.rol.rol
        }));

        renderNavbar("mi-chaza");
        document.getElementById("link-vista-publica").href = `/chaza.html?vendedorId=${perfil.id}`;
    } catch {
        window.location.href = "/login.html";
        return;
    }

    modalEditProd = new bootstrap.Modal(document.getElementById("modalEditarProducto"));
    cargarMisProductos();
    cargarVentasRecibidas();

    document.getElementById("form-crear-producto").addEventListener("submit", crearProducto);
    document.getElementById("form-editar-producto").addEventListener("submit", actualizarProducto);

    window.addEventListener("languageChanged", () => {
        cargarMisProductos();
        cargarVentasRecibidas();
    });
});

async function cargarMisProductos() {
    const tbody = document.getElementById("tabla-mis-productos");
    try {
        const productos = await apiFetch("/productos/mis-productos");

        const activos = productos.filter(p => p.estado.estadoProducto === "ACTIVO");
        const stockTotal = activos.reduce((acc, p) => acc + (p.stock || 0), 0);
        document.getElementById("stat-activos").textContent = activos.length;
        document.getElementById("stat-stock").textContent = stockTotal;

        if (!productos.length) {
            tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted">${t("prod.empty")}</td></tr>`;
            return;
        }

        tbody.innerHTML = productos.map(p => {
            const est = p.estado.estadoProducto;
            const badge = est === "ACTIVO" ? "bg-success" : est === "PAUSADO" ? "bg-secondary" : "bg-danger";
            const disabled = est === "OCULTO" ? "disabled" : "";
            return `
                <tr>
                    <td class="fw-semibold">
                        <a href="/producto.html?id=${p.id}" class="text-decoration-none text-dark">${escapeHtml(p.nombre)}</a>
                    </td>
                    <td>${escapeHtml(tCat(p.categoria.categoria))}</td>
                    <td>${formatCOP(p.precio)}</td>
                    <td>${p.stock}</td>
                    <td><span class="badge ${badge}">${tStatus(est)}</span></td>
                    <td>
                        <button class="btn btn-sm btn-outline-primary me-1" ${disabled} onclick='abrirModalEditar(${JSON.stringify(p)})'>
                            <i class="bi bi-pencil"></i>
                        </button>
                        <button class="btn btn-sm btn-outline-danger" onclick="eliminarProducto(${p.id})">
                            <i class="bi bi-trash"></i>
                        </button>
                    </td>
                </tr>
            `;
        }).join("");
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="6" class="text-danger">${escapeHtml(err.message)}</td></tr>`;
    }
}

async function crearProducto(e) {
    e.preventDefault();
    const payload = {
        nombre: document.getElementById("prod-nombre").value.trim(),
        categoria: document.getElementById("prod-categoria").value,
        precio: Number(document.getElementById("prod-precio").value),
        stock: Number(document.getElementById("prod-stock").value),
        descripcion: document.getElementById("prod-descripcion").value.trim()
    };

    try {
        await apiFetch("/productos", {
            method: "POST",
            body: JSON.stringify(payload)
        });
        showAlert("alert-chaza-panel", t("prod.created"), "success");
        document.getElementById("form-crear-producto").reset();
        cargarMisProductos();
    } catch (err) {
        showAlert("alert-chaza-panel", err.message, "danger");
    }
}

function abrirModalEditar(p) {
    document.getElementById("edit-prod-id").value = p.id;
    document.getElementById("edit-prod-nombre").value = p.nombre;
    document.getElementById("edit-prod-categoria").value = p.categoria.categoria;
    document.getElementById("edit-prod-precio").value = p.precio;
    document.getElementById("edit-prod-stock").value = p.stock;
    document.getElementById("edit-prod-estado").value = p.estado.estadoProducto;
    document.getElementById("edit-prod-descripcion").value = p.descripcion;
    modalEditProd.show();
}

async function actualizarProducto(e) {
    e.preventDefault();
    const id = document.getElementById("edit-prod-id").value;
    const payload = {
        nombre: document.getElementById("edit-prod-nombre").value.trim(),
        categoria: document.getElementById("edit-prod-categoria").value,
        precio: Number(document.getElementById("edit-prod-precio").value),
        stock: Number(document.getElementById("edit-prod-stock").value),
        estado: document.getElementById("edit-prod-estado").value,
        descripcion: document.getElementById("edit-prod-descripcion").value.trim()
    };

    try {
        await apiFetch(`/productos/${id}`, {
            method: "PUT",
            body: JSON.stringify(payload)
        });
        modalEditProd.hide();
        showAlert("alert-chaza-panel", t("prod.updated"), "success");
        cargarMisProductos();
    } catch (err) {
        showAlert("alert-chaza-panel", err.message, "danger");
    }
}

async function eliminarProducto(id) {
    if (!confirm(t("prod.delete_confirm"))) return;
    try {
        await apiFetch(`/productos/${id}`, { method: "DELETE" });
        showAlert("alert-chaza-panel", t("prod.deleted"), "success");
        cargarMisProductos();
    } catch (err) {
        showAlert("alert-chaza-panel", err.message, "danger");
    }
}

async function cargarVentasRecibidas() {
    const tbody = document.getElementById("tabla-ventas-recibidas");
    try {
        const lista = await apiFetch("/interacciones/recibidas");

        const pendientes = lista.filter(i => i.estado.estadoInteraccion === "CONTACTADO").length;
        const confirmadas = lista.filter(i => i.estado.estadoInteraccion === "VENTA_CONFIRMADA").length;
        document.getElementById("stat-pendientes").textContent = pendientes;
        document.getElementById("stat-confirmadas").textContent = confirmadas;

        if (!lista.length) {
            tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted">${t("sales.empty")}</td></tr>`;
            return;
        }

        tbody.innerHTML = lista.map(i => {
            const est = i.estado.estadoInteraccion;
            const badge = est === "VENTA_CONFIRMADA" ? "bg-success" : est === "NO_CONCRETADA" ? "bg-danger" : "bg-warning text-dark";
            const acciones = est === "CONTACTADO"
                ? `
                    <button class="btn btn-sm btn-success me-1" onclick="resolverInteraccion(${i.id}, 'confirmar')">${t("sales.confirm")}</button>
                    <button class="btn btn-sm btn-outline-danger" onclick="resolverInteraccion(${i.id}, 'cancelar')">${t("sales.cancel")}</button>
                  `
                : `<span class="text-muted small">${t("sales.closed")}</span>`;

            return `
                <tr>
                    <td class="fw-semibold">${escapeHtml(i.producto.nombre)}</td>
                    <td>
                        <div>${escapeHtml(i.usuario.nombre)}</div>
                        <small class="text-muted">${escapeHtml(i.usuario.correo)}</small>
                    </td>
                    <td>${i.cantidad}</td>
                    <td class="fw-bold">${formatCOP(i.precio_unitario * i.cantidad)}</td>
                    <td><span class="badge ${badge}">${tStatus(est)}</span></td>
                    <td>${acciones}</td>
                </tr>
            `;
        }).join("");
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="6" class="text-danger">${escapeHtml(err.message)}</td></tr>`;
    }
}

async function resolverInteraccion(id, accion) {
    try {
        await apiFetch(`/interacciones/${id}/${accion}`, { method: "PUT" });
        showAlert("alert-chaza-panel", accion === "confirmar" ? t("sales.confirmed_msg") : t("sales.canceled_msg"), "success");
        cargarVentasRecibidas();
        cargarMisProductos();
    } catch (err) {
        showAlert("alert-chaza-panel", err.message, "danger");
    }
}