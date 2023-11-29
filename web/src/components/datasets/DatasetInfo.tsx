// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import * as Redux from 'redux'
import {
  Accordion,
  Box,
  Card,
  CardContent,
  Divider,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
} from '@mui/material'
import { Chip, Drawer } from '@mui/material'
import { Field, Run, Tag } from '../../types/api'
import { IState } from '../../store/reducers'
import { connect, useSelector } from 'react-redux'
import { createTheme } from '@mui/material/styles'
import { fetchJobFacets, fetchTags, resetFacets } from '../../store/actionCreators'
import { stopWatchDuration } from '../../helpers/time'
import { useTheme } from '@emotion/react'
import AccordionDetails from '@mui/material/AccordionDetails'
import AccordionSummary from '@mui/material/AccordionSummary'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import MQTooltip from '../core/tooltip/MQTooltip'
import MqCode from '../core/code/MqCode'
import MqEmpty from '../core/empty/MqEmpty'
import MqJsonView from '../core/json-view/MqJsonView'
import MqText from '../core/text/MqText'
import React, { FunctionComponent, useEffect, useState } from 'react'
import ReadMoreIcon from '@mui/icons-material/ReadMore'
import RunStatus from '../jobs/RunStatus'

export interface DispatchProps {
  fetchJobFacets: typeof fetchJobFacets
  resetFacets: typeof resetFacets
}

interface JobFacets {
  [key: string]: object
}

export interface JobFacetsProps {
  jobFacets: JobFacets
}

export interface SqlFacet {
  query: string
}

type DatasetInfoProps = {
  datasetFields: Field[]
  facets?: object
  run?: Run
} & JobFacetsProps &
  DispatchProps

const formatColumnTags = (tags: string[], tag_desc: Tag[]) => {
  const theme = createTheme(useTheme())
  return (
    <>
      {tags.map((tag, index) => {
        const tagDescription = tag_desc.find((tagItem) => tagItem.name === tag)
        const tooltipTitle = tagDescription?.description || 'No Tag Description'
        return (
          <MQTooltip title={tooltipTitle} key={tag}>
            <Chip
              label={tag}
              size='small'
              style={{
                display: 'row',
                marginRight: index < tags.length - 1 ? theme.spacing(1) : 0,
                marginTop: 3,
              }}
            />
          </MQTooltip>
        )
      })}
    </>
  )
}

