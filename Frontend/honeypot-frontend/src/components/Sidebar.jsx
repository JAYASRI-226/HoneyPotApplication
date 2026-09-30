import { NavLink } from "react-router-dom";
import {
  LayoutDashboard,
  Radar,
  Crosshair,
  Terminal,
  Zap,
  ShieldAlert,
} from "lucide-react";
import { useApp } from "../store/AppContext";

const LINKS = [
  { to: "/", label: "Dashboard", icon: LayoutDashboard, end: true },
  { to: "/honeypots", label: "Honeypots", icon: Radar },
  { to: "/attackers", label: "Attackers", icon: Crosshair },
  { to: "/sessions", label: "Sessions", icon: Terminal },
  { to: "/events", label: "Events", icon: Zap },
  { to: "/alerts", label: "Alerts", icon: ShieldAlert, badge: "alerts" },
];

function Sidebar({ open, onNavigate }) {
  const { summary, online } = useApp();
  const alertCount = summary?.newAlerts ?? summary?.alerts ?? 0;

  return (
    <aside className={`sidebar ${open ? "open" : ""}`}>
      <div className="brand">
        <div className="brand-logo">
          <Radar size={22} strokeWidth={2.2} />
        </div>
        <div className="brand-text">
          <h1>HoneyPot Intel</h1>
          <span>Multi-stage detection</span>
        </div>
      </div>

      <nav className="nav">
        <div className="nav-label">Operations</div>
        {LINKS.map(({ to, label, icon: Icon, end, badge }) => {
          const count = badge === "alerts" ? alertCount : 0;
          return (
            <NavLink
              key={to}
              to={to}
              end={end}
              onClick={onNavigate}
              className={({ isActive }) => `nav-link ${isActive ? "active" : ""}`}
            >
              <span className="icon">
                <Icon size={18} />
              </span>
              {label}
              {count > 0 && <span className="nav-badge">{count > 99 ? "99+" : count}</span>}
            </NavLink>
          );
        })}
      </nav>

      <div className="sidebar-footer">
        <span className="pulse-dot" style={online ? undefined : { background: "var(--sev-critical)", animation: "none" }} />
        {online ? "Backend online" : "Backend offline"}
      </div>
    </aside>
  );
}

export default Sidebar;
