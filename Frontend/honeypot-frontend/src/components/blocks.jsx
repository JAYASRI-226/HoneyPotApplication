import { ShieldAlert, Crosshair, Clock } from "lucide-react";
import { SeverityBadge, StageBadge, StatusChip, RiskMeter } from "./ui";
import { sevColor, stageColor, eventLabel, timeAgo, fmtTime } from "../utils/format";

/* ------------------------------ Alert row ----------------------------- */

export function AlertRow({ alert, onOpen, actions }) {
  const color = sevColor(alert.severity);
  return (
    <div className="alert-row" onClick={onOpen} style={{ cursor: onOpen ? "pointer" : "default" }}>
      <div className="alert-icon" style={{ background: `${color}1f`, color, border: `1px solid ${color}44` }}>
        <ShieldAlert size={19} />
      </div>
      <div className="alert-content">
        <div className="alert-title">
          {alert.message}
          <SeverityBadge severity={alert.severity} />
        </div>
        {alert.description && <div className="alert-desc">{alert.description}</div>}
        <div className="alert-foot">
          {alert.attackType && <span className="chip">{alert.attackType}</span>}
          {alert.stage && <StageBadge stage={alert.stage} />}
          {alert.sourceIp && <span className="ip">{alert.sourceIp}</span>}
          <span className="row-flex" style={{ gap: 5 }}>
            <Clock size={11} /> {timeAgo(alert.createdAt)}
          </span>
          <StatusChip status={alert.status} />
          {actions && (
            <span className="row-flex" style={{ gap: 7, marginLeft: "auto" }} onClick={(e) => e.stopPropagation()}>
              {actions}
            </span>
          )}
        </div>
      </div>
    </div>
  );
}

/* ------------------------------ Feed item ----------------------------- */

export function FeedItem({ event }) {
  const color = sevColor(event.severity);
  return (
    <div className="feed-item">
      <span className="feed-sev" style={{ background: color }} />
      <div className="feed-main">
        <div className="feed-title">
          {eventLabel(event.eventType)}
          {event.command && <code className="cmd">{event.command}</code>}
          <SeverityBadge severity={event.severity} showDot={false} />
        </div>
        <div className="feed-sub" title={event.eventData}>
          {event.sourceIp} → {event.destinationIp}:{event.destinationPort}
          {event.eventData ? ` · ${event.eventData}` : ""}
        </div>
      </div>
      <span className="feed-time">{fmtTime(event.timestamp)}</span>
    </div>
  );
}

/* ---------------------------- Attacker row ---------------------------- */

export function AttackerRow({ attacker, onOpen }) {
  return (
    <div className="list-row" onClick={onOpen} style={{ cursor: onOpen ? "pointer" : "default" }}>
      <div className="list-main">
        <div className="list-title">
          <Crosshair size={14} style={{ color: "var(--text-mute)" }} />
          <span className="ip">{attacker.ipAddress}</span>
          <SeverityBadge severity={attacker.maxSeverity} showDot={false} />
        </div>
        <div className="list-sub">
          {attacker.country || "Unknown location"}
          {attacker.city ? ` · ${attacker.city}` : ""} · {attacker.totalSessions ?? 0} sessions ·{" "}
          {attacker.eventCount ?? 0} events
        </div>
      </div>
      <RiskMeter score={attacker.riskScore} />
    </div>
  );
}

/* ----------------------------- Stage pills ---------------------------- */

export function StagePills({ stages }) {
  const list = String(stages || "")
    .split(",")
    .map((s) => s.trim())
    .filter(Boolean);
  if (!list.length) return <span className="muted">—</span>;
  return (
    <div className="row-flex wrap" style={{ gap: 5 }}>
      {list.map((s) => (
        <StageBadge key={s} stage={s} />
      ))}
    </div>
  );
}

/* ------------------------------ Kill chain ---------------------------- */

export function KillChain({ chain = [] }) {
  if (!chain.length) return null;
  return (
    <div className="killchain">
      {chain.map((group, i) => {
        const color = stageColor(group.stage);
        const last = i === chain.length - 1;
        return (
          <div className="kc-stage" key={`${group.stage}-${i}`}>
            <div className="kc-rail">
              <div className="kc-node" style={{ borderColor: color, color, boxShadow: `0 0 0 4px ${color}14` }}>
                {i + 1}
              </div>
              {!last && <div className="kc-line" />}
            </div>
            <div className="kc-body" style={{ borderColor: `${color}44` }}>
              <div className="kc-head">
                <div className="kc-title" style={{ color }}>
                  <span className="dot" style={{ background: color }} />
                  {group.label}
                </div>
                <div className="row-flex" style={{ gap: 9 }}>
                  <SeverityBadge severity={group.maxSeverity} />
                  <span className="kc-meta">{group.eventCount} events</span>
                </div>
              </div>
              <div className="kc-events">
                {group.events.map((ev) => (
                  <div className="kc-event" key={ev.id} title={ev.eventData}>
                    <span className="feed-sev" style={{ background: sevColor(ev.severity) }} />
                    <span>{eventLabel(ev.eventType)}</span>
                    {ev.command && <span className="cmd">{ev.command}</span>}
                    <time>{fmtTime(ev.timestamp)}</time>
                  </div>
                ))}
              </div>
            </div>
          </div>
        );
      })}
    </div>
  );
}
