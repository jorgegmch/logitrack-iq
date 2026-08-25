/*
usuarios.js - Gestion de usuarios: listar, registrar, activar/desactivar.
Pagina exclusiva para rol ADMIN.
*/

function mostrarErrorUsuarios(mensaje) {
    const alerta = document.getElementById('usuariosAlert');
    alerta.textContent = mensaje;
    alerta.classList.add('show');
    setTimeout(() => alerta.classList.remove('show'), 5000);
}

function mostrarExitoUsuarios(mensaje) {
    const alerta = document.getElementById('usuariosAlertSuccess');
    alerta.textContent = mensaje;
    alerta.classList.add('show');
    setTimeout(() => alerta.classList.remove('show'), 4000);
}

function badgeRol(rol) {
    return `<span class="role-badge">${rol}</span>`;
}

function badgeEstado(activo) {
    return activo
        ? '<span class="badge badge-success">ACTIVO</span>'
        : '<span class="badge badge-danger">INACTIVO</span>';
}

function renderizarTablaUsuarios(usuarios) {
    const tbody = document.getElementById('tablaUsuarios');
    tbody.innerHTML = '';

    if (usuarios.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="empty-state">No hay usuarios registrados</td></tr>';
        return;
    }

    usuarios.forEach((usuario) => {
        const fila = document.createElement('tr');
        const textoBoton = usuario.activo ? 'Desactivar' : 'Reactivar';
        const claseBoton = usuario.activo ? 'btn-danger' : 'btn-secondary';

        fila.innerHTML = `
            <td>${usuario.idUsuario}</td>
            <td>${usuario.username}</td>
            <td>${badgeRol(usuario.rol)}</td>
            <td>${badgeEstado(usuario.activo)}</td>
            <td class="table-actions">
                <button class="btn ${claseBoton} btn-sm" data-id="${usuario.idUsuario}" data-activo="${usuario.activo}">
                    ${textoBoton}
                </button>
            </td>
        `;
        tbody.appendChild(fila);
    });

    tbody.querySelectorAll('button[data-id]').forEach((boton) => {
        boton.addEventListener('click', manejarCambioEstado);
    });
}

async function cargarUsuarios() {
    try {
        const usuarios = await apiGet('/usuarios');
        renderizarTablaUsuarios(usuarios);
    } catch (error) {
        mostrarErrorUsuarios(error.message);
    }
}

async function manejarCambioEstado(evento) {
    const boton = evento.currentTarget;
    const id = boton.dataset.id;
    const estaActivo = boton.dataset.activo === 'true';
    const endpoint = estaActivo ? `/usuarios/${id}/desactivar` : `/usuarios/${id}/reactivar`;

    boton.disabled = true;

    try {
        await apiPatch(endpoint);
        mostrarExitoUsuarios(`Usuario ${estaActivo ? 'desactivado' : 'reactivado'} correctamente.`);
        cargarUsuarios();
    } catch (error) {
        mostrarErrorUsuarios(error.message);
        boton.disabled = false;
    }
}

/* ---------- Modal de registro ---------- */

function abrirModalRegistro() {
    document.getElementById('formRegistro').reset();
    ocultarErrorModal();
    document.getElementById('modalRegistro').classList.add('show');
}

function cerrarModalRegistro() {
    document.getElementById('modalRegistro').classList.remove('show');
}

function mostrarErrorModal(mensaje) {
    const alerta = document.getElementById('modalAlert');
    alerta.textContent = mensaje;
    alerta.classList.add('show');
}

function ocultarErrorModal() {
    const alerta = document.getElementById('modalAlert');
    alerta.classList.remove('show');
    alerta.textContent = '';
}

async function manejarSubmitRegistro(evento) {
    evento.preventDefault();
    ocultarErrorModal();

    const username = document.getElementById('nuevoUsername').value.trim();
    const password = document.getElementById('nuevoPassword').value;
    const rol = document.getElementById('nuevoRol').value;
    const boton = document.getElementById('btnGuardarUsuario');

    boton.disabled = true;
    boton.textContent = 'Registrando...';

    try {
        await apiPost('/auth/register', { username, password, rol });
        cerrarModalRegistro();
        mostrarExitoUsuarios(`Usuario "${username}" registrado correctamente.`);
        cargarUsuarios();
    } catch (error) {
        mostrarErrorModal(error.message);
    } finally {
        boton.disabled = false;
        boton.textContent = 'Registrar';
    }
}

/* ---------- Inicializacion ---------- */

function inicializarUsuarios() {
    requerirAutenticacion();

    if (!esAdmin()) {
        window.location.href = '/html/dashboard.html';
        return;
    }

    inicializarLayoutComun('usuarios');
    cargarUsuarios();

    document.getElementById('btnAbrirRegistro').addEventListener('click', abrirModalRegistro);
    document.getElementById('btnCerrarModal').addEventListener('click', cerrarModalRegistro);
    document.getElementById('btnCancelarModal').addEventListener('click', cerrarModalRegistro);
    document.getElementById('formRegistro').addEventListener('submit', manejarSubmitRegistro);
}

document.addEventListener('DOMContentLoaded', inicializarUsuarios);