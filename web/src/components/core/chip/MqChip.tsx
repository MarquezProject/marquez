// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { IconDefinition } from '@fortawesome/fontawesome-svg-core'
import { createTheme } from '@mui/material/styles'
import { useTheme } from '@emotion/react'
import Box from '@mui/material/Box'
import MqSmallIcon from '../small-icon/MqSmallIcon'
import MqText from '../text/MqText'
import React from 'react'

interface MqChipProps {
  selected?: boolean
  onSelect?: (label: string) => void
  icon?: IconDefinition
  foregroundColor?: string
  backgroundColor?: string
  text?: string
  value: string
  selectable?: boolean
}

/**
 * This is a simple button that can be either selected or unselected. Is is configurable with icons and/or text
 */
const MqChip: React.FC<MqChipProps> = ({
  selected,
  onSelect,
  icon,
  text,
  value,
  foregroundColor,
  backgroundColor,
  selectable,
}) => {
  const theme = createTheme(useTheme())

  return (
    <Box
      id={`chip-${value}`}
      sx={{
        display: 'inline-block',
        borderRadius: theme.spacing(2),
        padding: '2px 12px',
        cursor: 'pointer',
        userSelect: 'none',
        boxShadow: selected ? `0 0 1px 1px ${theme.palette.secondary.main}` : 'initial',
      }}
      onClick={() => {
        if (selectable !== false && onSelect) {
          onSelect(value)
        }
      }}
    >
      {icon && foregroundColor && backgroundColor && (
        <Box display={'inherit'}>
          <MqSmallIcon
            icon={icon}
            shape={'rect'}
            foregroundColor={foregroundColor}
            backgroundColor={backgroundColor}
          />
        </Box>
      )}
      {text && (
        <Box ml={icon ? 1 : 0} display={'inline'}>
          <MqText font={'mono'} inline>
            {text}
          </MqText>
        </Box>
      )}
    </Box>
  )
}

export default MqChip
