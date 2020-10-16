import * as React from 'react'
import { WithStyles as IWithStyles, createStyles, withStyles } from '@material-ui/core/styles'
import { styled } from '@material-ui/core/styles'
import CircularProgress from '@material-ui/core/CircularProgress'

interface IProps {}

const styles = () => {
  return createStyles({
    container: {
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      width: '100%'
    }
  })
}

const StyledProgress = styled(CircularProgress)({
  margin: '0 auto',
  position: 'absolute'
})

const CustomLoader = (props: IWithStyles<typeof styles> & IProps) => {
  const { classes } = props
  return (
    <div className={classes.container}>
      <StyledProgress color='secondary' />
      <StyledProgress color='secondary' size={80} />
    </div>
  )
}

export default withStyles(styles)(CustomLoader)
