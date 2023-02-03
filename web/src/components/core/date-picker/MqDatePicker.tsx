// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { DateTimePicker } from '@material-ui/pickers'
import { Theme } from '@material-ui/core'
import { alpha } from '@material-ui/core/styles'
import React from 'react'
import createStyles from '@material-ui/core/styles/createStyles'
import withStyles, { WithStyles } from '@material-ui/core/styles/withStyles'

const styles = (theme: Theme) =>
  createStyles({
    root: {
      minWidth: '200px',
      cursor: 'pointer',
      backgroundColor: 'transparent',
      border: `2px solid ${theme.palette.common.white}`,
      padding: `${theme.spacing(1)}px ${theme.spacing(3)}px`,
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
        '& > input': {
          paddingBottom: 0
        }
      }
    }
  })

interface OwnProps {
  value: string
  onChange: (e: any) => void
  label?: string
  format?: string
}

type DatePickerProps = WithStyles<typeof styles> & OwnProps

class MqDatePicker extends React.Component<DatePickerProps> {
  render() {
    const { classes, value, onChange, label = '', format = 'MMM DD yyyy hh:mm a' } = this.props
    return (
      <DateTimePicker
        label={label}
        className={classes.root}
        value={value}
        onChange={onChange}
        format={format}
      />
    )
  }
}

export default withStyles(styles)(MqDatePicker)
