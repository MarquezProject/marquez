// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import React from 'react'
import SVG from 'react-inlinesvg'

import { Link as RouterLink, useLocation } from 'react-router-dom'
import Box from '@mui/material/Box'

import { DRAWER_WIDTH, HEADER_HEIGHT } from '../../helpers/theme'
import { Drawer, createTheme } from '@mui/material'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCogs, faDatabase } from '@fortawesome/free-solid-svg-icons'
import MqIconButton from '../core/icon-button/MqIconButton'

// for i18n
import '../../i18n/config'
import { FormControl, MenuItem, Select } from '@mui/material'
import { MqInputNoIcon } from '../core/input-base/MqInputBase'
import { useTheme } from '@emotion/react'

interface SidenavProps {}

const Sidenav: React.FC<SidenavProps> = () => {
  const i18next = require('i18next')
  const changeLanguage = (lng: string) => {
    i18next.changeLanguage(lng)
  }
  const theme = createTheme(useTheme())

  const location = useLocation()

  return (
    <Drawer
      sx={{
        marginTop: `${HEADER_HEIGHT}px`,
        width: `${DRAWER_WIDTH}px`,
        flexShrink: 0,
        whiteSpace: 'nowrap',
        '& > :first-of-type': {
          borderRight: 'none',
        },
      }}
      variant='permanent'
    >
      <Box
        position={'relative'}
        width={DRAWER_WIDTH}
        display={'flex'}
        flexDirection={'column'}
        justifyContent={'space-between'}
        height={'100%'}
        pt={2}
        pb={2}
        sx={{
          borderRight: `2px dashed ${theme.palette.secondary.main}`,
        }}
      >
        <Box ml={2}>
          <RouterLink
            to={'/'}
            style={{
              textDecoration: 'none',
            }}
          >
            <MqIconButton
              id={'homeDrawerButton'}
              title={i18next.t('sidenav.jobs')}
              active={location.pathname === '/'}
            >
              <FontAwesomeIcon icon={faCogs} size={'2x'} />
            </MqIconButton>
          </RouterLink>
          <RouterLink
            to={'/datasets'}
            style={{
              textDecoration: 'none',
            }}
          >
            <MqIconButton
              id={'datasetsDrawerButton'}
              title={i18next.t('sidenav.datasets')}
              active={location.pathname === '/datasets'}
            >
              <FontAwesomeIcon icon={faDatabase} size={'2x'} />
            </MqIconButton>
          </RouterLink>
          <RouterLink
            to={'/events'}
            style={{
              textDecoration: 'none',
            }}
          >
            <MqIconButton
              id={'eventsButton'}
              title={i18next.t('sidenav.events')}
              active={location.pathname === '/events'}
            >
              <SVG
                src='https://raw.githubusercontent.com/MarquezProject/marquez/main/web/src/img/iconSearchArrow.svg'
                width={'30px'}
              />
            </MqIconButton>
          </RouterLink>

          {/* todo remove this link for now until direct linking available */}
          {/*<RouterLink to={'/lineage'} sx={{
              textDecoration: 'none'
            }}>*/}
          {/*  <MqIconButton*/}
          {/*    id={'lineageDrawerButton'}*/}
          {/*    title={'Lineage'}*/}
          {/*    active={this.props.location.pathname.startsWith('/lineage')}*/}
          {/*  >*/}
          {/*    <FontAwesomeIcon icon={faArrowRight} size={'2x'} />*/}
          {/*  </MqIconButton>*/}
          {/*</RouterLink>*/}
        </Box>
        <FormControl
          variant='outlined'
          sx={{
            maxWidth: '100px',
          }}
        >
          <Box px={1}>
            <Select
              fullWidth
              value={i18next.resolvedLanguage}
              onChange={(event) => {
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

export default Sidenav
