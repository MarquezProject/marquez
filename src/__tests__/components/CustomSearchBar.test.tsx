import * as React from 'react'
import { shallow } from 'enzyme'

import CustomSearchBar from '../../components/CustomSearchBar'

describe('CustomSearchBar Component', () => {
const wrapper = shallow(<CustomSearchBar />)
  it('Should render', () => {
    expect(wrapper.exists()).toBe(true)
  })
  it('renders a snapshot that matches previous', () => {
    expect(wrapper).toMatchSnapshot()
  })

  // TODO figure out how to set state of search bar
  // wrapper.setState({search: 'test'})

  // it('should render text explaning that there was no matching dataset found', () => {
  //   expect(
  //     wrapper
  //       .find(Typography)
  //       .text()
  //       .toLowerCase()
  //   ).toContain('test')
  // })
})
