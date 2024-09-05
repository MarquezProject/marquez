// Copyright 2018-2024 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { LineChart } from '@mui/x-charts'
import { theme } from '../../helpers/theme'
import Box from '@mui/system/Box'
import ParentSize from '@visx/responsive/lib/components/ParentSize'
import React from 'react'
import {LineageMetric} from "../../store/requests/lineageMetrics";
import lineage from "../../store/reducers/lineage";

interface Props {
  lineageMetrics: LineageMetric[]
}

const formatTime = (date: Date) =>
  date.toLocaleTimeString([], { hour: 'numeric', minute: '2-digit' });


const StackedLineageEvents = ({ lineageMetrics }: Props) => {
  const labels = lineageMetrics.map(item => `${formatTime(new Date(item.startInterval))} - ${formatTime(new Date(item.endInterval))}`);
  const failData = lineageMetrics.map(item => item.fail);
  const startData = lineageMetrics.map(item => item.start);
  const completeData = lineageMetrics.map(item => item.complete);
  const abortData = lineageMetrics.map(item => item.abort);

  return (
    <Box>
      <ParentSize>
        {(parent) => (
          <>
            <LineChart
              sx={{
                backgroundImage: 'radial-gradient(circle at 1px 1px, #bdbdbd42 1px, transparent 0)',
                backgroundSize: `20px 20px`,
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
              margin={{ left: 0, right: 0, top: 0, bottom: 0 }}
              xAxis={[{ data: labels, scaleType: 'point', disableLine: true, disableTicks: true }]}
              bottomAxis={null}
              leftAxis={null}
              slotProps={{
                legend: {
                  hidden: true,
                }
              }}
            />
          </>
        )}
      </ParentSize>
    </Box>
  )
}

export default StackedLineageEvents
