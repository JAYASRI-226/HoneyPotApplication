import { useEffect, useState } from "react";
import { Outlet, useLocation } from "react-router-dom";
import Sidebar from "./Sidebar";
import Navbar from "./Navbar";

const TITLES = {
  "/": { title: "Dashboard", subtitle: "Real-time honeypot intelligence" },
  "/honeypots": { title: "Honeypots", subtitle: "Decoy sensors & status" },
  "/attackers": { title: "Attackers", subtitle: "Threat actors ranked by risk" },
  "/sessions": { title: "Sessions", subtitle: "Attack sessions & kill chains" },
  "/events": { title: "Events", subtitle: "Raw honeypot event log" },
  "/alerts": { title: "Alerts", subtitle: "Detection alerts & triage" },
};

function Layout() {
  const [open, setOpen] = useState(false);
  const { pathname } = useLocation();

  useEffect(() => setOpen(false), [pathname]);

  const meta = pathname.startsWith("/sessions/")
    ? { title: "Session Detail", subtitle: "Multi-stage kill-chain analysis" }
    : TITLES[pathname] || { title: "HoneyPot Intel", subtitle: "Hybrid detection platform" };

  return (
    <div className="app">
      <Sidebar open={open} onNavigate={() => setOpen(false)} />
      <div className={`backdrop ${open ? "show" : ""}`} onClick={() => setOpen(false)} />
      <div className="main">
        <Navbar title={meta.title} subtitle={meta.subtitle} onMenu={() => setOpen((o) => !o)} />
        <div className="content">
          <Outlet />
        </div>
      </div>
    </div>
  );
}

export default Layout;
