import { useState } from "react";
import { Radar, Plus, X, MapPin, Clock } from "lucide-react";
import { useApp } from "../store/AppContext";
import { useApi } from "../hooks/useApi";
import { api } from "../services/api";
import { Card, CardHead, Loader, ErrorState, Empty, StatusChip } from "../components/ui";
import { fmtDateTime } from "../utils/format";

const TYPES = ["SSH", "TELNET", "HTTP", "WEB"];
const STATUSES = ["ACTIVE", "INACTIVE", "OFFLINE"];
const EMPTY = { name: "", type: "SSH", ipAddress: "", status: "ACTIVE" };

export default function Honeypots() {
  const { refreshToken } = useApp();
  const { data, loading, error, refetch } = useApi(() => api.honeypots(), [refreshToken]);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState(EMPTY);
  const [saving, setSaving] = useState(false);
  const [err, setErr] = useState(null);

  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.value }));

  async function submit(e) {
    e.preventDefault();
    setSaving(true);
    setErr(null);
    try {
      await api.createHoneypot(form);
      setForm(EMPTY);
      setShowForm(false);
      refetch();
    } catch (ex) {
      setErr(ex.message);
    } finally {
      setSaving(false);
    }
  }

  const list = data || [];
  const active = list.filter((h) => h.status === "ACTIVE").length;

  return (
    <>
      <div className="page-head">
        <div>
          <h2>Honeypot Sensors</h2>
          <p>
            {list.length} deployed · {active} active — decoy systems that attract and record attacker behaviour.
          </p>
        </div>
        <button className="btn primary" onClick={() => setShowForm((v) => !v)}>
          {showForm ? <X size={16} /> : <Plus size={16} />}
          {showForm ? "Cancel" : "Add sensor"}
        </button>
      </div>

      {showForm && (
        <Card className="card-pad" style={{ marginBottom: 18 }}>
          <form onSubmit={submit} className="grid cols-2" style={{ gap: 14 }}>
            <label className="field">
              <span>Name</span>
              <input className="input" value={form.name} onChange={set("name")} placeholder="Cowrie SSH Honeypot" required maxLength={100} />
            </label>
            <label className="field">
              <span>IP address</span>
              <input className="input mono" value={form.ipAddress} onChange={set("ipAddress")} placeholder="172.19.0.3" required />
            </label>
            <label className="field">
              <span>Type</span>
              <select className="select" value={form.type} onChange={set("type")}>
                {TYPES.map((t) => <option key={t} value={t}>{t}</option>)}
              </select>
            </label>
            <label className="field">
              <span>Status</span>
              <select className="select" value={form.status} onChange={set("status")}>
                {STATUSES.map((t) => <option key={t} value={t}>{t}</option>)}
              </select>
            </label>
            {err && <div className="form-err" style={{ gridColumn: "1 / -1" }}>{err}</div>}
            <div style={{ gridColumn: "1 / -1" }} className="row-flex">
              <button className="btn primary" type="submit" disabled={saving}>
                {saving ? "Registering…" : "Register sensor"}
              </button>
            </div>
          </form>
        </Card>
      )}

      {loading && !data ? (
        <Loader label="Loading sensors…" />
      ) : error && !data ? (
        <ErrorState message={error.message} onRetry={refetch} />
      ) : list.length === 0 ? (
        <Card><Empty icon={<Radar size={22} />} title="No honeypots registered" message="Add your first sensor to start tracking attacks." /></Card>
      ) : (
        <div className="grid cols-3">
          {list.map((h) => (
            <Card key={h.id} className="card-pad">
              <div className="row-flex" style={{ justifyContent: "space-between" }}>
                <div className="row-flex">
                  <div className="stat-icon" style={{ "--accent-color": "var(--accent)" }}>
                    <Radar size={20} />
                  </div>
                  <div>
                    <div style={{ fontWeight: 650, fontSize: 15 }}>{h.name}</div>
                    <div className="muted mono" style={{ fontSize: 11.5 }}>{h.type} sensor</div>
                  </div>
                </div>
                <StatusChip status={h.status} />
              </div>
              <div className="meta-grid mt">
                <div className="meta-item">
                  <div className="k"><MapPin size={11} /> IP address</div>
                  <div className="v ip" style={{ fontSize: 14 }}>{h.ipAddress}</div>
                </div>
                <div className="meta-item">
                  <div className="k"><Clock size={11} /> Deployed</div>
                  <div className="v" style={{ fontSize: 13 }}>{fmtDateTime(h.createdAt)}</div>
                </div>
              </div>
            </Card>
          ))}
        </div>
      )}
    </>
  );
}
