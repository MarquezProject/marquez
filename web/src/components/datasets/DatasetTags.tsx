// Copyright 2018-2024 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0
import * as Redux from 'redux'
import { Autocomplete, TextField } from '@mui/material'
import { Box, createTheme } from '@mui/material'
import { IState } from '../../store/reducers'
import { LocalOffer } from '@mui/icons-material'
import {
  addDatasetFieldTag,
  addDatasetTag,
  addTags,
  deleteDatasetFieldTag,
  deleteDatasetTag,
} from '../../store/actionCreators'
import { bindActionCreators } from 'redux'
import { connect, useSelector } from 'react-redux'
import { useTheme } from '@emotion/react'
import AddIcon from '@mui/icons-material/Add'
import Button from '@mui/material/Button'
import Chip from '@mui/material/Chip'
import ClickAwayListener from '@mui/material/ClickAwayListener'
import Dialog from '@mui/material/Dialog'
import DialogActions from '@mui/material/DialogActions'
import DialogContent from '@mui/material/DialogContent'
import DialogTitle from '@mui/material/DialogTitle'
import FormControl from '@mui/material/FormControl'
import Grow from '@mui/material/Grow'
import IconButton from '@mui/material/IconButton'
import MQText from '../core/text/MqText'
import MQTooltip from '../core/tooltip/MQTooltip'
import MenuItem from '@mui/material/MenuItem'
import MenuList from '@mui/material/MenuList'
import Paper from '@mui/material/Paper'
import Popper from '@mui/material/Popper'
import React, { useRef, useState } from 'react'
import Select from '@mui/material/Select'
import Snackbar from '@mui/material/Snackbar'

interface DatasetTagsProps {
  fieldTag: boolean
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
  addTags: typeof addTags
}

type IProps = DatasetTagsProps & DispatchProps

