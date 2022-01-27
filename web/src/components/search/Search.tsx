// SPDX-License-Identifier: Apache-2.0

import * as Redux from 'redux'
import { Box, Theme, createStyles, darken } from '@material-ui/core'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { GroupedSearch } from '../../types/api'
import { IState } from '../../store/reducers'
import { MqInputBase } from '../core/input-base/MqInputBase'
import { RouteComponentProps, withRouter } from 'react-router-dom'
import { THEME_EXTRA, theme } from '../../helpers/theme'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { faCog, faDatabase, faSearch, faSort, faTimes } from '@fortawesome/free-solid-svg-icons'
import { fetchSearch, setSelectedNode } from '../../store/actionCreators'
import { parseSearchGroup } from '../../helpers/nodes'
import ClickAwayListener from '@material-ui/core/ClickAwayListener'
import MqChipGroup from '../core/chip/MqChipGroup'
import MqText from '../core/text/MqText'
import React from 'react'
import SearchListItem from './SearchListItem'
import SearchPlaceholder from './SearchPlaceholder'
import debounce from '@material-ui/core/utils/debounce'
import withStyles, { WithStyles } from '@material-ui/core/styles/withStyles'

const INITIAL_SEARCH_FILTER = [
  {
    text: 'All',
    value: 'All'
  },
  {
    icon: faCog,
    foregroundColor: theme.palette.common.white,
    backgroundColor: theme.palette.primary.main,
    text: 'Jobs',
    value: 'JOB'
  },
  {
    icon: faDatabase,
    foregroundColor: theme.palette.common.white,
    backgroundColor: theme.palette.primary.main,
    text: 'Datasets',
    value: 'DATASET'
  }
]

const INITIAL_SEARCH_SORT_FILTER = [
  {
    icon: faSort,
    value: 'Sort',
    foregroundColor: theme.palette.common.white,
    backgroundColor: 'transparent',
    selectable: false
  },
  {
    text: 'Updated',
    value: 'UPDATE_AT'
  },
  {
    text: 'Name',
    value: 'NAME'
  }
]

const styles = (theme: Theme) =>
  createStyles({
    searchContainer: {
      position: 'relative'
    },
    search: {
      zIndex: theme.zIndex.appBar + 2
    },
    searchIcon: {
      zIndex: theme.zIndex.appBar + 3,
      position: 'absolute',
      left: theme.spacing(12),
      top: 11
    },
    groupName: {
      borderTop: `2px solid ${theme.palette.common.white}`,
      borderBottom: `2px solid ${theme.palette.common.white}`,
      padding: `${theme.spacing(1)}px ${theme.spacing(3)}px ${theme.spacing(0.5)}px ${theme.spacing(
        1
      )}px`,
      backgroundColor: darken(theme.palette.background.paper, 0.05)
    },
    closeIcon: {
      position: 'absolute',
      zIndex: theme.zIndex.appBar + 3,
      right: theme.spacing(12),
      top: 11,
      cursor: 'pointer'
    },
    dropdown: {
      position: 'absolute',
      top: theme.spacing(-2),
      width: '100%',
      right: 0,
      left: 0,
      zIndex: theme.zIndex.appBar + 1,
      border: `2px dashed ${theme.palette.secondary.main}`,
      borderRadius: theme.spacing(1),
      backgroundColor: theme.palette.background.default
    },
    listHeader: {
      marginTop: '52px',
      padding: `${theme.spacing(2)}px`,
      display: 'flex',
      justifyContent: 'space-between'
    },
    spaceRight: {
      marginRight: theme.spacing(1)
    },
    listContainer: {
      margin: 0,
      overflow: 'auto',
      maxHeight: `calc(100vh - ${theme.spacing(30)}px)`,
      paddingLeft: 0,
      borderBottomLeftRadius: theme.spacing(1),
      borderBottomRightRadius: theme.spacing(1)
    },
    listFooter: {
      paddingTop: theme.spacing(2),
      paddingBottom: theme.spacing(2)
    }
  })

interface StateProps {
  searchResults: Map<string, GroupedSearch[]>
  isSearching: boolean
  isSearchingInit: boolean
}

interface DispatchProps {
  setSelectedNode: typeof setSelectedNode
  fetchSearch: typeof fetchSearch
}

interface SearchState {
  open: boolean
  search: string
  selected: string
  filter: string
  sort: string
}

type UnregisterCallback = () => void

type SearchProps = StateProps & DispatchProps & WithStyles<typeof styles> & RouteComponentProps

class Search extends React.Component<SearchProps, SearchState> {
  private unlisten!: UnregisterCallback

  constructor(props: SearchProps) {
    super(props)
    this.state = {
      open: true,
      search: '',
      selected: '',
      filter: 'All',
      sort: 'UPDATE_AT'
    }
    this.fetchSearch = debounce(this.fetchSearch, 300)
  }

  componentDidMount() {
    // close search on a route change
    this.unlisten = this.props.history.listen(() => {
      this.setState({
        open: false
      })
    })
  }

  componentWillUnmount() {
    this.unlisten()
  }

