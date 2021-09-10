import React, { ReactElement } from 'react'

import { Box, Theme } from '@material-ui/core'
import { Dataset } from '../types/api'
import { Link } from 'react-router-dom'
import { WithStyles, alpha, createStyles, withStyles } from '@material-ui/core/styles'
import { formatUpdatedAt } from '../helpers'
import MqText from './core/text/MqText'

const styles = (theme: Theme) => {
  return createStyles({
    link: {
      textDecoration: 'none',
      border: `2px dashed ${theme.palette.secondary.main}`,
      display: 'block',
      marginBottom: theme.spacing(2),
      borderRadius: theme.shape.borderRadius,
      transition: theme.transitions.create(['background-color']),
      '&:hover': {
        backgroundColor: alpha(theme.palette.common.white, 0.1)
      }
    }
  })
}

type IProps = WithStyles<typeof styles> &
  Pick<Dataset, 'name' | 'description' | 'updatedAt' | 'tags'> & {
    setSelectedNode: (payload: string) => void
  }

class DatasetPreviewCard extends React.Component<IProps> {
  render(): ReactElement {
    const { classes, name, description, updatedAt, setSelectedNode } = this.props
    const { link } = classes
    return (
      <Link
        className={link}
        to={{ pathname: `/datasets/${name}` }}
        onClick={() => setSelectedNode(name)}
      >
        <Box p={2}>
          <Box display='flex' justifyContent='space-between' alignItems={'center'} mb={1}>
            <MqText subheading font={'mono'}>
              {name}
            </MqText>
            <Box mt={1}>
              <MqText subdued>{formatUpdatedAt(updatedAt)}</MqText>
            </Box>
          </Box>
          <MqText subdued>
            {description || 'There is no description available for this dataset'}
          </MqText>
        </Box>
      </Link>
    )
  }
}

export default withStyles(styles)(DatasetPreviewCard)