const DatasetTags: React.FC<IProps> = (props) => {
  const {
    fieldTag,
    namespace,
    datasetName,
    datasetTags,
    deleteDatasetTag,
    addDatasetTag,
    deleteDatasetFieldTag,
    addDatasetFieldTag,
    datasetField,
    addTags,
  } = props

  const closeDialog = () => setDialogOpen(false)
  const i18next = require('i18next')

  const [isDialogOpen, setDialogOpen] = useState(false)
  const [listTag, setListTag] = useState('')

  const options = ['Add a Tag', 'Edit a Tag Description']
  const [openDropDown, setOpenDropDown] = useState(false)
  const [openTagDesc, setOpenTagDesc] = useState(false)
  const anchorRef = useRef<HTMLDivElement>(null)
  const [selectedIndex, setSelectedIndex] = useState(0)
  const [tagDescription, setTagDescription] = useState('No Description')
  const [snackbarOpen, setSnackbarOpen] = useState(false)
  const theme = createTheme(useTheme())

  const handleMenuItemClick = (
    _event: React.MouseEvent<HTMLLIElement, MouseEvent>,
    index: number
  ) => {
    setSelectedIndex(index)
    setOpenDropDown(false)
  }

  const handleTagDescClose = () => {
    setOpenTagDesc(false)
    setListTag('')
    setTagDescription('No Description')
  }

  const handleDropDownClose = (event: Event) => {
    if (anchorRef.current && anchorRef.current.contains(event.target as HTMLElement)) {
      return
    }
    setOpenDropDown(false)
  }

  const handleTagDescChange = (_event: any, value: string) => {
    const selectedTagData = tagData.find((tag) => tag.name === value)
    setListTag(value)
    setTagDescription(selectedTagData ? selectedTagData.description : 'No Description')
  }

  const handleDescriptionChange = (event: any) => {
    setTagDescription(event.target.value)
  }

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

  const addTag = () => {
    addTags(listTag, tagDescription)
    setSnackbarOpen(true)
    setOpenTagDesc(false)
    setListTag('')
    setTagDescription('No Description')
  }

  return (
    <>
      <Snackbar
        open={snackbarOpen}
        autoHideDuration={1000}
        style={{ zIndex: theme.zIndex.snackbar }}
        onClose={() => setSnackbarOpen(false)}
        message={'Tag updated.'}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'left' }}
      />
      <Box display={'flex'} alignItems={'center'}>
        {!fieldTag && <LocalOffer sx={{ mr: 1 }} fontSize={'small'} color={'primary'} />}
        <Autocomplete
          size={'small'}
          multiple
          disableClearable={true}
          onChange={(_, value) => {
            value
              .filter((tag) => !datasetTags.includes(tag))
              .forEach((tag) => {
                datasetField
                  ? addDatasetFieldTag(namespace, datasetName, tag, datasetField)
                  : addDatasetTag(namespace, datasetName, tag)
              })
          }}
          renderTags={(value) =>
            value.map((option) => (
              <Chip
                key={option}
                onDelete={() => {
                  handleDelete(option)
                }}
                sx={{ mr: 1 }}
                color={'primary'}
                variant='outlined'
                label={option}
                size='small'
              />
            ))
          }
          renderInput={(params) => (
            <TextField
              {...params}
              InputLabelProps={{
                ...params.InputProps,
              }}
              placeholder={datasetTags.length === 0 ? 'Tags' : ''}
              autoFocus
              size={'small'}
              color={'secondary'}
              variant={'outlined'}
              id='tag'
              fullWidth
              sx={{
                ml: 1,
                minWidth: 140,
                maxWidth: 480,
                borderWidth: 0,
                '& fieldset': { border: 'none' },
              }}
            />
          )}
          options={tagData.map((option) => option.name)}
          value={datasetTags}
        />
        {!fieldTag && (
          <MQTooltip title={'Add a tag to the system'}>
            <IconButton sx={{ ml: 2 }}>
              <AddIcon />
            </IconButton>
          </MQTooltip>
        )}
      </Box>
      <Popper
        sx={{
          zIndex: 1,
        }}
        open={openDropDown}
        anchorEl={anchorRef.current}
        role={undefined}
        transition
        disablePortal
      >
        {({ TransitionProps, placement }) => (
          <Grow
            {...TransitionProps}
            style={{
              transformOrigin: placement === 'bottom' ? 'center top' : 'center bottom',
            }}
          >
            <Paper>
              <ClickAwayListener onClickAway={handleDropDownClose}>
                <MenuList id='split-button-menu' autoFocusItem>
                  {options.map((option, index) => (
                    <MenuItem
                      key={option}
                      selected={index === selectedIndex}
                      disabled={index === 1 && !!datasetField}
                      onClick={(event) => handleMenuItemClick(event, index)}
                    >
                      {option}
                    </MenuItem>
                  ))}
                </MenuList>
              </ClickAwayListener>
            </Paper>
          </Grow>
        )}
      </Popper>
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
      <Dialog open={openTagDesc} fullWidth maxWidth='sm'>
        <DialogTitle>Select a Tag to change</DialogTitle>
        <DialogContent>
          <MQText subheading>Tag</MQText>
          <Autocomplete
            options={tagData.map((option) => option.name)}
            freeSolo
            autoSelect
            onChange={handleTagDescChange}
            renderInput={(params) => (
              <TextField
                {...params}
                placeholder={'Search for a Tag...or enter a new one.'}
                autoFocus
                margin='dense'
                id='tag'
                fullWidth
                variant='outlined'
                InputLabelProps={{
                  ...params.InputProps,
                  shrink: false,
                }}
              />
            )}
          />
          <MQText subheading bottomMargin>
            Description
          </MQText>
          <TextField
            autoFocus
            multiline
            id='tag-description'
            name='tag-description'
            fullWidth
            variant='outlined'
            placeholder={'No Description'}
            onChange={handleDescriptionChange}
            rows={6}
            value={tagDescription}
            InputProps={{
              style: { padding: '12px 16px' },
            }}
            InputLabelProps={{
              shrink: false,
            }}
          />
        </DialogContent>
        <DialogActions>
          <Button color='primary' onClick={addTag} disabled={listTag === ''}>
            Submit
          </Button>
          <Button color='primary' onClick={handleTagDescClose}>
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
      deleteDatasetTag: deleteDatasetTag,
      addDatasetTag: addDatasetTag,
      deleteDatasetFieldTag: deleteDatasetFieldTag,
      addDatasetFieldTag: addDatasetFieldTag,
      addTags: addTags,
    },
    dispatch
  )

export default connect(null, mapDispatchToProps)(DatasetTags)
