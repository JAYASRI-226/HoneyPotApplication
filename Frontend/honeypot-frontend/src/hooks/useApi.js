import { useCallback, useEffect, useRef, useState } from "react";

/**
 * Small data-fetching hook with loading/error state, manual refetch and
 * optional polling (for the "live" dashboard). Pass a stable `deps` array.
 */
export function useApi(fetcher, deps = [], { poll = 0, initialData = null } = {}) {
  const [data, setData] = useState(initialData);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const mounted = useRef(true);
  const fetcherRef = useRef(fetcher);
  fetcherRef.current = fetcher;

  const load = useCallback(async () => {
    try {
      const result = await fetcherRef.current();
      if (mounted.current) {
        setData(result);
        setError(null);
      }
    } catch (e) {
      if (mounted.current) setError(e);
    } finally {
      if (mounted.current) setLoading(false);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps);

  useEffect(() => {
    mounted.current = true;
    setLoading(true);
    load();
    let id;
    if (poll > 0) id = setInterval(load, poll);
    return () => {
      mounted.current = false;
      if (id) clearInterval(id);
    };
  }, [load, poll]);

  return { data, loading, error, refetch: load, setData };
}

export default useApi;
