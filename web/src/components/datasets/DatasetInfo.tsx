// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { Box, Table, TableBody, TableCell, TableHead, TableRow } from '@material-ui/core'
import { Field, Run } from '../../types/api'
import { stopWatchDuration } from '../../helpers/time'
import MqCode from '../core/code/MqCode'
import MqEmpty from '../core/empty/MqEmpty'
import MqJsonView from '../core/json-view/MqJsonView'
import MqText from '../core/text/MqText'
import React, { FunctionComponent, useEffect } from 'react'
import RunStatus from '../jobs/RunStatus'
import * as Redux from 'redux'
import { IState } from '../../store/reducers'
import { connect } from 'react-redux'
import { fetchJobFacets, resetFacets } from '../../store/actionCreators'

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


type DatasetInfoProps = {
  datasetFields: Field[]
  facets?: object
  run?: Run
} & JobFacetsProps &
  DispatchProps

const DatasetInfo: FunctionComponent<DatasetInfoProps> = props => {
  const { datasetFields, facets, run, jobFacets, fetchJobFacets, resetFacets } = props
  const i18next = require('i18next')

  useEffect(() => {
    run && fetchJobFacets(run.id)
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
      {datasetFields.length === 0 && (
        <MqEmpty
          title={i18next.t('dataset_info.empty_title')}
          body={i18next.t('dataset_info.empty_body')}
        />
      )}
      {datasetFields.length > 0 && (
        <Table size='small'>
          <TableHead>
            <TableRow>
              <TableCell align='left'>
                <MqText subheading inline>
                  {i18next.t('dataset_info_columns.name')}
                </MqText>
              </TableCell>
              <TableCell align='left'>
                <MqText subheading inline>
                  {i18next.t('dataset_info_columns.type')}
                </MqText>
              </TableCell>
              <TableCell align='left'>
                <MqText subheading inline>
                  {i18next.t('dataset_info_columns.description')}
                </MqText>
              </TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {datasetFields.map(field => {
              return (
                <TableRow key={field.name}>
                  <TableCell align='left'>{field.name}</TableCell>
                  <TableCell align='left'>{field.type}</TableCell>
                  <TableCell align='left'>{field.description || 'no description'}</TableCell>
                </TableRow>
              )
            })}
          </TableBody>
        </Table>
      )}
      {facets && (
        <Box mt={2}>
          <Box mb={1}>
            <MqText subheading>{i18next.t('dataset_info.facets_subhead')}</MqText>
          </Box>
          <MqJsonView data={facets} searchable={true} placeholder='Search' />
        </Box>
      )}
      {run && (
        <Box mt={2}>
          <Box mb={1}>
            <Box display={'flex'} alignItems={'center'} justifyContent={'space-between'}>
              <Box display={'flex'} alignItems={'center'}>
                <RunStatus run={run} />
                <MqText subheading>{i18next.t('dataset_info.run_subhead')}</MqText>
              </Box>
              <Box display={'flex'}>
                <MqText bold>{i18next.t('dataset_info.duration')}&nbsp;</MqText>
                <MqText subdued>{stopWatchDuration(run.durationMs)}</MqText>
              </Box>
            </Box>
            <MqText subdued>{run.jobVersion && run.jobVersion.name}</MqText>
          </Box>
          {<MqCode code={(jobFacets?.sql as SqlFacet)?.query} language={'sql'}/>}
        </Box>
      )}
    </Box>
  )
}

const mapStateToProps = (state: IState) => ({
  jobFacets: state.facets.result
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  Redux.bindActionCreators(
    {
      fetchJobFacets: fetchJobFacets,
      resetFacets: resetFacets
    },
    dispatch
  )

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(DatasetInfo)
