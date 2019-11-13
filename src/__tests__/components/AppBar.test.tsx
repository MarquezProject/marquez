import { mount } from 'enzyme'
import * as React from 'react'
import AppBar from '../../components/AppBar'

describe('AppBar Test', () => {
  const wrapper = mount(<AppBar />)
  it('Should render', () => {
    expect(wrapper.exists()).toBe(true)
  })

  const componentText = wrapper.render().text()

  it('should render the dataset name', () => {
    expect(componentText).toContain('MARQUEZ')
  })
})
