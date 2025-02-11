// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { Box } from '@mui/material'
import { theme } from '../../helpers/theme'
import { trackEvent } from '../ga4'
import MqText from '../core/text/MqText'
import React from 'react'
import Typewriter from './Typewriter'

const importI18next = () => {
  return require('i18next')
}

interface SearchPlaceholderProps {}

const SearchPlaceholder: React.FC<SearchPlaceholderProps> = () => {
  const i18next = importI18next()
  React.useEffect(() => {
    trackEvent('SearchPlaceholder', 'View Search Placeholder')
  }, [])
  
  return (
    <Box
      sx={{
        zIndex: theme.zIndex.appBar + 3,
        position: 'absolute',
        left: 40,
        height: '100%',
        display: 'flex',
        alignItems: 'center',
        overflow: 'visible',
        pointerEvents: 'none',
      }}
    >
      <Box display={'inline'}>
        <MqText
          disabled
          inline
          font={'mono'}
          aria-label={i18next.t('search.search_aria')}
          aria-required='true'
        >
          Search your
        </MqText>
        <MqText bold inline>
          {' '}
          <Typewriter words={['Jobs and Datasets…']} repeatCount={3} />
        </MqText>
      </Box>
    </Box>
  )
}

export default SearchPlaceholder
