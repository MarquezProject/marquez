import {Badge, Box, Button, Menu, MenuItem} from "@mui/material";
import {theme} from "../../helpers/theme";
import {Archive, Feedback, Notifications} from "@mui/icons-material";
import IconButton from "@mui/material/IconButton";
import React from "react";

export const Notification = () => {
  const [anchorEl, setAnchorEl] = React.useState<HTMLButtonElement | null>(null);

  const handleClick = (event: React.MouseEvent<HTMLButtonElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const open = Boolean(anchorEl);
  const id = open ? 'simple-popover' : undefined;
 return (
   <>
     <IconButton aria-describedby={id} onClick={handleClick}>
       <Badge badgeContent={4} color={'primary'}>
         <Notifications/>
       </Badge>
     </IconButton>
     <Menu
       id={id}
       open={open}
       anchorEl={anchorEl}
       onClose={handleClose}
       slotProps={{
         paper: {
           sx: {
             backgroundColor: theme.palette.background.default,
             backgroundImage: 'none',
           },
         },
       }}
     >
       <MenuItem>
         <Feedback color={'error'} fontSize={'small'} />{' '}
         <Box ml={2}>delivery_times_7_days failed at 10:24 AM</Box>
         <IconButton size={'small'} sx={{ ml: 2 }}>
           <Archive fontSize={'small'} />
         </IconButton>
       </MenuItem>
       <MenuItem>
         <Feedback color={'error'} fontSize={'small'} />{' '}
         <Box ml={2}>delivery_times_7_days failed at 10:24 AM</Box>
         <IconButton size={'small'} sx={{ ml: 2 }}>
           <Archive fontSize={'small'} />
         </IconButton>
       </MenuItem>
       <MenuItem>
         <Feedback color={'error'} fontSize={'small'} />{' '}
         <Box ml={2}>delivery_times_7_days failed at 10:24 AM</Box>
         <IconButton size={'small'} sx={{ ml: 2 }}>
           <Archive fontSize={'small'} />
         </IconButton>
       </MenuItem>
       <MenuItem>
         <Feedback color={'error'} fontSize={'small'} />{' '}
         <Box ml={2}>delivery_times_7_days failed at 10:24 AM</Box>
         <IconButton size={'small'} sx={{ ml: 2 }}>
           <Archive fontSize={'small'} />
         </IconButton>
       </MenuItem>
       <MenuItem>
         <Button>Archive all</Button>
       </MenuItem>
     </Menu>
   </>
 )
}