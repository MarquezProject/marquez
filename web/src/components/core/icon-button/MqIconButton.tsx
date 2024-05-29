// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import React, { ReactElement } from 'react'

import { Link as RouterLink } from 'react-router-dom'
import { THEME_EXTRA, theme } from '../../../helpers/theme'
import { lighten } from '@mui/material'
import Box from '@mui/material/Box'
import ButtonBase from '@mui/material/ButtonBase'

interface OwnProps {
  id: string
  title: string
  children: ReactElement
  active: boolean
  to: string
}

type IconButtonProps = OwnProps

const MqIconButton: React.FC<IconButtonProps> = ({ id, title, active, children, to }) => {
  return (
    <Box
      sx={{
        color: 'transparent',
        transition: theme.transitions.create(['color']),
        '&:hover': {
          color: THEME_EXTRA.typography.subdued,
        },
      }}
    >
      <ButtonBase
        id={id}
        component={RouterLink}
        to={to}
        disableRipple={true}
        sx={Object.assign(
          {
            width: theme.spacing(6),
            height: theme.spacing(6),
            borderRadius: theme.spacing(1),
            color: theme.palette.secondary.main,
            transition: theme.transitions.create(['background-color', 'color']),
            border: '2px solid transparent',
          },
          active
            ? {
                background: lighten(theme.palette.background.default, 0.05),
                color: theme.palette.common.white,
              }
            : {}
        )}
      >
        {children}
      </ButtonBase>
      <Box
        display={'flex'}
        justifyContent={'center'}
        sx={{
          fontFamily: 'Karla',
          userSelect: 'none',
          fontSize: '.625rem',
        }}
      >
        {title}
      </Box>
    </Box>
  )
}

export default MqIconButton
