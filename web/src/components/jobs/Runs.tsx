// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { ArrowBackIosRounded } from '@material-ui/icons'
import {
  Box,
  Chip,
  IconButton,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Theme,
  WithStyles,
  createStyles,
  withStyles
} from '@material-ui/core'
import { Run } from '../../types/api'
import { alpha } from '@material-ui/core/styles'
import { formatUpdatedAt } from '../../helpers'
import { stopWatchDuration } from '../../helpers/time'
import MqCode from '../core/code/MqCode'
import MqEmpty from '../core/empty/MqEmpty'
import MqText from '../core/text/MqText'
import React, { FunctionComponent, SetStateAction } from 'react'
import RunInfo from './RunInfo'
import RunStatus from './RunStatus'
import transitions from '@material-ui/core/styles/transitions'

const styles = (theme: Theme) => {
  return createStyles({
    status: {
      gridArea: 'status',
      width: theme.spacing(2),
      height: theme.spacing(2),
      borderRadius: '50%'
    },
    tableRow: {
      cursor: 'pointer',
      transition: transitions.create(['background-color']),
      '&:hover': {
        backgroundColor: alpha(theme.palette.common.white, 0.1)
      }
    }
  })
}

interface RunsProps {
  runs: Run[]
  facets?: object
}

const Runs: FunctionComponent<RunsProps & WithStyles<typeof styles>> = props => {
  const { runs, facets, classes } = props
  const i18next = require('i18next')
  if (runs.length === 0) {
    return <MqEmpty title={i18next.t('jobs.empty_title')} body={i18next.t('jobs.empty_body')} />
  }

  const [infoView, setInfoView] = React.useState<Run | null>(null)
  const handleClick = (newValue: SetStateAction<Run | null>) => {
    setInfoView(newValue)
  }

  if (infoView) {
    return (
      <>
        <Box display={'flex'} alignItems={'center'} width={'100%'} justifyContent={'space-between'}>
          <Chip label={infoView.id} />
          <IconButton onClick={() => handleClick(null)}>
            <ArrowBackIosRounded fontSize={'small'} />
          </IconButton>
        </Box>
        <RunInfo run={infoView} />
      </>
    )
  }

  return (
    <Box>
      <Table size='small'>
        <TableHead>
          <TableRow>
            <TableCell align='left'>
              <MqText subheading inline>
                {i18next.t('runs_columns.id')}
              </MqText>
            </TableCell>
            <TableCell align='left'>
              <MqText subheading inline>
                {i18next.t('runs_columns.state')}
              </MqText>
            </TableCell>
            <TableCell align='left'>
              <MqText subheading inline>
                {i18next.t('runs_columns.created_at')}
              </MqText>
            </TableCell>
            <TableCell align='left'>
              <MqText subheading inline>
                {i18next.t('runs_columns.started_at')}
              </MqText>
            </TableCell>
            <TableCell align='left'>
              <MqText subheading inline>
                {i18next.t('runs_columns.ended_at')}
              </MqText>
            </TableCell>
            <TableCell align='left'>
              <MqText subheading inline>
                {i18next.t('runs_columns.duration')}
              </MqText>
            </TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {runs.map(run => {
            return (
              <TableRow key={run.id} className={classes.tableRow} onClick={() => handleClick(run)}>
                <TableCell align='left'>{run.id}</TableCell>
                <TableCell align='left'>
                  <Box display={'flex'} alignItems={'center'}>
                    <RunStatus run={run} />
                    <MqText>{run.state}</MqText>
                  </Box>
                </TableCell>
                <TableCell align='left'>{formatUpdatedAt(run.createdAt)}</TableCell>
                <TableCell align='left'>{formatUpdatedAt(run.startedAt)}</TableCell>
                <TableCell align='left'>{formatUpdatedAt(run.endedAt)}</TableCell>
                <TableCell align='left'>{stopWatchDuration(run.durationMs)}</TableCell>
              </TableRow>
            )
          })}
        </TableBody>
      </Table>
      {facets && (
        <Box mt={2}>
          <Box mb={1}>
            <MqText subheading>{i18next.t('jobs.runs_subhead')}</MqText>
          </Box>
          <MqCode code={JSON.stringify(facets, null, '\t')} />
        </Box>
      )}
    </Box>
  )
}

export default withStyles(styles)(Runs)
