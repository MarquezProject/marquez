// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import * as Redux from 'redux'
import { Box, Button } from '@mui/material'
import { Dataset } from '../../types/api'
import { IState } from '../../store/reducers'
import { LineageDataset } from '../lineage/types'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { fetchDataset, resetDataset } from '../../store/actionCreators'
import { fileSize } from '../../helpers'
import { saveAs } from 'file-saver'
import { useParams } from 'react-router-dom'
import MqEmpty from '../core/empty/MqEmpty'
import MqJsonView from '../../components/core/json-view/MqJsonView'
import MqText from '../core/text/MqText'
import React, { FunctionComponent, useEffect } from 'react'

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

const DatasetColumnLineage: FunctionComponent<IProps> = (props) => {
  const i18next = require('i18next')
  const { dataset, lineageDataset, fetchDataset, resetDataset } = props
  const { name, namespace } = useParams()

  useEffect(() => {
    if (namespace && name) {
      fetchDataset(namespace, name)
    }
  }, [name, namespace])

  // unmounting
  useEffect(
    () => () => {
      resetDataset()
    },
    []
  )

  const handleDownloadPayload = (data: object) => {
    const title = `${lineageDataset.name}-${lineageDataset.namespace}-columnLineage`
    const blob = new Blob([JSON.stringify(data)], { type: 'application/json' })
    saveAs(blob, `${title}.json`)
  }

  const columnLineage = dataset?.columnLineage
  return (
    <>
      {columnLineage ? (
        <>
          {fileSize(JSON.stringify(columnLineage)).kiloBytes > 500 ? (
            <Box p={2}>
              <MqEmpty title={'Payload is too big for render'}>
                <div>
                  <MqText subdued>Please click on button and download payload as file</MqText>
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
            <MqJsonView data={columnLineage} />
          )}
        </>
      ) : (
        <MqEmpty
          title={i18next.t('datasets_column_lineage.empty_title')}
          body={i18next.t('datasets_column_lineage.empty_body')}
        />
      )}
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
      resetDataset: resetDataset,
    },
    dispatch
  )

export default connect(mapStateToProps, mapDispatchToProps)(DatasetColumnLineage)
