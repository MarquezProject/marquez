// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'
import { Box, Container, CssBaseline } from '@mui/material'
import { Helmet, HelmetProvider } from 'react-helmet-async'
import { LocalizationProvider } from '@mui/x-date-pickers'
import { Provider } from 'react-redux'
import { ReduxRouter, createRouterMiddleware } from '@lagunovsky/redux-react-router'
import { Route, Routes } from 'react-router-dom'
import { StyledEngineProvider, ThemeProvider } from '@mui/material/styles'
import { applyMiddleware, createStore } from 'redux'
import { composeWithDevTools } from '@redux-devtools/extension'
import { createBrowserHistory } from 'history'
import { theme } from '../helpers/theme'
import BottomBar from './bottom-bar/BottomBar'
import Datasets from '../routes/datasets/Datasets'
import Events from '../routes/events/Events'
import Header from './header/Header'
import Jobs from '../routes/jobs/Jobs'
import Lineage from './lineage/Lineage'
import React, { ReactElement } from 'react'
import Sidenav from './sidenav/Sidenav'
import Toast from './Toast'
import createRootReducer from '../store/reducers'
import createSagaMiddleware from 'redux-saga'
import rootSaga from '../store/sagas'

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

const TITLE = 'Marquez'

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
                <Box ml={12}>
                  <Sidenav />
                  <Container maxWidth={'lg'} disableGutters={true}>
                    <Header />
                  </Container>
                  <Routes>
                    <Route path={'/'} element={<Jobs />} />
                    <Route path={'/datasets'} element={<Datasets />} />
                    <Route path={'/events'} element={<Events />} />
                    <Route
                      path={'/lineage/:nodeType/:namespace/:nodeName'}
                      element={
                        <>
                          <Lineage />
                          <BottomBar />
                        </>
                      }
                    />
                  </Routes>
                  <Toast />
                </Box>
              </LocalizationProvider>
            </ThemeProvider>
          </StyledEngineProvider>
        </ReduxRouter>
      </HelmetProvider>
    </Provider>
  )
}

export default App
