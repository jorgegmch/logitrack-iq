import "dotenv/config";
import express from "express";
import { z } from "zod";
import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { SSEServerTransport } from "@modelcontextprotocol/sdk/server/sse.js";

const API_BASE_URL = process.env.API_BASE_URL ?? "http://localhost:8080";
const AGENTE_USERNAME = process.env.AGENTE_MCP_USERNAME;
const AGENTE_PASSWORD = process.env.AGENTE_MCP_PASSWORD;
const MCP_PORT = process.env.MCP_PORT ?? 3001;

if (!AGENTE_USERNAME || !AGENTE_PASSWORD) {
    console.error(
        "Faltan AGENTE_MCP_USERNAME / AGENTE_MCP_PASSWORD en el archivo .env. " +
        "Copia .env.example a .env y completa las credenciales del usuario AGENTE."
    );
    process.exit(1);
}

// ---------------------------------------------------------------------------
// Cliente HTTP hacia la API de LogiTrack IQ, con login JWT en memoria.
// R32: este servidor NO tiene logica de negocio propia ni acceso directo
// a la base de datos — todo pasa por la API REST, usando el usuario AGENTE.
// ---------------------------------------------------------------------------

let cachedToken = null;

async function login() {
    const respuesta = await fetch(`${API_BASE_URL}/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username: AGENTE_USERNAME, password: AGENTE_PASSWORD }),
    });

    if (!respuesta.ok) {
        throw new Error(
            `Login fallido contra ${API_BASE_URL}/auth/login (status ${respuesta.status}). ` +
            "Verifica que el usuario AGENTE exista y las credenciales sean correctas."
        );
    }

    const datos = await respuesta.json();

    // Intento defensivo: el nombre exacto del campo del JWT en
    // LoginResponse.java no estaba confirmado al escribir este archivo.
    // Se prueban los nombres mas comunes; si ninguno aparece, se lanza
    // un error claro con el cuerpo real de la respuesta para diagnosticar.
    const token = datos.token ?? datos.jwt ?? datos.accessToken;

    if (!token) {
        throw new Error(
            "No se encontro un campo de token reconocido en la respuesta de login. " +
            `Cuerpo recibido: ${JSON.stringify(datos)}. ` +
            "Revisa LoginResponse.java y ajusta el nombre del campo en login() (src/index.js)."
        );
    }

    return token;
}

async function apiFetch(path, options = {}) {
    if (!cachedToken) {
        cachedToken = await login();
    }

    const hacerPeticion = async () =>
        fetch(`${API_BASE_URL}${path}`, {
            ...options,
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${cachedToken}`,
                ...(options.headers ?? {}),
            },
        });

    let respuesta = await hacerPeticion();

    // Si el token expiro o quedo invalido, se reintenta una sola vez
    // con un login nuevo.
    if (respuesta.status === 401) {
        cachedToken = await login();
        respuesta = await hacerPeticion();
    }

    if (!respuesta.ok) {
        const cuerpoError = await respuesta.text();
        throw new Error(`Error ${respuesta.status} en ${path}: ${cuerpoError}`);
    }

    // 204 No Content no tiene cuerpo JSON que parsear
    if (respuesta.status === 204) {
        return null;
    }

    return respuesta.json();
}

// ---------------------------------------------------------------------------
// Servidor MCP y las 6 herramientas exactas (02-especificacion.md, seccion 9)
// ---------------------------------------------------------------------------

const server = new McpServer({
    name: "logitrack-iq-mcp-server",
    version: "1.0.0",
});

server.tool(
    "consultar_stock_producto",
    "Consulta el stock total calculado de un producto especifico, agregado desde los movimientos de inventario.",
    { productoId: z.number().int().positive() },
    async ({ productoId }) => {
        const resultado = await apiFetch(`/productos/${productoId}/stock`);
        return { content: [{ type: "text", text: JSON.stringify(resultado) }] };
    }
);

