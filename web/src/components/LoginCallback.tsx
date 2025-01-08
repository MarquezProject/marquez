import React, { useEffect } from 'react'
import { Box, CircularProgress } from '@mui/material'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

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
          navigate('/', { replace: true })
        } else {
          navigate('/login')
        }
      } catch (error) {
        console.error('Error handling redirect:', error)
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