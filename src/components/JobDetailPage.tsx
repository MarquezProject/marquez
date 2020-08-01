import React, { FunctionComponent, useState, useEffect } from 'react'
import {
  withStyles,
  createStyles,
  WithStyles as IWithStyles,
  Theme as ITheme
} from '@material-ui/core/styles'
import { Typography, Box, Fab, Tooltip } from '@material-ui/core'
import CloseIcon from '@material-ui/icons/Close'
import OpenWithSharpIcon from '@material-ui/icons/OpenWithSharp'
import Modal from '@material-ui/core/Modal'
import HowToRegIcon from '@material-ui/icons/HowToReg'
import { useParams, useHistory } from 'react-router-dom'
import _find from 'lodash/find'

const globalStyles = require('../global_styles.css')
const { jobRunNew, jobRunFailed, jobRunCompleted, jobRunAborted, jobRunRunning } = globalStyles
import { formatUpdatedAt } from '../helpers'

import { IJob } from '../types'

const colorMap = {
  NEW: jobRunNew,
  FAILED: jobRunFailed,
  COMPLETED: jobRunCompleted,
  ABORTED: jobRunAborted,
  RUNNING: jobRunRunning
}

const styles = ({ palette, spacing, shadows }: ITheme) => {
  return createStyles({
    root: {
      marginTop: '52vh',
      height: '48vh',
      padding: '0 6% 1%',
    },
    topSection: {
      display: 'grid',
      gridTemplateColumns: '40px 3fr 1fr',
      gridTemplateRows: '1fr 1fr',
      /* eslint-disable @typescript-eslint/quotes */
      gridTemplateAreas: `'status name owner-icon' '. description owner'`,
      alignItems: 'center',
      margin: '0px 6% 0px 0px'
    },
    _status: {
      gridArea: 'status',
      width: spacing(2),
      height: spacing(2),
      borderRadius: '50%'
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
    failed: {
      backgroundColor: jobRunFailed
    },
    passed: {
      backgroundColor: jobRunCompleted
    },
    _name: {
      gridArea: 'name'
    },
    _description: {
      gridArea: 'description'
    },
    _owner: {
      gridArea: 'owner',
      justifySelf: 'end'
    },
    _ownerIcon: {
      gridArea: 'owner-icon',
      justifySelf: 'end'
    },
    _SQL: {
      overflow: 'hidden'
    },
    _SQLComment: {
      color: palette.grey[400]
    },
    SQLModalContainer: {
      width: '80%',
      height: '80%',
      position: 'relative',
      margin: '10vh 10%'
    },
    SQLModal: {
      backgroundColor: 'white',
      width: '100%',
      height: '100%',
      overflow: 'scroll',
      boxShadow: shadows[1],
      // using border to create effect of padding, which will not work when there's overflow
      border: '1rem solid white',
      borderTop: 'none',
      borderLeft: '2rem solid white'
    },
    SQLModalTitle: {
      backgroundColor: 'white',
      paddingTop: '1rem',
      fontSize: '2rem',
      fontWeight: 700,
      position: 'fixed',
      width: '80%',
      right: '10%'
    },
    copyToClipboard: {
      position: 'absolute',
      bottom: '1rem',
      right: '1rem',
      cursor: 'pointer'
    },
    closeButton: {
      color: '#7D7D7D',
      backgroundColor: '#ffffff',
      position: "absolute",
      right: '6%',
      marginTop: '12px'
    }
  })
}

type IProps = IWithStyles<typeof styles> & { jobs: IJob[] } & { fetchJobRuns: any}

const StyledTypography = withStyles({
  root: {
    maxWidth: '90%'
  }
})(Typography)

const StyledTypographySQL = withStyles({
  root: {
    whiteSpace: 'pre',
    fontFamily: `'Inconsolata', monospace`
  }
})(Typography)

const StyledExpandButton = withStyles({
  root: {
    transform: 'rotate(45deg)',
    position: 'absolute',
    right: 0,
    bottom: 0,
    backgroundColor: 'white',
    cursor: 'pointer'
  }
})(OpenWithSharpIcon)

const StyledCloseIcon = withStyles({
  root: {
    position: 'absolute',
    right: '2rem',
    top: '1rem',
    backgroundColor: 'white',
    cursor: 'pointer',
    zIndex: 10
  }
})(CloseIcon)

const displaySQL = (SQL: string, SQLCommentClass: string) => {
  return !!SQL && SQL !== '' ? (
    SQL.split('\n').map((line, i) => {
      const extraClass = line.trim().startsWith('--') ? SQLCommentClass : ''
      return (
        <StyledTypographySQL key={i} className={extraClass}>
          {line}
        </StyledTypographySQL>
      )
    })
  ) : (
    <StyledTypographySQL align='center'>
      There is no SQL for this job at this time.
    </StyledTypographySQL>
  )
}
const JobDetailPage: FunctionComponent<IProps> = props => {
  const { jobs, classes, fetchJobRuns } = props
  const [SQLModalOpen, setSQLModalOpen] = useState(false)

  const { jobName } = useParams()
  const history = useHistory()

  const job = _find(jobs, j => j.name === jobName)

  // useEffect hook to run on any change to jobName
  useEffect(() => {
    job ? fetchJobRuns(job.name, job.namespace) : null
  }, [jobs.length])

  if (!job || jobs.length == 0) {
    return (
      <Box
      p={4}
      display='flex'
      flexDirection='column'
      justifyContent='space-between'
      className={classes.root}
      >
        <Typography align='center'>
          No job by the name of <strong>&quot;{jobName}&quot;</strong> found
        </Typography>
      </Box>
    )
  }

  const {
    root,
    _status,
    _name,
    _description,
    _SQL,
    _SQLComment,
    SQLModalContainer,
    SQLModal,
    SQLModalTitle,
    _owner,
    _ownerIcon,
    lastUpdated,
    topSection,
    copyToClipboard,
    closeButton
  } = classes

  const {
    name,
    description,
    updatedAt = '',
    latestRun,
    location,
    namespace,
    context = { SQL: '' }
  } = job as IJob

  const latestRuns = job ? job.latestRuns || [] : []
  const { SQL } = context

  return (
    <Box
      p={4}
      display='flex'
      flexDirection='column'
      justifyContent='space-between'
      className={root}
    >
      <div className={topSection}>
        <Tooltip title={latestRun ? latestRun.state : ''} placement="top">
          {latestRun ? <div className={`${_status}`} style={{backgroundColor: colorMap[latestRun.state]}} /> : <div></div>}
        </Tooltip>
        <Typography color='secondary' variant='h3' className={_name}>
          <a href={location} className='link' target='_'>
            {name}
          </a>
        </Typography>
        <StyledTypography color='primary' className={_description}>
          {description}
        </StyledTypography>
        <HowToRegIcon color='secondary' className={_ownerIcon} />
        <Typography className={_owner}>{namespace}</Typography>
        <Fab className={closeButton} onClick={() => history.push('/')} size="small" aria-label="edit">
          <CloseIcon />
        </Fab>
      </div>
      <Box
        className={_SQL}
        width='80%'
        minHeight={200}
        maxHeight={250}
        bgcolor='white'
        boxShadow={1}
        // using border to create effect of padding, which will not work when there's overflow
        border='1rem solid white'
        borderLeft='none'
        paddingLeft='2rem'
        mx='auto'
        my={2}
        borderRadius='3px'
        position='relative'
      >
        {!!SQL && SQL !== '' && (
          <StyledExpandButton color='secondary' onClick={() => setSQLModalOpen(true)} />
        )}
        {!!SQL && SQL !== '' && (
          <Modal aria-labelledby='modal-title' open={SQLModalOpen}>
            <div className={SQLModalContainer}>
              {/* Need this extra container for the absolutely-positioned elements */}
              <StyledCloseIcon fontSize='large' onClick={() => setSQLModalOpen(false)} />
              <Typography
                color='secondary'
                className={copyToClipboard}
                onClick={() => {
                  if (SQL) {
                    navigator.clipboard.writeText(SQL)
                  }
                }}
              >
                copy to clipboard
              </Typography>
              <div className={SQLModal}>
                <Typography id='modal-title' align='center' gutterBottom className={SQLModalTitle}>
                  {name}
                </Typography>
                {/* gutter (because we cannot put margin on the fixed-positioned title)*/}
                <Box height='4rem'></Box> {displaySQL(SQL, _SQLComment)}
              </div>
            </div>
          </Modal>
        )}
        {displaySQL(SQL, _SQLComment)}
      </Box>
      <div style={{display: 'flex'}}>
        <div className={classes.latestRunContainer}>
          {
            latestRuns.map(r => {
              return (
                <Tooltip key={r.id} title={r.state} placement="top">
                  <div key={r.id} className={classes.squareShape} style={{backgroundColor: colorMap[r.state]}}></div>
                </Tooltip>
              )
            })
          }
        </div>
        <div>
          <Typography className={lastUpdated}>
            {formatUpdatedAt(updatedAt)}
          </Typography>
        </div>
      </div>
    </Box>
  )
}

export default withStyles(styles)(JobDetailPage)
