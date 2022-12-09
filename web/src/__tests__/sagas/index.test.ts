// SPDX-License-Identifier: Apache-2.0

import * as actionTypes from '../../store/actionCreators/actionTypes'
import * as actions from '../../store/actionCreators'
import * as api from '../../store/requests'
import * as matchers from 'redux-saga-test-plan/matchers'
import { expectSaga, testSaga } from 'redux-saga-test-plan'
import { fetchNamespaces } from '../../store/sagas/'

describe('Main (That\'s so Fetch) Saga', () => {
  const mockNamespaces = [
    { name: 'Mock Namespace 1', description: '', createdAt: '', owner: 'CDA' },
    { name: 'Mock Namespace 2', description: '', createdAt: '', owner: 'CDA' }
  ]

  let sagaTest

  describe('integration test', () => {
    beforeEach(() => {
      sagaTest = expectSaga(fetchNamespaces)
        // provide mock implementation return value for various functions & requests
        .provide([
          [matchers.call.fn(api.getNamespaces), { namespaces: mockNamespaces }]
        ])
    })

    it('calls api.fetchNamespaces', () => {
      return sagaTest.call(api.getNamespaces).run()
    })
  })

  describe('unit test', () => {
    it('when it works, yields a PUT to fetchDatasetsSuccess', () => {
      return sagaTest.put
        .like({ action: { type: actionTypes.FETCH_NAMESPACES_SUCCESS } })
        .run()
    })

    it('if an error is thrown, yields a PUT to applicationError', () => {
      const testError = new Error('Something went wrong')
      const unitTestSaga = testSaga(fetchNamespaces)
      expect(
        unitTestSaga
          .next()
          .call(api.getNamespaces)
          .next({ namespaces: [] })
          .throw(testError)
          .inspect(fn => {
            expect(fn.payload.action.type).toEqual(actions.applicationError(testError).type)
          })
      )
    })
  })
})
