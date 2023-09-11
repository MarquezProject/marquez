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
import { DataGrid, GridColDef } from '@mui/x-data-grid'

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

  const columns: GridColDef[] = [
    {
      field: 'name',
      headerName: i18next.t('jobs_route.name_col'),
      renderCell: (params) => (<MqText
        link
        linkTo={`/lineage/${encodeNode('JOB', params.row.namespace, params.row.name)}`}
      >
        {params.row.name}
      </MqText>),
      flex: 2,
      editable: false
    },
    { field: 'namespace', headerName: i18next.t('jobs_route.namespace_col'), flex: 1, editable: false },
    {
      field: 'updatedAt',
      headerName: i18next.t('jobs_route.updated_col'),
      renderCell: (params) => (<MqText>{formatUpdatedAt(params.row.updatedAt)}</MqText>),
      flex: 1,
      editable: false
    },
    {
      field: 'latestRun',
      headerName: i18next.t('jobs_route.latest_run_col'),
      renderCell: (params) => (<MqText>
        {params.row.latestRun && params.row.latestRun.durationMs
          ? stopWatchDuration(params.row.latestRun.durationMs)
          : 'N/A'}
      </MqText>),
      flex: 1,
      editable: false
    },
    {
      field: 'runState',
      headerName: i18next.t('jobs_route.latest_run_state_col'),
      renderCell: (params) => (<MqStatus
        color={params.row.latestRun && runStateColor(params.row.latestRun.state || 'NEW')}
        label={params.row.latestRun && params.row.latestRun.state ? params.row.latestRun.state : 'N/A'}
      />),
      flex: 1,
      editable: false,
    },
  ]

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
                <DataGrid
                  rows={jobs}
                  columns={columns}
                  getRowId={(row) => JSON.stringify(row.id)}
                  disableRowSelectionOnClick
                />
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
