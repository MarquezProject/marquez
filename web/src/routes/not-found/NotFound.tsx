// Copyright 2018-2024 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import * as React from 'react'
import { Box } from '@mui/system'
import { Button } from '@mui/material'
import { Link as RouterLink } from 'react-router-dom'
import MqEmpty from '../../components/core/empty/MqEmpty'

export const NotFound = () => {
  return (
    <Box pt={4} display={'flex'} justifyContent={'center'}>
      <MqEmpty title={'Not Found'}>
        <>
          Sorry, the page you are looking for does not exist.
          <Button size={'small'} component={RouterLink} to={'/'}>
            Go Home
          </Button>
        </>
      </MqEmpty>
    </Box>
  )
}
