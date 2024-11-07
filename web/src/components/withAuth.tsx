import React, { useEffect, useState } from 'react';
import axios from 'axios';
import logging from '../routes/config/logging';

const withAuth = (WrappedComponent: React.ComponentType) => {
  const AuthComponent: React.FC = (props) => {
    const [loading, setLoading] = useState<boolean>(true);
    const [authenticated, setAuthenticated] = useState<boolean>(false);

    useEffect(() => {
      logging.info('Initiating SAML check.', 'SAML');
      axios({
        method: 'GET',
        url: 'http://a2c309e8def534afa843c0d5bc61f788-fd91484ebb3b3116.elb.us-west-2.amazonaws.com/whoami',
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
    }, []);

    const RedirectToLogin = () => {
      window.location.replace('http://a2c309e8def534afa843c0d5bc61f788-fd91484ebb3b3116.elb.us-west-2.amazonaws.com/login');
    };

    if (loading) return <p>Loading from Marquez withAuth class...</p>;

    if (!authenticated) 
      return null;

    return <WrappedComponent {...props} />;
  };

  return AuthComponent;
};

export default withAuth;