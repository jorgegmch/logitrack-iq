/*
login.js - Logica de la pantalla de inicio de sesion.
*/

function inicializarFooterAnio() {
    const spanAnio = document.getElementById('footerYear');
    if (spanAnio) {
        spanAnio.textContent = new Date().getFullYear();
    }
}

function mostrarErrorLogin(mensaje) {
    const alerta = document.getElementById('loginAlert');
    alerta.textContent = mensaje;
    alerta.classList.add('show');
}

function ocultarErrorLogin() {
    const alerta = document.getElementById('loginAlert');
    alerta.classList.remove('show');
    alerta.textContent = '';
}

async function manejarSubmitLogin(evento) {
    evento.preventDefault();
    ocultarErrorLogin();

    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;
    const boton = document.getElementById('loginButton');

    if (!username || !password) {
        mostrarErrorLogin('Debe ingresar usuario y contraseña.');
        return;
    }

    boton.disabled = true;
    boton.textContent = 'Ingresando...';

    try {
        const respuesta = await apiPost('/auth/login', { username, password });
        guardarSesion(respuesta.idUsuario, respuesta.token, respuesta.username, respuesta.rol);
        window.location.href = '/html/dashboard.html';
    } catch (error) {
        mostrarErrorLogin(error.message);
        boton.disabled = false;
        boton.textContent = 'Iniciar sesión';
    }
}

function inicializarLogin() {
    inicializarFooterAnio();

    if (obtenerToken()) {
        window.location.href = '/html/dashboard.html';
        return;
    }

    const form = document.getElementById('loginForm');
    form.addEventListener('submit', manejarSubmitLogin);
}

document.addEventListener('DOMContentLoaded', inicializarLogin);