// Copyright 2018-2024 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { Bar, BarChart, Cell, ResponsiveContainer, XAxis, YAxis } from 'recharts'
import { IState } from '../../store/reducers'
import { Run, RunState } from '../../types/api'
import { connect } from 'react-redux'
import { runStateColor } from '../../helpers/nodes'
import { theme } from '../../helpers/theme'
import Box from '@mui/material/Box'
import React from 'react'

interface Props {
  runs: Run[]
}

const MAX = 20

/**
 * Pad runs with 0 length runs to the left until the limit is reached
 * @param runs
 * @param length
 */
const leftPad = (
  runs: { createdAt: string; state: RunState; durationMs: number }[],
  length: number
) => {
  const paddedRuns = [...runs]
  while (paddedRuns.length < length) {
    paddedRuns.unshift({
      createdAt: '',
      durationMs: 0,
      state: 'NEW',
    })
  }
  return paddedRuns
}

const runToGraph = (runs: Run[]) => {
  const graphRuns = runs.map((run) => {
    return {
      createdAt: run.createdAt,
      state: run.state,
      durationMs: run.durationMs,
    }
  })
  return leftPad(graphRuns, 20)
}

const dummyGraphRuns = (count: number) => {
  return Array.from({ length: count }, (_) => {
    return {
      createdAt: '',
      state: 'COMPLETED' as RunState,
      durationMs: Math.floor(Math.random() * 10000),
    }
  })
}

const xAxisLabel = (_: any, index: number) => {
  if (index + 1 === MAX) return 'NOW'
  return `~${MAX - index - 1}`
}

const yAxisLabel = (value: any) => {
  return `${value / 1000}s`
}

export const RunHistoryTimeline: React.FC<Props> = ({ runs }: Props) => {
  // const runGraph = runToGraph(runs)
  const runGraph = dummyGraphRuns(20)
  return (
    <Box height={100} width={'100%'}>
      <ResponsiveContainer width='100%' height='100%'>
        <BarChart
          width={600}
          height={100}
          data={runGraph}
          margin={{
            top: 0,
            right: 0,
            left: 0,
            bottom: 0,
          }}
        >
          <XAxis tickFormatter={xAxisLabel} />
          <YAxis tickFormatter={yAxisLabel} dataKey={'durationMs'} />
          <Bar dataKey='durationMs' fill={theme.palette.primary.main}>
            {runGraph.map((entry, index) => (
              <Cell key={`cell-${index}`} fill={runStateColor(entry.state)} />
            ))}
          </Bar>
        </BarChart>
      </ResponsiveContainer>
    </Box>
  )
}

const mapStateToProps = (state: IState) => {
  return {
    runs: state.runs.result,
  }
}

export default connect(mapStateToProps)(RunHistoryTimeline)
