import { createContext, useCallback, useContext, useState } from "react";
import { api } from "../services/api";
import { useApi } from "../hooks/useApi";

const AppCtx = createContext(null);

/**
 * Global app status: the dashboard summary (polled), backend online state, and a
 * `refreshToken` that pages watch so the navbar "Sync" button refreshes every view.
 */
export function AppProvider({ children }) {
  const [refreshToken, setRefreshToken] = useState(0);
  const [syncing, setSyncing] = useState(false);

  const summary = useApi(() => api.summary(), [], { poll: 20000 });
  const online = !summary.error;

  const refreshAll = useCallback(() => {
    summary.refetch();
    setRefreshToken((t) => t + 1);
  }, [summary]);

  const sync = useCallback(async () => {
    setSyncing(true);
    try {
      await api.runIngest();
    } catch {
      /* ingest may be disabled — still refresh the UI */
    }
    setSyncing(false);
    refreshAll();
  }, [refreshAll]);

  return (
    <AppCtx.Provider
      value={{
        summary: summary.data,
        summaryLoading: summary.loading,
        online,
        syncing,
        sync,
        refreshAll,
        refreshToken,
        lastEventAt: summary.data?.lastEventAt,
      }}
    >
      {children}
    </AppCtx.Provider>
  );
}

export function useApp() {
  const ctx = useContext(AppCtx);
  if (!ctx) throw new Error("useApp must be used inside <AppProvider>");
  return ctx;
}

export default AppCtx;
