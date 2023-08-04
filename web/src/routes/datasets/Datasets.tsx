// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import * as Redux from 'redux'
import { ChevronLeftRounded, ChevronRightRounded } from '@mui/icons-material'
import {
  Container,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
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
    }
  }

  const i18next = require('i18next')
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
              <Table size='small'>
                <TableHead>
                  <TableRow>
                    <TableCell key={i18next.t('datasets_route.name_col')} align='left'>
                      <MqText subheading>{i18next.t('datasets_route.name_col')}</MqText>
                    </TableCell>
                    <TableCell key={i18next.t('datasets_route.namespace_col')} align='left'>
                      <MqText subheading>{i18next.t('datasets_route.namespace_col')}</MqText>
                    </TableCell>
                    <TableCell key={i18next.t('datasets_route.source_col')} align='left'>
                      <MqText subheading>{i18next.t('datasets_route.source_col')}</MqText>
                    </TableCell>
                    <TableCell key={i18next.t('datasets_route.updated_col')} align='left'>
                      <MqText subheading>{i18next.t('datasets_route.updated_col')}</MqText>
                    </TableCell>
                    <TableCell key={i18next.t('datasets_route.status_col')} align='left'>
                      <MqText subheading>{i18next.t('datasets_route.status_col')}</MqText>
                    </TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {datasets
                    .filter((dataset) => !dataset.deleted)
                    .map((dataset) => {
                      return (
                        <TableRow key={dataset.name}>
                          <TableCell align='left'>
                            <MqText
                              link
                              linkTo={`/lineage/${encodeNode(
                                'DATASET',
                                dataset.namespace,
                                dataset.name
                              )}`}
                            >
                              {dataset.name}
                            </MqText>
                          </TableCell>
                          <TableCell align='left'>
                            <MqText>{dataset.namespace}</MqText>
                          </TableCell>
                          <TableCell align='left'>
                            <MqText>{dataset.sourceName}</MqText>
                          </TableCell>
                          <TableCell align='left'>
                            <MqText>{formatUpdatedAt(dataset.updatedAt)}</MqText>
                          </TableCell>
                          <TableCell align='left'>
                            {datasetFacetsStatus(dataset.facets) ? (
                              <>
                                <MqStatus color={datasetFacetsStatus(dataset.facets)} />
                              </>
                            ) : (
                              <MqText>N/A</MqText>
                            )}
                          </TableCell>
                        </TableRow>
                      )
                    })}
                </TableBody>
              </Table>
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
