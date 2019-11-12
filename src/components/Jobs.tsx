import React, { ReactElement } from 'react'
import * as RRD from 'react-router-dom'
import { Typography, Box } from '@material-ui/core'

interface IProps {}

interface IState {}

type IAllProps = RRD.RouteComponentProps & IProps

class Jobs extends React.Component<IAllProps, IState> {
  render(): ReactElement {
    return (
      <Box mt={8}>
        <Typography color='primary'> Jobs Component </Typography>
      </Box>
    )
  }
}

export default Jobs
