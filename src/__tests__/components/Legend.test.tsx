import { mount } from 'enzyme'
import * as React from 'react'

import Legend from '../../components/Legend'

describe('Legend Component Tests', () => {
  const wrapper = mount(<Legend customClassName='' />)
  it('Should render', () => {
    expect(wrapper.exists()).toBe(true)
  })
  const componentText = wrapper.render().text()
  it("Should render 'datasets' key", () => {
    expect(componentText).toContain('datasets')
  })
  it("Should render 'jobs' key", () => {
    expect(componentText).toContain('jobs')
  })
})
