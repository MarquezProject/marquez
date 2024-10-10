import { Box } from '@mui/system'
import { IntervalMetric } from '../../store/requests/intervalMetrics'
import { formatNumber } from '../../helpers/numbers'
import MiniGraph from './MiniGraph'
import MqText from '../../components/core/text/MqText'
import React from 'react'

interface Props {
  metrics: IntervalMetric[]
  isLoading: boolean
  label: string
  color: string
}

export const MiniGraphContainer = ({ metrics, label, color, isLoading }: Props) => {
  return (
    <>
      <Box display={'flex'} justifyContent={'space-between'} alignItems={'center'}>
        <MqText small font={'mono'} subdued>
          {label.toUpperCase()}
        </MqText>
        {metrics && metrics.length > 0 && (
          <MqText large>{formatNumber(metrics[metrics.length - 1].count)}</MqText>
        )}
      </Box>
      <MiniGraph intervalMetrics={metrics} color={color} label={label} isLoading={isLoading} />
    </>
  )
}
