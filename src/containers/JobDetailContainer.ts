import { connect } from 'react-redux'
import * as Redux from 'redux'
import { bindActionCreators } from 'redux'
import JobDetailPage from '../components/JobDetailPage'
import { IState } from '../reducers'

import { findMatchingEntities } from '../actionCreators'

const mapStateToProps = (state: IState) => ({
  jobs: state.jobs
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) => bindActionCreators({}, dispatch)

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(JobDetailPage)
