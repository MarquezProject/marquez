import {
  Box,
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
import { formatUpdatedAt } from '../../helpers'
import { runColorMap } from '../../helpers/runs'
import { stopWatchDuration } from '../../helpers/time'
import MqCode from '../core/code/MqCode'
import MqText from '../core/text/MqText'
import React, { FunctionComponent } from 'react'

const RUN_COLUMNS = ['Status', 'Created At', 'Start Time', 'End Time', 'Duration']

const styles = (theme: Theme) => {
  return createStyles({
    status: {
      gridArea: 'status',
      width: theme.spacing(2),
      height: theme.spacing(2),
      borderRadius: '50%'
    }
  })
}

interface RunsProps {
  runs: Run[]
  facets?: object
}

const Runs: FunctionComponent<RunsProps & WithStyles<typeof styles>> = props => {
  const { runs, facets, classes } = props
  if (runs.length === 0) {
    return null
  }

  return (
    <Box>
      <Table size='small'>
        <TableHead>
          <TableRow>
            {RUN_COLUMNS.map(column => {
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
          {runs.map(run => {
            return (
              <TableRow key={run.id}>
                <TableCell align='left'>
                  <Box display={'flex'} alignItems={'center'}>
                    <Box
                      mr={1}
                      className={classes.status}
                      style={{ backgroundColor: runColorMap[run.state] }}
                    />
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
            <MqText subheading>Facets</MqText>
          </Box>
          <MqCode code={JSON.stringify(facets, null, '\t')} />
        </Box>
      )}
    </Box>
  )
}

export default withStyles(styles)(Runs)
