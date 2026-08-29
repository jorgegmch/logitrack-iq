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
      confirmado visualmente que las rutas se renderizan correctamente
      y sin errores, incluidas `/productos/riesgo` y `/bodegas/criticas`
      ya corregidas (evidencia visual: `03-authorize-admin.png` en
      `docs/capturas/swagger-endpoints-protegidos/`, que muestra varios
      grupos de rutas renderizados)
- [x] Evidencia de endpoints **protegidos** en Swagger (login real + JWT
      + intento sin token + intento con rol incorrecto) — ver
      `docs/capturas/swagger-endpoints-protegidos/`: sin token (403),
      login admin, Authorize, endpoint autorizado (200), login
      AGENTE, intento de AGENTE sobre endpoint exclusivo-ADMIN (403)

## Documento PDF de la orden

- [x] Confirmar librería (OpenPDF 1.3.32, paquete clásico `com.lowagie.text`)
- [x] Generación con datos completos (R29)
- [x] Marca de agua diagonal BORRADOR (R30)
- [x] Endpoints `POST`/`GET /ordenes/{id}/pdf`
- [x] **Rediseño visual** del PDF: barra de título con color, ficha de
      metadatos (fecha/bodega), tabla de detalle producto/proveedor con
      encabezados, bloque de total destacado, pie de página. Corrige
      dos errores de compilación propios de OpenPDF (`NO_BORDER` y
      `BOX` pertenecen a `Rectangle`, no a `Element`)
- [x] Verificado dentro del contenedor Docker: marca de agua BORRADOR
      se sigue renderizando correctamente sobre el nuevo layout

## Corrección post-cierre del backend (descubierta durante pruebas de MCP)

