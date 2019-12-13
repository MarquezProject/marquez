import { mount } from 'enzyme'
import * as React from 'react'
import Typography from '@material-ui/core/Typography'
import Modal from '@material-ui/core/Modal'
import Box from '@material-ui/core/Box'
import OpenWithSharpIcon from '@material-ui/icons/OpenWithSharp'

import JobDetailPage from '../../components/JobDetailPage'
import { formatUpdatedAt } from '../../helpers'

const jobs = require('../../../docker/db/data/jobs.json')

jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'), // use actual for all non-hook parts
  useParams: jest.fn(),
  useHistory: () => ({
    push: jest.fn()
  })
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
    it('should render text explaining that there was no matching job found', () => {
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
      it('job name should contain a link to the job location', () => {
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

      describe('if there is SQL to render', () => {
        it('should render a Modal', () => {
          expect(wrapper.find(Modal)).toHaveLength(1)
        })
        it(`should render an 'expand' button`, () => {
          expect(wrapper.find(OpenWithSharpIcon)).toHaveLength(1)
        })
        it('renders a snapshot that matches previous', () => {
          expect(wrapper).toMatchSnapshot()
        })
      })

      describe('if there is no SQL', () => {
        const job = { ...jobs[0], context: {} }
        useParams.mockImplementation(() => ({
          jobName: job.name
        }))

        const wrapper = mount(<JobDetailPage />)

        wrapper.setProps({ jobs: jobs.map(j => (j.name === job.name ? job : j)) })

        it('should render text saying so', () => {
          expect(
            wrapper
              .find(Box)
              .last()
              .find(Typography)
              .last()
              .text()
          ).toContain('no SQL')
        })

        it('should not render a Modal', () => {
          expect(wrapper.find(Modal)).toHaveLength(0)
        })
        it(`should not render an 'expand' button`, () => {
          expect(wrapper.find(OpenWithSharpIcon)).toHaveLength(0)
        })
      })
    })
  })
})
