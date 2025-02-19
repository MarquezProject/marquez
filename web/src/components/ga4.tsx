import ReactGA from 'react-ga4';

function decodeBase64(str: string) {
  return window.atob(str);
}

const initializeGA = () => {
  const isStaging = window.location.origin.includes('staging');
  const gaId = isStaging 
    ? decodeBase64('Ry1KNkc1QlYzRVY1')
    : decodeBase64('Ry1RRjJSSFgzSFJK')
  ReactGA.initialize(gaId);
};
  
  const trackPageView = () => {
    ReactGA.send({ hitType: 'pageview', page: window.location.pathname });
  };
  
  const trackEvent = (category: string, action: string, label?: string) => {
    ReactGA.event({ category, action, label });
  };

  export { initializeGA, trackPageView, trackEvent };