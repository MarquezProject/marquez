import * as Redux from 'redux'
import { Dataset } from '../../types/api'
import { IState } from '../../store/reducers'
import { Theme } from '@material-ui/core/styles/createMuiTheme'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { fetchDatasets } from '../../store/actionCreators'
import Box from '@material-ui/core/Box'
import React from 'react'
import createStyles from '@material-ui/core/styles/createStyles'
import withStyles, { WithStyles } from '@material-ui/core/styles/withStyles'

const styles = (theme: Theme) => createStyles({})

interface StateProps {
  datasets: Dataset[]
}

interface DispatchProps {
  fetchDatasets: typeof fetchDatasets
}

type DatasetsProps = WithStyles<typeof styles> & StateProps & DispatchProps

class Datasets extends React.Component<DatasetsProps> {
  componentDidMount() {
    this.props.fetchDatasets('food_delivery')
  }

  render() {
    const { datasets } = this.props
    return (
      <Box>
        {datasets.map(dataset => {
          return <Box key={dataset.id.name}>{dataset.name}</Box>
        })}
      </Box>
    )
  }
}

const mapStateToProps = (state: IState) => ({
  datasets: state.datasets
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      fetchDatasets: fetchDatasets
    },
    dispatch
  )

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(withStyles(styles)(Datasets))
