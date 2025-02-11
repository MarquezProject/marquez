import { Box, Button, Container } from '@mui/material'
import { Helmet } from 'react-helmet-async'
import { Navigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import MqText from './core/text/MqText'
import React, { useEffect } from 'react'
import nu_logo from './sidenav/logoNu.svg'
import { trackEvent } from './ga4'

const Login = () => {
  const { isAuthenticated, login } = useAuth()

  useEffect(() => {
    document.title = 'Nu Data Lineage'
    trackEvent('Login', 'View Login Page')
  }, [])

  // If already authenticated, redirect away from the login page
  if (isAuthenticated) {
    return <Navigate to='/' replace />
  }

  const handleLoginClick = () => {
    login();
    trackEvent('Login', 'Click Sign In with Okta')
  }

  return (
    <Container>
      <Box
        display='flex'
        flexDirection='column'
        alignItems='center'
        justifyContent='center'
        minHeight='100vh'
      >
        <Box
          display='flex'
          flexDirection='column'
          alignItems='center'
          justifyContent='center'
          bgcolor='#2E3339' // Cor cinza
          p={2}
          mt={2}
          borderRadius={2}
        >
          <img
            src={nu_logo}
            height={230}
            alt='Nu Logo'
            style={{ filter: 'invert(1)', marginTop: '10px' }}
          />
          <Helmet>
            <title>Nu Data Lineage</title>
          </Helmet>
          <MqText sx={{ fontSize: '24px', marginBottom: '30px' }}>
            Welcome to Nu Data Lineage
          </MqText>

          <Button
            sx={{ fontSize: '18px', marginBottom: '20px' }}
            variant='contained'
            color='primary'
            onClick={handleLoginClick}
          >
            Sign in with Okta
          </Button>
        </Box>
      </Box>
    </Container>
  )
}

export default Login
