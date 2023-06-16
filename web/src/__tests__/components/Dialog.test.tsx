// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import * as React from 'react'
import Button from '@mui/material/Button'
import Dialog from '../../components/Dialog'
import { render, screen } from '@testing-library/react'
import * as actionTypes from '../../store/actionCreators/actionTypes'

describe('Dialog Component', () => {

  const ignoreWarning = () => {

  }

  const dialogToggle = (field: string) => ({
    type: actionTypes.DIALOG_TOGGLE,
    payload: {
      field: 'Description of dialog...'
    }
  })

  const mockProps = {
    dialogIsOpen: true,
    dialogToggle: dialogToggle,
    ignoreWarning: ignoreWarning,
    editWarningField: 'Description of dialog...'
  }

  render(<Dialog {...mockProps} />)

  it('should render two buttons on the dialog', () => {
    expect(screen.getAllByRole('button')).toHaveLength(2)
  })

  it('renders a snapshot that matches previous', () => {
    expect(screen).toMatchSnapshot()
  })
})
