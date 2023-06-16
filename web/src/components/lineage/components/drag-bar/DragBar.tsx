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

interface DragBarState {
  isResizing: boolean
  lastY: number
}

type DragBarProps = DispatchProps

// height of bar / 2
const MAGIC_OFFSET_BASE = 4

const DragBar: React.FC<DragBarProps> = ({ setBottomBarHeight }) => {
  const [state, setState] = React.useState<DragBarState>({
    isResizing: false,
    lastY: 0
  })

  React.useEffect(() => {
    window.addEventListener('mousemove', e => handleMousemove(e))
    window.addEventListener('mouseup', () => handleMouseup())

    return () => {
      window.removeEventListener('mousemove', handleMousemove)
      window.removeEventListener('mouseup', handleMouseup)
    }
  }, [])


  const handleMousedown = (e: React.MouseEvent) => {
    setState({ isResizing: true, lastY: e.clientY })
  }

  const handleMousemove = (e: MouseEvent) => {
    if (!state.isResizing) {
      return
    }
    setBottomBarHeight(window.innerHeight - e.clientY - MAGIC_OFFSET_BASE)
  }

  const handleMouseup = () => {
    setState({ ...state, isResizing: false })
  }

  const theme = createTheme(useTheme())

  return (
    <Box
      sx={Object.assign({
        backgroundColor: theme.palette.secondary.main,
        height: theme.spacing(1),
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'space-evenly',
        cursor: 'ns-resize',
        transition: theme.transitions.create(['background-color']),
        '&:hover': {
          backgroundColor: theme.palette.primary.main
        }
      }, state.isResizing ? {
        backgroundColor: theme.palette.primary.main
      } : {})}
      onMouseDown={handleMousedown}
    >
      <Box sx={{
        width: theme.spacing(5),
        height: 1,
        backgroundColor: theme.palette.common.white
      }} />
      <Box sx={{
        width: theme.spacing(5),
        height: 1,
        backgroundColor: theme.palette.common.white
      }} />
    </Box>
  )
}

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      setBottomBarHeight: setBottomBarHeight
    },
    dispatch
  )
export default connect(null, mapDispatchToProps)(DragBar)
