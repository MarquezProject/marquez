// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import React, { ReactElement } from 'react'

import { theme } from '../../../helpers/theme'
import Box from '@mui/material/Box'
import ButtonBase from '@mui/material/ButtonBase'

interface OwnProps {
  id: string
  title: string
  children: ReactElement
  active: boolean
}

type IconButtonProps = OwnProps

const MqIconButton: React.FC<IconButtonProps> = ({ id, title, active, children }) => {
  return (
    <Box
      sx={{
        color: 'transparent',
        transition: theme.transitions.create(['color']),
        '&:hover': {
          color: theme.palette.primary.main,
        },
      }}
    >
      <ButtonBase
        id={id}
        disableRipple={true}
        sx={Object.assign(
          {
            width: theme.spacing(8),
            height: theme.spacing(8),
            borderRadius: theme.spacing(2),
            color: theme.palette.secondary.main,
            background: theme.palette.background.default,
            transition: theme.transitions.create(['background-color', 'color']),
            border: '2px solid transparent',
            '&:hover': {
              border: `2px dashed ${theme.palette.primary.main}`,
            },
          },
          active
            ? {
                background: theme.palette.primary.main,
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
        width={theme.spacing(8)}
        sx={{
          fontFamily: 'Karla',
          userSelect: 'none',
        }}
      >
        {title}
      </Box>
    </Box>
  )
}

export default MqIconButton
