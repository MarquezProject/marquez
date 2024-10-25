import React, { ReactElement, useState, useEffect } from 'react';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { Box, Container, CssBaseline } from '@mui/material';
import { Helmet, HelmetProvider } from 'react-helmet-async';
import { LocalizationProvider } from '@mui/x-date-pickers';
import { NotFound } from '../routes/not-found/NotFound';
import { Provider } from 'react-redux';
import { ReduxRouter, createRouterMiddleware } from '@lagunovsky/redux-react-router';
import { Route, Routes } from 'react-router-dom';
import { StyledEngineProvider, ThemeProvider } from '@mui/material/styles';
import { applyMiddleware, createStore } from 'redux';
import { composeWithDevTools } from '@redux-devtools/extension';
import { createBrowserHistory } from 'history';
import { theme } from '../helpers/theme';
import ColumnLevel from '../routes/column-level/ColumnLevel';
import Dashboard from '../routes/dashboard/Dashboard';
import Datasets from '../routes/datasets/Datasets';
import Events from '../routes/events/Events';
import Header from './header/Header';
import Jobs from '../routes/jobs/Jobs';
import Sidenav from './sidenav/Sidenav';
import TableLevel from '../routes/table-level/TableLevel';
import Toast from './Toast';
import createRootReducer from '../store/reducers';
import createSagaMiddleware from 'redux-saga';
import rootSaga from '../store/sagas';
import ProtectedRoute from '../routes/protected-route/ProtectedRoute';
import axios from 'axios';
import logging from '../config/logging';

interface IApplicationProps {
  // Define the props that the Application component will receive
  // For example:
  title: string;
}

const sagaMiddleware = createSagaMiddleware({
  onError: (error, _sagaStackIgnored) => {
    console.log('There was an error in the saga', error);
  },
});
const history = createBrowserHistory();
const historyMiddleware = createRouterMiddleware(history);

const store = createStore(
  createRootReducer(history),
  composeWithDevTools(applyMiddleware(sagaMiddleware, historyMiddleware))
);

sagaMiddleware.run(rootSaga);

const TITLE = 'Marquez';

interface IApplicationProps {
  title: string;
}

const Application: React.FunctionComponent<IApplicationProps> = (props) => {
  const [loading, setLoading] = useState<boolean>(true);
  const [email, setEmail] = useState<string>('');

  useEffect(() => {
    logging.info('Initiating SAML check.', 'SAML');

    axios({
      method: 'GET',
      url: 'http://localhost:3000',
      withCredentials: true,
    })
      .then((response) => {
        logging.info(response.data.user, 'SAML');

        if (response.data.user.nameID) {
          setEmail(response.data.user.nameID);
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
    window.location.replace('http://localhost:3000/login');
  };

  if (loading) return <p>Loading ...</p>;

  return <p>Hello {email}!</p>;
};

const App = (): ReactElement => {
  return (
    <Provider store={store}>
      <HelmetProvider>
        <ReduxRouter history={history}>
          <StyledEngineProvider injectFirst>
            <ThemeProvider theme={theme}>
              <LocalizationProvider dateAdapter={AdapterDayjs}>
                <Helmet>
                  <title>{TITLE}</title>
                </Helmet>
                <CssBaseline />
                <Box ml={'80px'}>
                  <Sidenav />
                  <Container maxWidth={'lg'} disableGutters={true}>
                    <Header />
                  </Container>
                  <Routes>
                    <Route path={'/'} element={<Dashboard />} />
                    <Route path={'/jobs'} element={<Jobs />} />
                    <Route path={'/datasets'} element={<Datasets />} />
                    <Route path={'/events'} element={<Events />} />
                    <Route
                      path={'/datasets/column-level/:namespace/:name'}
                      element={<ColumnLevel />}
                    />
                    <Route path={'/lineage/:nodeType/:namespace/:name'} element={<TableLevel />} />
                    <Route path='*' element={<NotFound />} />
                  </Routes>
                  <Toast />
                  <Application title="Marquez" /> {/* Integrate the Application component */}
                </Box>
              </LocalizationProvider>
            </ThemeProvider>
          </StyledEngineProvider>
        </ReduxRouter>
      </HelmetProvider>
    </Provider>
  );
};

export default App;