/*
reportes.js - Reporte de resumen general + reportes avanzados con filtros
(movimientos y auditoria).
*/

function mostrarErrorReportes(mensaje) {
    const alerta = document.getElementById('reportesAlert');
    alerta.textContent = mensaje;
    alerta.classList.add('show');
    setTimeout(() => alerta.classList.remove('show'), 5000);
}

function renderizarTablaStockBodega(datos) {
    const tbody = document.getElementById('tablaStockBodega');
    tbody.innerHTML = datos.length === 0
        ? '<tr><td colspan="2" class="empty-state">Sin datos</td></tr>'
        : datos.map((i) => `<tr><td>${i.nombreBodega}</td><td>${i.stockTotal}</td></tr>`).join('');
}

function renderizarTablaProductosMovidos(datos) {
    const tbody = document.getElementById('tablaProductosMovidos');
    tbody.innerHTML = datos.length === 0
        ? '<tr><td colspan="2" class="empty-state">Sin datos</td></tr>'
        : datos.map((i) => `<tr><td>${i.nombreProducto}</td><td>${i.cantidadTotalMovida}</td></tr>`).join('');
}

async function cargarReportes() {
    try {
        const resumen = await apiGet('/reportes/resumen');
        renderizarTablaStockBodega(resumen.stockTotalPorBodega);
        renderizarTablaProductosMovidos(resumen.productosMasMovidos);
    } catch (error) {
        mostrarErrorReportes(error.message);
    }
}

/* ---------- Catalogos para los selects ---------- */

async function cargarCatalogosFiltros() {
    try {
        const [bodegas, productos] = await Promise.all([apiGet('/bodegas'), apiGet('/productos')]);

        const selectsBodega = [document.getElementById('filtroMovBodega')];
        const selectsProducto = [document.getElementById('filtroMovProducto'), document.getElementById('filtroAudProducto')];

        selectsBodega.forEach((select) => {
            bodegas.forEach((b) => {
                const opt = document.createElement('option');
                opt.value = b.idBodega;
                opt.textContent = b.nombre;
                select.appendChild(opt);
            });
        });

        selectsProducto.forEach((select) => {
            productos.forEach((p) => {
                const opt = document.createElement('option');
                opt.value = p.idProducto;
                opt.textContent = p.nombre;
                select.appendChild(opt);
            });
        });
    } catch (error) {
        mostrarErrorReportes('No se pudieron cargar los catálogos: ' + error.message);
    }
}

/* ---------- Movimientos filtrados ---------- */

function badgeTipoMov(tipo) {
    if (tipo === 'ENTRADA') return '<span class="badge badge-success">ENTRADA</span>';
    if (tipo === 'SALIDA') return '<span class="badge badge-danger">SALIDA</span>';
    return '<span class="badge badge-warning">TRANSFERENCIA</span>';
}

function renderizarMovFiltrados(movimientos) {
    const tbody = document.getElementById('tablaMovFiltrados');
    if (movimientos.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="empty-state">Sin resultados</td></tr>';
        return;
    }
    tbody.innerHTML = movimientos.map((m) => {
        const origen = m.bodegaOrigenId ? m.bodegaOrigenId.nombre : '—';
        const destino = m.bodegaDestinoId ? m.bodegaDestinoId.nombre : '—';
        const detalle = (m.detalles || []).map((d) => `${d.productoId.nombre} (${d.cantidad})`).join(', ') || '—';
        const fecha = new Date(m.fecha).toLocaleString('es-CO', { dateStyle: 'short', timeStyle: 'short' });
        return `<tr><td>${fecha}</td><td>${badgeTipoMov(m.tipo)}</td><td>${origen}</td><td>${destino}</td><td>${detalle}</td></tr>`;
    }).join('');
}

