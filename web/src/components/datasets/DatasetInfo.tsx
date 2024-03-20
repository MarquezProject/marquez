// Copyright 2018-2024 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0
import * as Redux from 'redux'
import { Box, Table, TableBody, TableCell, TableHead, TableRow } from '@mui/material'
import { Field, Run } from '../../types/api'
import { IState } from '../../store/reducers'

import { connect, useSelector } from 'react-redux'
import { fetchJobFacets, resetFacets } from '../../store/actionCreators'
import Collapse from '@mui/material/Collapse'
import DatasetTags from './DatasetTags'
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown'
import MqEmpty from '../core/empty/MqEmpty'
import MqJsonView from '../core/json-view/MqJsonView'
import MqText from '../core/text/MqText'
import React, { FunctionComponent, useEffect, useState } from 'react'

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

const DatasetInfo: FunctionComponent<DatasetInfoProps> = (props) => {
  const { datasetFields, facets, run, fetchJobFacets, resetFacets } = props
  const i18next = require('i18next')
  const dsNamespace = useSelector(
    (state: IState) => state.datasetVersions.result.versions[0].namespace
  )
  const dsName = useSelector((state: IState) => state.datasetVersions.result.versions[0].name)

  const loadCollapsedState = () => {
    const storedState = localStorage.getItem(`dsi_${dsNamespace}_${dsName}`)
    return storedState ? JSON.parse(storedState) : []
  }

  useEffect(() => {
    run && fetchJobFacets(run.id)
  }, [run])

  useEffect(
    () => () => {
      resetFacets()
    },
    []
  )
  const [expandedRows, setExpandedRows] = useState<number[]>(loadCollapsedState)

  const toggleRow = (index: number) => {
    setExpandedRows((prevExpandedRows) => {
      const newExpandedRows = prevExpandedRows.includes(index)
        ? prevExpandedRows.filter((rowIndex) => rowIndex !== index)
        : [...prevExpandedRows, index]

      localStorage.setItem(`dsi_${dsNamespace}_${dsName}`, JSON.stringify(newExpandedRows))

      return newExpandedRows
    })
  }

  useEffect(() => {
    for (const key in localStorage) {
      if (key !== `dsi_${dsNamespace}_${dsName}`) {
        localStorage.removeItem(key)
      }
    }
  }, [dsNamespace, dsName])

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
                <TableCell align='left'></TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {datasetFields.map((field, index) => {
                return (
                  <React.Fragment key={field.name}>
                    <TableRow
                      sx={{ cursor: 'pointer' }}
                      onClick={() => toggleRow(index)}
                      className='expandable-row'
                    >
                      <TableCell align='left'>{field.name}</TableCell>
                      <TableCell align='left'>{field.type}</TableCell>
                      <TableCell align='left'>{field.description || 'no description'}</TableCell>
                      <TableCell align='right'>
                        <KeyboardArrowDownIcon
                          sx={{
                            rotate: expandedRows.includes(index) ? '180deg' : 0,
                            transition: 'rotate .3s',
                          }}
                        />
                      </TableCell>
                    </TableRow>
                    <TableRow>
                      <TableCell colSpan={4} style={{ padding: 0, border: 'none' }}>
                        <Collapse in={expandedRows.includes(index)} timeout='auto'>
                          <Box p={2}>
                            <DatasetTags
                              namespace={dsNamespace}
                              datasetName={dsName}
                              datasetTags={field.tags}
                              datasetField={field.name}
                            />
                          </Box>
                        </Collapse>
                      </TableCell>
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
