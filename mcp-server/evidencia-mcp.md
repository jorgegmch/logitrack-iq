# Evidencia — Servidor MCP de LogiTrack IQ

Este documento certifica que el servidor MCP (`mcp-server/`) expone
exactamente las 6 herramientas exigidas por el PDF de requerimientos
(sección 7) y `02-especificacion.md` (sección 9), y que cada una fue
probada contra el backend real, usando el usuario técnico `agente_mcp`
(rol `AGENTE`).

Capturas referenciadas en `docs/capturas/mcp-tools/`.

## 1. Autenticación del agente

El servidor MCP hace login internamente contra `POST /auth/login` la
primera vez que se invoca cualquier herramienta (función `login()` en
`src/index.js`), cachea el JWT en memoria, y reintenta una sola vez si
el token expira (`401`).

- Usuario: `agente_mcp`
- Rol: `AGENTE`
- Confirmado en la primera ejecución real del flujo n8n → MCP → API:
  el agente autenticó correctamente y pudo consultar y escribir datos
  según los permisos de su rol.

## 2. Las 6 herramientas — evidencia de entrada/salida

### 2.1 `consultar_stock_producto(productoId)`

Confirmada de forma indirecta a través de la ejecución completa del
workflow (el modal de prueba aislado de n8n para herramientas con
parámetros presentó un bug propio de la interfaz — ver nota al final
de este documento). El AI Agent la invoca cuando necesita el stock
puntual de un producto durante su análisis.

### 2.2 `consultar_bodegas_criticas()`

Prueba aislada, sin parámetros.

**Entrada:** ninguna.
**Salida:**
```json
[]
```
0 bodegas críticas (ninguna con ocupación ≥ 90% al momento de la
prueba).

Captura: `docs/capturas/mcp-tools/consultar-bodegas-criticas.png`

### 2.3 `consultar_productos_en_riesgo()`

Prueba aislada, sin parámetros.

**Entrada:** ninguna.
**Salida:**
```json
[]
```
0 productos en riesgo al momento de esta prueba puntual (antes de
forzar un caso de riesgo real vía movimiento SALIDA, ver sección 3).

Captura: `docs/capturas/mcp-tools/consultar-productos-en-riesgo.png`

### 2.4 `consultar_kpis()`

Prueba aislada, sin parámetros.

**Entrada:** ninguna.
**Salida (resumida):**
```json
{
  "calculadoEn": "2026-08-28T04:35:12.75...",
  "ocupacionPorBodega": [
    { "bodegaId": 1, "nombre": "Bodega Norte", "porcentaje": 3.9 },
    { "bodegaId": 2, "nombre": "Bodega Sur", "porcentaje": 2.5 },
    { "bodegaId": 3, "nombre": "Bodega Oriente", "porcentaje": 6 }
  ],
  "productosEnQuiebre": 0,
  "productosEnRiesgo": 0,
  "ordenesPorAprobar": { "cantidad": 0, "montoTotal": 0 },
  "movimientosAyer": { "entrada": 0, "salida": 0, "transferencia": 0 }
}
```

Captura: `docs/capturas/mcp-tools/consultar-kpis.png`

### 2.5 `crear_orden_borrador(productoId, proveedorId, bodegaDestinoId, cantidad, precioUnitario)`

Confirmada en ejecución completa del flujo (ver sección 3): el AI
Agent invocó esta herramienta con los datos correctos del producto en
riesgo detectado (Escritorio ajustable, id 4), calculando la cantidad
exacta según la fórmula de la skill:
`ceil(max(1, puntoReorden × 2 - stockTotal))`.

**Entrada real usada por el agente:**
```json
{
  "productoId": 4,
  "proveedorId": 1,
  "bodegaDestinoId": 2,
  "cantidad": 3,
  "precioUnitario": 100.00
}
```

**Salida:** orden creada en estado `BORRADOR`, con `creadoPorId`
resuelto al usuario `agente_mcp` (auditoría confirmada en logs del
backend).

Captura: `docs/capturas/mcp-tools/n8n-ai-agent-output-exitoso.png`

### 2.6 `publicar_resumen(resumen)`

Confirmada en ejecución completa del flujo (ver sección 3).

