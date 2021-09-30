import React, {FunctionComponent} from 'react'
import {DatasetVersion} from '../../types/api'
import {Table, TableBody, TableCell, TableHead, TableRow} from '@material-ui/core'
import MqText from '../core/text/MqText'
import {formatUpdatedAt} from '../../helpers'
import {stopWatchDuration} from '../../helpers/time'

const DATASET_VERSIONS_COLUMNS = ['Last Updated', 'Creator Duration', 'Field Count']

interface DatasetVersionsProps {
  versions: DatasetVersion[]
}

const DatasetVersions: FunctionComponent<DatasetVersionsProps> = props => {
  const {versions} = props
  if (versions.length === 0) {
    return null
  }

  return           <Table size='small'>
    <TableHead>
      <TableRow>
        {DATASET_VERSIONS_COLUMNS.map(column => {
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
      {versions.map(version => {
        return (
          <TableRow key={version.createdAt}>
            <TableCell align='left'>{formatUpdatedAt(version.createdAt)}</TableCell>
            <TableCell align='left'>
              {version.createdByRun
                ? stopWatchDuration(version.createdByRun.durationMs)
                : 'N/A'}
            </TableCell>
            <TableCell align='left'>{version.fields.length}</TableCell>
          </TableRow>
        )
      })}
    </TableBody>
  </Table>
}

export default DatasetVersions
