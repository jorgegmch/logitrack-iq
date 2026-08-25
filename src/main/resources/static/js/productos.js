/*
productos.js - CRUD completo de productos (listar, crear, editar, eliminar).
Eliminar es exclusivo de rol ADMIN.
*/

let productoEnEdicion = null;

function mostrarErrorProductos(mensaje) {
    const alerta = document.getElementById('productosAlert');
    alerta.textContent = mensaje;
    alerta.classList.add('show');
    setTimeout(() => alerta.classList.remove('show'), 5000);
}

function mostrarExitoProductos(mensaje) {
    const alerta = document.getElementById('productosAlertSuccess');
    alerta.textContent = mensaje;
    alerta.classList.add('show');
    setTimeout(() => alerta.classList.remove('show'), 4000);
}

function formatearPrecio(precio) {
    return new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(precio);
}

function renderizarTablaProductos(productos) {
    const tbody = document.getElementById('tablaProductos');
    tbody.innerHTML = '';

    if (productos.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="empty-state">No hay productos registrados</td></tr>';
        return;
    }

    productos.forEach((producto) => {
        const fila = document.createElement('tr');
        fila.innerHTML = `
            <td>${producto.idProducto}</td>
            <td>${producto.nombre}</td>
            <td>${producto.categoria || '—'}</td>
            <td>${formatearPrecio(producto.precio)}</td>
            <td class="table-actions">
                <button class="btn btn-secondary btn-sm" data-action="editar" data-id="${producto.idProducto}">Editar</button>
                <button class="btn btn-danger btn-sm solo-admin" data-action="eliminar" data-id="${producto.idProducto}">Eliminar</button>
            </td>
        `;
        tbody.appendChild(fila);
    });

    if (!esAdmin()) {
        tbody.querySelectorAll('.solo-admin').forEach((el) => el.classList.add('hidden-role'));
    }

    tbody.querySelectorAll('button[data-action="editar"]').forEach((boton) => {
        boton.addEventListener('click', () => abrirModalEdicion(boton.dataset.id, productosCache));
    });
    tbody.querySelectorAll('button[data-action="eliminar"]').forEach((boton) => {
        boton.addEventListener('click', () => manejarEliminarProducto(boton.dataset.id));
    });
}

let productosCache = [];

async function cargarProductos() {
    try {
        productosCache = await apiGet('/productos');
        renderizarTablaProductos(productosCache);
    } catch (error) {
        mostrarErrorProductos(error.message);
    }
}

async function manejarEliminarProducto(id) {
    if (!confirm('¿Está seguro de eliminar este producto? Esta acción no se puede deshacer.')) {
        return;
    }

    try {
        await apiDelete(`/productos/${id}`);
        mostrarExitoProductos('Producto eliminado correctamente.');
        cargarProductos();
    } catch (error) {
        mostrarErrorProductos(error.message);
    }
}

/* ---------- Modal crear/editar ---------- */

function abrirModalCreacion() {
    productoEnEdicion = null;
    document.getElementById('modalProductoTitulo').textContent = 'Nuevo producto';
    document.getElementById('formProducto').reset();
    document.getElementById('productoId').value = '';
    ocultarErrorModalProducto();
    document.getElementById('modalProducto').classList.add('show');
}

function abrirModalEdicion(id, productos) {
    const producto = productos.find((p) => String(p.idProducto) === String(id));
    if (!producto) {
        return;
    }

    productoEnEdicion = producto;
    document.getElementById('modalProductoTitulo').textContent = 'Editar producto';
    document.getElementById('productoId').value = producto.idProducto;
    document.getElementById('productoNombre').value = producto.nombre;
    document.getElementById('productoCategoria').value = producto.categoria || '';
    document.getElementById('productoPrecio').value = producto.precio;
    ocultarErrorModalProducto();
    document.getElementById('modalProducto').classList.add('show');
}

function cerrarModalProducto() {
    document.getElementById('modalProducto').classList.remove('show');
}

function mostrarErrorModalProducto(mensaje) {
    const alerta = document.getElementById('modalProductoAlert');
    alerta.textContent = mensaje;
    alerta.classList.add('show');
}

function ocultarErrorModalProducto() {
    const alerta = document.getElementById('modalProductoAlert');
    alerta.classList.remove('show');
    alerta.textContent = '';
}

async function manejarSubmitProducto(evento) {
    evento.preventDefault();
    ocultarErrorModalProducto();

    const nombre = document.getElementById('productoNombre').value.trim();
    const categoria = document.getElementById('productoCategoria').value.trim();
    const precio = parseFloat(document.getElementById('productoPrecio').value);
    const boton = document.getElementById('btnGuardarProducto');

    const datos = { nombre, categoria: categoria || null, precio };

    boton.disabled = true;
    boton.textContent = 'Guardando...';

    try {
        if (productoEnEdicion) {
            await apiPut(`/productos/${productoEnEdicion.idProducto}`, datos);
            mostrarExitoProductos('Producto actualizado correctamente.');
        } else {
            await apiPost('/productos', datos);
            mostrarExitoProductos('Producto creado correctamente.');
        }
        cerrarModalProducto();
        cargarProductos();
    } catch (error) {
        mostrarErrorModalProducto(error.message);
    } finally {
        boton.disabled = false;
        boton.textContent = 'Guardar';
    }
}

/* ---------- Inicializacion ---------- */

function inicializarProductos() {
    requerirAutenticacion();
    inicializarLayoutComun('productos');
    cargarProductos();

    document.getElementById('btnNuevoProducto').addEventListener('click', abrirModalCreacion);
    document.getElementById('btnCerrarModalProducto').addEventListener('click', cerrarModalProducto);
    document.getElementById('btnCancelarModalProducto').addEventListener('click', cerrarModalProducto);
    document.getElementById('formProducto').addEventListener('submit', manejarSubmitProducto);
}

document.addEventListener('DOMContentLoaded', inicializarProductos);