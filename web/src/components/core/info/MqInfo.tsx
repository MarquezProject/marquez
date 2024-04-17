// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import Box from '@mui/material/Box'
import MqText from '../text/MqText'
import React, { ReactElement } from 'react'

interface MqInfoProps {
  icon: ReactElement
  label: string
  value: ReactElement | string | number
}

export const MqInfo: React.FC<MqInfoProps> = ({ icon, label, value }) => {
  return (
    <Box>
      <Box display={'flex'} alignItems={'center'} mb={1}>
        {icon}
        <Box ml={1}>
          <MqText subdued>{label}</MqText>
        </Box>
      </Box>
      <Box display={'flex'} alignItems={'center'}>
        <MqText bold>{value}</MqText>
      </Box>
    </Box>
  )
}
