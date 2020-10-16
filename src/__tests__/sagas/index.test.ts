import * as actionTypes from '../../constants/ActionTypes'
import * as actions from '../../actionCreators'
import * as api from '../../requests'
import * as matchers from 'redux-saga-test-plan/matchers'
import { expectSaga, testSaga } from 'redux-saga-test-plan'
import { fetchNamespacesDatasetsAndJobs } from '../../sagas/'

import { INamespaceAPI } from '../../types/api'

describe('Main (That\'s so Fetch) Saga', () => {
  const mockNamespaces: INamespaceAPI[] = [
    { name: 'Mock Namespace 1', description: '', createdAt: '', owner: 'CDA' },
    { name: 'Mock Namespace 2', description: '', createdAt: '', owner: 'CDA' }
  ]

  let sagaTest

  describe('integration test', () => {
    beforeEach(() => {
      sagaTest = expectSaga(fetchNamespacesDatasetsAndJobs)
        // provide mock implementation return value for various functions & requests
        .provide([
          [matchers.call.fn(api.fetchNamespaces), { namespaces: mockNamespaces }],
          [matchers.call.fn(api.fetchDatasets), { datasets: [] }],
          [matchers.call.fn(api.fetchJobs), { jobs: [] }]
        ])
    })

    it('calls api.fetchNamespaces', () => {
      return sagaTest.call(api.fetchNamespaces).run()
    })

    it('calls api.fetchDatasets for each namespace that is returned', () => {
      mockNamespaces.forEach(n => {
        sagaTest.call(api.fetchDatasets, n)
      })
      return sagaTest.run()
    })
  })

  describe('unit test', () => {
    it('when it works, yields a PUT to fetchDatasetsSuccess', () => {
      return sagaTest.put
        .like({ action: { type: actionTypes.FETCH_NAMESPACES_SUCCESS } })
        .put.like({ action: { type: actionTypes.FETCH_DATASETS_SUCCESS } })
        .put.like({ action: { type: actionTypes.FETCH_JOBS_SUCCESS } })
        .run()
    })

    it('if an error is thrown, yields a PUT to applicationError', () => {
      const testError = new Error('Something went wrong')
      const unitTestSaga = testSaga(fetchNamespacesDatasetsAndJobs)
      expect(
        unitTestSaga
          .next()
          .call(api.fetchNamespaces)
          .next({ namespaces: [] })
          .throw(testError)
          .inspect(fn => {
            expect(fn.payload.action.type).toEqual(actions.applicationError(testError).type)
          })
      )
    })
  })
})
