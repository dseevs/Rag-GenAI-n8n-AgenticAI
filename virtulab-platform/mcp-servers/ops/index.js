#!/usr/bin/env node
/**
 * VirtuLab MCP — Ops tools (Phase 8)
 */
import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { z } from "zod";

const RAG_URL = process.env.VIRTULAB_RAG_URL ?? "http://localhost:8086";
const NOTIF_URL = process.env.VIRTULAB_NOTIF_URL ?? "http://localhost:8092";
const AUDIT_URL = process.env.VIRTULAB_AUDIT_URL ?? "http://localhost:8096";
const TOKEN = process.env.VIRTULAB_API_TOKEN ?? "";

async function apiGet(base, path, auth = false) {
  const headers = { Accept: "application/json" };
  if (auth && TOKEN) headers.Authorization = `Bearer ${TOKEN}`;
  const res = await fetch(`${base}${path}`, { headers });
  if (!res.ok) throw new Error(`HTTP ${res.status}: ${await res.text()}`);
  return res.json();
}

const server = new McpServer({ name: "virtulab-ops", version: "0.1.0" });

server.tool(
  "rag_stats",
  "RAG corpus chunk statistics (requires DEVELOPER token)",
  {},
  async () => {
    const data = await apiGet(RAG_URL, "/api/v1/rag/stats", true);
    return { content: [{ type: "text", text: JSON.stringify(data, null, 2) }] };
  }
);

server.tool(
  "notification_stats",
  "RabbitMQ notification consumer processed count",
  {},
  async () => {
    const data = await apiGet(NOTIF_URL, "/api/v1/notifications/stats");
    return { content: [{ type: "text", text: JSON.stringify(data, null, 2) }] };
  }
);

server.tool(
  "audit_recent",
  "Recent immutable audit trail events",
  {
    userId: z.string().optional(),
    limit: z.number().int().min(1).max(200).default(20),
  },
  async ({ userId, limit }) => {
    const q = new URLSearchParams({ limit: String(limit) });
    if (userId) q.set("userId", userId);
    const data = await apiGet(AUDIT_URL, `/api/v1/audit/events?${q}`);
    return { content: [{ type: "text", text: JSON.stringify(data, null, 2) }] };
  }
);

server.tool(
  "service_health",
  "Check actuator health for a VirtuLab service port",
  { port: z.number().int().default(8097) },
  async ({ port }) => {
    const data = await apiGet(`http://localhost:${port}`, "/actuator/health");
    return { content: [{ type: "text", text: JSON.stringify(data, null, 2) }] };
  }
);

const transport = new StdioServerTransport();
await server.connect(transport);
