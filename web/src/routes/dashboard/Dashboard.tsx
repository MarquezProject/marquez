import * as Redux from 'redux'
import { Box, Stack } from '@mui/system'
import { Button, ButtonGroup, Container, Divider, Drawer, Grid, Skeleton } from '@mui/material'
import { ChevronRight } from '@mui/icons-material'
import { HEADER_HEIGHT, theme } from '../../helpers/theme'
import { IState } from '../../store/reducers'
import { IntervalMetric } from '../../store/requests/intervalMetrics'
import { Job, RunState } from '../../types/api'
import { LineageMetric } from '../../store/requests/lineageMetrics'
import { MiniGraphContainer } from './MiniGraphContainer'
import { Nullable } from '../../types/util/Nullable'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import {
  fetchDatasetMetrics,
  fetchJobMetrics,
  fetchJobs,
  fetchLineageMetrics,
  fetchSourceMetrics,
} from '../../store/actionCreators'
import { useSearchParams } from 'react-router-dom'
import CircularProgress from '@mui/material/CircularProgress/CircularProgress'
import JobRunItem from './JobRunItem'
import JobsDrawer from './JobsDrawer'
import MQTooltip from '../../components/core/tooltip/MQTooltip'
import MqEmpty from '../../components/core/empty/MqEmpty'
import MqText from '../../components/core/text/MqText'
import React, { useEffect } from 'react'
import SplitButton from '../../components/dashboard/SplitButton'
import StackedLineageEvents from './StackedLineageEvents'

interface StateProps {
  lineageMetrics: LineageMetric[]
  isLineageMetricsLoading: boolean
  jobs: Job[]
  isJobsLoading: boolean
  jobMetrics: IntervalMetric[]
  datasetMetrics: IntervalMetric[]
  sourceMetrics: IntervalMetric[]
  isSourceMetricsLoading: boolean
  isJobMetricsLoading: boolean
  isDatasetMetricsLoading: boolean
}

interface DispatchProps {
  fetchLineageMetrics: typeof fetchLineageMetrics
  fetchJobMetrics: typeof fetchJobMetrics
  fetchDatasetMetrics: typeof fetchDatasetMetrics
  fetchJobs: typeof fetchJobs
  fetchSourceMetrics: typeof fetchSourceMetrics
}

const TIMEFRAMES = ['24 Hours', '7 Days']
type RefreshInterval = '30s' | '5m' | '10m' | 'Never'
const REFRESH_INTERVALS: RefreshInterval[] = ['30s', '5m', '10m', 'Never']

const JOB_RUN_LIMIT = 10

const INTERVAL_TO_MS_MAP: Record<RefreshInterval, number> = {
  '30s': 30000,
  '5m': 300000,
  '10m': 600000,
  Never: 0,
}

const states: { label: RunState; color: string; bgColor: string }[] = [
  { label: 'RUNNING', color: theme.palette.info.main, bgColor: 'secondary' },
  { label: 'COMPLETED', color: theme.palette.primary.main, bgColor: 'primary' },
  { label: 'FAILED', color: theme.palette.error.main, bgColor: 'error' },
  { label: 'ABORTED', color: theme.palette.secondary.main, bgColor: 'secondary' },
]

