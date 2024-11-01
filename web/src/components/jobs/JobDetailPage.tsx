// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import React, { ChangeEvent, FunctionComponent, useEffect } from 'react'

import '../../i18n/config'
import * as Redux from 'redux'
import { Alert, Job, Run } from '../../types/api'
import {
  Box,
  CircularProgress,
  Divider,
  Tab,
  Tabs,
  ToggleButton,
  ToggleButtonGroup,
} from '@mui/material'
import {
  Cancel,
  CheckCircle,
  MoreVert,
} from '@mui/icons-material'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { IState } from '../../store/reducers'
import { LineageJob } from '../../types/lineage'
import { MqInfoHorizontal } from '../core/info/MqInfo'
import { Nullable } from '../../types/util/Nullable'
import { createTheme } from '@mui/material/styles'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import {
  deleteAlert,
  deleteJob,
  dialogToggle,
  fetchAlerts,
  fetchJob,
  fetchLatestRuns,
  resetJobs,
  resetRuns,
  setTabIndex,
  updateAlert,
} from '../../store/actionCreators'
import { faCog } from '@fortawesome/free-solid-svg-icons/faCog'
import { formatUpdatedAt } from '../../helpers'
import { runStateColor } from '../../helpers/nodes'
import { stopWatchDuration } from '../../helpers/time'
import {truncateText, truncateTextFront} from '../../helpers/text'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useTheme } from '@emotion/react'
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
  fetchAlerts: typeof fetchAlerts
  updateAlert: typeof updateAlert
  deleteAlert: typeof deleteAlert
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
  alerts: Alert[]
  isAlertsLoading: boolean
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
    alerts,
    fetchAlerts,
    updateAlert,
    deleteAlert,
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
    if (job) {
      fetchAlerts(job.uuid, 'job')
    }
  }, [job])

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

  const failAlert = alerts.find((alert) => alert.entityType === 'job' && alert.type === 'FAIL')
  const successAlert = alerts.find(
    (alert) => alert.entityType === 'job' && alert.type === 'COMPLETE'
  )

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
              <MQTooltip title={job.name}>
                <Box>
                  <MqText font={'mono'} subheading>
                    {truncateTextFront(job.name, 28)}
                  </MqText>
                </Box>
              </MQTooltip>
            </Box>
            {job.description && (
              <Box mt={1}>
                <MqText subdued>{job.description}</MqText>
              </Box>
            )}
          </Box>
          <Box display={'flex'} alignItems={'center'}>
            <IconButton size={'small'}>
              <MoreVert fontSize={'small'} />
            </IconButton>
            {/*<Box mr={1}>*/}
            {/*  <Button*/}
            {/*    variant='outlined'*/}
            {/*    size={'small'}*/}
            {/*    sx={{*/}
            {/*      borderColor: theme.palette.error.main,*/}
            {/*      color: theme.palette.error.main,*/}
            {/*      '&:hover': {*/}
            {/*        borderColor: alpha(theme.palette.error.main, 0.3),*/}
            {/*        backgroundColor: alpha(theme.palette.error.main, 0.3),*/}
            {/*      },*/}
            {/*    }}*/}
            {/*    onClick={() => {*/}
            {/*      props.dialogToggle('')*/}
            {/*    }}*/}
            {/*  >*/}
            {/*    {i18next.t('jobs.dialog_delete')}*/}
            {/*  </Button>*/}
            {/*  <Dialog*/}
            {/*    dialogIsOpen={display.dialogIsOpen}*/}
            {/*    dialogToggle={dialogToggle}*/}
            {/*    title={i18next.t('jobs.dialog_confirmation_title')}*/}
            {/*    ignoreWarning={() => {*/}
            {/*      deleteJob(job.name, job.namespace)*/}
            {/*      props.dialogToggle('')*/}
            {/*    }}*/}
            {/*  />*/}
            {/*</Box>*/}
            {/*<Box mr={1}>*/}
            {/*  <Button*/}
            {/*    size={'small'}*/}
            {/*    variant='outlined'*/}
            {/*    color='primary'*/}
            {/*    target={'_blank'}*/}
            {/*    href={job.location}*/}
            {/*    disabled={!job.location}*/}
            {/*  >*/}
            {/*    {i18next.t('jobs.location')}*/}
            {/*  </Button>*/}
            {/*</Box>*/}
            {/*<IconButton onClick={() => setSearchParams({})} size='small'>*/}
            {/*  <CloseIcon fontSize={'small'} />*/}
            {/*</IconButton>*/}
          </Box>
        </Box>
      </Box>
      <MqInfoHorizontal label={'Created at'.toUpperCase()} value={formatUpdatedAt(job.createdAt)} />
      <MqInfoHorizontal label={'Updated at'.toUpperCase()} value={formatUpdatedAt(job.updatedAt)} />
      <MqInfoHorizontal label={'Last Runtime'.toUpperCase()} value={lastRuntime} />
      <MqInfoHorizontal
        label={'Notifications'.toUpperCase()}
        value={
          <ToggleButtonGroup exclusive size={'small'}>
            <MQTooltip title={'Notify on Failure'}>
              <ToggleButton
                value='check'
                size={'small'}
                color={'error'}
                selected={!!failAlert}
                onChange={() =>
                  !failAlert ? updateAlert(job.uuid, 'job', 'FAIL') : deleteAlert(failAlert.uuid)
                }
              >
                <Cancel fontSize={'small'} />
              </ToggleButton>
            </MQTooltip>
            <MQTooltip title={'Notify on Success'}>
              <ToggleButton
                value='check'
                size={'small'}
                color={'primary'}
                selected={!!successAlert}
                onChange={() =>
                  !successAlert
                    ? updateAlert(job.uuid, 'job', 'COMPLETE')
                    : deleteAlert(successAlert.uuid)
                }
              >
                <CheckCircle fontSize={'small'} />
              </ToggleButton>
            </MQTooltip>
          </ToggleButtonGroup>
        }
      />
      <MqInfoHorizontal
        label={'Last Started'.toUpperCase()}
        value={job.latestRun ? formatUpdatedAt(job.latestRun.startedAt) : 'N/A'}
      />
      <MqInfoHorizontal label={'Last Finished'.toUpperCase()} value={lastFinished} />
      <MqInfoHorizontal
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
      <MqInfoHorizontal
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
      <JobTags jobTags={job.tags} jobName={job.name} namespace={job.namespace} />
      <Box
        mb={2}
        display={'flex'}
        justifyContent={'space-between'}
        alignItems={'center'}
        sx={{ borderBottom: 1, borderColor: 'divider', width: '100%' }}
      >
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
  alerts: state.alerts.alerts,
  isAlertsLoading: state.alerts.isLoading,
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      fetchAlerts: fetchAlerts,
      updateAlert: updateAlert,
      deleteAlert: deleteAlert,
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
