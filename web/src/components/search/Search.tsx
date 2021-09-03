import { Box, Theme, createStyles, darken } from '@material-ui/core'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { GroupedSearch } from '../../types/api'
import { SearchBase } from './SearchBase'
import { THEME_EXTRA, theme } from '../../helpers/theme'
import { connect } from 'react-redux'
import { faCog, faDatabase, faSearch, faSort, faTimes } from '@fortawesome/free-solid-svg-icons'
import { groupBy } from '../../types/util/groupBy'
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
    value: 'Jobs'
  },
  {
    icon: faDatabase,
    foregroundColor: theme.palette.common.white,
    backgroundColor: theme.palette.primary.main,
    text: 'Datasets',
    value: 'Datasets'
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
    text: 'Recent',
    value: 'Recent'
  },
  {
    text: 'Relevance',
    value: 'Name'
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
      top: 10
    },
    groupName: {
      borderTop: `1px solid ${theme.palette.primary.main}`,
      borderBottom: `1px solid ${theme.palette.primary.main}`,
      padding: `${theme.spacing(0.5)}px ${theme.spacing(3)}px ${theme.spacing(
        0.5
      )}px ${theme.spacing(1)}px`,
      backgroundColor: darken(theme.palette.background.paper, 0.05)
    },
    closeIcon: {
      position: 'absolute',
      zIndex: theme.zIndex.appBar + 3,
      right: theme.spacing(12),
      top: 10,
      cursor: 'pointer'
    },
    dropdown: {
      position: 'absolute',
      top: theme.spacing(-2),
      width: '100%',
      right: 0,
      left: 0,
      zIndex: theme.zIndex.appBar + 1,
      border: `2px dashed ${theme.palette.common.white}`,
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
  // fetchSearch: (q: string, filter: string, sort: string) => void
  // purgeSearch: () => void
  // nodeSearch: (search: string) => void
}

interface SearchState {
  open: boolean
  search: string
  selected: string
  filter: string
  sort: string
}

type UnregisterCallback = () => void

type SearchProps = StateProps & DispatchProps & WithStyles<typeof styles>

class Search extends React.Component<SearchProps, SearchState> {
  private unlisten!: UnregisterCallback

  constructor(props: SearchProps) {
    super(props)
    this.state = {
      open: true,
      search: '',
      selected: '',
      filter: 'All',
      sort: 'Recent'
    }
    // this.fetchSearch = debounce(this.fetchSearch, 300)
  }

  componentDidMount() {
    // close search on a route change
    // this.unlisten = this.props.history.listen(() => {
    //   this.setState({
    //     open: false
    //   })
    // })
  }

  componentWillUnmount() {
    // this.unlisten()
  }

  // determineSelected = (offset: 1 | -1) => {
  //   const currentIndex = this.props.searchResults.rawResults.findIndex(
  //     result => result.name === this.state.selected
  //   )
  //   const newIndex = currentIndex + offset
  //   if (newIndex >= 0 && newIndex < this.props.searchResults.rawResults.length) {
  //     return this.props.searchResults.rawResults[currentIndex + offset].name
  //   } else {
  //     return this.state.selected
  //   }
  // }

  handleKeyboard = (event: React.KeyboardEvent<HTMLElement>) => {
    switch (event.key) {
      case 'ArrowDown':
        break
      case 'ArrowUp':
        break
      case 'Escape':
        break
      case 'Enter':
        break
      default:
        this.setState({
          open: true,
          selected: ''
        })
    }
  }

  onSearch = (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    this.setState({ search: event.target.value }, () => {
      // this.fetchSearch(
      //   this.state.search,
      //   this.state.filter.toUpperCase(),
      //   this.state.sort.toUpperCase()
      // )
    })
  }

  onSelectFilter = (label: string) => {
    this.setState(
      {
        filter: label
      },
      () => {
        // this.fetchSearch(
        //   this.state.search,
        //   this.state.filter.toUpperCase(),
        //   this.state.sort.toUpperCase()
        // )
      }
    )
  }

  onSelectSortFilter = (label: string) => {
    this.setState(
      {
        sort: label
      },
      () => {
        // this.fetchSearch(
        //   this.state.search,
        //   this.state.filter.toUpperCase(),
        //   this.state.sort.toUpperCase()
        // )
      }
    )
  }

  // fetchSearch(q: string, filter = 'ALL', sort = 'RECENT') {
  //   // this.props.fetchSearch(q, filter, sort)
  // }