  onSearch = (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    this.setState({ search: event.target.value, open: true }, () => {
      this.fetchSearch(
        this.state.search,
        this.state.filter.toUpperCase(),
        this.state.sort.toUpperCase()
      )
    })
  }

  onSelectFilter = (label: string) => {
    this.setState(
      {
        filter: label
      },
      () => {
        this.fetchSearch(
          this.state.search,
          this.state.filter.toUpperCase(),
          this.state.sort.toUpperCase()
        )
      }
    )
  }

  onSelectSortFilter = (label: string) => {
    this.setState(
      {
        sort: label
      },
      () => {
        this.fetchSearch(
          this.state.search,
          this.state.filter.toUpperCase(),
          this.state.sort.toUpperCase()
        )
      }
    )
  }

  fetchSearch(q: string, filter = 'ALL', sort = 'NAME') {
    this.props.fetchSearch(q, filter, sort)
  }

  render() {
    const { classes, isSearching, isSearchingInit } = this.props
    return (
      <Box width={538} position={'relative'} px={10} mr={-8} id={'searchContainer'}>
        <Box className={classes.searchIcon}>
          <FontAwesomeIcon icon={faSearch} color={THEME_EXTRA.typography.disabled} />
        </Box>
        {this.state.search.length === 0 && <SearchPlaceholder />}
        {this.state.search.length > 0 && (
          <Box className={classes.closeIcon}>
            <FontAwesomeIcon
              icon={faTimes}
              size={'1x'}
              color={THEME_EXTRA.typography.disabled}
              onClick={() => {
                this.setState({ open: false, search: '', selected: '' })
              }}
            />
          </Box>
        )}
        <Box>
          <MqInputBase
            spellCheck={false}
            className={classes.search}
            fullWidth={true}
            onFocus={() => this.setState({ open: true })}
            onChange={event => this.onSearch(event)}
            value={this.state.search}
            autoComplete={'off'}
            id={'searchBar'}
          />
          <ClickAwayListener
            mouseEvent='onMouseDown'
            touchEvent='onTouchStart'
            onClickAway={() => this.setState({ open: false })}
          >
            <Box>
              {this.state.open && this.state.search.length > 0 && (
                <Box position={'absolute'} width={'100%'} className={classes.dropdown}>
                  <Box className={classes.listHeader}>
                    <MqChipGroup
                      chips={INITIAL_SEARCH_FILTER}
                      onSelect={this.onSelectFilter}
                      initialSelection={this.state.filter}
                    />
                    <MqChipGroup
                      chips={INITIAL_SEARCH_SORT_FILTER}
                      onSelect={this.onSelectSortFilter}
                      initialSelection={this.state.sort}
                    />
                  </Box>
                  <Box className={classes.listContainer}>
                    {this.props.searchResults.size === 0 && (
                      <Box m={2} display={'flex'} alignItems={'center'} justifyContent={'center'}>
                        <MqText>
                          {isSearching || !isSearchingInit ? 'Searching...' : 'No Results'}
                        </MqText>
                      </Box>
                    )}
                    {[...this.props.searchResults].map((resultsWithGroups, index) => {
                      return resultsWithGroups.map(result => {
                        if (typeof result === 'string') {
                          // is group
                          if (result.length > 0) {
                            return (
                              <Box
                                className={classes.groupName}
                                key={result}
                                display={'flex'}
                                justifyContent={'space-between'}
                                alignItems={'center'}
                              >
                                <Box>
                                  <MqText bold font={'mono'}>
                                    {parseSearchGroup(result, 'group')}
                                  </MqText>
                                </Box>
                                <Box>
                                  <MqText bold font={'mono'} small>
                                    {parseSearchGroup(result, 'namespace')}
                                  </MqText>
                                </Box>
                              </Box>
                            )
                          } else return null
                          // is a list of group members
                        } else if (result.length) {
                          return (
                            <Box key={result[0].group + index}>
                              {result.map(listItem => {
                                return (
                                  <SearchListItem
                                    key={listItem.name}
                                    searchResult={listItem}
                                    search={this.state.search}
                                    selected={listItem.name === this.state.selected}
                                    onClick={nodeName => {
                                      this.setState({
                                        open: false,
                                        search: nodeName
                                      })
                                      this.props.setSelectedNode(listItem.nodeId)
                                    }}
                                  />
                                )
                              })}
                            </Box>
                          )
                        } else {
                          return null
                        }
                      })
                    })}
                  </Box>
                </Box>
              )}
            </Box>
          </ClickAwayListener>
        </Box>
      </Box>
    )
  }
}

const mapStateToProps = (state: IState) => {
  return {
    searchResults: state.search.data.results,
    rawResults: state.search.data.rawResults,
    isSearching: state.search.isLoading,
    isSearchingInit: state.search.init
  }
}

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      setSelectedNode: setSelectedNode,
      fetchSearch: fetchSearch
    },
    dispatch
  )

export default withStyles(styles)(
  connect(
    mapStateToProps,
    mapDispatchToProps
  )(withRouter(Search))
)
