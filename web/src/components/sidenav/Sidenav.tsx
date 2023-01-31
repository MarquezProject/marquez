// Copyright 2018-2023 contributors to the Marquez project
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

// for i18n
import '../../i18n/config'
import { FormControl, MenuItem, Select } from '@material-ui/core'
import { MqInputNoIcon } from '../core/input-base/MqInputBase'

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
    },
    formControl: {
      maxWidth: '100px'
    }
  })

type SidenavProps = WithStyles<typeof styles> & RouteComponentProps

class Sidenav extends React.Component<SidenavProps> {
  render() {
    const { classes } = this.props
    const i18next = require('i18next')
    const changeLanguage = (lng: string) => {
      i18next.changeLanguage(lng)
    }
    return (
      <Drawer className={classes.drawer} variant='permanent'>
        <Box
          position={'relative'}
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
                title={i18next.t('sidenav.jobs')}
                active={this.props.location.pathname === '/'}
              >
                <FontAwesomeIcon icon={faCogs} size={'2x'} />
              </MqIconButton>
            </RouterLink>
            <RouterLink to={'/datasets'} className={classes.link}>
              <MqIconButton
                id={'datasetsDrawerButton'}
                title={i18next.t('sidenav.datasets')}
                active={this.props.location.pathname === '/datasets'}
              >
                <FontAwesomeIcon icon={faDatabase} size={'2x'} />
              </MqIconButton>
            </RouterLink>
            <RouterLink to={'/events'} className={classes.link}>
              <MqIconButton
                id={'eventsButton'}
                title={i18next.t('sidenav.events')}
                active={this.props.location.pathname === '/events'}
              >
                <SVG
                  src='https://raw.githubusercontent.com/MarquezProject/marquez/main/web/src/img/iconSearchArrow.svg'
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
          <FormControl variant='outlined' className={classes.formControl}>
            <Box px={1}>
              <Select
                fullWidth
                value={i18next.resolvedLanguage}
                onChange={event => {
                  changeLanguage(event.target.value as string)
                  window.location.reload()
                }}
                input={<MqInputNoIcon />}
              >
                <MenuItem key={'en'} value={'en'}>
                  {'en'}
                </MenuItem>
                <MenuItem key={'es'} value={'es'}>
                  {'es'}
                </MenuItem>
                <MenuItem key={'fr'} value={'fr'}>
                  {'fr'}
                </MenuItem>
                <MenuItem key={'pl'} value={'pl'}>
                  {'pl'}
                </MenuItem>
              </Select>
            </Box>
          </FormControl>
        </Box>
      </Drawer>
    )
  }
}

export default withStyles(styles)(withRouter(Sidenav))
