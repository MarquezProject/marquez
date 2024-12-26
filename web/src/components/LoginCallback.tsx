import React, { useEffect } from 'react'
import { Box, CircularProgress } from '@mui/material'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

const LoginCallback = () => {
  const navigate = useNavigate()
  const { oktaAuth } = useAuth()

  useEffect(() => {
    const handleCallback = async () => {
      try {
        await oktaAuth.handleRedirect()
        navigate('/', { replace: true })
      } catch (error) {
        console.error('Error handling redirect:', error)
        navigate('/login')
      }
    }
    handleCallback()
  }, [oktaAuth, navigate])

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