describe('NetworkGraph Component', () => {
  // TODO: There's an issue with rendering this component in jest

  //const wrapper = mount(<NetworkGraph jobs={jobs} datasets={datasets} classes={{}} />)
  test.skip('Should render', () => {
    expect(wrapper.exists()).toBe(true)
  })
  test.skip('renders a snapshot that matches previous', () => {
    expect(wrapper).toMatchSnapshot()
  })
  test.skip('renders Loader if isLoading is true', () => {
    // to-do
  })
})
