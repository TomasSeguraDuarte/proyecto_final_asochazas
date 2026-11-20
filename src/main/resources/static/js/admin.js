document.addEventListener("DOMContentLoaded", async () => {
    const user = getSessionUser();
    if (!user || user.rol !== "ADMIN") {
        window.location.href = "/index.html";
        return;
    }

    await initI18n();
    renderNavbar("admin");
    cargarSolicitudesPendientes();
    cargarUsuarios();
    cargarProductosAdmin();

    window.addEventListener("languageChanged", () => {
        cargarSolicitudesPendientes();
        cargarUsuarios();
        cargarProductosAdmin();
    });
});

async function cargarSolicitudesPendientes() {
    const tbody = document.getElementById("tabla-solicitudes");
    try {
        const lista = await apiFetch("/solicitudes/pendientes");
        if (!lista.length) {
            tbody.innerHTML = `<tr><td colspan="4" class="text-center text-muted">${t("admin.pending_empty")}</td></tr>`;
            return;
        }
        tbody.innerHTML = lista.map(s => `
            <tr>
                <td>
                    <div class="fw-semibold">${escapeHtml(s.usuario.nombre)}</div>
                    <small class="text-muted">${escapeHtml(s.usuario.correo)}</small>
                </td>
                <td class="fw-bold">${escapeHtml(s.propuesta_nombre)}</td>
                <td>${escapeHtml(s.propuesta_descripcion)}</td>
                <td>
                    <button class="btn btn-sm btn-success me-1" onclick="aprobarSolicitud(${s.id})">${t("admin.approve")}</button>
                    <button class="btn btn-sm btn-outline-danger" onclick="rechazarSolicitud(${s.id})">${t("admin.reject")}</button>
                </td>
            </tr>
        `).join("");
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="4" class="text-danger">${escapeHtml(err.message)}</td></tr>`;
    }
}

async function aprobarSolicitud(id) {
    try {
        await apiFetch(`/solicitudes/${id}/aprobar`, { method: "PUT" });
        showAlert("alert-admin", t("admin.approved_msg"), "success");
        cargarSolicitudesPendientes();
        cargarUsuarios();
    } catch (err) {
        showAlert("alert-admin", err.message, "danger");
    }
}

async function rechazarSolicitud(id) {
    const justificacion = prompt(t("admin.reject_prompt"));
    if (!justificacion || !justificacion.trim()) return;

    try {
        await apiFetch(`/solicitudes/${id}/rechazar`, {
            method: "PUT",
            body: JSON.stringify({ justificacion: justificacion.trim() })
        });
        showAlert("alert-admin", t("admin.rejected_msg"), "warning");
        cargarSolicitudesPendientes();
    } catch (err) {
        showAlert("alert-admin", err.message, "danger");
    }
}

async function cargarUsuarios() {
    const tbody = document.getElementById("tabla-usuarios");
    const adminActual = getSessionUser();
    try {
        const usuarios = await apiFetch("/usuarios");
        tbody.innerHTML = usuarios.map(u => {
            const rol = u.rol.rol;
            const estado = u.estado.estadoUsuario;
            const badgeEstado = estado === "ACTIVO" ? "bg-success" : "bg-danger";

            let botones = "";
            if (u.id !== adminActual.id) {
                if (estado === "ACTIVO") {
                    botones += `<button class="btn btn-sm btn-outline-danger me-1" onclick="suspenderUsuario(${u.id})">${t("admin.suspend")}</button>`;
                } else {
                    botones += `<button class="btn btn-sm btn-outline-success me-1" onclick="reactivarUsuario(${u.id})">${t("admin.reactivate")}</button>`;
                }
                if (rol === "VENDEDOR") {
                    botones += `<button class="btn btn-sm btn-outline-warning text-dark me-1" onclick="revocarVendedor(${u.id})">${t("admin.revoke_seller")}</button>`;
                }
                if (rol !== "ADMIN" && estado === "ACTIVO") {
                    botones += `<button class="btn btn-sm btn-outline-dark" onclick="promoverAdmin(${u.id}, '${escapeHtml(u.nombre)}')">${t("admin.make_admin")}</button>`;
                }
            } else {
                botones = `<span class="text-muted small">${t("admin.your_account")}</span>`;
            }

            return `
                <tr>
                    <td>${u.id}</td>
                    <td class="fw-semibold">${escapeHtml(u.nombre)}</td>
                    <td>${escapeHtml(u.correo)}</td>
                    <td><span class="badge bg-dark">${rol}</span></td>
                    <td><span class="badge ${badgeEstado}">${tStatus(estado)}</span></td>
                    <td>${botones}</td>
                </tr>
            `;
        }).join("");
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="6" class="text-danger">${escapeHtml(err.message)}</td></tr>`;
    }
}

