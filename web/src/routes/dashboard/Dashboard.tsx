import * as Redux from 'redux'
import { Box, Stack } from '@mui/system'
import { Button, ButtonGroup, Container, Divider, Drawer, Grid, Skeleton } from '@mui/material'
import { ChevronRight } from '@mui/icons-material'
import { HEADER_HEIGHT, theme } from '../../helpers/theme'
import { IState } from '../../store/reducers'
import { IntervalMetric } from '../../store/requests/intervalMetrics'
import { Job } from '../../types/api'
import { LineageMetric } from '../../store/requests/lineageMetrics'
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
import JobRunItem from './JobRunItem'
import JobsDrawer from './JobsDrawer'
import MQTooltip from '../../components/core/tooltip/MQTooltip'
import MiniGraph from './MiniGraph'
import MqText from '../../components/core/text/MqText'
import React, { useEffect } from 'react'
import SplitButton from '../../components/dashboard/SplitButton'
import StackedLineageEvents from './StackedLineageEvents'
import TimelineDrawer from './TimelineDrawer'
import {formatNumber} from "../../helpers/numbers";

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

const JOB_RUN_LIMIT = 4

const INTERVAL_TO_MS_MAP: Record<RefreshInterval, number> = {
  '30s': 30000,
  '5m': 300000,
  '10m': 600000,
  Never: 0,
}

const states = [
  { label: 'Started', color: theme.palette.info.main, bgColor: 'secondary' },
  { label: 'Completed', color: theme.palette.primary.main, bgColor: 'primary' },
  { label: 'Failed', color: theme.palette.error.main, bgColor: 'error' },
  { label: 'Aborted', color: theme.palette.secondary.main, bgColor: 'secondary' },
]

