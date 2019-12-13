import React, { FunctionComponent, useState } from 'react'
import {
  withStyles,
  createStyles,
  WithStyles as IWithStyles,
  Theme as ITheme
} from '@material-ui/core/styles'
import { Typography, Box } from '@material-ui/core'
import OpenWithSharpIcon from '@material-ui/icons/OpenWithSharp'
import CloseIcon from '@material-ui/icons/Close'
import Modal from '@material-ui/core/Modal'
import HowToRegIcon from '@material-ui/icons/HowToReg'
import { useParams } from 'react-router-dom'
import _find from 'lodash/find'

const globalStyles = require('../global_styles.css')
const { vibrantGreen } = globalStyles
import { formatUpdatedAt } from '../helpers'

import { IJob } from '../types'

const styles = ({ palette, spacing, shadows }: ITheme) => {
  return createStyles({
    root: {
      marginTop: '52vh',
      height: '48vh'
    },
    topSection: {
      display: 'grid',
      gridTemplateColumns: '40px 3fr 1fr',
      gridTemplateRows: '1fr 1fr',
      /* eslint-disable @typescript-eslint/quotes */
      gridTemplateAreas: `'status name owner-icon' '. description owner'`,
      alignItems: 'center'
    },
    lastUpdated: {
      color: palette.grey[600]
    },
    _status: {
      gridArea: 'status',
      width: spacing(2),
      height: spacing(2),
      borderRadius: '50%'
    },
    failed: {
      backgroundColor: palette.error.main
    },
    passed: {
      backgroundColor: vibrantGreen
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
    }
  })
}

type IProps = IWithStyles<typeof styles> & { jobs: IJob[] }

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
  const [SQLModalOpen, setSQLModalOpen] = useState(false)
  const { jobs, classes } = props
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
    copyToClipboard
  } = classes
  const { jobName } = useParams()
  const job = _find(jobs, j => j.name === jobName)
  if (!job) {
    return (
      <Box
        p={4}
        display='flex'
        flexDirection='column'
        justifyContent='space-between'
        className={root}
      >
        <Typography align='center'>
          No job by the name of <strong>&quot;{jobName}&quot;</strong> found
        </Typography>
      </Box>
    )
  }
  const {
    name,
    description,
    updatedAt = '',
    status = 'passed',
    location,
    namespace,
    context = { SQL: '' }
  } = job

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
        <div className={`${_status} ${classes[status]}`} />
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
      <Typography className={lastUpdated} align='right'>
        {formatUpdatedAt(updatedAt)}
      </Typography>
    </Box>
  )
}

export default withStyles(styles)(JobDetailPage)
