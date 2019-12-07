import { mount } from 'enzyme'
import * as React from 'react'
import Typography from '@material-ui/core/Typography'
import Box from '@material-ui/core/Box'

import JobDetailPage from '../../components/JobDetailPage'
import { formatUpdatedAt } from '../../helpers'

const jobs = require('../../../docker/db/data/jobs.json')

jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'), // use actual for all non-hook parts
  useParams: jest.fn()
}))
import { useParams } from 'react-router-dom'

describe('JobDetailPage Component', () => {
  describe('when there is no match for the jobName in url params', () => {
    useParams.mockImplementation(() => ({
      jobName: 'test.job'
    }))

    const wrapper = mount(<JobDetailPage />)

    it('should render', () => {
      expect(wrapper.exists()).toBe(true)
    })
    it('should render text explaning that there was no matching job found', () => {
      expect(
        wrapper
          .find(Typography)
          .text()
          .toLowerCase()
      ).toContain('no job')
    })
    it('renders a snapshot that matches previous', () => {
      expect(wrapper).toMatchSnapshot()
    })

    describe('when there is a match for the jobName in url params', () => {
      const job = jobs[0]

      useParams.mockImplementation(() => ({
        jobName: job.name
      }))

      const wrapper = mount(<JobDetailPage />)

      wrapper.setProps({ jobs })

      it('should render', () => {
        expect(wrapper.exists()).toBe(true)
      })
      it(`does not render 'no job'`, () => {
        expect(
          wrapper.findWhere(n =>
            n
              .text()
              .toLowerCase()
              .includes('no job')
          )
        ).toHaveLength(0)
      })

      it('should render the job name', () => {
        expect(
          wrapper
            .find(Typography)
            .first()
            .text()
        ).toContain(job.name)
      })
      it('job name should contain a link to the job description', () => {
        expect(
          wrapper
            .find(Typography)
            .first()
            .find('a')
            .first()
            .html()
        ).toContain(job.location)
      })
      it('should render the job description', () => {
        expect(
          wrapper
            .find(Typography)
            .at(1) // zero-indexed
            .text()
        ).toContain(job.description)
      })
      it('should render the job time', () => {
        expect(
          wrapper
            .find(Typography)
            .at(3) // zero-indexed
            .text()
        ).toContain(formatUpdatedAt(job.updatedAt))
      })

      it('if there is no SQL, should render text saying so', () => {
        const job = { ...jobs[0], context: {} }

        useParams.mockImplementation(() => ({
          jobName: job.name
        }))

        const wrapper = mount(<JobDetailPage />)

        wrapper.setProps({ jobs: jobs.map(j => (j.name === job.name ? job : j)) })

        expect(
          wrapper
            .find(Box)
            .last()
            .find(Typography)
            .first()
            .text()
        ).toContain('no SQL')
      })
      it('renders a snapshot that matches previous', () => {
        expect(wrapper).toMatchSnapshot()
      })
    })
  })
})
//

//
// const componentText = wrapper.render().text()
