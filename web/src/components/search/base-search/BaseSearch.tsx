// Copyright 2018-2024 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import * as Redux from 'redux'
import { GroupedSearch } from '../../../types/api'
import { IState } from '../../../store/reducers'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { faCog, faDatabase, faSort } from '@fortawesome/free-solid-svg-icons'
import { fetchSearch, setSelectedNode } from '../../../store/actionCreators'
import { parseSearchGroup } from '../../../helpers/nodes'
import { theme } from '../../../helpers/theme'
import Box from '@mui/system/Box'
import MqChipGroup from '../../core/chip/MqChipGroup'
import MqText from '../../core/text/MqText'
import React, { useEffect, useState } from 'react'
import SearchListItem from '../SearchListItem'

interface BaseSearchProps {
  search: string
}

interface StateProps {
  searchResults: Map<string, GroupedSearch[]>
  isSearching: boolean
  isSearchingInit: boolean
}

interface DispatchProps {
  setSelectedNode: typeof setSelectedNode
  fetchSearch: typeof fetchSearch
}

const INITIAL_SEARCH_FILTER = [
  {
    text: 'All',
    value: 'All',
  },
  {
    icon: faCog,
    foregroundColor: theme.palette.common.white,
    backgroundColor: theme.palette.primary.main,
    text: 'JOBS',
    value: 'JOB',
  },
  {
    icon: faDatabase,
    foregroundColor: theme.palette.common.white,
    backgroundColor: theme.palette.info.main,
    text: 'DATASETS',
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

const BaseSearch: React.FC<BaseSearchProps & StateProps & DispatchProps> = ({
  search,
  isSearchingInit,
  searchResults,
  isSearching,
  fetchSearch,
  setSelectedNode,
}) => {
  const [filter, setFilter] = useState('All')
  const [sort, setSort] = useState('UPDATE_AT')

  const i18next = require('i18next')

  const onSelectFilter = (label: string) => {
    setFilter(label)
    fetchSearch(search, label.toUpperCase(), sort.toUpperCase())
  }

  const onSelectSortFilter = (label: string) => {
    setSort(label)
    fetchSearch(search, filter.toUpperCase(), label.toUpperCase())
  }

  const searchApi = (q: string, filter = 'ALL', sort = 'NAME') => {
    fetchSearch(q, filter, sort)
  }

  useEffect(() => {
    if (search.length > 0) {
      searchApi(search, filter, sort)
    }
  }, [search, filter, sort])

  return (
    <>
      <Box
        sx={{
          padding: theme.spacing(2),
          display: 'flex',
          justifyContent: 'space-between',
        }}
      >
        <MqChipGroup
          chips={INITIAL_SEARCH_FILTER}
          onSelect={onSelectFilter}
          initialSelection={filter}
        />
        <MqChipGroup
          chips={INITIAL_SEARCH_SORT_FILTER}
          onSelect={onSelectSortFilter}
          initialSelection={sort}
        />
      </Box>
      <Box
        sx={{
          margin: 0,
          overflow: 'auto',
          maxHeight: `calc(100vh - ${theme.spacing(20)})`,
          paddingLeft: 0,
          borderBottomLeftRadius: theme.spacing(1),
          borderBottomRightRadius: theme.spacing(1),
        }}
      >
        {searchResults.size === 0 && (
          <Box m={2} display={'flex'} alignItems={'center'} justifyContent={'center'}>
            <MqText>
              {isSearching || !isSearchingInit
                ? i18next.t('search.status')
                : i18next.t('search.none')}
            </MqText>
          </Box>
        )}
        {[...searchResults].map((resultsWithGroups, index) => {
          return resultsWithGroups.map((result) => {
            if (typeof result === 'string') {
              // is group
              if (result.length > 0) {
                return (
                  <Box
                    sx={{
                      borderTop: `2px dashed ${theme.palette.secondary.main}`,
                      borderBottom: `2px dashed ${theme.palette.secondary.main}`,
                      padding: `${theme.spacing(0)} ${theme.spacing(3)} ${theme.spacing(
                        0.5
                      )} ${theme.spacing(1)}`,
                      backgroundColor: theme.palette.background.paper,
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
                          search={search}
                          onClick={() => {
                            setSelectedNode(listItem.nodeId)
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
    </>
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

export default connect(mapStateToProps, mapDispatchToProps)(BaseSearch)
