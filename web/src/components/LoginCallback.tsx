
import React, { useEffect } from 'react'
import { Box, CircularProgress } from '@mui/material'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

const LoginCallback = () => {
  const navigate = useNavigate()
  const { oktaAuth } = useAuth()

  useEffect(() => {
    let isMounted = true

    const handleCallback = async () => {
      try {
        console.log('Handling redirect...')
        await oktaAuth.handleRedirect()
        console.log('Redirect handled successfully')
        if (isMounted) {
          navigate('/', { replace: true })
        }
      } catch (error) {
        console.error('Error handling redirect:', error)
        if (isMounted) {
          navigate('/login')
        }
      }
    }

    handleCallback()

    return () => {
      isMounted = false
    }
  }, [oktaAuth, navigate])

  return (
    <Box display='flex' justifyContent='center' alignItems='center' minHeight='100vh'>
      <CircularProgress />
    </Box>
  )
}

export default LoginCallback