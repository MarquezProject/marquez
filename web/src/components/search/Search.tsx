// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { Box, Chip } from '@mui/material'
import { Close, SearchOutlined } from '@mui/icons-material'
import { DRAWER_WIDTH, theme } from '../../helpers/theme'
import { MqInputBase } from '../core/input-base/MqInputBase'
import { useLocation } from 'react-router'
import BaseSearch from './base-search/BaseSearch'
import ClickAwayListener from '@mui/material/ClickAwayListener'
import IconButton from '@mui/material/IconButton'
import React, { useEffect, useRef, useState } from 'react'
import SearchPlaceholder from './SearchPlaceholder'

const useCmdKShortcut = (callback: () => void) => {
  useEffect(() => {
    const handleKeyDown = (event: KeyboardEvent) => {
      if ((event.metaKey || event.ctrlKey) && event.key === 'k') {
        event.preventDefault() // Prevent the default browser action
        callback()
      }
    }

    window.addEventListener('keydown', handleKeyDown)

    return () => {
      window.removeEventListener('keydown', handleKeyDown)
    }
  }, [callback])
}

const Search: React.FC = () => {
  const [search, setSearch] = useState('')
  const [open, setOpen] = useState(true)

  const inputRef = useRef<HTMLInputElement>(null)

  // focus on cmd + k
  useCmdKShortcut(() => {
    if (inputRef.current) {
      inputRef.current.focus()
    }
  })

  const location = useLocation()
  useEffect(() => {
    // close search on a route change
    setOpen(false)
    setSearch('')
  }, [location])

  return (
    <Box width={`calc(100vw - ${DRAWER_WIDTH}px)`} position={'relative'} id={'searchContainer'}>
      <Box
        sx={{
          zIndex: theme.zIndex.appBar + 3,
          position: 'absolute',
          left: theme.spacing(4),
          display: 'flex',
          alignItems: 'center',
          height: '100%',
        }}
      ></Box>
      {search.length === 0 && <SearchPlaceholder />}
      {search.length > 0 && (
        <Box
          sx={{
            position: 'absolute',
            zIndex: theme.zIndex.appBar + 3,
            right: theme.spacing(12),
            display: 'flex',
            alignItems: 'center',
            height: '100%',
            cursor: 'pointer',
          }}
        ></Box>
      )}
      <Box>
        <MqInputBase
          spellCheck={false}
          sx={{
            zIndex: theme.zIndex.appBar + 2,
            height: '64px',
          }}
          inputRef={inputRef}
          fullWidth={true}
          autoFocus
          startAdornment={<SearchOutlined />}
          endAdornment={
            <>
              <IconButton
                sx={{ mr: 1 }}
                size={'small'}
                onClick={() => {
                  setOpen(false)
                  setSearch('')
                }}
              >
                <Close />
              </IconButton>
              <Chip size={'small'} variant={'outlined'} label={'⌘K'}></Chip>
            </>
          }
          onFocus={() => setOpen(true)}
          onChange={(event) => {
            setSearch(event.target.value)
            setOpen(true)
          }}
          value={search}
          autoComplete={'off'}
          id={'searchBar'}
        />
        <ClickAwayListener
          mouseEvent='onMouseDown'
          touchEvent='onTouchStart'
          onClickAway={() => setOpen(false)}
        >
          <BaseSearch search={search} open={open} />
        </ClickAwayListener>
      </Box>
    </Box>
  )
}

export default Search
