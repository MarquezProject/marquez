// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import * as Redux from 'redux'
import { Box, FormControlLabel, TextField, createTheme } from '@mui/material'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { setLineageGraphDepth } from '../../../../store/actionCreators'
import { useTheme } from '@emotion/react'
import React from 'react'

interface DepthConfigProps {
  depth: number
  setDepth: (depth: number) => void
}

const DepthConfig: React.FC<DepthConfigProps> = ({ setDepth, depth }) => {
  const theme = createTheme(useTheme())

  const i18next = require('i18next')
  const GRAPH_TITLE = i18next.t('lineage.graph_depth_title')
  return (
    <Box
      sx={{
        display: 'flex',
        justifyContent: 'space-evenly',
        alignItems: 'center',
        zIndex: theme.zIndex.appBar,
      }}
    >
      <FormControlLabel
        sx={{
          marginLeft: 0,
          '& .MuiFormControlLabel-label': {
            marginRight: '.5rem',
          },
        }}
        labelPlacement='start'
        control={
          <TextField
            type='number'
            value={depth}
            onChange={(e) =>
              setDepth(isNaN(parseInt(e.target.value)) ? 0 : parseInt(e.target.value))
            }
            variant='outlined'
            size='small'
            aria-label={GRAPH_TITLE}
            sx={{
              textAlign: 'center',
            }}
            inputProps={{
              min: 0,
              max: 100,
            }}
          />
        }
        label={GRAPH_TITLE}
      />
    </Box>
  )
}

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      setDepth: setLineageGraphDepth,
    },
    dispatch
  )

export default connect(null, mapDispatchToProps)(DepthConfig)
