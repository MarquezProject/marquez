// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import * as Redux from 'redux'
import { Box, Button, Tab, Tabs, createTheme } from '@mui/material'
import { CircularProgress } from '@mui/material'
import { DatasetVersion } from '../../types/api'
import { IState } from '../../store/reducers'
import { LineageDataset } from '../lineage/types'
import { alpha } from '@mui/material/styles'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { datasetFacetsStatus } from '../../helpers/nodes'
import {
  deleteDataset,
  dialogToggle,
  fetchDatasetVersions,
  resetDataset,
  resetDatasetVersions,
  setTabIndex,
} from '../../store/actionCreators'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useTheme } from '@emotion/react'
import CloseIcon from '@mui/icons-material/Close'
import DatasetColumnLineage from './DatasetColumnLineage'
import DatasetInfo from './DatasetInfo'
import DatasetTags from './DatasetTags'
import DatasetVersions from './DatasetVersions'
import Dialog from '../Dialog'
import IconButton from '@mui/material/IconButton'
import Io from '../io/Io'
import MqStatus from '../core/status/MqStatus'
import MqText from '../core/text/MqText'
import React, { ChangeEvent, FunctionComponent, useEffect } from 'react'

interface StateProps {
  lineageDataset: LineageDataset
  versions: DatasetVersion[]
  versionsLoading: boolean
  datasets: IState['datasets']
  display: IState['display']
  tabIndex: IState['lineage']['tabIndex']
}

interface DispatchProps {
  fetchDatasetVersions: typeof fetchDatasetVersions
  resetDatasetVersions: typeof resetDatasetVersions
  resetDataset: typeof resetDataset
  deleteDataset: typeof deleteDataset
  dialogToggle: typeof dialogToggle
  setTabIndex: typeof setTabIndex
}

type IProps = StateProps & DispatchProps

function a11yProps(index: number) {
  return {
    id: `tab-${index}`,
    'aria-controls': `simple-tabpanel-${index}`,
  }
}

const DatasetDetailPage: FunctionComponent<IProps> = (props) => {
  const {
    datasets,
    display,
    fetchDatasetVersions,
    resetDataset,
    resetDatasetVersions,
    deleteDataset,
    dialogToggle,
    versions,
    versionsLoading,
    lineageDataset,
    tabIndex,
    setTabIndex,
  } = props
  const navigate = useNavigate()
  const i18next = require('i18next')
  const theme = createTheme(useTheme())
  const [_, setSearchParams] = useSearchParams()

  // unmounting
  useEffect(
    () => () => {
      resetDataset()
      resetDatasetVersions()
    },
    []
  )

  useEffect(() => {
    fetchDatasetVersions(lineageDataset.namespace, lineageDataset.name)
  }, [lineageDataset.name, datasets.refreshTags])

  // if the dataset is deleted then redirect to datasets end point
  useEffect(() => {
    if (datasets.deletedDatasetName) {
      navigate('/datasets')
    }
  }, [datasets.deletedDatasetName])

  const handleChange = (_: ChangeEvent, newValue: number) => {
    setTabIndex(newValue)
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
    <Box
      my={2}
      sx={{
        padding: `0 ${theme.spacing(2)}`,
      }}
    >
      <Box>
        <DatasetTags
          datasetTags={tags}
          datasetName={lineageDataset.name}
          namespace={lineageDataset.namespace}
        />
        <Box display={'flex'} justifyContent={'space-between'} mb={2}>
          <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
            <Tabs
              value={tabIndex}
              onChange={handleChange}
              textColor='primary'
              indicatorColor='primary'
            >
              <Tab
                label={i18next.t('datasets.latest_tab')}
                {...a11yProps(0)}
                disableRipple={true}
              />
              <Tab label={'I/O'} {...a11yProps(1)} disableRipple={true} />
              <Tab
                label={i18next.t('datasets.history_tab')}
                {...a11yProps(2)}
                disableRipple={true}
              />
              <Tab
                label={i18next.t('datasets.column_lineage_tab')}
                {...a11yProps(3)}
                disableRipple={true}
              />
            </Tabs>
          </Box>
          <Box display={'flex'} alignItems={'center'}>
            <Box mr={1}>
              <Button
                variant='outlined'
                sx={{
                  borderColor: theme.palette.error.main,
                  color: theme.palette.error.main,
                  '&:hover': {
                    borderColor: alpha(theme.palette.error.main, 0.3),
                    backgroundColor: alpha(theme.palette.error.main, 0.3),
                  },
                }}
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
            <IconButton onClick={() => setSearchParams({})}>
              <CloseIcon fontSize={'small'} />
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
      {tabIndex === 0 && (
        <DatasetInfo
          datasetFields={firstVersion.fields}
          facets={firstVersion.facets}
          run={firstVersion.createdByRun}
        />
      )}
      {tabIndex === 1 && <Io />}
      {tabIndex === 2 && <DatasetVersions versions={props.versions} />}
      {tabIndex === 3 && <DatasetColumnLineage lineageDataset={props.lineageDataset} />}
    </Box>
  )
}

const mapStateToProps = (state: IState) => ({
  datasets: state.datasets,
  display: state.display,
  versions: state.datasetVersions.result.versions,
  versionsLoading: state.datasetVersions.isLoading,
  tabIndex: state.lineage.tabIndex,
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      fetchDatasetVersions: fetchDatasetVersions,
      resetDatasetVersions: resetDatasetVersions,
      resetDataset: resetDataset,
      deleteDataset: deleteDataset,
      dialogToggle: dialogToggle,
      setTabIndex: setTabIndex,
    },
    dispatch
  )

export default connect(mapStateToProps, mapDispatchToProps)(DatasetDetailPage)
