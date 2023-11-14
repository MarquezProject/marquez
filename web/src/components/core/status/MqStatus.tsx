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
        display: 'flex',
        alignItems: 'center',
        gap: theme.spacing(1),
      }}
    >
      <Box
        sx={{
          width: theme.spacing(2),
          height: theme.spacing(2),
          borderRadius: '50%',
        }}
        style={{ backgroundColor: color }}
      />
      {label && <MqText>{label}</MqText>}
    </Box>
  )
}

export default MqStatus
