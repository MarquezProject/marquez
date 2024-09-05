import * as Redux from 'redux'
import { Box, Stack } from '@mui/system'
import {
  Button,
  ButtonGroup,
  Container,
  Divider,
  Drawer,
  Grid,
  List,
  ListItem,
  Skeleton,
} from '@mui/material'
import { ChevronRight, Code, Computer, RunCircleOutlined, Source } from '@mui/icons-material'
import { HEADER_HEIGHT, theme } from '../../helpers/theme'
import { IState } from '../../store/reducers'
import { LineageMetric } from '../../store/requests/lineageMetrics'
import {
  Timeline,
  TimelineConnector,
  TimelineContent,
  TimelineDot,
  TimelineItem,
  TimelineSeparator,
  timelineItemClasses,
} from '@mui/lab'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { fetchLineageMetrics } from '../../store/actionCreators'
import { useSearchParams } from 'react-router-dom'
import JobRunItem from './JobRunItem'
import JobsDrawer from './JobsDrawer'
import MQTooltip from '../../components/core/tooltip/MQTooltip'
import MqText from '../../components/core/text/MqText'
import React, { useEffect } from 'react'
import SplitButton from '../../components/dashboard/SplitButton'
import StackedLineageEvents from './StackedLineageEvents'
import TimelineDrawer from './TimelineDrawer'

interface StateProps {
  lineageMetrics: LineageMetric[]
  isLineageMetricsLoading: boolean
}

interface DispatchProps {
  fetchLineageMetrics: typeof fetchLineageMetrics
}

const TIMEFRAMES = ['24 Hours', '7 Days']

type RefreshInterval = '30s' | '5m' | '10m' | 'Never'

const REFRESH_INTERVALS: RefreshInterval[] = ['30s', '5m', '10m', 'Never']

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
    if (timeframe === '24 Hours') {
      setSearchParams({ timeframe: 'day' })
      fetchLineageMetrics('day')
    } else if (timeframe === '7 Days') {
      setSearchParams({ timeframe: 'week' })
      fetchLineageMetrics('week')
    }
  }, [timeframe])

  useEffect(() => {
    const intervalTime = INTERVAL_TO_MS_MAP[intervalKey]

    if (intervalTime > 0) {
      const intervalId = setInterval(() => {
        fetchLineageMetrics(timeframe === '24 Hours' ? 'day' : 'week')
      }, intervalTime)

      return () => clearInterval(intervalId)
    }

    return
  }, [intervalKey])

  const failed = lineageMetrics.map((item) => item.fail).reduce((a, b) => a + b, 0)
  const started = lineageMetrics.map((item) => item.start).reduce((a, b) => a + b, 0)
  const completed = lineageMetrics.map((item) => item.complete).reduce((a, b) => a + b, 0)
  const aborted = lineageMetrics.map((item) => item.abort).reduce((a, b) => a + b, 0)

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
          <MqText heading>Data Ops</MqText>
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
                    onClick={() => setTimeframe(tf)}
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
            <Grid item xs={12} md={12}>
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
                              {state.label === 'Failed'
                                ? failed
                                : state.label === 'Completed'
                                ? completed
                                : state.label === 'Aborted'
                                ? aborted
                                : started}
                            </Stack>
                          </Button>
                        </MQTooltip>
                      ))}
                    </ButtonGroup>
                  </Box>
                </>
              )}
            </Grid>
            {/*<Grid container item xs={12} md={4} spacing={2}>*/}
            {/*  <Grid item xs={6}>*/}
            {/*    <MqText label subdued>*/}
            {/*      Data Sources*/}
            {/*    </MqText>*/}
            {/*    <MqText>800</MqText>*/}
            {/*    <MiniGraph />*/}
            {/*  </Grid>*/}
            {/*  <Grid item xs={6}>*/}
            {/*    <MqText label subdued>*/}
            {/*      Datasets*/}
            {/*    </MqText>*/}
            {/*    <MqText>800</MqText>*/}
            {/*    <MiniGraph />*/}
            {/*  </Grid>*/}
            {/*  <Grid item xs={6}>*/}
            {/*    <MqText label subdued>*/}
            {/*      Datasets With Schemas*/}
            {/*    </MqText>*/}
            {/*    <MqText>800</MqText>*/}
            {/*    <MiniGraph />*/}
            {/*  </Grid>*/}
            {/*  <Grid item xs={6}>*/}
            {/*    <MqText label subdued>*/}
            {/*      Jobs*/}
            {/*    </MqText>*/}
            {/*    <MqText>800</MqText>*/}
            {/*    <MiniGraph />*/}
            {/*  </Grid>*/}
            {/*</Grid>*/}
            <Grid item xs={12}>
              <Divider />
            </Grid>
            <Grid
              item
              md={8}
              sm={12}
              borderRight={{
                xs: 'none',
                md: 1,
              }}
              borderColor={{
                md: 'divider',
              }}
            >
              <Box
                mr={{
                  xs: 0,
                  md: 2,
                }}
              >
                <Box display={'flex'} justifyContent={'space-between'} alignItems={'center'} mb={1}>
                  <MqText subdued>{selectedState} JOBS</MqText>
                  <Button
                    disableRipple
                    size={'small'}
                    endIcon={<ChevronRight />}
                    onClick={() => setJobsDrawerOpen(true)}
                  >
                    See More
                  </Button>
                </Box>

                <JobRunItem />
                <JobRunItem />
                <JobRunItem />
                <JobRunItem />
              </Box>
            </Grid>
            <Grid item sm={12} md={4}>
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
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      fetchLineageMetrics: fetchLineageMetrics,
    },
    dispatch
  )

export default connect(mapStateToProps, mapDispatchToProps)(Dashboard)
