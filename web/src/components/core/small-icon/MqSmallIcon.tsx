// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { IconDefinition } from '@fortawesome/free-solid-svg-icons'
import Box from '@mui/material/Box'
import React from 'react'

interface OwnProps {
  icon: IconDefinition
  backgroundColor: string
  foregroundColor: string
  shape: 'circle' | 'rect'
}

const MqSmallIcon: React.FC<OwnProps> = ({ icon, backgroundColor, foregroundColor, shape }) => {
  return (
    <Box
      width={16}
      height={16}
      bgcolor={backgroundColor}
      borderRadius={shape === 'circle' ? '50%' : '4px'}
      display={'flex'}
      justifyContent={'center'}
      alignItems={'center'}
    >
      <FontAwesomeIcon
        style={{
          width: '10px !important',
          height: 9,
          fontSize: 9,
        }}
        fixedWidth
        icon={icon}
        color={foregroundColor}
      />
    </Box>
  )
}

export default MqSmallIcon
