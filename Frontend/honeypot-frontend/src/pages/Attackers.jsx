import { useMemo, useState } from "react";
import { Crosshair, Search, ArrowUpDown } from "lucide-react";
import { useApp } from "../store/AppContext";
import { useApi } from "../hooks/useApi";
import { api } from "../services/api";
import { Card, Loader, ErrorState, Empty, RiskMeter, SeverityBadge } from "../components/ui";
import { timeAgo, riskLabel } from "../utils/format";

const SORTS = {
  risk: (a, b) => (b.riskScore || 0) - (a.riskScore || 0),
  events: (a, b) => (b.eventCount || 0) - (a.eventCount || 0),
  sessions: (a, b) => (b.totalSessions || 0) - (a.totalSessions || 0),
  recent: (a, b) => new Date(b.lastSeen) - new Date(a.lastSeen),
};

export default function Attackers() {
  const { refreshToken } = useApp();
  const { data, loading, error, refetch } = useApi(() => api.attackers(), [refreshToken]);
  const [q, setQ] = useState("");
  const [sort, setSort] = useState("risk");

  const rows = useMemo(() => {
    const list = [...(data || [])];
    const needle = q.trim().toLowerCase();
    const filtered = needle
      ? list.filter((a) =>
          [a.ipAddress, a.country, a.city].some((v) => String(v || "").toLowerCase().includes(needle))
        )
      : list;
    return filtered.sort(SORTS[sort]);
  }, [data, q, sort]);

  return (
    <>
      <div className="page-head">
        <div>
          <h2>Attackers</h2>
          <p>{rows.length} threat actor{rows.length === 1 ? "" : "s"} profiled by the correlation engine.</p>
        </div>
        <div className="toolbar">
          <div className="row-flex" style={{ position: "relative" }}>
            <Search size={15} style={{ position: "absolute", left: 11, color: "var(--text-mute)" }} />
            <input className="input" style={{ paddingLeft: 34 }} placeholder="Search IP or location…" value={q} onChange={(e) => setQ(e.target.value)} />
          </div>
          <select className="select" value={sort} onChange={(e) => setSort(e.target.value)}>
            <option value="risk">Sort: Risk score</option>
            <option value="events">Sort: Events</option>
            <option value="sessions">Sort: Sessions</option>
            <option value="recent">Sort: Last seen</option>
          </select>
        </div>
      </div>

      <Card>
        {loading && !data ? (
          <Loader label="Profiling attackers…" />
        ) : error && !data ? (
          <ErrorState message={error.message} onRetry={refetch} />
        ) : rows.length === 0 ? (
          <Empty icon={<Crosshair size={22} />} title="No attackers found" message={q ? "No actor matches your search." : "Attackers appear once honeypot activity is ingested."} />
        ) : (
          <div className="table-wrap">
            <table className="data">
              <thead>
                <tr>
                  <th>Attacker</th>
                  <th>Location</th>
                  <th className="th-sortable" onClick={() => setSort("sessions")}>Sessions <ArrowUpDown size={11} /></th>
                  <th className="th-sortable" onClick={() => setSort("events")}>Events <ArrowUpDown size={11} /></th>
                  <th>Max severity</th>
                  <th className="th-sortable" onClick={() => setSort("recent")}>Last seen <ArrowUpDown size={11} /></th>
                  <th className="th-sortable" onClick={() => setSort("risk")}>Risk <ArrowUpDown size={11} /></th>
                </tr>
              </thead>
              <tbody>
                {rows.map((a) => (
                  <tr key={a.id}>
                    <td className="strong"><span className="ip">{a.ipAddress}</span></td>
                    <td>
                      {a.country || "—"}
                      {a.city ? <span className="muted"> · {a.city}</span> : null}
                    </td>
                    <td>{a.totalSessions ?? 0}</td>
                    <td>{a.eventCount ?? 0}</td>
                    <td><SeverityBadge severity={a.maxSeverity} /></td>
                    <td className="nowrap">{timeAgo(a.lastSeen)}</td>
                    <td>
                      <div className="row-flex" style={{ gap: 8 }}>
                        <RiskMeter score={a.riskScore} />
                        <span className="muted" style={{ fontSize: 11 }}>{riskLabel(a.riskScore)}</span>
                      </div>
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
