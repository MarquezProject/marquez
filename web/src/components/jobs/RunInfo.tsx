// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0
import * as Redux from 'redux'
import { Box } from '@mui/material'
import { IState } from '../../store/reducers'
import { Run } from '../../types/api'
import { connect } from 'react-redux'
import { fetchJobFacets, resetFacets } from '../../store/actionCreators'
import MqCode from '../core/code/MqCode'
import MqJsonView from '../core/json-view/MqJsonView'
import MqText from '../core/text/MqText'
import React, { FunctionComponent, useEffect } from 'react'

export interface DispatchProps {
  fetchJobFacets: typeof fetchJobFacets
  resetFacets: typeof resetFacets
}

interface JobFacets {
  [key: string]: object
}

export interface JobFacetsProps {
  jobFacets: JobFacets
}

export interface SqlFacet {
  query: string
}

export interface SourceCodeFacet {
  language: string
  sourceCode: string
  _producer: string
  _schemaURL: string
}

type RunInfoProps = {
  run: Run
} & JobFacetsProps &
  DispatchProps

const RunInfo: FunctionComponent<RunInfoProps> = (props) => {
  const { run, jobFacets, fetchJobFacets, resetFacets } = props

  useEffect(() => {
    fetchJobFacets(run.id)
  }, [])

  // unmounting
  useEffect(
    () => () => {
      resetFacets()
    },
    []
  )

  return (
    <Box>
      {<MqCode code={(jobFacets?.sql as SqlFacet)?.query} language={'sql'} />}
      {jobFacets?.sourceCode && (
        <MqCode
          code={(jobFacets.sourceCode as SourceCodeFacet)?.sourceCode}
          language={(jobFacets.sourceCode as SourceCodeFacet)?.language}
        />
      )}
      {jobFacets && (
        <Box mt={2}>
          <Box mb={1}>
            <MqText subheading>JOB FACETS</MqText>
          </Box>
          <MqJsonView data={jobFacets} aria-label={'Job facets'} aria-required='true' />
        </Box>
      )}
      {run.facets && (
        <Box mt={2}>
          <Box mb={1}>
            <MqText subheading>RUN FACETS</MqText>
          </Box>
          <MqJsonView data={run.facets} aria-label={'Run facets'} aria-required='true' />
        </Box>
      )}
    </Box>
  )
}

const mapStateToProps = (state: IState) => ({
  jobFacets: state.facets.result,
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  Redux.bindActionCreators(
    {
      fetchJobFacets: fetchJobFacets,
      resetFacets: resetFacets,
    },
    dispatch
  )

export default connect(mapStateToProps, mapDispatchToProps)(RunInfo)
