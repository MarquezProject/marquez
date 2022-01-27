// SPDX-License-Identifier: Apache-2.0

import * as Redux from 'redux'
import { Container, Table, TableBody, TableCell, TableHead, TableRow } from '@material-ui/core'
import { IState } from '../../store/reducers'
import { Job } from '../../types/api'
import { MqScreenLoad } from '../../components/core/screen-load/MqScreenLoad'
import { Nullable } from '../../types/util/Nullable'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { encodeNode } from '../../helpers/nodes'
import { fetchJobs, resetJobs } from '../../store/actionCreators'
import { formatUpdatedAt } from '../../helpers'
import { stopWatchDuration } from '../../helpers/time'
import Box from '@material-ui/core/Box'
import MqEmpty from '../../components/core/empty/MqEmpty'
import MqText from '../../components/core/text/MqText'
import React from 'react'

interface StateProps {
  jobs: Job[]
  isJobsInit: boolean
  isJobsLoading: boolean
  selectedNamespace: Nullable<string>
}

interface DispatchProps {
  fetchJobs: typeof fetchJobs
  resetJobs: typeof resetJobs
}

type JobsProps = StateProps & DispatchProps

const JOB_COLUMNS = ['Name', 'Namespace', 'Updated At', 'Last Runtime']

class Jobs extends React.Component<JobsProps> {
  componentDidMount() {
    if (this.props.selectedNamespace) {
      this.props.fetchJobs(this.props.selectedNamespace)
    }
  }

  componentDidUpdate(prevProps: Readonly<JobsProps>) {
    if (
      prevProps.selectedNamespace !== this.props.selectedNamespace &&
      this.props.selectedNamespace
    ) {
      this.props.fetchJobs(this.props.selectedNamespace)
    }
  }

  componentWillUnmount() {
    this.props.resetJobs()
  }

  render() {
    const { jobs, isJobsLoading, isJobsInit } = this.props
    return (
      <Container maxWidth={'lg'} disableGutters>
        <MqScreenLoad loading={isJobsLoading || !isJobsInit}>
          <>
            {jobs.length === 0 ? (
              <Box p={2}>
                <MqEmpty title={'No jobs found'}>
                  <MqText subdued>
                    Try changing namespaces or consulting our documentation to add jobs.
                  </MqText>
                </MqEmpty>
              </Box>
            ) : (
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
              </>
            )}
          </>
        </MqScreenLoad>
      </Container>
    )
  }
}

const mapStateToProps = (state: IState) => ({
  isJobsInit: state.jobs.init,
  jobs: state.jobs.result,
  isJobsLoading: state.jobs.isLoading,
  selectedNamespace: state.namespaces.selectedNamespace
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
)(Jobs)