const DatasetInfo: FunctionComponent<DatasetInfoProps> = (props) => {
  const { datasetFields, facets, run, jobFacets, fetchJobFacets, resetFacets } = props
  const i18next = require('i18next')

  const [open, setOpen] = useState(false)
  const [selectedKey, setSelectedKey] = useState<string | undefined>(undefined)
  const theme = createTheme(useTheme())

  useEffect(() => {
    run && fetchJobFacets(run.id)
    run && fetchTags()
  }, [run])

  // unmounting
  useEffect(
    () => () => {
      resetFacets()
    },
    []
  )

  const tagData = useSelector((state: IState) => state.tags.tags)
  const handleOpen = (key: string) => {
    setOpen(true)
    setSelectedKey(key)
  }

  const selectedField = datasetFields.find((field) => field.name === selectedKey)
  const selectedFieldTags = selectedField?.tags || []
  const selectedFieldDesc = selectedField?.description || 'No Description'

  return (
    <Box>
      {datasetFields.length === 0 && (
        <MqEmpty
          title={i18next.t('dataset_info.empty_title')}
          body={i18next.t('dataset_info.empty_body')}
        />
      )}
      {datasetFields.length > 0 && (
        <>
          <Table size='small'>
            <TableHead>
              <TableRow>
                <TableCell align='left'>
                  <MqText subheading inline>
                    {i18next.t('dataset_info_columns.name')}
                  </MqText>
                </TableCell>
                <TableCell align='left'>
                  <MqText subheading inline>
                    {i18next.t('dataset_info_columns.type')}
                  </MqText>
                </TableCell>
                <TableCell align='left'>
                  <MqText subheading inline>
                    {i18next.t('dataset_info_columns.description')}
                  </MqText>
                </TableCell>
                <TableCell align='left'></TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {datasetFields.map((field) => {
                return (
                  <TableRow key={field.name}>
                    <TableCell align='left'>{field.name}</TableCell>
                    <TableCell align='left'>{field.type}</TableCell>
                    <TableCell align='left'>{field.description || 'no description'}</TableCell>
                    <TableCell>
                      <ReadMoreIcon
                        onClick={() => handleOpen(field.name)}
                        sx={{ align: 'Right' }}
                      ></ReadMoreIcon>
                    </TableCell>
                  </TableRow>
                )
              })}
            </TableBody>
          </Table>
          <Drawer
            elevation={0}
            anchor='right'
            open={open}
            onClose={() => setOpen(false)}
            sx={{ zIndex: theme.zIndex.drawer + 1 }}
            PaperProps={{
              sx: {
                width: 400,
                backgroundColor: theme.palette.background.paper,
                border: `2px dashed ${theme.palette.secondary.main}`,
                p: 1,
              },
            }}
          >
            <Card>
              <CardContent sx={{ backgroundColor: theme.palette.background.paper }}>
                <MqText heading bottomMargin>
                  {selectedKey}
                </MqText>
              </CardContent>
            </Card>
            <Divider />
            <Card>
              <CardContent sx={{ backgroundColor: theme.palette.background.paper }}>
                <MqText bottomMargin>{selectedFieldDesc}</MqText>
              </CardContent>
            </Card>
            <Accordion elevation={0}>
              <AccordionSummary
                expandIcon={<ExpandMoreIcon />}
                sx={{
                  backgroundColor: theme.palette.background.paper,
                }}
              >
                <MqText bold bottomMargin>
                  Tags
                </MqText>
              </AccordionSummary>
              <AccordionDetails
                sx={{
                  backgroundColor: theme.palette.background.paper,
                }}
              >
                {selectedFieldTags.length > 0
                  ? formatColumnTags(selectedFieldTags, tagData)
                  : 'No Tags'}
              </AccordionDetails>
            </Accordion>
          </Drawer>
        </>
      )}
      {facets && (
        <Box mt={2}>
          <Box mb={1}>
            <MqText subheading>{i18next.t('dataset_info.facets_subhead')}</MqText>
          </Box>
          <MqJsonView
            data={facets}
            searchable={true}
            aria-label={i18next.t('dataset_info.facets_subhead_aria')}
            aria-required='True'
            placeholder='Search'
          />
        </Box>
      )}
      {run && (
        <Box mt={2}>
          <Box mb={1}>
            <Box display={'flex'} alignItems={'center'} justifyContent={'space-between'}>
              <Box display={'flex'} alignItems={'center'}>
                <RunStatus run={run} />
                <MqText subheading>{i18next.t('dataset_info.run_subhead')}</MqText>
              </Box>
              <Box display={'flex'}>
                <MqText bold>{i18next.t('dataset_info.duration')}&nbsp;</MqText>
                <MqText subdued>{stopWatchDuration(run.durationMs)}</MqText>
              </Box>
            </Box>
            <MqText subdued>{run.jobVersion && run.jobVersion.name}</MqText>
          </Box>
          {<MqCode code={(jobFacets?.sql as SqlFacet)?.query} language={'sql'} />}
        </Box>
      )}
    </Box>
  )
}

const mapStateToProps = (state: IState) => ({
  jobFacets: state.facets.result,
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  Redux.bindActionCreators(
    {
      fetchJobFacets: fetchJobFacets,
      resetFacets: resetFacets,
      fetchTags: fetchTags,
    },
    dispatch
  )

export default connect(mapStateToProps, mapDispatchToProps)(DatasetInfo)
