import React, { FunctionComponent, useState } from 'react'
import * as RRD from 'react-router-dom'
import { Box, Typography } from '@material-ui/core'

import Pagination from 'material-ui-flat-pagination'
import _chunk from 'lodash/chunk'

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
    filter: {
      marginLeft: '-4%'
    },
    lowerHalf: {
      display: 'flex',
      flexDirection: 'column',
      padding: '50vh 5% 1%',
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

type IAllProps = RRD.RouteComponentProps & IWithStyles<typeof styles> & IProps

const Home:  FunctionComponent<IAllProps> = props => {
  const [datasetPageIndex, setDatasetPageIndex] = useState(0)
  const [jobPageIndex, setJobPageIndex] = useState(0)

  const limit = 5

  const { datasets, jobs, classes, showJobs, setShowJobs } = props

  const matchingDatasets = datasets.filter(d => d.matches)
  const matchingJobs = jobs.filter(j => j.matches)

  const chunkedDatasets = _chunk(matchingDatasets, limit)
  const chunkedJobs = _chunk(matchingJobs, limit)

  const displayDatasets = chunkedDatasets[datasetPageIndex] || matchingDatasets
  const displayJobs = chunkedJobs[jobPageIndex] || matchingJobs

  return (
    <div className={classes.lowerHalf}>
      <div className={classes.filter}>
        <FilterContainer showJobs={setShowJobs} />
      </div>
      <div className={classes.row}>
        <Box className={classes.column}>
          {matchingDatasets.length > 0 ? (
            <Typography className={classes.header} color='secondary' variant='h3'>
              {!showJobs ? 'Popular Datasets' : 'Matching Datasets'}
            </Typography>
          ) : (
            <Typography className={classes.noDatasets}>no datasets found!</Typography>
          )}
          {displayDatasets.map(d => (
            <DatasetPreviewCard
              key={d.name}
              name={d.name}
              description={d.description}
              updatedAt={d.createdAt}
            />
          ))}
          {matchingDatasets.length > 0 ? (
          <Pagination
            limit={limit}
            offset={datasetPageIndex * limit}
            total={matchingDatasets.length}
            onClick={(e, offset, page) => setDatasetPageIndex(page - 1)}
          />
          ) : null}
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
            {displayJobs.map(d => (
              <JobPreviewCard
                key={d.name}
                name={d.name}
                description={d.description}
                updatedAt={d.createdAt}
                latestRun={d.latestRun}
              />
            ))}
            {matchingJobs.length > 0 ? (
              <Pagination
                limit={limit}
                offset={jobPageIndex * limit}
                total={matchingJobs.length}
                onClick={(e, offset, page) => setJobPageIndex(page - 1)}
              />
            ) : null}
          </Box>
        ) : null}
      </div>
    </div>
  )
}

export default withStyles(styles)(Home)
