import * as React from 'react'
import CircularProgress from '@material-ui/core/CircularProgress'
import { styled } from '@material-ui/core/styles'
import { withStyles, createStyles, WithStyles as IWithStyles } from '@material-ui/core/styles'

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