- [x] **Fix:** `ResumenPanelService.publicarResumen` violaba la
      restricción `UNIQUE` de `fecha` al reemplazar el resumen del día
      (`duplicate key value violates unique constraint
      "resumen_panel_fecha_key"`). Causa: Hibernate reordena el flush
      de una transacción y ejecutaba el `INSERT` antes que el
      `DELETE` del resumen anterior, aunque el código los llama en
      ese orden. Corregido con `resumenPanelRepository.flush()`
      explícito inmediatamente después del `delete()`. No afecta
      ninguna regla ni test documentado en `evidencia-sdd.md` — es un
      bug de comportamiento real de Hibernate no cubierto por los
      tests existentes (ninguno probaba "publicar dos veces el mismo
      día contra la base de datos real").

## Servidor MCP

- [x] Inicializar proyecto en `mcp-server/`
- [x] Implementar las 6 herramientas exactas
- [x] Usuario `agente_mcp` conectado y probado (login real confirmado
      end-to-end: Thunder Client → backend, y servidor MCP → backend)
- [x] Evidencia de entrada/salida por herramienta — ver
      `mcp-server/evidencia-mcp.md` y `docs/capturas/mcp-tools/`
- [x] **Fix:** `Already connected to a transport` — el servidor usaba
      una única instancia global de `McpServer`, que no soporta más
      de una conexión SSE. Corregido con una fábrica
      (`crearServidorMcp()`) que crea una instancia nueva por cada
      conexión entrante en `/sse`
- [x] **Fix:** Gemini rechazaba las 6 herramientas con `400
      Bad Request` (`Unknown name "exclusiveMinimum"`) — Zod traduce
      `.positive()` a esa palabra clave de JSON Schema, no soportada
      por la API de Gemini. Corregido reemplazando `.positive()` por
      `.min(1)` en todos los campos numéricos de las 6 tools
- [x] **Fix:** el esquema de `publicar_resumen` nunca definió el
      campo `tipo` en `accionesSugeridas`, aunque el backend lo exige
      (`AccionSugeridaDTO`). Corregido agregando
      `tipo: z.enum([...])` al esquema

## Skill y flujo n8n

- [x] `skills/operacion-logitrack/SKILL.md`
- [x] Flujo `Resumen diario de inventario` (JSON) — construido con
      Schedule Trigger (6am America/Bogota), AI Agent (Google Gemini
      Chat Model) + MCP Client Tool apuntando al servidor MCP, y
      ramas de éxito/error
- [x] Export del flujo + capturas de ejecución exitosa y error
      controlado — ver `docs/capturas/mcp-tools/` y
      `n8n/resumen-diario-inventario.json`
- [x] Ejecución end-to-end verificada: consulta KPIs y riesgo → crea
      máximo una orden BORRADOR para el primer producto en riesgo,
      con cantidad calculada según la fórmula de la skill → publica
      un único resumen del panel con alerta y acciones sugeridas
      correctamente tipadas
- [x] Verificado también dentro del entorno dockerizado, con el
      endpoint del nodo "MCP Client Tool" apuntando a
      `http://mcp-server:3001/sse` (nombre de servicio Docker)

**Decisión sobre la URL del MCP Client Tool:** el JSON exportado en el
repo mantiene `http://host.docker.internal:3001/sse` como valor por
defecto (funciona para el modo manual/mixto: n8n en Docker suelto +
backend/mcp-server corriendo como procesos locales). Cuando se ejecuta
el stack completo vía `docker compose up`, hay que cambiar ese campo a
`http://mcp-server:3001/sse` antes de correr el workflow — documentado
en el README.

## Dashboard (CERRADO)

- [x] Sección "Torre de control — LogiTrack IQ" agregada al
      `dashboard.html` existente (no se creó un frontend separado; se
      amplió el sistema base, decisión documentada más abajo)
- [x] `api.js` migrado de `localStorage` a `sessionStorage` (sección
      11 del PDF) — cambio único y compartido por todo el sistema, ya
      que se decidió reutilizar un solo login/api en vez de duplicar
      la capa de sesión
- [x] KPIs (4 indicadores), movimientos de ayer, ocupación por bodega
- [x] Resumen diario del panel (narrativa, alertas, acciones sugeridas)
- [x] Productos en riesgo
- [x] Tabla "Órdenes en BORRADOR": solo estado BORRADOR, acciones
      Aprobar/Cancelar (ADMIN)
- [x] Tabla "Todas las órdenes (histórico)": solo estados que ya
      salieron de BORRADOR (APROBADA/RECIBIDA/CANCELADA), sin
      duplicar las que siguen en BORRADOR. Para APROBADA: Recibir/
      Cancelar (ADMIN); para RECIBIDA/CANCELADA: sin acciones
      (estados finales)
- [x] Generar y visualizar PDF de cualquier orden (marca de agua
      BORRADOR visible cuando corresponde) — modal con visor embebido
- [x] Botón "Aprobar"/"Recibir"/"Cancelar" visibles solo para ADMIN
      autenticado
- [x] Tablas actualizadas automáticamente tras cualquier cambio de
      estado (BORRADOR ↔ histórico ↔ KPIs, refrescados en conjunto)
- [x] **Fix de alineación:** altura de fila fija (`height` + CSS) en
      las tablas de órdenes, para que las filas con botones de acción
      y las filas sin acciones (estados finales) no queden desalineadas
- [x] Verificado dentro del contenedor Docker: dashboard, KPIs,
      histórico y generación de PDF funcionan igual que en modo manual

**Decisión de diseño:** en vez de crear una carpeta `frontend/` nueva
en la raíz (como sugiere literalmente la "Estructura de referencia"
del PDF), se amplió el `dashboard.html`/`api.js`/`dashboard.js`/
`style.css` ya existentes del proyecto base. El PDF permite
explícitamente adaptar la estructura "siempre que las
responsabilidades estén separadas de forma clara". Un único login,
un único `api.js`, un único dashboard — la sección de LogiTrack IQ
vive claramente delimitada debajo del contenido original, sin romper
ninguna función heredada.

## Docker (CERRADO)

- [x] `Dockerfile` del backend (build multi-stage Maven → JRE)
- [x] `mcp-server/Dockerfile` (Node 20 alpine)
- [x] `docker-compose.yml` (backend + mcp-server + n8n, misma red
      interna `logitrack-net`, comunicación por nombre de servicio;
      `application.properties` y `mcp-server/.env` montados como
      volumen/env_file en tiempo de ejecución, no incluidos en las
      imágenes)
- [x] `docker compose up --build` levanta los 3 servicios
      correctamente — verificado: backend arranca limpio, n8n
      accesible, dashboard funcional, flujo n8n → mcp-server → backend
      confirmado con la URL de red Docker
- [x] **Decisión:** no se hace push a Docker Hub — como el repo se
      clona de todas formas en la máquina del centro de estudios,
      `docker compose build` construye las imágenes ahí mismo desde
      el código fuente; publicar en Docker Hub sería redundante
- [x] Documentar en README las dos rutas válidas de ejecución en la
      máquina del centro de estudios: vía Docker (`docker compose up
      --build`, recomendada) y manual (clonar + `mvnw`/`npm start`/n8n
      local), incluyendo el ajuste de URL del MCP Client Tool según la
      ruta elegida — ver sección "🚀 Instalación y ejecución" del
      `README.md`

## Cierre SDD y entrega

- [x] `docs/sdd/evidencia-sdd.md` — finalizado: enlaces a los 4
      documentos, evidencia de endpoints protegidos agregada (sección
      3.16), referencias a capturas inexistentes corregidas, reflexión
      final revisada (146 palabras)
- [x] README definitivo — instalación (Docker y manual), usuarios de
      prueba, rutas principales, evidencia de MCP y Swagger embebida,
      diagrama de arquitectura, espacio para el video
- [x] Diagrama n8n → MCP → API → BD → dashboard (`docs/diagrama-arquitectura.svg`)
- [ ] Video 4-6 min

## Nota menor (informativa, no es una tarea pendiente)

`mcp-server/env.example` contiene actualmente los mismos valores que
`.env`. Esto no representa un riesgo real — la contraseña de
`agente_mcp` ya está documentada intencionalmente en `data.sql` como
usuario de prueba para el profesor. Es solo una inconsistencia
estética de convención (el `.example` idealmente llevaría placeholders
genéricos en vez de valores reales copiados), sin impacto funcional
ni de seguridad. No requiere acción.

## Estado del backend + pruebas (BLOQUE CERRADO — sin pendientes)

La capa de backend + pruebas queda completa y verificada, incluyendo
las correcciones de ruta exigidas por el PDF de requerimientos
(`/productos/riesgo`, `/bodegas/criticas`), la confirmación visual de
Swagger UI, el fix de `ResumenPanelService` documentado arriba
(descubierto durante las pruebas reales de MCP/n8n), y la evidencia de
endpoints protegidos en vivo (login real + JWT + 401/403 por falta de
token y por rol incorrecto). El deliverable #4 del PDF queda
completo.

## Estado del bloque MCP + n8n (CERRADO)

Las 6 herramientas MCP están implementadas, probadas contra el
backend real (login real de `agente_mcp` confirmado), y documentadas
con evidencia de entrada/salida. El flujo `Resumen diario de
inventario` en n8n fue ejecutado de punta a punta con éxito, tanto en
modo manual como dentro del entorno dockerizado, respetando todas las
reglas de la skill (R32 incluida — sin herramienta de aprobación).
Tres bugs reales fueron encontrados y corregidos durante esta fase
(ver tabla arriba).

## Estado del bloque Dashboard (CERRADO)

La torre de control de LogiTrack IQ quedó integrada al dashboard
existente del proyecto base, con las 6 transiciones de estado de
orden accesibles desde la interfaz (crear queda excluido a propósito,
según R32/sección 9: las órdenes las crea el flujo automatizado, no
el dashboard). Verificado en vivo, tanto manual como en Docker:
aprobar, recibir (con movimiento ENTRADA automático confirmado),
cancelar, generar/visualizar PDF con y sin marca de agua.

## Estado del bloque Docker (CERRADO)

Los 3 servicios (backend, mcp-server, n8n) levantan correctamente con
un solo comando (`docker compose up --build`), comunicándose por
nombre de servicio dentro de la misma red interna. Verificado de
extremo a extremo: dashboard, generación de PDF, y ejecución completa
del flujo de n8n contra el backend dockerizado. Único pendiente:
documentar los pasos de ejecución en el README.

Siguiente bloque del proyecto: cierre SDD y entrega final (README,
diagrama, video).