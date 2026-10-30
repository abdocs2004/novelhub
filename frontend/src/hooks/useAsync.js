import { useCallback, useEffect, useState } from 'react'
import { errorCode, errorMessage } from '../api/client.js'

/**
 * Runs an async function on mount (and whenever `deps` change) and tracks loading / error state.
 * Returns { data, loading, error, errorCode, reload, setData }.
 */
export function useAsync(fn, deps = []) {
  const [state, setState] = useState({ data: null, loading: true, error: null, code: null })

  const run = useCallback(() => {
    let cancelled = false
    setState((s) => ({ ...s, loading: true, error: null, code: null }))
    fn()
      .then((data) => !cancelled && setState({ data, loading: false, error: null, code: null }))
      .catch((err) => !cancelled && setState({ data: null, loading: false, error: errorMessage(err), code: errorCode(err) }))
    return () => {
      cancelled = true
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps)

  useEffect(() => run(), [run])

  const setData = (data) => setState((s) => ({ ...s, data }))

  return { data: state.data, loading: state.loading, error: state.error, errorCode: state.code, reload: run, setData }
}
