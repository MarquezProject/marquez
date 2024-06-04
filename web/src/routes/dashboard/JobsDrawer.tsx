import { Box } from '@mui/system'
import { theme } from '../../helpers/theme'
import JobRunItem from './JobRunItem'
import MqText from '../../components/core/text/MqText'
import React from 'react'
const WIDTH = 800

const JobsDrawer = () => {
  return (
    <Box width={`${WIDTH}px`}>
      <Box px={2}>
        <Box
          position={'sticky'}
          top={0}
          bgcolor={theme.palette.background.default}
          pt={2}
          zIndex={theme.zIndex.appBar}
          sx={{ borderBottom: 1, borderColor: 'divider', width: '100%' }}
          mb={2}
        >
          <Box display={'flex'} alignItems={'center'} justifyContent={'space-between'} pb={2}>
            <MqText font={'mono'} heading>
              Job Runs
            </MqText>
          </Box>
        </Box>
        <Box>
          <JobRunItem />
          <JobRunItem />
          <JobRunItem />
          <JobRunItem />
          <JobRunItem />
          <JobRunItem />
          <JobRunItem />
          <JobRunItem />
          <JobRunItem />
        </Box>
      </Box>
    </Box>
  )
}

export default JobsDrawer
