// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import * as Redux from 'redux'
import { Box, Button, Chip, Tab, Tabs, createTheme } from '@mui/material'
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
} from '../../store/actionCreators'
import { useNavigate } from 'react-router-dom'
import CloseIcon from '@mui/icons-material/Close'
import DatasetColumnLineage from './DatasetColumnLineage'
import DatasetInfo from './DatasetInfo'
import DatasetVersions from './DatasetVersions'
import Dialog from '../Dialog'
import IconButton from '@mui/material/IconButton'
import MqStatus from '../core/status/MqStatus'
import MqText from '../core/text/MqText'

import { useTheme } from '@emotion/react'
import React, { ChangeEvent, FunctionComponent, SetStateAction, useEffect } from 'react'

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
  } = props
  const navigate = useNavigate()
  const i18next = require('i18next')
  const theme = createTheme(useTheme())

  useEffect(() => {
    fetchDatasetVersions(props.lineageDataset.namespace, props.lineageDataset.name)
  }, [props.lineageDataset.name])

  useEffect(() => {
    if (datasets.deletedDatasetName) {
      navigate('/datasets')
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
    <Box
      my={2}
      sx={{
        padding: `0 ${theme.spacing(2)}`,
      }}
    >
      <Box>
        {tags.length > 0 && (
          <ul
            style={{
              display: 'flex',
              flexWrap: 'wrap',
              listStyle: 'none',
              margin: 0,
              padding: 0,
            }}
          >
            {tags.map((tag, index) => (
              <li
                key={tag}
                style={
                  index < tags.length - 1
                    ? {
                        marginRight: theme.spacing(1),
                      }
                    : {}
                }
              >
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
            <IconButton onClick={() => navigate('/datasets')}>
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
  versionsLoading: state.datasetVersions.isLoading,
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      fetchDatasetVersions: fetchDatasetVersions,
      resetDatasetVersions: resetDatasetVersions,
      resetDataset: resetDataset,
      deleteDataset: deleteDataset,
      dialogToggle: dialogToggle,
    },
    dispatch
  )

export default connect(mapStateToProps, mapDispatchToProps)(DatasetDetailPage)
