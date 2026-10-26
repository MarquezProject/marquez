// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { Box } from '@mui/material'
import MqText from '../text/MqText'
import React from 'react'

export const OPENLINEAGE_INTEGRATIONS_URL = 'https://openlineage.io/docs/integrations/about'

// Shown in empty states so first-time users know how to get lineage data into Marquez.
const GettingStartedLink: React.FC = () => {
  const i18next = require('i18next')
  return (
    <Box my={1}>
      <MqText subdued>{i18next.t('empty_state.getting_started')}</MqText>
      <MqText link href={OPENLINEAGE_INTEGRATIONS_URL}>
        {i18next.t('empty_state.integrations_link')}
      </MqText>
    </Box>
  )
}

export default GettingStartedLink
