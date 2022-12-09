// SPDX-License-Identifier: Apache-2.0

import { Theme, createStyles, withStyles } from '@material-ui/core'
import { alpha } from '@material-ui/core/styles'
import InputBase from '@material-ui/core/InputBase'

export const MqInputBase = withStyles((theme: Theme) =>
  createStyles({
    input: {
      borderRadius: theme.spacing(4),
      position: 'relative',
      backgroundColor: 'transparent',
      border: `2px solid ${theme.palette.common.white}`,
      fontSize: 16,
      padding: `${theme.spacing(1)}px ${theme.spacing(5)}px`,
      transition: theme.transitions.create(['border-color', 'box-shadow']),
      '&:focus': {
        borderColor: theme.palette.primary.main,
        boxShadow: `${alpha(theme.palette.primary.main, 0.25)} 0 0 0 3px`,
        borderRadius: theme.spacing(4)
      }
    }
  })
)(InputBase)
