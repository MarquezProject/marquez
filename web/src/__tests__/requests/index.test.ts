// SPDX-License-Identifier: Apache-2.0

import * as requestUtils from '../../store/requests'
import { parseResponse } from '../../store/requests'

export const mockFetch = (requestBody: any = []) => {
  return jest.fn().mockImplementation(() => Promise.resolve({
    json: () => Promise.resolve(requestBody),
    text: () => Promise.resolve(JSON.stringify(requestBody)),
    ok: true
  }))
}

const generateMockResponse = (status = 200, ok: boolean, returnBody?: object) => ({
  ok,
  status,
  json: () => returnBody || {},
  text: () => returnBody ? JSON.stringify(returnBody) : ''
})

describe('parseResponse function', () => {
  describe('for a successful response', () => {
    it('returns Success if body does not exist', async () => {
      const testResponse = generateMockResponse(201, true, null)
      expect(parseResponse(testResponse, 'testFunctionName')).resolves.toEqual('Success')
    })

    it('returns parsed body if body does exist', async () => {
      const testBody = { hasBody: true }
      const testResponse = generateMockResponse(200, true, testBody)
      expect(parseResponse(testResponse, 'testFunctionName')).resolves.toEqual(testBody)
    })
  })

  describe('for a unsuccessful response', () => {

    let spy
    let testResponse
    beforeEach(() => {
      spy = jest.spyOn(requestUtils, 'genericErrorMessageConstructor').mockImplementation(() => { })
      testResponse = generateMockResponse(500, false, {})
    })
    it('throws an error', async () => {
      await expect(parseResponse(testResponse, 'testFunctionName')).rejects.toThrow()
    })

    it('calls genericErrorMessageConstructor', async () => {
      expect(spy).toHaveBeenCalled()
    })
  })
})
