// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import React, { ChangeEvent, FunctionComponent, useEffect } from 'react'

import '../../i18n/config'
import * as Redux from 'redux'
import { Box, Button, CircularProgress, Divider, Grid, Tab, Tabs } from '@mui/material'
import { CalendarIcon } from '@mui/x-date-pickers'
import {
  DirectionsRun,
  EscalatorWarning,
  Speed,
  SportsScore,
  Start,
  Title,
} from '@mui/icons-material'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { IState } from '../../store/reducers'
import { Job, Run } from '../../types/api'
import { LineageJob } from '../../types/lineage'
import { MqInfo } from '../core/info/MqInfo'
import { Nullable } from '../../types/util/Nullable'
import { alpha, createTheme } from '@mui/material/styles'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import {
  deleteJob,
  dialogToggle,
  fetchJob,
  fetchLatestRuns,
  resetJobs,
  resetRuns,
  setTabIndex,
} from '../../store/actionCreators'
import { faCog } from '@fortawesome/free-solid-svg-icons/faCog'
import { formatUpdatedAt } from '../../helpers'
import { runStateColor } from '../../helpers/nodes'
import { stopWatchDuration } from '../../helpers/time'
import { truncateText } from '../../helpers/text'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useTheme } from '@emotion/react'
import CloseIcon from '@mui/icons-material/Close'
import Dialog from '../Dialog'
import IconButton from '@mui/material/IconButton'
import JobTags from './JobTags'
import MQTooltip from '../core/tooltip/MQTooltip'
import MqEmpty from '../core/empty/MqEmpty'
import MqStatus from '../core/status/MqStatus'
import MqText from '../core/text/MqText'
import RunInfo from './RunInfo'
import Runs from './Runs'

interface DispatchProps {
  fetchLatestRuns: typeof fetchLatestRuns
  resetRuns: typeof resetRuns
  resetJobs: typeof resetJobs
  deleteJob: typeof deleteJob
  dialogToggle: typeof dialogToggle
  setTabIndex: typeof setTabIndex
  fetchJob: typeof fetchJob
}

type IProps = {
  lineageJob: LineageJob
  job: Nullable<Job>
  isJobLoading: boolean
  jobs: IState['jobs']
  display: IState['display']
  tabIndex: IState['lineage']['tabIndex']
  latestRuns: Run[]
  isLatestRunsLoading: boolean
} & DispatchProps

