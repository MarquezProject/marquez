import React, { ReactElement } from 'react'
import * as RRD from 'react-router-dom'
import { Typography, Box } from '@material-ui/core'

interface IProps {}

interface IState {}

type IAllProps = RRD.RouteComponentProps & IProps

class Datasets extends React.Component<IAllProps, IState> {
  render(): ReactElement {
    return (
      <Box mt={10}>
        <Typography color='primary' align='center'>
          {' '}
          Datasets Component{' '}
        </Typography>
      </Box>
    )
  }
}

export default Datasets
