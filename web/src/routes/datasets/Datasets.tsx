// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import * as Redux from 'redux'
import { ChevronLeftRounded, ChevronRightRounded } from '@mui/icons-material'
import {
  Chip,
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
  totalCount: number
}

interface DatasetsState {
  page: number
}

interface DispatchProps {
  fetchDatasets: typeof fetchDatasets
  resetDatasets: typeof resetDatasets
}

type DatasetsProps = StateProps & DispatchProps

const PAGE_SIZE = 20

const Datasets: React.FC<DatasetsProps> = ({
  datasets,
  totalCount,
  isDatasetsLoading,
  isDatasetsInit,
  selectedNamespace,
  fetchDatasets,
  resetDatasets,
}) => {
  const defaultState = {
    page: 0,
  }
  const [state, setState] = React.useState<DatasetsState>(defaultState)

  const theme = createTheme(useTheme())

  React.useEffect(() => {
    if (selectedNamespace) {
      fetchDatasets(selectedNamespace, PAGE_SIZE, state.page * PAGE_SIZE)
    }
  }, [selectedNamespace, state.page])

  React.useEffect(() => {
    return () => {
      // on unmount
      resetDatasets()
    }
  }, [])

  const handleClickPage = (direction: 'prev' | 'next') => {
    const directionPage = direction === 'next' ? state.page + 1 : state.page - 1

    fetchDatasets(selectedNamespace || '', PAGE_SIZE, directionPage * PAGE_SIZE)
    // reset page scroll
    window.scrollTo(0, 0)
    setState({ ...state, page: directionPage })
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
              <Box p={2} display={'flex'}>
                <MqText heading>{i18next.t('datasets_route.heading')}</MqText>
                <Chip
                  size={'small'}
                  variant={'outlined'}
                  color={'primary'}
                  sx={{ marginLeft: 1 }}
                  label={totalCount + ' total'}
                ></Chip>
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
                    <TableCell key={i18next.t('datasets.column_lineage_tab')} align='left'>
                      <MqText inline subheading>
                        Column Level
                      </MqText>
                      <Chip
                        sx={{ marginLeft: 1 }}
                        size={'small'}
                        variant={'outlined'}
                        color={'warning'}
                        label={'beta'}
                      ></Chip>
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
                          <TableCell>
                            {dataset.columnLineage ? (
                              <MqText
                                link
                                linkTo={`column-level/${encodeURIComponent(
                                  encodeURIComponent(dataset.id.namespace)
                                )}/${encodeURIComponent(dataset.id.name)}`}
                              >
                                View
                              </MqText>
                            ) : (
                              <MqText>N/A</MqText>
                            )}
                          </TableCell>
                        </TableRow>
                      )
                    })}
                </TableBody>
              </Table>
              <Box display={'flex'} justifyContent={'flex-end'} alignItems={'center'} mb={2}>
                <MqText subdued>
                  <>
                    {PAGE_SIZE * state.page + 1} -{' '}
                    {Math.min(PAGE_SIZE * (state.page + 1), totalCount)} of {totalCount}
                  </>
                </MqText>
                <Tooltip title={i18next.t('events_route.previous_page')}>
                  <span>
                    <IconButton
                      sx={{
                        marginLeft: theme.spacing(2),
                      }}
                      color='primary'
                      disabled={state.page === 0}
                      onClick={() => handleClickPage('prev')}
                      size='large'
                    >
                      <ChevronLeftRounded />
                    </IconButton>
                  </span>
                </Tooltip>
                <Tooltip title={i18next.t('events_route.next_page')}>
                  <span>
                    <IconButton
                      color='primary'
                      onClick={() => handleClickPage('next')}
                      size='large'
                      disabled={state.page === Math.ceil(totalCount / PAGE_SIZE) - 1}
                    >
                      <ChevronRightRounded />
                    </IconButton>
                  </span>
                </Tooltip>
              </Box>
            </>
          )}
        </>
      </MqScreenLoad>
    </Container>
  )
}

const mapStateToProps = (state: IState) => ({
  datasets: state.datasets.result,
  totalCount: state.datasets.totalCount,
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
