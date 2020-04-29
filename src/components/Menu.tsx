import React from 'react'
import Button from '@material-ui/core/Button'
import Menu from '@material-ui/core/Menu'
import MenuIcon from '@material-ui/icons/Menu'
import MenuItem from '@material-ui/core/MenuItem'

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
    const link = __FEEDBACK_FORM_URL__
    window.open(link, '_blank')
  }
  const apiDocsClicked = () => {
    handleClose()
    const link = __API_DOCS_URL__
    window.open(link, '_blank')
  }

  return (
    <div>
      <Button aria-controls='simple-menu' aria-haspopup='true' onClick={handleClick}>
        <MenuIcon htmlColor='#ffffff'></MenuIcon>
      </Button>
      <Menu
        id='simple-menu'
        anchorEl={anchorEl}
        keepMounted
        open={Boolean(anchorEl)}
        onClose={handleClose}
      >
        <MenuItem onClick={feedBackClicked}>Feedback</MenuItem>
        <MenuItem onClick={apiDocsClicked}>API Docs</MenuItem>
      </Menu>
    </div>
  )
}
