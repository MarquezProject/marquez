import React, { ReactElement } from 'react'
import {
  withStyles,
  createStyles,
  WithStyles as IWithStyles,
  Theme as ITheme
} from '@material-ui/core/styles'
import { Typography, Box } from '@material-ui/core'
import { formatUpdatedAt } from '../helpers'
import tagToBadge from '../config/tag-to-badge'

import { IDatasetAPI } from '../types/api'

const styles = ({ palette }: ITheme) => {
  return createStyles({
    rightCol: {
      textAlign: 'right'
    },
    lastUpdated: {
      color: palette.grey[600]
    }
  })
}

const StyledTypography = withStyles({
  root: {
    maxWidth: '90%'
  }
})(Typography)

type IProps = IWithStyles<typeof styles> &
  Pick<IDatasetAPI, 'name' | 'description' | 'updatedAt' | 'tags'>
interface IState {}

class DatasetPreviewCard extends React.Component<IProps, IState> {
  render(): ReactElement {
    const { classes, name, description, updatedAt = 'error', tags = ['is_pii'] } = this.props
    return (
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
          <div id='tagContainer'>{tags.map(t => tagToBadge[t])}</div>
          <Typography className={classes.lastUpdated}>{formatUpdatedAt(updatedAt)}</Typography>
        </Box>
      </Box>
    )
  }
}

export default withStyles(styles)(DatasetPreviewCard)
