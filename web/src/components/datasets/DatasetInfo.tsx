// Copyright 2018-2024 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0
import * as Redux from 'redux'
import { Box, Chip, Table, TableBody, TableCell, TableHead, TableRow } from '@mui/material'
import { Field, Run } from '../../types/api'
import { IState } from '../../store/reducers'
import { connect, useSelector } from 'react-redux'
import { fetchJobFacets, resetFacets } from '../../store/actionCreators'
import DatasetTags from './DatasetTags'
import MqEmpty from '../core/empty/MqEmpty'
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

type DatasetInfoProps = {
  datasetFields: Field[]
  facets?: object
  run?: Run
  showTags?: boolean
} & JobFacetsProps &
  DispatchProps

const DatasetInfo: FunctionComponent<DatasetInfoProps> = (props) => {
  const { datasetFields, facets, run, fetchJobFacets, resetFacets, showTags } = props
  const i18next = require('i18next')
  const dsNamespace = useSelector(
    (state: IState) => state.datasetVersions.result.versions[0].namespace
  )
  const dsName = useSelector((state: IState) => state.datasetVersions.result.versions[0].name)

  useEffect(() => {
    run && fetchJobFacets(run.id)
  }, [run])

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
        <>
          <Table size='small'>
            <TableHead>
              <TableRow>
                <TableCell align='left'>
                  <MqText subheading inline>
                    {i18next.t('dataset_info_columns.name')}
                  </MqText>
                </TableCell>
                {!showTags && (
                  <TableCell align='left'>
                    <MqText subheading inline>
                      {i18next.t('dataset_info_columns.type')}
                    </MqText>
                  </TableCell>
                )}
                {!showTags && (
                  <TableCell align='left'>
                    <MqText subheading inline>
                      {i18next.t('dataset_info_columns.description')}
                    </MqText>
                  </TableCell>
                )}
                {showTags && (
                  <TableCell align='left'>
                    <MqText subheading inline>
                      {i18next.t('dataset_tags.tags')}
                    </MqText>
                  </TableCell>
                )}
              </TableRow>
            </TableHead>
            <TableBody>
              {datasetFields.map((field) => {
                return (
                  <React.Fragment key={field.name}>
                    <TableRow>
                      <TableCell align='left'>{field.name}</TableCell>
                      {!showTags && (
                        <TableCell align='left'>
                          <Chip size={'small'} label={field.type} variant={'outlined'} />
                        </TableCell>
                      )}
                      {!showTags && (
                        <TableCell align='left'>{field.description || 'no description'}</TableCell>
                      )}
                      {showTags && (
                        <TableCell align='left'>
                          <DatasetTags
                            namespace={dsNamespace}
                            datasetName={dsName}
                            datasetTags={field.tags}
                            datasetField={field.name}
                          />
                        </TableCell>
                      )}
                    </TableRow>
                  </React.Fragment>
                )
              })}
            </TableBody>
          </Table>
        </>
      )}
      {facets && (
        <Box mt={2}>
          <Box mb={1}>
            <MqText subheading>{i18next.t('dataset_info.facets_subhead')}</MqText>
          </Box>
          <MqJsonView data={facets} aria-label={i18next.t('dataset_info.facets_subhead_aria')} />
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

export default connect(mapStateToProps, mapDispatchToProps)(DatasetInfo)
