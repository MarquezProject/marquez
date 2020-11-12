import * as React from 'react'
import { shallow } from 'enzyme'
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

})
