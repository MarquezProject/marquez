import { shallow } from 'enzyme'
import * as React from 'react'

import JobPreviewCard from '../../components/JobPreviewCard'
import { formatUpdatedAt } from '../../components/DatasetPreviewCard'

const jobs = require('../../../docker/db/data/jobs.json')

describe('formatUpdated Function', () => {
  const updatedAt = ''
  const formattedDate = formatUpdatedAt(updatedAt)
  it('Should return an empty string when passed a falsey value', () => {
    expect(formattedDate).toBe('')
  })
})

describe('JobPreviewCard Component', () => {
  const wrapper = shallow(<JobPreviewCard />)
  it('Should render', () => {
    expect(wrapper.exists()).toBe(true)
  })

  const job = jobs[0]

  wrapper.setProps(job)
  const componentText = wrapper.render().text()

  it('should render the job name', () => {
    expect(componentText).toContain(job.name)
  })
  it('should render the job description', () => {
    expect(componentText).toContain(job.description)
  })
  it('should render the job time', () => {
    expect(componentText).toContain(formatUpdatedAt(job.updatedAt))
  })
  test.skip('should render the job status', () => {
    expect(componentText).toContain(job.status)
  })
  it('renders a snapshot that matches previous', () => {
    expect(wrapper).toMatchSnapshot()
  })
})
