// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import React, { ReactElement } from 'react'
import Tooltip from '@mui/material/Tooltip'
import { createTheme } from '@mui/material/styles'
import { useTheme } from '@emotion/react'

interface MqToolTipProps {
    title : string | ReactElement
    children : ReactElement
}

//default style thing
const MQTooltip: React.FC<MqToolTipProps> = ({ title, children }) => {
    const theme = createTheme(useTheme())
    return <Tooltip title={title}
        componentsProps={{ tooltip: { sx:
        {
            backgroundColor: theme.palette.background.default,
            color: theme.palette.common.white,
            border: `1px solid ${theme.palette.common.white}`,
            maxWidth: '600px',
            fontSize: 14
        }, } }}
        >{children}</Tooltip>
}

export default MQTooltip