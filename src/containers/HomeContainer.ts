import { connect } from 'react-redux'
import * as Redux from 'redux'
import { bindActionCreators } from 'redux'
import Home from '../components/Home'
import { IState } from '../reducers'

import { findMatchingEntities } from '../actionCreators'

const mapStateToProps = (state: IState) => ({
  datasets: state.datasets,
  jobs: state.jobs
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators({ findMatchingEntities: findMatchingEntities }, dispatch)

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(Home)
