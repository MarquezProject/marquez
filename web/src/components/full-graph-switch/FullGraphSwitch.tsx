// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import * as Redux from 'redux'
import { Box, FormControlLabel, Switch } from '@mui/material'
import { IState } from '../../store/reducers'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { setShowFullGraph } from '../../store/actionCreators'
import React from 'react'

interface FullGraphSwitch {
  showFullGraph: boolean
  setShowFullGraph: (showFullGraph: boolean) => void
}

const FullGraphSwitch: React.FC<FullGraphSwitch> = ({ setShowFullGraph, showFullGraph }) => {
  const i18next = require('i18next')
  const FULL_GRAPH_LABEL = i18next.t('lineage.full_graph_label')
  return (
    <Box
      sx={(theme) => ({
        display: 'flex',
        justifyContent: 'space-evenly',
        alignItems: 'center',
        zIndex: theme.zIndex.appBar,
      })}
    >
      <FormControlLabel
        sx={{
          marginLeft: 0,
        }}
        labelPlacement='start'
        control={
          <Switch
            checked={showFullGraph}
            onChange={(_, checked) => setShowFullGraph(checked)}
            color='primary'
          />
        }
        label={FULL_GRAPH_LABEL}
      />
    </Box>
  )
}

const mapStateToProps = (state: IState) => ({
  showFullGraph: state.lineage.showFullGraph,
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      setShowFullGraph: setShowFullGraph,
    },
    dispatch
  )

export default connect(mapStateToProps, mapDispatchToProps)(FullGraphSwitch)
