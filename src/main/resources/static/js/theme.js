/*
theme.js — Manejo del modo oscuro/claro para LogiTrack.
Se carga en todas las paginas. Requiere un elemento con id="themeToggle"
en el HTML para funcionar el boton de alternancia.
*/

const TEMA_STORAGE_KEY = 'logitrack_theme';

function obtenerTemaGuardado() {
    return localStorage.getItem(TEMA_STORAGE_KEY) || 'dark';
}

function aplicarTema(tema) {
    if (tema === 'light') {
        document.documentElement.setAttribute('data-theme', 'light');
    } else {
        document.documentElement.removeAttribute('data-theme');
    }
    actualizarIconoToggle(tema);
}

function actualizarIconoToggle(tema) {
    const boton = document.getElementById('themeToggle');
    if (!boton) {
        return;
    }
    boton.textContent = tema === 'light' ? '☀️' : '🌙';
    boton.setAttribute('aria-label', tema === 'light' ? 'Cambiar a modo oscuro' : 'Cambiar a modo claro');
}

function alternarTema() {
    const temaActual = obtenerTemaGuardado();
    const temaNuevo = temaActual === 'light' ? 'dark' : 'light';
    localStorage.setItem(TEMA_STORAGE_KEY, temaNuevo);
    aplicarTema(temaNuevo);
}

function inicializarTema() {
    aplicarTema(obtenerTemaGuardado());
    const boton = document.getElementById('themeToggle');
    if (boton) {
        boton.addEventListener('click', alternarTema);
    }
}

document.addEventListener('DOMContentLoaded', inicializarTema);