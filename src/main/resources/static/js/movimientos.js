/*
movimientos.js - Registro y consulta de movimientos de inventario.
El usuario responsable siempre queda autoasignado (resuelto por el backend).
*/

let bodegasCache = [];
let productosCache = [];
let contadorFilasProducto = 0;

function mostrarErrorMovimientos(mensaje) {
    const alerta = document.getElementById('movimientosAlert');
    alerta.textContent = mensaje;
    alerta.classList.add('show');
    setTimeout(() => alerta.classList.remove('show'), 5000);
}

function mostrarExitoMovimientos(mensaje) {
    const alerta = document.getElementById('movimientosAlertSuccess');
    alerta.textContent = mensaje;
    alerta.classList.add('show');
    setTimeout(() => alerta.classList.remove('show'), 4000);
}

function badgeTipoMovimiento(tipo) {
    if (tipo === 'ENTRADA') {
        return '<span class="badge badge-success">ENTRADA</span>';
    }
    if (tipo === 'SALIDA') {
        return '<span class="badge badge-danger">SALIDA</span>';
    }
    return '<span class="badge badge-warning">TRANSFERENCIA</span>';
}

function formatearFecha(fechaIso) {
    const fecha = new Date(fechaIso);
    return fecha.toLocaleString('es-CO', { dateStyle: 'short', timeStyle: 'short' });
}

function formatearDetalle(detalles) {
    if (!detalles || detalles.length === 0) {
        return '—';
    }
    return detalles.map((d) => `${d.productoId.nombre} (${d.cantidad})`).join(', ');
}

function renderizarTablaMovimientos(movimientos) {
    const tbody = document.getElementById('tablaMovimientos');
    tbody.innerHTML = '';

    if (movimientos.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="empty-state">No hay movimientos registrados</td></tr>';
        return;
    }

    const ordenados = [...movimientos].sort((a, b) => new Date(b.fecha) - new Date(a.fecha));

    ordenados.forEach((movimiento) => {
        const fila = document.createElement('tr');
        const origen = movimiento.bodegaOrigenId ? movimiento.bodegaOrigenId.nombre : '—';
        const destino = movimiento.bodegaDestinoId ? movimiento.bodegaDestinoId.nombre : '—';

        fila.innerHTML = `
            <td>${movimiento.idMovimiento}</td>
            <td>${formatearFecha(movimiento.fecha)}</td>
            <td>${badgeTipoMovimiento(movimiento.tipo)}</td>
            <td>${movimiento.usuarioId.username}</td>
            <td>${origen}</td>
            <td>${destino}</td>
            <td>${formatearDetalle(movimiento.detalles)}</td>
        `;
        tbody.appendChild(fila);
    });
}

async function cargarMovimientos() {
    try {
        const movimientos = await apiGet('/movimientos');
        renderizarTablaMovimientos(movimientos);
    } catch (error) {
        mostrarErrorMovimientos(error.message);
    }
}

async function aplicarFiltroFecha() {
    const desde = document.getElementById('filtroDesde').value;
    const hasta = document.getElementById('filtroHasta').value;

    if (!desde || !hasta) {
        mostrarErrorMovimientos('Debe seleccionar fecha de inicio y de fin.');
        return;
    }

    try {
        const movimientos = await apiGet(`/movimientos/rango?desde=${desde}:00&hasta=${hasta}:00`);
        renderizarTablaMovimientos(movimientos);
    } catch (error) {
        mostrarErrorMovimientos(error.message);
    }
}

/* ---------- Carga de catalogos para el formulario ---------- */

async function cargarCatalogos() {
    try {
        [bodegasCache, productosCache] = await Promise.all([
            apiGet('/bodegas'),
            apiGet('/productos'),
        ]);
        poblarSelectBodegas(document.getElementById('movimientoBodegaOrigen'));
        poblarSelectBodegas(document.getElementById('movimientoBodegaDestino'));
    } catch (error) {
        mostrarErrorMovimientos('No se pudieron cargar los catálogos: ' + error.message);
    }
}

function poblarSelectBodegas(select) {
    select.innerHTML = '';
    bodegasCache.forEach((bodega) => {
        const option = document.createElement('option');
        option.value = bodega.idBodega;
        option.textContent = bodega.nombre;
        select.appendChild(option);
    });
}

/* ---------- Formulario dinamico segun tipo ---------- */

function actualizarCamposSegunTipo() {
    const tipo = document.getElementById('movimientoTipo').value;
    const grupoOrigen = document.getElementById('grupoBodegaOrigen');
    const grupoDestino = document.getElementById('grupoBodegaDestino');

    if (tipo === 'ENTRADA') {
        grupoOrigen.style.display = 'none';
        grupoDestino.style.display = 'block';
    } else if (tipo === 'SALIDA') {
        grupoOrigen.style.display = 'block';
        grupoDestino.style.display = 'none';
    } else {
        grupoOrigen.style.display = 'block';
        grupoDestino.style.display = 'block';
    }
}

/* ---------- Filas dinamicas de producto + cantidad ---------- */

