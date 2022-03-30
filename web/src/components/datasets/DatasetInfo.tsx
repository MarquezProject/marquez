// SPDX-License-Identifier: Apache-2.0

import { Box, Table, TableBody, TableCell, TableHead, TableRow } from '@material-ui/core'
import { Field, Run } from '../../types/api'
import { stopWatchDuration } from '../../helpers/time'
import MqCode from '../core/code/MqCode'
import MqEmpty from '../core/empty/MqEmpty'
import MqJson from '../core/code/MqJson'
import MqText from '../core/text/MqText'
import React, { FunctionComponent } from 'react'
import RunStatus from '../jobs/RunStatus'

const DATASET_COLUMNS = ['Field', 'Type', 'Description']

interface DatasetInfoProps {
  datasetFields: Field[]
  facets?: object
  run?: Run
}

const DatasetInfo: FunctionComponent<DatasetInfoProps> = props => {
  const { datasetFields, facets, run } = props

  if (datasetFields.length === 0) {
    return <MqEmpty title={'No Fields'} body={'Try adding dataset fields.'} />
  }

  return (
    <Box>
      <Table size='small'>
        <TableHead>
          <TableRow>
            {DATASET_COLUMNS.map(column => {
              return (
                <TableCell key={column} align='left'>
                  <MqText subheading inline>
                    {column}
                  </MqText>
                </TableCell>
              )
            })}
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
      {facets && (
        <Box mt={2}>
          <Box mb={1}>
            <MqText subheading>Facets</MqText>
          </Box>
          <MqJson code={facets} />
        </Box>
      )}
      {run && (
        <Box mt={2}>
          <Box mb={1}>
            <Box display={'flex'} alignItems={'center'} justifyContent={'space-between'}>
              <Box display={'flex'} alignItems={'center'}>
                <RunStatus run={run} />
                <MqText subheading>Created by Run</MqText>
              </Box>
              <Box display={'flex'}>
                <MqText bold>Duration:&nbsp;</MqText>
                <MqText subdued>{stopWatchDuration(run.durationMs)}</MqText>
              </Box>
            </Box>
            <MqText subdued>{run.jobVersion.name}</MqText>
          </Box>
          <MqCode code={run.context.sql} language={'sql'} />
        </Box>
      )}
    </Box>
  )
}

export default DatasetInfo
