import React, { ReactElement } from 'react';
import { AppBar, Toolbar, IconButton, Typography, Button, CircularProgress } from '@mui/material';
import { DRAWER_WIDTH } from '../../helpers/theme';
import { createTheme } from '@mui/material/styles';
import { useTheme } from '@emotion/react';
import Box from '@mui/material/Box';
import Search from '../search/Search';
import { useUser } from '../../context/UserContext';
import Menu from '@mui/material/Menu';
import MenuItem from '@mui/material/MenuItem';
import Avatar from '@mui/material/Avatar';

// Inside the Header component
const Header = (): ReactElement => {
  const theme = createTheme(useTheme());
  const { user, loading, error } = useUser();
  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
  const open = Boolean(anchorEl);

  const handleMenu = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleLogout = () => {
    window.location.href = '/logout';
  };

  const handleLogin = () => {
    window.location.href = '/oauth2/start?rd=https://localhost/';
  };

  return (
    <AppBar
      position='fixed'
      elevation={0}
      sx={{
        zIndex: theme.zIndex.drawer + 1,
        backgroundColor: theme.palette.background.default,
        borderBottom: `2px dashed ${theme.palette.secondary.main}`,
        left: `${DRAWER_WIDTH + 1}px`,
      }}
    >
      <Toolbar disableGutters>
        <Box
          sx={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            width: 'calc(100% - 97px)',
          }}
        >
          <Box display='flex' alignItems='center'>
            <Search />
          </Box>
          <Box display='flex' alignItems='center'>
            {loading ? (
              <CircularProgress size={24} />
            ) : error ? (
              <Typography variant='body1' color='error'>
                Error: {error}
              </Typography>
            ) : user ? (
              <>
                <Typography variant='body1' sx={{ marginRight: 2 }}>
                  Welcome, {user.email}
                </Typography>
                <IconButton
                  size='large'
                  edge='end'
                  color='inherit'
                  aria-label='account of current user'
                  aria-haspopup='true'
                  onClick={handleMenu}
                >
                  <Avatar>{user.email.charAt(0).toUpperCase()}</Avatar>
                </IconButton>
                <Menu
                  id='menu-appbar'
                  anchorEl={anchorEl}
                  anchorOrigin={{
                    vertical: 'top',
                    horizontal: 'right',
                  }}
                  keepMounted
                  transformOrigin={{
                    vertical: 'top',
                    horizontal: 'right',
                  }}
                  open={open}
                  onClose={handleClose}
                >
                  <MenuItem onClick={handleLogout}>Logout</MenuItem>
                </Menu>
              </>
            ) : (
              <Button color='inherit' onClick={handleLogin}>
                Login
              </Button>
            )}
          </Box>
        </Box>
      </Toolbar>
    </AppBar>
  );
};

export default Header;