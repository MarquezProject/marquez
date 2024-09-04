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
} from '@mui/material'
import {ChevronRight, Code, Computer, RunCircleOutlined, Source} from '@mui/icons-material'
import { HEADER_HEIGHT, theme } from '../../helpers/theme'
import {
  Timeline,
  TimelineConnector,
  TimelineContent,
  TimelineDot,
  TimelineItem,
  TimelineSeparator,
  timelineItemClasses,
} from '@mui/lab'
import JobRunItem from './JobRunItem'
import JobsDrawer from './JobsDrawer'
import MQTooltip from '../../components/core/tooltip/MQTooltip'
import MiniGraph from './MiniGraph'
import MqText from '../../components/core/text/MqText'
import React from 'react'
import SplitButton from '../../components/dashboard/SplitButton'
import StackedLineageEvents from './StackedLineageEvents'
import TimelineDrawer from './TimelineDrawer'

interface Props {}

const TIMEFRAMES = ['8 Hours', '24 Hours', '7 Days']
const REFRESH_INTERVALS = ['30s', '5m', '10m', 'Never']

const states = [
  { label: 'FAIL', color: theme.palette.error.main, bgColor: 'error' },
  { label: 'RUNNING', color: theme.palette.info.main, bgColor: 'info' },
  { label: 'COMPLETE', color: theme.palette.primary.main, bgColor: 'primary' },
  { label: 'ABORT', color: theme.palette.warning.main, bgColor: 'secondary' },
  { label: 'START', color: theme.palette.secondary.main, bgColor: 'secondary' },
  { label: 'OTHER', color: theme.palette.secondary.main, bgColor: 'secondary' },
]

const Dashboard: React.FC<Props> = () => {
  const [timeframe, setTimeframe] = React.useState('8 Hours')
  const [selectedState, setSelectedState] = React.useState('FAIL')
  const [jobsDrawerOpen, setJobsDrawerOpen] = React.useState(false)
  const [timelineOpen, setTimelineOpen] = React.useState(false)
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
              <SplitButton options={REFRESH_INTERVALS} />
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
            <Grid item xs={12} md={8}>
              <StackedLineageEvents label={'OpenLineage Events'} />
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
                          {Math.floor(Math.random() * 100)}
                        </Stack>
                      </Button>
                    </MQTooltip>
                  ))}
                </ButtonGroup>
              </Box>
            </Grid>
            <Grid container item xs={12} md={4} spacing={2}>
              <Grid item xs={6}>
                <MqText label subdued>
                  Data Sources
                </MqText>
                <MqText>800</MqText>
                <MiniGraph />
              </Grid>
              <Grid item xs={6}>
                <MqText label subdued>
                  Datasets
                </MqText>
                <MqText>800</MqText>
                <MiniGraph />
              </Grid>
              <Grid item xs={6}>
                <MqText label subdued>
                  Datasets With Schemas
                </MqText>
                <MqText>800</MqText>
                <MiniGraph />
              </Grid>
              <Grid item xs={6}>
                <MqText label subdued>
                  Jobs
                </MqText>
                <MqText>800</MqText>
                <MiniGraph />
              </Grid>
            </Grid>
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
                  <Button disableRipple size={'small'} endIcon={<ChevronRight />} onClick={() => setJobsDrawerOpen(true)}>
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
                <Button disableRipple size={'small'} sx={{ mr: 2 }} endIcon={<ChevronRight />} onClick={() => setTimelineOpen(true)}>
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

export default Dashboard
