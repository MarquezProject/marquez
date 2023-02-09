// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import * as Redux from 'redux'
import { Box, Button, Chip, Tab, Tabs } from '@material-ui/core'
import { DatasetVersion } from '../../types/api'
import { IState } from '../../store/reducers'
import {
  Theme as ITheme,
  WithStyles as IWithStyles,
  createStyles,
  withStyles
} from '@material-ui/core/styles'
import { LineageDataset } from '../lineage/types'
import { alpha } from '@material-ui/core/styles'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { datasetFacetsStatus } from '../../helpers/nodes'
import {
  deleteDataset,
  dialogToggle,
  fetchDatasetVersions,
  resetDataset,
  resetDatasetVersions
} from '../../store/actionCreators'
import { theme } from '../../helpers/theme'
import { useHistory } from 'react-router-dom'
import CircularProgress from '@material-ui/core/CircularProgress/CircularProgress'
import CloseIcon from '@material-ui/icons/Close'
import DatasetColumnLineage from './DatasetColumnLineage'
import DatasetInfo from './DatasetInfo'
import DatasetVersions from './DatasetVersions'
import Dialog from '../Dialog'
import IconButton from '@material-ui/core/IconButton'
import MqStatus from '../core/status/MqStatus'
import MqText from '../core/text/MqText'

import React, { ChangeEvent, FunctionComponent, SetStateAction, useEffect } from 'react'

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
    buttonDelete: {
      borderColor: theme.palette.error.main,
      color: theme.palette.error.main,
      '&:hover': {
        borderColor: alpha(theme.palette.error.main, 0.3),
        backgroundColor: alpha(theme.palette.error.main, 0.3)
      }
    }
  })
}

interface StateProps {
  lineageDataset: LineageDataset
  versions: DatasetVersion[]
  versionsLoading: boolean
  datasets: IState['datasets']
  display: IState['display']
}

interface DispatchProps {
  fetchDatasetVersions: typeof fetchDatasetVersions
  resetDatasetVersions: typeof resetDatasetVersions
  resetDataset: typeof resetDataset
  deleteDataset: typeof deleteDataset
  dialogToggle: typeof dialogToggle
}

type IProps = IWithStyles<typeof styles> & StateProps & DispatchProps

function a11yProps(index: number) {
  return {
    id: `tab-${index}`,
    'aria-controls': `simple-tabpanel-${index}`
  }
}

const DatasetDetailPage: FunctionComponent<IProps> = props => {
  const {
    classes,
    datasets,
    display,
    fetchDatasetVersions,
    resetDataset,
    resetDatasetVersions,
    deleteDataset,
    dialogToggle,
    versions,
    versionsLoading,
    lineageDataset
  } = props
  const { root } = classes
  const history = useHistory()
  const i18next = require('i18next')

  useEffect(() => {
    fetchDatasetVersions(props.lineageDataset.namespace, props.lineageDataset.name)
  }, [props.lineageDataset.name])

  useEffect(() => {
    if (datasets.deletedDatasetName) {
      history.push('/datasets')
    }
  }, [datasets.deletedDatasetName])

  // unmounting
  useEffect(
    () => () => {
      resetDataset()
      resetDatasetVersions()
    },
    []
  )

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

  const firstVersion = versions[0]
  const { name, tags, description } = firstVersion
  const facetsStatus = datasetFacetsStatus(firstVersion.facets)

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
              <Tab
                label={i18next.t('datasets.latest_tab')}
                {...a11yProps(0)}
                disableRipple={true}
              />
              <Tab
                label={i18next.t('datasets.history_tab')}
                {...a11yProps(1)}
                disableRipple={true}
              />
              <Tab
                label={i18next.t('datasets.column_lineage_tab')}
                {...a11yProps(1)}
                disableRipple={true}
              />
            </Tabs>
          </Box>
          <Box display={'flex'} alignItems={'center'}>
            <Box mr={1}>
              <Button
                variant='outlined'
                className={classes.buttonDelete}
                onClick={() => {
                  props.dialogToggle('')
                }}
              >
                {i18next.t('datasets.dialog_delete')}
              </Button>
              <Dialog
                dialogIsOpen={display.dialogIsOpen}
                dialogToggle={dialogToggle}
                title={i18next.t('jobs.dialog_confirmation_title')}
                ignoreWarning={() => {
                  deleteDataset(lineageDataset.name, lineageDataset.namespace)
                  props.dialogToggle('')
                }}
              />
            </Box>
            <IconButton onClick={() => history.push('/datasets')}>
              <CloseIcon />
            </IconButton>
          </Box>
        </Box>
        <Box display={'flex'} alignItems={'center'}>
          {facetsStatus && (
            <Box mr={1}>
              <MqStatus color={facetsStatus} />
            </Box>
          )}
          <MqText heading font={'mono'}>
            {name}
          </MqText>
        </Box>
        <Box mb={2}>
          <MqText subdued>{description}</MqText>
        </Box>
      </Box>
      {tab === 0 && (
        <DatasetInfo
          datasetFields={firstVersion.fields}
          facets={firstVersion.facets}
          run={firstVersion.createdByRun}
        />
      )}
      {tab === 1 && <DatasetVersions versions={props.versions} />}
      {tab === 2 && <DatasetColumnLineage lineageDataset={props.lineageDataset} />}
    </Box>
  )
}

const mapStateToProps = (state: IState) => ({
  datasets: state.datasets,
  display: state.display,
  versions: state.datasetVersions.result.versions,
  versionsLoading: state.datasetVersions.isLoading
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      fetchDatasetVersions: fetchDatasetVersions,
      resetDatasetVersions: resetDatasetVersions,
      resetDataset: resetDataset,
      deleteDataset: deleteDataset,
      dialogToggle: dialogToggle
    },
    dispatch
  )

export default connect(mapStateToProps, mapDispatchToProps)(withStyles(styles)(DatasetDetailPage))
