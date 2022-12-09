// SPDX-License-Identifier: Apache-2.0

import * as React from 'react'
import { mount } from 'enzyme'
import Box from '@material-ui/core/Box'
import Tooltip from '@material-ui/core/Tooltip'
import Typography from '@material-ui/core/Typography'

import { formatUpdatedAt } from '../../helpers'
import JobDetailPage from '../../components/jobs/JobDetailPage'

const jobs = require('../../../docker/db/data/jobs.json')

jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'), // use actual for all non-hook props
  useParams: jest.fn()
}))

import { useParams } from 'react-router-dom'

test.skip('JobDetailPage Component', () => {
  describe('when there is no match for the jobName in url params', () => {
    useParams.mockImplementation(() => ({
      jobName: 'job.nomatch'
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
  })

  // TODO accomodate fetching the latest job run in the test
  test.skip('when there is a match for the jobName in url params', () => {
    /*
      will replace this with imported job_runs.json once Willy is able to add
      seeding step for jobs runs
    */
    const tempJobRuns = [
      {
        runId: '870492da-ecfb-4be0-91b9-9a89ddd3db90',
        createdAt: '2019-05-09T19:49:24.201Z',
        updatedAt: '2019-05-09T19:49:24.201Z',
        nominalStartTime: '2019-05-12T19:49:24.201Z',
        nominalEndTime: '2019-05-12T19:52:24.201Z',
        runState: 'NEW',
        runArgs: {
          email: 'data@domain.com',
          emailOnFailure: false,
          emailOnRetry: true,
          retries: 2
        }
      },
      {
        runId: '870492da-ecfb-4be0-91b9-9a89ddd3db90',
        createdAt: '2019-05-09T19:49:24.201Z',
        updatedAt: '2019-05-09T19:49:24.201Z',
        nominalStartTime: '2019-05-12T19:49:24.201Z',
        nominalEndTime: '2019-05-12T19:52:24.201Z',
        runState: 'FAILED',
        runArgs: {
          email: 'data@domain.com',
          emailOnFailure: false,
          emailOnRetry: true,
          retries: 2
        }
      },
      {
        runId: '870492da-ecfb-4be0-91b9-9a89ddd3db90',
        createdAt: '2019-05-09T19:49:24.201Z',
        updatedAt: '2019-05-09T19:49:24.201Z',
        nominalStartTime: '2019-05-12T19:49:24.201Z',
        nominalEndTime: '2019-05-12T19:52:24.201Z',
        runState: 'COMPLETED',
        runArgs: {
          email: 'data@domain.com',
          emailOnFailure: false,
          emailOnRetry: true,
          retries: 2
        }
      },

      {
        runId: '870492da-ecfb-4be0-91b9-9a89ddd3db92',
        createdAt: '2019-05-09T19:49:24.201Z',
        updatedAt: '2019-05-09T19:49:24.201Z',
        nominalStartTime: '2019-05-12T19:49:24.201Z',
        nominalEndTime: '2019-05-12T19:52:24.201Z',
        runState: 'COMPLETED',
        runArgs: {
          email: 'data@domain.com',
          emailOnFailure: false,
          emailOnRetry: true,
          retries: 2
        }
      },

      {
        runId: '870492da-ecfb-4be0-91b9-9a89ddd3db93',
        createdAt: '2019-05-09T19:49:24.201Z',
        updatedAt: '2019-05-09T19:49:24.201Z',
        nominalStartTime: '2019-05-12T19:49:24.201Z',
        nominalEndTime: '2019-05-12T19:52:24.201Z',
        runState: 'FAILED',
        runArgs: {
          email: 'data@domain.com',
          emailOnFailure: false,
          emailOnRetry: true,
          retries: 2
        }
      },
      {
        runId: '870492da-ecfb-4be0-91b9-9a89ddd3db93',
        createdAt: '2019-05-09T19:49:24.201Z',
        updatedAt: '2019-05-09T19:49:24.201Z',
        nominalStartTime: '2019-05-12T19:49:24.201Z',
        nominalEndTime: '2019-05-12T19:52:24.201Z',
        runState: 'NEW',
        runArgs: {
          email: 'data@domain.com',
          emailOnFailure: false,
          emailOnRetry: true,
          retries: 2
        }
      },
      {
        runId: '870492da-ecfb-4be0-91b9-9a89ddd3db93',
        createdAt: '2019-05-09T19:49:24.201Z',
        updatedAt: '2019-05-09T19:49:24.201Z',
        nominalStartTime: '2019-05-12T19:49:24.201Z',
        nominalEndTime: '2019-05-12T19:52:24.201Z',
        runState: 'ABORTED',
        runArgs: {
          email: 'data@domain.com',
          emailOnFailure: false,
          emailOnRetry: true,
          retries: 2
        }
      },
      {
        runId: '870492da-ecfb-4be0-91b9-9a89ddd3db92',
        createdAt: '2019-05-09T19:49:24.201Z',
        updatedAt: '2019-05-09T19:49:24.201Z',
        nominalStartTime: '2019-05-12T19:49:24.201Z',
        nominalEndTime: '2019-05-12T19:52:24.201Z',
        runState: 'RUNNING',
        runArgs: {
          email: 'data@domain.com',
          emailOnFailure: false,
          emailOnRetry: true,
          retries: 2
        }
      },
      {
        runId: '870492da-ecfb-4be0-91b9-9a89ddd3db91',
        createdAt: '2019-05-09T19:49:24.201Z',
        updatedAt: '2019-05-09T19:49:24.201Z',
        nominalStartTime: '2019-05-12T19:49:24.201Z',
        nominalEndTime: '2019-05-12T19:52:24.201Z',
        runState: 'RUNNING',
        runArgs: {
          email: 'data@domain.com',
          emailOnFailure: false,
          emailOnRetry: true,
          retries: 2
        }
      },
      {
        runId: '870492da-ecfb-4be0-91b9-9a89ddd3db91',
        createdAt: '2019-05-09T19:49:24.201Z',
        updatedAt: '2019-05-09T19:49:24.201Z',
        nominalStartTime: '2019-05-12T19:49:24.201Z',
        nominalEndTime: '2019-05-12T19:52:24.201Z',
        runState: 'ABORTED',
        runArgs: {
          email: 'data@domain.com',
          emailOnFailure: false,
          emailOnRetry: true,
          retries: 2
        }
      }
    ]

    const job = { ...jobs[0], latestRuns: tempJobRuns }

    useParams.mockImplementation(() => ({
      jobName: job.name
    }))

    const wrapper = mount(<JobDetailPage />)

    wrapper.setProps({
      jobs: jobs.map(j => (j.name === job.name ? job : j)),
      fetchJobRuns: () => {}
    })

    it('should render', () => {
      expect(wrapper.exists()).toBe(true)
    })

    it('does not render \'no job\'', () => {
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

    it('should render a tooltip per job run', () => {
      expect(wrapper.find(Tooltip)).toHaveLength(job.latestRuns.length)
    })

    it('if there is no SQL, should render text saying so', () => {
      const job = { ...jobs[0], context: {} }

      useParams.mockImplementation(() => ({
        jobName: job.name
      }))

      const wrapper = mount(<JobDetailPage />)

      wrapper.setProps({
        jobs: jobs.map(j => (j.name === job.name ? job : j)),
        fetchJobRuns: () => {}
      })

      expect(
        wrapper
          .find(Box)
          .at(1) // zero-indexed
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
