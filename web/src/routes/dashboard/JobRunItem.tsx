// SPDX-License-Identifier: Apache-2.0

import { Box } from '@mui/system'
import { Chip, Divider } from '@mui/material'
import { formatUpdatedAt } from '../../helpers'
import { runStateColor } from '../../helpers/nodes'
import { theme } from '../../helpers/theme'
import MqStatus from '../../components/core/status/MqStatus'
import MqText from '../../components/core/text/MqText'
import React from 'react'
import airflow_logo from './airflow.svg'

const JobRunItem: React.FC = () => {
  return (
    <Box
      p={2}
      mb={2}
      border={1}
      borderColor={'divider'}
      sx={{
        cursor: 'pointer',
        transition: 'background-color 0.3s',
        '&:hover': {
          backgroundColor: theme.palette.action.hover,
        },
      }}
    >
      <Box display={'flex'} alignItems={'center'} justifyContent={'space-between'} mb={1}>
        <Box display={'flex'} alignItems={'center'}>
          <MqText sx={{ mr: 2 }}>example_marquez</MqText>
          <Chip sx={{ mr: 1 }} size={'small'} label={'tag1'} />
          <Chip sx={{ mr: 1 }} size={'small'} label={'tag2'} />
          <Chip size={'small'} label={'tag3'} />
        </Box>
        <Box>
          <Chip
            variant={'outlined'}
            size={'small'}
            icon={
              <img
                style={{ marginLeft: 8 }}
                src={airflow_logo}
                height={12}
                width={12}
                alt='Airflow Logo'
              />
            }
            label='Airflow'
          />
        </Box>
      </Box>
      <Box display={'flex'}>
        <Box>
          <MqText subdued>LAST 10 RUNS</MqText>
          <Box display={'flex'} height={40} alignItems={'flex-end'}>
            {Array.from({ length: 10 }, (_, i) => (
              <Box
                key={i}
                display={'flex'}
                alignItems={'center'}
                justifyContent={'space-between'}
                bgcolor={
                  Math.random() > 0.5 ? theme.palette.primary.main : theme.palette.error.main
                }
                mr={0.5}
                minHeight={2}
                width={5}
                height={Math.floor(Math.random() * 40)}
              />
            ))}
          </Box>
        </Box>
        <Divider sx={{ mx: 2 }} flexItem orientation={'vertical'} />
        <Box>
          <MqText subdued>UPDATED AT</MqText>
          <MqText>{formatUpdatedAt('2021-05-13T13:45:13Z')} </MqText>
        </Box>
        <Divider sx={{ mx: 2 }} flexItem orientation={'vertical'} />
        <Box>
          <MqText subdued>STATUS</MqText>
          <MqStatus label={'FAILED'} color={runStateColor('FAILED')} />
        </Box>
        <Divider sx={{ mx: 2 }} flexItem orientation={'vertical'} />
        <Box>
          <MqText subdued>LAST RUN</MqText>
          <MqText>4m 30s</MqText>
        </Box>
        <Divider sx={{ mx: 2 }} flexItem orientation={'vertical'} />
        <Box>
          <MqText subdued>TYPE</MqText>
          <Chip size={'small'} color={'primary'} variant={'outlined'} label={'BATCH'} />
        </Box>
        <Divider sx={{ mx: 2 }} flexItem orientation={'vertical'} />
        <Box>
          <MqText subdued>NAMESPACE</MqText>
          <MqText>food_delivery</MqText>
        </Box>
      </Box>
    </Box>
  )
}

export default JobRunItem
