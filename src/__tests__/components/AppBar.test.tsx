import { mount } from 'enzyme'
import * as React from 'react'
import AppBar from '../../components/AppBar'
import { MemoryRouter } from 'react-router-dom'

describe('AppBar Test', () => {
  // TODO: There's an issue with rendering this component in jest

  // const wrapper = mount(<MemoryRouter><AppBar /></MemoryRouter>)
  test.skip('Should render', () => {
    expect(wrapper.exists()).toBe(true)
  })

  // const componentText = wrapper.render().text()
  test.skip('should render the dataset name', () => {
    expect(componentText).toContain('MARQUEZ')
  })  
})
