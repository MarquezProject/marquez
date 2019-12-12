import { mount } from 'enzyme'
import * as React from 'react'
import Typography from '@material-ui/core/Typography'
import tagToBadge from '../../config/tag-to-badge'
import DatasetPreviewCard from '../../components/DatasetPreviewCard'
import { formatUpdatedAt } from '../../helpers'
import { MemoryRouter } from 'react-router-dom'

jest.mock('../../config/tag-to-badge') // https://jestjs.io/docs/en/manual-mocks
const datasets = require('../../../docker/db/data/datasets.json')
const dataset = datasets[0]
const tags = ['tag_a', 'tag_b', 'tag_c']
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
  it('should render a highlighted badge per matching tag in config', () => {
    const tagIsInFakeConfig = tag => !!tagToBadge.default[tag]
    expect(
      wrapper
        .find('.tagWrapper')
        .children()
        .filterWhere(item => item.prop('color') == 'highlighted')
    ).toHaveLength(tags.filter(tagIsInFakeConfig).length)
  })
  // wrapping in Router produces a new key each time, which makes the snapshots not match
  test.skip('renders a snapshot that matches previous', () => {
    expect(wrapper).toMatchSnapshot()
  })
})
