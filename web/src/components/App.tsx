import { ConnectedRouter, routerMiddleware } from 'connected-react-router'
import { Container, CssBaseline } from '@material-ui/core'
import { Helmet } from 'react-helmet'
import { MuiThemeProvider } from '@material-ui/core/styles'
import { Provider } from 'react-redux'
import { applyMiddleware, createStore } from 'redux'
import { composeWithDevTools } from 'redux-devtools-extension'
import { createBrowserHistory } from 'history'
import { theme } from '../helpers/theme'
import BottomBar from './bottom-bar/BottomBar'
import Header from './header/Header'
import Lineage from './lineage/Lineage'
import React, { ReactElement, useState } from 'react'
import Toast from './Toast'
import createRootReducer from '../reducers'
import createSagaMiddleware from 'redux-saga'
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

const TITLE = 'Marquez'

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
          <Lineage />
          <BottomBar setShowJobs={setShowJobs} showJobs={showJobs} />
          <Toast />
        </MuiThemeProvider>
      </ConnectedRouter>
    </Provider>
  )
}

export default App
