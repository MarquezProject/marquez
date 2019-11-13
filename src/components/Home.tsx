import React, { ReactElement } from 'react'
import * as RRD from 'react-router-dom'
import { Box, Typography } from '@material-ui/core'

import {
  withStyles,
  createStyles,
  WithStyles as IWithStyles,
  Theme as ITheme
} from '@material-ui/core/styles'

import NetworkGraph from './NetworkGraph'
import CustomSearchBar from './CustomSearchBar'
import DatasetPreviewCard from './DatasetPreviewCard'
import JobPreviewCard from './JobPreviewCard'

import { IDatasetsState } from '../reducers/datasets'
import { IJobsState } from '../reducers/jobs'

import { findMatchingEntities } from '../actionCreators'

const styles = (_theme: ITheme) => {
  return createStyles({
    header: {
      padding: '0% 0% 0% 1%'
    },
    column: {
      flex: 1
    },
    row: {
      display: 'flex',
      flexDirection: 'row',
      padding: '1% 5%'
    },
    search: {
      position: 'absolute',
      bottom: '52vh',
      width: '90%',
      left: '5%'
    },
    noDatasets: {
      color: '#9e9e9e',
      position: 'absolute',
      bottom: '21vh',
      left: '21%'
    },
    noJobs: {
      color: '#9e9e9e',
      position: 'absolute',
      bottom: '21vh',
      right: '21%'
    }
  })
}

interface IProps {
  datasets: IDatasetsState
  jobs: IJobsState
  findMatchingEntities: typeof findMatchingEntities
}

interface IState {
  showJobs: boolean
}

type IAllProps = RRD.RouteComponentProps & IWithStyles<typeof styles> & IProps

class Home extends React.Component<IAllProps, IState> {
  constructor(props: IAllProps) {
    super(props)
    this.state = { showJobs: false }
  }

  showJobs = (bool: boolean) => {
    this.setState({ showJobs: bool })
  }

  render(): ReactElement {
    const { datasets, jobs, classes, findMatchingEntities } = this.props
    const matchingDatasets = datasets.filter(d => d.matches)
    const matchingJobs = jobs.filter(j => j.matches)
    return (
      <Box display='flex' flexDirection='column' justifyContent='center'>
        <NetworkGraph jobs={jobs} datasets={datasets}></NetworkGraph>
        <CustomSearchBar
          customClassName={classes.search}
          findMatchingEntities={findMatchingEntities}
          showJobs={this.showJobs}
        ></CustomSearchBar>
        <div className={classes.row}>
          <Box className={classes.column}>
            {matchingDatasets.length > 0 ? (
              <Typography className={classes.header} color='secondary' variant='h3'>
                {!this.state.showJobs ? 'Popular Datasets' : 'Matching Datasets'}
              </Typography>
            ) : (
              <Typography className={classes.noDatasets}>no datasets found!</Typography>
            )}
            {matchingDatasets.map(d => (
              <DatasetPreviewCard
                key={d.name}
                name={d.name}
                description={d.description}
                updatedAt={d.createdAt}
              />
            ))}
          </Box>
          {this.state.showJobs ? (
            <Box className={classes.column}>
              {matchingJobs.length > 0 ? (
                <Typography className={classes.header} color='secondary' variant='h3'>
                  Matching Jobs
                </Typography>
              ) : (
                <Typography className={classes.noJobs}>no jobs found!</Typography>
              )}
              {matchingJobs.map(d => (
                <JobPreviewCard
                  /* should change to unique identifier */
                  key={d.name}
                  name={d.name}
                  description={d.description}
                  updatedAt={d.createdAt}
                  status={d.status}
                />
              ))}
            </Box>
          ) : null}
        </div>
      </Box>
    )
  }
}

export default withStyles(styles)(Home)
