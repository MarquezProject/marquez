import React, { ReactElement } from 'react'
import {
  withStyles,
  createStyles,
  WithStyles as IWithStyles,
  Theme as ITheme
} from '@material-ui/core/styles'
import { Typography, Box } from '@material-ui/core'
const globalStyles = require('../global_styles.css')
const { jobNodeGrey, datasetNodeWhite } = globalStyles

const styles = ({ spacing }: ITheme) => {
  return createStyles({
    datasetShape: {
      backgroundColor: datasetNodeWhite
    },
    jobShape: {
      borderRadius: '50%',
      backgroundColor: jobNodeGrey
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
