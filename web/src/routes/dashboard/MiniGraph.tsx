// Copyright 2018-2024 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { IntervalMetric } from '../../store/requests/intervalMetrics'
import { LineChart } from '@mui/x-charts'
import { Skeleton } from '@mui/material'
import { formatTime } from './StackedLineageEvents'
import { theme } from '../../helpers/theme'
import ParentSize from '@visx/responsive/lib/components/ParentSize'
import React from 'react'

interface Props {
  intervalMetrics?: IntervalMetric[]
  color: string
  label: string
  isLoading: boolean
}

const HEIGHT = 52

const MiniGraph: React.FC<Props> = ({ intervalMetrics, isLoading, color, label }) => {
  if (!intervalMetrics || isLoading) {
    return <Skeleton height={HEIGHT + 1} width={'100%'} />
  }
  const isWeek = intervalMetrics.length === 7

  const labels = intervalMetrics.map((item) => {
    if (isWeek) {
      return new Date(item.startInterval).toLocaleDateString(['UTC'], {
        weekday: 'long',
        month: 'short',
        day: 'numeric',
        timeZone: 'UTC',
      })
    }
    return `${formatTime(new Date(item.startInterval))} - ${formatTime(new Date(item.endInterval))}`
  })

  return (
    <ParentSize
      style={{ display: 'flex', borderBottom: `1px solid ${theme.palette.secondary.main}` }}
    >
      {(parent) => (
        <LineChart
          width={parent.width}
          height={HEIGHT}
          series={[
            {
              data: intervalMetrics?.map((item) => item.count),
              type: 'line',
              area: false,
              showMark: false,
              color: color,
              label: label,
            },
          ]}
          xAxis={[{ data: labels, scaleType: 'point', disableLine: true, disableTicks: true }]}
          leftAxis={null}
          slotProps={{
            legend: {
              hidden: true,
            },
          }}
          margin={{ left: 6, right: 6, top: 6, bottom: 6 }}
        />
      )}
    </ParentSize>
  )
}

export default MiniGraph
