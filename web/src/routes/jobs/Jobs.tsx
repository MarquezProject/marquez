import * as Redux from 'redux'
import { IState } from '../../store/reducers'
import { Job } from '../../types/api'
import { Pagination } from '@material-ui/lab'
import { Table, TableBody, TableCell, TableHead, TableRow } from '@material-ui/core'
import { Theme } from '@material-ui/core/styles/createMuiTheme'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { fetchJobs } from '../../store/actionCreators'
import { formatUpdatedAt } from '../../helpers'
import Box from '@material-ui/core/Box'
import MqText from '../../components/core/text/MqText'
import React from 'react'
import createStyles from '@material-ui/core/styles/createStyles'
import withStyles, { WithStyles } from '@material-ui/core/styles/withStyles'

const styles = (theme: Theme) => createStyles({})

interface StateProps {
  jobs: Job[]
}

interface DispatchProps {
  fetchJobs: typeof fetchJobs
}

type JobsProps = WithStyles<typeof styles> & StateProps & DispatchProps

const JOB_COLUMNS = ['Name', 'Namespace', 'Updated At']

class Jobs extends React.Component<JobsProps> {
  componentDidMount() {
    this.props.fetchJobs('food_delivery')
  }

  render() {
    const { jobs } = this.props
    return (
      <Box>
        <Box p={2}>
          <MqText heading>Jobs</MqText>
        </Box>
        <Table size='small'>
          <TableHead>
            <TableRow>
              {JOB_COLUMNS.map(field => {
                return (
                  <TableCell key={field} align='left'>
                    <MqText subheading>{field}</MqText>
                  </TableCell>
                )
              })}
            </TableRow>
          </TableHead>
          <TableBody>
            {jobs.map(jobs => {
              return (
                <TableRow key={jobs.name}>
                  <TableCell align='left'>
                    <MqText>{jobs.name}</MqText>
                  </TableCell>
                  <TableCell align='left'>
                    <MqText>{jobs.namespace}</MqText>
                  </TableCell>
                  <TableCell align='left'>
                    <MqText>{formatUpdatedAt(jobs.updatedAt)}</MqText>
                  </TableCell>
                </TableRow>
              )
            })}
          </TableBody>
        </Table>
        <Box display={'flex'} justifyContent={'flex-end'} mt={2} mr={2}>
          <Pagination color={'standard'} shape={'rounded'} onChange={() => {}} count={10} />
        </Box>
      </Box>
    )
  }
}

const mapStateToProps = (state: IState) => ({
  jobs: state.jobs
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      fetchJobs: fetchJobs
    },
    dispatch
  )

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(withStyles(styles)(Jobs))
