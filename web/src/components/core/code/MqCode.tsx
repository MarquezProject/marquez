// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { THEME_EXTRA, theme } from '../../../helpers/theme'
import { alpha } from '@mui/material/styles'
import { ocean } from 'react-syntax-highlighter/dist/cjs/styles/hljs'
import Box from '@mui/material/Box'
import MqText from '../text/MqText'
import React from 'react'
import SyntaxHighlighter from 'react-syntax-highlighter'

interface OwnProps {
  code?: string
  language?: string
  description?: string
}

const MqCode: React.FC<OwnProps> = ({ code, description, language }) => {
  return (
    <Box>
      {description && (
        <Box mb={2}>
          <MqText bold font={'mono'} subdued>
            {description}
          </MqText>
        </Box>
      )}
      <SyntaxHighlighter
        language={language}
        style={ocean}
        customStyle={{
          backgroundColor: alpha(theme.palette.common.white, 0.1),
          borderLeft: `2px dashed ${THEME_EXTRA.typography.subdued}`,
          padding: theme.spacing(2),
        }}
      >
        {code ? code : 'No code available'}
      </SyntaxHighlighter>
    </Box>
  )
}

export default MqCode
