import React from 'react';
import PropTypes from 'prop-types';
import { Route, Navigate, useLocation } from 'react-router-dom';

// Mock function to check SAML authentication status
const isAuthenticated = () => {
    // Check if a valid session or token exists
    const token = sessionStorage.getItem('samlToken');
    if (!token) {
      return false;
    }
  
    // Checks if token is expired
    const tokenExpiration = sessionStorage.getItem('samlTokenExpiration');
    return !(tokenExpiration && new Date(tokenExpiration) < new Date());
};

interface ProtectedRouteProps {
  component: React.ComponentType<any>;
  [key: string]: any;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ component: Component, ...rest }) => {
  const location = useLocation();
  return (
    <Route
      {...rest}
      element={
        isAuthenticated() ? (
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

ProtectedRoute.propTypes = {
  component: PropTypes.func.isRequired,
  location: PropTypes.object.isRequired,
};

export default ProtectedRoute;