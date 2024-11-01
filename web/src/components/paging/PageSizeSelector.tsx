import { Button, Menu, MenuItem } from '@mui/material'
import React, { useState } from 'react'

type PageSizeSelectorProps = {
  onChange: (pageSize: number) => void
  initialPageSize?: number
}

const PAGE_OPTIONS = [20, 50, 100]

const PageSizeSelector: React.FC<PageSizeSelectorProps> = ({ onChange, initialPageSize = 20 }) => {
  const [pageSize, setPageSize] = useState(initialPageSize)
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null)

  const handleButtonClick = (event: React.MouseEvent<HTMLButtonElement>) => {
    setAnchorEl(event.currentTarget)
  }

  const handleMenuItemClick = (option: number) => {
    setPageSize(option)
    onChange(option)
    setAnchorEl(null)
  }

  const handleClose = () => {
    setAnchorEl(null)
  }

  return (
    <>
      <Button variant='outlined' color='primary' onClick={handleButtonClick}>
        Items per page: {pageSize}
      </Button>

      <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={handleClose}>
        {PAGE_OPTIONS.map((option) => (
          <MenuItem key={option} onClick={() => handleMenuItemClick(option)}>
            {option} items per page
          </MenuItem>
        ))}
      </Menu>
    </>
  )
}

export default PageSizeSelector
