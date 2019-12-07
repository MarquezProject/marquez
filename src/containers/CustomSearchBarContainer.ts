import { connect } from 'react-redux'
import * as Redux from 'redux'
import { bindActionCreators } from 'redux'
import CustomSearchBar from '../components/CustomSearchBar'
import { IState } from '../reducers'

import { findMatchingEntities } from '../actionCreators'

const mapStateToProps = (_state: IState) => ({})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators({ findMatchingEntities: findMatchingEntities }, dispatch)

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(CustomSearchBar)
