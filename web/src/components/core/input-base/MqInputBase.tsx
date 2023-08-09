// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { alpha, createTheme } from '@mui/material/styles'
import { useTheme } from '@emotion/react'
import InputBase, { InputBaseProps } from '@mui/material/InputBase'
import React from 'react'

export interface MqInputBaseProps extends InputBaseProps {}

export const MqInputBase: React.FC<MqInputBaseProps> = (props) => {
  const theme = createTheme(useTheme())

  return (
    <InputBase
      {...props}
      sx={{
        ...props.sx,
        borderRadius: theme.spacing(4),
        position: 'relative',
        backgroundColor: 'transparent',
        border: `2px solid ${theme.palette.common.white}`,
        fontSize: 16,
        padding: `${theme.spacing(1)} ${theme.spacing(5)}`,
        transition: theme.transitions.create(['border-color', 'box-shadow']),
        '&:focus': {
          borderColor: theme.palette.primary.main,
          boxShadow: `${alpha(theme.palette.primary.main, 0.25)} 0 0 0 3px`,
          borderRadius: theme.spacing(4),
        },
        '&:hover': {
          borderColor: theme.palette.primary.main,
          boxShadow: `${alpha(theme.palette.primary.main, 0.25)} 0 0 0 3px`,
          '& > label': {
            color: theme.palette.primary.main,
            transition: theme.transitions.create(['color']),
          },
        },
      }}
    />
  )
}

export const MqInputNoIcon: React.FC<InputBaseProps> = (props) => {
  const theme = createTheme(useTheme())

  return (
    <InputBase
      {...props}
      sx={{
        borderRadius: theme.spacing(4),
        position: 'relative',
        backgroundColor: 'transparent',
        border: `2px solid ${theme.palette.common.white}`,
        fontSize: 16,
        padding: `${theme.spacing(1)} 0 ${theme.spacing(1)} ${theme.spacing(1)}`,
        transition: theme.transitions.create(['border-color', 'box-shadow']),
        '&:focus': {
          borderColor: theme.palette.primary.main,
          boxShadow: `${alpha(theme.palette.primary.main, 0.25)} 0 0 0 3px`,
          borderRadius: theme.spacing(4),
        },
        '&:hover': {
          borderColor: theme.palette.primary.main,
          boxShadow: `${alpha(theme.palette.primary.main, 0.25)} 0 0 0 3px`,
          '& > label': {
            color: theme.palette.primary.main,
            transition: theme.transitions.create(['color']),
          },
        },
      }}
    />
  )
}
