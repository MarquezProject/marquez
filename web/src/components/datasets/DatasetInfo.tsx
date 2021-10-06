import { Box, Table, TableBody, TableCell, TableHead, TableRow } from '@material-ui/core'
import { Field } from '../../types/api'
import MqCode from '../core/code/MqCode'
import MqText from '../core/text/MqText'
import React, { FunctionComponent } from 'react'

const DATASET_COLUMNS = ['Field', 'Type', 'Description']

interface DatasetInfoProps {
  datasetFields: Field[]
  facets?: object
}

const DatasetInfo: FunctionComponent<DatasetInfoProps> = props => {
  const { datasetFields, facets } = props

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
          <MqCode code={JSON.stringify(facets, null, '\t')} />
        </Box>
      )}
    </Box>
  )
}

export default DatasetInfo
