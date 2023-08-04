// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import * as Redux from 'redux'
import { Container, Table, TableBody, TableCell, TableHead, TableRow } from '@mui/material'
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
import Box from '@mui/material/Box'
import MqEmpty from '../../components/core/empty/MqEmpty'
import MqStatus from '../../components/core/status/MqStatus'
import MqText from '../../components/core/text/MqText'
import React from 'react'

interface StateProps {
  jobs: Job[]
  isJobsInit: boolean
  isJobsLoading: boolean
  selectedNamespace: Nullable<string>
}

interface DispatchProps {
  fetchJobs: typeof fetchJobs
  resetJobs: typeof resetJobs
}

type JobsProps = StateProps & DispatchProps

const Jobs: React.FC<JobsProps> = ({
  jobs,
  isJobsLoading,
  isJobsInit,
  selectedNamespace,
  fetchJobs,
  resetJobs,
}) => {
  const mounted = React.useRef<boolean>(false)
  const prevSelectedNamespace = React.useRef<Nullable<string>>()

  React.useEffect(() => {
    if (!mounted.current) {
      // on mount
      if (selectedNamespace) {
        fetchJobs(selectedNamespace)
      }
      mounted.current = true
    } else {
      // on update
      if (prevSelectedNamespace.current !== selectedNamespace && selectedNamespace) {
        fetchJobs(selectedNamespace)
      }

      prevSelectedNamespace.current = selectedNamespace
    }
  })

  React.useEffect(() => {
    return () => {
      // on unmount
      resetJobs()
    }
  }, [])

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
              <Box p={2}>
                <MqText heading>{i18next.t('jobs_route.heading')}</MqText>
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
            </>
          )}
        </>
      </MqScreenLoad>
    </Container>
  )
}

const mapStateToProps = (state: IState) => ({
  isJobsInit: state.jobs.init,
  jobs: state.jobs.result,
  isJobsLoading: state.jobs.isLoading,
  selectedNamespace: state.namespaces.selectedNamespace,
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
