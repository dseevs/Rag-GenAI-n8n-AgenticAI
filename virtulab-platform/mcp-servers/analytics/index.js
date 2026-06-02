#!/usr/bin/env node
/**
 * VirtuLab MCP — Analytics tools (Phase 8)
 * Env: VIRTULAB_API_TOKEN (Keycloak Bearer), VIRTULAB_ANALYTICS_URL (default http://localhost:8091)
 */
import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { z } from "zod";

const BASE = process.env.VIRTULAB_ANALYTICS_URL ?? "http://localhost:8091";
const ML_BASE = process.env.VIRTULAB_ML_URL ?? "http://localhost:8095";
const TOKEN = process.env.VIRTULAB_API_TOKEN ?? "";

async function apiGet(path) {
  const headers = { Accept: "application/json" };
  if (TOKEN) headers.Authorization = `Bearer ${TOKEN}`;
  const res = await fetch(`${BASE}${path}`, { headers });
  if (!res.ok) {
    throw new Error(`HTTP ${res.status}: ${await res.text()}`);
  }
  return res.json();
}

const server = new McpServer({ name: "virtulab-analytics", version: "0.1.0" });

server.tool(
  "org_funnel",
  "Query org lab completion funnel (analytics-query-service)",
  { orgId: z.string().default("org-dev") },
  async ({ orgId }) => {
    const data = await apiGet(`/api/v1/analytics/org-funnel?orgId=${encodeURIComponent(orgId)}`);
    return {
      content: [{ type: "text", text: JSON.stringify(data, null, 2) }],
    };
  }
);

server.tool(
  "experiment_summary",
  "Summary stats for an experiment",
  { experimentId: z.string() },
  async ({ experimentId }) => {
    const data = await apiGet(
      `/api/v1/analytics/experiment-summary?experimentId=${encodeURIComponent(experimentId)}`
    );
    return {
      content: [{ type: "text", text: JSON.stringify(data, null, 2) }],
    };
  }
);

server.tool(
  "ai_vs_progress",
  "Correlate AI usage with progress events per org",
  { orgId: z.string().default("org-dev") },
  async ({ orgId }) => {
    const data = await apiGet(`/api/v1/analytics/ai-vs-progress?orgId=${encodeURIComponent(orgId)}`);
    return {
      content: [{ type: "text", text: JSON.stringify(data, null, 2) }],
    };
  }
);

server.tool(
  "model_vs_actual",
  "ML completion predictions vs actual outcomes (ml-scoring-service)",
  { orgId: z.string().default("org-dev") },
  async ({ orgId }) => {
    const headers = { Accept: "application/json" };
    if (TOKEN) headers.Authorization = `Bearer ${TOKEN}`;
    const res = await fetch(
      `${ML_BASE}/api/v1/ml/model-vs-actual?orgId=${encodeURIComponent(orgId)}`,
      { headers }
    );
    if (!res.ok) throw new Error(`HTTP ${res.status}: ${await res.text()}`);
    const data = await res.json();
    return {
      content: [{ type: "text", text: JSON.stringify(data, null, 2) }],
    };
  }
);

const transport = new StdioServerTransport();
await server.connect(transport);
