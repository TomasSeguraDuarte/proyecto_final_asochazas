document.addEventListener("DOMContentLoaded", async () => {
    if (!getToken()) {
        window.location.href = "/login.html";
        return;
    }

    await initI18n();
    await sincronizarUsuarioActual();

    document.getElementById("form-editar-perfil").addEventListener("submit", guardarPerfil);
    document.getElementById("form-solicitud-vendedor").addEventListener("submit", enviarSolicitud);

    window.addEventListener("languageChanged", () => {
        cargarMisCompras();
        const user = getSessionUser();
        if (user && user.rol !== "VENDEDOR") {
            consultarMiSolicitud();
        }
    });
});

async function sincronizarUsuarioActual() {
    try {
        const perfil = await apiFetch("/auth/me");
        const rolNombre = perfil.rol.rol;

        if (rolNombre === "ADMIN") {
            window.location.href = "/admin.html";
            return;
        }

        localStorage.setItem("usuario", JSON.stringify({
            id: perfil.id,
            nombre: perfil.nombre,
            correo: perfil.correo,
            rol: rolNombre
        }));

        renderNavbar("perfil");

        document.getElementById("perfil-correo").value = perfil.correo;
        document.getElementById("perfil-rol").value = rolNombre;
        document.getElementById("perfil-nombre").value = perfil.nombre;
        document.getElementById("perfil-contacto").value = perfil.contacto || "";

        cargarMisCompras();

        if (rolNombre === "VENDEDOR") {
            document.getElementById("nav-item-solicitud").classList.add("d-none");
            document.getElementById("banner-vendedor").classList.remove("d-none");
        } else {
            consultarMiSolicitud();
        }
    } catch (err) {
        showAlert("alert-perfil", err.message, "danger");
    }
}

async function guardarPerfil(e) {
    e.preventDefault();
    const nombre = document.getElementById("perfil-nombre").value.trim();
    const contacto = document.getElementById("perfil-contacto").value.trim();

    try {
        await apiFetch("/usuarios/perfil", {
            method: "PUT",
            body: JSON.stringify({ nombre, contacto })
        });
        showAlert("alert-perfil", t("profile.updated"), "success");
        await sincronizarUsuarioActual();
    } catch (err) {
        showAlert("alert-perfil", err.message, "danger");
    }
}

async function cargarMisCompras() {
    const tbody = document.getElementById("tabla-mis-compras");
    try {
        const lista = await apiFetch("/interacciones/mis-contactos");
        if (!lista.length) {
            tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted">${t("purchases.empty")}</td></tr>`;
            return;
        }
        tbody.innerHTML = lista.map(i => {
            const est = i.estado.estadoInteraccion;
            const badge = est === "VENTA_CONFIRMADA" ? "bg-success" : est === "NO_CONCRETADA" ? "bg-danger" : "bg-warning text-dark";
            const vendedor = i.producto.vendedor;
            return `
                <tr>
                    <td class="fw-semibold">
                        <a href="/producto.html?id=${i.producto.id}" class="text-decoration-none text-dark">
                            ${escapeHtml(i.producto.nombre)}
                        </a>
                    </td>
                    <td>
                        <a href="/chaza.html?vendedorId=${vendedor.id}" class="fw-semibold text-decoration-none" style="color: var(--usco-vinotinto);">
                            ${escapeHtml(vendedor.nombre)}
                        </a>
                        <div class="small text-muted">${escapeHtml(vendedor.contacto || vendedor.correo)}</div>
                    </td>
                    <td>${i.cantidad}</td>
                    <td>${formatCOP(i.precio_unitario)}</td>
                    <td class="fw-bold">${formatCOP(i.precio_unitario * i.cantidad)}</td>
                    <td><span class="badge ${badge}">${tStatus(est)}</span></td>
                </tr>
            `;
        }).join("");
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="6" class="text-danger">${escapeHtml(err.message)}</td></tr>`;
    }
}

async function consultarMiSolicitud() {
    const div = document.getElementById("estado-mi-solicitud");
    try {
        const sol = await apiFetch("/solicitudes/mi-solicitud");
        const est = sol.estado.estadoSolicitud;
        const badge = est === "APROBADA" ? "bg-success" : est === "RECHAZADA" ? "bg-danger" : "bg-warning text-dark";
        div.innerHTML = `
            <p class="mb-1"><strong>${t("th.proposal")}:</strong> ${escapeHtml(sol.propuesta_nombre)}</p>
            <p class="mb-2"><strong>${t("th.status")}:</strong> <span class="badge ${badge}">${tStatus(est)}</span></p>
            ${sol.justificacion_admin ? `<div class="alert alert-secondary small mb-0"><strong>${t("req.admin_reply")}:</strong> ${escapeHtml(sol.justificacion_admin)}</div>` : ""}
        `;
    } catch {
        div.innerHTML = `<p class="text-muted mb-0">${t("req.none")}</p>`;
    }
}

async function enviarSolicitud(e) {
    e.preventDefault();
    const propuestaNombre = document.getElementById("sol-nombre").value.trim();
    const propuestaDescripcion = document.getElementById("sol-descripcion").value.trim();

    try {
        await apiFetch("/solicitudes", {
            method: "POST",
            body: JSON.stringify({ propuestaNombre, propuestaDescripcion })
        });
        showAlert("alert-perfil", t("req.sent"), "success");
        document.getElementById("form-solicitud-vendedor").reset();
        consultarMiSolicitud();
    } catch (err) {
        showAlert("alert-perfil", err.message, "danger");
    }
}