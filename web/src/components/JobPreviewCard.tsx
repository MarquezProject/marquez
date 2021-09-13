import React, { ReactElement } from 'react'

import {
  Theme as ITheme,
  WithStyles as IWithStyles,
  alpha,
  createStyles,
  withStyles
} from '@material-ui/core/styles'
import { Link } from 'react-router-dom'
const globalStyles = require('../global_styles.css')
const { vibrantGreen } = globalStyles
import { Box, Tooltip } from '@material-ui/core'
import { Job } from '../types/api'
import { formatUpdatedAt } from '../helpers'
import MqText from './core/text/MqText'
import transitions from '@material-ui/core/styles/transitions'

const { jobRunNew, jobRunFailed, jobRunCompleted, jobRunAborted, jobRunRunning } = globalStyles

const colorMap = {
  NEW: jobRunNew,
  FAILED: jobRunFailed,
  COMPLETED: jobRunCompleted,
  ABORTED: jobRunAborted,
  RUNNING: jobRunRunning
}

const styles = ({ palette, spacing, shape }: ITheme) => {
  return createStyles({
    rightCol: {
      textAlign: 'right'
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
      textDecoration: 'none',
      border: `1px solid ${palette.secondary.main}`,
      display: 'block',
      marginBottom: spacing(2),
      borderRadius: shape.borderRadius,
      transition: transitions.create(['background-color']),
      '&:hover': {
        backgroundColor: alpha(palette.common.white, 0.1)
      }
    }
  })
}

type IProps = IWithStyles<typeof styles> &
  Pick<Job, 'name' | 'description' | 'updatedAt' | 'latestRun'> & {
    setSelectedNode: (payload: string) => void
  }

interface IState {}

class JobPreviewCard extends React.Component<IProps, IState> {
  render(): ReactElement {
    const { classes, name, description, updatedAt = '', latestRun, setSelectedNode } = this.props
    return (
      <Link to={`/jobs/${name}`} className={classes.link} onClick={() => setSelectedNode(name)}>
        <Box p={2} display='flex' justifyContent='space-between'>
          <div>
            <MqText subheading font={'mono'}>
              {name}
            </MqText>
            <Box mt={1} maxWidth={'80%'}>
              <MqText subdued>{description}</MqText>
            </Box>
          </div>
          <Box
            className={classes.rightCol}
            display='flex'
            flexDirection='column'
            alignItems='flex-end'
            justifyContent='space-between'
          >
            {latestRun && (
              <Tooltip className='tagWrapper' title={latestRun.state} placement='top'>
                <div
                  className={classes.status}
                  style={{ backgroundColor: colorMap[latestRun.state] }}
                />
              </Tooltip>
            )}
            <Box mt={1}>
              <MqText subdued>{formatUpdatedAt(updatedAt)}</MqText>
            </Box>
          </Box>
        </Box>
      </Link>
    )
  }
}

export default withStyles(styles)(JobPreviewCard)
