// SPDX-License-Identifier: Apache-2.0

import { Box } from '@material-ui/core'
import { Run } from '../../types/api'
import { formatUpdatedAt } from '../../helpers'
import MqCode from '../core/code/MqCode'
import MqJson from '../core/code/MqJson'
import MqText from '../core/text/MqText'
import React, { FunctionComponent } from 'react'
import '../../i18n/config'
import { useTranslation } from 'react-i18next'

interface RunInfoProps {
  run: Run
}

const RunInfo: FunctionComponent<RunInfoProps> = props => {
  const { run } = props
  const { t } = useTranslation()

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
            <MqText subheading>{t('jobs.runinfo_subhead')}</MqText>
          </Box>
          <MqJson code={run.facets} />
        </Box>
      )}
    </Box>
  )
}

export default RunInfo
