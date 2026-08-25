/*
dashboard.js - Logica de la pantalla principal (resumen general).
*/

function mostrarErrorDashboard(mensaje) {
    const alerta = document.getElementById('dashboardAlert');
    alerta.textContent = mensaje;
    alerta.classList.add('show');
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

function renderizarUltimosMovimientos(movimientos) {
    const tbody = document.getElementById('tablaUltimosMovimientos');
    tbody.innerHTML = '';

    if (movimientos.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="empty-state">No hay movimientos registrados</td></tr>';
        return;
    }

    const ordenados = [...movimientos].sort((a, b) => new Date(b.fecha) - new Date(a.fecha));
    const ultimosCinco = ordenados.slice(0, 5);

    ultimosCinco.forEach((movimiento) => {
        const fila = document.createElement('tr');
        const bodegaOrigen = movimiento.bodegaOrigenId ? movimiento.bodegaOrigenId.nombre : '—';
        const bodegaDestino = movimiento.bodegaDestinoId ? movimiento.bodegaDestinoId.nombre : '—';

        fila.innerHTML = `
            <td>${formatearFecha(movimiento.fecha)}</td>
            <td>${badgeTipoMovimiento(movimiento.tipo)}</td>
            <td>${movimiento.usuarioId.username}</td>
            <td>${bodegaOrigen}</td>
            <td>${bodegaDestino}</td>
        `;
        tbody.appendChild(fila);
    });
}

function obtenerColorAcento() {
    return getComputedStyle(document.documentElement).getPropertyValue('--accent').trim();
}

function obtenerColorTexto() {
    return getComputedStyle(document.documentElement).getPropertyValue('--text-muted').trim();
}

function renderizarGraficoStockPorBodega(datos) {
    const ctx = document.getElementById('chartStockBodega');

    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: datos.map((item) => item.nombreBodega),
            datasets: [{
                label: 'Stock total',
                data: datos.map((item) => item.stockTotal),
                backgroundColor: obtenerColorAcento(),
                borderRadius: 6,
            }],
        },
        options: {
            responsive: true,
            animation: {
                duration: 900,
                easing: 'easeOutQuart',
            },
            plugins: {
                legend: { display: false },
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: { color: obtenerColorTexto() },
                    grid: { color: 'rgba(148, 163, 184, 0.15)' },
                },
                x: {
                    ticks: { color: obtenerColorTexto() },
                    grid: { display: false },
                },
            },
        },
    });
}

function renderizarGraficoProductosMovidos(datos) {
    const ctx = document.getElementById('chartProductosMovidos');

    new Chart(ctx, {
        type: 'bar',
        data: {
            labels: datos.map((item) => item.nombreProducto),
            datasets: [{
                label: 'Cantidad movida',
                data: datos.map((item) => item.cantidadTotalMovida),
                backgroundColor: obtenerColorAcento(),
                borderRadius: 6,
            }],
        },
        options: {
            indexAxis: 'y',
            responsive: true,
            animation: {
                duration: 900,
                easing: 'easeOutQuart',
            },
            plugins: {
                legend: { display: false },
            },
            scales: {
                x: {
                    beginAtZero: true,
                    ticks: { color: obtenerColorTexto() },
                    grid: { color: 'rgba(148, 163, 184, 0.15)' },
                },
                y: {
                    ticks: { color: obtenerColorTexto() },
                    grid: { display: false },
                },
            },
        },
    });
}

async function cargarDashboard() {
    try {
        const [productos, bodegas, stockBajo, movimientos, resumen] = await Promise.all([
            apiGet('/productos'),
            apiGet('/bodegas'),
            apiGet('/inventario/stock-bajo?limite=10'),
            apiGet('/movimientos'),
            apiGet('/reportes/resumen'),
        ]);

        document.getElementById('statProductos').textContent = productos.length;
        document.getElementById('statBodegas').textContent = bodegas.length;
        document.getElementById('statStockBajo').textContent = stockBajo.length;
        document.getElementById('statMovimientos').textContent = movimientos.length;

        renderizarUltimosMovimientos(movimientos);
        renderizarGraficoStockPorBodega(resumen.stockTotalPorBodega);
        renderizarGraficoProductosMovidos(resumen.productosMasMovidos);
    } catch (error) {
        mostrarErrorDashboard(error.message);
    }
}

function inicializarDashboard() {
    requerirAutenticacion();
    inicializarLayoutComun('dashboard');
    cargarDashboard();
}

document.addEventListener('DOMContentLoaded', inicializarDashboard);