  render() {
    const { classes, isSearching, isSearchingInit, searchResults } = this.props
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
                // this.props.purgeSearch()
              }}
            />
          </Box>
        )}
        <Box>
          <SearchBase
            className={classes.search}
            fullWidth={true}
            onFocus={() => this.setState({ open: true })}
            onChange={event => this.onSearch(event)}
            value={this.state.search}
            autoComplete={'off'}
            onKeyDown={event => this.handleKeyboard(event)}
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
                                    {/*{parseSearchGroup(result, 'group')}*/}
                                    todo determine group
                                  </MqText>
                                </Box>
                                <Box>
                                  <MqText bold font={'mono'} small>
                                    {result}
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
                                      // this.props.setSelectedNode(listItem.nodeId, listItem.type)
                                      // this.props.nodeSearch(nodeName)
                                      this.setState({
                                        open: false,
                                        search: nodeName
                                      })
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

const mapStateToProps = () => {
  return {
    searchResults: groupBy(
      [
        {
          nodeId: 'dataset:food_delivery:public.top_delivery_times',
          updatedAt: '2021-09-03T05:19:19.468809Z',
          name: 'public.top_delivery_times',
          type: 'DATASET',
          namespace: 'food_delivery'
        },
        {
          nodeId: 'dataset:food_delivery:public.customers',
          updatedAt: '2021-09-03T05:19:19.468809Z',
          name: 'public.customers',
          type: 'DATASET',
          namespace: 'food_delivery'
        },
        {
          nodeId: 'job:food_delivery:analytics.orders_popular_day_of_week',
          updatedAt: '2021-09-03T05:19:19.468809Z',
          name: 'analytics.orders_popular_day_of_week',
          type: 'JOB',
          namespace: 'food_delivery'
        },
        {
          nodeId: 'job:food_delivery:emails.email_discounts',
          updatedAt: '2021-09-03T05:18:54.468809Z',
          name: 'emails.email_discounts',
          type: 'JOB',
          namespace: 'food_delivery'
        },
        {
          nodeId: 'dataset:food_delivery:public.delivery_7_days',
          updatedAt: '2021-09-03T05:15:14.468809Z',
          name: 'public.delivery_7_days',
          type: 'DATASET',
          namespace: 'food_delivery'
        },
        {
          nodeId: 'job:food_delivery:analytics.delivery_times_7_days',
          updatedAt: '2021-09-03T05:15:14.468809Z',
          name: 'analytics.delivery_times_7_days',
          type: 'JOB',
          namespace: 'food_delivery'
        },
        {
          nodeId: 'dataset:food_delivery:public.restaurants',
          updatedAt: '2021-09-03T05:11:26.468809Z',
          name: 'public.restaurants',
          type: 'DATASET',
          namespace: 'food_delivery'
        },
        {
          nodeId: 'dataset:food_delivery:public.drivers',
          updatedAt: '2021-09-03T05:11:26.468809Z',
          name: 'public.drivers',
          type: 'DATASET',
          namespace: 'food_delivery'
        },
        {
          nodeId: 'dataset:food_delivery:public.order_status',
          updatedAt: '2021-09-03T05:11:26.468809Z',
          name: 'public.order_status',
          type: 'DATASET',
          namespace: 'food_delivery'
        },
        {
          nodeId: 'dataset:food_delivery:public.orders_7_days',
          updatedAt: '2021-09-03T05:11:26.468809Z',
          name: 'public.orders_7_days',
          type: 'DATASET',
          namespace: 'food_delivery'
        },
        {
          nodeId: 'job:food_delivery:etl.etl_delivery_7_days',
          updatedAt: '2021-09-03T05:11:26.468809Z',
          name: 'etl.etl_delivery_7_days',
          type: 'JOB',
          namespace: 'food_delivery'
        },
        {
          nodeId: 'job:food_delivery:etl.etl_customers',
          updatedAt: '2021-09-03T05:08:10.468809Z',
          name: 'etl.etl_customers',
          type: 'JOB',
          namespace: 'food_delivery'
        },
        {
          nodeId: 'job:food_delivery:etl.etl_restaurants',
          updatedAt: '2021-09-03T05:08:04.468809Z',
          name: 'etl.etl_restaurants',
          type: 'JOB',
          namespace: 'food_delivery'
        },
        {
          nodeId: 'job:food_delivery:etl.etl_order_status',
          updatedAt: '2021-09-03T05:07:08.468809Z',
          name: 'etl.etl_order_status',
          type: 'JOB',
          namespace: 'food_delivery'
        },
        {
          nodeId: 'dataset:food_delivery:public.categories',
          updatedAt: '2021-09-03T05:07:03.468809Z',
          name: 'public.categories',
          type: 'DATASET',
          namespace: 'food_delivery'
        }
      ].map(result => {
        return {
          ...result,
          group: `${result.namespace}:${result.name.substring(0, result.name.lastIndexOf('.'))}`
        }
      }),
      'namespace'
    ),
    isSearching: false,
    isSearchingInit: true
  }
}

export default withStyles(styles)(connect(mapStateToProps)(Search))
