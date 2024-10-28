// SPDX-License-Identifier: Apache-2.0

import { Box } from '@mui/system'
import { Chip, Divider } from '@mui/material'
import { Job } from '../../types/api'
import { encodeNode, runStateColor } from '../../helpers/nodes'
import { formatUpdatedAt } from '../../helpers'
import { stopWatchDuration } from '../../helpers/time'
import { theme } from '../../helpers/theme'
import { truncateText } from '../../helpers/text'
import { useNavigate } from 'react-router-dom'
import MQTooltip from '../../components/core/tooltip/MQTooltip'
import MqStatus from '../../components/core/status/MqStatus'
import MqText from '../../components/core/text/MqText'
import React, { useMemo } from 'react'

interface Props {
  job: Job
}

const JobRunItem: React.FC<Props> = ({ job }) => {
  const navigate = useNavigate()
  const reversedRuns = [...(job.latestRuns || [])].reverse()
  const longestRun = useMemo(
    () => job.latestRuns?.reduce((acc, run) => (acc.durationMs > run.durationMs ? acc : run)),
    [job.latestRuns]
  )
  return (
    <Box
      p={2}
      mb={2}
      border={1}
      borderColor={'divider'}
      borderRadius={2}
      onClick={() => {
        navigate(`/lineage/${encodeNode('JOB', job.namespace, job.name)}`)
      }}
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
          <MQTooltip title={job.name} placement={'top'}>
            <Box>
              <MqText bold font='mono' sx={{ mr: 2 }}>
                {truncateText(job.name, 75)}
              </MqText>
            </Box>
          </MQTooltip>
          {job.tags.slice(0, 3).map((tag, index) => (
            <Chip key={index} sx={{ mr: 1 }} size={'small'} label={tag} />
          ))}
          {job.tags.length > 3 && (
            <Chip sx={{ mr: 1 }} size={'small'} label={`+ ${job.tags.length - 3}`} />
          )}
        </Box>
      </Box>
      <Box display={'flex'}>
        <Box>
          <MqText subdued>LAST 10 RUNS</MqText>
          <Box display={'flex'} height={40} alignItems={'flex-end'}>
            {/*pad 10 - latestRuns length with a small grey bar*/}
            {Array.from({ length: 10 - (job.latestRuns?.length || 0) }, (_, i) => (
              <Box
                key={i}
                bgcolor={'divider'}
                mr={0.5}
                minHeight={2}
                width={5}
                sx={{
                  borderTopLeftRadius: theme.shape.borderRadius,
                  borderTopRightRadius: theme.shape.borderRadius,
                }}
              />
            ))}
            {reversedRuns.map((run) => {
              return (
                <MQTooltip
                  key={run.id}
                  title={
                    <>
                      <MqStatus label={run?.state} color={runStateColor(run.state)} />
                      <MqText sx={{ textAlign: 'center' }} subdued>
                        {run && run.durationMs ? stopWatchDuration(run.durationMs) : 'N/A'}
                      </MqText>
                    </>
                  }
                >
                  <Box
                    display={'flex'}
                    alignItems={'center'}
                    justifyContent={'space-between'}
                    bgcolor={runStateColor(run.state)}
                    mr={0.5}
                    minHeight={2}
                    width={5}
                    height={(run.durationMs / longestRun.durationMs) * 40}
                    sx={{
                      borderTopLeftRadius: theme.shape.borderRadius,
                      borderTopRightRadius: theme.shape.borderRadius,
                    }}
                  />
                </MQTooltip>
              )
            })}
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
          {job.latestRun ? (
            <MqStatus label={job.latestRun.state} color={runStateColor(job.latestRun.state)} />
          ) : (
            <MqStatus label={'N/A'} color={runStateColor('NEW')} />
          )}
        </Box>
        <Divider sx={{ mx: 2 }} flexItem orientation={'vertical'} />
        <Box>
          <MqText subdued>LAST RUN</MqText>
          <MqText>
            {job.latestRun && job.latestRun.durationMs
              ? stopWatchDuration(job.latestRun.durationMs)
              : 'N/A'}
          </MqText>
        </Box>
        <Box display={{ sm: 'none', md: 'inline-flex' }}>
          <Divider sx={{ mx: 2 }} flexItem orientation={'vertical'} />
          <Box>
            <MqText subdued>TYPE</MqText>
            <MqText font={'mono'}>{job.type}</MqText>
          </Box>
        </Box>
      </Box>
    </Box>
  )
}

export default JobRunItem