const JobDetailPage: FunctionComponent<IProps> = (props) => {
  const theme = createTheme(useTheme())
  const {
    job,
    isJobLoading,
    lineageJob,
    jobs,
    fetchLatestRuns,
    resetRuns,
    deleteJob,
    dialogToggle,
    display,
    tabIndex,
    setTabIndex,
    fetchJob,
    isLatestRunsLoading,
  } = props
  const navigate = useNavigate()
  const [_, setSearchParams] = useSearchParams()

  const handleChange = (_: ChangeEvent, newValue: number) => {
    setTabIndex(newValue)
  }

  const i18next = require('i18next')

  useEffect(() => {
    fetchJob(lineageJob.namespace, lineageJob.name)
    fetchLatestRuns(lineageJob.name, lineageJob.namespace)
  }, [lineageJob.name])

  useEffect(() => {
    if (jobs.deletedJobName) {
      navigate('/')
    }
  }, [jobs.deletedJobName])

  // unmounting
  useEffect(() => {
    return () => {
      resetJobs()
      resetRuns()
      setTabIndex(0)
    }
  }, [])

  if (!job || isJobLoading || isLatestRunsLoading) {
    return (
      <Box display={'flex'} justifyContent={'center'} mt={2}>
        <CircularProgress color='primary' />
      </Box>
    )
  }

  const lastFinished = (() => {
    const last = job.latestRuns?.find((run) => run.state !== 'RUNNING')
    return last ? formatUpdatedAt(last.endedAt) : 'N/A'
  })()

  const lastRuntime = (() => {
    const last = job.latestRuns?.find((run) => run.state !== 'RUNNING')
    return last ? stopWatchDuration(last.durationMs) : 'N/A'
  })()

  return (
    <Box px={2} display='flex' flexDirection='column' justifyContent='space-between'>
      <Box
        position={'sticky'}
        top={0}
        bgcolor={theme.palette.background.default}
        py={2}
        zIndex={theme.zIndex.appBar}
        sx={{ borderBottom: 1, borderColor: 'divider', width: '100%' }}
        mb={2}
      >
        <Box display={'flex'} alignItems={'center'} justifyContent={'space-between'}>
          <Box>
            <Box display={'flex'} alignItems={'center'}>
              <Box
                mr={2}
                borderRadius={theme.spacing(1)}
                p={1}
                width={32}
                height={32}
                display={'flex'}
                bgcolor={theme.palette.primary.main}
              >
                <FontAwesomeIcon
                  aria-hidden={'true'}
                  title={'Job'}
                  icon={faCog}
                  width={16}
                  height={16}
                  color={theme.palette.common.white}
                />
              </Box>
              <MqText font={'mono'} heading>
                {truncateText(job.name, 40)}
              </MqText>
            </Box>
            {job.description && (
              <Box mt={1}>
                <MqText subdued>{job.description}</MqText>
              </Box>
            )}
          </Box>
          <Box display={'flex'} alignItems={'center'}>
            <Box mr={1}>
              <Button
                variant='outlined'
                size={'small'}
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
                size={'small'}
                variant='outlined'
                color='primary'
                target={'_blank'}
                href={job.location}
                disabled={!job.location}
              >
                {i18next.t('jobs.location')}
              </Button>
            </Box>
            <IconButton onClick={() => setSearchParams({})} size='small'>
              <CloseIcon fontSize={'small'} />
            </IconButton>
          </Box>
        </Box>
      </Box>
      <Grid container spacing={2}>
        <Grid item xs={3}>
          <MqInfo
            icon={<CalendarIcon color={'disabled'} />}
            label={'Created at'.toUpperCase()}
            value={formatUpdatedAt(job.createdAt)}
          />
        </Grid>
        <Grid item xs={3}>
          <MqInfo
            icon={<CalendarIcon color={'disabled'} />}
            label={'Updated at'.toUpperCase()}
            value={formatUpdatedAt(job.updatedAt)}
          />
        </Grid>
        <Grid item xs={3}>
          <MqInfo
            icon={<Speed color={'disabled'} />}
            label={'Last Runtime'.toUpperCase()}
            value={lastRuntime}
          />
        </Grid>
        <Grid item xs={3}>
          <MqInfo
            icon={<Title color={'disabled'} />}
            label={'Type'.toUpperCase()}
            value={job.type ? job.type : 'N/A'}
          />
        </Grid>
        <Grid item xs={3}>
          <MqInfo
            icon={<Start color={'disabled'} />}
            label={'Last Started'.toUpperCase()}
            value={job.latestRun ? formatUpdatedAt(job.latestRun.startedAt) : 'N/A'}
          />
        </Grid>
        <Grid item xs={3}>
          <MqInfo
            icon={<SportsScore color={'disabled'} />}
            label={'Last Finished'.toUpperCase()}
            value={lastFinished}
          />
        </Grid>
        <Grid item xs={3}>
          <MqInfo
            icon={<DirectionsRun color={'disabled'} />}
            label={'Running Status'.toUpperCase()}
            value={
              <MqStatus
                label={job.latestRun?.state || 'N/A'}
                color={
                  job.latestRun?.state
                    ? runStateColor(job.latestRun.state)
                    : theme.palette.secondary.main
                }
              />
            }
          />
        </Grid>
        <Grid item xs={3}>
          <MqInfo
            icon={<EscalatorWarning color={'disabled'} />}
            label={'Parent Job'.toUpperCase()}
            value={
              job.parentJobName ? (
                <MQTooltip title={job.parentJobName}>
                  <>{truncateText(job.parentJobName, 16)}</>
                </MQTooltip>
              ) : (
                'N/A'
              )
            }
          />
        </Grid>
      </Grid>
      <Divider sx={{ my: 1 }} />
      <JobTags jobTags={job.tags} jobName={job.name} namespace={job.namespace} />
      <Box
        mb={2}
        display={'flex'}
        justifyContent={'space-between'}
        alignItems={'center'}
        sx={{ borderBottom: 1, borderColor: 'divider', width: '100%' }}
      >
        <Tabs value={tabIndex} onChange={handleChange} textColor='primary' indicatorColor='primary'>
          <Tab label={i18next.t('jobs.latest_tab')} disableRipple={true} />
          <Tab label={i18next.t('jobs.history_tab')} disableRipple={true} />
        </Tabs>
      </Box>
      {tabIndex === 0 ? (
        job.latestRun ? (
          <RunInfo run={job.latestRun} />
        ) : (
          !job.latestRun && (
            <MqEmpty title={i18next.t('jobs.empty_title')} body={i18next.t('jobs.empty_body')} />
          )
        )
      ) : null}
      {tabIndex === 1 && <Runs jobName={job.name} jobNamespace={job.namespace} />}
    </Box>
  )
}

const mapStateToProps = (state: IState) => ({
  latestRuns: state.runs.latestRuns,
  isLatestRunsLoading: state.runs.isLatestRunsLoading,
  display: state.display,
  jobs: state.jobs,
  tabIndex: state.lineage.tabIndex,
  job: state.job.result,
  isJobLoading: state.job.isLoading,
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      fetchLatestRuns: fetchLatestRuns,
      resetRuns: resetRuns,
      resetJobs: resetJobs,
      deleteJob: deleteJob,
      dialogToggle: dialogToggle,
      setTabIndex: setTabIndex,
      fetchJob: fetchJob,
    },
    dispatch
  )

export default connect(mapStateToProps, mapDispatchToProps)(JobDetailPage)
