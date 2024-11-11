// src/context/UserContext.tsx
import React, { createContext, useContext, useEffect, useState, ReactNode } from 'react';
import axios from 'axios';

interface User {
  email: string;
}

// Define the context's properties, including loading and error states
interface UserContextProps {
    user: User | null;
    loading: boolean;
    error: string | null;
  }

// Create the UserContext with default values
const UserContext = createContext<UserContextProps>({
    user: null,
    loading: true,
    error: null,
  });

interface UserProviderProps {
  children: ReactNode;
}

// Create the UserProvider component
export const UserProvider: React.FC<UserProviderProps> = ({ children }) => {
    const [user, setUser] = useState<User | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
  
    useEffect(() => {
      const fetchUser = async () => {
        try {
          const response = await axios.get('/whoami'); // Ensure this endpoint is correctly set up in your backend
          setUser(response.data.user);
        } catch (err) {
          console.error('Failed to fetch user:', err);
          setError('Failed to load user information.');
        } finally {
          setLoading(false);
        }
      };
  
      fetchUser();
    }, []);
  
    const contextValue = React.useMemo(() => ({ user, loading, error }), [user, loading, error]);

    return (
      <UserContext.Provider value={contextValue}>
        {children}
      </UserContext.Provider>
    );
  };
  
  // Create a custom hook to use the UserContext
  export const useUser = () => useContext(UserContext);