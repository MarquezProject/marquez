// Copyright 2018-2024 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { LineChart } from '@mui/x-charts'
import { theme } from '../../helpers/theme'
import ParentSize from '@visx/responsive/lib/components/ParentSize'
import React from 'react'

interface Props {}

const MiniGraph: React.FC<Props> = () => {
  return (
    <ParentSize>
      {(parent) => (
        <LineChart
          width={parent.width}
          height={69}
          series={[
            {
              data: [34, 233, 236, 237, 354, 459, 652, 920],
              type: 'line',
              showMark: false,
              area: true,
              color: theme.palette.info.main,
            },
          ]}
          leftAxis={null}
          bottomAxis={null}
          margin={{ left: 0, right: 0, top: 0, bottom: 0 }}
        />
      )}
    </ParentSize>
  )
}

export default MiniGraph
