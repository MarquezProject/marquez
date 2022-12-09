// SPDX-License-Identifier: Apache-2.0

import * as React from 'react'
import { shallow } from 'enzyme'
import Button from '@material-ui/core/Button'
import Dialog from '../../components/Dialog'

describe('Dialog Component', () => {

  const mockProps = {
    dialogIsOpen: true,
    dialogToggle: dialogToggle,
    ignoreWarning: ignoreWarning
  }

  const ignoreWarning = () => {
    return true
  }

  const dialogToggle = () => {
    return true
  }

  const wrapper = shallow(<Dialog {...mockProps} />)

  it('should render two buttons on the dialog', () => {
    expect(wrapper.find(Button)).toHaveLength(2)
  })

  it('renders a snapshot that matches previous', () => {
    expect(wrapper).toMatchSnapshot()
  })

})
