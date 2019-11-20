import React, { ReactElement } from 'react'
import {
  withStyles,
  createStyles,
  WithStyles as IWithStyles,
  Theme as ITheme
} from '@material-ui/core/styles'
import { Typography, Box, Tooltip } from '@material-ui/core'
import { formatUpdatedAt } from '../helpers'
import tagToBadge from '../config/tag-to-badge'

import { IDatasetAPI } from '../types/api'
const _  = require('lodash')

const styles = ({ palette }: ITheme) => {
  return createStyles({
    rightCol: {
      textAlign: 'right'
    },
    lastUpdated: {
      color: palette.grey[600]
    },
    tagContainer: {
      display: 'flex'
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
    const { classes, name, description, updatedAt, tags = [] } = this.props
    const { tagContainer } = classes
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
          <div id='tagContainer' className={tagContainer}>
            {_.keys(tagToBadge.default).map((key: string) => {
              return (
                <div key={key}>
                  <Tooltip className="tagWrapper" title={key} placement="top">
                    {tags.includes(key) ? tagToBadge.highlighted[key] : tagToBadge.default[key]}
                  </Tooltip>
                </div>
              )
            })}
          </div>
          <Typography className={classes.lastUpdated}>{formatUpdatedAt(updatedAt)}</Typography>
        </Box>
      </Box>
    )
  }
}

export default withStyles(styles)(DatasetPreviewCard)
