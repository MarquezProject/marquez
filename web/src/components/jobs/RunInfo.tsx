// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { Box } from '@material-ui/core'
import { Run } from '../../types/api'
import { formatUpdatedAt } from '../../helpers'
import MqCode from '../core/code/MqCode'
import MqJsonView from '../core/json-view/MqJsonView'
import MqText from '../core/text/MqText'
import React, { FunctionComponent } from 'react'

interface RunInfoProps {
  run: Run
}

const RunInfo: FunctionComponent<RunInfoProps> = props => {
  const { run } = props
  const i18next = require('i18next')

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
            <MqText subheading>{i18next.t('jobs.runinfo_subhead')}</MqText>
          </Box>
          <MqJsonView data={run.facets} searchable={true} placeholder='Search' />
        </Box>
      )}
    </Box>
  )
}

export default RunInfo
