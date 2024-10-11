// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { Box } from '@mui/material'
import { ChevronLeftRounded, ChevronRightRounded } from '@mui/icons-material'
import { theme } from '../../helpers/theme'
import IconButton from '@mui/material/IconButton'
import MQTooltip from '../core/tooltip/MQTooltip'
import MqText from '../core/text/MqText'
import React, { FunctionComponent } from 'react'

const i18next = require('i18next')

interface Props {
  pageSize: number
  currentPage: number
  totalCount: number
  incrementPage: (page: number) => void
  decrementPage: (page: number) => void
}

const MqPaging: FunctionComponent<Props> = (props) => {
  const { pageSize, currentPage, incrementPage, decrementPage, totalCount } = props
  return (
    <Box display={'flex'} justifyContent={'flex-end'} alignItems={'center'}>
      <MqText subdued>
        <>
          {pageSize * currentPage + 1} - {Math.min(pageSize * (currentPage + 1), totalCount)} of{' '}
          {totalCount}
        </>
      </MqText>
      <MQTooltip title={i18next.t('events_route.previous_page')}>
        <span>
          <IconButton
            sx={{
              marginLeft: theme.spacing(2),
            }}
            color='primary'
            disabled={currentPage === 0}
            onClick={() => decrementPage(1)}
            size='small'
          >
            <ChevronLeftRounded />
          </IconButton>
        </span>
      </MQTooltip>
      <MQTooltip title={i18next.t('events_route.next_page')}>
        <span>
          <IconButton
            color='primary'
            onClick={() => incrementPage(1)}
            size='small'
            disabled={currentPage === Math.ceil(totalCount / pageSize) - 1}
          >
            <ChevronRightRounded />
          </IconButton>
        </span>
      </MQTooltip>
    </Box>
  )
}

export default MqPaging
