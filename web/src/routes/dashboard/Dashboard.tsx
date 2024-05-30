import { Box } from '@mui/system'
import { Button, ButtonGroup, Container, Divider, Grid } from '@mui/material'
import MqText from '../../components/core/text/MqText'
import React from 'react'
import StackedLineageEvents from './StackedLineageEvents'
import MiniGraph from "./MiniGraph";

interface Props {}

const TIMEFRAMES = ['8 Hours', '24 Hours', '7 Days']
const REFRESH_INTERVALS = ['30s', '5m', '10m', 'Never']

const Dashboard: React.FC<Props> = ({}: Props) => {
  const [timeframe, setTimeframe] = React.useState('8 Hours')
  const [refreshInterval, setRefreshInterval] = React.useState('30s')
  return (
    <Container maxWidth={'lg'}>
      <Box pt={2} display={'flex'} justifyContent={'flex-end'}>
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
          <Divider sx={{ mx: 2 }} orientation={'vertical'} />
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
            <StackedLineageEvents label={'OpenLineage Events'} />
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
                Datasets With Schemas
              </MqText>
              <MqText>800</MqText>
              <MiniGraph />
            </Grid>
          </Grid>
        </Grid>
      </Box>
    </Container>
  )
}

export default Dashboard
