import React, { ReactElement } from 'react'
import {
  withStyles,
  createStyles,
  WithStyles as IWithStyles,
  Theme as ITheme
} from '@material-ui/core/styles'
import { Link } from 'react-router-dom'

const globalStyles = require('../global_styles.css')
const { vibrantGreen } = globalStyles
import { Typography, Box, Tooltip } from '@material-ui/core'
import { formatUpdatedAt } from '../helpers'

import { IJobAPI } from '../types/api'

const { jobRunNew, jobRunFailed, jobRunCompleted, jobRunAborted, jobRunRunning } = globalStyles

const colorMap = {
  NEW: jobRunNew,
  FAILED: jobRunFailed,
  COMPLETED: jobRunCompleted,
  ABORTED: jobRunAborted,
  RUNNING: jobRunRunning
}

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
    },
    link: {
      textDecoration: 'none'
    }
  })
}

type IProps = IWithStyles<typeof styles> &
  Pick<IJobAPI, 'name' | 'description' | 'updatedAt' | 'latestRun'>
interface IState {}

const StyledTypography = withStyles({
  root: {
    maxWidth: '90%'
  }
})(Typography)

class JobPreviewCard extends React.Component<IProps, IState> {
  render(): ReactElement {
    const { classes, name, description, updatedAt = '', latestRun } = this.props

    return (
      <Link to={`/jobs/${name}`} className={classes.link}>
        <Box
          p={2}
          m={1}
          bgcolor='white'
          boxShadow={3}
          display='flex'
          justifyContent='space-between'
        >
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
            <Tooltip className="tagWrapper" title={latestRun.runState} placement="top">
              <div className={classes.status} style={{backgroundColor: colorMap[latestRun.runState]}} />
            </Tooltip>
            <Typography className={classes.lastUpdated}>{formatUpdatedAt(updatedAt)}</Typography>
          </Box>
        </Box>
      </Link>
    )
  }
}

export default withStyles(styles)(JobPreviewCard)
