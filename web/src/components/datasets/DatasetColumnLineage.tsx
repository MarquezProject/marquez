// SPDX-License-Identifier: Apache-2.0

import React, { FunctionComponent, useEffect } from 'react'
import * as Redux from 'redux'
import { Box, Button } from '@material-ui/core'
import MqEmpty from '../core/empty/MqEmpty'
import MqJson from '../core/code/MqJson'
import MqText from '../core/text/MqText'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { Dataset } from '../../types/api'
import { fetchDataset, resetDataset } from '../../store/actionCreators'
import { fileSize } from '../../helpers'
import { LineageDataset } from '../lineage/types'
import { saveAs } from 'file-saver'
import { IState } from '../../store/reducers'

interface DatasetColumnLineageProps {
  lineageDataset: LineageDataset
}

interface StateProps {
  dataset: Dataset
}

interface DispatchProps {
  fetchDataset: typeof fetchDataset
  resetDataset: typeof resetDataset
}

type IProps = DatasetColumnLineageProps & DispatchProps & StateProps

const DatasetColumnLineage: FunctionComponent<IProps> = props => {
  const { dataset, lineageDataset, fetchDataset, resetDataset } = props
  const columnLineage = dataset.columnLineage

  useEffect(() => {
    fetchDataset(lineageDataset.namespace, lineageDataset.name)
  }, [lineageDataset.name])

  // unmounting
  useEffect(() => () => {
    resetDataset()
  }, [])

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

const mapStateToProps = (state: IState) => ({
  dataset: state.dataset.result,
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      fetchDataset: fetchDataset,
      resetDataset: resetDataset
    },
    dispatch
  )

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(DatasetColumnLineage)