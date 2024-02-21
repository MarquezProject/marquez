// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0
import * as Redux from 'redux'
import { Box } from '@mui/material'
import { IState } from '../../store/reducers'
import { Run } from '../../types/api'
import { connect } from 'react-redux'
import { fetchJobFacets, resetFacets } from '../../store/actionCreators'
import { formatUpdatedAt } from '../../helpers'
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

type RunInfoProps = {
  run: Run
} & JobFacetsProps &
  DispatchProps

const RunInfo: FunctionComponent<RunInfoProps> = (props) => {
  const { run, jobFacets, fetchJobFacets, resetFacets } = props
  const i18next = require('i18next')

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
    <Box mt={2}>
      {<MqCode code={(jobFacets?.sql as SqlFacet)?.query} language={'sql'} />}
      <Box display={'flex'} justifyContent={'flex-end'} alignItems={'center'} mt={1}>
        <Box ml={1}>
          <MqText subdued>{formatUpdatedAt(run.updatedAt)}</MqText>
        </Box>
      </Box>
      {run.facets && (
        <Box mt={2}>
          <Box mb={1}>
            <MqText subheading>{i18next.t('jobs.runinfo_subhead')}</MqText>
          </Box>
          <MqJsonView
            data={run.facets}
            aria-label={i18next.t('jobs.facets_subhead_aria')}
            aria-required='true'
            placeholder={i18next.t('jobs.search')}
          />
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
