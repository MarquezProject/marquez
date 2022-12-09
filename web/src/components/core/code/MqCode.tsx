// SPDX-License-Identifier: Apache-2.0

import { THEME_EXTRA, theme } from '../../../helpers/theme'
import { alpha } from '@material-ui/core/styles'
import { ocean } from 'react-syntax-highlighter/dist/cjs/styles/hljs'
import Box from '@material-ui/core/Box'
import MqText from '../text/MqText'
import React from 'react'
import SyntaxHighlighter from 'react-syntax-highlighter'
import createStyles from '@material-ui/core/styles/createStyles'
import withStyles, { WithStyles } from '@material-ui/core/styles/withStyles'

const styles = () => createStyles({})

interface OwnProps {
  code?: string
  language?: string
  description?: string
}

const MqCode: React.FC<OwnProps & WithStyles<typeof styles>> = ({
  code,
  description,
  language
}) => {
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
          padding: theme.spacing(2)
        }}
      >
        {code ? code : 'No code available'}
      </SyntaxHighlighter>
    </Box>
  )
}

export default withStyles(styles)(MqCode)
