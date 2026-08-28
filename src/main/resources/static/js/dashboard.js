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
    if (!fechaIso) return '—';
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

/* ============================================
Adiciones para LogiTrack IQ — torre de control
(agregado al final de dashboard.js existente,
reutiliza apiGet/apiPost/apiPatch/esAdmin/
formatearFecha ya definidos en api.js y arriba
en este mismo archivo)
============================================ */

let ordenesBorradorCacheIQ = [];

function mostrarErrorIQ(mensaje) {
    mostrarErrorDashboard(mensaje);
}

function mostrarExitoIQ(mensaje) {
    const alerta = document.getElementById('dashboardAlertSuccessIQ');
    if (!alerta) return;
    alerta.textContent = mensaje;
    alerta.classList.add('show');
    setTimeout(() => alerta.classList.remove('show'), 4000);
}

function formatearMonedaIQ(valor) {
    if (valor === null || valor === undefined) return '—';
    return new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(valor);
}

/* ---------- KPIs de LogiTrack IQ ---------- */

function renderizarKpisIQ(kpi) {
    document.getElementById('statQuiebre').textContent = kpi.productosEnQuiebre ?? '—';
    document.getElementById('statRiesgo').textContent = kpi.productosEnRiesgo ?? '—';
    document.getElementById('statOrdenes').textContent = kpi.ordenesPorAprobar ? kpi.ordenesPorAprobar.cantidad : '—';
    document.getElementById('statOrdenesMonto').textContent = kpi.ordenesPorAprobar
        ? formatearMonedaIQ(kpi.ordenesPorAprobar.montoTotal) : '';
    document.getElementById('statCalculadoEn').textContent = formatearFecha(kpi.calculadoEn);

    const tbodyOcupacion = document.getElementById('tablaOcupacionIQ');
    if (!kpi.ocupacionPorBodega || kpi.ocupacionPorBodega.length === 0) {
        tbodyOcupacion.innerHTML = '<tr><td colspan="2" class="empty-state">Sin datos</td></tr>';
    } else {
        tbodyOcupacion.innerHTML = kpi.ocupacionPorBodega.map((b) => {
            const critica = b.porcentaje >= 90;
            const badgeClase = critica ? 'badge-danger' : 'badge-success';
            return `<tr><td>${b.nombre}</td><td><span class="badge ${badgeClase}">${b.porcentaje.toFixed(1)}%</span></td></tr>`;
        }).join('');
    }

    if (kpi.movimientosAyer) {
        document.getElementById('movEntradaIQ').textContent = kpi.movimientosAyer.entrada ?? 0;
        document.getElementById('movSalidaIQ').textContent = kpi.movimientosAyer.salida ?? 0;
        document.getElementById('movTransferenciaIQ').textContent = kpi.movimientosAyer.transferencia ?? 0;
    }
}

async function cargarKpisIQ() {
    const kpi = await apiGet('/kpis');
    renderizarKpisIQ(kpi);
}

/* ---------- Resumen del panel ---------- */

function renderizarResumenIQ(resumen) {
    if (!resumen) {
        document.getElementById('resumenNarrativaIQ').textContent = 'Sin resumen publicado todavía.';
        document.getElementById('listaAlertasIQ').innerHTML = '<div class="empty-state">Sin alertas</div>';
        document.getElementById('listaAccionesIQ').innerHTML = '<div class="empty-state">Sin acciones sugeridas</div>';
        return;
    }

    let contenido;
    try {
        contenido = JSON.parse(resumen.contenidoJson);
    } catch (error) {
        document.getElementById('resumenNarrativaIQ').textContent = 'No se pudo interpretar el resumen publicado.';
        return;
    }

    document.getElementById('resumenNarrativaIQ').textContent = contenido.narrativa || '—';

    const listaAlertas = document.getElementById('listaAlertasIQ');
    const alertas = contenido.alertas || [];
    listaAlertas.innerHTML = alertas.length === 0
        ? '<div class="empty-state">Sin alertas</div>'
        : alertas.map((a) => `
            <div class="item-card">
                <div class="item-titulo">${a.titulo || a.severidad}</div>
                <div class="item-detalle">${a.detalle || ''}</div>
            </div>`).join('');

    const listaAcciones = document.getElementById('listaAccionesIQ');
    const acciones = contenido.accionesSugeridas || [];
    listaAcciones.innerHTML = acciones.length === 0
        ? '<div class="empty-state">Sin acciones sugeridas</div>'
        : acciones.map((a) => `
            <div class="item-card">
                <div class="item-titulo">${a.tipo}</div>
                <div class="item-detalle">${a.descripcion || ''}</div>
            </div>`).join('');
}

