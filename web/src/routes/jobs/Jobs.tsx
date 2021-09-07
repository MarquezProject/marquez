import { Theme } from '@material-ui/core/styles/createMuiTheme'
import Box from '@material-ui/core/Box'
import React from 'react'
import createStyles from '@material-ui/core/styles/createStyles'
import withStyles, { WithStyles } from '@material-ui/core/styles/withStyles'

const styles = (theme: Theme) => createStyles({})

type JobsProps = WithStyles<typeof styles>

class Jobs extends React.Component<JobsProps> {
  render() {
    return <Box>this is for jobs</Box>
  }
}

export default withStyles(styles)(Jobs)
