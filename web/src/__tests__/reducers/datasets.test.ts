// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import * as actionTypes from '../../store/actionCreators/actionTypes'
import datasetsReducer, {IDatasetsAction, initialState} from '../../store/reducers/datasets'

const datasets = require('../../../docker/db/data/datasets.json')

describe('datasets reducer', () => {
  it('should handle FETCH_DATASETS_SUCCESS', () => {
    const action = {
      type: actionTypes.FETCH_DATASETS_SUCCESS,
      payload: {
        datasets: datasets,
        totalCount: 16
      }
    } as IDatasetsAction
    expect(datasetsReducer(initialState, action)).toStrictEqual({
      init: true,
      isLoading: false,
      result: datasets,
      totalCount: 16,
      deletedDatasetName: '',
      refreshTags: false
    })
  })

})
