import * as React from 'react'
import * as Redux from 'redux'
import { IState } from '../reducers'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
const styles = require('./Toast.css')

interface IProps {
  error?: string
  success?: string
}

export const Toast = (props: IProps) =>
  props.error || props.success ? (
    <div
      className={`${styles.container} ${
        props.error ? styles.error : styles.success
      } shadow animated faster bounceInUp`}
    >
      <p>{props.error || props.success}</p>
    </div>
  ) : null

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
