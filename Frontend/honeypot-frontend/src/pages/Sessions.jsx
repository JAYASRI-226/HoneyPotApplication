import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Terminal, Search } from "lucide-react";
import { useApp } from "../store/AppContext";
import { useApi } from "../hooks/useApi";
import { api } from "../services/api";
import { Card, Loader, ErrorState, Empty, RiskMeter, SeverityBadge, StatusChip } from "../components/ui";
import { StagePills } from "../components/blocks";
import { fmtDateTime } from "../utils/format";

export default function Sessions() {
  const nav = useNavigate();
  const { refreshToken } = useApp();
  const { data, loading, error, refetch } = useApi(() => api.sessions(), [refreshToken]);
  const [q, setQ] = useState("");
  const [filter, setFilter] = useState("all");

  const rows = useMemo(() => {
    let list = [...(data || [])];
    if (filter === "multi") list = list.filter((s) => s.multiStage);
    if (filter === "critical") list = list.filter((s) => s.maxSeverity === "CRITICAL");
    const needle = q.trim().toLowerCase();
    if (needle) {
      list = list.filter((s) =>
        [s.attackerIp, s.attackType, s.sensorSessionId, s.commands].some((v) =>
          String(v || "").toLowerCase().includes(needle)
        )
      );
    }
    return list;
  }, [data, q, filter]);

  const seg = [
    { id: "all", label: "All" },
    { id: "multi", label: "Multi-stage" },
    { id: "critical", label: "Critical" },
  ];

  return (
    <>
      <div className="page-head">
        <div>
          <h2>Attack Sessions</h2>
          <p>{rows.length} session{rows.length === 1 ? "" : "s"} — click any row for its full kill-chain breakdown.</p>
        </div>
        <div className="toolbar">
          <div className="row-flex" style={{ position: "relative" }}>
            <Search size={15} style={{ position: "absolute", left: 11, color: "var(--text-mute)" }} />
            <input className="input" style={{ paddingLeft: 34 }} placeholder="Search IP, command, type…" value={q} onChange={(e) => setQ(e.target.value)} />
          </div>
          <div className="seg">
            {seg.map((s) => (
              <button key={s.id} className={filter === s.id ? "active" : ""} onClick={() => setFilter(s.id)}>
                {s.label}
              </button>
            ))}
          </div>
        </div>
      </div>

      <Card>
        {loading && !data ? (
          <Loader label="Loading sessions…" />
        ) : error && !data ? (
          <ErrorState message={error.message} onRetry={refetch} />
        ) : rows.length === 0 ? (
          <Empty icon={<Terminal size={22} />} title="No sessions" message="Sessions appear once attackers connect to a honeypot." />
        ) : (
          <div className="table-wrap">
            <table className="data">
              <thead>
                <tr>
                  <th>Session</th>
                  <th>Attacker</th>
                  <th>Kill-chain stages</th>
                  <th>Events</th>
                  <th>Started</th>
                  <th>Severity</th>
                  <th>Risk</th>
                </tr>
              </thead>
              <tbody>
                {rows.map((s) => (
                  <tr key={s.id} className="clickable" onClick={() => nav(`/sessions/${s.id}`)}>
                    <td className="strong">
                      <div className="row-flex" style={{ gap: 7 }}>
                        #{s.id}
                        {s.multiStage && <span className="badge sev-critical">MULTI</span>}
                        <StatusChip status={s.status} />
                      </div>
                      <div className="muted" style={{ fontSize: 11.5, fontWeight: 400, marginTop: 2 }}>
                        {s.attackType || "Unclassified"} · {s.protocol}
                      </div>
                    </td>
                    <td><span className="ip">{s.attackerIp}</span></td>
                    <td><StagePills stages={s.stages} /></td>
                    <td>{s.eventCount ?? 0}</td>
                    <td className="nowrap">{fmtDateTime(s.startTime)}</td>
                    <td><SeverityBadge severity={s.maxSeverity} /></td>
                    <td><RiskMeter score={s.riskScore} /></td>
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
