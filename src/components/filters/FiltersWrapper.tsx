import * as Redux from 'redux'
import { IState } from '../../reducers'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import Filters from './Filters'

import { Dataset, Namespace } from '../../types/api'
import { filterDatasets, filterJobs, findMatchingEntities } from '../../actionCreators'
import React, { FunctionComponent } from 'react'

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

const mapStateToProps = (state: IState) => ({
  datasets: state.datasets,
  namespaces: state.namespaces
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators({ findMatchingEntities, filterDatasets, filterJobs }, dispatch)

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(FiltersWrapper)
