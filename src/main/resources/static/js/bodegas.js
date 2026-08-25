/*
bodegas.js - CRUD completo de bodegas (listar, crear, editar, eliminar).
Crear, editar y eliminar son exclusivos de rol ADMIN.
*/

let bodegaEnEdicion = null;
let bodegasCache = [];

function mostrarErrorBodegas(mensaje) {
    const alerta = document.getElementById('bodegasAlert');
    alerta.textContent = mensaje;
    alerta.classList.add('show');
    setTimeout(() => alerta.classList.remove('show'), 5000);
}

function mostrarExitoBodegas(mensaje) {
    const alerta = document.getElementById('bodegasAlertSuccess');
    alerta.textContent = mensaje;
    alerta.classList.add('show');
    setTimeout(() => alerta.classList.remove('show'), 4000);
}

function renderizarTablaBodegas(bodegas) {
    const tbody = document.getElementById('tablaBodegas');
    tbody.innerHTML = '';

    if (bodegas.length === 0) {
        const colspan = esAdmin() ? 6 : 5;
        tbody.innerHTML = `<tr><td colspan="${colspan}" class="empty-state">No hay bodegas registradas</td></tr>`;
        return;
    }

    bodegas.forEach((bodega) => {
        const fila = document.createElement('tr');
        const encargado = bodega.encargadoId ? bodega.encargadoId.username : '—';

        const celdaAcciones = esAdmin()
            ? `<td class="table-actions">
                   <button class="btn btn-secondary btn-sm" data-action="editar" data-id="${bodega.idBodega}">Editar</button>
                   <button class="btn btn-danger btn-sm" data-action="eliminar" data-id="${bodega.idBodega}">Eliminar</button>
               </td>`
            : '';

        fila.innerHTML = `
            <td>${bodega.idBodega}</td>
            <td>${bodega.nombre}</td>
            <td>${bodega.ubicacion}</td>
            <td>${bodega.capacidad}</td>
            <td>${encargado}</td>
            ${celdaAcciones}
        `;
        tbody.appendChild(fila);
    });

    tbody.querySelectorAll('button[data-action="editar"]').forEach((boton) => {
        boton.addEventListener('click', () => abrirModalEdicion(boton.dataset.id));
    });
    tbody.querySelectorAll('button[data-action="eliminar"]').forEach((boton) => {
        boton.addEventListener('click', () => manejarEliminarBodega(boton.dataset.id));
    });
}

async function cargarBodegas() {
    try {
        bodegasCache = await apiGet('/bodegas');
        renderizarTablaBodegas(bodegasCache);
    } catch (error) {
        mostrarErrorBodegas(error.message);
    }
}

async function cargarUsuariosParaSelector() {
    if (!esAdmin()) {
        return;
    }

    try {
        const usuarios = await apiGet('/usuarios');
        const select = document.getElementById('bodegaEncargado');
        select.innerHTML = '<option value="">Sin encargado</option>';
        usuarios.forEach((usuario) => {
            const option = document.createElement('option');
            option.value = usuario.idUsuario;
            option.textContent = `${usuario.username} (${usuario.rol})`;
            select.appendChild(option);
        });
    } catch (error) {
        mostrarErrorBodegas('No se pudo cargar la lista de encargados: ' + error.message);
    }
}

async function manejarEliminarBodega(id) {
    if (!confirm('¿Está seguro de eliminar esta bodega? Esta acción no se puede deshacer.')) {
        return;
    }

    try {
        await apiDelete(`/bodegas/${id}`);
        mostrarExitoBodegas('Bodega eliminada correctamente.');
        cargarBodegas();
    } catch (error) {
        mostrarErrorBodegas(error.message);
    }
}

/* ---------- Modal crear/editar ---------- */

function abrirModalCreacion() {
    bodegaEnEdicion = null;
    document.getElementById('modalBodegaTitulo').textContent = 'Nueva bodega';
    document.getElementById('formBodega').reset();
    document.getElementById('bodegaId').value = '';
    ocultarErrorModalBodega();
    document.getElementById('modalBodega').classList.add('show');
}

function abrirModalEdicion(id) {
    const bodega = bodegasCache.find((b) => String(b.idBodega) === String(id));
    if (!bodega) {
        return;
    }

    bodegaEnEdicion = bodega;
    document.getElementById('modalBodegaTitulo').textContent = 'Editar bodega';
    document.getElementById('bodegaId').value = bodega.idBodega;
    document.getElementById('bodegaNombre').value = bodega.nombre;
    document.getElementById('bodegaUbicacion').value = bodega.ubicacion;
    document.getElementById('bodegaCapacidad').value = bodega.capacidad;
    document.getElementById('bodegaEncargado').value = bodega.encargadoId ? bodega.encargadoId.idUsuario : '';
    ocultarErrorModalBodega();
    document.getElementById('modalBodega').classList.add('show');
}

function cerrarModalBodega() {
    document.getElementById('modalBodega').classList.remove('show');
}

function mostrarErrorModalBodega(mensaje) {
    const alerta = document.getElementById('modalBodegaAlert');
    alerta.textContent = mensaje;
    alerta.classList.add('show');
}

function ocultarErrorModalBodega() {
    const alerta = document.getElementById('modalBodegaAlert');
    alerta.classList.remove('show');
    alerta.textContent = '';
}

async function manejarSubmitBodega(evento) {
    evento.preventDefault();
    ocultarErrorModalBodega();

    const nombre = document.getElementById('bodegaNombre').value.trim();
    const ubicacion = document.getElementById('bodegaUbicacion').value.trim();
    const capacidad = parseInt(document.getElementById('bodegaCapacidad').value, 10);
    const encargadoValor = document.getElementById('bodegaEncargado').value;
    const encargadoId = encargadoValor ? parseInt(encargadoValor, 10) : null;
    const boton = document.getElementById('btnGuardarBodega');

    const datos = { nombre, ubicacion, capacidad, encargadoId };

    boton.disabled = true;
    boton.textContent = 'Guardando...';

    try {
        if (bodegaEnEdicion) {
            await apiPut(`/bodegas/${bodegaEnEdicion.idBodega}`, datos);
            mostrarExitoBodegas('Bodega actualizada correctamente.');
        } else {
            await apiPost('/bodegas', datos);
            mostrarExitoBodegas('Bodega creada correctamente.');
        }
        cerrarModalBodega();
        cargarBodegas();
    } catch (error) {
        mostrarErrorModalBodega(error.message);
    } finally {
        boton.disabled = false;
        boton.textContent = 'Guardar';
    }
}

/* ---------- Inicializacion ---------- */

function inicializarBodegas() {
    requerirAutenticacion();
    inicializarLayoutComun('bodegas');
    cargarBodegas();
    cargarUsuariosParaSelector();

    if (!esAdmin()) {
        document.getElementById('thAccionesBodega').classList.add('hidden-role');
    }

    document.getElementById('btnNuevaBodega').addEventListener('click', abrirModalCreacion);
    document.getElementById('btnCerrarModalBodega').addEventListener('click', cerrarModalBodega);
    document.getElementById('btnCancelarModalBodega').addEventListener('click', cerrarModalBodega);
    document.getElementById('formBodega').addEventListener('submit', manejarSubmitBodega);
}

document.addEventListener('DOMContentLoaded', inicializarBodegas);