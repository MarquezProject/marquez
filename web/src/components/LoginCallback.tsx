/**
 * LoginCallback Component
 *
 * This component handles the callback from Okta after a user has authenticated.
 * It processes the authentication response, sets the user information, logs the user info,
 * and redirects the user to the appropriate page.
 *
 * Author: Jonathan Moraes
 * Created: 2025-02-19
 * Reason: To handle the Okta authentication callback and manage user session state.
 */

import React, { useEffect } from 'react'
import { Box, CircularProgress } from '@mui/material'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { trackEvent } from './ga4'

const LoginCallback = () => {
  const navigate = useNavigate()
  const { oktaAuth, setUser } = useAuth()

  useEffect(() => {
    const handleCallback = async () => {
      try {
        await oktaAuth.handleRedirect()
        const isAuthenticated = await oktaAuth.isAuthenticated()
        if (isAuthenticated) {
          const userInfo = await oktaAuth.getUser()
          setUser(userInfo)
          await fetch('/api/loguserinfo', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(userInfo),
          });
          trackEvent('LoginCallback', 'Login Successful', userInfo.email);
          navigate('/', { replace: true })
        } else {
          navigate('/login')
        }
      } catch (error) {
        console.error('Error handling redirect:', error)
        trackEvent('LoginCallback', 'Login Failed', error.message)
        navigate('/login')
      }
    }
    handleCallback()
  }, [oktaAuth, setUser, navigate])

  return (
    <Box
      display="flex"
      justifyContent="center"
      alignItems="center"
      minHeight="100vh"
    >
      <CircularProgress />
    </Box>
  )
}

export default LoginCallback