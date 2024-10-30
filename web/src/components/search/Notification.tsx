import {Badge, Box, Divider, ListItemIcon, ListItemText, Menu, MenuItem} from "@mui/material";
import {theme} from "../../helpers/theme";
import {Archive, Notifications, Warning} from "@mui/icons-material";
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
     <IconButton aria-describedby={id} onClick={handleClick} disableRipple>
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
       <MenuItem dense disableRipple>
         <ListItemIcon>
           <Warning color="error" fontSize="small" />
         </ListItemIcon>
         <Box sx={{ flexGrow: 1 }}>
           <ListItemText
             primary={`JOB food_delivery delivery_times_7_days failed`}
             secondary={`at 10:24am`}
             primaryTypographyProps={{ variant: 'body2' }}
             secondaryTypographyProps={{ variant: 'caption', color: 'text.secondary' }}
           />
         </Box>
         <IconButton size="small" sx={{ ml: 2 }}>
           <Archive color={'secondary'} fontSize="small" />
         </IconButton>
       </MenuItem>
       <Divider />
       {/*archive all */}
       <MenuItem dense disableRipple>
            Archive all
       </MenuItem>
     </Menu>
   </>
 )
}