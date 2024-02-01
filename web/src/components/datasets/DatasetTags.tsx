// Copyright 2018-2024 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0
import * as Redux from 'redux'
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
import { createTheme } from '@mui/material'
import { useTheme } from '@emotion/react'
import Button from '@mui/material/Button'
import Chip from '@mui/material/Chip'
import Dialog from '@mui/material/Dialog'
import DialogActions from '@mui/material/DialogActions'
import DialogContent from '@mui/material/DialogContent'
import DialogTitle from '@mui/material/DialogTitle'
import FormControl from '@mui/material/FormControl'
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
      <Button onClick={openDialog} variant='outlined' color='primary' sx={{ m: 1 }}>
        Add a Tag
      </Button>
      {formatTags(datasetTags, tagData)}
      <Dialog open={isDialogOpen} onClose={closeDialog} maxWidth='md'>
        <DialogTitle>Add Tags</DialogTitle>
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
              <MenuItem value=''>Select a tag to add...</MenuItem>
              {tagData.map((option) => (
                <MenuItem style={{ whiteSpace: 'normal' }} key={option.name} value={option.name}>
                  {`${option.name} - ${option.description || 'No Tag Description'}`}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </DialogContent>
        <DialogActions>
          <Button color='primary' onClick={handleTagChange} disabled={listTag === ''}>
            Add Tag
          </Button>
          <Button onClick={closeDialog} color='primary'>
            Cancel
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
