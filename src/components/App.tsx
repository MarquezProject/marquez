import React, { ReactElement, useState } from 'react'
import { Helmet } from 'react-helmet'
import { CssBaseline } from '@material-ui/core'
import AppBar from './AppBar'
const globalStyles = require('../global_styles.css')
const { neptune, telescopeBlack } = globalStyles
import { Route, Switch } from 'react-router-dom'
import { Grid } from '@material-ui/core'
import { MuiThemeProvider, createMuiTheme } from '@material-ui/core/styles'
import { createStore, applyMiddleware } from 'redux'
import { Provider } from 'react-redux'
import logger from 'redux-logger'
import createSagaMiddleware from 'redux-saga'
import { composeWithDevTools } from 'redux-devtools-extension'
import { createBrowserHistory } from 'history'
import { routerMiddleware, ConnectedRouter } from 'connected-react-router'
import {
  withStyles,
  createStyles,
  WithStyles as IWithStyles,
  Theme as ITheme
} from '@material-ui/core/styles'

import createRootReducer from '../reducers'
import rootSaga from '../sagas'
import HomeContainer from '../containers/HomeContainer'
import CustomSearchBarContainer from '../containers/CustomSearchBarContainer'
import Toast from '../containers/ToastContainer'
import NetworkGraphContainer from '../containers/NetworkGraphContainer'
import DatasetDetailContainer from '../containers/DatasetDetailContainer'
import JobDetailContainer from '../containers/JobDetailContainer'

const sagaMiddleware = createSagaMiddleware({
  onError: (error, _sagaStackIgnored) => {
    console.log('There was an error in the saga', error)
  }
})
const history = createBrowserHistory()
const historyMiddleware = routerMiddleware(history)

const middleware = __DEVELOPMENT__
  ? [sagaMiddleware, historyMiddleware, logger]
  : [sagaMiddleware, historyMiddleware]

const store = createStore(
  createRootReducer(history),
  composeWithDevTools(applyMiddleware(...middleware))
)

sagaMiddleware.run(rootSaga)
// see the defaults here for MUI Theme -- https://material-ui.com/customization/default-theme/?expend-path=$.typography
const theme = createMuiTheme({
  typography: {
    h3: {
      fontSize: '1rem',
      fontWeight: 700,
      lineHeight: 2
    },
    fontSize: 14
  },
  palette: {
    primary: {
      main: telescopeBlack
    },
    secondary: {
      main: neptune
    }
  }
})

const styles = (_theme: ITheme) => {
  return createStyles({
    root: {
      height: '100vh',
      display: 'flex'
    }
  })
}

interface IProps extends IWithStyles<typeof styles> {}

const TITLE = 'Marquez | Data Kit'

const App = ({ classes }: IProps): ReactElement => {
  const [showJobs, setShowJobs] = useState(false)
  return (
    <Provider store={store}>
      <ConnectedRouter history={history}>
        <MuiThemeProvider theme={theme}>
          <Helmet>
            <title>{TITLE}</title>
          </Helmet>
          <CssBaseline />
          <Grid direction='column' alignItems='stretch' classes={classes} justify='space-between'>
            <AppBar />
            <NetworkGraphContainer />
            <CustomSearchBarContainer
              setShowJobs={setShowJobs}
              showJobs={showJobs}
            ></CustomSearchBarContainer>
            <Switch>
              <Route
                path='/'
                exact
                render={props => (
                  <HomeContainer {...props} showJobs={showJobs} setShowJobs={setShowJobs} />
                  )}
              />
              <Route path='/datasets/:datasetName' exact component={DatasetDetailContainer} />
              <Route path='/jobs/:jobName' exact component={JobDetailContainer} />
            </Switch>
            <Toast />
          </Grid>
        </MuiThemeProvider>
      </ConnectedRouter>
    </Provider>
  )
}

export default withStyles(styles)(App)
