/*
inventario.js - Consulta de inventario (solo lectura) y filtro de stock bajo.
*/

function mostrarErrorInventario(mensaje) {
    const alerta = document.getElementById('inventarioAlert');
    alerta.textContent = mensaje;
    alerta.classList.add('show');
    setTimeout(() => alerta.classList.remove('show'), 5000);
}

function badgeStock(stock, limite) {
    if (stock < limite) {
        return `<span class="badge badge-danger">${stock}</span>`;
    }
    return `<span class="badge badge-success">${stock}</span>`;
}

function renderizarTablaInventario(inventario, limiteReferencia) {
    const tbody = document.getElementById('tablaInventario');
    tbody.innerHTML = '';

    if (inventario.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" class="empty-state">No hay registros de inventario</td></tr>';
        return;
    }

    inventario.forEach((item) => {
        const fila = document.createElement('tr');
        fila.innerHTML = `
            <td>${item.idInvBodega}</td>
            <td>${item.bodegaId.nombre}</td>
            <td>${item.productoId.nombre}</td>
            <td>${badgeStock(item.stock, limiteReferencia)}</td>
        `;
        tbody.appendChild(fila);
    });
}

async function cargarInventarioCompleto() {
    try {
        const inventario = await apiGet('/inventario');
        renderizarTablaInventario(inventario, 10);
    } catch (error) {
        mostrarErrorInventario(error.message);
    }
}

async function cargarStockBajo() {
    const limite = parseInt(document.getElementById('filtroStockBajo').value, 10) || 10;

    try {
        const inventario = await apiGet(`/inventario/stock-bajo?limite=${limite}`);
        renderizarTablaInventario(inventario, limite);
    } catch (error) {
        mostrarErrorInventario(error.message);
    }
}

function inicializarInventario() {
    requerirAutenticacion();
    inicializarLayoutComun('inventario');
    cargarInventarioCompleto();

    document.getElementById('btnAplicarFiltro').addEventListener('click', cargarStockBajo);
    document.getElementById('btnLimpiarFiltro').addEventListener('click', cargarInventarioCompleto);
}

document.addEventListener('DOMContentLoaded', inicializarInventario);