// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import * as Redux from 'redux'
import { Box, FormControl, MenuItem, Select } from '@mui/material'
import { IState } from '../../store/reducers'
import { MqInputBase } from '../core/input-base/MqInputBase'
import { Namespace } from '../../types/api'
import { Nullable } from '../../types/util/Nullable'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { selectNamespace } from '../../store/actionCreators'
import { theme } from '../../helpers/theme'
import MqText from '../core/text/MqText'
import React from 'react'

interface OwnProps {}

interface StateProps {
  namespaces: Namespace[]
  selectedNamespace: Nullable<string>
}

interface DispatchProps {
  selectNamespace: typeof selectNamespace
}

type NamespaceSelectProps = OwnProps & StateProps & DispatchProps

const NamespaceSelect: React.FC<NamespaceSelectProps> = ({
  namespaces,
  selectedNamespace,
  selectNamespace,
}) => {
  const [open, setOpen] = React.useState(false)
  const i18next = require('i18next')

  if (selectedNamespace) {
    return (
      <FormControl
        variant='outlined'
        sx={{
          minWidth: '140px',
          position: 'relative',
        }}
        onClick={() => setOpen(!open)}
      >
        <Box
          sx={{
            position: 'absolute',
            left: '-4px',
            display: 'flex',
            alignItems: 'center',
            height: '100%',
          }}
        >
          <MqText color={theme.palette.primary.main} font={'mono'}>
            {i18next.t('namespace_select.prompt')}
          </MqText>
        </Box>
        <Select
          inputProps={{
            MenuProps: {
              PaperProps: {
                sx: {
                  backgroundImage: 'none',
                },
              },
            },
          }}
          labelId='namespace-label'
          id='namespace-select'
          value={selectedNamespace}
          onChange={(event) => {
            selectNamespace(event.target.value as string)
          }}
          label='Namespace'
          input={<MqInputBase />}
          open={open}
          onClick={() => setOpen(!open)}
          onClose={() => setOpen(false)}
          sx={{ cursor: 'pointer' }}
        >
          {namespaces.map((namespace) => (
            <MenuItem key={namespace.name} value={namespace.name}>
              {namespace.name}
            </MenuItem>
          ))}
        </Select>
      </FormControl>
    )
  } else return null
}

const mapStateToProps = (state: IState) => ({
  namespaces: state.namespaces.result,
  selectedNamespace: state.namespaces.selectedNamespace,
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      selectNamespace: selectNamespace,
    },
    dispatch
  )

export default connect(mapStateToProps, mapDispatchToProps)(NamespaceSelect)
