// SPDX-License-Identifier: Apache-2.0

import * as Redux from 'redux'
import { Container, Table, TableBody, TableCell, TableHead, TableRow } from '@material-ui/core'
import { Dataset } from '../../types/api'
import { IState } from '../../store/reducers'
import { MqScreenLoad } from '../../components/core/screen-load/MqScreenLoad'
import { Nullable } from '../../types/util/Nullable'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { encodeNode } from '../../helpers/nodes'
import { fetchDatasets, resetDatasets } from '../../store/actionCreators'
import { formatUpdatedAt } from '../../helpers'
import Box from '@material-ui/core/Box'
import MqEmpty from '../../components/core/empty/MqEmpty'
import MqText from '../../components/core/text/MqText'
import React from 'react'
import createStyles from '@material-ui/core/styles/createStyles'
import withStyles, { WithStyles } from '@material-ui/core/styles/withStyles'

const styles = () => createStyles({})

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

type DatasetsProps = WithStyles<typeof styles> & StateProps & DispatchProps

const DATASET_COLUMNS = ['Name', 'Namespace', 'Source', 'Updated At']

class Datasets extends React.Component<DatasetsProps> {
  componentDidMount() {
    if (this.props.selectedNamespace) {
      this.props.fetchDatasets(this.props.selectedNamespace)
    }
  }

  componentDidUpdate(prevProps: Readonly<DatasetsProps>) {
    if (
      prevProps.selectedNamespace !== this.props.selectedNamespace &&
      this.props.selectedNamespace
    ) {
      this.props.fetchDatasets(this.props.selectedNamespace)
    }
  }

  componentWillUnmount() {
    this.props.resetDatasets()
  }

  render() {
    const { datasets, isDatasetsLoading, isDatasetsInit } = this.props
    return (
      <Container maxWidth={'lg'} disableGutters>
        <MqScreenLoad loading={isDatasetsLoading || !isDatasetsInit}>
          <>
            {datasets.length === 0 ? (
              <Box p={2}>
                <MqEmpty title={'No datasets found'}>
                  <MqText subdued>
                    Try changing namespaces or consulting our documentation to add datasets.
                  </MqText>
                </MqEmpty>
              </Box>
            ) : (
              <>
                <Box p={2}>
                  <MqText heading>Datasets</MqText>
                </Box>
                <Table size='small'>
                  <TableHead>
                    <TableRow>
                      {DATASET_COLUMNS.map(field => {
                        return (
                          <TableCell key={field} align='left'>
                            <MqText subheading>{field}</MqText>
                          </TableCell>
                        )
                      })}
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {datasets.filter(dataset => !dataset.deleted).map(dataset => {
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

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(withStyles(styles)(Datasets))
