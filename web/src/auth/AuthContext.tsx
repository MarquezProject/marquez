import { AuthState, OktaAuth } from '@okta/okta-auth-js'
import React, { createContext, useContext, useEffect, useState } from 'react'

export const oktaAuth = new OktaAuth({
  issuer: 'https://nubank.okta.com/oauth2/default',
  clientId: '0oa20d6n6jb6nG5Mn0h8',
  redirectUri: window.location.origin + '/login/callback',
  pkce: true,
  tokenManager: {
    storage: 'localStorage',
  },
})

interface AuthContextType {
  isAuthenticated: boolean
  user: any
  loading: boolean
  login: () => Promise<void>
  logout: () => Promise<void>
  oktaAuth: OktaAuth
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [user, setUser] = useState<any>(null)
  const [loading, setLoading] = useState(true)

  const login = async () => {
    await oktaAuth.signInWithRedirect()
  }

  const logout = async () => {
    await oktaAuth.signOut()
  }

  useEffect(() => {
    // Define a synchronous callback for authStateManager
    const handleAuthState = (authState: AuthState): void => {
      // Wrap detailed async logic in an immediately-invoked async function
      ;(async () => {
        const loggedIn = !!authState?.isAuthenticated
        setIsAuthenticated(loggedIn)
        if (loggedIn) {
          const userInfo = await oktaAuth.getUser()
          setUser(userInfo)
        } else {
          setUser(null)
        }
        setLoading(false)
      })()
    }

    // Subscribe with the synchronous callback
    oktaAuth.authStateManager.subscribe(handleAuthState)

    // Force Okta to check tokens immediately
    oktaAuth.authStateManager.updateAuthState()

    // Cleanup on unmount
    return () => {
      oktaAuth.authStateManager.unsubscribe(handleAuthState)
    }
  }, [])

  return (
    <AuthContext.Provider value={{ isAuthenticated, user, loading, login, logout, oktaAuth }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used within an AuthProvider')
  return context
}
