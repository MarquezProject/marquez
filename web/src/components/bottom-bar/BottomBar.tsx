// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import React from 'react'

import { Box, Container, createTheme } from '@mui/material'
import { DRAWER_WIDTH } from '../../helpers/theme'
import { IState } from '../../store/reducers'
import { LineageNode } from '../lineage/types'
import { Undefinable } from '../../types/util/Nullable'
import { connect } from 'react-redux'
import { isLineageDataset, isLineageJob } from '../../helpers/nodes'
import { useTheme } from '@emotion/react'
import DatasetDetailPage from '../datasets/DatasetDetailPage'
import DragBar from '../lineage/components/drag-bar/DragBar'
import JobDetailPage from '../jobs/JobDetailPage'

interface OwnProps {
  selectedNodeData: Undefinable<LineageNode>
}

interface StateProps {
  bottomBarHeight: number
}

type BottomBarProps = StateProps & OwnProps

const BottomBar: React.FC<BottomBarProps> = ({ bottomBarHeight, selectedNodeData }) => {
  const theme = createTheme(useTheme())

  if (!selectedNodeData) {
    return null
  }
  const lineageJob = isLineageJob(selectedNodeData.data)
  const lineageDataset = isLineageDataset(selectedNodeData.data)
  return (
    <Box
      sx={{
        marginLeft: `${DRAWER_WIDTH}px`,
        right: 0,
        width: `calc(100% - ${DRAWER_WIDTH}px)`,
        bottom: 0,
        position: 'fixed',
        zIndex: theme.zIndex.appBar + 1,
      }}
    >
      <DragBar />
      <Box
        sx={{
          overflow: 'auto',
          backgroundColor: theme.palette.background.default,
        }}
        height={bottomBarHeight}
      >
        <Container maxWidth={'lg'} disableGutters={true}>
          {lineageJob && <JobDetailPage job={lineageJob} />}
          {lineageDataset && <DatasetDetailPage lineageDataset={lineageDataset} />}
        </Container>
      </Box>
    </Box>
  )
}

const mapStateToProps = (state: IState) => ({
  bottomBarHeight: state.lineage.bottomBarHeight,
  selectedNodeData: state.lineage.lineage.graph.find(
    (node) => state.lineage.selectedNode === node.id
  ),
})

export default connect(mapStateToProps)(BottomBar)
