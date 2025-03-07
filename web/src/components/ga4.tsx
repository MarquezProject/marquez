/**
 * GA4 Module
 *
 * This module provides functions to initialize Google Analytics 4 (GA4) and track page views and events.
 * It dynamically sets the GA4 tracking ID based on the environment (staging or production).
 *
 * Author: Jonathan Moraes
 * Created: 2025-02-19
 * Reason: To integrate Google Analytics 4 for tracking user interactions and page views in the application.
 */

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