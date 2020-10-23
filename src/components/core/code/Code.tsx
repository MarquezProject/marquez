import { THEME_EXTRA } from '../../../helpers/theme'
import { Theme } from '@material-ui/core/styles/createMuiTheme'
import { fade } from '@material-ui/core/styles'
import Box from '@material-ui/core/Box'
import MqText from '../text/MqText'
import React from 'react'
import createStyles from '@material-ui/core/styles/createStyles'
import withStyles, { WithStyles } from '@material-ui/core/styles/withStyles'

const styles = (theme: Theme) =>
  createStyles({
    // eslint-disable-next-line @typescript-eslint/ban-ts-ignore
    // @ts-ignore
    codeContainer: {
      padding: `${theme.spacing(2)}px ${theme.spacing(4)}px`,
      backgroundColor: fade(theme.palette.common.white, 0.1),
      borderLeft: `2px solid ${THEME_EXTRA.typography.subdued}`,
      whiteSpace: 'break-spaces'
    }
  })

interface OwnProps {
  code?: string
  description?: string
}

const Code: React.FC<OwnProps & WithStyles<typeof styles>> = ({ code, description, classes }) => {
  return (
    <Box className={classes.codeContainer}>
      {description && (
        <Box mb={2}>
          <MqText bold font={'mono'} subdued>
            {description}
          </MqText>
        </Box>
      )}
      <MqText font={'mono'} subdued>
        {code ? code : 'Nothing to show here'}
      </MqText>
    </Box>
  )
}

export default withStyles(styles)(Code)
