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

const StackedLineageEvents = ({ lineageMetrics }: Props) => {
  const labels = lineageMetrics.map(item => new Date(item.startInterval));
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
                backgroundSize: '24px 24px',
              }}
              width={parent.width}
              height={200}
              series={[
                {
                  data: startData,
                  type: 'line',
                  showMark: false,
                  color: theme.palette.info.main,
                },
                {
                  data: completeData,
                  type: 'line',
                  showMark: false,
                  color: theme.palette.primary.main,
                },
                {
                  data: failData,
                  type: 'line',
                  showMark: false,
                  color: theme.palette.error.main,
                },
                {
                  data: abortData,
                  type: 'line',
                  showMark: false,
                  color: theme.palette.secondary.main,
                },
              ]}
              margin={{ left: 4, right: 8, top: 6, bottom: 6 }}
              xAxis={[{ data: labels, scaleType: 'time', disableLine: true, disableTicks: true }]}
              bottomAxis={null}
              leftAxis={null}
            />
          </>
        )}
      </ParentSize>
    </Box>
  )
}

export default StackedLineageEvents
