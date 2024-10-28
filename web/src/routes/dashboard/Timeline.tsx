import { Box } from '@mui/system'
import { List, ListItem } from '@mui/material'
import {
  Timeline,
  TimelineConnector,
  TimelineContent,
  TimelineDot,
  TimelineItem,
  TimelineSeparator,
  timelineItemClasses,
} from '@mui/lab'
import MqText from '../../components/core/text/MqText'
import React from 'react'

const TimelineDrawer = () => {
  return (
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
          <TimelineDot color='primary' />
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
          <TimelineDot color={'primary'} />
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
            source code modified. New version e5af47b5-b1fa-49a6-8d3f-8a255e4fe787 created caused by
            the following:
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
          <TimelineDot color={'info'} />
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
            successfully completed, creating two datasets in 3 minutes and 4 minutes 30 seconds,
            respectively.
          </MqText>
        </TimelineContent>
      </TimelineItem>
      <TimelineItem>
        <TimelineSeparator>
          <TimelineDot color={'primary'} />
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
          <TimelineDot color={'info'} />
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
            successfully completed, creating two datasets in 3 minutes and 4 minutes 30 seconds,
            respectively.
          </MqText>
        </TimelineContent>
      </TimelineItem>
    </Timeline>
  )
}

export default TimelineDrawer
