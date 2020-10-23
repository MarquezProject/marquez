import React, { ReactElement } from 'react'

import { Box } from '@material-ui/core'
import {
  Theme as ITheme,
  WithStyles as IWithStyles,
  createStyles,
  withStyles
} from '@material-ui/core/styles'
import MqText from './core/text/MqText'

const styles = ({ spacing, palette }: ITheme) => {
  return createStyles({
    datasetShape: {
      backgroundColor: palette.common.white
    },
    jobShape: {
      borderRadius: '50%',
      backgroundColor: palette.common.white
    },
    shape: {
      width: spacing(2),
      height: spacing(2),
      marginRight: spacing(1),
      marginLeft: spacing(1)
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
      <Box className={customClassName} display='flex' alignItems={'center'}>
        <div className={`${classes.datasetShape} ${classes.shape}`} />
        <MqText font={'mono'}>datasets</MqText>
        <div className={`${classes.jobShape} ${classes.shape}`} />
        <MqText font={'mono'}>jobs</MqText>
      </Box>
    )
  }
}

export default withStyles(styles)(Legend)
