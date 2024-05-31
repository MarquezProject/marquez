import {Box} from '@mui/system'
import {Button, ButtonGroup, Chip, Container, Divider, Grid} from '@mui/material'
import MqText from '../../components/core/text/MqText'
import React from 'react'
import StackedLineageEvents from './StackedLineageEvents'
import MiniGraph from "./MiniGraph";
import {
    Timeline,
    TimelineConnector, TimelineContent,
    TimelineDot,
    TimelineItem, timelineItemClasses,
    TimelineOppositeContent, timelineOppositeContentClasses,
    TimelineSeparator
} from '@mui/lab'
import {Compare, Computer, FoodBank, Hotel, Repeat} from "@mui/icons-material";
import BarGraph from "./BarGraph";

interface Props {
}

const TIMEFRAMES = ['8 Hours', '24 Hours', '7 Days']
const REFRESH_INTERVALS = ['30s', '5m', '10m', 'Never']

const Dashboard: React.FC<Props> = ({}: Props) => {
    const [timeframe, setTimeframe] = React.useState('8 Hours')
    const [refreshInterval, setRefreshInterval] = React.useState('30s')
    return (
        <Container maxWidth={'lg'}>
            <Box pt={2} mb={2} display={'flex'} justifyContent={'space-between'} alignItems={'center'}>
                <MqText heading>Data Ops</MqText>
                <Box display={'flex'}>
                    {/* todo look into SplitButton to replace this one */}
                    <Box>
                        <MqText subdued>REFRESH</MqText>
                        <ButtonGroup size={'small'} variant='outlined'>
                            {REFRESH_INTERVALS.map((ri) => (
                                <Button
                                    key={ri}
                                    variant={refreshInterval === ri ? 'contained' : 'outlined'}
                                    onClick={() => setRefreshInterval(ri)}
                                >
                                    {ri}
                                </Button>
                            ))}
                        </ButtonGroup>
                    </Box>
                    <Divider sx={{mx: 2}} orientation={'vertical'}/>
                    <Box>
                        <MqText subdued>TIMEFRAME</MqText>
                        <ButtonGroup size={'small'} variant='outlined'>
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
                        <StackedLineageEvents label={'OpenLineage Events'}/>
                    </Grid>
                    <Grid container item xs={12} md={4} spacing={2}>
                        <Grid item xs={6}>
                            <MqText label subdued>
                                Data Sources
                            </MqText>
                            <MqText>800</MqText>
                            <MiniGraph/>
                        </Grid>
                        <Grid item xs={6}>
                            <MqText label subdued>
                                Datasets
                            </MqText>
                            <MqText>800</MqText>
                            <MiniGraph/>
                        </Grid>
                        <Grid item xs={6}>
                            <MqText label subdued>
                                Datasets With Schemas
                            </MqText>
                            <MqText>800</MqText>
                            <MiniGraph/>
                        </Grid>
                        <Grid item xs={6}>
                            <MqText label subdued>
                                Datasets With Schemas
                            </MqText>
                            <MqText>800</MqText>
                            <MiniGraph/>
                        </Grid>
                    </Grid>
                    <Grid item xs={12}><Divider sx={{mt: 1, pb: 1}}/> </Grid>
                    <Grid item xs={12} md={8} borderRight={1}  borderColor={'divider'}>
                        <Box display={'flex'} justifyContent={'space-between'} alignItems={'center'}>
                            <MqText subdued>RECENT ACTIVITY</MqText>
                            <Button size={'small'} sx={{mr: 2}}>See More</Button>
                        </Box>
                        <Timeline
                            sx={{
                                p: 0,
                                m: 0,
                                [`& .${timelineItemClasses.root}`]: {
                                    margin: 0,
                                    padding: 0
                                },

                                [`& .${timelineItemClasses.root}:before`]: {
                                    flex: 0,
                                    padding: 0,
                                },
                            }}
                        >
                            <TimelineItem>
                                <TimelineSeparator>
                                    <TimelineDot/>
                                    <TimelineConnector/>
                                </TimelineSeparator>
                                <TimelineContent>
                                    <MqText subdued sx={{mr: 1}} inline>10:30 AM</MqText>
                                    <MqText inline>The job <MqText inline
                                                                   link>delivery_time_7_days</MqText> successfully
                                        completed,
                                        creating two datasets in 3 minutes and 4 minutes 30 seconds,
                                        respectively.</MqText></TimelineContent>
                            </TimelineItem>
                            <TimelineItem>
                                <TimelineSeparator>
                                    <TimelineDot/>
                                    <TimelineConnector/>
                                </TimelineSeparator>
                                <TimelineContent>
                                    <MqText subdued sx={{mr: 1}} inline>10:30 AM</MqText>
                                    <MqText inline>The job <MqText inline
                                                                   link>delivery_time_7_days</MqText> successfully
                                        completed,
                                        creating two datasets in 3 minutes and 4 minutes 30 seconds,
                                        respectively.</MqText></TimelineContent>
                            </TimelineItem>
                            <TimelineItem>
                                <TimelineSeparator>
                                    <TimelineDot/>
                                    <TimelineConnector/>
                                </TimelineSeparator>
                                <TimelineContent>
                                    <MqText subdued sx={{mr: 1}} inline>10:30 AM</MqText>
                                    <MqText inline>The job <MqText inline
                                                                   link>delivery_time_7_days</MqText> successfully
                                        completed,
                                        creating two datasets in 3 minutes and 4 minutes 30 seconds,
                                        respectively.</MqText></TimelineContent>
                            </TimelineItem>
                            <TimelineItem>
                                <TimelineSeparator>
                                    <TimelineDot/>
                                    <TimelineConnector/>
                                </TimelineSeparator>
                                <TimelineContent>
                                    <MqText subdued sx={{mr: 1}} inline>10:30 AM</MqText>
                                    <MqText inline>The job <MqText inline
                                                                   link>delivery_time_7_days</MqText> successfully
                                        completed,
                                        creating two datasets in 3 minutes and 4 minutes 30 seconds,
                                        respectively.</MqText></TimelineContent>
                            </TimelineItem>
                            <TimelineItem>
                                <TimelineSeparator>
                                    <TimelineDot/>
                                    <TimelineConnector/>
                                </TimelineSeparator>
                                <TimelineContent>
                                    <MqText subdued sx={{mr: 1}} inline>10:30 AM</MqText>
                                    <MqText inline>The job <MqText inline
                                                                   link>delivery_time_7_days</MqText> successfully
                                        completed,
                                        creating two datasets in 3 minutes and 4 minutes 30 seconds,
                                        respectively.</MqText></TimelineContent>
                            </TimelineItem>
                            <TimelineItem>
                                <TimelineSeparator>
                                    <TimelineDot/>
                                    <TimelineConnector/>
                                </TimelineSeparator>
                                <TimelineContent>
                                    <MqText subdued sx={{mr: 1}} inline>10:30 AM</MqText>
                                    <MqText inline>The job <MqText inline
                                                                   link>delivery_time_7_days</MqText> successfully
                                        completed,
                                        creating two datasets in 3 minutes and 4 minutes 30 seconds,
                                        respectively.</MqText></TimelineContent>
                            </TimelineItem>
                            <TimelineItem>
                                <TimelineSeparator>
                                    <TimelineDot/>
                                    <TimelineConnector/>
                                </TimelineSeparator>
                                <TimelineContent>
                                    <MqText subdued sx={{mr: 1}} inline>10:30 AM</MqText>
                                    <MqText inline>The job <MqText inline
                                                                   link>delivery_time_7_days</MqText> successfully
                                        completed,
                                        creating two datasets in 3 minutes and 4 minutes 30 seconds,
                                        respectively.</MqText></TimelineContent>
                            </TimelineItem>
                        </Timeline>
                    </Grid>
                    <Grid item md={4} xs={12}>
                        <Box display={'flex'} justifyContent={'space-between'} alignItems={'center'}>
                            <MqText subdued label>RUNS</MqText>
                            <Box display={'flex'}>
                                <Chip color={'primary'} size={'small'} label={'10 passed'} variant={'outlined'}/>
                                <Divider sx={{mx: 1}} orientation={'vertical'}/>
                                <Chip color={'error'} size={'small'} label={'8 failed'} variant={'outlined'}/>
                            </Box>
                        </Box>
                        <BarGraph/>
                    </Grid>
                </Grid>
            </Box>
        </Container>
    )
}

export default Dashboard
