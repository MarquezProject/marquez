import { ArrowBackIosRounded, Refresh } from '@mui/icons-material'
import { Divider, FormControlLabel, Switch, TextField, Tooltip } from '@mui/material'
import { fetchLineage } from '../../store/actionCreators'
import { theme } from '../../helpers/theme'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'
import Box from '@mui/material/Box'
import IconButton from '@mui/material/IconButton'
import MqText from '../../components/core/text/MqText'
import React from 'react'

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
        <Tooltip title={`Back to ${nodeType === 'JOB' ? 'jobs' : 'datasets'}`}>
          <IconButton
            size={'small'}
            sx={{ mr: 2 }}
            onClick={() => navigate(nodeType === 'JOB' ? '/' : '/datasets')}
          >
            <ArrowBackIosRounded fontSize={'small'} />
          </IconButton>
        </Tooltip>
        <MqText heading>{nodeType === 'JOB' ? 'Jobs' : 'Datasets'}</MqText>
        <Divider orientation='vertical' flexItem sx={{ mx: 2 }} />
        <Box>
          <MqText subdued>Mode</MqText>
          <MqText font={'mono'}>Table Level</MqText>
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
                fetchLineage(nodeType, namespace, name, depth)
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
          sx={{ width: '80px', mr: 2 }}
          value={depth}
          onChange={(e) => {
            setDepth(isNaN(parseInt(e.target.value)) ? 0 : parseInt(e.target.value))
            searchParams.set('depth', e.target.value)
            setSearchParams(searchParams)
          }}
        />
        <Box display={'flex'} flexDirection={'column'}>
          <FormControlLabel
            control={
              <Switch
                size={'small'}
                defaultChecked
                value={isFull}
                onChange={(_, checked) => {
                  setIsFull(checked)
                }}
              />
            }
            label={<MqText font={'mono'}>Complete Mode</MqText>}
          />
          <FormControlLabel
            control={
              <Switch
                size={'small'}
                value={isCompact}
                onChange={(_, checked) => {
                  setIsCompact(checked)
                }}
              />
            }
            label={<MqText font={'mono'}>Compact Nodes</MqText>}
          />
        </Box>
      </Box>
    </Box>
  )
}
