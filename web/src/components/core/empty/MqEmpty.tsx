// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { Box } from '@mui/material'
import { createTheme } from '@mui/material/styles'
import { useTheme } from '@emotion/react'
import MqText from '../text/MqText'
import React, { ReactElement } from 'react'

interface MqEmptyProps {
  emoji?: string
  title?: string
  body?: string
  children?: ReactElement
}

const MqEmpty: React.FC<MqEmptyProps> = ({ title, body, emoji, children }) => {
  const theme = createTheme(useTheme())

  return (
    <Box
      sx={{
        display: 'flex',
        justifyContent: 'center',
      }}
    >
      <Box
        sx={{
          padding: theme.spacing(2),
          border: `2px dashed ${theme.palette.secondary.main}`,
          borderRadius: theme.shape.borderRadius,
          width: '400px',
        }}
      >
        {title && (
          <Box mb={1}>
            <MqText heading>{title}</MqText>
          </Box>
        )}
        {body && (
          <Box>
            <MqText inline>{body}</MqText>
          </Box>
        )}
        {children && <Box>{children}</Box>}
        {emoji && (
          <Box
            sx={{
              padding: theme.spacing(2),
              border: `2px dashed ${theme.palette.secondary.main}`,
              borderRadius: theme.shape.borderRadius,
              width: '400px',
            }}
            mt={1}
          >
            <span role={'img'} aria-label={'icon'}>
              {emoji}
            </span>
          </Box>
        )}
      </Box>
    </Box>
  )
}

export default MqEmpty
