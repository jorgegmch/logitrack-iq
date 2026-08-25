/*
api.js — Modulo central de comunicacion con la API de LogiTrack.
Maneja el token JWT, las llamadas fetch, y el manejo de errores HTTP.
*/

const API_BASE_URL = '';

const TOKEN_KEY = 'logitrack_token';
const USERNAME_KEY = 'logitrack_username';
const ROL_KEY = 'logitrack_rol';
const ID_USUARIO_KEY = 'logitrack_id_usuario';

/* ---------- Manejo de sesion ---------- */

function guardarSesion(idUsuario, token, username, rol) {
    localStorage.setItem(ID_USUARIO_KEY, idUsuario);
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USERNAME_KEY, username);
    localStorage.setItem(ROL_KEY, rol);
}

function obtenerIdUsuario() {
    return localStorage.getItem(ID_USUARIO_KEY);
}

function obtenerToken() {
    return localStorage.getItem(TOKEN_KEY);
}

function obtenerUsername() {
    return localStorage.getItem(USERNAME_KEY);
}

function obtenerRol() {
    return localStorage.getItem(ROL_KEY);
}

function esAdmin() {
    return obtenerRol() === 'ADMIN';
}

function cerrarSesion() {
    localStorage.removeItem(ID_USUARIO_KEY);
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USERNAME_KEY);
    localStorage.removeItem(ROL_KEY);
    window.location.href = '/html/login.html';
}

function requerirAutenticacion() {
    if (!obtenerToken()) {
        window.location.href = '/html/login.html';
    }
}

/* ---------- Cliente HTTP central ---------- */

async function apiFetch(endpoint, options = {}) {
    const headers = {
        ...options.headers,
    };

    if (options.body) {
        headers['Content-Type'] = 'application/json';
    }

    const token = obtenerToken();
    if (token) {
        headers['Authorization'] = 'Bearer ' + token;
    }

    let respuesta;
    try {
        respuesta = await fetch(API_BASE_URL + endpoint, {
            ...options,
            headers,
        });
    } catch (errorRed) {
        throw new Error('No se pudo conectar con el servidor. Verifique que la aplicacion este corriendo.');
    }

    if (respuesta.status === 401) {
        cerrarSesion();
        throw new Error('Sesion expirada. Inicie sesion nuevamente.');
    }

    if (respuesta.status === 204) {
        return null;
    }

    let cuerpo = null;
    const contentType = respuesta.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
        cuerpo = await respuesta.json();
    }

    if (!respuesta.ok) {
        const mensaje = (cuerpo && cuerpo.mensaje) ? cuerpo.mensaje : 'Ocurrio un error inesperado.';
        throw new Error(mensaje);
    }

    return cuerpo;
}

/* ---------- Atajos por metodo HTTP ---------- */

function apiGet(endpoint) {
    return apiFetch(endpoint, { method: 'GET' });
}

function apiPost(endpoint, datos) {
    return apiFetch(endpoint, { method: 'POST', body: JSON.stringify(datos) });
}

function apiPut(endpoint, datos) {
    return apiFetch(endpoint, { method: 'PUT', body: JSON.stringify(datos) });
}

function apiPatch(endpoint, datos) {
    return apiFetch(endpoint, { method: 'PATCH', body: datos ? JSON.stringify(datos) : undefined });
}

function apiDelete(endpoint) {
    return apiFetch(endpoint, { method: 'DELETE' });
}

/* ---------- Layout compartido (sidebar, topbar, logout) ---------- */

function inicializarLayoutComun(paginaActual) {
    document.querySelectorAll('.nav-link').forEach((link) => {
        if (link.dataset.page === paginaActual) {
            link.classList.add('active');
        }
    });

    if (!esAdmin()) {
        document.querySelectorAll('.solo-admin').forEach((el) => {
            el.classList.add('hidden-role');
        });
    }

    const badgeUsuario = document.getElementById('userBadge');
    if (badgeUsuario) {
        badgeUsuario.textContent = obtenerUsername();
    }

    const badgeRol = document.getElementById('roleBadge');
    if (badgeRol) {
        badgeRol.textContent = obtenerRol();
    }

    const botonLogout = document.getElementById('logoutButton');
    if (botonLogout) {
        botonLogout.addEventListener('click', cerrarSesion);
    }
}