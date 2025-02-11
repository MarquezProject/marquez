// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { Box, createTheme } from '@mui/material'
import { Run } from '../../types/api'
import { runStateColor } from '../../helpers/nodes'

import { useTheme } from '@emotion/react'
import MQTooltip from '../core/tooltip/MQTooltip'
import React, { FunctionComponent } from 'react'
import { trackEvent } from '../ga4'

interface RunStatusProps {
  run: Run
}

const RunStatus: FunctionComponent<RunStatusProps> = (props) => {
  const { run } = props
  const theme = createTheme(useTheme())
  const handleTooltipOpen = () => {
    trackEvent('RunStatus', 'View Run Status Tooltip', run.state);
  };

  return (
    <MQTooltip title={run.state} onOpen={handleTooltipOpen}>
      <Box
        mr={1}
        sx={{
          minWidth: theme.spacing(2),
          width: theme.spacing(2),
          height: theme.spacing(2),
          borderRadius: '50%',
        }}
        style={{ backgroundColor: runStateColor(run.state) }}
      />
    </MQTooltip>
  )
}

export default RunStatus
