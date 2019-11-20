import { mount } from 'enzyme'
import * as React from 'react'
import Loader from '../../components/Loader'

describe('Loader Component', () => {
  const wrapper = mount(<Loader />)

  it('renders a snapshot that matches previous', () => {
    expect(wrapper).toMatchSnapshot()
  })
})
