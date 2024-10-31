import React, { useEffect, useState } from 'react';
import axios from 'axios';
import logging from '../routes/config/logging';

const withAuth = (WrappedComponent: React.ComponentType) => {
  const AuthComponent: React.FC = (props) => {
    const [loading, setLoading] = useState<boolean>(true);
    const [authenticated, setAuthenticated] = useState<boolean>(false);

    useEffect(() => {
      if (process.env.REACT_APP_ENABLE_AUTH === 'true') {
        logging.info('Initiating SAML check.', 'SAML');

        axios({
          method: 'GET',
          url: `http://localhost:1337/whoami`,
          withCredentials: true,
        })
          .then((response) => {
            logging.info(response.data.user, 'SAML');

            if (response.data.user.nameID) {
              setAuthenticated(true);
              setLoading(false);
            } else {
              RedirectToLogin();
            }
          })
          .catch((error) => {
            logging.error(error, 'SAML');
            RedirectToLogin();
          });
      } else {
        // If authentication is disabled, set authenticated to true
        setAuthenticated(true);
        setLoading(false);
      }
    }, []);

    const RedirectToLogin = () => {
      window.location.replace(`http://localhost:1337/login`);
    };

    if (loading) return <p>Loading...</p>;

    if (!authenticated) return null;

    return <WrappedComponent {...props} />;
  };

  return AuthComponent;
};

export default withAuth;