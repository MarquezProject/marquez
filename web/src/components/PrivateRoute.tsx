// src/components/PrivateRoute.tsx
import React from 'react';
import { Navigate } from 'react-router-dom';
import CircularProgress from '@mui/material/CircularProgress';
import { useAuth } from '../auth/AuthContext';

export const PrivateRoute: React.FC<{children: React.ReactNode}> = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return <CircularProgress />;
  }

  return isAuthenticated ? <>{children}</> : <Navigate to="/login" />;
};