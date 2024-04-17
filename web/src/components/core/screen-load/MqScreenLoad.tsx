// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { HEADER_HEIGHT } from '../../../helpers/theme'
import Box from '@mui/material/Box'
import CircularProgress from '@mui/material/CircularProgress/CircularProgress'
import React, { ReactElement } from 'react'

interface MqScreenLoadProps {
  children?: ReactElement
  loading: boolean
  noHeader?: boolean
}

export const MqScreenLoad: React.FC<MqScreenLoadProps> = ({ loading, children, noHeader }) => {
  return loading || !children ? (
    <Box
      height={noHeader ? 'calc(100vh)' : `calc(100vh - ${HEADER_HEIGHT}px)`}
      display={'flex'}
      justifyContent={'center'}
      alignItems={'center'}
    >
      <CircularProgress color='primary' />
    </Box>
  ) : (
    children
  )
}
