// SPDX-License-Identifier: Apache-2.0

import React from 'react'

import * as Redux from 'redux'
import { Theme } from '@material-ui/core/styles'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { setBottomBarHeight } from '../../../../store/actionCreators'
import Box from '@material-ui/core/Box'
import classNames from 'classnames'
import createStyles from '@material-ui/core/styles/createStyles'
import withStyles, { WithStyles } from '@material-ui/core/styles/withStyles'

const styles = (theme: Theme) =>
  createStyles({
    dragBarContainer: {
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
    },
    resizing: {
      backgroundColor: theme.palette.primary.main
    },
    slit: {
      width: theme.spacing(5),
      height: 1,
      backgroundColor: theme.palette.common.white
    }
  })

interface DispatchProps {
  setBottomBarHeight: (offset: number) => void
}

interface DragBarState {
  isResizing: boolean
  lastY: number
}

type DragBarProps = WithStyles<typeof styles> & DispatchProps

// height of bar / 2
const MAGIC_OFFSET_BASE = 4

class DragBar extends React.Component<DragBarProps, DragBarState> {
  constructor(props: DragBarProps) {
    super(props)
    this.state = {
      isResizing: false,
      lastY: 0
    }
  }

  componentDidMount() {
    window.addEventListener('mousemove', e => this.handleMousemove(e))
    window.addEventListener('mouseup', () => this.handleMouseup())
  }

  componentWillUnmount() {
    window.removeEventListener('mousemove', this.handleMousemove)
    window.removeEventListener('mouseup', this.handleMouseup)
  }

  handleMousedown = (e: React.MouseEvent) => {
    this.setState({ isResizing: true, lastY: e.clientY })
  }

  handleMousemove = (e: MouseEvent) => {
    if (!this.state.isResizing) {
      return
    }
    this.props.setBottomBarHeight(window.innerHeight - e.clientY - MAGIC_OFFSET_BASE)
  }

  handleMouseup = () => {
    this.setState({ isResizing: false })
  }

  render() {
    const { classes } = this.props
    return (
      <Box
        className={classNames(classes.dragBarContainer, this.state.isResizing && classes.resizing)}
        onMouseDown={this.handleMousedown}
      >
        <Box className={classes.slit} />
        <Box className={classes.slit} />
      </Box>
    )
  }
}

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      setBottomBarHeight: setBottomBarHeight
    },
    dispatch
  )
export default connect(
  null,
  mapDispatchToProps
)(withStyles(styles)(DragBar))
