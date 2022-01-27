// SPDX-License-Identifier: Apache-2.0

import { Box } from '@material-ui/core'
import { Run } from '../../types/api'
import { formatUpdatedAt } from '../../helpers'
import MqCode from '../core/code/MqCode'
import MqJson from '../core/code/MqJson'
import MqText from '../core/text/MqText'
import React, { FunctionComponent } from 'react'

interface RunInfoProps {
  run: Run
}

const RunInfo: FunctionComponent<RunInfoProps> = props => {
  const { run } = props

  return (
    <Box mt={2}>
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
          <MqJson code={run.facets} />
        </Box>
      )}
    </Box>
  )
}

export default RunInfo