const Dashboard: React.FC = ({
  lineageMetrics,
  fetchLineageMetrics,
  isLineageMetricsLoading,
  jobs,
  fetchJobs,
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

  const [selectedState, setSelectedState] = React.useState('FAIL')
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
    fetchJobs('food_delivery', JOB_RUN_LIMIT, 0)
  }, [])

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

    return
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

  const { failed, started, completed, aborted } = metrics

  return (
    <>
      <Drawer
        anchor={'right'}
        open={jobsDrawerOpen}
        onClose={() => setJobsDrawerOpen(false)}
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
      >
        <Box>
          <TimelineDrawer />
        </Box>
      </Drawer>
      <Container maxWidth={'lg'}>
        <Box pt={2} mb={2} display={'flex'} justifyContent={'space-between'} alignItems={'center'}>
          <MqText heading>DataOps</MqText>
          <Box display={'flex'}>
            <Box>
              <MqText subdued>REFRESH</MqText>
              <SplitButton
                options={REFRESH_INTERVALS}
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
                            onClick={() => setSelectedState(state.label)}
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
                                  case 'Failed':
                                    return failed
                                  case 'Completed':
                                    return completed
                                  case 'Aborted':
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
                <Box display={'flex'} justifyContent={'space-between'} alignItems={'center'}>
                  <MqText small font={'mono'} subdued>
                    DATASETS
                  </MqText>
                  {datasetMetrics && datasetMetrics.length > 0 && (
                    <MqText large>{formatNumber(datasetMetrics[datasetMetrics.length - 1].count)}</MqText>
                  )}
                </Box>
                <MiniGraph
                  intervalMetrics={datasetMetrics}
                  color={theme.palette.info.main}
                  label={'Datasets'}
                  isLoading={isDatasetMetricsLoading}
                />
              </Grid>
              <Grid item xs={4} md={12} style={{ paddingTop: 0 }}>
                <Box display={'flex'} justifyContent={'space-between'} alignItems={'center'}>
                  <MqText small font={'mono'} subdued>
                    SOURCES
                  </MqText>
                  {sourceMetrics && sourceMetrics.length > 0 && (
                    <MqText large>{formatNumber(sourceMetrics[sourceMetrics.length - 1].count)}</MqText>
                  )}
                </Box>
                <MiniGraph
                  intervalMetrics={sourceMetrics}
                  color={theme.palette.warning.main}
                  label={'Sources'}
                  isLoading={isSourceMetricsLoading}
                />
              </Grid>
              <Grid item xs={4} md={12} style={{ paddingTop: 0 }}>
                <Box display={'flex'} justifyContent={'space-between'} alignItems={'center'}>
                  <MqText small font={'mono'} subdued>
                    JOBS
                  </MqText>
                  {jobMetrics && jobMetrics.length > 0 && (
                    <MqText large>{formatNumber(jobMetrics[jobMetrics.length - 1].count)}</MqText>
                  )}
                </Box>
                <MiniGraph
                  intervalMetrics={jobMetrics}
                  color={theme.palette.primary.main}
                  label={'Jobs'}
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
                  <Button
                    disableRipple
                    size={'small'}
                    endIcon={<ChevronRight />}
                    onClick={() => setJobsDrawerOpen(true)}
                  >
                    See More
                  </Button>
                </Box>
                {jobs.map((job) => (
                  <JobRunItem key={job.id.namespace + job.id.name} job={job} />
                ))}
              </Box>
            </Grid>
            {/* <Grid item sm={12} md={4}>
              <Box display={'flex'} justifyContent={'space-between'} alignItems={'center'}>
                <MqText subdued>RECENT ACTIVITY</MqText>
                <Button
                  disableRipple
                  size={'small'}
                  sx={{ mr: 2 }}
                  endIcon={<ChevronRight />}
                  onClick={() => setTimelineOpen(true)}
                >
                  See More
                </Button>
              </Box>
              <Timeline
                sx={{
                  p: 0,
                  m: 0,
                  [`& .${timelineItemClasses.root}`]: {
                    margin: 0,
                    padding: 0,
                  },

                  [`& .${timelineItemClasses.root}:before`]: {
                    flex: 0,
                    padding: 0,
                  },
                }}
              >
                <TimelineItem>
                  <TimelineSeparator>
                    <TimelineDot color='primary'>
                      <RunCircleOutlined color={'secondary'} />
                    </TimelineDot>
                    <TimelineConnector />
                  </TimelineSeparator>
                  <TimelineContent>
                    <MqText subdued sx={{ mr: 1 }} inline>
                      10:30 AM
                    </MqText>
                    <MqText inline link>
                      delivery_time_7_days
                    </MqText>
                    <MqText inline> completed in 3m 40s</MqText>
                  </TimelineContent>
                </TimelineItem>
                <TimelineItem>
                  <TimelineSeparator>
                    <TimelineDot color={'primary'}>
                      <Code color={'secondary'} />
                    </TimelineDot>
                    <TimelineConnector />
                  </TimelineSeparator>
                  <TimelineContent>
                    <MqText subdued sx={{ mr: 1 }} inline>
                      10:27 AM
                    </MqText>
                    <MqText link inline>
                      delivery_time_7_days{' '}
                    </MqText>
                    <MqText inline>
                      source code modified. New version e5af47b5-b1fa-49a6-8d3f-8a255e4fe787 created
                      caused by the following:
                      <List dense sx={{ p: 0 }}>
                        <ListItem dense>
                          <Box width={54}>
                            <MqText small inline subdued>
                              ADDED
                            </MqText>
                          </Box>
                          <MqText inline small font={'mono'}>
                            order_address_id
                          </MqText>
                        </ListItem>
                        <ListItem dense>
                          <Box width={54}>
                            <MqText small inline subdued>
                              REMOVED
                            </MqText>
                          </Box>
                          <MqText small font={'mono'}>
                            order_address
                          </MqText>
                        </ListItem>
                      </List>
                    </MqText>
                  </TimelineContent>
                </TimelineItem>
                <TimelineItem>
                  <TimelineSeparator>
                    <TimelineDot color={'info'}>
                      <Computer color={'secondary'} />
                    </TimelineDot>
                    <TimelineConnector />
                  </TimelineSeparator>
                  <TimelineContent>
                    <MqText subdued sx={{ mr: 1 }} inline>
                      10:24 AM
                    </MqText>
                    <MqText inline>
                      The job{' '}
                      <MqText inline link>
                        delivery_time_7_days
                      </MqText>{' '}
                      successfully completed, creating two datasets in 3 minutes and 4 minutes 30
                      seconds, respectively.
                    </MqText>
                  </TimelineContent>
                </TimelineItem>
                <TimelineItem>
                  <TimelineSeparator>
                    <TimelineDot color={'primary'}>
                      <Source color={'secondary'} />
                    </TimelineDot>
                    <TimelineConnector />
                  </TimelineSeparator>
                  <TimelineContent>
                    <MqText subdued sx={{ mr: 1 }} inline>
                      10:18 AM
                    </MqText>
                    <MqText inline link>
                      {' '}
                      orders_july_2023
                    </MqText>
                    <MqText inline> added to food_delivery_db.</MqText>
                  </TimelineContent>
                </TimelineItem>
                <TimelineItem>
                  <TimelineSeparator>
                    <TimelineDot color={'info'}>
                      <Computer color={'secondary'} />
                    </TimelineDot>
                    <TimelineConnector />
                  </TimelineSeparator>
                  <TimelineContent>
                    <MqText subdued sx={{ mr: 1 }} inline>
                      10:24 AM
                    </MqText>
                    <MqText inline>
                      The job{' '}
                      <MqText inline link>
                        delivery_time_7_days
                      </MqText>{' '}
                      successfully completed, creating two datasets in 3 minutes and 4 minutes 30
                      seconds, respectively.
                    </MqText>
                  </TimelineContent>
                </TimelineItem>
              </Timeline>
            </Grid> */}
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
