import { connect } from 'react-redux'
import * as Redux from 'redux'
import { bindActionCreators } from 'redux'
import JobDetailPage from '../components/JobDetailPage'
import { IState } from '../reducers'

import { fetchJobRuns } from '../actionCreators'

const mapStateToProps = (state: IState) => ({
  jobs: state.jobs
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) => bindActionCreators({
  fetchJobRuns: fetchJobRuns
}, dispatch)

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(JobDetailPage)
