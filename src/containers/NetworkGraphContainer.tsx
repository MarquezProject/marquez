import { connect } from 'react-redux'
import * as Redux from 'redux'
import { bindActionCreators } from 'redux'
import NetworkGraph from '../components/NetworkGraph'
import { IState } from '../reducers'


const mapStateToProps = (state: IState) => ({
  datasets: state.datasets,
  jobs: state.jobs,
  isLoading: state.display.isLoading,
  router: state.router
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) => ({
  actions: bindActionCreators({}, dispatch)
})

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(NetworkGraph)
