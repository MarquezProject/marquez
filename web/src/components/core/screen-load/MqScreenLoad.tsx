// SPDX-License-Identifier: Apache-2.0

import { HEADER_HEIGHT } from '../../../helpers/theme'
import Box from '@material-ui/core/Box/Box'
import CircularProgress from '@material-ui/core/CircularProgress/CircularProgress'
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
