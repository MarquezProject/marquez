// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import * as Redux from 'redux'
import { ChevronLeftRounded, ChevronRightRounded } from '@mui/icons-material'
import {
  Chip,
  Container,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Tooltip,
  createTheme,
} from '@mui/material'
import { IState } from '../../store/reducers'
import { Job } from '../../types/api'
import { MqScreenLoad } from '../../components/core/screen-load/MqScreenLoad'
import { Nullable } from '../../types/util/Nullable'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { encodeNode, runStateColor } from '../../helpers/nodes'
import { fetchJobs, resetJobs } from '../../store/actionCreators'
import { formatUpdatedAt } from '../../helpers'
import { stopWatchDuration } from '../../helpers/time'
import { useTheme } from '@emotion/react'
import Box from '@mui/material/Box'
import IconButton from '@mui/material/IconButton'
import MqEmpty from '../../components/core/empty/MqEmpty'
import MqStatus from '../../components/core/status/MqStatus'
import MqText from '../../components/core/text/MqText'
import React from 'react'

interface StateProps {
  jobs: Job[]
  isJobsInit: boolean
  isJobsLoading: boolean
  selectedNamespace: Nullable<string>
  totalCount: number
}

interface JobsState {
  page: number
}

interface DispatchProps {
  fetchJobs: typeof fetchJobs
  resetJobs: typeof resetJobs
}

type JobsProps = StateProps & DispatchProps

const PAGE_SIZE = 20

const Jobs: React.FC<JobsProps> = ({
  jobs,
  totalCount,
  isJobsLoading,
  isJobsInit,
  selectedNamespace,
  fetchJobs,
  resetJobs,
}) => {
  const defaultState = {
    page: 0,
  }
  const [state, setState] = React.useState<JobsState>(defaultState)

  const theme = createTheme(useTheme())

  React.useEffect(() => {
    if (selectedNamespace) {
      fetchJobs(selectedNamespace, PAGE_SIZE, state.page * PAGE_SIZE)
    }
  }, [selectedNamespace, state.page])

  React.useEffect(() => {
    return () => {
      // on unmount
      resetJobs()
    }
  }, [])

  const handleClickPage = (direction: 'prev' | 'next') => {
    const directionPage = direction === 'next' ? state.page + 1 : state.page - 1

    fetchJobs(selectedNamespace || '', PAGE_SIZE, directionPage * PAGE_SIZE)
    // reset page scroll
    window.scrollTo(0, 0)
    setState({ ...state, page: directionPage })
  }

  const i18next = require('i18next')
  return (
    <Container maxWidth={'lg'} disableGutters>
      <MqScreenLoad loading={isJobsLoading || !isJobsInit}>
        <>
          {jobs.length === 0 ? (
            <Box p={2}>
              <MqEmpty title={i18next.t('jobs_route.empty_title')}>
                <MqText subdued>{i18next.t('jobs_route.empty_body')}</MqText>
              </MqEmpty>
            </Box>
          ) : (
            <>
              <Box p={2} display={'flex'}>
                <MqText heading>{i18next.t('jobs_route.heading')}</MqText>
                <Chip
                  size={'small'}
                  variant={'outlined'}
                  color={'primary'}
                  sx={{ marginLeft: 1 }}
                  label={totalCount + ' total'}
                ></Chip>
              </Box>
              <Table size='small'>
                <TableHead>
                  <TableRow>
                    <TableCell key={i18next.t('jobs_route.name_col')} align='left'>
                      <MqText subheading>{i18next.t('datasets_route.name_col')}</MqText>
                    </TableCell>
                    <TableCell key={i18next.t('jobs_route.namespace_col')} align='left'>
                      <MqText subheading>{i18next.t('datasets_route.namespace_col')}</MqText>
                    </TableCell>
                    <TableCell key={i18next.t('jobs_route.updated_col')} align='left'>
                      <MqText subheading>{i18next.t('datasets_route.updated_col')}</MqText>
                    </TableCell>
                    <TableCell key={i18next.t('jobs_route.latest_run_col')} align='left'>
                      <MqText subheading>{i18next.t('jobs_route.latest_run_col')}</MqText>
                    </TableCell>
                    <TableCell key={i18next.t('jobs_route.latest_run_state_col')} align='left'>
                      <MqText subheading>{i18next.t('jobs_route.latest_run_state_col')}</MqText>
                    </TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {jobs.map((job) => {
                    return (
                      <TableRow key={job.name}>
                        <TableCell align='left'>
                          <MqText
                            link
                            linkTo={`/lineage/${encodeNode('JOB', job.namespace, job.name)}`}
                          >
                            {job.name}
                          </MqText>
                        </TableCell>
                        <TableCell align='left'>
                          <MqText>{job.namespace}</MqText>
                        </TableCell>
                        <TableCell align='left'>
                          <MqText>{formatUpdatedAt(job.updatedAt)}</MqText>
                        </TableCell>
                        <TableCell align='left'>
                          <MqText>
                            {job.latestRun && job.latestRun.durationMs
                              ? stopWatchDuration(job.latestRun.durationMs)
                              : 'N/A'}
                          </MqText>
                        </TableCell>
                        <TableCell key={i18next.t('jobs_route.latest_run_col')} align='left'>
                          <MqStatus
                            color={job.latestRun && runStateColor(job.latestRun.state || 'NEW')}
                            label={
                              job.latestRun && job.latestRun.state ? job.latestRun.state : 'N/A'
                            }
                          />
                        </TableCell>
                      </TableRow>
                    )
                  })}
                </TableBody>
              </Table>
              <Box display={'flex'} justifyContent={'flex-end'} alignItems={'center'} mb={2}>
                <MqText subdued>
                  <>
                    {PAGE_SIZE * state.page + 1} -{' '}
                    {Math.min(PAGE_SIZE * (state.page + 1), totalCount)} of {totalCount}
                  </>
                </MqText>
                <Tooltip title={i18next.t('events_route.previous_page')}>
                  <span>
                    <IconButton
                      sx={{
                        marginLeft: theme.spacing(2),
                      }}
                      color='primary'
                      disabled={state.page === 0}
                      onClick={() => handleClickPage('prev')}
                      size='large'
                    >
                      <ChevronLeftRounded />
                    </IconButton>
                  </span>
                </Tooltip>
                <Tooltip title={i18next.t('events_route.next_page')}>
                  <span>
                    <IconButton
                      color='primary'
                      onClick={() => handleClickPage('next')}
                      size='large'
                      disabled={state.page === Math.ceil(totalCount / PAGE_SIZE) - 1}
                    >
                      <ChevronRightRounded />
                    </IconButton>
                  </span>
                </Tooltip>
              </Box>
            </>
          )}
        </>
      </MqScreenLoad>
    </Container>
  )
}

const mapStateToProps = (state: IState) => ({
  jobs: state.jobs.result,
  isJobsInit: state.jobs.init,
  isJobsLoading: state.jobs.isLoading,
  selectedNamespace: state.namespaces.selectedNamespace,
  totalCount: state.jobs.totalCount,
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      fetchJobs: fetchJobs,
      resetJobs: resetJobs,
    },
    dispatch
  )

export default connect(mapStateToProps, mapDispatchToProps)(Jobs)
