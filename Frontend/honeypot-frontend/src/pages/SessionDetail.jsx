import { useParams, useNavigate } from "react-router-dom";
import { ArrowLeft, GitBranch, ShieldAlert, ListChecks } from "lucide-react";
import { useApp } from "../store/AppContext";
import { useApi } from "../hooks/useApi";
import { api } from "../services/api";
import { Card, CardHead, Loader, ErrorState, Empty, RiskMeter, SeverityBadge, StatusChip } from "../components/ui";
import { KillChain, AlertRow, StagePills } from "../components/blocks";
import { fmtDateTime, eventLabel, fmtTime } from "../utils/format";

function Meta({ label, children }) {
  return (
    <div className="meta-item">
      <div className="k">{label}</div>
      <div className="v" style={{ fontSize: 14 }}>{children}</div>
    </div>
  );
}

export default function SessionDetail() {
  const { id } = useParams();
  const nav = useNavigate();
  const { refreshToken } = useApp();
  const { data, loading, error, refetch } = useApi(() => api.sessionChain(id), [id, refreshToken]);

  if (loading && !data) return <Loader label="Reconstructing kill chain…" />;
  if (error && !data) return <ErrorState message={error.message} onRetry={refetch} />;
  if (!data?.session) return <Empty title="Session not found" message={`No session with id ${id}.`} />;

  const { session: s, chain = [], events = [], alerts = [] } = data;

  return (
    <>
      <div className="back-link" onClick={() => nav("/sessions")}>
        <ArrowLeft size={15} /> Back to sessions
      </div>

      <div className="page-head">
        <div>
          <h2 className="row-flex" style={{ gap: 10, flexWrap: "wrap" }}>
            Session #{s.id}
            {s.multiStage && <span className="badge sev-critical">MULTI-STAGE</span>}
            <SeverityBadge severity={s.maxSeverity} />
            <StatusChip status={s.status} />
          </h2>
          <p>{s.attackType || "Unclassified activity"} — correlated across {chain.length} kill-chain phase{chain.length === 1 ? "" : "s"}.</p>
        </div>
        <div className="row-flex" style={{ gap: 10 }}>
          <RiskMeter score={s.riskScore} />
        </div>
      </div>

      <Card className="card-pad" style={{ marginBottom: 18 }}>
        <div className="meta-grid">
          <Meta label="Attacker IP"><span className="ip">{s.attackerIp}</span></Meta>
          <Meta label="Origin">{s.attackerCountry || "—"}</Meta>
          <Meta label="Protocol">{(s.protocol || "—").toUpperCase()}</Meta>
          <Meta label="Sensor session"><span className="mono">{s.sensorSessionId}</span></Meta>
          <Meta label="Honeypot">{s.honeypotName || "—"}</Meta>
          <Meta label="Events">{s.eventCount ?? events.length}</Meta>
          <Meta label="Started">{fmtDateTime(s.startTime)}</Meta>
          <Meta label="Ended">{s.endTime ? fmtDateTime(s.endTime) : "in progress"}</Meta>
        </div>
        <div className="row-flex mt" style={{ gap: 8 }}>
          <span className="muted" style={{ fontSize: 12 }}>Observed stages:</span>
          <StagePills stages={s.stages} />
        </div>
        {s.commands && (
          <div className="mt">
            <div className="section-title"><ListChecks size={14} /> Commands executed</div>
            <div className="row-flex wrap" style={{ gap: 7 }}>
              {s.commands.split("|").map((c, i) => c.trim() && <code className="cmd" key={i}>{c.trim()}</code>)}
            </div>
          </div>
        )}
      </Card>

      <div className="grid split-2-1">
        <Card>
          <CardHead title="Attack Kill Chain" icon={<GitBranch size={16} />} sub="staged correlation of attacker behaviour" />
          <div className="card-pad">
            {chain.length === 0 ? <Empty title="No correlated events" /> : <KillChain chain={chain} />}
          </div>
        </Card>

        <div className="grid" style={{ gap: 18, alignContent: "start" }}>
          <Card>
            <CardHead title="Alerts Raised" icon={<ShieldAlert size={16} />} sub={`${alerts.length}`} />
            {alerts.length === 0 ? (
              <Empty icon={<ShieldAlert size={20} />} title="No alerts" message="This session did not trip a detection rule." />
            ) : (
              <div>
                {alerts.map((a) => <AlertRow key={a.id} alert={a} />)}
              </div>
            )}
          </Card>
        </div>
      </div>

      <Card style={{ marginTop: 18 }}>
        <CardHead title="Full Event Log" icon={<ListChecks size={16} />} sub={`${events.length} raw events`} />
        {events.length === 0 ? (
          <Empty title="No events" />
        ) : (
          <div className="table-wrap">
            <table className="data">
              <thead>
                <tr><th>Time</th><th>Event</th><th>Stage</th><th>Sev</th><th>Detail</th></tr>
              </thead>
              <tbody>
                {events.map((e) => (
                  <tr key={e.id}>
                    <td className="mono nowrap">{fmtTime(e.timestamp)}</td>
                    <td className="strong">{eventLabel(e.eventType)}</td>
                    <td>{e.stage?.replace(/_/g, " ") || "—"}</td>
                    <td><SeverityBadge severity={e.severity} showDot={false} /></td>
                    <td className="truncate" style={{ maxWidth: 420 }} title={e.eventData}>
                      {e.command ? <code className="cmd">{e.command}</code> : <span className="muted">{e.eventData}</span>}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </>
  );
}
