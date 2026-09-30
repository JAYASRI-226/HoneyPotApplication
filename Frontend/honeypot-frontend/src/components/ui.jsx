import { AlertTriangle, Inbox, RefreshCw } from "lucide-react";
import { sevColor, stageColor, riskColor } from "../utils/format";

/* ------------------------------- Card ------------------------------- */

export function Card({ className = "", children, style }) {
  return (
    <section className={`card ${className}`} style={style}>
      {children}
    </section>
  );
}

export function CardHead({ title, icon, sub, actions }) {
  return (
    <div className="card-head">
      <h3>
        {icon}
        {title}
        {sub && <span className="sub">· {sub}</span>}
      </h3>
      {actions && <div className="row-flex">{actions}</div>}
    </div>
  );
}

/* ----------------------------- Stat card ---------------------------- */

export function StatCard({ icon, label, value, accent = "var(--accent)", sub, onClick }) {
  return (
    <div
      className="card stat"
      style={{ "--accent-color": accent, cursor: onClick ? "pointer" : "default" }}
      onClick={onClick}
      role={onClick ? "button" : undefined}
    >
      <div className="stat-icon">{icon}</div>
      <div className="stat-body">
        <div className="stat-value">{value}</div>
        <div className="stat-label">{label}</div>
        {sub && <div className="stat-trend">{sub}</div>}
      </div>
    </div>
  );
}

/* ------------------------------ Badges ------------------------------ */

export function SeverityBadge({ severity, showDot = true }) {
  const sev = (severity || "INFO").toUpperCase();
  return (
    <span className={`badge sev-${sev.toLowerCase()}`}>
      {showDot && <span className="dot" />}
      {sev}
    </span>
  );
}

export function StageBadge({ stage }) {
  if (!stage) return null;
  const color = stageColor(stage);
  const label = stage.replace(/_/g, " ").toLowerCase().replace(/\b\w/g, (c) => c.toUpperCase());
  return (
    <span
      className="badge"
      style={{ color, background: `${color}22`, borderColor: `${color}55` }}
    >
      <span className="dot" />
      {label}
    </span>
  );
}

export function StatusChip({ status }) {
  const s = (status || "").toUpperCase();
  const on = s === "ACTIVE" || s === "NEW" || s === "ONLINE";
  return <span className={`chip ${on ? "on" : "off"}`}>{s || "—"}</span>;
}

/* ---------------------------- Risk meter ---------------------------- */

export function RiskMeter({ score = 0 }) {
  const value = Math.max(0, Math.min(100, Number(score) || 0));
  const color = riskColor(value);
  return (
    <div className="risk" title={`Risk score ${value}/100`}>
      <div className="risk-bar">
        <div className="risk-fill" style={{ width: `${value}%`, background: color }} />
      </div>
      <span className="risk-num" style={{ color }}>
        {value}
      </span>
    </div>
  );
}

/* -------------------------- Section title --------------------------- */

export function SectionTitle({ icon, children }) {
  return (
    <div className="section-title">
      {icon}
      {children}
    </div>
  );
}

/* ------------------------------ States ------------------------------ */

export function Loader({ label = "Loading…" }) {
  return (
    <div className="state">
      <div className="spinner" />
      <p>{label}</p>
    </div>
  );
}

export function Empty({ icon, title = "Nothing here yet", message }) {
  return (
    <div className="state">
      <div className="icon">{icon || <Inbox size={22} />}</div>
      <h4>{title}</h4>
      {message && <p>{message}</p>}
    </div>
  );
}

export function ErrorState({ message, onRetry }) {
  return (
    <div className="state">
      <div className="icon" style={{ color: "var(--sev-critical)" }}>
        <AlertTriangle size={22} />
      </div>
      <h4>Couldn’t load data</h4>
      <p>{message || "The backend may be offline. Start the Spring Boot API and retry."}</p>
      {onRetry && (
        <button className="btn" onClick={onRetry}>
          <RefreshCw size={15} /> Retry
        </button>
      )}
    </div>
  );
}

export function sevDot(severity) {
  return { background: sevColor(severity) };
}
