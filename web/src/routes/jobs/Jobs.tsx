// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import * as Redux from 'redux'
import {
  Button,
  Chip,
  Container,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
} from '@mui/material'
import { HEADER_HEIGHT } from '../../helpers/theme'
import { IState } from '../../store/reducers'
import { Job } from '../../types/api'
import { MqScreenLoad } from '../../components/core/screen-load/MqScreenLoad'
import { Nullable } from '../../types/util/Nullable'
import { Refresh } from '@mui/icons-material'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { encodeNode, runStateColor } from '../../helpers/nodes'
import { fetchJobs, resetJobs } from '../../store/actionCreators'
import { formatUpdatedAt } from '../../helpers'
import { stopWatchDuration } from '../../helpers/time'
import { truncateText } from '../../helpers/text'
import { useSearchParams } from 'react-router-dom'
import Box from '@mui/material/Box'
import CircularProgress from '@mui/material/CircularProgress/CircularProgress'
import IconButton from '@mui/material/IconButton'
import MQTooltip from '../../components/core/tooltip/MQTooltip'
import MqEmpty from '../../components/core/empty/MqEmpty'
import MqPaging from '../../components/paging/MqPaging'
import MqStatus from '../../components/core/status/MqStatus'
import MqText from '../../components/core/text/MqText'
import NamespaceSelect from '../../components/namespace-select/NamespaceSelect'
import PageSizeSelector from '../../components/paging/PageSizeSelector'
import React, { useState } from 'react'

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

const JOB_HEADER_HEIGHT = 64

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
  const [pageSize, setPageSize] = useState(20)
  const [currentPage, setCurrentPage] = useState(0)
  const [searchParams, setSearchParams] = useSearchParams()

  const handlePageSizeChange = (newPageSize: number) => {
    const currentOffset = currentPage * pageSize
    const newCurrentPage = Math.floor(currentOffset / newPageSize)

    setPageSize(newPageSize)
    setCurrentPage(newCurrentPage)

    setSearchParams({
      ...Object.fromEntries(searchParams),
      page: newCurrentPage.toString(),
    })

    fetchJobs(selectedNamespace, newPageSize, newCurrentPage * newPageSize)
  }

  const handleClickPage = (direction: 'prev' | 'next') => {
    let directionPage = direction === 'next' ? currentPage + 1 : currentPage - 1

    if (directionPage < 0) {
      directionPage = 0
    }

    setCurrentPage(directionPage)
    setSearchParams({
      ...Object.fromEntries(searchParams),
      page: directionPage.toString(),
    })

    fetchJobs(selectedNamespace, pageSize, directionPage * pageSize)

    window.scrollTo(0, 0)
    setState({ ...state, page: directionPage })
  }

  React.useEffect(() => {
    if (selectedNamespace) {
      fetchJobs(selectedNamespace, pageSize, currentPage * pageSize)
    }
  }, [selectedNamespace, pageSize, currentPage])

  React.useEffect(() => {
    return () => {
      resetJobs()
    }
  }, [])

  const i18next = require('i18next')
  return (
    <Container maxWidth={'xl'} disableGutters>
      <Box p={2} display={'flex'} justifyContent={'space-between'} alignItems={'center'}>
        <Box display={'flex'}>
          <MqText heading>{i18next.t('jobs_route.heading')}</MqText>
          {!isJobsLoading && (
            <Chip
              size={'small'}
              variant={'outlined'}
              color={'primary'}
              sx={{ marginLeft: 1 }}
              label={totalCount + ' total'}
            ></Chip>
          )}
        </Box>
        <Box display='flex' alignItems='center'>
          <Box display='flex' alignItems='center' flexGrow={1}>
            {isJobsLoading && <CircularProgress size={16} />}
            <NamespaceSelect />
          </Box>
          <MQTooltip title='Refresh'>
            <IconButton
              sx={{ ml: 2 }}
              color='primary'
              size='small'
              onClick={() => {
                if (selectedNamespace) {
                  fetchJobs(selectedNamespace, pageSize, state.page * pageSize)
                }
              }}
            >
              <Refresh fontSize='small' />
            </IconButton>
          </MQTooltip>
        </Box>
      </Box>
      <MqScreenLoad
        loading={isJobsLoading && !isJobsInit}
        customHeight={`calc(100vh - ${HEADER_HEIGHT}px - ${JOB_HEADER_HEIGHT}px)`}
      >
        <>
          {jobs.length === 0 ? (
            <Box p={2}>
              <MqEmpty title={i18next.t('jobs_route.empty_title')}>
                <>
                  <MqText subdued>{i18next.t('jobs_route.empty_body')}</MqText>
                  <Button
                    color={'primary'}
                    size={'small'}
                    onClick={() => {
                      if (selectedNamespace) {
                        fetchJobs(selectedNamespace, pageSize, currentPage * pageSize)
                      }
                    }}
                  >
                    Refresh
                  </Button>
                </>
              </MqEmpty>
            </Box>
          ) : (
            <>
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
                            {truncateText(job.name, 170)}
                          </MqText>
                        </TableCell>
                        <TableCell align='left'>
                          <MqText>{truncateText(job.namespace, 40)}</MqText>
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
              <Box
                display='flex'
                alignItems='center'
                justifyContent='flex-end'
                sx={{ marginTop: 2 }}
              >
                <MqPaging
                  pageSize={pageSize}
                  currentPage={currentPage}
                  totalCount={totalCount}
                  incrementPage={() => handleClickPage('next')}
                  decrementPage={() => handleClickPage('prev')}
                />

                <PageSizeSelector onChange={handlePageSizeChange} />
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