async function filtrarMovimientos() {
    const params = new URLSearchParams();
    const bodega = document.getElementById('filtroMovBodega').value;
    const producto = document.getElementById('filtroMovProducto').value;
    const tipo = document.getElementById('filtroMovTipo').value;
    const desde = document.getElementById('filtroMovDesde').value;
    const hasta = document.getElementById('filtroMovHasta').value;

    if (bodega) params.append('bodega', bodega);
    if (producto) params.append('producto', producto);
    if (tipo) params.append('tipoMovimiento', tipo);
    if (desde) params.append('fechaInicio', desde + ':00');
    if (hasta) params.append('fechaFin', hasta + ':00');

    try {
        const resultado = await apiGet('/api/reportes/movimientos?' + params.toString());
        renderizarMovFiltrados(resultado);
    } catch (error) {
        mostrarErrorReportes(error.message);
    }
}

function limpiarFiltrosMovimientos() {
    document.getElementById('filtroMovBodega').value = '';
    document.getElementById('filtroMovProducto').value = '';
    document.getElementById('filtroMovTipo').value = '';
    document.getElementById('filtroMovDesde').value = '';
    document.getElementById('filtroMovHasta').value = '';
    filtrarMovimientos();
}

/* ---------- Auditoria filtrada ---------- */

function badgeOperacion(tipo) {
    if (tipo === 'INSERT') return '<span class="badge badge-success">INSERT</span>';
    if (tipo === 'DELETE') return '<span class="badge badge-danger">DELETE</span>';
    return '<span class="badge badge-warning">UPDATE</span>';
}

function renderizarAudFiltrada(auditorias) {
    const tbody = document.getElementById('tablaAudFiltrada');
    if (auditorias.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" class="empty-state">Sin resultados</td></tr>';
        return;
    }
    tbody.innerHTML = auditorias.map((a) => {
        const fecha = new Date(a.fechaHora).toLocaleString('es-CO', { dateStyle: 'short', timeStyle: 'short' });
        return `<tr><td>${fecha}</td><td>${badgeOperacion(a.tipoOperacion)}</td><td>${a.usuarioId.username}</td><td>${a.entidadAfectada}</td></tr>`;
    }).join('');
}

async function filtrarAuditoria() {
    const params = new URLSearchParams();
    const producto = document.getElementById('filtroAudProducto').value;
    const desde = document.getElementById('filtroAudDesde').value;
    const hasta = document.getElementById('filtroAudHasta').value;
    const campo = document.getElementById('filtroAudCampo').value.trim();

    if (producto) params.append('producto', producto);
    if (desde) params.append('fechaInicio', desde + ':00');
    if (hasta) params.append('fechaFin', hasta + ':00');
    if (campo) params.append('campoModificado', campo);

    try {
        const resultado = await apiGet('/api/reportes/auditoria?' + params.toString());
        renderizarAudFiltrada(resultado);
    } catch (error) {
        mostrarErrorReportes(error.message);
    }
}

function limpiarFiltrosAuditoria() {
    document.getElementById('filtroAudProducto').value = '';
    document.getElementById('filtroAudDesde').value = '';
    document.getElementById('filtroAudHasta').value = '';
    document.getElementById('filtroAudCampo').value = '';
    filtrarAuditoria();
}

/* ---------- Inicializacion ---------- */

function inicializarReportes() {
    requerirAutenticacion();
    inicializarLayoutComun('reportes');
    cargarReportes();
    cargarCatalogosFiltros();
    filtrarMovimientos();
    filtrarAuditoria();

    document.getElementById('btnFiltrarMovimientos').addEventListener('click', filtrarMovimientos);
    document.getElementById('btnLimpiarMovimientos').addEventListener('click', limpiarFiltrosMovimientos);
    document.getElementById('btnFiltrarAuditoria').addEventListener('click', filtrarAuditoria);
    document.getElementById('btnLimpiarAuditoria').addEventListener('click', limpiarFiltrosAuditoria);
}

document.addEventListener('DOMContentLoaded', inicializarReportes);