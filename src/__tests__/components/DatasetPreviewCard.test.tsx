import { mount } from 'enzyme'
import * as React from 'react'
import fakeTagToBadge from '../../config/__mocks__/tag-to-badge'
import DatasetPreviewCard, { formatUpdatedAt } from '../../components/DatasetPreviewCard'

/*
  Mock the functionality of tagToBadge
  (reads from the __mocks__ folder of src/config)
*/
jest.mock('../../config/tag-to-badge')
const datasets = require('../../../docker/db/data/datasets.json')

describe('formatUpdated Function', () => {
  const updatedAt = ''
  const formatedDate = formatUpdatedAt(updatedAt)
  it('Should return an empty string when passed a falsey value', () => {
    expect(formatedDate).toBe('')
  })
})

describe('DatasetPreviewCard Component', () => {
  const wrapper = mount(<DatasetPreviewCard />)
  it('Should render', () => {
    expect(wrapper.exists()).toBe(true)
  })
  const dataset = datasets[0]
  const tags = ['tag_a', 'tag_b', 'tag_c']
  wrapper.setProps({ ...dataset, tags })
  const componentText = wrapper.render().text()

  it('should render the dataset name', () => {
    expect(componentText).toContain(dataset.name)
  })
  it('should render the description', () => {
    expect(componentText).toContain(dataset.description)
  })
  it('should render the time', () => {
    expect(componentText).toContain(formatUpdatedAt(dataset.updatedAt))
  })
  it('should render a badge per tag that has a corresponding entry in the tagToBadge config', () => {
    const tagIsInFakeConfig = tag => !!fakeTagToBadge[tag]

    expect(wrapper.find('#tagContainer').children()).toHaveLength(
      tags.filter(tagIsInFakeConfig).length
    )
  })
  it('renders a snapshot that matches previous', () => {
    expect(wrapper).toMatchSnapshot()
  })
})
