import { Menu, RefreshCw, Wifi, WifiOff } from "lucide-react";
import { useApp } from "../store/AppContext";
import { timeAgo } from "../utils/format";

function Navbar({ title, subtitle, onMenu }) {
  const { online, sync, syncing, refreshAll, lastEventAt } = useApp();

  return (
    <header className="navbar">
      <button className="hamburger" onClick={onMenu} aria-label="Toggle navigation">
        <Menu size={20} />
      </button>

      <div className="navbar-title">
        <h2>{title}</h2>
        {subtitle && <p>{subtitle}</p>}
      </div>

      <div className="navbar-spacer" />

      <div className={`status-pill ${online ? "" : "offline"}`} title={online ? "API reachable" : "API unreachable"}>
        {online ? <Wifi size={15} /> : <WifiOff size={15} />}
        <span className="text">{online ? "Live" : "Offline"}</span>
        {online && lastEventAt && (
          <span className="text" style={{ opacity: 0.7 }}>· {timeAgo(lastEventAt)}</span>
        )}
      </div>

      <button className="btn ghost sm" onClick={refreshAll} title="Refresh views">
        <RefreshCw size={15} />
      </button>

      <button className="btn primary sm" onClick={sync} disabled={syncing} title="Pull latest honeypot logs">
        <RefreshCw size={15} className={syncing ? "spin" : ""} />
        <span className="hide-sm">{syncing ? "Syncing…" : "Sync"}</span>
      </button>
    </header>
  );
}

export default Navbar;
