import { connect } from 'react-redux'
import * as Redux from 'redux'
import { bindActionCreators } from 'redux'
import DatasetDetailPage from '../components/DatasetDetailPage'
import { IState } from '../reducers'

const mapStateToProps = (state: IState) => ({
  datasets: state.datasets
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) => bindActionCreators({}, dispatch)

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(DatasetDetailPage)