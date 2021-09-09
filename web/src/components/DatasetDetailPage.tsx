import React, { FunctionComponent } from 'react'

import * as Redux from 'redux'
import { Box, Chip, Table, TableBody, TableCell, TableHead, TableRow } from '@material-ui/core'
import { IState } from '../store/reducers'
import {
  Theme as ITheme,
  WithStyles as IWithStyles,
  createStyles,
  withStyles
} from '@material-ui/core/styles'
import { LineageDataset } from './lineage/types'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { formatUpdatedAt } from '../helpers'
import { useHistory, useParams } from 'react-router-dom'
import CloseIcon from '@material-ui/icons/Close'
import IconButton from '@material-ui/core/IconButton'
import MqText from './core/text/MqText'

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
      '&:not(:last-of-type)': {
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
    updated: {
      marginTop: '10px'
    }
  })
}

const DATASET_COLUMNS = ['Attribute', 'Type', 'Description']

type IProps = IWithStyles<typeof styles> & { dataset: LineageDataset }

const DatasetDetailPage: FunctionComponent<IProps> = props => {
  const { dataset, classes } = props
  const { root } = classes
  const { datasetName } = useParams()
  const history = useHistory()
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
          {tags.length > 0 && (
            <ul className={classes.tagList}>
              {tags.map(tag => (
                <li key={tag.name} className={classes.tag}>
                  <Chip size='small' label={tag} />
                </li>
              ))}
            </ul>
          )}
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
              {fields.map(field => {
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
  datasets: state.datasets.result
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) => bindActionCreators({}, dispatch)

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(withStyles(styles)(DatasetDetailPage))
