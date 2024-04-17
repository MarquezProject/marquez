// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { ArrowBackIosRounded } from '@mui/icons-material'
import { Box, Chip, Table, TableBody, TableCell, TableHead, TableRow } from '@mui/material'
import { DatasetVersion } from '../../types/api'
import { alpha, createTheme } from '@mui/material/styles'
import { formatUpdatedAt } from '../../helpers'
import { useTheme } from '@emotion/react'
import DatasetInfo from './DatasetInfo'
import IconButton from '@mui/material/IconButton'
import MqCopy from '../core/copy/MqCopy'
import MqText from '../core/text/MqText'
import React, { FunctionComponent, SetStateAction } from 'react'

interface DatasetVersionsProps {
  versions: DatasetVersion[]
}

const DatasetVersions: FunctionComponent<DatasetVersionsProps> = (props) => {
  const { versions } = props

  const [infoView, setInfoView] = React.useState<DatasetVersion | null>(null)
  const handleClick = (newValue: SetStateAction<DatasetVersion | null>) => {
    setInfoView(newValue)
  }
  const i18next = require('i18next')
  const theme = createTheme(useTheme())

  if (versions.length === 0) {
    return null
  }
  if (infoView) {
    return (
      <>
        <Box display={'flex'} alignItems={'center'} width={'100%'} justifyContent={'space-between'}>
          <Chip size={'small'} variant={'outlined'} label={infoView.version} />
          <IconButton onClick={() => handleClick(null)} size='small'>
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
        {versions.map((version) => {
          return (
            <TableRow
              sx={{
                cursor: 'pointer',
                transition: theme.transitions.create(['background-color']),
                '&:hover': {
                  backgroundColor: alpha(theme.palette.common.white, 0.1),
                },
              }}
              key={version.createdAt}
              onClick={() => handleClick(version)}
            >
              <TableCell align='left'>
                <Box display={'flex'} alignItems={'center'}>
                  {version.version.substring(0, 8)}...
                  <MqCopy string={version.version} />
                </Box>
              </TableCell>
              <TableCell align='left'>{formatUpdatedAt(version.createdAt)}</TableCell>
              <TableCell align='left'>{version.fields.length}</TableCell>
              <TableCell align='left'>
                <Box display={'flex'} alignItems={'center'}>
                  {version.createdByRun ? (
                    <>
                      {version.createdByRun.id.substring(0, 8)}...
                      <MqCopy string={version.createdByRun.id} />
                    </>
                  ) : (
                    'N/A'
                  )}
                </Box>
              </TableCell>
              <TableCell align='left'>{version.lifecycleState || 'N/A'}</TableCell>
            </TableRow>
          )
        })}
      </TableBody>
    </Table>
  )
}

export default DatasetVersions
