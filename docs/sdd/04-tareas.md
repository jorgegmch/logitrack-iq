# 04. Tareas — LogiTrack IQ

Tareas pequeñas del proceso de implementación. Marcadas `[x]` cuando están
completas y verificadas (compilación y/o ejecución confirmada).

## Modelo de datos

- [x] Diseñar entidades nuevas (`Proveedor`, `OrdenCompra`, `ResumenPanel`)
      en `03-diseno.md`
- [x] Crear `schema.sql` unificado (tablas nuevas + rol AGENTE)
- [x] Crear `data.sql` unificado (seed + movimientos de inventario inicial
      trazables desde `detalle_movimiento`)
- [x] Ejecutar y verificar `schema.sql`/`data.sql` en Supabase
      (`db_logitrack_iq`)
- [x] Implementar entidad `Proveedor.java`
- [x] Implementar entidad `OrdenCompra.java` (sin `@Lob`, compatible con
      pooler de transacciones)
- [x] Implementar entidad `ResumenPanel.java`
- [x] Extender `Producto.java` con `proveedorPrincipalId`
- [x] Crear enum `EstadoOrden.java`
- [x] Extender enum `Rol.java` con `AGENTE`
- [x] Confirmar arranque limpio de la aplicación (`ddl-auto=validate` sin
      errores)

## Repositorios

- [x] `ProveedorRepository`
- [x] `OrdenCompraRepository` (filtro por estado)
- [x] `ResumenPanelRepository` (por fecha, último válido)
- [x] Extender `DetalleMovimientoRepository` con cálculos de stock desde
      movimientos (R33)
- [x] Extender `MovimientoRepository` con conteo por tipo y rango de
      fechas

## Servicios

- [x] `ProveedorService`
- [x] `OrdenCompraService` (máquina de estados R17-R19, recepción
      transaccional R18, invalidación de PDF R20, generación/obtención
      de PDF vía `PdfService`)
- [x] `ResumenPanelService` (validación completa del contrato R21-R26,
      reemplazo por fecha R11)
- [x] `StockCalculadoService` (reutilizable, R1/R33)
- [x] `KpiService` (4 indicadores, productos en riesgo, bodegas críticas)
- [x] `PdfService` (R29: datos completos, R30: marca de agua diagonal
      condicional — OpenPDF 1.3.32, paquete `com.lowagie.text`)

## DTOs

- [x] `OrdenCompraRequest`
- [x] `CambiarEstadoRequest`
- [x] `ProductoStockResponse`
- [x] `AlertaDTO`, `AccionSugeridaDTO`, `ResumenPanelRequest`
- [x] `DesgloseStockBodegaDTO`, `ProductoRiesgoDTO`, `OcupacionBodegaDTO`,
      `OrdenesPorAprobarDTO`, `MovimientosAyerDTO`, `KpiResponse`

## Pruebas (TDD)

- [x] Tests unitarios de `OrdenCompraService` (T3, T4, T5) —
      recreado tras confirmar que el archivo original nunca se
      copió al proyecto (verde directo, 5 tests: T3 x2, T4, T5, R20)
- [x] Tests unitarios de `KpiService` (T1, T2 + caso positivo) —
      recreado, mismo motivo (verde directo, 3 tests)
- [x] Test de integración `POST /ordenes` (rojo confirmado → verde
      confirmado)
- [x] Test T6: `AGENTE` intenta aprobar orden → 403 (rojo confirmado →
      verde confirmado, con `AccessDeniedHandlerImpl` respondiendo 403
      real por rol)
- [x] Prueba de integración adicional: `PATCH /ordenes/{id}/estado`
      (ADMIN aprueba correctamente, rojo → verde confirmado)
- [x] Test de integración `ProveedorController` (listar, buscarPorId
      encontrado/no encontrado — rojo → verde confirmado)
- [x] Test de integración `KpiController` (resumen — rojo → verde
      confirmado)
- [x] Test de integración `GET /productos/{id}/stock` (encontrado/no
      encontrado — rojo → verde confirmado)
- [x] Test de integración `OrdenCompraController`: `listar`,
      `buscarPorId` (rojo → verde confirmado)
- [x] Test de integración `ResumenPanelController` (GET, POST válido —
      rojo → verde confirmado)
- [x] Test T7: resumen con severidad/ID inválido → 400 (2 tests: severidad
      inválida y producto inexistente — rojo → verde confirmado)
- [x] Test unitario `PdfService`: marca de agua condicional según
      estado (BORRADOR sí / APROBADA no) + datos completos de la orden
      (R29, R30 — rojo → verde confirmado, verificado extrayendo texto
      real del PDF con `PdfTextExtractor`)
