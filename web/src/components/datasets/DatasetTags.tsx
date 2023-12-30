// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0
import * as Redux from 'redux'
import { IState } from '../../store/reducers'
import { Tag } from '../../types/api'
import { addDatasetTag, deleteDatasetTag, fetchTags } from '../../store/actionCreators'
import { bindActionCreators } from 'redux'
import { connect, useSelector } from 'react-redux'
import { createTheme } from '@mui/material'
import { useTheme } from '@emotion/react'
import Autocomplete, {
  AutocompleteChangeDetails,
  AutocompleteChangeReason,
} from '@mui/material/Autocomplete'
import Chip from '@mui/material/Chip'
import MQTooltip from '../core/tooltip/MQTooltip'
import React, { useEffect } from 'react'
import TextField from '@mui/material/TextField'

interface DatasetTagsProps {
  namespace: string
  datasetName: string
  datasetTags: string[]
}

interface DispatchProps {
  deleteDatasetTag: typeof deleteDatasetTag
  addDatasetTag: typeof addDatasetTag
  fetchTags: typeof fetchTags
}

type IProps = DatasetTagsProps & DispatchProps

const DatasetTags: React.FC<IProps> = (props) => {
  const { namespace, datasetName, datasetTags, deleteDatasetTag, addDatasetTag, fetchTags } = props

  useEffect(() => {
    fetchTags()
  }, [])

  useEffect(() => {
    fetchTags()
  }, [deleteDatasetTag, addDatasetTag])

  const tagData = useSelector((state: IState) => state.tags.tags)

  const handleTagChange = (
    _event: React.SyntheticEvent,
    _value: string[],
    reason: AutocompleteChangeReason,
    details?: AutocompleteChangeDetails<string> | undefined
  ) => {
    if (reason === 'selectOption' && details) {
      addDatasetTag(namespace, datasetName, details.option)
    }
  }

  const handleDelete = (deletedTag: string) => {
    deleteDatasetTag(namespace, datasetName, deletedTag)
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
    <Autocomplete
      multiple
      id='dataset-tags'
      disableClearable
      options={tagData.map((option) => option.name)}
      defaultValue={datasetTags}
      onChange={handleTagChange}
      renderTags={(value: string[]) => formatTags(value, tagData)}
      renderInput={(params) => (
        <TextField
          {...params}
          InputLabelProps={{
            shrink: true,
          }}
        />
      )}
    />
  )
}

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      fetchTags: fetchTags,
      deleteDatasetTag: deleteDatasetTag,
      addDatasetTag: addDatasetTag,
    },
    dispatch
  )

export default connect(null, mapDispatchToProps)(DatasetTags)
