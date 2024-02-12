// SPDX-License-Identifier: Apache-2.0
import { Box } from '@mui/system'
import { darkTheme } from '@uiw/react-json-view/dark'
import { theme } from '../../../helpers/theme'
import JsonView from '@uiw/react-json-view'
import React from 'react'

interface OwnProps {
  data: object
  searchable?: boolean
  placeholder?: string
}

type JsonViewProps = OwnProps

darkTheme.background = theme.palette.background.default
darkTheme.backgroundColor = theme.palette.background.default
darkTheme.borderLeftWidth = 2
darkTheme.borderLeftColor = theme.palette.grey[500]
darkTheme.borderLeftStyle = 'dashed'

const MqJsonView: React.FC<JsonViewProps> = ({ data }) => {
  return (
    <Box my={2}>
      <JsonView style={darkTheme} value={data} />
    </Box>
  )
}

export default MqJsonView
