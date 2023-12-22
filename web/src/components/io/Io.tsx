// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import * as Redux from 'redux'
import { Box } from '@mui/system'
import { IState } from '../../store/reducers'
import { LineageNode } from '../lineage/types'

import { LineageGraph } from '../../types/api'
import { Table, TableBody, TableCell, TableHead, TableRow } from '@mui/material'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { encodeNode } from '../../helpers/nodes'
import { setSelectedNode } from '../../store/actionCreators'
import { useParams } from 'react-router-dom'
import MqEmpty from '../core/empty/MqEmpty'
import MqText from '../core/text/MqText'
import React, { FunctionComponent } from 'react'

export interface DispatchProps {
  setSelectedNode: typeof setSelectedNode
}

interface IOProps {
  lineageGraph: LineageGraph
}

function determineName(node: string) {
  const colonIndex1 = node.indexOf(':')
  if (colonIndex1 !== -1) {
    const colonIndex2 = node.indexOf(':', colonIndex1 + 1)
    if (colonIndex2 !== -1) {
      return node.substring(colonIndex2 + 1)
    }
  }
  return ''
}

export const determineLink = (current: LineageNode, edge: string) => {
  return `/lineage/${encodeNode(
    current.type === 'JOB' ? 'DATASET' : 'JOB',
    edge.split(':')[1],
    determineName(edge)
  )}`
}

const Io: FunctionComponent<IOProps & DispatchProps> = ({ lineageGraph, setSelectedNode }) => {
  const i18next = require('i18next')

  const { name, namespace, nodeType } = useParams()
  const node = lineageGraph.graph.find((node) => node.id === `${nodeType}:${namespace}:${name}`)
  const inputs = node?.inEdges
  const outputs = node?.outEdges

  if (!node) {
    return null
  }

  return (
    <Box display={'flex'}>
      <Box width={'50%'}>
        <Table sx={{ p: 2, mr: 1 }}>
          <TableHead>
            <TableRow>
              <TableCell>
                <MqText bold>INPUTS</MqText>
              </TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {inputs?.map((input) => (
              <TableRow key={input.origin}>
                <TableCell>
                  <MqText
                    link
                    linkTo={determineLink(node, input.origin)}
                    onClick={() => {
                      setSelectedNode(input.origin)
                    }}
                  >
                    {determineName(input.origin)}
                  </MqText>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
        {inputs && inputs.length === 0 && (
          <Box mt={2}>
            <MqEmpty title={i18next.t('lineage.empty')}>
              <MqText subdued>{i18next.t('lineage.no_inputs')}</MqText>
            </MqEmpty>
          </Box>
        )}
      </Box>
      <Box width={'50%'}>
        <Table sx={{ p: 2, ml: 1 }}>
          <TableHead>
            <TableRow>
              <TableCell>
                <MqText bold>OUTPUTS</MqText>
              </TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {outputs?.map((output) => (
              <TableRow key={output.destination}>
                <TableCell>
                  <MqText
                    link
                    linkTo={determineLink(node, output.destination)}
                    onClick={() => setSelectedNode(output.destination)}
                  >
                    {determineName(output.destination)}
                  </MqText>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
        {outputs && outputs.length === 0 && (
          <Box mt={2}>
            <MqEmpty title={i18next.t('lineage.empty')}>
              <MqText subdued>{i18next.t('lineage.no_outputs')}</MqText>
            </MqEmpty>
          </Box>
        )}
      </Box>
    </Box>
  )
}

const mapStateToProps = (state: IState) => {
  return {
    lineageGraph: state.lineage.lineage,
  }
}

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      setSelectedNode: setSelectedNode,
    },
    dispatch
  )

export default connect(mapStateToProps, mapDispatchToProps)(Io)
