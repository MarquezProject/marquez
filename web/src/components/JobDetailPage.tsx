import React, { FunctionComponent, useEffect } from 'react'

import * as Redux from 'redux'
import { Box, Button, Tooltip } from '@material-ui/core'
import { IState } from '../reducers'
import {
  Theme as ITheme,
  WithStyles as IWithStyles,
  createStyles,
  withStyles
} from '@material-ui/core/styles'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { fetchJobRuns } from '../actionCreators'
import { useHistory, useParams } from 'react-router-dom'
import CloseIcon from '@material-ui/icons/Close'
import _find from 'lodash/find'
const globalStyles = require('../global_styles.css')
const { jobRunNew, jobRunFailed, jobRunCompleted, jobRunAborted, jobRunRunning } = globalStyles
import { IJob } from '../types'
import { formatUpdatedAt } from '../helpers'
import Code from './core/code/Code'
import IconButton from '@material-ui/core/IconButton'
import MqText from './core/text/MqText'

const colorMap = {
  NEW: jobRunNew,
  FAILED: jobRunFailed,
  COMPLETED: jobRunCompleted,
  ABORTED: jobRunAborted,
  RUNNING: jobRunRunning
}

const styles = ({ palette, spacing }: ITheme) => {
  return createStyles({
    root: {
      padding: spacing(2)
    },
    _status: {
      gridArea: 'status',
      width: spacing(2),
      height: spacing(2),
      borderRadius: '50%'
    },
    squareShape: {
      width: spacing(2),
      height: spacing(2),
      marginLeft: '5px',
      borderRadius: '50%'
    },
    lastUpdated: {
      color: palette.grey[600],
      padding: '0px 0px 5px 5px'
    },
    latestRunContainer: {
      float: 'right',
      display: 'flex'
    },
    failed: {
      backgroundColor: jobRunFailed
    },
    passed: {
      backgroundColor: jobRunCompleted
    },
    copyToClipboard: {
      position: 'absolute',
      bottom: '1rem',
      right: '1rem',
      cursor: 'pointer'
    }
  })
}

type IProps = IWithStyles<typeof styles> & { jobs: IJob[] } & { fetchJobRuns: any }

const JobDetailPage: FunctionComponent<IProps> = props => {
  const { jobs, classes, fetchJobRuns } = props

  const { jobName } = useParams()
  const history = useHistory()

  const job = _find(jobs, j => j.name === jobName)

  // useEffect hook to run on any change to jobName
  useEffect(() => {
    job ? fetchJobRuns(job.name, job.namespace) : null
  }, [jobs.length])

  if (!job || jobs.length == 0) {
    return (
      <Box
        p={2}
        display='flex'
        flexDirection='column'
        justifyContent='space-between'
        className={classes.root}
      >
        <MqText>
          No job by the name of <MqText bold>{`"${jobName}"`}</MqText> found
        </MqText>
      </Box>
    )
  }

  const { root, _status } = classes

  const {
    name,
    description,
    updatedAt = '',
    latestRun,
    location,
    namespace,
    context = { SQL: '' }
  } = job as IJob

  const latestRuns = job ? job.latestRuns || [] : []
  const { SQL } = context

  return (
    <Box
      p={4}
      display='flex'
      flexDirection='column'
      justifyContent='space-between'
      className={root}
    >
      <Box>
        <Box mb={1}>
          {latestRun && (
            <Tooltip title={latestRun.state} placement='top'>
              {latestRun && (
                <div
                  className={`${_status}`}
                  style={{ backgroundColor: colorMap[latestRun.state] }}
                />
              )}
            </Tooltip>
          )}
        </Box>
        <Box mb={2} display={'flex'} justifyContent={'space-between'} alignItems={'center'}>
          <MqText font={'mono'} heading>
            {namespace} / {name}
          </MqText>
          <Box display={'flex'} alignItems={'center'}>
            <Box mr={1}>
              <Button variant='outlined' color='primary' target={'_blank'} href={location}>
                Location
              </Button>
            </Box>
            <IconButton onClick={() => history.push('/')}>
              <CloseIcon />
            </IconButton>
          </Box>
        </Box>
        <Box mb={2}>
          <MqText subdued>{description}</MqText>
        </Box>
      </Box>
      <Code>{SQL}</Code>
      <Box display={'flex'} justifyContent={'flex-end'} alignItems={'center'} mt={1}>
        <div className={classes.latestRunContainer}>
          {latestRuns.map(r => {
            return (
              <Tooltip key={r.id} title={r.state} placement='top'>
                <div
                  key={r.id}
                  className={classes.squareShape}
                  style={{ backgroundColor: colorMap[r.state] }}
                />
              </Tooltip>
            )
          })}
        </div>
        <Box ml={1}>
          <MqText subdued>{formatUpdatedAt(updatedAt)}</MqText>
        </Box>
      </Box>
    </Box>
  )
}

const mapStateToProps = (state: IState) => ({
  jobs: state.jobs
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      fetchJobRuns: fetchJobRuns
    },
    dispatch
  )

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(withStyles(styles)(JobDetailPage))