server.tool(
    "consultar_bodegas_criticas",
    "Lista las bodegas con ocupacion critica (mayor o igual al 90%).",
    {},
    async () => {
        const resultado = await apiFetch("/bodegas/criticas");
        return { content: [{ type: "text", text: JSON.stringify(resultado) }] };
    }
);

server.tool(
    "consultar_productos_en_riesgo",
    "Lista los productos actualmente en riesgo de quiebre de stock (stock por debajo del punto de reorden).",
    {},
    async () => {
        const resultado = await apiFetch("/productos/riesgo");
        return { content: [{ type: "text", text: JSON.stringify(resultado) }] };
    }
);

server.tool(
    "consultar_kpis",
    "Obtiene el resumen completo de indicadores del dashboard de LogiTrack IQ.",
    {},
    async () => {
        const resultado = await apiFetch("/kpis");
        return { content: [{ type: "text", text: JSON.stringify(resultado) }] };
    }
);

server.tool(
    "crear_orden_borrador",
    "Crea una orden de compra en estado BORRADOR. Solo crea el registro; NO genera PDF ni la aprueba (R32: no existe herramienta para aprobar ordenes).",
    {
        productoId: z.number().int().positive(),
        proveedorId: z.number().int().positive(),
        bodegaDestinoId: z.number().int().positive(),
        cantidad: z.number().int().positive(),
        precioUnitario: z.number().nonnegative(),
    },
    async ({ productoId, proveedorId, bodegaDestinoId, cantidad, precioUnitario }) => {
        const resultado = await apiFetch("/ordenes", {
            method: "POST",
            body: JSON.stringify({ productoId, proveedorId, bodegaDestinoId, cantidad, precioUnitario }),
        });
        return { content: [{ type: "text", text: JSON.stringify(resultado) }] };
    }
);

server.tool(
    "publicar_resumen",
    "Publica el resumen diario del panel (fecha, narrativa, alertas, acciones sugeridas). Reemplaza el resumen existente para la misma fecha.",
    {
        fecha: z.string().describe("Fecha en formato YYYY-MM-DD"),
        narrativa: z.string(),
        alertas: z.array(z.object({
            severidad: z.enum(["BAJA", "MEDIA", "ALTA"]),
            titulo: z.string(),
            detalle: z.string(),
            productoId: z.number().int().positive().optional(),
            ordenId: z.number().int().positive().optional(),
            bodegaId: z.number().int().positive().optional(),
        })),
        accionesSugeridas: z.array(z.object({
            descripcion: z.string(),
            productoId: z.number().int().positive().optional(),
            ordenId: z.number().int().positive().optional(),
            bodegaId: z.number().int().positive().optional(),
        })),
    },
    async ({ fecha, narrativa, alertas, accionesSugeridas }) => {
        const resultado = await apiFetch("/panel/resumen", {
            method: "POST",
            body: JSON.stringify({ fecha, narrativa, alertas, accionesSugeridas }),
        });
        return { content: [{ type: "text", text: JSON.stringify(resultado) }] };
    }
);

// ---------------------------------------------------------------------------
// Transporte SSE sobre Express, para que n8n (MCP Client Tool) se conecte
// como cliente por HTTP. Patron oficial del SDK v1.x.
// ---------------------------------------------------------------------------

const app = express();
app.use(express.json());

const transportesActivos = {};

app.get("/sse", async (req, res) => {
    const transporte = new SSEServerTransport("/messages", res);
    transportesActivos[transporte.sessionId] = transporte;

    res.on("close", () => {
        delete transportesActivos[transporte.sessionId];
    });

    await server.connect(transporte);
});

app.post("/messages", async (req, res) => {
    const sessionId = req.query.sessionId;
    const transporte = transportesActivos[sessionId];

    if (!transporte) {
        res.status(400).send("No hay una sesion SSE activa con ese sessionId");
        return;
    }

    await transporte.handlePostMessage(req, res, req.body);
});

app.listen(MCP_PORT, () => {
    console.log(`Servidor MCP de LogiTrack IQ escuchando en http://localhost:${MCP_PORT}`);
    console.log(`Endpoint SSE para n8n: http://localhost:${MCP_PORT}/sse`);
});