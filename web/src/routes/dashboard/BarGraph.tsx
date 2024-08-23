// Copyright 2018-2024 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { BarChart } from '@mui/x-charts'
import { theme } from '../../helpers/theme'
import ParentSize from '@visx/responsive/lib/components/ParentSize'
import React from 'react'

interface Props {}

const BarGraph: React.FC<Props> = () => {
  return (
    <ParentSize>
      {(parent) => (
        <BarChart
          series={[
            { data: [4, 3, 5, 4, 3, 6, 7, 5], color: theme.palette.primary.main },
            { data: [2, 5, 7, 4, 3, 9, 0, 1], color: theme.palette.error.main },
          ]}
          width={parent.width}
          height={500}
          leftAxis={null}
          bottomAxis={null}
          layout={'horizontal'}
          margin={{ left: 0, right: 0, top: 0, bottom: 0 }}
        />
      )}
    </ParentSize>
  )
}

export default BarGraph
