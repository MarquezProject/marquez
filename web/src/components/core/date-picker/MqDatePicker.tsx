// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { DateTimePicker } from '@mui/x-date-pickers'
import { alpha, createTheme } from '@mui/material/styles'
import { useTheme } from '@emotion/react'
import React from 'react'
import dayjs from 'dayjs'

interface OwnProps {
  value: string
  onChange: (e: any) => void
  label?: string
  format?: string
}

type DatePickerProps = OwnProps

const MqDatePicker: React.FC<DatePickerProps> = ({
  value,
  onChange,
  label = '',
  format = 'MM DD YYYY hh:mm a',
}) => {
  const theme = createTheme(useTheme())

  return (
    <DateTimePicker
      label={label}
      sx={{
        minWidth: '200px',
        '.MuiFormLabel-root': {
          transform: 'translate(14px, 0px) scale(0.75)',
        },
        '&:hover': {
          '.MuiOutlinedInput-notchedOutline': {
            borderColor: `${theme.palette.primary.main} !important`,
            boxShadow: `${alpha(theme.palette.primary.main, 0.25)} 0 0 0 3px`,
            transition: theme.transitions.create(['border-color', 'box-shadow']),
          },
        },
        '.MuiOutlinedInput-notchedOutline': {
          border: `2px solid ${theme.palette.common.white}`,
          borderRadius: theme.spacing(4),
          top: '-16px',
          left: '-6px',
          '> legend': {
            display: 'none',
          },
        },
      }}
      value={dayjs(value)}
      onChange={onChange}
      format={format}
    />
  )
}

export default MqDatePicker
