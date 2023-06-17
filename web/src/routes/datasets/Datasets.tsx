// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import * as Redux from 'redux'
import { Container, Table, TableBody, TableCell, TableHead, TableRow, Theme, Tooltip } from '@material-ui/core'
import { ChevronLeftRounded, ChevronRightRounded } from '@material-ui/icons'
import IconButton from '@material-ui/core/IconButton'
import { Dataset } from '../../types/api'
import { IState } from '../../store/reducers'
import { MqScreenLoad } from '../../components/core/screen-load/MqScreenLoad'
import { Nullable } from '../../types/util/Nullable'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { datasetFacetsStatus, encodeNode } from '../../helpers/nodes'
import { fetchDatasets, resetDatasets } from '../../store/actionCreators'
import { formatUpdatedAt } from '../../helpers'
import Box from '@material-ui/core/Box'
import MqEmpty from '../../components/core/empty/MqEmpty'
import MqStatus from '../../components/core/status/MqStatus'
import MqText from '../../components/core/text/MqText'
import React from 'react'
import createStyles from '@material-ui/core/styles/createStyles'
import withStyles, { WithStyles } from '@material-ui/core/styles/withStyles'

const PAGE_SIZE = 20

const styles = (theme: Theme) => createStyles({
  ml2: {
    marginLeft: theme.spacing(2)
  }
})

interface StateProps {
  datasets: Dataset[]
  isDatasetsLoading: boolean
  isDatasetsInit: boolean
  selectedNamespace: Nullable<string>
}

interface DispatchProps {
  fetchDatasets: typeof fetchDatasets
  resetDatasets: typeof resetDatasets
}

interface DatasetsState {
  datasets: Dataset[]
  page: number
  pageIsLast: boolean
}

type DatasetsProps = WithStyles<typeof styles> & StateProps & DispatchProps

class Datasets extends React.Component<DatasetsProps, DatasetsState> {

  constructor(props: DatasetsProps) {
    super(props)
    this.state = {
      datasets: [],
      page: 1,
      pageIsLast: false
    }
  }

  componentDidMount() {
    if (this.props.selectedNamespace) {
      this.props.fetchDatasets(this.props.selectedNamespace, PAGE_SIZE)
    }
  }

  componentDidUpdate(prevProps: Readonly<DatasetsProps>) {
    const { datasets: datasetsState, page } = this.state
    const { datasets: datasetsProps } = this.props

    if (
      prevProps.selectedNamespace !== this.props.selectedNamespace &&
      this.props.selectedNamespace
    ) {
      this.props.fetchDatasets(this.props.selectedNamespace, PAGE_SIZE)
    }

    if (datasetsProps !== datasetsState) {
      this.setState({
        datasets: datasetsProps,
        pageIsLast: datasetsProps.length < page * PAGE_SIZE
      })
    }
  }

  componentWillUnmount() {
    this.props.resetDatasets()
  }

  getDatasets() {
    const { datasets, page } = this.state
    return datasets.slice((page - 1) * PAGE_SIZE, PAGE_SIZE + (page - 1) * PAGE_SIZE)
  }

  pageNavigation() {
    const { datasets, page, pageIsLast } = this.state
    const titlePos = datasets.length < PAGE_SIZE && page === 1
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

  handleClickPage(direction: 'prev' | 'next') {
    const { page } = this.state
    const directionPage = direction === 'next' ? page + 1 : page - 1

    if (this.props.selectedNamespace) {
      this.props.fetchDatasets(
        this.props.selectedNamespace,
        PAGE_SIZE * directionPage
      )
    }
    this.setState({ page: directionPage })
  }

  render() {
    const { isDatasetsLoading, isDatasetsInit, classes } = this.props
    const { datasets, page, pageIsLast } = this.state
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
                <Box p={2} display={'flex'} justifyContent={'space-between'}>
                  <Box>
                    <MqText heading>{i18next.t('datasets_route.heading')}</MqText>
                    Page: {this.pageNavigation()}
                  </Box>
                  <Box>
                    <Tooltip title={i18next.t('events_route.previous_page')}>
                      <IconButton
                        className={classes.ml2}
                        color='primary'
                        disabled={page === 1}
                        onClick={() => this.handleClickPage('prev')}
                      >
                        <ChevronLeftRounded />
                      </IconButton>
                    </Tooltip>
                    <Tooltip title={i18next.t('events_route.next_page')}>
                      <IconButton
                        color='primary'
                        disabled={pageIsLast}
                        onClick={() => this.handleClickPage('next')}
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
                    {this.getDatasets()
                      .filter(dataset => !dataset.deleted)
                      .map(dataset => {
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
            <Box display={'flex'} justifyContent={'flex-end'} mb={2}>
              <Tooltip title={i18next.t('events_route.previous_page')}>
                <IconButton
                  className={classes.ml2}
                  color='primary'
                  disabled={page === 1}
                  onClick={() => this.handleClickPage('prev')}
                >
                  <ChevronLeftRounded />
                </IconButton>
              </Tooltip>
              <Tooltip title={i18next.t('events_route.next_page')}>
                <IconButton
                  color='primary'
                  disabled={pageIsLast}
                  onClick={() => this.handleClickPage('next')}
                >
                  <ChevronRightRounded />
                </IconButton>
              </Tooltip>
            </Box>
          </>
        </MqScreenLoad>
      </Container>
    )
  }
}

const mapStateToProps = (state: IState) => ({
  datasets: state.datasets.result,
  isDatasetsLoading: state.datasets.isLoading,
  isDatasetsInit: state.datasets.init,
  selectedNamespace: state.namespaces.selectedNamespace
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      fetchDatasets: fetchDatasets,
      resetDatasets: resetDatasets
    },
    dispatch
  )

export default connect(mapStateToProps, mapDispatchToProps)(withStyles(styles)(Datasets))