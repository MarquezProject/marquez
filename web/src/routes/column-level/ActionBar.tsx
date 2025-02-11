import { ArrowBack, ArrowBackIosRounded, SwapHoriz } from '@mui/icons-material'
import {
  Box,
  CircularProgress,
  Divider,
  FormControl,
  IconButton,
  InputLabel,
  MenuItem,
  Select,
  SelectChangeEvent,
  TextField,
} from '@mui/material'
import { HEADER_HEIGHT, theme } from '../../helpers/theme'
import { fetchColumnLineage } from '../../store/actionCreators'
import { getColumnLineage } from '../../store/requests/columnlineage'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'
import MQTooltip from '../../components/core/tooltip/MQTooltip'
import MqText from '../../components/core/text/MqText'
import { trackEvent } from '../../components/ga4'
import React, { useState } from 'react'

interface ActionBarProps {
  fetchColumnLineage: typeof fetchColumnLineage
  depth: number
  setDepth: (depth: number) => void
  withDownstream: boolean
  setWithDownstream: (withDownstream: boolean) => void
}

export const ActionBar = ({
  depth,
  setDepth,
  withDownstream,
  setWithDownstream,
}: ActionBarProps) => {
  const { namespace, name } = useParams()
  const navigate = useNavigate()
  const [searchParams, setSearchParams] = useSearchParams()

  const [loading, setLoading] = useState(false)

  const handleDepthChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    setLoading(true)

    const newDepth = isNaN(parseInt(e.target.value)) ? 0 : parseInt(e.target.value)
    setDepth(newDepth)
    searchParams.set('depth', e.target.value)
    setSearchParams(searchParams)

    trackEvent('ActionBar', 'Change Depth', newDepth.toString())

    if (namespace && name) {
      await getColumnLineage('DATASET', namespace, name, newDepth, withDownstream)
    }
    setLoading(false)
  }

  const handleDirectionChange = async (e: SelectChangeEvent<'Both' | 'Upstream'>) => {
    setLoading(true)

    const newValue = e.target.value === 'Both'
    setWithDownstream(newValue)
    searchParams.set('withDownstream', newValue.toString())
    setSearchParams(searchParams)

    trackEvent('ActionBar', 'Change Direction', newValue ? 'Both' : 'Upstream')

    if (namespace && name) {
      await getColumnLineage('DATASET', namespace, name, depth, newValue)
    }
    setLoading(false)
  }

  const handleBackClick = () => {
    navigate('/');
    trackEvent('ActionBar', 'Navigate Back to Datasets')
  };

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
        <MQTooltip title={'Back to datasets'}>
          <IconButton size={'small'} sx={{ mr: 2 }} onClick={handleBackClick}>
            <ArrowBackIosRounded fontSize={'small'} />
          </IconButton>
        </MQTooltip>
        <MqText heading>Datasets</MqText>
        <Divider orientation='vertical' flexItem sx={{ mx: 2 }} />
        <Box>
          <MqText subdued>Mode</MqText>
          <MqText font={'mono'}>Column Level</MqText>
        </Box>
        <Divider orientation='vertical' flexItem sx={{ mx: 2 }} />
        <Box>
          <MqText subdued>Namespace</MqText>
          <MqText font={'mono'}>{namespace || 'Unknown namespace name'}</MqText>
        </Box>
        <Divider orientation='vertical' flexItem sx={{ mx: 2 }} />
        <Box>
          <MqText subdued>Name</MqText>
          <MqText font={'mono'}>{name || 'Unknown dataset name'}</MqText>
        </Box>
      </Box>
      <Box display={'flex'} alignItems={'center'}>
        {loading ? (
          <CircularProgress size={40} sx={{ width: '150px', mr: 2 }} />
        ) : (
          <>
            <MQTooltip
              title={
                'Expand to the left (upstream) or in both directions (upstream and downstream)'
              }
              placement='left-end'
            >
              <FormControl size='small' sx={{ width: '150px', mr: 2 }}>
                <InputLabel id='direction-label'>Direction</InputLabel>
                <Select
                  labelId='direction-label'
                  label='Direction'
                  id='direction-label'
                  size='small'
                  value={withDownstream ? 'Both' : 'Upstream'}
                  onChange={handleDirectionChange}
                  renderValue={(value) => (
                    <Box display='flex' alignItems='center'>
                      {value === 'Upstream' && <ArrowBack fontSize='small' sx={{ mr: 1 }} />}
                      {value === 'Both' && <SwapHoriz fontSize='small' sx={{ mr: 1 }} />}
                      {value}
                    </Box>
                  )}
                >
                  <MenuItem value={'Upstream'} sx={{ display: 'flex', alignItems: 'center' }}>
                    <ArrowBack fontSize='small' sx={{ mr: 1 }} />
                    Upstream
                  </MenuItem>
                  <MenuItem value={'Both'} sx={{ display: 'flex', alignItems: 'center' }}>
                    <SwapHoriz fontSize='small' sx={{ mr: 1 }} />
                    Both
                  </MenuItem>
                </Select>
              </FormControl>
            </MQTooltip>
            <MQTooltip title='Select the number of levels to display in the lineage'>
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
              />
            </MQTooltip>
          </>
        )}
      </Box>
    </Box>
  )
}
