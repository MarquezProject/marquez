// Copyright 2018-2024 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0
import { Box, Chip, Table, TableBody, TableCell, TableHead, TableRow } from '@mui/material'
import { Dataset, Field } from '../../types/api'
import { Link } from 'react-router-dom'
import { encodeQueryString } from '../../routes/column-level/ColumnLineageColumnNode'
import DatasetTags from './DatasetTags'
import IconButton from '@mui/material/IconButton'
import MQTooltip from '../core/tooltip/MQTooltip'
import MqEmpty from '../core/empty/MqEmpty'
import MqJsonView from '../core/json-view/MqJsonView'
import MqText from '../core/text/MqText'
import React, { FunctionComponent } from 'react'
import SplitscreenIcon from '@mui/icons-material/Splitscreen'

export interface JobFacetsProps {
  isCurrentVersion?: boolean
  dataset: Dataset
}

type DatasetInfoProps = {
  datasetFields: Field[]
  facets?: object
  showTags?: boolean
} & JobFacetsProps

const DatasetInfo: FunctionComponent<DatasetInfoProps> = (props) => {
  const { datasetFields, facets, dataset, showTags } = props
  const i18next = require('i18next')

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
                {!showTags && <TableCell align='left' />}
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
                const hasColumnLineage = dataset?.columnLineage?.find((f) => f.name === field.name)
                return (
                  <React.Fragment key={field.name}>
                    <TableRow>
                      <TableCell align='left'>
                        <MqText font={'mono'}>{field.name}</MqText>
                      </TableCell>
                      {!showTags && (
                        <TableCell align='left'>
                          <Chip
                            size={'small'}
                            label={
                              <MqText font={'mono'} small>
                                {field.type || 'N/A'}
                              </MqText>
                            }
                            variant={'outlined'}
                          />
                        </TableCell>
                      )}
                      {!showTags && (
                        <TableCell align='left'>
                          <MqText subdued>{field.description || 'no description'}</MqText>
                        </TableCell>
                      )}
                      {!showTags && (
                        <TableCell align='left'>
                          {dataset && (
                            <MQTooltip
                              title={
                                !dataset.columnLineage
                                  ? 'No Column Lineage, check facet'
                                  : i18next.t('dataset_info_columns.column_lineage')
                              }
                            >
                              <IconButton
                                disabled={!hasColumnLineage}
                                size={'small'}
                                component={Link}
                                to={`/datasets/column-level/${encodeURIComponent(
                                  dataset.namespace
                                )}/${encodeURIComponent(dataset.name)}?column=${encodeURIComponent(
                                  encodeQueryString(dataset.namespace, dataset.name, field.name)
                                )}&columnName=${encodeURIComponent(field.name)}`}
                              >
                                <SplitscreenIcon />
                              </IconButton>
                            </MQTooltip>
                          )}
                        </TableCell>
                      )}
                      {showTags && (
                        <TableCell align='left'>
                          <DatasetTags
                            namespace={dataset.namespace}
                            datasetName={dataset.name}
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

export default DatasetInfo
