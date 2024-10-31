import * as Redux from 'redux'
import { Archive, Check, Notifications, Warning } from '@mui/icons-material'
import { Badge, Box, Divider, ListItemIcon, ListItemText, Menu, MenuItem } from '@mui/material'
import { IState } from '../../store/reducers'
import { Notification } from '../../types/api'
import {
  archiveAllNotifications,
  archiveNotification,
  fetchNotifications,
} from '../../store/actionCreators'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { formatUpdatedAt } from '../../helpers'
import { theme } from '../../helpers/theme'
import { truncateText } from '../../helpers/text'
import { useNavigate } from 'react-router-dom'
import IconButton from '@mui/material/IconButton'
import React from 'react'

interface StateProps {
  notifications: Notification[]
  areNotificationsLoading: boolean
}

interface DispatchProps {
  fetchNotifications: typeof fetchNotifications
  archiveNotification: typeof archiveNotification
  archiveAllNotifications: typeof archiveAllNotifications
}

interface Props extends StateProps, DispatchProps {}

const Notification = ({
  notifications,
  fetchNotifications,
  archiveNotification,
  archiveAllNotifications,
}: Props) => {
  const [anchorEl, setAnchorEl] = React.useState<HTMLButtonElement | null>(null)

  const navigate = useNavigate()

  React.useEffect(() => {
    fetchNotifications()
  }, [fetchNotifications])

  // refresh every 30 seconds
  React.useEffect(() => {
    const interval = setInterval(() => {
      fetchNotifications()
    }, 30000)
    return () => clearInterval(interval)
  }, [fetchNotifications])

  const handleClick = (event: React.MouseEvent<HTMLButtonElement>) => {
    setAnchorEl(event.currentTarget)
  }

  const handleClose = () => {
    setAnchorEl(null)
  }

  const open = Boolean(anchorEl)
  const id = open ? 'notification-popover' : undefined
  return (
    <>
      <IconButton aria-describedby={id} onClick={handleClick} disableRipple>
        <Badge
          badgeContent={notifications.length}
          color={notifications.length === 0 ? 'secondary' : 'error'}
        >
          <Notifications />
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
        {notifications.map((notification) => (
          <MenuItem
            dense
            disableRipple
            key={notification.uuid}
            onClick={() => {
              notification.link && navigate(notification.link)
            }}
          >
            <ListItemIcon>
              {notification.type === 'COMPLETE' ? (
                <Check color={'primary'} fontSize='small' />
              ) : (
                <Warning color='error' fontSize='small' />
              )}
            </ListItemIcon>
            <Box sx={{ flexGrow: 1 }}>
              <ListItemText
                primary={`Run ${
                  notification.run_uuid ? truncateText(notification.run_uuid, 8) : ''
                } for job ${notification.displayName} transitioned to state ${notification.type} `}
                secondary={`at ${formatUpdatedAt(notification.createdAt)}`}
                primaryTypographyProps={{ variant: 'body2' }}
                secondaryTypographyProps={{ variant: 'caption', color: 'text.secondary' }}
              />
            </Box>
            <IconButton
              size='small'
              sx={{ ml: 2 }}
              onClick={(e) => {
                e.preventDefault()
                e.stopPropagation()
                return archiveNotification(notification.uuid);
              }}
            >
              <Archive color={'secondary'} fontSize='small' />
            </IconButton>
          </MenuItem>
        ))}
        <Divider />
        <MenuItem dense disableRipple onClick={() => archiveAllNotifications()}>
          Archive all
        </MenuItem>
      </Menu>
    </>
  )
}

const mapStateToProps = (state: IState) => ({
  notifications: state.notifications.notifications,
  areNotificationsLoading: state.notifications.isLoading,
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      fetchNotifications: fetchNotifications,
      archiveNotification: archiveNotification,
      archiveAllNotifications: archiveAllNotifications,
    },
    dispatch
  )

export default connect(mapStateToProps, mapDispatchToProps)(Notification)
