// SPDX-License-Identifier: Apache-2.0

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
