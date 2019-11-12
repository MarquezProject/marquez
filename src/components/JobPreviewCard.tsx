import React, { ReactElement } from 'react'
import {
  withStyles,
  createStyles,
  WithStyles as IWithStyles,
  Theme as ITheme
} from '@material-ui/core/styles'
const globalStyles = require('../global_styles.css')
const { vibrantGreen } = globalStyles
import { Typography, Box } from '@material-ui/core'

import { isoParse, timeFormat } from 'd3-time-format'

import { IJobAPI } from '../types/api'

const styles = ({ palette, spacing }: ITheme) => {
  return createStyles({
    rightCol: {
      textAlign: 'right'
    },
    lastUpdated: {
      color: palette.grey[600]
    },
    status: {
      width: spacing(2),
      height: spacing(2),
      borderRadius: '50%'
    },
    failed: {
      backgroundColor: palette.error.main
    },
    passed: {
      backgroundColor: vibrantGreen
    }
  })
}

type IProps = IWithStyles<typeof styles> &
  Pick<IJobAPI, 'name' | 'description' | 'updatedAt' | 'status'>
interface IState {}

const customTimeFormat = timeFormat('%b %d, %Y %I:%m%p')

const formatUpdatedAt = (updatedAt: string) => {
  const dateString = customTimeFormat(isoParse(updatedAt))
  return `${dateString.slice(0, -2)}${dateString.slice(-2).toLowerCase()}`
}

const StyledTypography = withStyles({
  root: {
    maxWidth: '90%'
  }
})(Typography)

class JobPreviewCard extends React.Component<IProps, IState> {
  render(): ReactElement {
    const { classes, name, description, updatedAt = '', status = 'passed' } = this.props

    return (
      <Box p={2} m={1} bgcolor='white' boxShadow={3} display='flex' justifyContent='space-between'>
        <div>
          <Typography color='secondary' variant='h3'>
            {name}
          </Typography>
          <StyledTypography color='primary'>{description}</StyledTypography>
        </div>
        <Box
          className={classes.rightCol}
          display='flex'
          flexDirection='column'
          alignItems='flex-end'
          justifyContent='space-between'
        >
          <div className={`${classes[status]} ${classes.status}`} />
          <Typography className={classes.lastUpdated}>{formatUpdatedAt(updatedAt)}</Typography>
        </Box>
      </Box>
    )
  }
}

export default withStyles(styles)(JobPreviewCard)
