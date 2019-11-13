import { mount, shallow } from 'enzyme'
import * as React from 'react'
// import Typography from '@material-ui/core/Typography'

import CustomSearchBar from '../../components/CustomSearchBar'

describe('CustomSearchBar Component', () => {
  const wrapper = shallow(<CustomSearchBar />)
  it('Should render', () => {
    expect(wrapper.exists()).toBe(true)
  })
  it('renders a snapshot that matches previous', () => {
    expect(wrapper).toMatchSnapshot()
  })
})
