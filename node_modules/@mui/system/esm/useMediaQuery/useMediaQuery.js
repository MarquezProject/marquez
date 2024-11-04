'use client';

import * as React from 'react';
import useEnhancedEffect from '@mui/utils/useEnhancedEffect';
import { getThemeProps } from "../useThemeProps/index.js";
import useTheme from "../useThemeWithoutDefault/index.js";
// TODO React 17: Remove `useMediaQueryOld` once React 17 support is removed
function useMediaQueryOld(query, defaultMatches, matchMedia, ssrMatchMedia, noSsr) {
  const [match, setMatch] = React.useState(() => {
    if (noSsr && matchMedia) {
      return matchMedia(query).matches;
    }
    if (ssrMatchMedia) {
      return ssrMatchMedia(query).matches;
    }

    // Once the component is mounted, we rely on the
    // event listeners to return the correct matches value.
    return defaultMatches;
  });
  useEnhancedEffect(() => {
    if (!matchMedia) {
      return undefined;
    }
    const queryList = matchMedia(query);
    const updateMatch = () => {
      setMatch(queryList.matches);
    };
    updateMatch();
    queryList.addEventListener('change', updateMatch);
    return () => {
      queryList.removeEventListener('change', updateMatch);
    };
  }, [query, matchMedia]);
  return match;
}

// See https://github.com/mui/material-ui/issues/41190#issuecomment-2040873379 for why
const safeReact = {
  ...React
};
const maybeReactUseSyncExternalStore = safeReact.useSyncExternalStore;
function useMediaQueryNew(query, defaultMatches, matchMedia, ssrMatchMedia, noSsr) {
  const getDefaultSnapshot = React.useCallback(() => defaultMatches, [defaultMatches]);
  const getServerSnapshot = React.useMemo(() => {
    if (noSsr && matchMedia) {
      return () => matchMedia(query).matches;
    }
    if (ssrMatchMedia !== null) {
      const {
        matches
      } = ssrMatchMedia(query);
      return () => matches;
    }
    return getDefaultSnapshot;
  }, [getDefaultSnapshot, query, ssrMatchMedia, noSsr, matchMedia]);
  const [getSnapshot, subscribe] = React.useMemo(() => {
    if (matchMedia === null) {
      return [getDefaultSnapshot, () => () => {}];
    }
    const mediaQueryList = matchMedia(query);
    return [() => mediaQueryList.matches, notify => {
      mediaQueryList.addEventListener('change', notify);
      return () => {
        mediaQueryList.removeEventListener('change', notify);
      };
    }];
  }, [getDefaultSnapshot, matchMedia, query]);
  const match = maybeReactUseSyncExternalStore(subscribe, getSnapshot, getServerSnapshot);
  return match;
}
export default function useMediaQuery(queryInput, options = {}) {
  const theme = useTheme();
  // Wait for jsdom to support the match media feature.
  // All the browsers MUI support have this built-in.
  // This defensive check is here for simplicity.
  // Most of the time, the match media logic isn't central to people tests.
  const supportMatchMedia = typeof window !== 'undefined' && typeof window.matchMedia !== 'undefined';
  const {
    defaultMatches = false,
    matchMedia = supportMatchMedia ? window.matchMedia : null,
    ssrMatchMedia = null,
    noSsr = false
  } = getThemeProps({
    name: 'MuiUseMediaQuery',
    props: options,
    theme
  });
  if (process.env.NODE_ENV !== 'production') {
    if (typeof queryInput === 'function' && theme === null) {
      console.error(['MUI: The `query` argument provided is invalid.', 'You are providing a function without a theme in the context.', 'One of the parent elements needs to use a ThemeProvider.'].join('\n'));
    }
  }
  let query = typeof queryInput === 'function' ? queryInput(theme) : queryInput;
  query = query.replace(/^@media( ?)/m, '');
  const useMediaQueryImplementation = maybeReactUseSyncExternalStore !== undefined ? useMediaQueryNew : useMediaQueryOld;
  const match = useMediaQueryImplementation(query, defaultMatches, matchMedia, ssrMatchMedia, noSsr);
  if (process.env.NODE_ENV !== 'production') {
    // eslint-disable-next-line react-hooks/rules-of-hooks
    React.useDebugValue({
      query,
      match
    });
  }
  return match;
}