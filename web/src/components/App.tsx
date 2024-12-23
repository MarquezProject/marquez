// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0
import React, { ReactElement } from 'react'
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'
import { Box, Container, CssBaseline } from '@mui/material'
import { LocalizationProvider } from '@mui/x-date-pickers'
import { StyledEngineProvider, ThemeProvider } from '@mui/material/styles'
import { Helmet, HelmetProvider } from 'react-helmet-async'
import { Provider } from 'react-redux'
import { applyMiddleware, createStore } from 'redux'
import { createBrowserHistory } from 'history'
import { composeWithDevTools } from '@redux-devtools/extension'
import { Route, Routes } from 'react-router-dom'
import { ReduxRouter, createRouterMiddleware } from '@lagunovsky/redux-react-router'
import createSagaMiddleware from 'redux-saga'

import { AuthProvider } from '../auth/AuthContext'
import ColumnLevel from '../routes/column-level/ColumnLevel'
import Dashboard from '../routes/dashboard/Dashboard'
import Datasets from '../routes/datasets/Datasets'
import Events from '../routes/events/Events'
import Jobs from '../routes/jobs/Jobs'
import { NotFound } from '../routes/not-found/NotFound'
import TableLevel from '../routes/table-level/TableLevel'
import { PrivateRoute } from './PrivateRoute'
import createRootReducer from '../store/reducers'
import rootSaga from '../store/sagas'
import { theme } from '../helpers/theme'
import ErrorBoundary from './ErrorBoundary'
import Header from './header/Header'
import Sidenav from './sidenav/Sidenav'
import Login from './Login'
import LoginCallback from './LoginCallback'
import Toast from './Toast'

const sagaMiddleware = createSagaMiddleware({
  onError: (error, _sagaStackIgnored) => {
    console.log('There was an error in the saga', error)
  },
})
const history = createBrowserHistory()
const historyMiddleware = createRouterMiddleware(history)

const store = createStore(
  createRootReducer(history),
  composeWithDevTools(applyMiddleware(sagaMiddleware, historyMiddleware))
)

sagaMiddleware.run(rootSaga)

const TITLE = 'Nu Data Lineage'

const App = (): ReactElement => {
  return (
    <ErrorBoundary>
      <AuthProvider>
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
                      <Container maxWidth={'xl'} disableGutters={true}>
                        <Header />
                      </Container>
                      <Routes>
                        <Route path='/login' element={<Login />} />
                        <Route path='/login/callback' element={<LoginCallback />} />
                        <Route
                          path='/'
                          element={
                            <PrivateRoute>
                              <Dashboard /> {/* Add this */}
                            </PrivateRoute>
                          }
                        />
                        <Route
                          path={'/jobs'}
                          element={
                            <PrivateRoute>
                              <Jobs />
                            </PrivateRoute>
                          }
                        />
                        <Route
                          path={'/datasets'}
                          element={
                            <PrivateRoute>
                              <Datasets />
                            </PrivateRoute>
                          }
                        />
                        <Route
                          path={'/events'}
                          element={
                            <PrivateRoute>
                              <Events />
                            </PrivateRoute>
                          }
                        />
                        <Route
                          path={'/datasets/column-level/:namespace/:name'}
                          element={<ColumnLevel />}
                        />
                        <Route
                          path={'/lineage/:nodeType/:namespace/:name'}
                          element={<TableLevel />}
                        />
                        <Route path='*' element={<NotFound />} />
                      </Routes>
                      <Toast />
                    </Box>
                  </LocalizationProvider>
                </ThemeProvider>
              </StyledEngineProvider>
            </ReduxRouter>
          </HelmetProvider>
        </Provider>
      </AuthProvider>
    </ErrorBoundary>
  )
}

export default App
