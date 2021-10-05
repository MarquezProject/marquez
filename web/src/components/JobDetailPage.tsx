import React, { FunctionComponent, useEffect } from 'react'

import * as Redux from 'redux'
import { Box, Button, Tooltip } from '@material-ui/core'
import { IState } from '../store/reducers'
import {
  Theme as ITheme,
  WithStyles as IWithStyles,
  createStyles,
  withStyles
} from '@material-ui/core/styles'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { useHistory, useParams } from 'react-router-dom'
import CloseIcon from '@material-ui/icons/Close'
const globalStyles = require('../global_styles.css')
const { jobRunNew, jobRunFailed, jobRunCompleted, jobRunAborted, jobRunRunning } = globalStyles
import { IJob } from '../types'
import { LineageJob } from './lineage/types'
import { Run } from '../types/api'
import { fetchRuns } from '../store/actionCreators'
import { formatUpdatedAt } from '../helpers'
import IconButton from '@material-ui/core/IconButton'
import MqCode from './core/code/MqCode'
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

interface DispatchProps {
  fetchRuns: typeof fetchRuns
}

type IProps = IWithStyles<typeof styles> & { job: LineageJob; runs: Run[] } & DispatchProps

const JobDetailPage: FunctionComponent<IProps> = props => {
  const { job, classes } = props

  const { jobName } = useParams()
  const history = useHistory()
  fetchRuns(job.name, job.namespace)
  console.log(props)

  useEffect(() => {
    console.log('fetch runs')
    fetchRuns(job.name, job.namespace)
  }, [job.name])

  if (!job) {
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
    context = { sql: '' }
  } = job as IJob

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
      <MqCode code={context.sql} />
      <Box display={'flex'} justifyContent={'flex-end'} alignItems={'center'} mt={1}>
        <Box ml={1}>
          <MqText subdued>{formatUpdatedAt(updatedAt)}</MqText>
        </Box>
      </Box>
    </Box>
  )
}

const mapStateToProps = (state: IState) => ({
  runs: state.runs.result
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      fetchRuns: fetchRuns
    },
    dispatch
  )

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(withStyles(styles)(JobDetailPage))
