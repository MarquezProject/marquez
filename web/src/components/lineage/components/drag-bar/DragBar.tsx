// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import React from 'react'

import * as Redux from 'redux'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { createTheme } from '@mui/material/styles'
import { setBottomBarHeight } from '../../../../store/actionCreators'
import { useTheme } from '@emotion/react'
import Box from '@mui/material/Box'

interface DispatchProps {
  setBottomBarHeight: (offset: number) => void
}

type DragBarProps = DispatchProps

// height of bar / 2
const MAGIC_OFFSET_BASE = 4

const DragBar: React.FC<DragBarProps> = ({ setBottomBarHeight }) => {
  const [isResizing, setIsResizing] = React.useState<boolean>(false)

  React.useEffect(() => {
    window.addEventListener('mousemove', handleMousemove)
    window.addEventListener('mouseup', handleMouseup)

    return () => {
      window.removeEventListener('mousemove', handleMousemove)
      window.removeEventListener('mouseup', handleMouseup)
    }
  }, [isResizing])

  const handleMousedown = () => {
    setIsResizing(true)
  }

  const handleMousemove = (e: MouseEvent) => {
    if (!isResizing) {
      return
    }
    setBottomBarHeight(window.innerHeight - e.clientY - MAGIC_OFFSET_BASE)
  }

  const handleMouseup = () => {
    setIsResizing(false)
  }

  const theme = createTheme(useTheme())

  return (
    <Box
      sx={Object.assign(
        {
          backgroundColor: theme.palette.secondary.main,
          height: theme.spacing(1),
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'space-evenly',
          cursor: 'ns-resize',
          transition: theme.transitions.create(['background-color']),
          '&:hover': {
            backgroundColor: theme.palette.primary.main,
          },
        },
        isResizing
          ? {
              backgroundColor: theme.palette.primary.main,
            }
          : {}
      )}
      onMouseDown={handleMousedown}
    >
      <Box
        sx={{
          width: theme.spacing(5),
          height: '1px',
          backgroundColor: theme.palette.common.white,
        }}
      />
      <Box
        sx={{
          width: theme.spacing(5),
          height: '1px',
          backgroundColor: theme.palette.common.white,
        }}
      />
    </Box>
  )
}

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      setBottomBarHeight: setBottomBarHeight,
    },
    dispatch
  )
export default connect(null, mapDispatchToProps)(DragBar)
