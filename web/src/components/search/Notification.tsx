import * as Redux from 'redux'
import { Archive, Cancel, CheckCircle, Notifications } from '@mui/icons-material'
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
import { truncateText, truncateTextFront } from '../../helpers/text'
import { useNavigate } from 'react-router-dom'
import IconButton from '@mui/material/IconButton'
import MQTooltip from '../core/tooltip/MQTooltip'
import MqText from '../core/text/MqText'
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
      <IconButton
        aria-describedby={id}
        onClick={handleClick}
        disableRipple
        disabled={notifications.length === 0}
      >
        <Badge
          badgeContent={notifications.length}
          color={notifications.length === 0 ? 'secondary' : 'warning'}
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
                <CheckCircle color={'primary'} fontSize='small' />
              ) : (
                <Cancel color='error' fontSize='small' />
              )}
            </ListItemIcon>
            <Box sx={{ flexGrow: 1 }}>
              <ListItemText
                primary={
                  <>
                    <MqText subdued inline>
                      Run{' '}
                    </MqText>
                    {notification.runUuid ? (
                      <MQTooltip title={notification.runUuid}>
                        <Box display={'inline'}>
                          <MqText inline>{truncateText(notification.runUuid, 8)}</MqText>
                        </Box>
                      </MQTooltip>
                    ) : (
                      ''
                    )}
                    <MqText subdued inline>
                      {' '}
                      for job{' '}
                    </MqText>
                    {truncateTextFront(notification.displayName, 16)}
                    <MqText subdued inline>
                      {' '}
                      transitioned to state{' '}
                    </MqText>
                    {notification.type}
                  </>
                }
                secondary={`at ${formatUpdatedAt(notification.createdAt)}`}
                primaryTypographyProps={{ variant: 'body2' }}
                secondaryTypographyProps={{ variant: 'caption', color: 'text.secondary' }}
              />
            </Box>
            <MQTooltip title={'Archive'}>
              <IconButton
                size='small'
                sx={{ ml: 2 }}
                onClick={(e) => {
                  e.preventDefault()
                  e.stopPropagation()
                  return archiveNotification(notification.uuid)
                }}
              >
                <Archive color={'secondary'} fontSize='small' />
              </IconButton>
            </MQTooltip>
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
