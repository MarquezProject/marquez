// SPDX-License-Identifier: Apache-2.0

import { Box, Container, CssBaseline } from '@material-ui/core'
import { ConnectedRouter, routerMiddleware } from 'connected-react-router'
import { Helmet } from 'react-helmet'
import { MuiThemeProvider } from '@material-ui/core/styles'
import { Provider } from 'react-redux'
import { Route, Switch } from 'react-router-dom'
import { applyMiddleware, createStore } from 'redux'
import { composeWithDevTools } from 'redux-devtools-extension'
import { createBrowserHistory } from 'history'
import { theme } from '../helpers/theme'
import BottomBar from './bottom-bar/BottomBar'
import Datasets from '../routes/datasets/Datasets'
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
  }
})
const history = createBrowserHistory()
const historyMiddleware = routerMiddleware(history)

const store = createStore(
  createRootReducer(history),
  composeWithDevTools(applyMiddleware(sagaMiddleware, historyMiddleware))
)

sagaMiddleware.run(rootSaga)

const TITLE = 'Marquez'

const App = (): ReactElement => {
  return (
    <Provider store={store}>
      <ConnectedRouter history={history}>
        <MuiThemeProvider theme={theme}>
          <Helmet>
            <title>{TITLE}</title>
          </Helmet>
          <CssBaseline />
          <Box ml={12}>
            <Sidenav />
            <Container maxWidth={'lg'} disableGutters={true}>
              <Header />
            </Container>
            <Switch>
              <Route path={'/'} exact>
                <Jobs />
              </Route>
              <Route path={'/datasets'} exact>
                <Datasets />
              </Route>
              <Route path={'/lineage/:nodeType/:namespace/:nodeName'}>
                <Lineage />
                <BottomBar />
              </Route>
            </Switch>
            <Toast />
          </Box>
        </MuiThemeProvider>
      </ConnectedRouter>
    </Provider>
  )
}

export default App