async function cargarResumenIQ() {
    try {
        const resumen = await apiGet('/panel/resumen');
        renderizarResumenIQ(resumen);
    } catch (error) {
        // 404: no hay resumen publicado todavía — no es un error real.
        renderizarResumenIQ(null);
    }
}

/* ---------- Productos en riesgo ---------- */

function renderizarRiesgoIQ(productos) {
    const tbody = document.getElementById('tablaRiesgoIQ');
    if (!productos || productos.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="empty-state">No hay productos en riesgo</td></tr>';
        return;
    }

    tbody.innerHTML = productos.map((p) => {
        const estadoBadge = p.estadoCobertura === 'SIN_CONSUMO'
            ? '<span class="badge badge-warning">SIN_CONSUMO</span>'
            : '<span class="badge badge-danger">CON_CONSUMO</span>';
        return `
            <tr>
                <td>${p.nombreProducto}</td>
                <td>${p.stockTotal}</td>
                <td>${p.consumoDiarioPromedio ?? '—'}</td>
                <td>${p.puntoReorden ?? '—'}</td>
                <td>${p.diasCobertura ?? 'Sin cobertura'}</td>
                <td>${estadoBadge}</td>
                <td>${p.bodegaDestinoId ?? '—'}</td>
            </tr>`;
    }).join('');
}

async function cargarRiesgoIQ() {
    const productos = await apiGet('/productos/riesgo');
    renderizarRiesgoIQ(productos);
}

/* ---------- Órdenes en BORRADOR ---------- */

function renderizarOrdenesIQ(ordenes) {
    ordenesBorradorCacheIQ = ordenes;
    const tbody = document.getElementById('tablaOrdenesIQ');

    if (!ordenes || ordenes.length === 0) {
        tbody.innerHTML = '<tr><td colspan="9" class="empty-state">No hay órdenes en BORRADOR</td></tr>';
        return;
    }

    tbody.innerHTML = ordenes.map((o) => {
        const botonPdf = o.pdfGenerado
            ? `<button class="btn btn-secondary btn-sm" data-action="ver-pdf" data-id="${o.idOrdenCompra}">Ver PDF</button>`
            : `<button class="btn btn-secondary btn-sm" data-action="generar-pdf" data-id="${o.idOrdenCompra}">Generar PDF</button>`;

        const botonAprobar = esAdmin()
            ? `<button class="btn btn-primary btn-sm" data-action="aprobar" data-id="${o.idOrdenCompra}">Aprobar</button> <button class="btn btn-danger btn-sm" data-action="cancelar" data-id="${o.idOrdenCompra}">Cancelar</button>`
            : '';

        return `
            <tr>
                <td>${o.idOrdenCompra}</td>
                <td>${o.productoId ? o.productoId.nombre : '—'}</td>
                <td>${o.proveedorId ? o.proveedorId.nombre : '—'}</td>
                <td>${o.bodegaDestinoId ? o.bodegaDestinoId.nombre : '—'}</td>
                <td>${o.cantidad}</td>
                <td>${formatearMonedaIQ(o.total)}</td>
                <td>${formatearFecha(o.fechaCreacion)}</td>
                <td>${botonPdf}</td>
                <td class="table-actions">${botonAprobar}</td>
            </tr>`;
    }).join('');

    tbody.querySelectorAll('button[data-action="generar-pdf"]').forEach((boton) => {
        boton.addEventListener('click', () => manejarGenerarPdfIQ(boton.dataset.id));
    });
    tbody.querySelectorAll('button[data-action="ver-pdf"]').forEach((boton) => {
        boton.addEventListener('click', () => manejarVerPdfIQ(boton.dataset.id));
    });
    tbody.querySelectorAll('button[data-action="aprobar"]').forEach((boton) => {
        boton.addEventListener('click', () => manejarAprobarIQ(boton.dataset.id));
    });
    tbody.querySelectorAll('button[data-action="cancelar"]').forEach((boton) => {
        boton.addEventListener('click', () => manejarCambiarEstadoIQ(boton.dataset.id, 'CANCELADA', '¿Cancelar esta orden de compra? Esta acción no se puede deshacer.').then(cargarOrdenesBorradorIQ));
    });
}

