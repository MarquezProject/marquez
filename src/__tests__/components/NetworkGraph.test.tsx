import { mount } from 'enzyme'
import * as React from 'react'
import { NetworkGraph } from '../../components/NetworkGraph'
const jobs = require('../../../docker/db/data/jobs.json')
const datasets = require('../../../docker/db/data/datasets.json')

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
