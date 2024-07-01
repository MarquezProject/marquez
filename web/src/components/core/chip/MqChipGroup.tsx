// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import React from 'react'

import { IconDefinition } from '@fortawesome/fontawesome-svg-core'
import { Undefinable } from '../../../types/util/Nullable'
import { createTheme } from '@mui/material/styles'
import { useTheme } from '@emotion/react'
import Box from '@mui/material/Box'
import MqChip from './MqChip'

interface OwnProps {
  chips: {
    icon?: IconDefinition
    foregroundColor?: string
    backgroundColor?: string
    text?: string
    value: string
    selectable?: boolean
  }[]
  initialSelection: string
  onSelect: (label: string) => void
}

interface StateProps {
  selected: Undefinable<string>
}

type MqChipGroupProps = OwnProps

/**
 * This functions as a standard button group and wraps the <Chip /> component with
 * selection logic and callbacks needed to manage and change state
 */
const MqChipGroup: React.FC<MqChipGroupProps> = ({ chips, initialSelection, onSelect }) => {
  const [state, setState] = React.useState<StateProps>({
    selected: initialSelection,
  })

  const handlerOnSelect = (label: string) => {
    setState({
      selected: label,
    })

    setTimeout(() => {
      onSelect(label)
    }, 1)
  }

  const theme = createTheme(useTheme())

  return (
    <Box
      sx={{
        border: `1px solid ${theme.palette.secondary.main}`,
        backgroundColor: theme.palette.background.paper,
        borderRadius: theme.spacing(2),
        display: 'inline-block',
        padding: '1px',
      }}
    >
      {chips.map((chip) => (
        <MqChip
          {...chip}
          onSelect={handlerOnSelect}
          selected={state.selected === chip.value}
          key={chip.value}
          value={chip.value}
        />
      ))}
    </Box>
  )
}

export default MqChipGroup
