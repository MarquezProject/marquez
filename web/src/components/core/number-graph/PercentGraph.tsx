// Copyright 2018-2024 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { Box } from '@mui/system'
import { LineChart } from '@mui/x-charts'
import { TrendingDown } from '@mui/icons-material'
import { theme } from '../../../helpers/theme'
import MqText from '../text/MqText'
import React from 'react'

interface Props {
  label: string
}

const time = [
  new Date(2015, 1, 0),
  new Date(2015, 2, 0),
  new Date(2015, 3, 0),
  new Date(2015, 4, 0),
  new Date(2015, 5, 0),
  new Date(2015, 6, 0),
  new Date(2015, 7, 0),
]
const a = [4000, 3000, 2000, 2780, 1890, 2390, 3490]
const b = [2400, 1398, 9800, 3908, 4800, 3800, 4300]

const getPercents = (array: number[]) => array.map((v, index) => (100 * v) / (a[index] + b[index]))

const PercentGraph: React.FC<Props> = ({ label }) => {
  return (
    <Box
      border={`1px solid ${theme.palette.secondary.main}`}
      borderRadius={theme.spacing(1)}
      width={'350px'}
      p={2}
      pr={0}
    >
      <Box display={'flex'}>
        <Box mr={2}>
          <MqText subdued bold>
            {label.toUpperCase()}
          </MqText>
          <MqText paragraph heading bold>
            46%
          </MqText>
          <Box display={'flex'} alignItems={'center'}>
            <TrendingDown color='error' />
            <MqText color='error'>-12%</MqText>
          </Box>
        </Box>

        <LineChart
          width={200}
          height={100}
          series={[
            {
              data: getPercents(a),
              type: 'line',
              area: true,
              stack: 'total',
              showMark: false,
              color: theme.palette.primary.main,
            },
            {
              data: getPercents(b),
              type: 'line',
              area: true,
              stack: 'total',
              showMark: false,
              color: theme.palette.background.default,
            },
          ]}
          leftAxis={null}
          bottomAxis={null}
          xAxis={[
            {
              scaleType: 'time',
              data: time,
              min: time[0].getTime(),
              max: time[time.length - 1].getTime(),
            },
          ]}
          margin={{ left: 5, right: 5, top: 5, bottom: 5 }}
        />
      </Box>
    </Box>
  )
}

export default PercentGraph
