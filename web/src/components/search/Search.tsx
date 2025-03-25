import { Box, Chip, CircularProgress, IconButton } from '@mui/material'
import { Close, SearchOutlined } from '@mui/icons-material'
import { DRAWER_WIDTH, HEADER_HEIGHT, theme } from '../../helpers/theme'
import { IState } from '../../store/reducers'
import { MqInputBase } from '../core/input-base/MqInputBase'
import { connect } from 'react-redux'
import { trackEvent } from '../ga4'
import { useLocation } from 'react-router'
import BaseSearch from './base-search/BaseSearch'
import ClickAwayListener from '@mui/material/ClickAwayListener'
import OpenSearch from './open-search/OpenSearch'
import React, { useEffect, useRef, useState } from 'react'
import SearchPlaceholder from './SearchPlaceholder'

interface StateProps {
  isLoading: boolean
}

interface SearchProps extends StateProps {
  onSearch: (query: string) => void // Add onSearch prop
}

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

const useEscapeShortcut = (callback: () => void) => {
  useEffect(() => {
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
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

const Search: React.FC<SearchProps> = ({ isLoading, onSearch }) => {
  const [search, setSearch] = useState('')
  const [open, setOpen] = useState(true)

  const inputRef = useRef<HTMLInputElement>(null)

  // focus on cmd + k
  useCmdKShortcut(() => {
    if (inputRef.current) {
      inputRef.current.focus()
    }
  })

  useEffect(() => {
    if (search === '') setOpen(false)
  }, [search])

  useEscapeShortcut(() => {
    setOpen(false)
  })

  const location = useLocation()
  useEffect(() => {
    // close search on a route change
    setOpen(false)
    setSearch('')
  }, [location])

  const handleSearch = () => {
    onSearch(search) // Call onSearch prop
    trackEvent('Search', 'Perform Search', search)
  }

  const handleFocus = () => {
    setOpen(true)
    trackEvent('Search', 'Focus Search Bar')
  }

  const handleClose = () => {
    setOpen(false)
    setSearch('')
    trackEvent('Search', 'Close Search Bar')
  }

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
            height: HEADER_HEIGHT - 1,
            paddingRight: theme.spacing(3),
          }}
          inputRef={inputRef}
          fullWidth={true}
          autoFocus
          startAdornment={<SearchOutlined />}
          endAdornment={
            <>
              {isLoading && <CircularProgress size={16} />}
              {open && (
                <IconButton color={'secondary'} sx={{ mr: 1 }} size={'small'} onClick={handleClose}>
                  <Close />
                </IconButton>
              )}
              <Chip
                color={open ? 'primary' : 'default'}
                size={'small'}
                variant={'outlined'}
                label={'⌘K'}
              />
            </>
          }
          onFocus={handleFocus}
          onChange={(event) => {
            setSearch(event.target.value)
            setOpen(true)
          }}
          onKeyPress={(event) => {
            if (event.key === 'Enter') {
              handleSearch() // Trigger search on Enter key press
            }
          }}
          value={search}
          autoComplete={'off'}
          id={'searchBar'}
        />
        <ClickAwayListener
          mouseEvent='onMouseDown'
          touchEvent='onTouchStart'
          onClickAway={handleClose}
        >
          <Box>
            {open && search.length > 0 && (
              <Box
                position={'absolute'}
                width={'100%'}
                sx={{
                  position: 'absolute',
                  width: '100%',
                  top: 0,
                  right: 0,
                  left: '-3px',
                  zIndex: theme.zIndex.appBar + 1,
                  border: `2px dashed ${theme.palette.secondary.main}`,
                  borderRadius: theme.spacing(1),
                  backgroundColor: theme.palette.background.default,
                  borderTopLeftRadius: 0,
                  borderBottomLeftRadius: 0,
                }}
              >
                <Box
                  mt={`${HEADER_HEIGHT - 1}px`}
                  borderTop={1}
                  borderColor={'divider'}
                  overflow={'auto'}
                  maxHeight={`calc(100vh - ${HEADER_HEIGHT}px - 24px)`}
                >
                  {process.env.REACT_APP_ADVANCED_SEARCH === 'true' ? (
                    <OpenSearch search={search} />
                  ) : (
                    <BaseSearch search={search} />
                  )}
                </Box>
              </Box>
            )}
          </Box>
        </ClickAwayListener>
      </Box>
    </Box>
  )
}

const mapStateToProps = (state: IState) => ({
  isLoading: state.openSearchJobs.isLoading || state.openSearchDatasets.isLoading,
})

export default connect(mapStateToProps)(Search)
