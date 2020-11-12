import * as React from 'react'
import { MemoryRouter } from 'react-router-dom'
import { formatUpdatedAt } from '../../helpers'
import { mount } from 'enzyme'
import DatasetPreviewCard from '../../components/DatasetPreviewCard'
import MqText from '../../components/core/text/MqText'

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
    expect(componentText).toContain("There is no description available for this dataset")
  })
  it('should render the time', () => {
    expect(
      wrapper
        .find(MqText)
        .at(1)
        .text()
    ).toContain(formatUpdatedAt(dataset.updatedAt))
  })
})
