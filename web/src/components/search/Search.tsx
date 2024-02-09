// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import * as Redux from 'redux'
import { Box, darken } from '@mui/material'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { GroupedSearch } from '../../types/api'
import { IState } from '../../store/reducers'
import { MqInputBase } from '../core/input-base/MqInputBase'
import { THEME_EXTRA, theme } from '../../helpers/theme'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { faCog, faDatabase, faSearch, faSort, faTimes } from '@fortawesome/free-solid-svg-icons'
import { fetchSearch, setSelectedNode } from '../../store/actionCreators'
import { parseSearchGroup } from '../../helpers/nodes'
import { useLocation } from 'react-router'
import ClickAwayListener from '@mui/material/ClickAwayListener'
import MqChipGroup from '../core/chip/MqChipGroup'
import MqText from '../core/text/MqText'
import React from 'react'
import SearchListItem from './SearchListItem'
import SearchPlaceholder from './SearchPlaceholder'
import debounce from '@mui/material/utils/debounce'

const i18next = require('i18next')

const INITIAL_SEARCH_FILTER = [
  {
    text: 'All',
    value: 'All',
  },
  {
    icon: faCog,
    foregroundColor: theme.palette.common.white,
    backgroundColor: theme.palette.primary.main,
    text: i18next.t('search.filter.jobs'),
    value: 'JOB',
  },
  {
    icon: faDatabase,
    foregroundColor: theme.palette.common.white,
    backgroundColor: theme.palette.primary.main,
    text: i18next.t('search.filter.datasets'),
    value: 'DATASET',
  },
]

const INITIAL_SEARCH_SORT_FILTER = [
  {
    icon: faSort,
    value: 'Sort',
    foregroundColor: theme.palette.common.white,
    backgroundColor: 'transparent',
    selectable: false,
  },
  {
    text: 'Updated at',
    value: 'UPDATE_AT',
  },
  {
    text: 'Name',
    value: 'NAME',
  },
]

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

type SearchProps = StateProps & DispatchProps

