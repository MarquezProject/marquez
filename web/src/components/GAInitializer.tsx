/**
 * GAInitializer Component
 *
 * This component initializes Google Analytics 4 (GA4) and tracks page views.
 * It sets the GA4 tracking ID based on the environment (staging or production)
 * and tracks page views whenever the location changes.
 *
 * Author: Jonathan Moraes
 * Created: 2025-02-19
 * Reason: To integrate Google Analytics 4 for tracking user interactions and page views in the application.
 */

import React, { useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { initializeGA, trackPageView } from './ga4';

const GAInitializer: React.FC = () => {
  const location = useLocation();

  useEffect(() => {
    initializeGA();
  }, []);

  useEffect(() => {
    trackPageView();
  }, [location]);

  return null;
};

export default GAInitializer;