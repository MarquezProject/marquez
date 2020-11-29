import React, { FunctionComponent } from 'react'

import * as Redux from 'redux'
import {
  Box,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Tooltip,
  Chip
} from '@material-ui/core'
import { Dataset } from '../types/api'
import { IState } from '../reducers'
import {
  Theme as ITheme,
  WithStyles as IWithStyles,
  createStyles,
  withStyles
} from '@material-ui/core/styles'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { formatUpdatedAt } from '../helpers'
import { useHistory, useParams } from 'react-router-dom'
import CloseIcon from '@material-ui/icons/Close'
import IconButton from '@material-ui/core/IconButton'
import InfoIcon from '@material-ui/icons/Info'
import MqText from './core/text/MqText'
import _find from 'lodash/find'

const styles = ({ spacing }: ITheme) => {
  return createStyles({
    root: {
      padding: `0 ${spacing(2)}px`
    },
    tagList: {
      display: 'flex',
      flexWrap: 'wrap',
      listStyle: 'none',
      margin: 0,
      padding: 0
    },
    tag: {
      "&:not(:last-of-type)": {
        marginRight: spacing(1)
      }
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
    }
  })
}

type IProps = IWithStyles<typeof styles> & { datasets: Dataset[] }

const DatasetDetailPage: FunctionComponent<IProps> = props => {
  const { datasets, classes } = props
  const { root, paper, infoIcon, tableCell, tableRow } = classes
  const { datasetName } = useParams()
  const history = useHistory()
  const dataset = _find(datasets, d => d.name === datasetName)
  if (!dataset) {
    return (
      <Box display='flex' justifyContent='center' className={root} mt={2}>
        <MqText subdued>
          No dataset by the name of <MqText bold inline>{`"${datasetName}"`}</MqText> found
        </MqText>
      </Box>
    )
  } else {
    const { name, description, updatedAt, fields, tags } = dataset
    return (
      <Box mt={2} className={root}>
        <Box>
          {tags.length > 0 &&
          <ul className={classes.tagList}>
            {tags.map(tag =>
              <li key={tag} className={classes.tag}>
                <Chip
                  size="small"
                  label={'thing'}
                />
              </li>
            )}
          </ul>
          }
          <Box display={'flex'} alignItems={'center'} justifyContent={'space-between'}>
            <MqText heading font={'mono'}>
              {name}
            </MqText>
            <IconButton onClick={() => history.push('/')}>
              <CloseIcon />
            </IconButton>
          </Box>
          <MqText subdued>{description}</MqText>
        </Box>
        {fields && fields.length > 0 ? (
          <Paper className={paper}>
            <Table size='small'>
              <TableHead>
                <TableRow className={tableRow}>
                  {fields.map(field => {
                    return (
                      <TableCell className={tableCell} key={field.name} align='center'>
                        <strong>{field.name}</strong>
                        <Tooltip title={field.type} placement='top'>
                          <div className={infoIcon}>
                            <InfoIcon color='disabled' fontSize='small' />
                          </div>
                        </Tooltip>
                      </TableCell>
                    )
                  })}
                </TableRow>
              </TableHead>
              <TableBody>
                <TableRow className={tableRow}>
                  {fields.map(field => {
                    return (
                      <TableCell className={tableCell} key={field.name} align='left'>
                        {field.description || 'no description'}
                      </TableCell>
                    )
                  })}
                </TableRow>
              </TableBody>
            </Table>
          </Paper>
        ) : (
          <div>
            <MqText subdued>schema not present</MqText>
          </div>
        )}
        <Box display={'flex'} justifyContent={'flex-end'} mt={1}>
          <MqText subdued>last updated: {formatUpdatedAt(updatedAt)}</MqText>
        </Box>
      </Box>
    )
  }
}

const mapStateToProps = (state: IState) => ({
  datasets: state.datasets
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) => bindActionCreators({}, dispatch)

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(withStyles(styles)(DatasetDetailPage))