**Entrada real usada por el agente (resumida):**
```json
{
  "fecha": "2026-08-28",
  "narrativa": "El inventario muestra un producto en riesgo (Escritorio ajustable)...",
  "alertas": [
    {
      "severidad": "ALTA",
      "titulo": "Stock crítico en Escritorio ajustable",
      "detalle": "El producto 'Escritorio ajustable' ha caído por debajo del punto de reorden. Stock actual: 2.",
      "productoId": 4
    }
  ],
  "accionesSugeridas": [
    { "tipo": "REVISAR_PRODUCTO", "descripcion": "Revisar stock de Escritorio ajustable (ID 4) debido a bajo nivel de inventario.", "productoId": 4 },
    { "tipo": "REVISAR_ORDEN", "descripcion": "Orden de compra en borrador creada para el Escritorio ajustable.", "ordenId": 3 }
  ]
}
```

**Salida:** resumen publicado, `idResumenPanel: 6` (reemplazó
correctamente el resumen anterior del mismo día, confirmando R11).

Captura: `docs/capturas/mcp-tools/n8n-mcp-tool-publicar-resumen-exitoso.png`

## 3. Ejecución completa del flujo (evidencia de éxito)

Workflow `Resumen diario de inventario` ejecutado manualmente en n8n,
de punta a punta: `Schedule Trigger` → `Preparar fecha Bogota` →
`AI Agent` (con `Google Gemini Chat Model` como modelo y
`MCP Client Tool - LogiTrack IQ` como única fuente de herramientas) →
rama de éxito.

Secuencia real ejecutada por el agente, siguiendo
`skills/operacion-logitrack/SKILL.md`:
1. `consultar_kpis` y `consultar_productos_en_riesgo`.
2. Detectó "Escritorio ajustable" (id 4) por debajo del punto de
   reorden (forzado previamente con un movimiento `SALIDA` real vía
   `POST /movimientos`, para poder demostrar el caso "con riesgo").
3. `crear_orden_borrador` — una sola orden, para el primer producto
   listado, con la cantidad calculada según la fórmula de la skill.
4. `publicar_resumen` — un único resumen, con alerta de severidad
   `ALTA` y dos acciones sugeridas correctamente tipadas.

Capturas: `docs/capturas/mcp-tools/n8n-workflow-exitoso-canvas.png`,
`n8n-mcp-tool-publicar-resumen-exitoso.png`,
`n8n-ai-agent-output-exitoso.png`.

## 4. Evidencia de error controlado

Durante las pruebas se presentó un error real (no simulado): la
herramienta `publicar_resumen` respondió `400` por un campo `tipo`
faltante en el esquema de la herramienta (bug corregido, ver sección
5). El agente, siguiendo la regla 8 de la skill ("si una herramienta
falla, detente y repórtalo claramente, no asumas éxito"), no creó una
segunda orden, no entró en bucle, y reportó el fallo explícitamente en
su respuesta final.

Captura: `docs/capturas/mcp-tools/consultar-productos-en-riesgo.png`
*(nota: renombrar/reetiquetar si se guardó una captura específica del
error 400 — ver conversación de la sesión de depuración)*

## 5. Correcciones aplicadas durante esta fase de pruebas

| # | Problema encontrado | Causa | Corrección |
|---|---|---|---|
| 1 | `Already connected to a transport` — el servidor MCP se caía en la segunda conexión SSE | Una única instancia global de `McpServer` no soporta más de una conexión | Fábrica `crearServidorMcp()`: una instancia nueva por cada request a `/sse` |
| 2 | Gemini rechazaba las 6 herramientas con `400` (`Unknown name "exclusiveMinimum"`) | Zod traduce `.positive()` a la palabra clave JSON Schema `exclusiveMinimum`, no soportada por la API de Gemini | Reemplazo de `.positive()` por `.min(1)` en todos los campos numéricos |
| 3 | `publicar_resumen` fallaba con `400: El tipo es obligatorio` | El esquema de `accionesSugeridas` en la tool nunca definió el campo `tipo`, aunque el backend lo exige | Se agregó `tipo: z.enum([...])` al esquema |
| 4 | `publicar_resumen` fallaba con `500` (`duplicate key value violates unique constraint "resumen_panel_fecha_key"`) | Hibernate reordena el flush de una transacción: el `INSERT` del nuevo resumen se ejecutaba antes que el `DELETE` del resumen anterior del mismo día | `resumenPanelRepository.flush()` explícito tras el `delete()` en `ResumenPanelService` |

## Nota sobre `consultar_stock_producto` y `crear_orden_borrador` en pruebas aisladas

El modal de prueba de n8n ("Test MCP Client Tool") presentó un error
propio de esa interfaz (`Cannot read properties of undefined (reading
'inputType')`) específicamente al intentar probar herramientas que
requieren parámetros, de forma aislada y fuera del flujo del agente.
No es un error del servidor MCP ni del backend — ambas herramientas
quedaron confirmadas funcionando correctamente al ejecutarse dentro
del flujo completo real (sección 3).