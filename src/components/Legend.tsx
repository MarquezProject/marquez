import { Box, Typography } from '@material-ui/core'
import {
  Theme as ITheme,
  WithStyles as IWithStyles,
  createStyles,
  withStyles
} from '@material-ui/core/styles'
import React, { ReactElement } from 'react'
const globalStyles = require('../global_styles.css')
const { datasetNodeWhite } = globalStyles

const styles = ({ spacing }: ITheme) => {
  return createStyles({
    datasetShape: {
      backgroundColor: datasetNodeWhite
    },
    jobShape: {
      borderRadius: '50%',
      backgroundColor: datasetNodeWhite
    },
    shape: {
      width: spacing(2),
      height: spacing(2),
      margin: '4px 6px 0px 6px'
    },
    text: {
      color: '#f2f2f2'
    }
  })
}

interface IProps {
  customClassName: string
}

type AllProps = IWithStyles<typeof styles> & IProps
interface IState {}

class Legend extends React.Component<AllProps, IState> {
  render(): ReactElement {
    const { classes, customClassName } = this.props
    return (
      <Box className={customClassName} display='flex'>
        <div className={`${classes.datasetShape} ${classes.shape}`} />
        <Typography className={classes.text}>datasets</Typography>
        <div className={`${classes.jobShape} ${classes.shape}`} />
        <Typography className={classes.text}>jobs</Typography>
      </Box>
    )
  }
}

export default withStyles(styles)(Legend)
