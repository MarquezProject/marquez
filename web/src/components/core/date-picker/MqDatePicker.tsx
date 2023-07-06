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

const MqDatePicker: React.FC<DatePickerProps> = ({ value, onChange, label = '', format = 'MM DD YYYY hh:mm a' }) => {
  const theme = createTheme(useTheme())

  return (
    <DateTimePicker
      label={label}
      sx={{
        minWidth: '200px', 
        cursor: 'pointer',
        backgroundColor: 'transparent',
        border: `2px solid ${theme.palette.common.white}`,
        padding: `${theme.spacing(1)} ${theme.spacing(3)}`,
        transition: theme.transitions.create(['border-color', 'box-shadow']),
        borderRadius: theme.spacing(4),
        '& *': {
          cursor: 'pointer'
        },
        '&:hover': {
          borderColor: theme.palette.primary.main,
          boxShadow: `${alpha(theme.palette.primary.main, 0.25)} 0 0 0 3px`,
          '& > label': {
            color: theme.palette.primary.main,
            transition: theme.transitions.create(['color'])
          }
        },
        '& > label': {
          top: 'initial',
          left: 'initial'
        },
        '& > div': {
          marginTop: theme.spacing(1),
          '&:before': {
            display: 'none'
          },
          '&:after': {
            display: 'none'
          },
        }
      }}
      value={dayjs(value)}
      onChange={onChange}
      format={format}
    />
  )
}

export default MqDatePicker