async function suspenderUsuario(id) {
    const justificacion = prompt(t("admin.suspend_prompt"));
    if (!justificacion || !justificacion.trim()) return;
    try {
        await apiFetch(`/usuarios/${id}/suspender`, {
            method: "PUT",
            body: JSON.stringify({ justificacion: justificacion.trim() })
        });
        showAlert("alert-admin", t("admin.suspended_msg"), "warning");
        cargarUsuarios();
    } catch (err) {
        showAlert("alert-admin", err.message, "danger");
    }
}

async function reactivarUsuario(id) {
    try {
        await apiFetch(`/usuarios/${id}/reactivar`, { method: "PUT" });
        showAlert("alert-admin", t("admin.reactivated_msg"), "success");
        cargarUsuarios();
    } catch (err) {
        showAlert("alert-admin", err.message, "danger");
    }
}

async function revocarVendedor(id) {
    const justificacion = prompt(t("admin.revoke_prompt"));
    if (!justificacion || !justificacion.trim()) return;
    try {
        await apiFetch(`/usuarios/${id}/revocar-vendedor`, {
            method: "PUT",
            body: JSON.stringify({ justificacion: justificacion.trim() })
        });
        showAlert("alert-admin", t("admin.revoked_msg"), "warning");
        cargarUsuarios();
        cargarProductosAdmin();
    } catch (err) {
        showAlert("alert-admin", err.message, "danger");
    }
}

async function promoverAdmin(id, nombre) {
    if (!confirm(t("admin.promote_confirm", { name: nombre }))) return;
    try {
        await apiFetch(`/usuarios/${id}/promover-admin`, { method: "PUT" });
        showAlert("alert-admin", t("admin.promoted_msg", { name: nombre }), "success");
        cargarUsuarios();
        cargarProductosAdmin();
    } catch (err) {
        showAlert("alert-admin", err.message, "danger");
    }
}

async function cargarProductosAdmin() {
    const tbody = document.getElementById("tabla-productos-admin");
    try {
        const productos = await apiFetch("/productos/admin/todos");
        tbody.innerHTML = productos.map(p => {
            const est = p.estado.estadoProducto;
            const badge = est === "ACTIVO" ? "bg-success" : est === "OCULTO" ? "bg-danger" : "bg-secondary";

            let accion = `<span class="text-muted small">-</span>`;
            if (est === "ACTIVO" || est === "PAUSADO") {
                accion = `<button class="btn btn-sm btn-outline-danger" onclick="ocultarProducto(${p.id})">${t("admin.hide")}</button>`;
            } else if (est === "OCULTO") {
                accion = `<button class="btn btn-sm btn-outline-success" onclick="restaurarProducto(${p.id})">${t("admin.restore")}</button>`;
            }

            return `
                <tr>
                    <td>${p.id}</td>
                    <td class="fw-semibold">${escapeHtml(p.nombre)}</td>
                    <td>${escapeHtml(p.vendedor.nombre)}</td>
                    <td>${formatCOP(p.precio)}</td>
                    <td><span class="badge ${badge}">${tStatus(est)}</span></td>
                    <td>${accion}</td>
                </tr>
            `;
        }).join("");
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="6" class="text-danger">${escapeHtml(err.message)}</td></tr>`;
    }
}

async function ocultarProducto(id) {
    const justificacion = prompt(t("admin.hide_prompt"));
    if (!justificacion || !justificacion.trim()) return;
    try {
        await apiFetch(`/productos/${id}/ocultar`, {
            method: "PUT",
            body: JSON.stringify({ justificacion: justificacion.trim() })
        });
        showAlert("alert-admin", t("admin.hidden_msg"), "warning");
        cargarProductosAdmin();
    } catch (err) {
        showAlert("alert-admin", err.message, "danger");
    }
}

async function restaurarProducto(id) {
    try {
        await apiFetch(`/productos/${id}/restaurar`, { method: "PUT" });
        showAlert("alert-admin", t("admin.restored_msg"), "success");
        cargarProductosAdmin();
    } catch (err) {
        showAlert("alert-admin", err.message, "danger");
    }
}