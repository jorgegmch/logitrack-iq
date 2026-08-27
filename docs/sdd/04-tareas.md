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
      transaccional R18, invalidación de PDF R20)
- [x] `ResumenPanelService` (validación completa del contrato R21-R26,
      reemplazo por fecha R11)
- [x] `StockCalculadoService` (reutilizable, R1/R33)
- [x] `KpiService` (4 indicadores, productos en riesgo, bodegas críticas)

## DTOs

- [x] `OrdenCompraRequest`
- [x] `CambiarEstadoRequest`
- [x] `AlertaDTO`, `AccionSugeridaDTO`, `ResumenPanelRequest`
- [x] `DesgloseStockBodegaDTO`, `ProductoRiesgoDTO`, `OcupacionBodegaDTO`,
      `OrdenesPorAprobarDTO`, `MovimientosAyerDTO`, `KpiResponse`

## Pruebas (TDD)

- [x] Tests unitarios de `OrdenCompraService` (T3, T4, T5)
- [x] Tests unitarios de `KpiService` (T1, T2 + caso positivo)
- [x] Test de integración `POST /ordenes` (rojo confirmado → verde
      confirmado)
- [x] Test T6: `AGENTE` intenta aprobar orden → 403 (rojo confirmado →
      verde confirmado, con `AccessDeniedHandlerImpl` respondiendo 403
      real por rol)
- [x] Prueba de integración adicional: `PATCH /ordenes/{id}/estado`
      (ADMIN aprueba correctamente, rojo → verde confirmado)
- [ ] Test T7: resumen con severidad/ID inválido → 400, se conserva el
      anterior
- [ ] Test T8: PDF de orden en BORRADOR con marca de agua, invalidación al
      cambiar estado

## Controladores y seguridad

- [x] `OrdenCompraController`: `crear` (`POST /ordenes`)
- [x] `OrdenCompraController`: `cambiarEstado` (`PATCH /ordenes/{id}/estado`)
- [ ] `OrdenCompraController`: `listar`, `buscarPorId`
- [ ] `OrdenCompraController`: generar/obtener PDF
- [ ] `ProveedorController`
- [ ] `ResumenPanelController`
- [ ] `KpiController` (+ endpoints de riesgo y bodegas críticas)
- [ ] Endpoint `GET /productos/{id}/stock`
- [x] Actualizar `SecurityConfig` con las reglas de la sección 7 de
      `03-diseno.md` (POST /ordenes, PATCH /ordenes/*/estado, POST
      /ordenes/*/pdf, POST /panel/resumen, correccion de POST
      /movimientos excluyendo AGENTE)
- [ ] Documentar endpoints nuevos en Swagger/OpenAPI

## Documento PDF de la orden

- [ ] Confirmar libreria (OpenPDF por defecto)
- [ ] Generacion con datos completos (R29)
- [ ] Marca de agua diagonal BORRADOR (R30)
- [ ] Endpoints `POST`/`GET /ordenes/{id}/pdf`

## Servidor MCP

- [ ] Inicializar proyecto en `mcp-server/`
- [ ] Implementar las 6 herramientas exactas
- [ ] Usuario `agente_mcp` conectado y probado
- [ ] Evidencia de entrada/salida por herramienta

## Skill y flujo n8n

- [ ] `skills/operacion-logitrack/SKILL.md`
- [ ] Flujo `Resumen diario de inventario` (JSON)
- [ ] Export del flujo + capturas de ejecucion exitosa y error controlado

## Dashboard

- [ ] `frontend/` conectado a la API real
- [ ] KPIs, riesgo, ordenes BORRADOR, PDF, boton Aprobar solo ADMIN

## Docker

- [ ] `Dockerfile` del backend
- [ ] `docker-compose.yml` (backend + n8n)
- [ ] Build y push a Docker Hub

## Cierre SDD y entrega

- [ ] `docs/sdd/evidencia-sdd.md` (hashes, tabla regla-prueba, evidencia
      roja/verde, reflexion) - en construccion, ver borrador actual
- [ ] README definitivo
- [ ] Diagrama n8n -> MCP -> API -> BD -> dashboard
- [ ] Video 4-6 min