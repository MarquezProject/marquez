import React from 'react'

import { Box, Container, Theme } from '@material-ui/core'
import { IState } from '../../reducers'
import { Route, Switch } from 'react-router-dom'
import { WithStyles, createStyles, withStyles } from '@material-ui/core/styles'
import { connect } from 'react-redux'
import DatasetDetailPage from '../DatasetDetailPage'
import DragBar from '../lineage/components/drag-bar/DragBar'
import Home from '../Home'
import JobDetailPage from '../JobDetailPage'

const styles = (theme: Theme) => {
  return createStyles({
    overflow: {
      overflow: 'auto',
      backgroundColor: theme.palette.background.default
    },
    bottomBar: {
      right: 0,
      width: '100%',
      bottom: 0,
      position: 'fixed'
    }
  })
}

interface OwnProps {
  setShowJobs: (bool: boolean) => void
  showJobs: boolean
}

interface StateProps {
  bottomBarHeight: number
}

type BottomBarProps = StateProps & OwnProps & WithStyles<typeof styles>

class BottomBar extends React.Component<BottomBarProps> {
  render() {
    const { classes, bottomBarHeight, showJobs } = this.props
    return (
      <Box className={classes.bottomBar}>
        <DragBar />
        <Box className={classes.overflow} height={bottomBarHeight}>
          <Container maxWidth={'lg'} disableGutters={true}>
            <Switch>
              <Route path='/' exact render={props => <Home {...props} showJobs={showJobs} />} />
              <Route path='/datasets/:datasetName' exact component={DatasetDetailPage} />
              <Route path='/jobs/:jobName' exact component={JobDetailPage} />
            </Switch>
          </Container>
        </Box>
      </Box>
    )
  }
}

const mapStateToProps = (state: IState) => ({
  bottomBarHeight: state.lineage.bottomBarHeight
})

export default connect(mapStateToProps)(withStyles(styles)(BottomBar))
