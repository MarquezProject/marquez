// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import React, { ChangeEvent, FunctionComponent, SetStateAction, useEffect } from 'react'

import '../../i18n/config'
import * as Redux from 'redux'
import { Box, Button, CircularProgress, Tab, Tabs } from '@mui/material'
import { IState } from '../../store/reducers'
import { LineageJob } from '../lineage/types'
import { Run } from '../../types/api'
import { alpha, createTheme } from '@mui/material/styles'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import {
  deleteJob,
  dialogToggle,
  fetchRuns,
  resetJobs,
  resetRuns,
} from '../../store/actionCreators'
import { jobRunsStatus } from '../../helpers/nodes'
import { useNavigate } from 'react-router-dom'
import { useTheme } from '@emotion/react'
import CloseIcon from '@mui/icons-material/Close'
import Dialog from '../Dialog'
import IconButton from '@mui/material/IconButton'
import MqEmpty from '../core/empty/MqEmpty'
import MqStatus from '../core/status/MqStatus'
import MqText from '../core/text/MqText'
import RunInfo from './RunInfo'
import Runs from './Runs'

interface DispatchProps {
  fetchRuns: typeof fetchRuns
  resetRuns: typeof resetRuns
  resetJobs: typeof resetJobs
  deleteJob: typeof deleteJob
  dialogToggle: typeof dialogToggle
}

type IProps = {
  job: LineageJob
  jobs: IState['jobs']
  runs: Run[]
  runsLoading: boolean
  display: IState['display']
} & DispatchProps

const JobDetailPage: FunctionComponent<IProps> = (props) => {
  const theme = createTheme(useTheme())
  const { job, jobs, fetchRuns, resetRuns, deleteJob, dialogToggle, runs, display, runsLoading } =
    props
  const navigate = useNavigate()

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
      navigate('/')
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
      sx={{
        padding: theme.spacing(2),
      }}
    >
      <Box mb={2} display={'flex'} justifyContent={'space-between'} alignItems={'center'}>
        <Tabs value={tab} onChange={handleChange} textColor='primary' indicatorColor='primary'>
          <Tab label={i18next.t('jobs.latest_tab')} disableRipple={true} />
          <Tab label={i18next.t('jobs.history_tab')} disableRipple={true} />
        </Tabs>
        <Box display={'flex'} alignItems={'center'}>
          <Box mr={1}>
            <Button
              variant='outlined'
              sx={{
                borderColor: theme.palette.error.main,
                color: theme.palette.error.main,
                '&:hover': {
                  borderColor: alpha(theme.palette.error.main, 0.3),
                  backgroundColor: alpha(theme.palette.error.main, 0.3),
                },
              }}
              onClick={() => {
                props.dialogToggle('')
              }}
            >
              {i18next.t('jobs.dialog_delete')}
            </Button>
            <Dialog
              dialogIsOpen={display.dialogIsOpen}
              dialogToggle={dialogToggle}
              title={i18next.t('jobs.dialog_confirmation_title')}
              ignoreWarning={() => {
                deleteJob(job.name, job.namespace)
                props.dialogToggle('')
              }}
            />
          </Box>
          <Box mr={1}>
            <Button
              variant='outlined'
              color='primary'
              target={'_blank'}
              href={job.location}
              disabled={!job.location}
            >
              {i18next.t('jobs.location')}
            </Button>
          </Box>
          <IconButton onClick={() => navigate('/')} size='large'>
            <CloseIcon />
          </IconButton>
        </Box>
      </Box>
      <Box display={'flex'} alignItems={'center'}>
        {runs.length && (
          <Box mr={1}>
            <MqStatus color={jobRunsStatus(runs)} />
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
  jobs: state.jobs,
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      fetchRuns: fetchRuns,
      resetRuns: resetRuns,
      resetJobs: resetJobs,
      deleteJob: deleteJob,
      dialogToggle: dialogToggle,
    },
    dispatch
  )

export default connect(mapStateToProps, mapDispatchToProps)(JobDetailPage)
