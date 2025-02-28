import {
  Alert,
  Box,
  CircularProgress,
  Divider,
  FormControlLabel,
  IconButton,
  Snackbar,
  Switch,
  TextField,
} from '@mui/material'
import { ArrowBackIosRounded, Refresh } from '@mui/icons-material'
import { HEADER_HEIGHT, theme } from '../../helpers/theme'
import { fetchLineage } from '../../store/actionCreators'
import { getLineage } from '../../store/requests/lineage'
import { trackEvent } from '../../components/ga4'
import { truncateText } from '../../helpers/text'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'
import MQTooltip from '../../components/core/tooltip/MQTooltip'
import MqText from '../../components/core/text/MqText'
import React, { useEffect, useState } from 'react'

interface ActionBarProps {
  nodeType: 'DATASET' | 'JOB'
  fetchLineage: typeof fetchLineage
  depth: number
  setDepth: (depth: number) => void
  isCompact: boolean
  setIsCompact: (isCompact: boolean) => void
  isFull: boolean
  setIsFull: (isFull: boolean) => void
  isLoading: boolean
}

export const ActionBar = ({
  nodeType,
  fetchLineage,
  depth,
  setDepth,
  isCompact,
  setIsCompact,
  isFull,
  setIsFull,
  isLoading,
}: ActionBarProps) => {
  const { namespace, name } = useParams()
  const navigate = useNavigate()
  const [searchParams, setSearchParams] = useSearchParams()

  const [loading, setLoading] = useState(false)
  const [openSnackbar, setOpenSnackbar] = useState(false)
  const [maxDepth, setMaxDepth] = useState<number | null>(null)
  const [prevObjectsCount, setPrevObjectsCount] = useState<number | null>(null)
  const [prevDepth, setPrevDepth] = useState<number | null>(null)

  useEffect(() => {
    const resetLimitState = () => {
      setMaxDepth(null)
      setPrevObjectsCount(null)
      setPrevDepth(null)
    }

    const prevName = localStorage.getItem('prevName')
    if (prevName && prevName !== name) {
      localStorage.removeItem('maxDepth')
    }

    localStorage.setItem('prevName', name || '')

    resetLimitState()
  }, [name])

  useEffect(() => {
    const storedMaxDepth = localStorage.getItem('maxDepth')
    if (storedMaxDepth) {
      const parsedDepth = parseInt(storedMaxDepth)
      setMaxDepth(parsedDepth)

      if (depth > parsedDepth) {
        setDepth(parsedDepth)
        searchParams.set('depth', parsedDepth.toString())
        setSearchParams(searchParams)
      }
    }

    if (!searchParams.has('isCompact')) {
      searchParams.set('isCompact', 'true')
      setSearchParams(searchParams)
      setIsCompact(true)
    }
  }, [])

  const handleBackClick = () => {
    navigate(nodeType === 'JOB' ? '/jobs' : '/')
    trackEvent('ActionBar', 'Click Back Button', nodeType)
  }

  const handleRefreshClick = () => {
    if (namespace && name) {
      fetchLineage(nodeType, namespace, name, depth, true)
      trackEvent('ActionBar', 'Refresh Lineage', nodeType)
    }
  }

  const handleDepthChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    setLoading(true)

    const newDepth = isNaN(parseInt(e.target.value)) ? 0 : parseInt(e.target.value)

    if (maxDepth !== null && newDepth > maxDepth) {
      setDepth(maxDepth)
      searchParams.set('depth', maxDepth.toString())
      setSearchParams(searchParams)
      setOpenSnackbar(true)
      setTimeout(() => setLoading(false), 2000)
      return
    }

    setDepth(newDepth)
    searchParams.set('depth', e.target.value)
    setSearchParams(searchParams)

    if (namespace && name) {
      try {
        const response = await getLineage(nodeType, namespace, name, newDepth)

        if (Array.isArray(response.graph)) {
          const totalObjects = response.graph.length

          if (prevObjectsCount !== null && prevObjectsCount === totalObjects) {
            const newMaxDepth = prevDepth || newDepth

            setDepth(newMaxDepth)
            searchParams.set('depth', newMaxDepth.toString())
            setSearchParams(searchParams)

            setMaxDepth(newMaxDepth)
            localStorage.setItem('maxDepth', newMaxDepth.toString())
            setOpenSnackbar(true)
          }

          setPrevObjectsCount(totalObjects)
          setPrevDepth(newDepth)
        } else {
          return
        }
      } catch (error) {
        return
      }
    }

    setLoading(false)
    trackEvent('ActionBar', 'Change Depth', newDepth.toString())
  }

  const handleCloseSnackbar = () => {
    setOpenSnackbar(false)
  }

  const handleAllDependenciesToggle = (checked: boolean) => {
    setIsFull(checked)
    searchParams.set('isFull', checked.toString())
    setSearchParams(searchParams)
    trackEvent('ActionBar', 'Toggle All Dependencies', checked.toString())
  }

  const handleHideColumnNamesToggle = (checked: boolean) => {
    setIsCompact(checked)
    searchParams.set('isCompact', checked.toString())
    setSearchParams(searchParams)
    trackEvent('ActionBar', 'Toggle Hide Column Names', checked.toString())
  }
  return (
    <Box
      sx={{
        borderBottomWidth: 2,
        borderTopWidth: 0,
        borderLeftWidth: 0,
        borderRightWidth: 0,
        borderStyle: 'dashed',
      }}
      display={'flex'}
      height={HEADER_HEIGHT - 1}
      justifyContent={'space-between'}
      alignItems={'center'}
      px={2}
      borderColor={theme.palette.secondary.main}
    >
      <Box display={'flex'} alignItems={'center'}>
        <MQTooltip title={`Back to ${nodeType === 'JOB' ? 'jobs' : 'datasets'}`}>
          <IconButton size={'small'} sx={{ mr: 2 }} onClick={handleBackClick}>
            <ArrowBackIosRounded fontSize={'small'} />
          </IconButton>
        </MQTooltip>
        <MqText heading>{nodeType === 'JOB' ? 'Jobs' : 'Datasets'}</MqText>
        <Divider orientation='vertical' flexItem sx={{ mx: 2 }} />
        <Box>
          <MqText subdued>Mode</MqText>
          <MqText font={'mono'}>Table Level</MqText>
        </Box>
        <Divider orientation='vertical' flexItem sx={{ mx: 2 }} />
        <Box>
          <MqText subdued>Namespace</MqText>
          <MqText font={'mono'}>
            {namespace ? truncateText(namespace, 50) : 'Unknown namespace name'}
          </MqText>
        </Box>
        <Divider orientation='vertical' flexItem sx={{ mx: 2 }} />
        <Box>
          <MqText subdued>Name</MqText>
          <MqText font={'mono'}>{name ? truncateText(name, 190) : 'Unknown dataset name'}</MqText>
        </Box>
      </Box>
      <Box display={'flex'} alignItems={'center'}>
        <MQTooltip title={'Refresh'}>
          <IconButton sx={{ mr: 2 }} color={'primary'} size={'small'} onClick={handleRefreshClick}>
            <Refresh fontSize={'small'} />
          </IconButton>
        </MQTooltip>
        <MQTooltip title={'Select the number of levels to display in the lineage'}>
          {loading || isLoading === true ? (
            <CircularProgress size={40} sx={{ width: '80px', mr: 2 }} />
          ) : (
            <TextField
              id='column-level-depth'
              type='number'
              inputProps={{ min: 0 }}
              label='Depth'
              variant='outlined'
              size='small'
              sx={{ width: '80px' }}
              value={depth}
              onChange={handleDepthChange}
              disabled={loading}
            />
          )}
        </MQTooltip>

        <Box display={'flex'} flexDirection={'column'} sx={{ marginLeft: 2 }}>
          <MQTooltip title={'Show all dependencies, including indirect ones'}>
            <FormControlLabel
              control={
                <Switch
                  size={'small'}
                  value={isFull}
                  defaultChecked={searchParams.get('isFull') === 'true'}
                  onChange={(_, checked) => handleAllDependenciesToggle(checked)}
                />
              }
              label={<MqText font={'mono'}>All dependencies</MqText>}
            />
          </MQTooltip>
          <MQTooltip title={'Hide column names for each dataset'}>
            <FormControlLabel
              control={
                <Switch
                  size={'small'}
                  checked={isCompact}
                  defaultChecked={searchParams.get('isCompact') === 'true'}
                  onChange={(_, checked) => handleHideColumnNamesToggle(checked)}
                />
              }
              label={<MqText font={'mono'}>Hide column names</MqText>}
            />
          </MQTooltip>
        </Box>
      </Box>
      <Snackbar open={openSnackbar} autoHideDuration={1500} onClose={handleCloseSnackbar}>
        <Alert
          onClose={handleCloseSnackbar}
          severity='info'
          variant='filled'
          sx={{ width: '100%', backgroundColor: '#FFFFFF', color: '#191E26' }}
        >
          You’ve reached the maximum depth.
        </Alert>
      </Snackbar>
    </Box>
  )
}
