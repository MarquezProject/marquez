import React, { ReactElement } from 'react'
import { AppBar, Toolbar, Typography } from '@material-ui/core'
import {
  withStyles,
  createStyles,
  WithStyles as IWithStyles,
  Theme as ITheme
} from '@material-ui/core/styles'

import Menu from './Menu'

const styles = ({ zIndex }: ITheme) => {
  return createStyles({
    appBar: {
      zIndex: zIndex.drawer + 1,
      height: '0px'
    },
    rightToolbar: {
      marginLeft: 'auto',
      marginRight: 2.5
    },
    icon: {
      fontFamily: 'Karla',
      fontWeight: 'bold'
    }
  })
}

interface IProps extends IWithStyles<typeof styles> {}

const MyAppBar = (props: IProps): ReactElement => {
  const { classes } = props
  return (
    <AppBar position='fixed' className={classes.appBar}>
      <Toolbar>
        <Typography className={classes.icon} variant='h4' color='inherit' noWrap>
          MARQUEZ
        </Typography>
        <div className={classes.rightToolbar}>
          <Menu></Menu>
        </div>
      </Toolbar>
    </AppBar>
  )
}

export default withStyles(styles)(MyAppBar)
