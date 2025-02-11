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