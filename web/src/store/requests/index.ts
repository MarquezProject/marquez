// SPDX-License-Identifier: Apache-2.0

import { APIError, HttpMethod } from '../../types'

export const genericErrorMessageConstructor = (functionName: string, error: APIError): string => {
  const { code, message, details } = error
  throw `${functionName} responded with error code ${code}: ${message}.  Here are the details: ${details}`
}

interface IParams {
  method: HttpMethod
  body?: string
}

export const parseResponse = async (response: Response, functionName: string) => {
  const body = await response.text()
  let json

  /*eslint no-unsafe-finally: "off"*/
  try {
    json = JSON.parse(body)
  } finally {
    if (response.ok) {
      return json || 'Success'
    } else {
      const errorMessage = json || {
        code: response.status,
        message: 'Unknown error occurred',
        details: body
      }
      const error = genericErrorMessageConstructor(functionName, errorMessage)
      throw new Error(error)
    }
  }
}

export const genericFetchWrapper = async (url: string, params: IParams, functionName: string) => {
  const response = await fetch(url, params)
  return parseResponse(response, functionName)
}

export * from './datasets'
export * from './namespaces'
export * from './jobs'
