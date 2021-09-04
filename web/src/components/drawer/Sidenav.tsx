import React from 'react'

import { Theme } from '@material-ui/core/styles/createMuiTheme'
import createStyles from '@material-ui/core/styles/createStyles'
import withStyles, { WithStyles } from '@material-ui/core/styles/withStyles'

import { RouteComponentProps, Link as RouterLink, withRouter } from 'react-router-dom'
import Box from '@material-ui/core/Box'

import { DRAWER_WIDTH, HEADER_HEIGHT } from '../../helpers/theme'
import { Drawer } from '@material-ui/core'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faArrowRight, faCog, faDatabase } from '@fortawesome/free-solid-svg-icons'
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

type SidenavProps = WithStyles<typeof styles>

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
              <MqIconButton id={'homeDrawerButton'} title={'Jobs'} active={true}>
                <FontAwesomeIcon icon={faCog} size={'2x'} />
              </MqIconButton>
            </RouterLink>
            <RouterLink to={'/explore'} className={classes.link}>
              <MqIconButton id={'datasetsDrawerButton'} title={'Datasets'} active={false}>
                <FontAwesomeIcon icon={faDatabase} size={'2x'} />
              </MqIconButton>
            </RouterLink>
            <RouterLink to={'/lineage'} className={classes.link}>
              <MqIconButton id={'lineageDrawerButton'} title={'Lineage'} active={false}>
                <FontAwesomeIcon icon={faArrowRight} size={'2x'} />
              </MqIconButton>
            </RouterLink>
          </Box>
        </Box>
      </Drawer>
    )
  }
}

export default withStyles(styles)(Sidenav)
