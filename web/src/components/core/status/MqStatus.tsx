// SPDX-License-Identifier: Apache-2.0

import { theme } from '../../../helpers/theme'
import Box from '@mui/material/Box'
import MqText from '../text/MqText'
import React from 'react'

interface OwnProps {
  color: string | null
  label?: string
}

const MqStatus: React.FC<OwnProps> = ({ label, color }) => {
  if (!color) {
    return null
  }
  return (
    <Box
      sx={{
        display: 'inline-flex',
        alignItems: 'center',
        gap: theme.spacing(1),
      }}
      border={`1px solid ${color}`}
      p={'4px 8px'}
      borderRadius={theme.spacing(1)}
    >
      <Box
        sx={{
          width: theme.spacing(1.5),
          height: theme.spacing(1.5),
          borderRadius: '50%',
        }}
        display={'flex'}
        style={{ backgroundColor: color }}
      />
      {label && (
        <MqText small bold color={color}>
          {label}
        </MqText>
      )}
    </Box>
  )
}

export default MqStatus
