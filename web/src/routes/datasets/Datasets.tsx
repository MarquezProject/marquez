import { Theme } from '@material-ui/core/styles/createMuiTheme'
import Box from '@material-ui/core/Box'
import React from 'react'
import createStyles from '@material-ui/core/styles/createStyles'
import withStyles, { WithStyles } from '@material-ui/core/styles/withStyles'

const styles = (theme: Theme) => createStyles({})

type DatasetsProps = WithStyles<typeof styles>

class Datasets extends React.Component<DatasetsProps> {
  render() {
    return <Box>this is for datasets</Box>
  }
}

export default withStyles(styles)(Datasets)
