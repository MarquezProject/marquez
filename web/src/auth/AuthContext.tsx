// src/auth/AuthContext.tsx
import React, { createContext, useContext, useState, useEffect } from 'react';
import { OktaAuth } from '@okta/okta-auth-js';

export const oktaAuth = new OktaAuth({
  issuer: 'https://nubank.okta.com/oauth2/default',
  clientId: '0oa20d6n6jb6nG5Mn0h8',
  redirectUri: 'http://localhost:3000/login/callback',
  scopes: ['openid', 'profile', 'email'],
  pkce: true,
  tokenManager: {
    storage: 'localStorage'
  }
});

interface AuthContextType {
  isAuthenticated: boolean;
  user: any;
  login: () => Promise<void>;
  logout: () => Promise<void>;
  oktaAuth: OktaAuth;
  loading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: React.ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const checkAuthentication = async () => {
      const authenticated = await oktaAuth.isAuthenticated();
      setIsAuthenticated(authenticated);
      if (authenticated) {
        const userInfo = await oktaAuth.getUser();
        setUser(userInfo);
      }
      setLoading(false);
    };

    checkAuthentication();
  }, []);

  const login = async () => {
    await oktaAuth.signInWithRedirect();
  };

  const logout = async () => {
    await oktaAuth.signOut();
    setIsAuthenticated(false);
    setUser(null);
  };

  const value = React.useMemo(() => ({
    isAuthenticated,
    user,
    login,
    logout,
    oktaAuth,
    loading
  }), [isAuthenticated, user, loading]);

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};