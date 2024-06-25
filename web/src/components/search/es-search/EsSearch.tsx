// Copyright 2018-2024 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import * as Redux from 'redux'
import { Chip, Divider } from '@mui/material'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { IEsSearchDatasetsState } from '../../../store/reducers/esSearchDatasets'
import { IEsSearchJobsState } from '../../../store/reducers/esSearch'
import { IState } from '../../../store/reducers'
import { Nullable } from '../../../types/util/Nullable'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { faCog } from '@fortawesome/free-solid-svg-icons/faCog'
import { faDatabase } from '@fortawesome/free-solid-svg-icons'
import { fetchEsSearchDatasets, fetchEsSearchJobs } from '../../../store/actionCreators'
import { theme } from '../../../helpers/theme'
import { truncateText } from '../../../helpers/text'
import Box from '@mui/system/Box'
import MQTooltip from '../../core/tooltip/MQTooltip'
import MqEmpty from '../../core/empty/MqEmpty'
import MqText from '../../core/text/MqText'
import React, { useEffect } from 'react'

interface StateProps {
  esSearchJobs: IEsSearchJobsState
  esSearchDatasets: IEsSearchDatasetsState
}

interface DispatchProps {
  fetchEsSearchJobs: typeof fetchEsSearchJobs
  fetchEsSearchDatasets: typeof fetchEsSearchDatasets
}

interface Props {
  search: string
}

type TextSegment = {
  text: string
  isHighlighted: boolean
}

function parseStringToSegments(input: string): TextSegment[] {
  return input.split(/(<em>.*?<\/em>)/).map((segment) => {
    if (segment.startsWith('<em>') && segment.endsWith('</em>')) {
      return {
        text: segment.slice(4, -5),
        isHighlighted: true,
      }
    } else {
      return {
        text: segment,
        isHighlighted: false,
      }
    }
  })
}

function getValueAfterLastPeriod(s: string) {
  return s.split('.').pop()
}

const useArrowKeys = (callback: (direction: 'up' | 'down') => void) => {
  useEffect(() => {
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'ArrowDown') {
        event.preventDefault() // Prevent the default browser action
        callback('down')
      } else if (event.key === 'ArrowUp') {
        event.preventDefault() // Prevent the default browser action
        callback('up')
      }
    }
    window.addEventListener('keydown', handleKeyDown)
    return () => {
      window.removeEventListener('keydown', handleKeyDown)
    }
  }, [callback])
}

