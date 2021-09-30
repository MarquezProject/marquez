import { LineageDataset } from '../lineage/types'
import { Table, TableBody, TableCell, TableHead, TableRow } from '@material-ui/core'
import MqText from '../core/text/MqText'
import React, { FunctionComponent } from 'react'

const DATASET_COLUMNS = ['Attribute', 'Type', 'Description']

interface DatasetInfoProps {
  dataset: LineageDataset
}

const DatasetInfo: FunctionComponent<DatasetInfoProps> = props => {
  const { dataset } = props

  return (
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
        {dataset.fields.map(field => {
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
  )
}

export default DatasetInfo
