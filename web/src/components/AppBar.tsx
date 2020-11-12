import { AppBar, Toolbar, Typography } from '@material-ui/core'
import { Link } from 'react-router-dom'
import React, { ReactElement } from 'react'

import {
  Theme as ITheme,
  WithStyles as IWithStyles,
  createStyles,
  withStyles
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
      fontWeight: 'bold',
      marginTop: 3
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
        <Link className={link} to='/'>
          <img src={require('../img/marquez-logo.png')} height={60} alt='Marquez Logo' />
        </Link>
        <Link className={link} to='/'>
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
