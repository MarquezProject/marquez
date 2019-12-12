import React, { FunctionComponent } from 'react'
import {
  withStyles,
  createStyles,
  WithStyles as IWithStyles
} from '@material-ui/core/styles'
import { Typography, Box, Tooltip } from '@material-ui/core'

import Table from '@material-ui/core/Table'
// import TableBody from '@material-ui/core/TableBody'
import TableCell from '@material-ui/core/TableCell'
import TableHead from '@material-ui/core/TableHead'
import TableRow from '@material-ui/core/TableRow'
import Paper from '@material-ui/core/Paper'

import tagToBadge from '../config/tag-to-badge'
import InfoIcon from '@material-ui/icons/Info'

import { formatUpdatedAt } from '../helpers' 

import { useParams } from 'react-router-dom'
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
      float: 'right',
      paddingRight: '60px',
      paddingTop: '3px'
    },
    tableCell: {
      paddingTop: '12px'
    },
    paper: {
      overflowX: 'auto',
      marginTop: '10px'
    },
    updated: {
      marginTop: '10px'
    }
  })
}

type IProps = IWithStyles<typeof styles> & { datasets: IDataset[] }

const DatasetDetailPage: FunctionComponent<IProps> = props => {
  const { datasets, classes } = props
  const {
    root, paper, updated, tagContainer, noData, infoIcon, tableCell
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
      tags = [],
      updatedAt
    } = dataset

    let {
      fields
    } = dataset
    
    // placeholder logic to seed 'fields' attribute if API returns nothing
    !fields ? fields = [
      {
      'name': 'uuid',
      'type': 'STRING'
      },
      {
      'name': 'name',
      'type': 'STRING'
      },
      {
      'name': 'mass',
      'type': 'INTEGER'
      },
      {
      'name': 'dimension',
      'type': 'STRING'
      },
      {
      'name': 'interstellar',
      'type': 'BOOLEAN'
      }
    ] : null

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
        </Box>
        <Paper className={paper}>
          <Table size="small">
            <TableHead>
              <TableRow>
                {
                  fields.map((field) => {
                    return (
                    <TableCell className={tableCell} key={field.name} align="left"><strong>{field.name}</strong>
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
            {/* <TableBody>
              {rows.map(row => (
                <TableRow key={row.id}>
                  {
                    fields.map(field => {
                      return <TableCell key={field} align="left">{row[field].toString()}</TableCell>
                    })
                  }
                </TableRow>
              ))}
            </TableBody> */}
          </Table>
        </Paper>
        <Typography className={updated} color='primary' align='right'>
          {formatUpdatedAt(updatedAt)}
        </Typography>
      </Box>
    )
  }
}

export default withStyles(styles)(DatasetDetailPage)

