import * as Redux from 'redux'
import { Box } from '@mui/system'
import { IState } from '../../store/reducers'
import { Job } from '../../types/api'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { fetchJobs } from '../../store/actionCreators'
import { theme } from '../../helpers/theme'
import CircularProgress from '@mui/material/CircularProgress/CircularProgress'
import JobRunItem from './JobRunItem'
import MqPaging from '../../components/paging/MqPaging'
import MqText from '../../components/core/text/MqText'
import React, { useEffect } from 'react'
const WIDTH = 800
const PAGE_SIZE = 10

interface StateProps {
  jobs: Job[]
  isJobsLoading: boolean
  jobCount: number
}

interface DispatchProps {
  fetchJobs: typeof fetchJobs
}

type JobsDrawerProps = StateProps & DispatchProps

const JobsDrawer = ({ jobs, isJobsLoading, jobCount, fetchJobs }: JobsDrawerProps) => {
  const [page, setPage] = React.useState<number>(0)

  useEffect(() => {
    fetchJobs(null, PAGE_SIZE, page * PAGE_SIZE)
  }, [page])

  const handleClickPage = (direction: 'prev' | 'next') => {
    const directionPage = direction === 'next' ? page + 1 : page - 1
    fetchJobs(null, PAGE_SIZE, directionPage * PAGE_SIZE)
    setPage(directionPage)
  }

  return (
    <Box width={`${WIDTH}px`}>
      <Box px={2}>
        <Box
          position={'sticky'}
          top={0}
          bgcolor={theme.palette.background.default}
          pt={2}
          zIndex={theme.zIndex.appBar}
          sx={{ borderBottom: 1, borderColor: 'divider', width: '100%' }}
          mb={2}
        >
          <Box display={'flex'} alignItems={'center'} justifyContent={'space-between'} pb={2}>
            <MqText font={'mono'} heading>
              Jobs
            </MqText>
            <MqPaging
              pageSize={PAGE_SIZE}
              currentPage={page}
              totalCount={jobCount}
              incrementPage={() => handleClickPage('next')}
              decrementPage={() => handleClickPage('prev')}
            />
          </Box>
        </Box>
        <Box>
          {jobs.map((job) => (
            <JobRunItem key={job.id.namespace + job.id.name} job={job} />
          ))}
          {isJobsLoading && (
            <Box display={'flex'} justifyContent={'center'}>
              <CircularProgress />
            </Box>
          )}
        </Box>
      </Box>
    </Box>
  )
}
const mapStateToProps = (state: IState) => ({
  jobs: state.jobs.result,
  jobCount: state.jobs.totalCount,
  isJobsLoading: state.jobs.isLoading,
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      fetchJobs: fetchJobs,
    },
    dispatch
  )

export default connect(mapStateToProps, mapDispatchToProps)(JobsDrawer)
