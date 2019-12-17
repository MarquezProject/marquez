import React, { ReactElement } from 'react'
import * as RRD from 'react-router-dom'
import { Box, Typography } from '@material-ui/core'

import {
  withStyles,
  createStyles,
  WithStyles as IWithStyles,
  Theme as ITheme
} from '@material-ui/core/styles'

import FilterContainer from '../containers/FilterContainer'
import DatasetPreviewCard from './DatasetPreviewCard'
import JobPreviewCard from './JobPreviewCard'

import { IDatasetsState } from '../reducers/datasets'
import { IJobsState } from '../reducers/jobs'

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
      flexDirection: 'row'
    },
    lowerHalf: {
      display: 'flex',
      flexDirection: 'column',
      padding: '52vh 5% 1%',
      position: 'absolute',
      top: 0,
      zIndex: 1,
      width: '100%'
    },
    noDatasets: {
      color: '#9e9e9e',
      position: 'fixed',
      bottom: '20vh',
      left: '21%'
    },
    noJobs: {
      color: '#9e9e9e',
      position: 'fixed',
      bottom: '20vh',
      right: '21%'
    }
  })
}

interface IProps {
  datasets: IDatasetsState
  jobs: IJobsState
  showJobs: boolean
  setShowJobs: (bool: boolean) => void
}

interface IState {}

type IAllProps = RRD.RouteComponentProps & IWithStyles<typeof styles> & IProps

class Home extends React.Component<IAllProps, IState> {
  render(): ReactElement {
    const { datasets, jobs, classes, showJobs, setShowJobs } = this.props
    const matchingDatasets = datasets.filter(d => d.matches)
    const matchingJobs = jobs.filter(j => j.matches)
    return (
      <div>
        <Box justifyContent='center' className={classes.lowerHalf}>
          <FilterContainer showJobs={setShowJobs} />
          <div className={classes.row}>
            <Box className={classes.column}>
              {matchingDatasets.length > 0 ? (
                <Typography className={classes.header} color='secondary' variant='h3'>
                  {!showJobs ? 'Popular Datasets' : 'Matching Datasets'}
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
            {showJobs ? (
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
      </div>
    )
  }
}

export default withStyles(styles)(Home)
