import { mount } from 'enzyme'
import * as React from 'react'
import Typography from '@material-ui/core/Typography'
import DatasetPreviewCard from '../../components/DatasetPreviewCard'
import { formatUpdatedAt } from '../../helpers'
import { MemoryRouter } from 'react-router-dom'

const datasets = require('../../../docker/db/data/datasets.json')
const dataset = datasets[0]
const tags = ['TAG_A', 'TAG_B', 'TAG_C']
dataset.tags = tags

describe('DatasetPreviewCard Component', () => {
  const wrapper = mount(<MemoryRouter><DatasetPreviewCard { ...dataset }/></MemoryRouter>)
  it('Should render', () => {
    expect(wrapper.exists()).toBe(true)
  })
  
  const componentText = wrapper.render().text()

  it('should render the dataset name', () => {
    expect(componentText).toContain(dataset.name)
  })
  it('should render the description', () => {
    expect(componentText).toContain(dataset.description)
  })
  it('should render the time', () => {
    expect(
      wrapper
        .find(Typography)
        .last()
        .text()
    ).toContain(formatUpdatedAt(dataset.updatedAt))
  })
  // wrapping in Router produces a new key each time, which makes the snapshots not match
  test.skip('renders a snapshot that matches previous', () => {
    expect(wrapper).toMatchSnapshot()
  })
})
