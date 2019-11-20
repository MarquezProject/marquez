import { mount } from 'enzyme'
import * as React from 'react'
import Typography from '@material-ui/core/Typography'
import tagToBadge from '../../config/tag-to-badge'
import DatasetPreviewCard from '../../components/DatasetPreviewCard'
import { formatUpdatedAt } from '../../helpers'

jest.mock('../../config/tag-to-badge') // https://jestjs.io/docs/en/manual-mocks
const datasets = require('../../../docker/db/data/datasets.json')

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
  it('renders a snapshot that matches previous', () => {
    expect(wrapper).toMatchSnapshot()
  })
})
