// SPDX-License-Identifier: Apache-2.0
import { Box } from '@mui/system'
import { THEME_EXTRA, theme } from '../../../helpers/theme'
import { githubDarkTheme } from '@uiw/react-json-view/githubDark'
import JsonView from '@uiw/react-json-view'
import React from 'react'

interface OwnProps {
  data: object
}

type JsonViewProps = OwnProps

githubDarkTheme.background = theme.palette.background.default
githubDarkTheme.backgroundColor = theme.palette.background.default
githubDarkTheme.borderLeftWidth = 2
githubDarkTheme.borderLeftColor = theme.palette.grey[500]
githubDarkTheme.borderLeftStyle = 'dashed'

const mqTheme = {
  ...githubDarkTheme,
  '--w-rjv-info-color': THEME_EXTRA.typography.subdued,
  '--w-rjv-type-null-color': theme.palette.warning.main,
  '--w-rjv-type-boolean-color': theme.palette.error.main,
  '--w-rjv-copied-color': theme.palette.primary.main,
  '--w-rjv-key-string': theme.palette.common.white,
  '--w-rjv-type-string-color': theme.palette.info.main,
  '--w-rjv-ellipsis-color': theme.palette.info.main,
  '--w-rjv-key-number': theme.palette.primary.main,
  '--w-rjv-type-float-color': theme.palette.primary.main,
}

const MqJsonView: React.FC<JsonViewProps> = ({ data }) => {
  return (
    <Box my={2}>
      <JsonView style={mqTheme} collapsed={2} value={data} />
    </Box>
  )
}

export default MqJsonView
