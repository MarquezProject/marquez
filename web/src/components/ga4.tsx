import ReactGA from 'react-ga4';

const initializeGA = () => {
    ReactGA.initialize('G-QF2RHX3HRJ');
  };
  
  const trackPageView = () => {
    ReactGA.send({ hitType: 'pageview', page: window.location.pathname });
  };
  
  const trackEvent = (category: string, action: string, label?: string) => {
    ReactGA.event({ category, action, label });
  };

  export { initializeGA, trackPageView, trackEvent };