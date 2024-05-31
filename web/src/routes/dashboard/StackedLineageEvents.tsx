// Copyright 2018-2024 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { LineChart } from '@mui/x-charts'
import { Chip, Divider } from '@mui/material'
import { theme } from '../../helpers/theme'
import Box from '@mui/system/Box'
import MqText from '../../components/core/text/MqText'
import ParentSize from '@visx/responsive/lib/components/ParentSize'
import React from 'react'

interface Props {
  label: string
}

const StackedLineageEvents: React.FC<Props> = ({ label }) => {
  return (
    <Box>
      <Box display={'flex'} justifyContent={'space-between'}>
        <MqText subdued>EVENTS</MqText>
        <Box display={'flex'} alignItems={'center'}>
          <Chip color={'info'} size='small' label={'5.4K'} variant={'outlined'} />
          <Divider orientation={'vertical'} sx={{ mx: 1 }} />
          <Chip color={'primary'} size='small' label={'5k'} variant={'outlined'} />
          <Divider orientation={'vertical'} sx={{ mx: 1 }} />
          <Chip color={'error'} size='small' label={'400'} variant={'outlined'} />
        </Box>
      </Box>

      <ParentSize>
        {(parent) => (
          <>
            <LineChart
                sx={{
                    backgroundImage: 'radial-gradient(circle at 1px 1px, #bdbdbd42 1px, transparent 0)',
                    backgroundSize: '24px 24px'
                }}
              width={parent.width}
              height={200}
              series={[
                {
                  data: [34, 233, 236, 237, 354, 459, 652, 920],
                  type: 'line',
                  showMark: false,
                  color: theme.palette.primary.main,
                },
                {
                  data: [23, 148, 276, 349, 502, 658, 789, 934],
                  type: 'line',
                  showMark: false,
                  color: theme.palette.info.main,
                },
                {
                  data: [108, 128, 439, 476, 496, 544, 622, 970],
                  type: 'line',
                  showMark: false,
                  color: theme.palette.error.main,
                },
              ]}
              leftAxis={null}
              bottomAxis={null}
              margin={{ left: 4, right: 4, top: 0, bottom: 0 }}
            />
          </>
        )}
      </ParentSize>
    </Box>
  )
}

export default StackedLineageEvents
