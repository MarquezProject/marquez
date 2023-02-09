// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { ArrowBackIosRounded } from '@material-ui/icons'
import { Box, Chip, Table, TableBody, TableCell, TableHead, TableRow } from '@material-ui/core'
import { DatasetVersion } from '../../types/api'
import { Theme as ITheme } from '@material-ui/core/styles/createTheme'
import { WithStyles as IWithStyles } from '@material-ui/core/styles/withStyles'
import { alpha, createStyles, withStyles } from '@material-ui/core/styles'
import { formatUpdatedAt } from '../../helpers'
import DatasetInfo from './DatasetInfo'
import IconButton from '@material-ui/core/IconButton'
import MqText from '../core/text/MqText'
import React, { FunctionComponent, SetStateAction } from 'react'
import RunStatus from '../jobs/RunStatus'
import transitions from '@material-ui/core/styles/transitions'

const styles = (theme: ITheme) => {
  return createStyles({
    tableRow: {
      cursor: 'pointer',
      transition: transitions.create(['background-color']),
      '&:hover': {
        backgroundColor: alpha(theme.palette.common.white, 0.1)
      }
    }
  })
}

interface DatasetVersionsProps {
  versions: DatasetVersion[]
}

const DatasetVersions: FunctionComponent<DatasetVersionsProps &
  IWithStyles<typeof styles>> = props => {
  const { versions, classes } = props

  const [infoView, setInfoView] = React.useState<DatasetVersion | null>(null)
  const handleClick = (newValue: SetStateAction<DatasetVersion | null>) => {
    setInfoView(newValue)
  }
  const i18next = require('i18next')

  if (versions.length === 0) {
    return null
  }
  if (infoView) {
    return (
      <>
        <Box display={'flex'} alignItems={'center'} width={'100%'} justifyContent={'space-between'}>
          <Chip label={infoView.version} />
          <IconButton onClick={() => handleClick(null)}>
            <ArrowBackIosRounded fontSize={'small'} />
          </IconButton>
        </Box>
        <DatasetInfo
          datasetFields={infoView.fields}
          facets={infoView.facets}
          run={infoView.createdByRun}
        />
      </>
    )
  }
  return (
    <Table size='small'>
      <TableHead>
        <TableRow>
          <TableCell align='left'>
            <MqText subheading inline>
              {i18next.t('dataset_versions_columns.version')}
            </MqText>
          </TableCell>
          <TableCell align='left'>
            <MqText subheading inline>
              {i18next.t('dataset_versions_columns.created_at')}
            </MqText>
          </TableCell>
          <TableCell align='left'>
            <MqText subheading inline>
              {i18next.t('dataset_versions_columns.fields')}
            </MqText>
          </TableCell>
          <TableCell align='left'>
            <MqText subheading inline>
              {i18next.t('dataset_versions_columns.created_by_run')}
            </MqText>
          </TableCell>
          <TableCell align='left'>
            <MqText subheading inline>
              {i18next.t('dataset_versions_columns.lifecycle_state')}
            </MqText>
          </TableCell>
        </TableRow>
      </TableHead>
      <TableBody>
        {versions.map(version => {
          return (
            <TableRow
              className={classes.tableRow}
              key={version.createdAt}
              onClick={() => handleClick(version)}
            >
              <TableCell align='left'>{version.version}</TableCell>
              <TableCell align='left'>{formatUpdatedAt(version.createdAt)}</TableCell>
              <TableCell align='left'>{version.fields.length}</TableCell>
              <TableCell align='left'>
                <Box display={'flex'} alignItems={'center'}>
                  {version.createdByRun ? (
                    <>
                      <RunStatus run={version.createdByRun} />
                      {version.createdByRun ? version.createdByRun.id : 'N/A'}
                    </>
                  ) : (
                    'N/A'
                  )}
                </Box>
              </TableCell>
              <TableCell align='left'>{version.lifecycleState}</TableCell>
            </TableRow>
          )
        })}
      </TableBody>
    </Table>
  )
}

export default withStyles(styles)(DatasetVersions)
