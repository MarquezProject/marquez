import * as Redux from 'redux'
import { Box } from '@mui/system'
import {
  CircularProgress,
  Divider,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
} from '@mui/material'
import { ColumnLineageGraph, Dataset } from '../../types/api'
import { IState } from '../../store/reducers'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { fetchDataset } from '../../store/actionCreators'
import { findConnectedNodes } from './layout'
import { useSearchParams } from 'react-router-dom'
import CloseIcon from '@mui/icons-material/Close'
import IconButton from '@mui/material/IconButton'
import MqJsonView from '../../components/core/json-view/MqJsonView'
import MqText from '../../components/core/text/MqText'
import React, { useEffect } from 'react'

const i18next = require('i18next')

const WIDTH = 600

interface StateProps {
  dataset: Dataset
  isDatasetLoading: boolean
  columnLineage: ColumnLineageGraph
}
interface DispatchProps {
  fetchDataset: typeof fetchDataset
}
const ColumnLevelDrawer = ({
  dataset,
  fetchDataset,
  columnLineage,
  isDatasetLoading,
}: StateProps & DispatchProps) => {
  const [searchParams, setSearchParams] = useSearchParams()
  useEffect(() => {
    const dataset = searchParams.get('dataset')
    const namespace = searchParams.get('namespace')
    if (dataset && namespace) {
      fetchDataset(namespace, dataset)
    }
  }, [])

  if (!columnLineage) {
    return null
  }

  const column = searchParams.get('column')
  const connectedColumns = findConnectedNodes(columnLineage.graph, column)

  return (
    <Box width={`${WIDTH}px`}>
      <Box p={2} display={'flex'} justifyContent={'space-between'} alignItems={'center'}>
        <MqText heading>{searchParams.get('dataset')}</MqText>
        <IconButton
          onClick={() => {
            setSearchParams({})
          }}
        >
          <CloseIcon />
        </IconButton>
      </Box>
      <Divider />
      {!dataset || isDatasetLoading ? (
        <Box mt={2} display={'flex'} justifyContent={'center'}>
          <CircularProgress color='primary' />
        </Box>
      ) : (
        <>
          <Box p={2}>
            <MqText subheading>Schema</MqText>
          </Box>
          {dataset.fields.length > 0 && (
            <Table size='small'>
              <TableHead>
                <TableRow>
                  <TableCell align='left'>
                    <MqText subheading inline>
                      {i18next.t('dataset_info_columns.name')}
                    </MqText>
                  </TableCell>
                  <TableCell align='left'>
                    <MqText subheading inline>
                      {i18next.t('dataset_info_columns.type')}
                    </MqText>
                  </TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {dataset.fields.map((field) => {
                  return (
                    <TableRow key={field.name}>
                      <TableCell align='left'>{field.name}</TableCell>
                      <TableCell align='left'>{field.type}</TableCell>
                    </TableRow>
                  )
                })}
              </TableBody>
            </Table>
          )}
          {connectedColumns.length > 0 && column && (
            <>
              <Box p={2}>
                <MqText bold subheading>
                  Connected Columns
                </MqText>
              </Box>
              <Table size='small'>
                <TableHead>
                  <TableRow>
                    <TableCell align='left'>
                      <MqText subheading inline>
                        Dataset
                      </MqText>
                    </TableCell>
                    <TableCell align='left'>
                      <MqText subheading inline>
                        Field
                      </MqText>
                    </TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {connectedColumns.map((c) => {
                    return (
                      <TableRow key={c.id}>
                        <TableCell align='left'>{c.data?.dataset || 'Unknown'}</TableCell>
                        <TableCell align='left'>{c.data?.field || 'Unknown'}</TableCell>
                      </TableRow>
                    )
                  })}
                </TableBody>
              </Table>
            </>
          )}
        </>
      )}
      {dataset && dataset.columnLineage && (
        <>
          <Box p={2}>
            <MqText subheading>Column Lineage Facets</MqText>

            <MqJsonView data={dataset.columnLineage} />
          </Box>
        </>
      )}
    </Box>
  )
}

const mapStateToProps = (state: IState) => ({
  dataset: state.dataset.result,
  isDatasetLoading: state.dataset.isLoading,
  columnLineage: state.columnLineage.columnLineage,
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      fetchDataset: fetchDataset,
    },
    dispatch
  )
export default connect(mapStateToProps, mapDispatchToProps)(ColumnLevelDrawer)