function agregarFilaProducto() {
    contadorFilasProducto += 1;
    const idFila = `filaProducto${contadorFilasProducto}`;

    const contenedor = document.getElementById('listaProductosMovimiento');
    const fila = document.createElement('div');
    fila.id = idFila;
    fila.style.display = 'flex';
    fila.style.gap = '8px';
    fila.style.marginBottom = '8px';

    const selectProducto = document.createElement('select');
    selectProducto.className = 'select-producto-fila';
    selectProducto.style.flex = '2';
    productosCache.forEach((producto) => {
        const option = document.createElement('option');
        option.value = producto.idProducto;
        option.textContent = producto.nombre;
        selectProducto.appendChild(option);
    });

    const inputCantidad = document.createElement('input');
    inputCantidad.type = 'number';
    inputCantidad.className = 'input-cantidad-fila';
    inputCantidad.min = '1';
    inputCantidad.value = '1';
    inputCantidad.style.flex = '1';

    const botonQuitar = document.createElement('button');
    botonQuitar.type = 'button';
    botonQuitar.className = 'btn btn-danger btn-sm';
    botonQuitar.textContent = '×';
    botonQuitar.style.width = 'auto';
    botonQuitar.addEventListener('click', () => fila.remove());

    fila.appendChild(selectProducto);
    fila.appendChild(inputCantidad);
    fila.appendChild(botonQuitar);
    contenedor.appendChild(fila);
}

function limpiarFilasProducto() {
    document.getElementById('listaProductosMovimiento').innerHTML = '';
    contadorFilasProducto = 0;
}

function obtenerProductosYCantidades() {
    const productoIds = [];
    const cantidades = [];

    document.querySelectorAll('#listaProductosMovimiento > div').forEach((fila) => {
        const select = fila.querySelector('.select-producto-fila');
        const input = fila.querySelector('.input-cantidad-fila');
        productoIds.push(parseInt(select.value, 10));
        cantidades.push(parseInt(input.value, 10));
    });

    return { productoIds, cantidades };
}

/* ---------- Modal ---------- */

function abrirModalMovimiento() {
    document.getElementById('formMovimiento').reset();
    limpiarFilasProducto();
    agregarFilaProducto();
    actualizarCamposSegunTipo();
    ocultarErrorModalMovimiento();
    document.getElementById('modalMovimiento').classList.add('show');
}

function cerrarModalMovimiento() {
    document.getElementById('modalMovimiento').classList.remove('show');
}

function mostrarErrorModalMovimiento(mensaje) {
    const alerta = document.getElementById('modalMovimientoAlert');
    alerta.textContent = mensaje;
    alerta.classList.add('show');
}

function ocultarErrorModalMovimiento() {
    const alerta = document.getElementById('modalMovimientoAlert');
    alerta.classList.remove('show');
    alerta.textContent = '';
}

async function manejarSubmitMovimiento(evento) {
    evento.preventDefault();
    ocultarErrorModalMovimiento();

    const tipo = document.getElementById('movimientoTipo').value;
    const { productoIds, cantidades } = obtenerProductosYCantidades();

    if (productoIds.length === 0) {
        mostrarErrorModalMovimiento('Debe agregar al menos un producto.');
        return;
    }

    const datos = { tipo, productoIds, cantidades, bodegaOrigenId: null, bodegaDestinoId: null };

    if (tipo === 'ENTRADA' || tipo === 'TRANSFERENCIA') {
        datos.bodegaDestinoId = parseInt(document.getElementById('movimientoBodegaDestino').value, 10);
    }
    if (tipo === 'SALIDA' || tipo === 'TRANSFERENCIA') {
        datos.bodegaOrigenId = parseInt(document.getElementById('movimientoBodegaOrigen').value, 10);
    }

    const boton = document.getElementById('btnGuardarMovimiento');
    boton.disabled = true;
    boton.textContent = 'Registrando...';

    try {
        await apiPost('/movimientos', datos);
        cerrarModalMovimiento();
        mostrarExitoMovimientos('Movimiento registrado correctamente.');
        cargarMovimientos();
    } catch (error) {
        mostrarErrorModalMovimiento(error.message);
    } finally {
        boton.disabled = false;
        boton.textContent = 'Registrar';
    }
}

/* ---------- Inicializacion ---------- */

function inicializarMovimientos() {
    requerirAutenticacion();
    inicializarLayoutComun('movimientos');
    cargarMovimientos();
    cargarCatalogos();

    document.getElementById('btnNuevoMovimiento').addEventListener('click', abrirModalMovimiento);
    document.getElementById('btnCerrarModalMovimiento').addEventListener('click', cerrarModalMovimiento);
    document.getElementById('btnCancelarModalMovimiento').addEventListener('click', cerrarModalMovimiento);
    document.getElementById('formMovimiento').addEventListener('submit', manejarSubmitMovimiento);
    document.getElementById('movimientoTipo').addEventListener('change', actualizarCamposSegunTipo);
    document.getElementById('btnAgregarProducto').addEventListener('click', agregarFilaProducto);
    document.getElementById('btnAplicarFiltroFecha').addEventListener('click', aplicarFiltroFecha);
    document.getElementById('btnLimpiarFiltroFecha').addEventListener('click', cargarMovimientos);
}

document.addEventListener('DOMContentLoaded', inicializarMovimientos);