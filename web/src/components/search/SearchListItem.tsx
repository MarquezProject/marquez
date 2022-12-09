// SPDX-License-Identifier: Apache-2.0

import { Box, Theme, createStyles, darken } from '@material-ui/core'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { JobOrDataset } from '../lineage/types'
import { Link as RouterLink } from 'react-router-dom'
import { SearchResult } from '../../types/api'
import { encodeNode } from '../../helpers/nodes'
import { faCog } from '@fortawesome/free-solid-svg-icons/faCog'
import { faDatabase } from '@fortawesome/free-solid-svg-icons'
import { theme } from '../../helpers/theme'
import MqText from '../core/text/MqText'
import React from 'react'
import classNames from 'classnames'
import moment from 'moment'
import withStyles, { WithStyles } from '@material-ui/core/styles/withStyles'

const styles = (theme: Theme) =>
  createStyles({
    listItem: {
      display: 'block',
      color: 'inherit',
      textDecoration: 'none',
      margin: 0,
      cursor: 'pointer',
      padding: `${theme.spacing(1)}px ${theme.spacing(3)}px`,
      '&:not(:last-child)': {
        borderBottom: `1px solid ${theme.palette.secondary.main}`
      },
      '&:last-child': {
        borderBottomLeftRadius: '2px',
        borderBottomRightRadius: '2px'
      },
      '&:hover, &.selected': {
        backgroundColor: darken(theme.palette.background.paper, 0.02)
      },
      '&:nth-child(even)': {
        backgroundColor: darken(theme.palette.background.paper, 0.2),
        '&:hover, &.selected': {
          backgroundColor: darken(theme.palette.background.paper, 0.02)
        }
      }
    },
    textOverflow: {
      display: 'inline',
      whiteSpace: 'nowrap',
      overflow: 'hidden',
      textOverflow: 'ellipsis',
      maxWidth: '370px'
    }
  })

interface OwnProps {
  key: string | number
  searchResult: SearchResult
  search: string
  onClick: (nodeName: string) => void
  selected: boolean
}

const searchResultIcon: { [key in JobOrDataset]: JSX.Element } = {
  JOB: <FontAwesomeIcon icon={faCog} color={theme.palette.primary.main} />,
  DATASET: <FontAwesomeIcon icon={faDatabase} color={theme.palette.primary.main} />
}

type DkSearchListItemProps = OwnProps & WithStyles<typeof styles>

class SearchListItem extends React.Component<DkSearchListItemProps> {
  render() {
    const { classes, searchResult, search, selected } = this.props
    const name = searchResult.name.substring(
      searchResult.name.lastIndexOf('.') + 1,
      searchResult.name.length
    )
    const searchMatchIndex = name.toLowerCase().indexOf(search.toLowerCase())
    return (
      <RouterLink
        key={this.props.key}
        className={classNames(classes.listItem, selected && 'selected')}
        onClick={() => this.props.onClick(searchResult.name)}
        to={`/lineage/${encodeNode(searchResult.type, searchResult.namespace, searchResult.name)}`}
      >
        <Box display={'flex'} alignItems={'center'} justifyContent={'space-between'}>
          <Box display={'flex'} alignItems={'center'}>
            <Box display={'inline'} mr={1}>
              {searchResultIcon[searchResult.type]}
            </Box>
            <Box className={classes.textOverflow}>
              {searchMatchIndex === -1 ? (
                <MqText inline font={'mono'} bold small>
                  {name}
                </MqText>
              ) : (
                <>
                  <MqText inline font={'mono'} bold small>
                    {name.substring(0, searchMatchIndex)}
                  </MqText>
                  <MqText inline font={'mono'} bold highlight small>
                    {name.substring(searchMatchIndex, searchMatchIndex + this.props.search.length)}
                  </MqText>
                  <MqText inline font={'mono'} bold small>
                    {name.substring(
                      searchMatchIndex + this.props.search.length,
                      searchResult.name.length
                    )}
                  </MqText>
                </>
              )}
            </Box>
          </Box>
          <Box>
            <MqText subdued small>
              {moment(searchResult.updatedAt).fromNow()}
            </MqText>
          </Box>
        </Box>
      </RouterLink>
    )
  }
}

export default withStyles(styles)(SearchListItem)
