import { ArrowBackIosRounded, Refresh } from '@mui/icons-material'
import {
  Box,
  CircularProgress,
  Divider,
  FormControlLabel,
  IconButton,
  Switch,
  TextField,
} from '@mui/material'
import { HEADER_HEIGHT, theme } from '../../helpers/theme'
import { fetchLineage } from '../../store/actionCreators'
import { getLineage } from '../../store/requests/lineage'
import { truncateText } from '../../helpers/text'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'
import MQTooltip from '../../components/core/tooltip/MQTooltip'
import MqText from '../../components/core/text/MqText'
import React, { useEffect, useState } from 'react'
import { trackEvent } from '../../components/ga4'

interface ActionBarProps {
  nodeType: 'DATASET' | 'JOB'
  fetchLineage: typeof fetchLineage
  depth: number
  setDepth: (depth: number) => void
  isCompact: boolean
  setIsCompact: (isCompact: boolean) => void
  isFull: boolean
  setIsFull: (isFull: boolean) => void
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
}: ActionBarProps) => {
  const { namespace, name } = useParams()
  const navigate = useNavigate()
  const [searchParams, setSearchParams] = useSearchParams()

  const [loading, setLoading] = useState(false)

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
    setDepth(newDepth)
    searchParams.set('depth', e.target.value)
    setSearchParams(searchParams)

    if (namespace && name) {
      await getLineage(nodeType, namespace, name, newDepth)
    }
    setLoading(false)
    trackEvent('ActionBar', 'Change Depth', newDepth.toString())
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

  useEffect(() => {
    if (!searchParams.has('isCompact')) {
      searchParams.set('isCompact', 'true')
      setSearchParams(searchParams)
      setIsCompact(true)
    }
  }, [])

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
          {loading ? (
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
    </Box>
  )
}
