/**
 * PrivateRoute Component
 *
 * This component provides a wrapper for routes that require authentication.
 * It checks if the user is authenticated and either renders the child components
 * or redirects the user to the login page.
 *
 * Author: Jonathan Moraes
 * Created: 2025-02-19
 * Reason: To protect routes that require authentication and ensure only authenticated users can access them.
 */

import { useAuth } from '../auth/AuthContext'
import CircularProgress from '@mui/material/CircularProgress'
import React from 'react'
import { Navigate } from 'react-router-dom'

export const PrivateRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated, loading } = useAuth()

  if (loading) {
    return <CircularProgress />
  }

  return isAuthenticated ? <>{children}</> : <Navigate to='/login' />
}