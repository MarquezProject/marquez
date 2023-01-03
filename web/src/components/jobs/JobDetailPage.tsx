// SPDX-License-Identifier: Apache-2.0

import React, { ChangeEvent, FunctionComponent, SetStateAction, useEffect } from 'react'

import '../../i18n/config'
import * as Redux from 'redux'
import { alpha } from '@material-ui/core/styles'
import { Box, Button, CircularProgress, Tab, Tabs } from '@material-ui/core'
import { IState } from '../../store/reducers'
import {
  Theme as ITheme,
  WithStyles as IWithStyles,
  createStyles,
  withStyles
} from '@material-ui/core/styles'
import { theme } from '../../helpers/theme'
import { LineageJob } from '../lineage/types'
import { Run } from '../../types/api'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { fetchRuns, resetRuns, resetJobs, deleteJob, dialogToggle } from '../../store/actionCreators'
import { useHistory } from 'react-router-dom'
import CloseIcon from '@material-ui/icons/Close'
import Dialog from '../Dialog'
import IconButton from '@material-ui/core/IconButton'
import MqEmpty from '../core/empty/MqEmpty'
import MqText from '../core/text/MqText'
import RunInfo from './RunInfo'
import RunStatus from './RunStatus'
import Runs from './Runs'

const styles = ({ spacing }: ITheme) => {
  return createStyles({
    root: {
      padding: spacing(2)
    },
    buttonDelete: {
      backgroundColor: theme.palette.error.main,
      color: 'white',
      '&:hover': {
        backgroundColor: alpha(theme.palette.error.main, 0.9)
      },
    }
  })
}

interface DispatchProps {
  fetchRuns: typeof fetchRuns
  resetRuns: typeof resetRuns
  resetJobs: typeof resetJobs
  deleteJob: typeof deleteJob
  dialogToggle: typeof dialogToggle
}

type IProps = IWithStyles<typeof styles> & {
  job: LineageJob
  jobs: IState['jobs']
  runs: Run[]
  runsLoading: boolean
  display: IState['display']
} & DispatchProps

const JobDetailPage: FunctionComponent<IProps> = props => {
  const { job, jobs, classes, fetchRuns, resetRuns, deleteJob, dialogToggle, runs, display, runsLoading } = props
  const history = useHistory()

  const [tab, setTab] = React.useState(0)
  const handleChange = (event: ChangeEvent, newValue: SetStateAction<number>) => {
    setTab(newValue)
  }
  const i18next = require('i18next')

  useEffect(() => {
    fetchRuns(job.name, job.namespace)
  }, [job.name])

  useEffect(() => {
    if (jobs.deletedJobName) {
      history.push('/')
    }
  }, [jobs.deletedJobName])

  // unmounting
  useEffect(() => {
    return () => {
      resetRuns()
      resetJobs()
    }
  }, [])

  if (runsLoading) {
    return (
      <Box display={'flex'} justifyContent={'center'}>
        <CircularProgress color='primary' />
      </Box>
    )
  }

  return (
    <Box
      p={4}
      display='flex'
      flexDirection='column'
      justifyContent='space-between'
      className={classes.root}
    >
      <Box mb={2} display={'flex'} justifyContent={'space-between'} alignItems={'center'}>
        <Tabs value={tab} onChange={handleChange} textColor='primary' indicatorColor='primary'>
          <Tab label={i18next.t('jobs.latest_tab')} disableRipple={true} />
          <Tab label={i18next.t('jobs.history_tab')} disableRipple={true} />
        </Tabs>
        <Box display={'flex'} alignItems={'center'}>
          <Box mr={1}>
            <Button
              color='primary'
              className={classes.buttonDelete}
              onClick={() => {
                props.dialogToggle('')
              }}
            >
              {i18next.t('jobs.delete')}
            </Button>
            <Dialog
              dialogIsOpen={display.dialogIsOpen}
              dialogToggle={dialogToggle}
              title={'Are you sure?'} // i18next.t('jobs.dialogTitleConfirm')
              ignoreWarning={() => {
                deleteJob(job.name, job.namespace)
                props.dialogToggle('')
              }}
            />
          </Box>
          <Box mr={1}>
            <Button variant='outlined' color='primary' target={'_blank'} href={job.location}>
              {i18next.t('jobs.location')}
            </Button>
          </Box>
          <IconButton onClick={() => history.push('/')}>
            <CloseIcon />
          </IconButton>
        </Box>
      </Box>
      <Box display={'flex'} alignItems={'center'}>
        {job.latestRun && (
          <Box mr={1}>
            <RunStatus run={job.latestRun} />
          </Box>
        )}
        <MqText font={'mono'} heading>
          {job.name}
        </MqText>
      </Box>

      <Box mt={1}>
        <MqText subdued>{job.description}</MqText>
      </Box>
      {tab === 0 ? (
        job.latestRun ? (
          <RunInfo run={job.latestRun} />
        ) : (
          !job.latestRun && (
            <MqEmpty title={i18next.t('jobs.empty_title')} body={i18next.t('jobs.empty_body')} />
          )
        )
      ) : null}
      {tab === 1 && <Runs runs={runs} />}
    </Box>
  )
}

const mapStateToProps = (state: IState) => ({
  runs: state.runs.result,
  runsLoading: state.runs.isLoading,
  display: state.display,
  jobs: state.jobs
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      fetchRuns: fetchRuns,
      resetRuns: resetRuns,
      resetJobs: resetJobs,
      deleteJob: deleteJob,
      dialogToggle: dialogToggle
    },
    dispatch
  )

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(withStyles(styles)(JobDetailPage))
