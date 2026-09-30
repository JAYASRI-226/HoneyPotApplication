// Shared formatting + colour helpers for the dashboard.

export const SEV_COLOR = {
  INFO: "#64748b",
  LOW: "#38bdf8",
  MEDIUM: "#fbbf24",
  HIGH: "#fb923c",
  CRITICAL: "#f43f5e",
};

export const SEV_ORDER = ["INFO", "LOW", "MEDIUM", "HIGH", "CRITICAL"];

export const STAGE_COLOR = {
  UNKNOWN: "#64748b",
  RECONNAISSANCE: "#38bdf8",
  ACCESS: "#fbbf24",
  EXECUTION: "#fb923c",
  DELIVERY: "#a78bfa",
  LATERAL_MOVEMENT: "#f43f5e",
};

export const STAGE_ORDER = [
  "RECONNAISSANCE",
  "ACCESS",
  "EXECUTION",
  "DELIVERY",
  "LATERAL_MOVEMENT",
];

export const sevColor = (sev) => SEV_COLOR[sev] || SEV_COLOR.INFO;
export const sevClass = (sev) => `sev-${(sev || "info").toLowerCase()}`;
export const stageColor = (stage) => STAGE_COLOR[stage] || STAGE_COLOR.UNKNOWN;

export function riskColor(score) {
  const s = Number(score) || 0;
  if (s >= 75) return SEV_COLOR.CRITICAL;
  if (s >= 50) return SEV_COLOR.HIGH;
  if (s >= 25) return SEV_COLOR.MEDIUM;
  if (s > 0) return SEV_COLOR.LOW;
  return SEV_COLOR.INFO;
}

export function riskLabel(score) {
  const s = Number(score) || 0;
  if (s >= 75) return "Critical";
  if (s >= 50) return "High";
  if (s >= 25) return "Medium";
  if (s > 0) return "Low";
  return "None";
}

// Backend timestamps are ISO local date-times (no zone suffix). Normalise for Safari.
function toDate(value) {
  if (!value) return null;
  if (value instanceof Date) return value;
  const iso = String(value).includes("T") ? String(value) : String(value).replace(" ", "T");
  const d = new Date(iso.endsWith("Z") ? iso : `${iso}Z`);
  return Number.isNaN(d.getTime()) ? null : d;
}

export function fmtDateTime(value) {
  const d = toDate(value);
  if (!d) return "—";
  return d.toLocaleString(undefined, {
    year: "numeric",
    month: "short",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
    hour12: false,
  });
}

export function fmtTime(value) {
  const d = toDate(value);
  if (!d) return "—";
  return d.toLocaleTimeString(undefined, {
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
    hour12: false,
  });
}

export function fmtDate(value) {
  const d = toDate(value);
  if (!d) return "—";
  return d.toLocaleDateString(undefined, { year: "numeric", month: "short", day: "2-digit" });
}

export function timeAgo(value) {
  const d = toDate(value);
  if (!d) return "—";
  const secs = Math.floor((Date.now() - d.getTime()) / 1000);
  if (secs < 5) return "just now";
  if (secs < 60) return `${secs}s ago`;
  const mins = Math.floor(secs / 60);
  if (mins < 60) return `${mins}m ago`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24) return `${hrs}h ago`;
  const days = Math.floor(hrs / 24);
  if (days < 30) return `${days}d ago`;
  return fmtDate(value);
}

// Friendly label for a raw cowrie event id.
export function eventLabel(eventType) {
  if (!eventType) return "Event";
  const map = {
    "cowrie.session.connect": "Connection opened",
    "cowrie.session.closed": "Connection closed",
    "cowrie.client.version": "Client version probe",
    "cowrie.client.kex": "Key exchange / fingerprint",
    "cowrie.client.size": "Terminal size",
    "cowrie.session.params": "Session parameters",
    "cowrie.login.success": "Login SUCCESS",
    "cowrie.login.failed": "Login failed",
    "cowrie.command.input": "Command",
    "cowrie.log.closed": "TTY log closed",
  };
  return map[eventType] || eventType.replace("cowrie.", "").replace(/\./g, " ");
}

export function titleCase(str) {
  if (!str) return "";
  return str
    .toLowerCase()
    .split(/[\s_-]+/)
    .map((w) => w.charAt(0).toUpperCase() + w.slice(1))
    .join(" ");
}

export function compactNumber(n) {
  const num = Number(n) || 0;
  if (num >= 1_000_000) return `${(num / 1_000_000).toFixed(1)}M`;
  if (num >= 1000) return `${(num / 1000).toFixed(1)}k`;
  return String(num);
}