async function cargarOrdenesBorradorIQ() {
    const ordenes = await apiGet('/ordenes?estado=BORRADOR');
    renderizarOrdenesIQ(ordenes);
}

async function manejarGenerarPdfIQ(id) {
    try {
        const blob = await apiFetch(`/ordenes/${id}/pdf`, { method: 'POST' });
        abrirPdfEnModalIQ(blob);
        mostrarExitoIQ('PDF generado correctamente.');
        cargarOrdenesBorradorIQ();
    } catch (error) {
        mostrarErrorIQ(error.message);
    }
}

async function manejarVerPdfIQ(id) {
    try {
        const blob = await apiGet(`/ordenes/${id}/pdf`);
        abrirPdfEnModalIQ(blob);
    } catch (error) {
        mostrarErrorIQ(error.message);
    }
}

function abrirPdfEnModalIQ(blob) {
    const url = URL.createObjectURL(blob);
    document.getElementById('framePdfIQ').src = url;
    document.getElementById('modalPdfIQ').classList.add('show');
}

function cerrarModalPdfIQ() {
    document.getElementById('modalPdfIQ').classList.remove('show');
    document.getElementById('framePdfIQ').src = '';
}

async function manejarAprobarIQ(id) {
    if (!confirm('¿Aprobar esta orden de compra?')) {
        return;
    }
    try {
        await apiPatch(`/ordenes/${id}/estado`, { estado: 'APROBADA' });
        mostrarExitoIQ('Orden aprobada correctamente.');
        await Promise.all([cargarOrdenesBorradorIQ(), cargarKpisIQ(), cargarHistoricoIQ()]);
    } catch (error) {
        mostrarErrorIQ(error.message);
    }
}

/* ---------- Inicialización de la sección LogiTrack IQ ---------- */

async function cargarTorreControlIQ() {
    try {
        await Promise.all([cargarKpisIQ(), cargarResumenIQ(), cargarRiesgoIQ(), cargarOrdenesBorradorIQ()]);
    } catch (error) {
        mostrarErrorIQ(error.message);
    }
}

/* ---------- Histórico de todas las órdenes (cualquier estado) ---------- */

function badgeEstadoOrdenIQ(estado) {
    if (estado === 'BORRADOR') return '<span class="badge badge-warning">BORRADOR</span>';
    if (estado === 'APROBADA') return '<span class="badge badge-success">APROBADA</span>';
    if (estado === 'RECIBIDA') return '<span class="badge badge-success">RECIBIDA</span>';
    return '<span class="badge badge-danger">CANCELADA</span>';
}

