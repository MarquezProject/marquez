// SPDX-License-Identifier: Apache-2.0

import React from 'react'
import SVG from 'react-inlinesvg'

import createStyles from '@material-ui/core/styles/createStyles'
import withStyles, { WithStyles } from '@material-ui/core/styles/withStyles'

import { RouteComponentProps, Link as RouterLink, withRouter } from 'react-router-dom'
import Box from '@material-ui/core/Box'

import { DRAWER_WIDTH, HEADER_HEIGHT } from '../../helpers/theme'
import { Drawer, Theme } from '@material-ui/core'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCogs, faDatabase } from '@fortawesome/free-solid-svg-icons'
import MqIconButton from '../core/icon-button/MqIconButton'

const styles = (theme: Theme) =>
  createStyles({
    drawer: {
      marginTop: HEADER_HEIGHT,
      width: DRAWER_WIDTH,
      flexShrink: 0,
      whiteSpace: 'nowrap',
      '& > :first-child': {
        borderRight: 'none'
      }
    },
    background: {
      backgroundColor: theme.palette.background.default,
      borderRight: `2px dashed ${theme.palette.secondary.main}`
    },
    link: {
      textDecoration: 'none'
    }
  })

type SidenavProps = WithStyles<typeof styles> & RouteComponentProps

class Sidenav extends React.Component<SidenavProps> {
  render() {
    const { classes } = this.props
    return (
      <Drawer className={classes.drawer} variant='permanent'>
        <Box
          width={DRAWER_WIDTH}
          display={'flex'}
          flexDirection={'column'}
          justifyContent={'space-between'}
          height={'100%'}
          pt={2}
          pb={2}
          className={classes.background}
        >
          <Box ml={2}>
            <RouterLink to={'/'} className={classes.link}>
              <MqIconButton
                id={'homeDrawerButton'}
                title={'JOBS'}
                active={this.props.location.pathname === '/'}
              >
                <FontAwesomeIcon icon={faCogs} size={'2x'} />
              </MqIconButton>
            </RouterLink>
            <RouterLink to={'/datasets'} className={classes.link}>
              <MqIconButton
                id={'datasetsDrawerButton'}
                title={'DATASETS'}
                active={this.props.location.pathname === '/datasets'}
              >
                <FontAwesomeIcon icon={faDatabase} size={'2x'} />
              </MqIconButton>
            </RouterLink>
            <RouterLink to={'/events'} className={classes.link}>
              <MqIconButton
                id={'eventsButton'}
                title={'EVENTS'}
                active={this.props.location.pathname === '/events'}
              >
                <SVG
                  src="https://raw.githubusercontent.com/MarquezProject/marquez/main/web/src/img/iconSearchArrow.svg"
                  width={'30px'}
                />
              </MqIconButton>
            </RouterLink>

            {/* todo remove this link for now until direct linking available */}
            {/*<RouterLink to={'/lineage'} className={classes.link}>*/}
            {/*  <MqIconButton*/}
            {/*    id={'lineageDrawerButton'}*/}
            {/*    title={'Lineage'}*/}
            {/*    active={this.props.location.pathname.startsWith('/lineage')}*/}
            {/*  >*/}
            {/*    <FontAwesomeIcon icon={faArrowRight} size={'2x'} />*/}
            {/*  </MqIconButton>*/}
            {/*</RouterLink>*/}
          </Box>
        </Box>
      </Drawer>
    )
  }
}

export default withStyles(styles)(withRouter(Sidenav))
