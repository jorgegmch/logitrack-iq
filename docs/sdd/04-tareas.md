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
      **recreado** tras confirmar que el archivo original nunca se
      copió al proyecto (verde directo, 5 tests: T3 x2, T4, T5, R20)
- [x] Tests unitarios de `KpiService` (T1, T2 + caso positivo) —
      **recreado**, mismo motivo (verde directo, 3 tests)
- [x] Test de integración `POST /ordenes` (rojo confirmado → verde
      confirmado)
- [x] Test T6: `AGENTE` intenta aprobar orden → 403 (rojo confirmado →
      verde confirmado, con `AccessDeniedHandlerImpl` respondiendo 403
      real por rol)
- [x] Prueba de integración adicional: `PATCH /ordenes/{id}/estado`
      (ADMIN aprueba correctamente, rojo → verde confirmado)
- [x] Test de integración `ProveedorController` (listar, buscarPorId
      encontrado/no encontrado — rojo → verde confirmado)
- [x] Test de integración `KpiController` (resumen, riesgo, bodegas
      críticas — rojo → verde confirmado)
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
      confirmando que `pdfGenerado`/`fechaGeneracionPdf` quedan `null`
      tras cualquier transición

## Controladores y seguridad

- [x] `OrdenCompraController`: `crear` (`POST /ordenes`)
- [x] `OrdenCompraController`: `cambiarEstado` (`PATCH /ordenes/{id}/estado`)
- [x] `OrdenCompraController`: `listar`, `buscarPorId`
- [x] `OrdenCompraController`: `generarPdf`, `obtenerPdf`
- [x] `ProveedorController` (listar, buscarPorId)
- [x] `ResumenPanelController` (GET, POST)
- [x] `KpiController` (resumen + endpoints de riesgo y bodegas críticas)
- [x] Endpoint `GET /productos/{id}/stock`
- [x] Actualizar `SecurityConfig` con las reglas de la sección 7 de
      `03-diseno.md` (POST /ordenes, PATCH /ordenes/*/estado, POST
      /ordenes/*/pdf, POST /panel/resumen, corrección de POST
      /movimientos excluyendo AGENTE)
- [x] Anotar endpoints nuevos con Swagger/OpenAPI (`@Operation`,
      `@ApiResponse`, `@Tag` ya presentes en todos los controladores
      nuevos y en los métodos agregados a controladores existentes)
- [ ] **Pendiente real:** verificar en vivo en `/swagger-ui/index.html`
      que la documentación se renderiza correctamente (las anotaciones
      ya están en el código, pero nunca se confirmó visualmente que
      Swagger UI las levante sin errores)

## Documento PDF de la orden

- [x] Confirmar librería (OpenPDF 1.3.32, paquete clásico
      `com.lowagie.text` — se evitó deliberadamente la 2.x/3.x, que
      renombra todo a `org.openpdf`)
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

- [ ] `docs/sdd/evidencia-sdd.md` (hashes, tabla regla→prueba, evidencia
      roja/verde, reflexión) — en construcción, ver documento actual
- [ ] README definitivo
- [ ] Diagrama n8n → MCP → API → BD → dashboard
- [ ] Video 4-6 min

## Estado del backend + pruebas (BLOQUE CERRADO)

Con la recreación de `OrdenCompraServiceTest.java`, `KpiServiceTest.java`,
y el test dedicado de R20, **la capa de backend + pruebas queda
completa y verificada** — todos los tests documentados en este
checklist existen realmente en el proyecto y compilan/pasan. Solo
queda pendiente la verificación visual de Swagger UI (tarea trivial,
no bloqueante) antes de considerar este bloque totalmente cerrado.

Siguiente bloque del proyecto: servidor MCP → dashboard → n8n → Docker.