function renderizarHistoricoIQ(ordenes) {
    const tbody = document.getElementById('tablaHistoricoIQ');

    if (!ordenes || ordenes.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="empty-state">No hay órdenes registradas</td></tr>';
        return;
    }

    const ordenadas = [...ordenes].sort((a, b) => b.idOrdenCompra - a.idOrdenCompra);

    tbody.innerHTML = ordenadas.map((o) => {
        const botonPdf = o.pdfGenerado
            ? `<button class="btn btn-secondary btn-sm" data-action="ver-pdf-hist" data-id="${o.idOrdenCompra}">Ver PDF</button>`
            : `<button class="btn btn-secondary btn-sm" data-action="generar-pdf-hist" data-id="${o.idOrdenCompra}">Generar PDF</button>`;

        let botonesEstado = '';
        if (esAdmin() && o.estado === 'APROBADA') {
            botonesEstado += `<button class="btn btn-primary btn-sm" data-action="recibir-hist" data-id="${o.idOrdenCompra}">Recibir</button> `;
            botonesEstado += `<button class="btn btn-danger btn-sm" data-action="cancelar-hist" data-id="${o.idOrdenCompra}">Cancelar</button>`;
        }
        // Sin esto, las filas sin botones quedan mas bajas que las que
        // si tienen (el padding del boton agrega altura), y las lineas
        // divisorias entre filas se ven desalineadas. Un placeholder
        // invisible del mismo tamano reserva la misma altura.
        if (botonesEstado === '') {
            botonesEstado = '<button class="btn btn-sm" style="visibility:hidden;" disabled>—</button>';
        }

        return `
            <tr>
                <td>${o.idOrdenCompra}</td>
                <td>${o.productoId ? o.productoId.nombre : '—'}</td>
                <td>${badgeEstadoOrdenIQ(o.estado)}</td>
                <td>${o.cantidad}</td>
                <td>${formatearMonedaIQ(o.total)}</td>
                <td>${formatearFecha(o.fechaCreacion)}</td>
                <td>${botonPdf}</td>
                <td class="table-actions">${botonesEstado}</td>
            </tr>`;
    }).join('');

    tbody.querySelectorAll('button[data-action="generar-pdf-hist"]').forEach((boton) => {
        boton.addEventListener('click', async () => {
            await manejarGenerarPdfIQ(boton.dataset.id);
            cargarHistoricoIQ();
        });
    });
    tbody.querySelectorAll('button[data-action="ver-pdf-hist"]').forEach((boton) => {
        boton.addEventListener('click', () => manejarVerPdfIQ(boton.dataset.id));
    });
    tbody.querySelectorAll('button[data-action="recibir-hist"]').forEach((boton) => {
        boton.addEventListener('click', () => manejarCambiarEstadoIQ(boton.dataset.id, 'RECIBIDA', '¿Marcar esta orden como recibida? Esto registrará automáticamente un movimiento de ENTRADA en el inventario.'));
    });
    tbody.querySelectorAll('button[data-action="cancelar-hist"]').forEach((boton) => {
        boton.addEventListener('click', () => manejarCambiarEstadoIQ(boton.dataset.id, 'CANCELADA', '¿Cancelar esta orden de compra? Esta acción no se puede deshacer.'));
    });
}

/**
 * Maneja cualquier transición de estado válida (APROBADA, RECIBIDA,
 * CANCELADA) desde la tabla de histórico. Reutiliza el mismo endpoint
 * PATCH /ordenes/{id}/estado que ya usaba "Aprobar" en la tabla de
 * BORRADOR, generalizado para las demás transiciones de la máquina
 * de estados (R17-R19).
 */
async function manejarCambiarEstadoIQ(id, nuevoEstado, mensajeConfirmacion) {
    if (!confirm(mensajeConfirmacion)) {
        return;
    }
    try {
        await apiPatch(`/ordenes/${id}/estado`, { estado: nuevoEstado });
        mostrarExitoIQ(`Orden actualizada a estado ${nuevoEstado} correctamente.`);
        await Promise.all([cargarHistoricoIQ(), cargarOrdenesBorradorIQ(), cargarKpisIQ()]);
    } catch (error) {
        mostrarErrorIQ(error.message);
    }
}

async function cargarHistoricoIQ() {
    try {
        const ordenes = await apiGet('/ordenes');
        // El historico muestra unicamente ordenes que ya salieron de
        // BORRADOR (APROBADA, RECIBIDA, CANCELADA) — las que siguen en
        // BORRADOR ya se muestran en la tabla "Ordenes en BORRADOR" de
        // arriba, para evitar duplicar la misma orden en dos tablas.
        const sinBorrador = (ordenes || []).filter((o) => o.estado !== 'BORRADOR');
        renderizarHistoricoIQ(sinBorrador);
    } catch (error) {
        mostrarErrorIQ(error.message);
    }
}

function inicializarTorreControlIQ() {
    const btnCerrar = document.getElementById('btnCerrarModalPdfIQ');
    if (btnCerrar) {
        btnCerrar.addEventListener('click', cerrarModalPdfIQ);
    }
    const btnRefrescar = document.getElementById('btnRefrescarIQ');
    if (btnRefrescar) {
        btnRefrescar.addEventListener('click', cargarTorreControlIQ);
    }
    const btnRefrescarHistorico = document.getElementById('btnRefrescarHistoricoIQ');
    if (btnRefrescarHistorico) {
        btnRefrescarHistorico.addEventListener('click', cargarHistoricoIQ);
    }
    cargarTorreControlIQ();
    cargarHistoricoIQ();
}

document.addEventListener('DOMContentLoaded', inicializarTorreControlIQ);