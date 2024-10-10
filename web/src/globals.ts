// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

//  from webpack
declare const __NODE_ENV__: string
declare const __DEVELOPMENT__: boolean

declare const __API_URL__: string
declare const __API_BETA_URL__: string
declare const __REACT_APP_ADVANCED_SEARCH__: boolean

declare const __FEEDBACK_FORM_URL__: string
declare const __API_DOCS_URL__: string

export const API_URL = __API_URL__
export const API_BETA_URL = __API_BETA_URL__
export const FEEDBACK_FORM_URL = __FEEDBACK_FORM_URL__
export const API_DOCS_URL = __API_DOCS_URL__
export const REACT_APP_ADVANCED_SEARCH = __REACT_APP_ADVANCED_SEARCH__
