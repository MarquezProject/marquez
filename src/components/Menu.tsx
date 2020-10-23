import React from 'react'

import IconButton from '@material-ui/core/IconButton'
import Menu from '@material-ui/core/Menu'
import MenuIcon from '@material-ui/icons/Menu'
import MenuItem from '@material-ui/core/MenuItem'
import MqText from './core/text/MqText'

export default function SimpleMenu() {
  const [anchorEl, setAnchorEl] = React.useState(null)

  const handleClick = (event: any) => {
    setAnchorEl(event.currentTarget)
  }

  const handleClose = () => {
    setAnchorEl(null)
  }

  const feedBackClicked = () => {
    handleClose()
    // eslint-disable-next-line no-undef
    const link = __FEEDBACK_FORM_URL__
    window.open(link, '_blank')
  }
  const apiDocsClicked = () => {
    handleClose()
    // eslint-disable-next-line no-undef
    const link = __API_DOCS_URL__
    window.open(link, '_blank')
  }

  return (
    <div>
      <IconButton aria-controls='simple-menu' aria-haspopup='true' onClick={handleClick}>
        <MenuIcon htmlColor='#ffffff' />
      </IconButton>
      <Menu
        id='simple-menu'
        anchorEl={anchorEl}
        keepMounted
        open={Boolean(anchorEl)}
        onClose={handleClose}
      >
        <MenuItem onClick={feedBackClicked}>
          <MqText>Feedback</MqText>
        </MenuItem>
        <MenuItem onClick={apiDocsClicked}>
          <MqText>API Docs</MqText>
        </MenuItem>
      </Menu>
    </div>
  )
}
