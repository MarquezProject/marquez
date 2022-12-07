// SPDX-License-Identifier: Apache-2.0

import { Box } from '@material-ui/core'
import MqEmpty from '../core/empty/MqEmpty'
import MqJson from '../core/code/MqJson'
import React, { FunctionComponent } from 'react'

interface DatasetColumnLineageProps {
  columnLineage: object
}

const DatasetColumnLineage: FunctionComponent<DatasetColumnLineageProps> = props => {
  const { columnLineage } = props

  return (
    <Box>
      {columnLineage === null && <MqEmpty title={'No column lineage'} body={'Column lineage not available for the specified dataset.'} />}
      {columnLineage && (
        <Box mt={2}>
          <MqJson code={columnLineage} />
        </Box>
      )}
    </Box>
  )
}

export default DatasetColumnLineage
