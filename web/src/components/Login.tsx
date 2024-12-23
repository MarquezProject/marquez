import React from 'react'
import { Box, Button, Container, Typography } from '@mui/material'
import { useAuth } from '../auth/AuthContext'

const Login = () => {
  const { login } = useAuth()

  return (
    <Container>
      <Box
        display='flex'
        flexDirection='column'
        alignItems='center'
        justifyContent='center'
        minHeight='100vh'
      >
        <Typography variant='h4' gutterBottom>
          Welcome to Nu Data Lineage
        </Typography>
        <Button variant='contained' color='primary' onClick={login}>
          Sign in with Okta
        </Button>
      </Box>
    </Container>
  )
}

export default Login