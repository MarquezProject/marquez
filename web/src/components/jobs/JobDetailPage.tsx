import React, { ChangeEvent, FunctionComponent, SetStateAction, useEffect } from 'react'

import * as Redux from 'redux'
import { Box, Button, Tab, Tabs } from '@material-ui/core'
import { IJob } from '../../types'
import { IState } from '../../store/reducers'
import {
  Theme as ITheme,
  WithStyles as IWithStyles,
  createStyles,
  withStyles
} from '@material-ui/core/styles'
import { LineageJob } from '../lineage/types'
import { Run } from '../../types/api'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { fetchRuns, resetRuns } from '../../store/actionCreators'
import { formatUpdatedAt } from '../../helpers'
import { useHistory, useParams } from 'react-router-dom'
import CloseIcon from '@material-ui/icons/Close'
import IconButton from '@material-ui/core/IconButton'
import MqCode from '../core/code/MqCode'
import MqText from '../core/text/MqText'
import Runs from './Runs'

const styles = ({ palette, spacing }: ITheme) => {
  return createStyles({
    root: {
      padding: spacing(2)
    },
    squareShape: {
      width: spacing(2),
      height: spacing(2),
      marginLeft: '5px',
      borderRadius: '50%'
    },
    lastUpdated: {
      color: palette.grey[600],
      padding: '0px 0px 5px 5px'
    },
    latestRunContainer: {
      float: 'right',
      display: 'flex'
    },
    copyToClipboard: {
      position: 'absolute',
      bottom: '1rem',
      right: '1rem',
      cursor: 'pointer'
    }
  })
}

interface DispatchProps {
  fetchRuns: typeof fetchRuns
  resetRuns: typeof resetRuns
}

type IProps = IWithStyles<typeof styles> & { job: LineageJob; runs: Run[] } & DispatchProps

const JobDetailPage: FunctionComponent<IProps> = props => {
  const { job, classes, fetchRuns, resetRuns, runs } = props

  const { jobName } = useParams()
  const history = useHistory()

  const [tab, setTab] = React.useState(0)
  const handleChange = (event: ChangeEvent, newValue: SetStateAction<number>) => {
    setTab(newValue)
  }

  useEffect(() => {
    fetchRuns(job.name, job.namespace)
  }, [job.name])

  // unmounting
  useEffect(() => {
    return () => {
      resetRuns()
    }
  }, [])

  if (!job) {
    return (
      <Box
        p={2}
        display='flex'
        flexDirection='column'
        justifyContent='space-between'
        className={classes.root}
      >
        <MqText>
          No job by the name of <MqText bold>{`"${jobName}"`}</MqText> found
        </MqText>
      </Box>
    )
  }

  const { root } = classes

  const { name, description, updatedAt = '', location, context = { sql: '' } } = job as IJob

  return (
    <Box
      p={4}
      display='flex'
      flexDirection='column'
      justifyContent='space-between'
      className={root}
    >
      <Box mb={2} display={'flex'} justifyContent={'space-between'} alignItems={'center'}>
        <Tabs value={tab} onChange={handleChange} textColor='primary' indicatorColor='primary'>
          <Tab label='Current Run' disableRipple={true} />
          <Tab label='Previous Runs' disableRipple={true} />
        </Tabs>
        <Box display={'flex'} alignItems={'center'}>
          <Box mr={1}>
            <Button variant='outlined' color='primary' target={'_blank'} href={location}>
              Location
            </Button>
          </Box>
          <IconButton onClick={() => history.push('/')}>
            <CloseIcon />
          </IconButton>
        </Box>
      </Box>
      <MqText font={'mono'} heading>
        {name}
      </MqText>
      <Box mt={1} mb={2}>
        <MqText subdued>{description}</MqText>
      </Box>
      {tab === 0 && (
        <>
          <MqCode code={context.sql} />
          <Box display={'flex'} justifyContent={'flex-end'} alignItems={'center'} mt={1}>
            <Box ml={1}>
              <MqText subdued>{formatUpdatedAt(updatedAt)}</MqText>
            </Box>
          </Box>
        </>
      )}
      {tab === 1 && <Runs runs={runs} />}
    </Box>
  )
}

const mapStateToProps = (state: IState) => ({
  runs: state.runs.result,
  runsLoading: state.runs.isLoading
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      fetchRuns: fetchRuns,
      resetRuns: resetRuns
    },
    dispatch
  )

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(withStyles(styles)(JobDetailPage))
