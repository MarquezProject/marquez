// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import * as Redux from 'redux'
import { ChevronLeftRounded, ChevronRightRounded } from '@mui/icons-material'
import {
  Container,
  Tooltip,
  createTheme,
} from '@mui/material'
import { Dataset } from '../../types/api'
import { IState } from '../../store/reducers'
import { MqScreenLoad } from '../../components/core/screen-load/MqScreenLoad'
import { Nullable } from '../../types/util/Nullable'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { datasetFacetsStatus, encodeNode } from '../../helpers/nodes'
import { fetchDatasets, resetDatasets } from '../../store/actionCreators'
import { formatUpdatedAt } from '../../helpers'
import { useTheme } from '@emotion/react'
import Box from '@mui/material/Box'
import IconButton from '@mui/material/IconButton'
import MqEmpty from '../../components/core/empty/MqEmpty'
import MqStatus from '../../components/core/status/MqStatus'
import MqText from '../../components/core/text/MqText'
import React from 'react'
import { DataGrid, GridColDef } from '@mui/x-data-grid'

interface StateProps {
  datasets: Dataset[]
  isDatasetsLoading: boolean
  isDatasetsInit: boolean
  selectedNamespace: Nullable<string>
}

interface DatasetsState {
  datasets: Dataset[]
  page: number
  pageIsLast: boolean
}

interface DispatchProps {
  fetchDatasets: typeof fetchDatasets
  resetDatasets: typeof resetDatasets
}

type DatasetsProps = StateProps & DispatchProps

const Datasets: React.FC<DatasetsProps> = ({
  datasets,
  isDatasetsLoading,
  isDatasetsInit,
  selectedNamespace,
  fetchDatasets,
  resetDatasets,
}) => {
  const PAGE_SIZE = 20
  const mounted = React.useRef<boolean>(false)
  const prevSelectedNamespace = React.useRef<Nullable<string>>()

  const defaultState = {
    datasets: [],
    page: 1,
    pageIsLast: false,
  }
  const [state, setState] = React.useState<DatasetsState>(defaultState)

  const theme = createTheme(useTheme())

  React.useEffect(() => {
    if (!mounted.current) {
      // on mount
      if (selectedNamespace) {
        fetchDatasets(selectedNamespace, PAGE_SIZE)
      }
      mounted.current = true
    } else {
      // on update
      if (prevSelectedNamespace.current !== selectedNamespace && selectedNamespace) {
        fetchDatasets(selectedNamespace, PAGE_SIZE)
        setState(defaultState)
      }

      if (datasets !== state.datasets) {
        setState({
          ...state,
          datasets,
          pageIsLast: datasets.length < state.page * PAGE_SIZE,
        })
      }

      prevSelectedNamespace.current = selectedNamespace
    }
  })

  React.useEffect(() => {
    return () => {
      // on unmount
      resetDatasets()
    }
  }, [])

  const pageNavigation = () => {
    const { datasets, page, pageIsLast } = state
    const titlePos =
      datasets.length < PAGE_SIZE && page === 1
        ? `1 - ${datasets.length}`
        : datasets.length > PAGE_SIZE && page === 1
        ? `1 - ${PAGE_SIZE}`
        : datasets.length && page > 1 && pageIsLast === false
        ? `${PAGE_SIZE * page - PAGE_SIZE + 1} - ${PAGE_SIZE * page}`
        : datasets.length && page > 1 && pageIsLast
        ? `${PAGE_SIZE * page - PAGE_SIZE + 1} - ${datasets.length}`
        : `${datasets.length}`
    return `${page} (${titlePos})`
  }

  const handleClickPage = (direction: 'prev' | 'next') => {
    const directionPage = direction === 'next' ? state.page + 1 : state.page - 1

    if (selectedNamespace) {
      fetchDatasets(selectedNamespace, PAGE_SIZE * directionPage)
      setState({ ...state, page: directionPage })
      setPaginationModel({ ...paginationModel, page: directionPage - 1 })
    }
  }

  const i18next = require('i18next')

  const columns: GridColDef[] = [
    {
      field: 'name',
      headerName: i18next.t('datasets_route.name_col'),
      renderCell: (params) => {
        return <MqText
          link
          linkTo={`/lineage/${encodeNode(
            'DATASET',
            params.row.namespace,
            params.row.name
          )}`}
        >
          {params.row.name}
        </MqText>
      },
      width: 400
    },
    { field: 'namespace', headerName: i18next.t('datasets_route.namespace_col'), width: 200 },
    { field: 'sourceName', headerName: i18next.t('datasets_route.source_col'), width: 200 },
    {
      field: 'updatedAt',
      headerName: i18next.t('datasets_route.updated_col'),
      renderCell: (params) => {
        return <MqText>{formatUpdatedAt(params.row.updatedAt)}</MqText>
      },
      width: 200
    },
    {
      field: 'facets',
      headerName: i18next.t('datasets_route.status_col'),
      renderCell: (params) => {
        return (datasetFacetsStatus(params.row.facets) ? (
          <>
            <MqStatus color={datasetFacetsStatus(params.row.facets)} />
          </>
        ) : (
          <MqText>N/A</MqText>
        ))
      },
      width: 200
    }
  ]

  const [paginationModel, setPaginationModel] = React.useState({
    pageSize: PAGE_SIZE,
    page: 0,
  });


  return (
    <Container maxWidth={'lg'} disableGutters>
      <MqScreenLoad loading={isDatasetsLoading || !isDatasetsInit}>
        <>
          {datasets.length === 0 ? (
            <Box p={2}>
              <MqEmpty title={i18next.t('datasets_route.empty_title')}>
                <MqText subdued>{i18next.t('datasets_route.empty_body')}</MqText>
              </MqEmpty>
            </Box>
          ) : (
            <>
              <Box display={'flex'} justifyContent={'space-between'} p={2}>
                <Box>
                  <MqText heading>{i18next.t('datasets_route.heading')}</MqText>
                  Page: {pageNavigation()}
                </Box>
                <Box>
                  <Tooltip title={i18next.t('events_route.previous_page')}>
                    <IconButton
                      sx={{
                        marginLeft: theme.spacing(2),
                      }}
                      color='primary'
                      disabled={state.page === 1}
                      onClick={() => handleClickPage('prev')}
                      size='large'
                    >
                      <ChevronLeftRounded />
                    </IconButton>
                  </Tooltip>
                  <Tooltip title={i18next.t('events_route.next_page')}>
                    <IconButton
                      color='primary'
                      disabled={state.pageIsLast}
                      onClick={() => handleClickPage('next')}
                      size='large'
                    >
                      <ChevronRightRounded />
                    </IconButton>
                  </Tooltip>
                </Box>
              </Box>
                <DataGrid
                  rows={datasets.filter(dataset => !dataset.deleted)}
                  columns={columns}
                  getRowId={(row) => JSON.stringify(row.id)}
                  disableRowSelectionOnClick
                  paginationModel={paginationModel}
                  onPaginationModelChange={setPaginationModel}
                />
            </>
          )}
        </>
      </MqScreenLoad>
    </Container>
  )
}

const mapStateToProps = (state: IState) => ({
  datasets: state.datasets.result,
  isDatasetsLoading: state.datasets.isLoading,
  isDatasetsInit: state.datasets.init,
  selectedNamespace: state.namespaces.selectedNamespace,
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      fetchDatasets: fetchDatasets,
      resetDatasets: resetDatasets,
    },
    dispatch
  )

export default connect(mapStateToProps, mapDispatchToProps)(Datasets)
