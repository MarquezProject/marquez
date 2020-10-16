import React, { ReactElement } from 'react'

import {
  Link
} from 'react-router-dom'

import { Box, Typography } from '@material-ui/core'
import {
  Theme as ITheme,
  WithStyles as IWithStyles,
  createStyles,
  withStyles
} from '@material-ui/core/styles'
import { formatUpdatedAt } from '../helpers'

import { Dataset } from '../types/api'
const _  = require('lodash')

const styles = ({ palette }: ITheme) => {
  return createStyles({
    rightCol: {
      textAlign: 'right'
    },
    lastUpdated: {
      color: palette.grey[600]
    },
    link: {
      textDecoration: 'none'
    }
  })
}

const StyledTypography = withStyles({
  root: {
    maxWidth: '90%'
  }
})(Typography)

type IProps = IWithStyles<typeof styles> &
  Pick<Dataset, 'name' | 'description' | 'updatedAt' | 'tags'>
interface IState {}

class DatasetPreviewCard extends React.Component<IProps, IState> {
  render(): ReactElement {
    const { classes, name, description, updatedAt } = this.props
    const { link } = classes
    return (
      <Link className={link} to={{pathname: `/datasets/${name}`}}>
        <Box p={2} m={1} bgcolor='white' boxShadow={3} display='flex' justifyContent='space-between'>
          <div>
            <Typography color='secondary' variant='h3'>
              {name}
            </Typography>
            <StyledTypography color='primary'>{description}</StyledTypography>
          </div>
          <Box
            className={classes.rightCol}
            display='flex'
            flexDirection='column'
            alignItems='flex-end'
            justifyContent='space-between'
          >
            <Typography className={classes.lastUpdated}>{formatUpdatedAt(updatedAt)}</Typography>
          </Box>
        </Box>
      </Link>
    )
  }
}

export default withStyles(styles)(DatasetPreviewCard)
