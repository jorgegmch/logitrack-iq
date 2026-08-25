# 01. Propuesta — LogiTrack IQ

## Problema

LogiTrack S.A. cuenta con un backend funcional (LogiTrack API) para bodegas,
productos y movimientos de inventario, pero la información se revisa
manualmente. No existe una vista diaria que detecte productos en riesgo de
faltante ni que prepare una propuesta de compra, lo que genera reacción
tardía ante quiebres de stock.

## Objetivo

Extender LogiTrack API para construir una torre de control de inventario que:
- Calcule el stock real a partir de los movimientos registrados.
- Detecte productos por debajo de su punto de reorden.
- Permita que un flujo automatizado (n8n + MCP) proponga una orden de
  compra en estado BORRADOR.
- Permita a un administrador aprobar y recibir esa orden, actualizando el
  inventario automáticamente.
- Muestre el resultado en un dashboard con indicadores, alertas y acciones.

## Alcance

- Nuevas entidades: `Proveedor`, `OrdenCompra`, `ResumenPanel`, y relación
  `Producto.proveedorPrincipal`.
- Cálculo de stock desde movimientos (ENTRADA, SALIDA, TRANSFERENCIA).
- Endpoints de KPIs, riesgo, bodegas críticas, órdenes y resumen del panel.
- Máquina de estados de la orden (BORRADOR → APROBADA → RECIBIDA/CANCELADA)
  con recepción transaccional.
- Generación de PDF de la orden con marca de agua BORRADOR.
- Servidor MCP con exactamente 6 herramientas sobre la API REST.
- Skill operativa (`SKILL.md`) y flujo n8n único con AI Agent.
- Dashboard web (HTML/CSS/JS sin framework) conectado a la API real.
- Rol `AGENTE` en seguridad, con permisos restringidos frente a `ADMIN`.
- Proceso SDD documentado y evidencia TDD (rojo → verde) con trazabilidad
  regla → prueba.

## Fuera de alcance

- No se crea un backend independiente ni se reemplazan las funciones ya
  construidas en LogiTrack API (autenticación, CRUD de bodegas/productos/
  movimientos del proyecto anterior).
- No se implementa una herramienta MCP para aprobar, cancelar o recibir
  órdenes (restricción obligatoria de diseño).
- No se exige diseño visual avanzado, animaciones ni interfaz móvil en el
  dashboard.
- No se exige una librería externa de validación de JSON Schema para el
  contrato del resumen del panel.
- No se calculan ni modifican datos directamente desde el dashboard, MCP o
  n8n; toda escritura pasa por la API.

## Nota de corrección de requerimientos

El documento de requerimientos original menciona MySQL como motor de base
de datos. Esto fue un error de transcripción aclarado por el profesor: el
proyecto base (LogiTrack API) usa **PostgreSQL vía Supabase**, y este es el
motor real que se usará también en LogiTrack IQ.