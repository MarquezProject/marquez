import React, { FunctionComponent } from 'react'
import {
  withStyles,
  createStyles,
  WithStyles as IWithStyles,
  Theme as ITheme
} from '@material-ui/core/styles'
import { Typography, Box, Tooltip, Table, TableCell, TableHead, TableRow, TableBody, Paper } from '@material-ui/core'

import InfoIcon from '@material-ui/icons/Info'

import { formatUpdatedAt } from '../helpers'

import { useParams } from 'react-router-dom'
import _find from 'lodash/find'

import { Dataset } from '../types/api'

const styles = ({ shadows }: ITheme) => {
  return createStyles({
    root: {
      marginTop: '52vh',
      height: '48vh',
      padding: '0 6% 1%',
    },
    noData: {
      padding: '125px 0 0 0'
    },
    noSchema: {
      boxShadow: shadows[1],
      padding: '1rem'
    },
    noSchemaTitle: {
      fontSize: '14px'
    },
    infoIcon: {
      paddingLeft: '3px',
      paddingTop: '3px'
    },
    tableCell: {
      display: 'flex',
      paddingTop: '12px',
      flexFlow: 'row nowrap',
      flexGrow: 1,
      flexBasis: 0
    },
    tableRow: {
      display: 'flex',
      width: '100%'
    },
    paper: {
      overflowX: 'auto',
      marginTop: '10px',
      display: 'flex',
      flexFlow: 'column nowrap'
    },
    updated: {
      marginTop: '10px'
    },
  })
}

type IProps = IWithStyles<typeof styles> & { datasets: Dataset[] }

const DatasetDetailPage: FunctionComponent<IProps> = props => {
  const { datasets, classes } = props
  const {
    root, paper, updated, noData, noSchema, noSchemaTitle, infoIcon, tableCell, tableRow
  } = classes
  const { datasetName } = useParams()
  const dataset = _find(datasets, d => d.name === datasetName)
  if (!dataset) {
    return (
      <Box
        mt={10}
        display='flex'
        justifyContent="center"
        className={root}
      >
        <Typography align='center' className={noData}>
          No dataset by the name of <strong>&quot;{datasetName}&quot;</strong> found
        </Typography>
      </Box>
    )
  } else {
    const {
      name,
      description,
      updatedAt,
      fields
    } = dataset

    return (
      <Box mt={10} className={root}>
        <Box display='flex' justifyContent='space-between'>
          <div>
            <Typography color='secondary' align='left'>
              <strong>{name}</strong>
            </Typography>
            <Typography color='primary' align='left'>
              {description}
            </Typography>
          </div>
        </Box>
        {fields && fields.length > 0 ? (
          <Paper className={paper}>
            <Table size="small">
              <TableHead>
                <TableRow className={tableRow}>
                  {
                    fields.map((field) => {
                      return (
                      <TableCell className={tableCell} key={field.name} align="center"><strong>{field.name}</strong>
                        <Tooltip title={field.type} placement="top">
                          <div className={infoIcon}>
                            <InfoIcon color='disabled' fontSize='small' />
                          </div>
                        </Tooltip>
                      </TableCell>
                      )
                    })
                  }
                </TableRow>
              </TableHead>
              <TableBody>
                <TableRow className={tableRow}>
                  {fields.map((field) => {
                    return <TableCell className={tableCell} key={field.name} align="left">{field.description || 'no description'}</TableCell>
                  })}
                </TableRow>
              </TableBody>
            </Table>
          </Paper>
        ) : (
          <div className={noSchema}>
            <Typography className={noSchemaTitle}>
              schema not present
            </Typography>
          </div>
        )}
        <Typography className={updated} color='primary' align='right'>
          last updated: {formatUpdatedAt(updatedAt)}
        </Typography>
      </Box>
    )
  }
}

export default withStyles(styles)(DatasetDetailPage)
