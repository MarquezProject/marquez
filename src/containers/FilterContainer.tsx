import { connect } from 'react-redux'
import * as Redux from 'redux'
import { bindActionCreators } from 'redux'
import Filters from '../components/Filters'
import { IState } from '../reducers'

import { findMatchingEntities, filterDatasets, filterJobs } from '../actionCreators'
import React, { FunctionComponent } from 'react'
import { Namespace, Dataset } from '../types/api'

const mapStateToProps = (state: IState) => ({
  datasets: state.datasets,
  namespaces: state.namespaces
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators({ findMatchingEntities, filterDatasets, filterJobs }, dispatch)

export interface IProps {
  namespaces: Namespace[]
  datasets: Dataset[]
  filterJobs: typeof filterJobs
  filterDatasets: typeof filterDatasets
  showJobs: (bool: boolean) => void
}

/*  exporting for testing purposes */
export const FiltersWrapper: FunctionComponent<IProps> = props => {
  const { namespaces, datasets, filterJobs, filterDatasets, showJobs } = props
  const shouldRender = !!namespaces.length && !!datasets.length
  return shouldRender ? (
    <Filters
      namespaces={namespaces}
      datasets={datasets}
      filterDatasets={filterDatasets}
      filterJobs={filterJobs}
      showJobs={showJobs}
    />
  ) : null
}

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(FiltersWrapper)
