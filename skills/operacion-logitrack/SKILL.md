# Skill: Operación diaria de LogiTrack IQ

Este skill define el comportamiento permitido del agente automatizado
que ejecuta el resumen diario de inventario de LogiTrack IQ.

## Reglas obligatorias

1. Siempre consulta primero `consultar_kpis` y `consultar_productos_en_riesgo`
   antes de tomar cualquier otra acción.
2. Puedes crear como máximo **una** orden de compra en BORRADOR por
   ejecución, únicamente para el **primer producto** listado en
   `consultar_productos_en_riesgo`.
3. La cantidad de la orden se calcula como:
   `ceil(max(1, puntoReorden * 2 - stockTotal))`.
4. Usa `proveedorId` y `bodegaDestinoId` exactamente como vienen en la
   respuesta de `consultar_productos_en_riesgo` para ese producto —
   nunca los inventes ni los tomes de otra fuente.
5. Nunca apruebes, canceles ni recibas órdenes. No existe herramienta
   para eso y no debes simular esa acción de ninguna otra forma.
6. Al final, publica **un único** resumen con `publicar_resumen` que
   cumpla exactamente este contrato:
   - `fecha`: fecha actual en America/Bogota, formato YYYY-MM-DD.
   - `narrativa`: entre 20 y 500 caracteres, resume lo encontrado.
   - `alertas`: arreglo (puede ir vacío). Cada alerta tiene
     `severidad` (BAJA/MEDIA/ALTA), `titulo`, `detalle`, y al menos
     uno de `productoId`/`ordenId`/`bodegaId`.
   - `accionesSugeridas`: arreglo (puede ir vacío). Cada acción tiene
     `tipo` (REVISAR_ORDEN/REVISAR_PRODUCTO/REVISAR_BODEGA),
     `descripcion`, y exactamente uno de
     `ordenId`/`productoId`/`bodegaId`.
7. Si no hay productos en riesgo, no crees ninguna orden — publica
   igualmente el resumen del día indicando que no hay riesgos.
8. Si cualquier herramienta falla, detente, no intentes de nuevo de
   forma indefinida, y reporta el error con claridad en tu respuesta
   final en lugar de asumir un resultado exitoso.