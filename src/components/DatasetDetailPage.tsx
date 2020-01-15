import React, { FunctionComponent } from 'react'
import {
  withStyles,
  createStyles,
  WithStyles as IWithStyles
} from '@material-ui/core/styles'
import { Typography, Box, Tooltip, Fab, Table, TableCell, TableHead, TableRow, TableBody, Paper } from '@material-ui/core'

import CloseIcon from '@material-ui/icons/Close'

import tagToBadge from '../config/tag-to-badge'
import InfoIcon from '@material-ui/icons/Info'

import { formatUpdatedAt } from '../helpers'

import { useParams, useHistory } from 'react-router-dom'
import _find from 'lodash/find'
import _keys from 'lodash/keys'

import { IDataset } from '../types'

const styles = () => {
  return createStyles({
    root: {
      marginTop: '52vh',
      height: '48vh',
      padding: '0 6% 1%',
    },
    tagContainer: {
      display: 'flex',
      margin: '12px 12px 0px 0px'
    },
    noData: {
      padding: '125px 0 0 0'
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
    closeButton: {
      color: '#7D7D7D',
      backgroundColor: '#ffffff'
    },
    tagHolder: {
      display: 'flex',
      padding: '9px 12px'
    }
  })
}

type IProps = IWithStyles<typeof styles> & { datasets: IDataset[] }

const DatasetDetailPage: FunctionComponent<IProps> = props => {
  const { datasets, classes } = props
  const {
    root, paper, updated, tagContainer, noData, infoIcon, tableCell, tableRow, closeButton, tagHolder
  } = classes
  const { datasetName } = useParams()
  const history = useHistory()
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
      tags = [],
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
          <div id='tagContainer' className={tagContainer}>
            <div className={tagHolder}>
              {_keys(tagToBadge.default).map((key: string) => {
                return (
                  <div key={key}>
                    <Tooltip className="tagWrapper" title={key} placement="top">
                      {tags.includes(key) ? tagToBadge.highlighted[key] : tagToBadge.default[key]}
                    </Tooltip>
                  </div>
                )
              })}
            </div>
            <Fab className={closeButton} onClick={() => history.push('/')} size="small" aria-label="edit">
              <CloseIcon />
            </Fab>
          </div>
        </Box>
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
        <Typography className={updated} color='primary' align='right'>
          last updated: {formatUpdatedAt(updatedAt)}
        </Typography>
      </Box>
    )
  }
}

export default withStyles(styles)(DatasetDetailPage)
