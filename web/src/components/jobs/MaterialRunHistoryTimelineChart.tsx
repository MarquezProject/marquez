// Copyright 2018-2024 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { BarChart } from '@mui/x-charts/BarChart'
import { IState } from '../../store/reducers'
import { Run, RunState } from '../../types/api'
import { axisClasses } from '@mui/x-charts/ChartsAxis'
import { connect } from 'react-redux'
import { formatUpdatedAt } from '../../helpers'
import { runStateColor } from '../../helpers/nodes'
import Box from '@mui/material/Box'
import React from 'react'

interface Props {
  runs: Run[]
}
const MAX = 20
const dummyGraphRuns = (count: number) => {
  return Array.from({ length: count }, (_, i) => {
    return {
      label: MAX === i + 1 ? 'LAST' : `~${MAX - i - 1}`,
      createdAt: '2024-02-22T22:00:00Z',
      state: i % 2 === 0 ? ('COMPLETED' as RunState) : ('FAILED' as RunState),
      durationMs: Math.floor(Math.random() * 10000),
    }
  })
}

export const MaterialRunHistoryTimelineChart: React.FC<Props> = () => {
  const runGraph = dummyGraphRuns(20)
  const labels = Object.values(runGraph).map((item) => item.label)
  const state = Object.values(runGraph).map((item) => item.state)

  const chartSetting = {
    yAxis: [
      {
        label: 'Duration (seconds)',
        valueFormatter: (value: number | null) => {
          if (value === null) return ''
          return `${value / 1000}s`
        },
      },
    ],
    series: [
      {
        dataKey: 'durationMs',
        valueFormatter: (value: number | null, { dataIndex }: { dataIndex: number }) => {
          const { createdAt } = runGraph[dataIndex]
          if (value === null) return ''
          return `${value / 1000}s at ${formatUpdatedAt(createdAt)}`
        },
      },
    ],
    height: 200,
    width: 816,
    sx: {
      [`& .${axisClasses.directionY} .${axisClasses.label}`]: {
        transform: 'translateX(-10px)',
      },
    },
  }

  const barColors = state.map((val) => {
    return runStateColor(val)
  })

  return (
    <Box width={'100%'}>
      <BarChart
        dataset={runGraph}
        {...chartSetting}
        xAxis={[
          {
            scaleType: 'band',
            data: labels,
            tickPlacement: 'middle',
            colorMap: {
              type: 'ordinal',
              values: labels,
              colors: barColors,
            },
          },
        ]}
      />
    </Box>
  )
}

const mapStateToProps = (state: IState) => {
  return {
    runs: state.runs.result,
  }
}

export default connect(mapStateToProps)(MaterialRunHistoryTimelineChart)
