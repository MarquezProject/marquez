// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import Box from '@mui/material/Box'
import CircularProgress from '@mui/material/CircularProgress/CircularProgress'
import React, { ReactElement } from 'react'

interface MqScreenLoadProps {
  children?: ReactElement
  loading: boolean
  customHeight?: string
}

export const MqScreenLoad: React.FC<MqScreenLoadProps> = ({ loading, children, customHeight }) => {
  return loading || !children ? (
    <Box
      height={customHeight ? customHeight : 'calc(100vh)'}
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
