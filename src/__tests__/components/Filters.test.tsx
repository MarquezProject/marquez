import { shallow } from 'enzyme'
import * as React from 'react'
import Filters from '../../components/filters/Filters'
import Select from '@material-ui/core/Select'
const namespaces = require('../../../docker/db/data/namespaces.json')

describe('Filters component', () => {
  const mockProps = {
    datasets: [],
    jobs: [],
    namespaces,
    filterDatasets: () => {},
    filterJobs: () => {}
  }

  const wrapper = shallow(<Filters {...mockProps} />)

  it('should render only one Select component on initial render', () => {
    expect(wrapper.find(Select)).toHaveLength(1)
  })

  test.skip('should render second Select component after onChange event for first Select', () => {
    const clickEvent = { target: { value: 'namespace' } }
    wrapper.find(Select).simulate('change', clickEvent)
    expect(wrapper.find(Select)).toHaveLength(2)
  })

  it('renders a snapshot that matches previous', () => {
    expect(wrapper).toMatchSnapshot()
  })
})
