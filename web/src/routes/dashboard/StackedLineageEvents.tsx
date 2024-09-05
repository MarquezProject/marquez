// Copyright 2018-2024 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { Chip } from '@mui/material'
import { LineChart } from '@mui/x-charts'
import { LineageMetric } from '../../store/requests/lineageMetrics'
import { pluralize } from '../../helpers/text'
import { sum } from 'lodash'
import { theme } from '../../helpers/theme'
import Box from '@mui/system/Box'
import ParentSize from '@visx/responsive/lib/components/ParentSize'
import React from 'react'

interface Props {
  lineageMetrics: LineageMetric[]
}

const formatTime = (date: Date) =>
  date.toLocaleTimeString([], {
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  })

const StackedLineageEvents = ({ lineageMetrics }: Props) => {
  const isWeek = lineageMetrics.length === 7

  const labels = lineageMetrics.map((item) => {
    if (isWeek) {
      return new Date(item.startInterval).toLocaleDateString('en-US', {
        weekday: 'long',
        month: 'short',
        day: 'numeric',
      })
    }
    return `${formatTime(new Date(item.startInterval))} - ${formatTime(new Date(item.endInterval))}`
  })

  const failData = lineageMetrics.map((item) => item.fail)
  const startData = lineageMetrics.map((item) => item.start)
  const completeData = lineageMetrics.map((item) => item.complete)
  const abortData = lineageMetrics.map((item) => item.abort)
  const totalEvents = sum(failData) + sum(startData) + sum(completeData) + sum(abortData)

  return (
    <Box
      mx={'-16px'}
      position={'relative'}
      sx={{
        overflowX: 'hidden', // prevents background from stretching horizontally
        maxWidth: 'calc(100% + 32px)', // ensures the container doesn't stretch beyond the parent size
      }}
    >
      <Chip
        size={'small'}
        variant={'outlined'}
        sx={{
          position: 'absolute',
          top: 8,
          right: 8,
        }}
        label={pluralize(totalEvents, 'event', 'events')}
      ></Chip>
      <ParentSize>
        {(parent) => (
          <LineChart
            sx={{
              backgroundImage: 'radial-gradient(circle at 1px 1px, #bdbdbd42 1px, transparent 0)',
              backgroundSize: '20px 20px',
              overflow: 'hidden', // confines the background to the chart area
              backgroundPosition: 'left 16px top', // Adjust the starting position of the background
              clipPath: 'inset(0 16px 0 16px)', // Clips 16px from the left and right
            }}
            width={parent.width}
            height={200}
            series={[
              {
                data: startData,
                type: 'line',
                showMark: false,
                color: theme.palette.info.main,
                label: 'Started',
              },
              {
                data: completeData,
                type: 'line',
                showMark: false,
                label: 'Completed',
                color: theme.palette.primary.main,
              },
              {
                data: failData,
                type: 'line',
                showMark: false,
                color: theme.palette.error.main,
                label: 'Failed',
                disableHighlight: true,
              },
              {
                data: abortData,
                type: 'line',
                label: 'Aborted',
                showMark: false,
                color: theme.palette.secondary.main,
              },
            ]}
            margin={{ left: 16, right: 16, top: 6, bottom: 0 }}
            xAxis={[{ data: labels, scaleType: 'point', disableLine: true, disableTicks: true }]}
            bottomAxis={null}
            leftAxis={null}
            slotProps={{
              legend: {
                hidden: true,
              },
            }}
          />
        )}
      </ParentSize>
    </Box>
  )
}

export default StackedLineageEvents
