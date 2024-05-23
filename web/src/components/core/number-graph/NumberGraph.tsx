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

const NumberGraph: React.FC<Props> = ({ label }) => {
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
            3k
          </MqText>
          <Box display={'flex'} alignItems={'center'}>
            <TrendingDown color='error' />
            <MqText color='error'>-12%</MqText>
          </Box>
        </Box>
        <LineChart
          series={[
            {
              data: [2, 5.5, 2, 8.5, 1.5, 5],
              color: theme.palette.primary.main,
              area: true,
              showMark: false,
            },
          ]}
          height={100}
          leftAxis={null}
          bottomAxis={null}
          width={200}
          margin={{ left: 4, right: 4, top: 4, bottom: 4 }}
        />
      </Box>
    </Box>
  )
}

export default NumberGraph
