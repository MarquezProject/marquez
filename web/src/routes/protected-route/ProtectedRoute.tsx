import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { Route, Navigate, useLocation } from 'react-router-dom';
import axios from 'axios';
import logging from '../config/logging';

// Mock function to check SAML authentication status
const isAuthenticated = async () => {
  try {
    const response = await axios.get('http://'+ `${process.env.MARQUEZ_WEB_AUTH_SERVER_HOST}:${process.env.MARQUEZ_WEB_AUTH_SERVER_PORT}`+ '/whoami', { withCredentials: true });
    logging.info(response.data.user, 'SAML');
    return response.data.user?.nameID;
  } catch (error) {
    logging.error(error, 'SAML');
    return false;
  }
};

interface ProtectedRouteProps {
  component: React.ComponentType<any>;
  [key: string]: any;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ component: Component, ...rest }) => {
  const location = useLocation();
  const [loading, setLoading] = useState(true);
  const [authenticated, setAuthenticated] = useState(false);

  useEffect(() => {
    const checkAuth = async () => {
      const auth = await isAuthenticated();
      setAuthenticated(auth);
      setLoading(false);
    };
    checkAuth();
  }, []);

  if (loading) {
    return <p>Loading...</p>;
  }

  return (
    <Route
      {...rest}
      element={
        authenticated ? (
          <Component />
        ) : (
          <Navigate to="/login" state={{ from: location }} />
        )
      }
    />
  );
};

ProtectedRoute.propTypes = {
  component: PropTypes.func.isRequired,
};

export default ProtectedRoute;