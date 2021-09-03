import { AppBar, IconButton, Toolbar } from '@material-ui/core'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { Link } from 'react-router-dom'
import { Theme, WithStyles, createStyles, withStyles } from '@material-ui/core/styles'
import { faSync } from '@fortawesome/free-solid-svg-icons'
import Box from '@material-ui/core/Box'
import React, { ReactElement } from 'react'
import Search from '../search/Search'

const styles = (theme: Theme) => {
  return createStyles({
    appBar: {
      zIndex: theme.zIndex.drawer + 1,
      backgroundColor: theme.palette.background.default,
      borderBottom: `2px dashed ${theme.palette.secondary.main}`,
      padding: `${theme.spacing(2)}px 0`
    },
    toolbar: {
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center'
    },
    innerToolbar: {
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center',
      maxWidth: theme.breakpoints.values.lg,
      width: theme.breakpoints.values.lg,
      margin: '0 auto'
    }
  })
}

interface OwnProps {
  setShowJobs: (bool: boolean) => void
  showJobs: boolean
}

type HeaderProps = OwnProps & WithStyles<typeof styles>

const Header = (props: HeaderProps): ReactElement => {
  const { classes } = props
  return (
    <AppBar position='fixed' elevation={0} className={classes.appBar}>
      <Toolbar>
        <Box className={classes.innerToolbar}>
          <Link to='/'>
            <img src={require('../../img/marquez_logo.svg')} height={48} alt='Marquez Logo' />
          </Link>
          <Box display={'flex'} alignItems={'center'}>
            <Search />
            <IconButton aria-label='refresh'>
              <FontAwesomeIcon icon={faSync} size={'xs'} />
            </IconButton>
          </Box>
        </Box>
      </Toolbar>
    </AppBar>
  )
}

export default withStyles(styles)(Header)
