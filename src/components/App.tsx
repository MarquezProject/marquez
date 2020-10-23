import { ConnectedRouter, routerMiddleware } from 'connected-react-router'
import { Container, CssBaseline } from '@material-ui/core'
import { Helmet } from 'react-helmet'
import { Theme as ITheme, createStyles, withStyles } from '@material-ui/core/styles'
import { MuiThemeProvider } from '@material-ui/core/styles'
import { Provider } from 'react-redux'
import { Route, Switch } from 'react-router-dom'
import { applyMiddleware, createStore } from 'redux'
import { composeWithDevTools } from 'redux-devtools-extension'
import { createBrowserHistory } from 'history'
import React, { ReactElement, useState } from 'react'
import createSagaMiddleware from 'redux-saga'

import { theme } from '../helpers/theme'
import DatasetDetailPage from './DatasetDetailPage'
import Header from './header/Header'
import Home from './Home'
import JobDetailPage from './JobDetailPage'
import NetworkGraph from './NetworkGraph'
import Toast from './Toast'
import createRootReducer from '../reducers'
import rootSaga from '../sagas'

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

const styles = (_theme: ITheme) => {
  return createStyles({
    root: {
      height: '100vh',
      display: 'flex'
    }
  })
}

const TITLE = 'Marquez | Data Kit'

const App = (): ReactElement => {
  const [showJobs, setShowJobs] = useState(false)
  return (
    <Provider store={store}>
      <ConnectedRouter history={history}>
        <MuiThemeProvider theme={theme}>
          <Helmet>
            <title>{TITLE}</title>
          </Helmet>
          <CssBaseline />
          <Container maxWidth={'lg'} disableGutters={true}>
            <Header setShowJobs={setShowJobs} showJobs={showJobs} />
          </Container>
          <NetworkGraph />
          <Container maxWidth={'lg'} disableGutters={true}>
            <Switch>
              <Route
                path='/'
                exact
                render={props => <Home {...props} showJobs={showJobs} setShowJobs={setShowJobs} />}
              />
              <Route path='/datasets/:datasetName' exact component={DatasetDetailPage} />
              <Route path='/jobs/:jobName' exact component={JobDetailPage} />
            </Switch>
          </Container>
          <Toast />
        </MuiThemeProvider>
      </ConnectedRouter>
    </Provider>
  )
}

export default withStyles(styles)(App)
