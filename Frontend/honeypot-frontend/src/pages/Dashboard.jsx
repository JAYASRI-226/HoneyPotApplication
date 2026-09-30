import { Link, useNavigate } from "react-router-dom";
import {
  Radar,
  Terminal,
  GitBranch,
  Zap,
  ShieldAlert,
  Crosshair,
  TrendingUp,
  Activity,
  PieChart as PieIcon,
  ArrowRight,
} from "lucide-react";
import { useApp } from "../store/AppContext";
import { useApi } from "../hooks/useApi";
import { api } from "../services/api";
import { Card, CardHead, StatCard, Loader, ErrorState, Empty, RiskMeter } from "../components/ui";
import { TimelineChart, EventTypesChart, SeverityDonut, StageFunnel } from "../components/charts";
import { FeedItem, AttackerRow, AlertRow, StagePills } from "../components/blocks";
import { sevColor, fmtDateTime } from "../utils/format";

function Dash() {
  const nav = useNavigate();
  const { summary, summaryLoading, online, refreshAll, refreshToken } = useApp();

  const timeline = useApi(() => api.timeline(), [refreshToken], { poll: 20000 });
  const eventTypes = useApi(() => api.eventTypes(), [refreshToken]);
  const topAttackers = useApi(() => api.topAttackers(6), [refreshToken]);
  const recentEvents = useApi(() => api.recentEvents(18), [refreshToken], { poll: 15000 });
  const recentSessions = useApi(() => api.recentSessions(6), [refreshToken]);
  const alerts = useApi(() => api.alerts(), [refreshToken], { poll: 20000 });

  if (summaryLoading && !summary) return <Loader label="Loading threat overview…" />;
  if (!online && !summary) return <ErrorState onRetry={refreshAll} />;

  const s = summary || {};
  const sev = s.severityBreakdown || [];
  const stages = s.stageBreakdown || [];

  return (
    <>
      <div className="page-head">
        <div>
          <h2>Threat Overview</h2>
          <p>
            Live correlation of honeypot activity across the attack kill chain
            {s.lastEventAt ? ` · last event ${fmtDateTime(s.lastEventAt)}` : ""}.
          </p>
        </div>
        <div className="legend">
          {sev.map((x) => (
            <span key={x.name}>
              <i style={{ background: sevColor(x.name) }} /> {x.label} · {x.count}
            </span>
          ))}
        </div>
      </div>

      {/* ---------------------------- Stat cards ---------------------------- */}
      <div className="grid stats-grid">
        <StatCard icon={<Radar size={22} />} label="Active honeypots" accent="#22d3ee"
          value={`${s.activeHoneypots ?? 0}/${s.honeypots ?? 0}`} sub="sensors online" onClick={() => nav("/honeypots")} />
        <StatCard icon={<Terminal size={22} />} label="Attack sessions" accent="#6366f1"
          value={s.sessions ?? 0} sub={`${s.activeSessions ?? 0} active now`} onClick={() => nav("/sessions")} />
        <StatCard icon={<GitBranch size={22} />} label="Multi-stage attacks" accent="#f43f5e"
          value={s.multiStageSessions ?? 0} sub="correlated kill chains" onClick={() => nav("/sessions")} />
        <StatCard icon={<Zap size={22} />} label="Events ingested" accent="#38bdf8"
          value={s.events ?? 0} sub={s.topAttackType || "—"} onClick={() => nav("/events")} />
        <StatCard icon={<ShieldAlert size={22} />} label="Alerts" accent="#fb923c"
          value={s.alerts ?? 0} sub={`${s.criticalAlerts ?? 0} critical · ${s.newAlerts ?? 0} new`} onClick={() => nav("/alerts")} />
        <StatCard icon={<Crosshair size={22} />} label="Unique attackers" accent="#a78bfa"
          value={s.attackers ?? 0} sub={s.topAttackerIp ? `top ${s.topAttackerIp}` : "—"} onClick={() => nav("/attackers")} />
        <StatCard icon={<TrendingUp size={22} />} label="Peak risk score" accent="#34d399"
          value={`${s.highestRisk ?? 0}`} sub="out of 100" />
      </div>

      {/* --------------------- Timeline + severity donut -------------------- */}
      <div className="grid split-2-1 mt">
        <Card>
          <CardHead title="Attack Timeline" icon={<Activity size={16} />} sub="events & critical over time" />
          <div className="card-pad">
            {timeline.loading && !timeline.data ? (
              <div className="skeleton" style={{ height: 264 }} />
            ) : (timeline.data?.length ?? 0) === 0 ? (
              <Empty title="No timeline data" message="Events will appear here once honeypot activity is ingested." />
            ) : (
              <TimelineChart data={timeline.data} />
            )}
          </div>
        </Card>

        <Card>
          <CardHead title="Severity Mix" icon={<PieIcon size={16} />} />
          <div className="card-pad">
            {sev.length === 0 ? (
              <Empty title="No events yet" />
            ) : (
              <SeverityDonut data={sev} />
            )}
          </div>
        </Card>
      </div>

      {/* -------------------- Kill-chain stages + event types --------------- */}
      <div className="grid cols-2 mt">
        <Card>
          <CardHead title="Kill-chain Stages" icon={<GitBranch size={16} />} sub="event classification" />
          <div className="card-pad">
            {stages.length === 0 ? <Empty title="No stages classified" /> : <StageFunnel data={stages} />}
          </div>
        </Card>

        <Card>
          <CardHead title="Event Types" icon={<Zap size={16} />} sub="cowrie telemetry" />
          <div className="card-pad">
            {(eventTypes.data?.length ?? 0) === 0 ? (
              eventTypes.loading ? <div className="skeleton" style={{ height: 220 }} /> : <Empty title="No events" />
            ) : (
              <EventTypesChart data={eventTypes.data} />
            )}
          </div>
        </Card>
      </div>

      {/* --------------------- Live feed + top attackers -------------------- */}
      <div className="grid split-2-1 mt">
        <Card>
          <CardHead
            title="Live Attack Feed"
            icon={<Activity size={16} />}
            sub="latest honeypot events"
            actions={<span className="chip on"><span className="pulse-dot" /> streaming</span>}
          />
          {(recentEvents.data?.length ?? 0) === 0 ? (
            recentEvents.loading ? <div className="card-pad"><Loader label="Listening…" /></div> : <Empty icon={<Zap size={22} />} title="Feed is quiet" message="No recent events." />
          ) : (
            <div className="feed">
              {recentEvents.data.map((e) => (
                <FeedItem key={e.id} event={e} />
              ))}
            </div>
          )}
        </Card>

        <Card>
          <CardHead
            title="Top Attackers"
            icon={<Crosshair size={16} />}
            actions={<Link to="/attackers" className="btn ghost sm">All <ArrowRight size={14} /></Link>}
          />
          {(topAttackers.data?.length ?? 0) === 0 ? (
            <Empty icon={<Crosshair size={22} />} title="No attackers yet" />
          ) : (
            <div>
              {topAttackers.data.map((a) => (
                <AttackerRow key={a.id} attacker={a} onOpen={() => nav("/attackers")} />
              ))}
            </div>
          )}
        </Card>
      </div>

      {/* -------------------- Recent sessions + alerts ------------------- */}
      <div className="grid cols-2 mt">
        <Card>
          <CardHead
            title="Recent Sessions"
            icon={<Terminal size={16} />}
            actions={<Link to="/sessions" className="btn ghost sm">All <ArrowRight size={14} /></Link>}
          />
          {(recentSessions.data?.length ?? 0) === 0 ? (
            <Empty icon={<Terminal size={22} />} title="No sessions" />
          ) : (
            <div className="table-wrap">
              <table className="data">
                <thead>
                  <tr><th>Session</th><th>Attacker</th><th>Stages</th><th>Risk</th></tr>
                </thead>
                <tbody>
                  {recentSessions.data.map((ss) => (
                    <tr key={ss.id} className="clickable" onClick={() => nav(`/sessions/${ss.id}`)}>
                      <td className="strong">
                        #{ss.id}
                        {ss.multiStage && <span className="badge sev-critical" style={{ marginLeft: 7 }}>MULTI</span>}
                        <div className="muted" style={{ fontSize: 11, fontWeight: 400 }}>{ss.attackType}</div>
                      </td>
                      <td><span className="ip">{ss.attackerIp}</span></td>
                      <td><StagePills stages={ss.stages} /></td>
                      <td><RiskMeter score={ss.riskScore} /></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </Card>

        <Card>
          <CardHead
            title="Recent Alerts"
            icon={<ShieldAlert size={16} />}
            actions={<Link to="/alerts" className="btn ghost sm">All <ArrowRight size={14} /></Link>}
          />
          {(alerts.data?.length ?? 0) === 0 ? (
            <Empty icon={<ShieldAlert size={22} />} title="No alerts" message="The detection engine raises alerts on multi-stage or successful-login activity." />
          ) : (
            <div>
              {alerts.data.slice(0, 4).map((a) => (
                <AlertRow key={a.id} alert={a} onOpen={() => nav("/alerts")} />
              ))}
            </div>
          )}
        </Card>
      </div>
    </>
  );
}

export default function Dashboard() {
  return <Dash />;
}
