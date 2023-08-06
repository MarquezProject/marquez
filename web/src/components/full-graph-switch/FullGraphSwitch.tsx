// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import * as Redux from 'redux'
import { Box, FormControlLabel, Switch } from '@mui/material'
import { IState } from '../../store/reducers'
import { Theme, WithStyles, createStyles, withStyles } from '@material-ui/core/styles'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { setShowFullGraph } from '../../store/actionCreators'
import React from 'react'

const styles = (theme: Theme) =>
  createStyles({
    root: {
      display: 'flex',
      justifyContent: 'space-evenly',
      alignItems: 'center',
      zIndex: theme.zIndex.appBar
    },
    formControl: {
      marginLeft: 0
    }
  })

interface FullGraphSwitch extends WithStyles<typeof styles> {
  showFullGraph: boolean
  setShowFullGraph: (showFullGraph: boolean) => void
}

const FullGraphSwitch: React.FC<FullGraphSwitch> = ({
  classes,
  setShowFullGraph,
  showFullGraph
}) => {
  const i18next = require('i18next')
  const FULL_GRAPH_LABEL = i18next.t('lineage.full_graph_label')
  return (
    <Box className={classes.root}>
      <FormControlLabel
        className={classes.formControl}
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
  showFullGraph: state.lineage.showFullGraph
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      setShowFullGraph: setShowFullGraph
    },
    dispatch
  )

export default connect(mapStateToProps, mapDispatchToProps)(withStyles(styles)(FullGraphSwitch))
