// SPDX-License-Identifier: Apache-2.0

import React, { ChangeEvent, FunctionComponent, SetStateAction, useEffect } from 'react'

import * as Redux from 'redux'
import { Box, Chip, Tab, Tabs } from '@material-ui/core'
import { DatasetVersion } from '../../types/api'
import { IState } from '../../store/reducers'
import {
  Theme as ITheme,
  WithStyles as IWithStyles,
  createStyles,
  withStyles
} from '@material-ui/core/styles'
import { LineageDataset } from '../lineage/types'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { fetchDatasetVersions, resetDatasetVersions } from '../../store/actionCreators'
import { useHistory } from 'react-router-dom'
import CircularProgress from '@material-ui/core/CircularProgress/CircularProgress'
import CloseIcon from '@material-ui/icons/Close'
import DatasetInfo from './DatasetInfo'
import DatasetVersions from './DatasetVersions'
import IconButton from '@material-ui/core/IconButton'
import MqText from '../core/text/MqText'

const styles = ({ spacing }: ITheme) => {
  return createStyles({
    root: {
      padding: `0 ${spacing(2)}px`
    },
    tagList: {
      display: 'flex',
      flexWrap: 'wrap',
      listStyle: 'none',
      margin: 0,
      padding: 0
    },
    tag: {
      '&:not(:last-of-type)': {
        marginRight: spacing(1)
      }
    },
    noData: {
      padding: '125px 0 0 0'
    },
    infoIcon: {
      paddingLeft: '3px',
      paddingTop: '3px'
    },
    updated: {
      marginTop: '10px'
    }
  })
}

interface StateProps {
  dataset: LineageDataset
  versions: DatasetVersion[]
  versionsLoading: boolean
}

interface DispatchProps {
  fetchDatasetVersions: typeof fetchDatasetVersions
  resetDatasetVersions: typeof resetDatasetVersions
}

type IProps = IWithStyles<typeof styles> & StateProps & DispatchProps

function a11yProps(index: number) {
  return {
    id: `tab-${index}`,
    'aria-controls': `simple-tabpanel-${index}`
  }
}

const DatasetDetailPage: FunctionComponent<IProps> = props => {
  const { classes, fetchDatasetVersions, resetDatasetVersions, versions, versionsLoading } = props
  const { root } = classes
  const history = useHistory()

  useEffect(() => {
    fetchDatasetVersions(props.dataset.namespace, props.dataset.name)
  }, [props.dataset.name])

  // unmounting
  useEffect(() => {
    return () => {
      resetDatasetVersions()
    }
  }, [])

  const [tab, setTab] = React.useState(0)
  const handleChange = (event: ChangeEvent, newValue: SetStateAction<number>) => {
    setTab(newValue)
  }

  if (versionsLoading) {
    return (
      <Box display={'flex'} justifyContent={'center'}>
        <CircularProgress color='primary' />
      </Box>
    )
  }

  if (versions.length === 0) {
    return null
  }

  const dataset = versions[0]
  const { name, tags, description } = dataset

  return (
    <Box my={2} className={root}>
      <Box>
        {tags.length > 0 && (
          <ul className={classes.tagList}>
            {tags.map(tag => (
              <li key={tag} className={classes.tag}>
                <Chip size='small' label={tag} />
              </li>
            ))}
          </ul>
        )}
        <Box display={'flex'} justifyContent={'space-between'} mb={2}>
          <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
            <Tabs value={tab} onChange={handleChange} textColor='primary' indicatorColor='primary'>
              <Tab label='LATEST SCHEMA' {...a11yProps(0)} disableRipple={true} />
              <Tab label='VERSION HISTORY' {...a11yProps(1)} disableRipple={true} />
            </Tabs>
          </Box>
          <IconButton onClick={() => history.push('/datasets')}>
            <CloseIcon />
          </IconButton>
        </Box>
        <MqText heading font={'mono'}>
          {name}
        </MqText>
        <Box mb={2}>
          <MqText subdued>{description}</MqText>
        </Box>
      </Box>
      {tab === 0 && (
        <DatasetInfo
          datasetFields={dataset.fields}
          facets={dataset.facets}
          run={dataset.createdByRun}
        />
      )}
      {tab === 1 && <DatasetVersions versions={props.versions} />}
    </Box>
  )
}

const mapStateToProps = (state: IState) => ({
  datasets: state.datasets.result,
  versions: state.datasetVersions.result.versions,
  versionsLoading: state.datasetVersions.isLoading
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      fetchDatasetVersions: fetchDatasetVersions,
      resetDatasetVersions: resetDatasetVersions
    },
    dispatch
  )

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(withStyles(styles)(DatasetDetailPage))