const Dashboard: React.FC = ({
  lineageMetrics,
  fetchLineageMetrics,
  isLineageMetricsLoading,
  jobs,
  fetchJobs,
  isJobsLoading,
  fetchJobMetrics,
  fetchDatasetMetrics,
  fetchSourceMetrics,
  jobMetrics,
  datasetMetrics,
  sourceMetrics,
  isJobMetricsLoading,
  isDatasetMetricsLoading,
  isSourceMetricsLoading,
}: StateProps & DispatchProps) => {
  const [searchParams, setSearchParams] = useSearchParams()
  const [timeframe, setTimeframe] = React.useState(
    searchParams.get('timeframe') === 'week' ? '7 Days' : '24 Hours'
  )
  const [intervalKey, setIntervalKey] = React.useState<RefreshInterval>('30s')
  const [selectedState, setSelectedState] = React.useState<Nullable<RunState>>(null)
  const [jobsDrawerOpen, setJobsDrawerOpen] = React.useState(false)
  const [timelineOpen, setTimelineOpen] = React.useState(false)

  useEffect(() => {
    const currentSearchParams = searchParams.get('timeframe')
    if (currentSearchParams === 'day' && timeframe !== '24 Hours') {
      setTimeframe('24 Hours')
    } else if (currentSearchParams === 'week' && timeframe !== '7 Days') {
      setTimeframe('7 Days')
    }
  }, [searchParams])

  useEffect(() => {
    if (timeframe === '24 Hours') {
      fetchLineageMetrics('day')
      fetchJobMetrics('day')
      fetchDatasetMetrics('day')
      fetchSourceMetrics('day')
    } else if (timeframe === '7 Days') {
      fetchLineageMetrics('week')
      fetchJobMetrics('week')
      fetchDatasetMetrics('week')
      fetchSourceMetrics('week')
    }
  }, [timeframe])

  useEffect(() => {
    fetchJobs(null, JOB_RUN_LIMIT, 0, selectedState ? selectedState : undefined)
  }, [selectedState])

  useEffect(() => {
    const intervalTime = INTERVAL_TO_MS_MAP[intervalKey]

    if (intervalTime > 0) {
      const intervalId = setInterval(() => {
        const currentSearchParams = searchParams.get('timeframe')
        fetchLineageMetrics(currentSearchParams === 'week' ? 'week' : 'day')
        fetchJobMetrics(currentSearchParams === 'week' ? 'week' : 'day')
        fetchDatasetMetrics(currentSearchParams === 'week' ? 'week' : 'day')
        fetchSourceMetrics(currentSearchParams === 'week' ? 'week' : 'day')
      }, intervalTime)
      return () => clearInterval(intervalId)
    }
    return () => clearInterval(0)
  }, [intervalKey, searchParams])

  const metrics = lineageMetrics.reduce(
    (acc, item) => {
      acc.failed += item.fail
      acc.started += item.start
      acc.completed += item.complete
      acc.aborted += item.abort
      return acc
    },
    { failed: 0, started: 0, completed: 0, aborted: 0 }
  )

  const refresh = () => {
    const currentSearchParams = searchParams.get('timeframe')
    fetchJobs(null, JOB_RUN_LIMIT, 0)
    fetchLineageMetrics(currentSearchParams === 'week' ? 'week' : 'day')
    fetchJobMetrics(currentSearchParams === 'week' ? 'week' : 'day')
    fetchDatasetMetrics(currentSearchParams === 'week' ? 'week' : 'day')
    fetchSourceMetrics(currentSearchParams === 'week' ? 'week' : 'day')
  }

  const { failed, started, completed, aborted } = metrics

  return (
    <>
      <Drawer
        anchor={'right'}
        open={jobsDrawerOpen}
        onClose={() => {
          setJobsDrawerOpen(false)
          fetchJobs(null, JOB_RUN_LIMIT, 0)
        }}
        PaperProps={{
          sx: {
            backgroundColor: theme.palette.background.default,
            backgroundImage: 'none',
            mt: `${HEADER_HEIGHT}px`,
            height: `calc(100vh - ${HEADER_HEIGHT}px)`,
          },
        }}
      >
        <Box>
          <JobsDrawer />
        </Box>
      </Drawer>
      <Drawer
        anchor={'right'}
        open={timelineOpen}
        onClose={() => setTimelineOpen(false)}
        PaperProps={{
          sx: {
            backgroundColor: theme.palette.background.default,
            backgroundImage: 'none',
            mt: `${HEADER_HEIGHT}px`,
            height: `calc(100vh - ${HEADER_HEIGHT}px)`,
          },
        }}
      ></Drawer>
      <Container maxWidth={'lg'}>
        <Box pt={2} mb={2} display={'flex'} justifyContent={'space-between'} alignItems={'center'}>
          <MqText heading>DataOps</MqText>
          <Box display={'flex'}>
            <Box>
              <MqText subdued>REFRESH</MqText>
              <SplitButton
                options={REFRESH_INTERVALS}
                onRefresh={() => refresh()}
                onClick={(option) => {
                  setIntervalKey(option as RefreshInterval)
                }}
              />
            </Box>
            <Divider sx={{ mx: 2 }} orientation={'vertical'} />
            <Box>
              <MqText subdued>TIMEFRAME</MqText>
              <ButtonGroup color={'secondary'} size={'small'} variant='outlined'>
                {TIMEFRAMES.map((tf) => (
                  <Button
                    key={tf}
                    variant={timeframe === tf ? 'contained' : 'outlined'}
                    onClick={() => {
                      setTimeframe(tf)
                      setSearchParams({ timeframe: tf === '7 Days' ? 'week' : 'day' })
                    }}
                  >
                    {tf}
                  </Button>
                ))}
              </ButtonGroup>
            </Box>
          </Box>
        </Box>
        <Box mt={1}>
          <Grid container spacing={2}>
            <Grid item xs={12} md={10}>
              {isLineageMetricsLoading ? (
                <Box>
                  <Skeleton variant={'rectangular'} height={200} />
                  <Box display={'flex'} justifyContent={'space-around'} mt={'4px'}>
                    <Skeleton variant={'rectangular'} width={'15%'} height={36} />
                    <Skeleton variant={'rectangular'} width={'15%'} height={36} />
                    <Skeleton variant={'rectangular'} width={'15%'} height={36} />
                    <Skeleton variant={'rectangular'} width={'15%'} height={36} />
                  </Box>
                </Box>
              ) : (
                <>
                  <StackedLineageEvents lineageMetrics={lineageMetrics} />
                  <Box display={'flex'}>
                    <ButtonGroup size={'small'} fullWidth>
                      {states.map((state) => (
                        <MQTooltip key={state.label} title={state.label}>
                          <Button
                            onClick={() =>
                              selectedState === state.label
                                ? setSelectedState(null)
                                : setSelectedState(state.label)
                            }
                            variant={'text'}
                            color={state.bgColor as any}
                            sx={{
                              height: 40,
                              width: `calc(100% / ${states.length})`,
                              '&:hover': {
                                '.hover-box': {
                                  width: selectedState === state.label ? 80 : 40,
                                },
                              },
                            }}
                          >
                            <Stack>
                              <Box
                                className={'hover-box'}
                                width={selectedState === state.label ? 80 : 20}
                                borderRadius={theme.shape.borderRadius}
                                height={4}
                                bgcolor={state.color}
                                sx={{ transition: '.3s ease-in-out' }}
                              />
                              {(() => {
                                switch (state.label) {
                                  case 'FAILED':
                                    return failed
                                  case 'COMPLETED':
                                    return completed
                                  case 'ABORTED':
                                    return aborted
                                  default:
                                    return started
                                }
                              })()}
                            </Stack>
                          </Button>
                        </MQTooltip>
                      ))}
                    </ButtonGroup>
                  </Box>
                </>
              )}
            </Grid>
            <Grid container item alignItems={'flex-start'} xs={12} md={2} spacing={2}>
              <Grid item xs={4} md={12} style={{ paddingTop: 4 }}>
                <MiniGraphContainer
                  metrics={datasetMetrics}
                  label={'Datasets'}
                  color={theme.palette.info.main}
                  isLoading={isDatasetMetricsLoading}
                />
              </Grid>
              <Grid item xs={4} md={12} style={{ paddingTop: 0 }}>
                <MiniGraphContainer
                  metrics={sourceMetrics}
                  label={'Sources'}
                  color={theme.palette.warning.main}
                  isLoading={isSourceMetricsLoading}
                />
              </Grid>
              <Grid item xs={4} md={12} style={{ paddingTop: 0 }}>
                <MiniGraphContainer
                  metrics={jobMetrics}
                  label={'Jobs'}
                  color={theme.palette.primary.main}
                  isLoading={isJobMetricsLoading}
                />
              </Grid>
            </Grid>
            <Grid item xs={12}>
              <Divider />
            </Grid>
            <Grid item sm={12}>
              <Box
                mr={{
                  xs: 0,
                  md: 0,
                }}
              >
                <Box display={'flex'} justifyContent={'space-between'} alignItems={'center'} mb={1}>
                  <MqText subheading>Jobs</MqText>
                  <Box display={'flex'} alignItems={'center'}>
                    {isJobsLoading && (
                      <CircularProgress sx={{ mr: 2 }} size={16} color={'primary'} />
                    )}
                    <Button
                      disableRipple
                      size={'small'}
                      endIcon={<ChevronRight />}
                      onClick={() => setJobsDrawerOpen(true)}
                    >
                      See More
                    </Button>
                  </Box>
                </Box>
                {jobs.slice(0, JOB_RUN_LIMIT).map((job) => (
                  <JobRunItem key={job.id.namespace + job.id.name} job={job} />
                ))}
                {!isJobsLoading && jobs.length === 0 && (
                  <MqEmpty title={'No jobs found'}>
                    <>
                      <MqText subdued>
                        {
                          'Try changing namespaces, run state, or consulting our documentation to add jobs.'
                        }
                      </MqText>
                    </>
                  </MqEmpty>
                )}
              </Box>
            </Grid>
          </Grid>
        </Box>
      </Container>
    </>
  )
}

const mapStateToProps = (state: IState) => ({
  lineageMetrics: state.lineageMetrics.data,
  isLineageMetricsLoading: state.lineageMetrics.isLoading,
  jobs: state.jobs.result,
  isJobsLoading: state.jobs.isLoading,
  jobMetrics: state.jobMetrics.data,
  isJobMetricsLoading: state.jobMetrics.isLoading,
  datasetMetrics: state.datasetMetrics.data,
  isDatasetMetricsLoading: state.datasetMetrics.isLoading,
  sourceMetrics: state.sourceMetrics.data,
  isSourceMetricsLoading: state.sourceMetrics.isLoading,
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      fetchLineageMetrics: fetchLineageMetrics,
      fetchJobMetrics: fetchJobMetrics,
      fetchSourceMetrics: fetchSourceMetrics,
      fetchDatasetMetrics: fetchDatasetMetrics,
      fetchJobs: fetchJobs,
    },
    dispatch
  )

export default connect(mapStateToProps, mapDispatchToProps)(Dashboard)
