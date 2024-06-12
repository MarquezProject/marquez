// Copyright 2018-2024 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import * as Redux from 'redux'
import { Chip } from '@mui/material'
import { IEsSearchState } from '../../../store/reducers/esSearch'
import { IState } from '../../../store/reducers'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { fetchEsSearch } from '../../../store/actionCreators'
import Box from '@mui/system/Box'
import MqText from '../../core/text/MqText'
import React, { useEffect } from 'react'

interface StateProps {
  esSearch: IEsSearchState
}

interface DispatchProps {
  fetchEsSearch: typeof fetchEsSearch
}

interface Props {
  search: string
}

type TextSegment = {
  text: string
  isBold: boolean
}

function parseStringToSegments(input: string): TextSegment[] {
  return input.split(/(<em>.*?<\/em>)/).map((segment) => {
    if (segment.startsWith('<em>') && segment.endsWith('</em>')) {
      return {
        text: segment.slice(4, -5),
        isBold: true,
      }
    } else {
      return {
        text: segment,
        isBold: false,
      }
    }
  })
}

const EsSearch: React.FC<StateProps & DispatchProps & Props> = ({
  search,
  fetchEsSearch,
  esSearch,
}) => {
  useEffect(() => {
    fetchEsSearch(search)
  }, [search, fetchEsSearch])
  console.log(esSearch)

  return (
    <Box>
      {esSearch.data.hits.map((hit, index) => {
        return (
          <Box key={hit.run_id} p={1} borderBottom={1} borderColor={'divider'}>
            {hit.name}
            <Box>
              {Object.entries(esSearch.data.highlights[index]).map(([key, value]) => {
                return value.map((highlightedString: any, idx: number) => {
                  return (
                    <Box key={`${key}-${highlightedString}`} display={'flex'} alignItems={'center'}>
                      <Chip label={key} size={'small'} sx={{ mr: 1 }} />
                      {parseStringToSegments(highlightedString || '').map((segment) => (
                        <MqText
                          subdued
                          small
                          key={`${segment.text}-${idx}`}
                          inline
                          highlight={segment.isBold}
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
        )
      })}
    </Box>
  )
}

const mapStateToProps = (state: IState) => {
  return {
    esSearch: state.esSearch,
  }
}

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      fetchEsSearch: fetchEsSearch,
    },
    dispatch
  )

export default connect(mapStateToProps, mapDispatchToProps)(EsSearch)
