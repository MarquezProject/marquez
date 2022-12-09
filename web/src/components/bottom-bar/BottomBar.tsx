// SPDX-License-Identifier: Apache-2.0

import React from 'react'

import { Box, Container, Theme } from '@material-ui/core'
import { DRAWER_WIDTH } from '../../helpers/theme'
import { IState } from '../../store/reducers'
import { LineageNode } from '../lineage/types'
import { Undefinable } from '../../types/util/Nullable'
import { WithStyles, createStyles, withStyles } from '@material-ui/core/styles'
import { connect } from 'react-redux'
import { isLineageDataset, isLineageJob } from '../../helpers/nodes'
import DatasetDetailPage from '../datasets/DatasetDetailPage'
import DragBar from '../lineage/components/drag-bar/DragBar'
import JobDetailPage from '../jobs/JobDetailPage'

const styles = (theme: Theme) => {
  return createStyles({
    overflow: {
      overflow: 'auto',
      backgroundColor: theme.palette.background.default
    },
    bottomBar: {
      marginLeft: DRAWER_WIDTH,
      right: 0,
      width: `calc(100% - ${DRAWER_WIDTH}px)`,
      bottom: 0,
      position: 'fixed'
    }
  })
}

interface OwnProps {
  selectedNodeData: Undefinable<LineageNode>
}

interface StateProps {
  bottomBarHeight: number
}

type BottomBarProps = StateProps & OwnProps & WithStyles<typeof styles>

class BottomBar extends React.Component<BottomBarProps> {
  render() {
    const { classes, bottomBarHeight, selectedNodeData } = this.props
    if (!selectedNodeData) {
      return null
    }
    const lineageJob = isLineageJob(selectedNodeData.data)
    const lineageDataset = isLineageDataset(selectedNodeData.data)
    return (
      <Box className={classes.bottomBar}>
        <DragBar />
        <Box className={classes.overflow} height={bottomBarHeight}>
          <Container maxWidth={'lg'} disableGutters={true}>
            {lineageJob && <JobDetailPage job={lineageJob} />}
            {lineageDataset && <DatasetDetailPage dataset={lineageDataset} />}
          </Container>
        </Box>
      </Box>
    )
  }
}

const mapStateToProps = (state: IState) => ({
  bottomBarHeight: state.lineage.bottomBarHeight,
  selectedNodeData: state.lineage.lineage.graph.find(node => state.lineage.selectedNode === node.id)
})

export default connect(mapStateToProps)(withStyles(styles)(BottomBar))
