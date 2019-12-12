import React, { ReactElement } from 'react'
import { AppBar, Toolbar, Typography } from '@material-ui/core'
import {
  Link
} from 'react-router-dom'

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
    },
    link: {
      textDecoration: 'none',
      color: 'white'
    }
  })
}

interface IProps extends IWithStyles<typeof styles> {}

const MyAppBar = (props: IProps): ReactElement => {
  const { classes } = props
  const { link } = classes
  return (
    <AppBar position='fixed' className={classes.appBar}>
      <Toolbar>
        <Link className={link} to="/">
          <Typography className={classes.icon} variant='h4' color='inherit' noWrap>
            MARQUEZ
          </Typography>
        </Link>
        <div className={classes.rightToolbar}>
          <Menu></Menu>
        </div>
      </Toolbar>
    </AppBar>
  )
}

export default withStyles(styles)(MyAppBar)
