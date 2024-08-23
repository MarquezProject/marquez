// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { Box, darken } from '@mui/material'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { JobOrDataset } from '../../types/lineage'
import { Link as RouterLink } from 'react-router-dom'
import { SearchResult } from '../../types/api'
import { encodeNode } from '../../helpers/nodes'
import { faCog } from '@fortawesome/free-solid-svg-icons/faCog'
import { faDatabase } from '@fortawesome/free-solid-svg-icons'
import { theme } from '../../helpers/theme'
import MqText from '../core/text/MqText'
import React from 'react'
import moment from 'moment'

interface OwnProps {
  searchResult: SearchResult
  search: string
  onClick: (nodeName: string) => void
}

const searchResultIcon: { [key in JobOrDataset]: JSX.Element } = {
  JOB: <FontAwesomeIcon icon={faCog} color={theme.palette.primary.main} />,
  DATASET: <FontAwesomeIcon icon={faDatabase} color={theme.palette.info.main} />,
}

type DkSearchListItemProps = OwnProps

const SearchListItem: React.FC<DkSearchListItemProps> = ({ searchResult, search, onClick }) => {
  const name = searchResult.name.substring(
    searchResult.name.lastIndexOf('.') + 1,
    searchResult.name.length
  )
  const searchMatchIndex = name.toLowerCase().indexOf(search.toLowerCase())
  return (
    <RouterLink
      style={{
        textDecoration: 'none',
      }}
      onClick={() => onClick(searchResult.name)}
      to={`/lineage/${encodeNode(searchResult.type, searchResult.namespace, searchResult.name)}`}
    >
      <Box
        sx={{
          display: 'block',
          color: 'inherit',
          textDecoration: 'none',
          margin: 0,
          cursor: 'pointer',
          padding: `${theme.spacing(1)} ${theme.spacing(3)}`,
          '&:not(:last-child)': {
            borderBottom: `1px solid ${theme.palette.secondary.main}`,
          },
          '&:last-child': {
            borderBottomLeftRadius: '2px',
            borderBottomRightRadius: '2px',
          },
          '&:hover': {
            backgroundColor: darken(theme.palette.background.paper, 0.02),
          },
          '&:nth-pf-type(even)': {
            backgroundColor: darken(theme.palette.background.paper, 0.2),
            '&:hover': {
              backgroundColor: darken(theme.palette.background.paper, 0.02),
            },
          },
        }}
      >
        <Box display={'flex'} alignItems={'center'} justifyContent={'space-between'}>
          <Box display={'flex'} alignItems={'center'}>
            <Box display={'inline'} mr={1}>
              {searchResultIcon[searchResult.type]}
            </Box>
            <Box
              sx={{
                display: 'inline',
                whiteSpace: 'nowrap',
                overflow: 'hidden',
                textOverflow: 'ellipsis',
                maxWidth: '370px',
              }}
            >
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
                    {name.substring(searchMatchIndex, searchMatchIndex + search.length)}
                  </MqText>
                  <MqText inline font={'mono'} bold small>
                    {name.substring(searchMatchIndex + search.length, searchResult.name.length)}
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
      </Box>
    </RouterLink>
  )
}

export default SearchListItem