const EsSearch: React.FC<StateProps & DispatchProps & Props> = ({
  search,
  fetchEsSearchJobs,
  fetchEsSearchDatasets,
  esSearchJobs,
  esSearchDatasets,
}) => {
  const [selectedIndex, setSelectedIndex] = React.useState<Nullable<number>>(null)

  useArrowKeys((direction) => {
    if (direction === 'up') {
      setSelectedIndex(selectedIndex === null ? null : Math.max(selectedIndex - 1, 0))
    } else {
      setSelectedIndex(
        selectedIndex === null
          ? 0
          : Math.min(
              selectedIndex + 1,
              esSearchJobs.data.hits.length + esSearchDatasets.data.hits.length - 1
            )
      )
    }
  })

  useEffect(() => {
    fetchEsSearchJobs(search)
    fetchEsSearchDatasets(search)
  }, [search, fetchEsSearchJobs])

  useEffect(() => {
    setSelectedIndex(null)
  }, [esSearchJobs.data.hits, esSearchDatasets.data.hits])

  if (esSearchJobs.data.hits.length === 0 && esSearchDatasets.data.hits.length === 0) {
    return (
      <Box my={4}>
        <MqEmpty title={'No Hits'} body={'Keep typing or try a more precise search.'} />
      </Box>
    )
  }

  return (
    <Box>
      {esSearchJobs.data.hits.map((hit, index) => {
        return (
          <Box
            key={hit.run_id}
            px={2}
            py={1}
            borderBottom={1}
            borderColor={'divider'}
            sx={{
              transition: 'background-color 0.3s',
              cursor: 'pointer',
              '&:hover': {
                backgroundColor: theme.palette.action.hover,
              },
              backgroundColor: selectedIndex === index ? theme.palette.action.hover : undefined,
            }}
          >
            <Box display={'flex'}>
              <Box display={'flex'} alignItems={'center'}>
                <FontAwesomeIcon icon={faCog} color={theme.palette.primary.main} />
              </Box>
              <Box ml={2} width={280} minWidth={280}>
                <MQTooltip title={hit.name}>
                  <Box>
                    <MqText font={'mono'}>{truncateText(hit.name, 30)}</MqText>
                  </Box>
                </MQTooltip>
                <MQTooltip title={hit.namespace}>
                  <Box>
                    <MqText subdued>{truncateText(hit.namespace, 30)}</MqText>
                  </Box>
                </MQTooltip>
              </Box>
              <Divider flexItem sx={{ mx: 1 }} orientation={'vertical'} />
              <Box>
                <MqText subdued>Match</MqText>
                <Box>
                  {Object.entries(esSearchJobs.data.highlights[index]).map(([key, value]) => {
                    return value.map((highlightedString: any, idx: number) => {
                      return (
                        <Box
                          key={`${key}-${value}-${idx}`}
                          display={'flex'}
                          alignItems={'center'}
                          mb={0.5}
                        >
                          <Chip
                            label={getValueAfterLastPeriod(key)}
                            variant={'outlined'}
                            size={'small'}
                            sx={{ mr: 1 }}
                          />
                          <Box>
                            {parseStringToSegments(highlightedString || '').map(
                              (segment, index) => (
                                <MqText
                                  subdued
                                  small
                                  key={`${key}-${highlightedString}-${segment.text}-${index}`}
                                  inline
                                  highlight={segment.isHighlighted}
                                >
                                  {segment.text}
                                </MqText>
                              )
                            )}
                          </Box>
                        </Box>
                      )
                    })
                  })}
                </Box>
              </Box>
              {hit.facets.sourceCode?.language && (
                <>
                  <Divider flexItem sx={{ mx: 1 }} orientation={'vertical'} />
                  <Box>
                    <MqText subdued>{'Language'}</MqText>
                    <Chip
                      size={'small'}
                      variant={'outlined'}
                      label={hit.facets.sourceCode.language}
                    />
                  </Box>
                </>
              )}
            </Box>
          </Box>
        )
      })}
      {esSearchDatasets.data.hits.map((hit, index) => {
        return (
          <Box
            key={hit.run_id}
            px={2}
            py={1}
            borderBottom={1}
            borderColor={'divider'}
            sx={{
              transition: 'background-color 0.3s',
              cursor: 'pointer',
              '&:hover': {
                backgroundColor: theme.palette.action.hover,
              },
              backgroundColor:
                selectedIndex === index + esSearchDatasets.data.hits.length
                  ? theme.palette.action.hover
                  : undefined,
            }}
          >
            <Box display={'flex'}>
              <Box display={'flex'} alignItems={'center'}>
                <FontAwesomeIcon icon={faDatabase} color={theme.palette.info.main} />
              </Box>
              <Box ml={2} width={280} minWidth={280}>
                <MQTooltip title={hit.name}>
                  <Box>
                    <MqText font={'mono'}>{truncateText(hit.name, 30)}</MqText>
                  </Box>
                </MQTooltip>
                <MQTooltip title={hit.namespace}>
                  <Box>
                    <MqText subdued>{truncateText(hit.namespace, 30)}</MqText>
                  </Box>
                </MQTooltip>
              </Box>
              <Divider orientation={'vertical'} sx={{ mx: 1 }} flexItem />
              <Box>
                <MqText subdued>Match</MqText>
                <Box>
                  {Object.entries(esSearchDatasets.data.highlights[index]).map(([key, value]) => {
                    return value.map((highlightedString: any, idx: number) => {
                      return (
                        <Box
                          key={`${key}-${value}-${idx}`}
                          display={'flex'}
                          alignItems={'center'}
                          mb={0.5}
                          mr={0.5}
                        >
                          <Chip label={key} variant={'outlined'} size={'small'} sx={{ mr: 1 }} />
                          {parseStringToSegments(highlightedString || '').map((segment, index) => (
                            <MqText
                              subdued
                              small
                              key={`${key}-${highlightedString}-${segment.text}-${index}`}
                              inline
                              highlight={segment.isHighlighted}
                            >
                              {segment.text}
                            </MqText>
                          ))}
                        </Box>
                      )
                    })
                  })}
                </Box>
              </Box>
              <Divider orientation={'vertical'} flexItem sx={{ mx: 1 }} />
              <Box display={'flex'} flexDirection={'column'} justifyContent={'flex-start'}>
                <MqText subdued>Total Fields</MqText>
                <MqText font={'mono'}>{hit.facets.schema.fields.length.toString()} fields</MqText>
              </Box>
            </Box>
          </Box>
        )
      })}
    </Box>
  )
}

const mapStateToProps = (state: IState) => {
  return {
    esSearchJobs: state.esSearchJobs,
    esSearchDatasets: state.esSearchDatasets,
  }
}

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      fetchEsSearchJobs: fetchEsSearchJobs,
      fetchEsSearchDatasets: fetchEsSearchDatasets,
    },
    dispatch
  )

export default connect(mapStateToProps, mapDispatchToProps)(EsSearch)
