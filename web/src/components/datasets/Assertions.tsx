// SPDX-License-Identifier: Apache-2.0

import { Assertion } from '../../types/api'
import { Table, TableBody, TableCell, TableHead, TableRow } from '@mui/material'
import { theme } from '../../helpers/theme'
import MqStatus from '../core/status/MqStatus'
import MqText from '../core/text/MqText'
import React from 'react'

interface OwnProps {
  assertions: Assertion[]
  hasHeader?: boolean
}

const Assertions: React.FC<OwnProps> = ({ assertions, hasHeader }) => {
  if (assertions.length === 0) {
    return null
  }
  return (
    <Table size={'small'}>
      {hasHeader && (
        <TableHead>
          <TableRow>
            <TableCell align={'left'}>
              <MqText bold>COLUMN</MqText>
            </TableCell>
            <TableCell align={'left'}>
              <MqText bold>ASSERTION</MqText>
            </TableCell>
            <TableCell align={'left'}>
              <MqText bold>STATUS</MqText>
            </TableCell>
          </TableRow>
        </TableHead>
      )}
      <TableBody>
        {assertions.map((assertion) => {
          const sx = { borderBottom: 'none' }
          return (
            <TableRow key={`${assertion.column}-${assertion.assertion}`}>
              <TableCell align={'left'} sx={sx}>
                <MqText font={'mono'}>{assertion.column}</MqText>
              </TableCell>
              <TableCell align={'left'} sx={sx}>
                <MqText subdued>{assertion.assertion}</MqText>
              </TableCell>
              <TableCell align={'left'} sx={sx}>
                {
                  <MqStatus
                    label={assertion.success ? 'pass'.toUpperCase() : 'fail'.toUpperCase()}
                    color={
                      assertion.success ? theme.palette.primary.main : theme.palette.error.main
                    }
                  ></MqStatus>
                }
              </TableCell>
            </TableRow>
          )
        })}
      </TableBody>
    </Table>
  )
}

export default Assertions
