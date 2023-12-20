import { ArrowBackIosRounded, Refresh } from '@mui/icons-material'
import { Divider, TextField, Tooltip } from '@mui/material'
import { fetchColumnLineage } from '../../store/actionCreators'
import { theme } from '../../helpers/theme'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'
import Box from '@mui/material/Box'
import IconButton from '@mui/material/IconButton'
import MqText from '../../components/core/text/MqText'
import React from 'react'

interface ActionBarProps {
  fetchColumnLineage: typeof fetchColumnLineage
  depth: number
  setDepth: (depth: number) => void
}

export const ActionBar = ({ fetchColumnLineage, depth, setDepth }: ActionBarProps) => {
  const { namespace, name } = useParams()
  const navigate = useNavigate()
  const [searchParams, setSearchParams] = useSearchParams()
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
      height={'64px'}
      justifyContent={'space-between'}
      alignItems={'center'}
      px={2}
      borderColor={theme.palette.secondary.main}
    >
      <Box display={'flex'} alignItems={'center'}>
        <Tooltip title={'Back to datasets'}>
          <IconButton size={'small'} sx={{ mr: 2 }} onClick={() => navigate('/datasets')}>
            <ArrowBackIosRounded fontSize={'small'} />
          </IconButton>
        </Tooltip>
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
        <Tooltip title={'Refresh'}>
          <IconButton
            sx={{ mr: 2 }}
            color={'primary'}
            size={'small'}
            onClick={() => {
              if (namespace && name) {
                fetchColumnLineage('DATASET', namespace, name, depth)
              }
            }}
          >
            <Refresh fontSize={'small'} />
          </IconButton>
        </Tooltip>
        <TextField
          id='column-level-depth'
          type='number'
          label='Depth'
          variant='outlined'
          size='small'
          sx={{ width: '80px' }}
          value={depth}
          onChange={(e) => {
            setDepth(isNaN(parseInt(e.target.value)) ? 0 : parseInt(e.target.value))
            searchParams.set('depth', e.target.value)
            setSearchParams(searchParams)
          }}
        />
      </Box>
    </Box>
  )
}
