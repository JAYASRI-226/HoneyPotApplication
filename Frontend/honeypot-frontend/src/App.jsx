import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AppProvider } from "./store/AppContext";
import Layout from "./components/Layout";
import Dashboard from "./pages/Dashboard";
import Honeypots from "./pages/Honeypots";
import Attackers from "./pages/Attackers";
import Sessions from "./pages/Sessions";
import SessionDetail from "./pages/SessionDetail";
import Events from "./pages/Events";
import Alerts from "./pages/Alerts";

function App() {
  return (
    <AppProvider>
      <BrowserRouter>
        <Routes>
          <Route element={<Layout />}>
            <Route path="/" element={<Dashboard />} />
            <Route path="/honeypots" element={<Honeypots />} />
            <Route path="/attackers" element={<Attackers />} />
            <Route path="/sessions" element={<Sessions />} />
            <Route path="/sessions/:id" element={<SessionDetail />} />
            <Route path="/events" element={<Events />} />
            <Route path="/alerts" element={<Alerts />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </AppProvider>
  );
}

export default App;
