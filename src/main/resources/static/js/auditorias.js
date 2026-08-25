/*
auditorias.js - Consulta de auditoria (solo lectura). Pagina exclusiva ADMIN.
El detalle de valoresAnteriores/valoresNuevos se muestra como diff
campo por campo, en vez de JSON crudo.
*/

const ETIQUETAS_CAMPOS = {
    idProducto: 'ID Producto', idBodega: 'ID Bodega', idUsuario: 'ID Usuario',
    idMovimiento: 'ID Movimiento', idDetMov: 'ID Detalle',
    nombre: 'Nombre', categoria: 'Categoría', precio: 'Precio',
    ubicacion: 'Ubicación', capacidad: 'Capacidad', encargadoId: 'Encargado',
    username: 'Usuario', password: 'Contraseña', rol: 'Rol', activo: 'Activo',
    fecha: 'Fecha', tipo: 'Tipo', usuarioId: 'Responsable',
    bodegaOrigenId: 'Bodega origen', bodegaDestinoId: 'Bodega destino',
    detalles: 'Detalle de productos', cantidad: 'Cantidad', stock: 'Stock',
};

function mostrarErrorAuditorias(mensaje) {
    const alerta = document.getElementById('auditoriasAlert');
    alerta.textContent = mensaje;
    alerta.classList.add('show');
    setTimeout(() => alerta.classList.remove('show'), 5000);
}

function badgeTipoOperacion(tipo) {
    if (tipo === 'INSERT') {
        return '<span class="badge badge-success">INSERT</span>';
    }
    if (tipo === 'DELETE') {
        return '<span class="badge badge-danger">DELETE</span>';
    }
    return '<span class="badge badge-warning">UPDATE</span>';
}

function formatearFecha(fechaIso) {
    const fecha = new Date(fechaIso);
    return fecha.toLocaleString('es-CO', { dateStyle: 'short', timeStyle: 'short' });
}

function etiquetaCampo(clave) {
    return ETIQUETAS_CAMPOS[clave] || clave;
}

function esArrayDeFecha(valor) {
    return Array.isArray(valor) && valor.length >= 6 && valor.every((n) => typeof n === 'number');
}

function formatearArrayFecha(valor) {
    const [anio, mes, dia, hora, minuto, segundo] = valor;
    const fecha = new Date(anio, mes - 1, dia, hora, minuto, segundo || 0);
    return fecha.toLocaleString('es-CO', { dateStyle: 'short', timeStyle: 'short' });
}

function formatearDetalleMovimiento(detalles) {
    return detalles
        .map((d) => {
            const nombreProducto = d.productoId ? d.productoId.nombre : 'Producto desconocido';
            return `${nombreProducto} × ${d.cantidad}`;
        })
        .join(', ');
}

function formatearValorCampo(valor, clave) {
    if (valor === null || valor === undefined) {
        return '—';
    }
    if (typeof valor === 'boolean') {
        return valor ? 'Sí' : 'No';
    }
    if (esArrayDeFecha(valor)) {
        return formatearArrayFecha(valor);
    }
    if (Array.isArray(valor)) {
        if (valor.length === 0) {
            return '—';
        }
        if (clave === 'detalles') {
            return formatearDetalleMovimiento(valor);
        }
        return valor.map((item) => formatearValorCampo(item)).join('; ');
    }
    if (typeof valor === 'object') {
        return valor.nombre || valor.username || valor.idProducto || valor.idBodega || valor.idUsuario || JSON.stringify(valor);
    }
    return String(valor);
}

function parsearJsonSeguro(texto) {
    if (!texto) {
        return null;
    }
    try {
        return JSON.parse(texto);
    } catch (error) {
        return null;
    }
}

function renderizarTablaDetalle(anteriorObj, nuevoObj) {
    const tbody = document.getElementById('tablaDetalleCampos');
    tbody.innerHTML = '';

    const claves = new Set([
        ...Object.keys(anteriorObj || {}),
        ...Object.keys(nuevoObj || {}),
    ]);

    if (claves.size === 0) {
        tbody.innerHTML = '<tr><td colspan="3" class="empty-state">Sin datos disponibles</td></tr>';
        return;
    }

    claves.forEach((clave) => {
        if (clave === 'password') {
            return;
        }

        const valorAnterior = anteriorObj ? anteriorObj[clave] : undefined;
        const valorNuevo = nuevoObj ? nuevoObj[clave] : undefined;
        const textoAnterior = formatearValorCampo(valorAnterior, clave);
        const textoNuevo = formatearValorCampo(valorNuevo, clave);
        const cambio = textoAnterior !== textoNuevo;

        const fila = document.createElement('tr');
        fila.innerHTML = `
            <td>${etiquetaCampo(clave)}</td>
            <td>${anteriorObj ? textoAnterior : '—'}</td>
            <td>${cambio ? `<strong style="color: var(--accent);">${textoNuevo}</strong>` : textoNuevo}</td>
        `;
        tbody.appendChild(fila);
    });
}