- [x] Test de integración `OrdenCompraController`: `generarPdf`,
      `obtenerPdf` (incluye caso AGENTE sin permiso → 403 — rojo → verde
      confirmado)
- [x] Test T8 completo: invalidación específica del PDF al cambiar
      estado (R20) — test dedicado agregado a `OrdenCompraServiceTest`
- [x] Test de integración `GET /productos/riesgo` (ruta exacta exigida
      por el PDF, distinta de `/kpis/riesgo` — rojo → verde confirmado,
      rojo genuino: colisión de rutas con `/productos/{id}`)
- [x] Test de integración `GET /bodegas/criticas` (ruta exacta exigida
      por el PDF, distinta de `/kpis/bodegas-criticas` — rojo → verde
      confirmado, mismo tipo de colisión de rutas)
- [x] **Refactor:** eliminadas las rutas duplicadas `/kpis/riesgo` y
      `/kpis/bodegas-criticas` de `KpiController` (quedan exclusivamente
      en `/productos/riesgo` y `/bodegas/criticas`). `KpiService` no
      cambió — solo dejó de ser consumido desde `KpiController` para
      esas dos operaciones. `KpiController` ahora solo expone
      `GET /kpis` (resumen agregado del dashboard)

## Controladores y seguridad

- [x] `OrdenCompraController`: `crear` (`POST /ordenes`)
- [x] `OrdenCompraController`: `cambiarEstado` (`PATCH /ordenes/{id}/estado`)
- [x] `OrdenCompraController`: `listar`, `buscarPorId`
- [x] `OrdenCompraController`: `generarPdf`, `obtenerPdf`
- [x] `ProveedorController` (listar, buscarPorId)
- [x] `ResumenPanelController` (GET, POST)
- [x] `KpiController` (resumen)
- [x] Endpoint `GET /productos/{id}/stock`
- [x] Endpoint `GET /productos/riesgo` (ruta exacta del PDF)
- [x] Endpoint `GET /bodegas/criticas` (ruta exacta del PDF)
- [x] Actualizar `SecurityConfig` con las reglas de la sección 7 de
      `03-diseno.md`
- [x] Anotar endpoints nuevos con Swagger/OpenAPI
- [x] Verificar en vivo Swagger UI (`/swagger-ui/index.html`) — **hecho**,
      capturas confirmando que las 30+ rutas se renderizan correctamente
      y sin errores, incluidas `/productos/riesgo` y `/bodegas/criticas`
      ya corregidas
- [ ] Evidencia de endpoints **protegidos** en Swagger (login real + JWT
      + intento sin token + intento con rol incorrecto) — pendiente,
      ver explicación abajo

## Documento PDF de la orden

- [x] Confirmar librería (OpenPDF 1.3.32, paquete clásico `com.lowagie.text`)
- [x] Generación con datos completos (R29)
- [x] Marca de agua diagonal BORRADOR (R30)
- [x] Endpoints `POST`/`GET /ordenes/{id}/pdf`

## Servidor MCP

- [ ] Inicializar proyecto en `mcp-server/`
- [ ] Implementar las 6 herramientas exactas
- [ ] Usuario `agente_mcp` conectado y probado
- [ ] Evidencia de entrada/salida por herramienta

## Skill y flujo n8n

- [ ] `skills/operacion-logitrack/SKILL.md`
- [ ] Flujo `Resumen diario de inventario` (JSON)
- [ ] Export del flujo + capturas de ejecución exitosa y error controlado

## Dashboard

- [ ] `frontend/` conectado a la API real
- [ ] KPIs, riesgo, órdenes BORRADOR, PDF, botón Aprobar solo ADMIN

## Docker

- [ ] `Dockerfile` del backend
- [ ] `docker-compose.yml` (backend + n8n)
- [ ] Build y push a Docker Hub

## Cierre SDD y entrega

- [ ] `docs/sdd/evidencia-sdd.md` — en construcción
- [ ] README definitivo
- [ ] Diagrama n8n → MCP → API → BD → dashboard
- [ ] Video 4-6 min

## Estado del backend + pruebas (BLOQUE CERRADO)

La capa de backend + pruebas queda completa y verificada, incluyendo
las correcciones de ruta exigidas por el PDF de requerimientos
(`/productos/riesgo`, `/bodegas/criticas`) y la confirmación visual de
Swagger UI. Solo queda pendiente la evidencia de "endpoints protegidos"
en vivo (login + JWT + intento sin permisos) antes de dar por cerrado
el deliverable #4 del PDF por completo.

Siguiente bloque del proyecto: servidor MCP → dashboard → n8n → Docker.