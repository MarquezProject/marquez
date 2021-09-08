import * as Redux from 'redux'
import { Container, Table, TableBody, TableCell, TableHead, TableRow } from '@material-ui/core'
import { IState } from '../../store/reducers'
import { Job } from '../../types/api'
import { MqScreenLoad } from '../../components/core/screen-load/MqScreenLoad'
import { Pagination } from '@material-ui/lab'
import { Theme } from '@material-ui/core/styles/createMuiTheme'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { encodeNode } from '../../helpers/nodes'
import { fetchJobs, resetJobs } from '../../store/actionCreators'
import { formatUpdatedAt } from '../../helpers'
import { stopWatchDuration } from '../../helpers/time'
import Box from '@material-ui/core/Box'
import MqText from '../../components/core/text/MqText'
import React from 'react'
import createStyles from '@material-ui/core/styles/createStyles'
import withStyles, { WithStyles } from '@material-ui/core/styles/withStyles'

const styles = (theme: Theme) => createStyles({})

interface StateProps {
  jobs: Job[]
  isJobsLoading: boolean
}

interface DispatchProps {
  fetchJobs: typeof fetchJobs
  resetJobs: typeof resetJobs
}

type JobsProps = WithStyles<typeof styles> & StateProps & DispatchProps

const JOB_COLUMNS = ['Name', 'Namespace', 'Updated At', 'Last Runtime']

class Jobs extends React.Component<JobsProps> {
  componentDidMount() {
    this.props.fetchJobs('food_delivery')
  }

  componentWillUnmount() {
    this.props.resetJobs()
  }

  render() {
    const { jobs, isJobsLoading } = this.props
    return (
      <Container maxWidth={'lg'} disableGutters>
        <MqScreenLoad loading={isJobsLoading}>
          <>
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
                {jobs.map(job => {
                  return (
                    <TableRow key={job.name}>
                      <TableCell align='left'>
                        <MqText
                          link
                          linkTo={`/lineage/${encodeNode('JOB', job.namespace, job.name)}`}
                        >
                          {job.name}
                        </MqText>
                      </TableCell>
                      <TableCell align='left'>
                        <MqText>{job.namespace}</MqText>
                      </TableCell>
                      <TableCell align='left'>
                        <MqText>{formatUpdatedAt(job.updatedAt)}</MqText>
                      </TableCell>
                      <TableCell align='left'>
                        <MqText>
                          {job.latestRun && job.latestRun.durationMs
                            ? stopWatchDuration(job.latestRun.durationMs)
                            : 'N/A'}
                        </MqText>
                      </TableCell>
                    </TableRow>
                  )
                })}
              </TableBody>
            </Table>
            <Box display={'flex'} justifyContent={'flex-end'} mt={2} mr={2}>
              <Pagination color={'standard'} shape={'rounded'} onChange={() => {}} count={10} />
            </Box>
          </>
        </MqScreenLoad>
      </Container>
    )
  }
}

const mapStateToProps = (state: IState) => ({
  jobs: state.jobs.result,
  isJobsLoading: state.jobs.isLoading
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      fetchJobs: fetchJobs,
      resetJobs: resetJobs
    },
    dispatch
  )

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(withStyles(styles)(Jobs))
