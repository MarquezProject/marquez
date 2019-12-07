import { connect } from 'react-redux'
import * as Redux from 'redux'
import { bindActionCreators } from 'redux'
import Home from '../components/Home'
import { IState } from '../reducers'

const mapStateToProps = (state: IState) => ({
  datasets: state.datasets,
  jobs: state.jobs
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) => bindActionCreators({}, dispatch)

interface IInjectedProps {
  showJobs: boolean
  setShowJobs: (bool: boolean) => void
}

type IStateProps = ReturnType<typeof mapStateToProps>

interface IDispatchProps {}

export default connect<IStateProps, IDispatchProps, IInjectedProps>(
  mapStateToProps,
  mapDispatchToProps
)(Home)
