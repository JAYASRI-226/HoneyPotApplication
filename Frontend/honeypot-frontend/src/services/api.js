// Central API client. In dev, Vite proxies "/api" to the Spring Boot backend
// (see vite.config.js), so this stays same-origin. Override with VITE_API_URL if needed.
const API_URL = import.meta.env.VITE_API_URL || "/api";

async function request(path, options) {
  const res = await fetch(`${API_URL}${path}`, {
    headers: { "Content-Type": "application/json" },
    ...options,
  });
  if (!res.ok) {
    let detail = `${res.status} ${res.statusText}`;
    try {
      const body = await res.json();
      if (body?.message) detail = body.message;
    } catch {
      /* ignore */
    }
    throw new Error(detail);
  }
  if (res.status === 204) return null;
  return res.json();
}

const get = (path) => request(path);
const post = (path, body) =>
  request(path, { method: "POST", body: body ? JSON.stringify(body) : undefined });
const put = (path, body) => request(path, { method: "PUT", body: JSON.stringify(body) });

export const api = {
  // Dashboard analytics
  summary: () => get("/dashboard/summary"),
  timeline: () => get("/dashboard/timeline"),
  eventTypes: () => get("/dashboard/event-types"),
  stages: () => get("/dashboard/stages"),
  severity: () => get("/dashboard/severity"),
  topAttackers: (limit = 8) => get(`/dashboard/top-attackers?limit=${limit}`),
  geo: () => get("/dashboard/geo"),
  recentEvents: (limit = 25) => get(`/dashboard/recent-events?limit=${limit}`),
  recentSessions: (limit = 10) => get(`/dashboard/recent-sessions?limit=${limit}`),

  // Resources
  honeypots: () => get("/honeypots"),
  attackers: () => get("/attackers"),
  attacker: (id) => get(`/attackers/${id}`),
  sessions: () => get("/attack-sessions"),
  session: (id) => get(`/attack-sessions/${id}`),
  sessionChain: (id) => get(`/attack-sessions/${id}/chain`),
  events: () => get("/attack-events"),
  alerts: () => get("/alerts"),

  // Mutations / control
  createHoneypot: (body) => post("/honeypots", body),
  updateAlert: (id, body) => put(`/alerts/${id}`, body),
  runIngest: () => post("/ingest/run"),
  ingestStatus: () => get("/ingest/status"),
};

export default api;