function renderizarTablaAuditorias(auditorias) {
    const tbody = document.getElementById('tablaAuditorias');
    tbody.innerHTML = '';

    if (auditorias.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="empty-state">No hay registros de auditoría</td></tr>';
        return;
    }

    const ordenadas = [...auditorias].sort((a, b) => new Date(b.fechaHora) - new Date(a.fechaHora));

    ordenadas.forEach((auditoria) => {
        const fila = document.createElement('tr');
        fila.innerHTML = `
            <td>${auditoria.idAuditoria}</td>
            <td>${formatearFecha(auditoria.fechaHora)}</td>
            <td>${badgeTipoOperacion(auditoria.tipoOperacion)}</td>
            <td>${auditoria.usuarioId.username}</td>
            <td>${auditoria.entidadAfectada}</td>
            <td><button class="btn btn-secondary btn-sm" data-id="${auditoria.idAuditoria}">Ver detalle</button></td>
        `;
        tbody.appendChild(fila);
    });

    tbody.querySelectorAll('button[data-id]').forEach((boton) => {
        boton.addEventListener('click', () => mostrarDetalle(boton.dataset.id, ordenadas));
    });
}

function mostrarDetalle(id, auditorias) {
    const auditoria = auditorias.find((a) => String(a.idAuditoria) === String(id));
    if (!auditoria) {
        return;
    }

    const anteriorObj = parsearJsonSeguro(auditoria.valoresAnteriores);
    const nuevoObj = parsearJsonSeguro(auditoria.valoresNuevos);

    renderizarTablaDetalle(anteriorObj, nuevoObj);
    document.getElementById('modalDetalleAuditoria').classList.add('show');
}

async function cargarAuditorias() {
    try {
        const auditorias = await apiGet('/auditorias');
        renderizarTablaAuditorias(auditorias);
    } catch (error) {
        mostrarErrorAuditorias(error.message);
    }
}

async function cargarUsuariosParaFiltro() {
    try {
        const usuarios = await apiGet('/usuarios');
        const select = document.getElementById('filtroUsuario');
        usuarios.forEach((usuario) => {
            const option = document.createElement('option');
            option.value = usuario.idUsuario;
            option.textContent = usuario.username;
            select.appendChild(option);
        });
    } catch (error) {
        mostrarErrorAuditorias('No se pudo cargar la lista de usuarios: ' + error.message);
    }
}

async function aplicarFiltrosAuditoria() {
    const usuarioId = document.getElementById('filtroUsuario').value;
    const tipoOperacion = document.getElementById('filtroTipoOperacion').value;

    try {
        let auditorias;
        if (usuarioId) {
            auditorias = await apiGet(`/auditorias/usuario/${usuarioId}`);
        } else if (tipoOperacion) {
            auditorias = await apiGet(`/auditorias/tipo/${tipoOperacion}`);
        } else {
            auditorias = await apiGet('/auditorias');
        }
        renderizarTablaAuditorias(auditorias);
    } catch (error) {
        mostrarErrorAuditorias(error.message);
    }
}

/* ---------- Inicializacion ---------- */

function inicializarAuditorias() {
    requerirAutenticacion();

    if (!esAdmin()) {
        window.location.href = '/html/dashboard.html';
        return;
    }

    inicializarLayoutComun('auditorias');
    cargarAuditorias();
    cargarUsuariosParaFiltro();

    document.getElementById('btnAplicarFiltroAuditoria').addEventListener('click', aplicarFiltrosAuditoria);
    document.getElementById('btnLimpiarFiltroAuditoria').addEventListener('click', () => {
        document.getElementById('filtroUsuario').value = '';
        document.getElementById('filtroTipoOperacion').value = '';
        cargarAuditorias();
    });
    document.getElementById('btnCerrarModalDetalle').addEventListener('click', () => {
        document.getElementById('modalDetalleAuditoria').classList.remove('show');
    });
}

document.addEventListener('DOMContentLoaded', inicializarAuditorias);