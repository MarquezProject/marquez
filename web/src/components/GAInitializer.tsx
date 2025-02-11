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