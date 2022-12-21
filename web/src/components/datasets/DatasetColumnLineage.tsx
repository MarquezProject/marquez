// SPDX-License-Identifier: Apache-2.0

import { Box, Button } from '@material-ui/core'
import MqEmpty from '../core/empty/MqEmpty'
import MqJson from '../core/code/MqJson'
import MqText from '../core/text/MqText'
import React, { FunctionComponent } from 'react'
import { fileSize } from '../../helpers'
import { LineageDataset } from '../lineage/types'
import { saveAs } from 'file-saver'

interface DatasetColumnLineageProps {
  columnLineage: object
  lineageDataset: LineageDataset
}

const DatasetColumnLineage: FunctionComponent<DatasetColumnLineageProps> = props => {
  const { columnLineage, lineageDataset } = props

  const handleDownloadPayload = (data: object) => {
    const title = `${lineageDataset.name}-${lineageDataset.namespace}-columnLineage`
    const blob = new Blob([JSON.stringify(data)], { type: 'application/json' })
    saveAs(blob, `${title}.json`)
  }

  return (
    <>
      {columnLineage
        ? (
          <>
            {fileSize(JSON.stringify(columnLineage)).kiloBytes > 500 ? (
              <Box p={2}>
                <MqEmpty title={'Payload is too big for render'}>
                  <div>
                    <MqText subdued>
                      Please click on button and download payload as file
                    </MqText>
                    <br />
                    <Button
                      variant='outlined'
                      color='primary'
                      onClick={() => handleDownloadPayload(columnLineage)}
                    >
                      Download payload
                    </Button>
                  </div>
                </MqEmpty>
              </Box>
            ) : (
              <MqJson
                code={columnLineage}
                wrapLongLines={true}
                showLineNumbers={true}
              />
            )}
          </>
        )
        : (
          <MqEmpty title={'No column lineage'} body={'Column lineage not available for the specified dataset.'} />
        )
      }
    </>
  )
}

export default DatasetColumnLineage
