import { useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { ShieldAlert, Check, CheckCheck } from "lucide-react";
import { useApp } from "../store/AppContext";
import { useApi } from "../hooks/useApi";
import { api } from "../services/api";
import { Card, Loader, ErrorState, Empty } from "../components/ui";
import { AlertRow } from "../components/blocks";
import { SEV_ORDER } from "../utils/format";

const STATUSES = ["NEW", "ACKNOWLEDGED", "RESOLVED"];

export default function Alerts() {
  const nav = useNavigate();
  const { refreshToken } = useApp();
  const { data, loading, error, refetch, setData } = useApi(() => api.alerts(), [refreshToken], { poll: 20000 });
  const [sev, setSev] = useState("ALL");
  const [status, setStatus] = useState("ALL");

  const rows = useMemo(() => {
    let list = data || [];
    if (sev !== "ALL") list = list.filter((a) => a.severity === sev);
    if (status !== "ALL") list = list.filter((a) => a.status === status);
    return list;
  }, [data, sev, status]);

  const counts = useMemo(() => {
    const list = data || [];
    return {
      total: list.length,
      new: list.filter((a) => a.status === "NEW").length,
      critical: list.filter((a) => a.severity === "CRITICAL").length,
    };
  }, [data]);

  async function changeStatus(alert, next) {
    const prev = data;
    setData((list) => (list || []).map((a) => (a.id === alert.id ? { ...a, status: next } : a)));
    try {
      await api.updateAlert(alert.id, { status: next });
    } catch {
      setData(prev);
    }
  }

  return (
    <>
      <div className="page-head">
        <div>
          <h2>Detection Alerts</h2>
          <p>
            {counts.total} total · {counts.new} new · {counts.critical} critical — raised automatically by the correlation engine.
          </p>
        </div>
        <div className="toolbar">
          <select className="select" value={sev} onChange={(e) => setSev(e.target.value)}>
            <option value="ALL">All severities</option>
            {[...SEV_ORDER].reverse().map((s) => <option key={s} value={s}>{s}</option>)}
          </select>
          <div className="seg">
            <button className={status === "ALL" ? "active" : ""} onClick={() => setStatus("ALL")}>All</button>
            {STATUSES.map((s) => (
              <button key={s} className={status === s ? "active" : ""} onClick={() => setStatus(s)}>
                {s[0] + s.slice(1).toLowerCase()}
              </button>
            ))}
          </div>
        </div>
      </div>

      <Card>
        {loading && !data ? (
          <Loader label="Loading alerts…" />
        ) : error && !data ? (
          <ErrorState message={error.message} onRetry={refetch} />
        ) : rows.length === 0 ? (
          <Empty icon={<ShieldAlert size={22} />} title="No alerts" message="Alerts are generated on multi-stage attacks, successful logins, and brute-force bursts." />
        ) : (
          <div>
            {rows.map((a) => (
              <AlertRow
                key={a.id}
                alert={a}
                onOpen={a.sessionId ? () => nav(`/sessions/${a.sessionId}`) : undefined}
                actions={
                  <>
                    {a.status !== "ACKNOWLEDGED" && a.status !== "RESOLVED" && (
                      <button className="btn ghost sm" onClick={() => changeStatus(a, "ACKNOWLEDGED")} title="Acknowledge">
                        <Check size={14} /> Ack
                      </button>
                    )}
                    {a.status !== "RESOLVED" && (
                      <button className="btn sm" onClick={() => changeStatus(a, "RESOLVED")} title="Resolve">
                        <CheckCheck size={14} /> Resolve
                      </button>
                    )}
                  </>
                }
              />
            ))}
          </div>
        )}
      </Card>
    </>
  );
}
