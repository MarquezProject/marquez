// SPDX-License-Identifier: Apache-2.0

import { Box } from '@mui/system'
import { Chip, Divider } from '@mui/material'
import { Job } from '../../types/api'
import { formatUpdatedAt } from '../../helpers'
import { runStateColor } from '../../helpers/nodes'
import { stopWatchDuration } from '../../helpers/time'
import { theme } from '../../helpers/theme'
import { truncateText } from '../../helpers/text'
import MQTooltip from '../../components/core/tooltip/MQTooltip'
import MqStatus from '../../components/core/status/MqStatus'
import MqText from '../../components/core/text/MqText'
import React from 'react'
import airflow_logo from './airflow.svg'

interface Props {
  job: Job
}

const JobRunItem: React.FC<Props> = ({ job }) => {
  return (
    <Box
      p={2}
      mb={2}
      border={1}
      borderColor={'divider'}
      borderRadius={2}
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
          <MqText bold font='mono' sx={{ mr: 2 }}>
            {truncateText(job.name, 40)}
          </MqText>
          {job.tags.slice(0, 3).map((tag, index) => (
            <Chip key={index} sx={{ mr: 1 }} size={'small'} label={tag} />
          ))}
          {job.tags.length > 3 && (
            <Chip sx={{ mr: 1 }} size={'small'} label={`+ ${job.tags.length - 3}`} />
          )}
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
              <MQTooltip key={i} title={stopWatchDuration(Math.random() * 10000)}>
                <Box
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
                  sx={{
                    borderTopLeftRadius: theme.shape.borderRadius,
                    borderTopRightRadius: theme.shape.borderRadius,
                  }}
                />
              </MQTooltip>
            ))}
          </Box>
        </Box>
        <Box display={{ sm: 'none', md: 'inline-flex' }}>
          <Divider sx={{ mx: 2 }} flexItem orientation={'vertical'} />
          <Box>
            <MqText subdued>CREATED AT</MqText>
            <MqText>{formatUpdatedAt(job.createdAt)} </MqText>
          </Box>
        </Box>
        <Divider sx={{ mx: 2 }} flexItem orientation={'vertical'} />
        <Box>
          <MqText subdued>UPDATED AT</MqText>
          <MqText>{formatUpdatedAt(job.updatedAt)} </MqText>
        </Box>
        <Box display={{ sm: 'none', md: 'inline-flex' }}>
          <Divider sx={{ mx: 2 }} flexItem orientation={'vertical'} />
          <Box>
            <MqText subdued>NAMESPACE</MqText>
            <MqText>{job.namespace}</MqText>
          </Box>
        </Box>
        <Divider sx={{ mx: 2 }} flexItem orientation={'vertical'} />
        <Box>
          <MqText subdued>STATUS</MqText>
          <MqStatus label={job.latestRun.state} color={runStateColor(job.latestRun.state)} />
        </Box>
        <Divider sx={{ mx: 2 }} flexItem orientation={'vertical'} />
        <Box>
          <MqText subdued>LAST RUN</MqText>
          <MqText>{stopWatchDuration(job.latestRun.durationMs)}</MqText>
        </Box>
        <Box display={{ sm: 'none', md: 'inline-flex' }}>
          <Divider sx={{ mx: 2 }} flexItem orientation={'vertical'} />
          <Box>
            <MqText subdued>TYPE</MqText>
            <Chip size={'small'} color={'primary'} variant={'outlined'} label={'BATCH'} />
          </Box>
        </Box>
      </Box>
    </Box>
  )
}

export default JobRunItem
