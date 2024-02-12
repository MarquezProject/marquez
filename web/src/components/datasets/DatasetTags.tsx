// Copyright 2018-2024 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0
import * as Redux from 'redux'
import { Box, createTheme } from '@mui/material'
import { IState } from '../../store/reducers'
import { Tag } from '../../types/api'
import {
  addDatasetFieldTag,
  addDatasetTag,
  deleteDatasetFieldTag,
  deleteDatasetTag,
  fetchTags,
} from '../../store/actionCreators'
import { bindActionCreators } from 'redux'
import { connect, useSelector } from 'react-redux'
import { useTheme } from '@emotion/react'
import AddIcon from '@mui/icons-material/Add'
import Button from '@mui/material/Button'
import Chip from '@mui/material/Chip'
import Dialog from '@mui/material/Dialog'
import DialogActions from '@mui/material/DialogActions'
import DialogContent from '@mui/material/DialogContent'
import DialogTitle from '@mui/material/DialogTitle'
import FormControl from '@mui/material/FormControl'
import IconButton from '@mui/material/IconButton'
import MQText from '../core/text/MqText'
import MQTooltip from '../core/tooltip/MQTooltip'
import MenuItem from '@mui/material/MenuItem'
import React, { useEffect, useState } from 'react'
import Select from '@mui/material/Select'

interface DatasetTagsProps {
  namespace: string
  datasetName: string
  datasetTags: string[]
  datasetField?: string
}

interface DispatchProps {
  deleteDatasetTag: typeof deleteDatasetTag
  addDatasetTag: typeof addDatasetTag
  deleteDatasetFieldTag: typeof deleteDatasetFieldTag
  addDatasetFieldTag: typeof addDatasetFieldTag
  fetchTags: typeof fetchTags
}

type IProps = DatasetTagsProps & DispatchProps

const DatasetTags: React.FC<IProps> = (props) => {
  const {
    namespace,
    datasetName,
    datasetTags,
    deleteDatasetTag,
    addDatasetTag,
    deleteDatasetFieldTag,
    addDatasetFieldTag,
    fetchTags,
    datasetField,
  } = props

  const [isDialogOpen, setDialogOpen] = useState(false)
  const [listTag, setListTag] = useState('')

  const openDialog = () => setDialogOpen(true)
  const closeDialog = () => setDialogOpen(false)
  const i18next = require('i18next')

  useEffect(() => {
    fetchTags()
  }, [])

  const tagData = useSelector((state: IState) => state.tags.tags)

  const handleTagListChange = (event: any) => {
    setListTag(event.target.value)
  }

  const handleTagChange = () => {
    datasetField
      ? addDatasetFieldTag(namespace, datasetName, listTag, datasetField)
      : addDatasetTag(namespace, datasetName, listTag)
  }

  const handleDelete = (deletedTag: string) => {
    datasetField
      ? deleteDatasetFieldTag(namespace, datasetName, deletedTag, datasetField)
      : deleteDatasetTag(namespace, datasetName, deletedTag)
  }

  const formatTags = (tags: string[], tag_desc: Tag[]) => {
    const theme = createTheme(useTheme())
    return tags.map((tag, index) => {
      const tagDescription = tag_desc.find((tagItem) => tagItem.name === tag)
      const tooltipTitle = tagDescription?.description || 'No Tag Description'
      return (
        <MQTooltip title={tooltipTitle} key={tag}>
          <Chip
            color={'primary'}
            label={tag}
            size='small'
            onDelete={() => handleDelete(tag)}
            style={{
              display: 'row',
              marginRight: index < tags.length - 1 ? theme.spacing(1) : 0,
            }}
          />
        </MQTooltip>
      )
    })
  }

  return (
    <>
      <Box display={'flex'} alignItems={'center'}>
        <MQText subheading>{i18next.t('dataset_tags.tags')}</MQText>
        <MQTooltip placement='left' title={i18next.t('dataset_tags.tooltip')} key='tag-tooltip'>
          <IconButton
            onClick={openDialog}
            size='small'
            color='primary'
            sx={{ m: 1 }}
            aria-label='add'
          >
            <AddIcon fontSize='small' color='primary' />
          </IconButton>
        </MQTooltip>
      </Box>
      {formatTags(datasetTags, tagData)}
      <Dialog open={isDialogOpen} onClose={closeDialog} fullWidth maxWidth='sm'>
        <DialogTitle>{i18next.t('dataset_tags.dialogtitle')}</DialogTitle>
        <DialogContent>
          <FormControl variant='outlined' size='small' fullWidth>
            <Select
              displayEmpty
              value={listTag}
              onChange={handleTagListChange}
              inputProps={{
                name: 'tags',
                id: 'tag-select',
              }}
            >
              <MenuItem value=''>{i18next.t('dataset_tags.selecttagtoadd')}</MenuItem>
              {tagData.map((option) => (
                <MenuItem
                  style={{ whiteSpace: 'normal', maxWidth: '800px' }}
                  key={option.name}
                  value={option.name}
                >
                  <div>
                    <MQText bold>{option.name}</MQText>
                    <MQText subdued overflowHidden>
                      {option.description || 'No Tag Description'}
                    </MQText>
                  </div>
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </DialogContent>
        <DialogActions>
          <Button color='primary' onClick={handleTagChange} disabled={listTag === ''}>
            {i18next.t('dataset_tags.addatagdialog')}
          </Button>
          <Button onClick={closeDialog} color='primary'>
            {i18next.t('dataset_tags.canceltagdialog')}
          </Button>
        </DialogActions>
      </Dialog>
    </>
  )
}

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      fetchTags: fetchTags,
      deleteDatasetTag: deleteDatasetTag,
      addDatasetTag: addDatasetTag,
      deleteDatasetFieldTag: deleteDatasetFieldTag,
      addDatasetFieldTag: addDatasetFieldTag,
    },
    dispatch
  )

export default connect(null, mapDispatchToProps)(DatasetTags)
