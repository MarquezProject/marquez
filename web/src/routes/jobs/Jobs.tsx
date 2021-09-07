import * as Redux from 'redux'
import { IState } from '../../store/reducers'
import { Job } from '../../types/api'
import { Theme } from '@material-ui/core/styles/createMuiTheme'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { fetchJobs } from '../../store/actionCreators'
import Box from '@material-ui/core/Box'
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

class Jobs extends React.Component<JobsProps> {
  componentDidMount() {
    this.props.fetchJobs('food_delivery')
  }

  render() {
    const { jobs } = this.props
    return (
      <Box>
        {jobs.map(job => {
          return <Box key={job.name}>{job.name}</Box>
        })}
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
