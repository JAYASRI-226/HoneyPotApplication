import { useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { Zap, Search, ExternalLink } from "lucide-react";
import { useApp } from "../store/AppContext";
import { useApi } from "../hooks/useApi";
import { api } from "../services/api";
import { Card, Loader, ErrorState, Empty, SeverityBadge, StageBadge } from "../components/ui";
import { fmtDateTime, eventLabel, SEV_ORDER, STAGE_ORDER } from "../utils/format";

const MAX_ROWS = 400;

export default function Events() {
  const { refreshToken } = useApp();
  const { data, loading, error, refetch } = useApi(() => api.events(), [refreshToken]);
  const [q, setQ] = useState("");
  const [sev, setSev] = useState("ALL");
  const [stage, setStage] = useState("ALL");

  const rows = useMemo(() => {
    let list = data || [];
    if (sev !== "ALL") list = list.filter((e) => e.severity === sev);
    if (stage !== "ALL") list = list.filter((e) => e.stage === stage);
    const needle = q.trim().toLowerCase();
    if (needle) {
      list = list.filter((e) =>
        [e.sourceIp, e.command, e.eventData, e.eventType, e.destinationIp].some((v) =>
          String(v || "").toLowerCase().includes(needle)
        )
      );
    }
    return list;
  }, [data, q, sev, stage]);

  const shown = rows.slice(0, MAX_ROWS);
  const total = data?.length ?? 0;

  return (
    <>
      <div className="page-head">
        <div>
          <h2>Event Log</h2>
          <p>
            {rows.length} of {total} events{rows.length > MAX_ROWS ? ` · showing first ${MAX_ROWS}` : ""}
          </p>
        </div>
        <div className="toolbar">
          <div className="row-flex" style={{ position: "relative" }}>
            <Search size={15} style={{ position: "absolute", left: 11, color: "var(--text-mute)" }} />
            <input className="input" style={{ paddingLeft: 34 }} placeholder="Search IP, command, event…" value={q} onChange={(e) => setQ(e.target.value)} />
          </div>
          <select className="select" value={stage} onChange={(e) => setStage(e.target.value)}>
            <option value="ALL">All stages</option>
            <option value="UNKNOWN">Unclassified</option>
            {STAGE_ORDER.map((s) => <option key={s} value={s}>{s.replace(/_/g, " ")}</option>)}
          </select>
          <select className="select" value={sev} onChange={(e) => setSev(e.target.value)}>
            <option value="ALL">All severities</option>
            {SEV_ORDER.map((s) => <option key={s} value={s}>{s}</option>)}
          </select>
        </div>
      </div>

      <Card>
        {loading && !data ? (
          <Loader label="Loading events…" />
        ) : error && !data ? (
          <ErrorState message={error.message} onRetry={refetch} />
        ) : shown.length === 0 ? (
          <Empty icon={<Zap size={22} />} title="No events match" message={total ? "Try clearing your filters." : "Events appear once honeypot activity is ingested."} />
        ) : (
          <div className="table-wrap">
            <table className="data">
              <thead>
                <tr>
                  <th>Timestamp</th>
                  <th>Event</th>
                  <th>Stage</th>
                  <th>Sev</th>
                  <th>Source</th>
                  <th>Detail</th>
                  <th>Session</th>
                </tr>
              </thead>
              <tbody>
                {shown.map((e) => (
                  <tr key={e.id}>
                    <td className="mono nowrap">{fmtDateTime(e.timestamp)}</td>
                    <td className="strong">{eventLabel(e.eventType)}</td>
                    <td><StageBadge stage={e.stage} /></td>
                    <td><SeverityBadge severity={e.severity} showDot={false} /></td>
                    <td><span className="ip">{e.sourceIp}</span></td>
                    <td className="truncate" style={{ maxWidth: 360 }} title={e.eventData}>
                      {e.command ? <code className="cmd">{e.command}</code> : <span className="muted">{e.eventData}</span>}
                    </td>
                    <td>
                      {e.sessionId ? (
                        <Link to={`/sessions/${e.sessionId}`} className="btn ghost sm" onClick={(ev) => ev.stopPropagation()}>
                          #{e.sessionId} <ExternalLink size={12} />
                        </Link>
                      ) : "—"}
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
