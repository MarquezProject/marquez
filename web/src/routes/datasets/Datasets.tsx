import * as Redux from 'redux'
import { Container, Table, TableBody, TableCell, TableHead, TableRow } from '@material-ui/core'
import { Dataset } from '../../types/api'
import { IState } from '../../store/reducers'
import { Pagination } from '@material-ui/lab'
import { Theme } from '@material-ui/core/styles/createMuiTheme'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { fetchDatasets, resetDatasets } from '../../store/actionCreators'
import { formatUpdatedAt } from '../../helpers'
import Box from '@material-ui/core/Box'
import MqText from '../../components/core/text/MqText'
import React from 'react'
import createStyles from '@material-ui/core/styles/createStyles'
import withStyles, { WithStyles } from '@material-ui/core/styles/withStyles'

const styles = (theme: Theme) => createStyles({})

interface StateProps {
  datasets: Dataset[]
}

interface DispatchProps {
  fetchDatasets: typeof fetchDatasets
  resetDatasets: typeof resetDatasets
}

type DatasetsProps = WithStyles<typeof styles> & StateProps & DispatchProps

const DATASET_COLUMNS = ['Name', 'Namespace', 'Updated At']

class Datasets extends React.Component<DatasetsProps> {
  componentDidMount() {
    this.props.fetchDatasets('food_delivery')
  }

  componentWillUnmount() {
    this.props.resetDatasets()
  }

  render() {
    const { datasets } = this.props
    return (
      <Container maxWidth={'lg'} disableGutters>
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
            {datasets.map(dataset => {
              return (
                <TableRow key={dataset.name}>
                  <TableCell align='left'>
                    <MqText>{dataset.name}</MqText>
                  </TableCell>
                  <TableCell align='left'>
                    <MqText>{dataset.namespace}</MqText>
                  </TableCell>
                  <TableCell align='left'>
                    <MqText>{formatUpdatedAt(dataset.updatedAt)}</MqText>
                  </TableCell>
                </TableRow>
              )
            })}
          </TableBody>
        </Table>
        <Box display={'flex'} justifyContent={'flex-end'} mt={2} mr={2}>
          <Pagination color={'standard'} shape={'rounded'} onChange={() => {}} count={10} />
        </Box>
      </Container>
    )
  }
}

const mapStateToProps = (state: IState) => ({
  datasets: state.datasets
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