const Search: React.FC<SearchProps> = (props: SearchProps) => {
  const [state, setState] = React.useState<SearchState>({
    open: true,
    search: '',
    selected: '',
    filter: 'All',
    sort: 'UPDATE_AT',
  })

  const fetchSearch = (q: string, filter = 'ALL', sort = 'NAME') => {
    props.fetchSearch(q, filter, sort)
  }

  debounce(fetchSearch, 300)

  const location = useLocation()
  React.useEffect(() => {
    // close search on a route change
    setState({ ...state, open: false })
  }, [location])

  const onSearch = (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setState({ ...state, search: event.target.value, open: true })
    if (event.target.value.length > 0) {
      fetchSearch(event.target.value, state.filter.toUpperCase(), state.sort.toUpperCase())
    }
  }

  const onSelectFilter = (label: string) => {
    setState({
      ...state,
      filter: label,
    })

    setTimeout(() => {
      fetchSearch(state.search, state.filter.toUpperCase(), state.sort.toUpperCase())
    }, 1)
  }

  const onSelectSortFilter = (label: string) => {
    setState({
      ...state,
      sort: label,
    })

    setTimeout(() => {
      fetchSearch(state.search, state.filter.toUpperCase(), state.sort.toUpperCase())
    }, 1)
  }

  const { isSearching, isSearchingInit } = props

  return (
    <Box width={538} position={'relative'} px={10} mr={-8} id={'searchContainer'}>
      <Box
        sx={{
          zIndex: theme.zIndex.appBar + 3,
          position: 'absolute',
          left: theme.spacing(12),
          display: 'flex',
          alignItems: 'center',
          height: '100%',
        }}
      >
        <FontAwesomeIcon icon={faSearch} color={THEME_EXTRA.typography.disabled} />
      </Box>
      {state.search.length === 0 && <SearchPlaceholder />}
      {state.search.length > 0 && (
        <Box
          sx={{
            position: 'absolute',
            zIndex: theme.zIndex.appBar + 3,
            right: theme.spacing(12),
            display: 'flex',
            alignItems: 'center',
            height: '100%',
            cursor: 'pointer',
          }}
        >
          <FontAwesomeIcon
            icon={faTimes}
            size={'1x'}
            color={THEME_EXTRA.typography.disabled}
            onClick={() => {
              setState({ ...state, open: false, search: '', selected: '' })
            }}
          />
        </Box>
      )}
      <Box>
        <MqInputBase
          spellCheck={false}
          sx={{
            zIndex: theme.zIndex.appBar + 2,
          }}
          fullWidth={true}
          onFocus={() => setState({ ...state, open: true })}
          onChange={(event) => onSearch(event)}
          value={state.search}
          autoComplete={'off'}
          id={'searchBar'}
        />
        <ClickAwayListener
          mouseEvent='onMouseDown'
          touchEvent='onTouchStart'
          onClickAway={() => setState({ ...state, open: false })}
        >
          <Box>
            {state.open && state.search.length > 0 && (
              <Box
                position={'absolute'}
                width={'100%'}
                sx={{
                  position: 'absolute',
                  top: theme.spacing(-2),
                  width: '100%',
                  right: 0,
                  left: 0,
                  zIndex: theme.zIndex.appBar + 1,
                  border: `2px dashed ${theme.palette.secondary.main}`,
                  borderRadius: theme.spacing(1),
                  backgroundColor: theme.palette.background.default,
                }}
              >
                <Box
                  sx={{
                    marginTop: '64px',
                    padding: theme.spacing(2),
                    display: 'flex',
                    justifyContent: 'space-between',
                  }}
                >
                  <MqChipGroup
                    chips={INITIAL_SEARCH_FILTER}
                    onSelect={onSelectFilter}
                    initialSelection={state.filter}
                  />
                  <MqChipGroup
                    chips={INITIAL_SEARCH_SORT_FILTER}
                    onSelect={onSelectSortFilter}
                    initialSelection={state.sort}
                  />
                </Box>
                <Box
                  sx={{
                    margin: 0,
                    overflow: 'auto',
                    maxHeight: `calc(100vh - ${theme.spacing(30)})`,
                    paddingLeft: 0,
                    borderBottomLeftRadius: theme.spacing(1),
                    borderBottomRightRadius: theme.spacing(1),
                  }}
                >
                  {props.searchResults.size === 0 && (
                    <Box m={2} display={'flex'} alignItems={'center'} justifyContent={'center'}>
                      <MqText>
                        {isSearching || !isSearchingInit
                          ? i18next.t('search.status')
                          : i18next.t('search.none')}
                      </MqText>
                    </Box>
                  )}
                  {[...props.searchResults].map((resultsWithGroups, index) => {
                    return resultsWithGroups.map((result) => {
                      if (typeof result === 'string') {
                        // is group
                        if (result.length > 0) {
                          return (
                            <Box
                              sx={{
                                borderTop: `2px solid ${theme.palette.common.white}`,
                                borderBottom: `2px solid ${theme.palette.common.white}`,
                                padding: `${theme.spacing(1)} ${theme.spacing(3)} ${theme.spacing(
                                  0.5
                                )} ${theme.spacing(1)}`,
                                backgroundColor: darken(theme.palette.background.paper, 0.05),
                              }}
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
                            {result.map((listItem) => {
                              return (
                                <React.Fragment key={listItem.name}>
                                  <SearchListItem
                                    searchResult={listItem}
                                    search={state.search}
                                    selected={listItem.name === state.selected}
                                    onClick={(nodeName) => {
                                      setState({
                                        ...state,
                                        open: false,
                                        search: nodeName,
                                      })
                                      props.setSelectedNode(listItem.nodeId)
                                    }}
                                  />
                                </React.Fragment>
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

const mapStateToProps = (state: IState) => {
  return {
    searchResults: state.search.data.results,
    rawResults: state.search.data.rawResults,
    isSearching: state.search.isLoading,
    isSearchingInit: state.search.init,
  }
}

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      setSelectedNode: setSelectedNode,
      fetchSearch: fetchSearch,
    },
    dispatch
  )

export default connect(mapStateToProps, mapDispatchToProps)(Search)
