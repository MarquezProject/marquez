import { connect } from 'react-redux'
import * as Redux from 'redux'
import { bindActionCreators } from 'redux'
import Toast from '../components/Toast'
import { IState } from '../reducers'

const mapStateToProps = (state: IState) => ({
  error: state.display.error,
  success: state.display.success
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) => ({
  actions: bindActionCreators({}, dispatch)
})

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(Toast)
