import { Box } from '@material-ui/core'
import React, { FunctionComponent } from 'react'
import MqCode from '../core/code/MqCode'
import MqText from '../core/text/MqText'
import {formatUpdatedAt} from '../../helpers'
import {Run} from '../../types/api'

interface RunInfoProps {
  run: Run
}

const RunInfo: FunctionComponent<RunInfoProps> = props => {
  const { run } = props

  return (
    <>
      <MqCode code={run.context.sql} />
      <Box display={'flex'} justifyContent={'flex-end'} alignItems={'center'} mt={1}>
        <Box ml={1}>
          <MqText subdued>{formatUpdatedAt(run.updatedAt)}</MqText>
        </Box>
      </Box>
      {run.facets && (
        <Box mt={2}>
          <Box mb={1}>
            <MqText subheading>Facets</MqText>
          </Box>
          <MqCode code={JSON.stringify(run.facets, null, '\t')} />
        </Box>
      )}
    </>
  )
}

export default RunInfo
