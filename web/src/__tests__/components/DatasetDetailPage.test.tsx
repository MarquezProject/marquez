// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import * as React from 'react'
import { render, screen } from '@testing-library/react';

import { formatUpdatedAt } from '../../helpers'
import DatasetDetailPage from '../../components/datasets/DatasetDetailPage'

const datasets = require('../../../docker/db/data/datasets.json')
const dataset = datasets[0]

jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'), // use actual for all non-hook parts
  useParams: jest.fn(),
  useNavigate: () => ({
    push: jest.fn()
  })
}))

import { useParams } from 'react-router-dom'

test.skip('DatasetDetailPage Component', () => {

  describe('when there is no match for the datasetName in url params', () => {
    // useParams.mockImplementation(() => ({
    //   datasetName: 'test.dataset'
    // }))

    render(<DatasetDetailPage />)
    screen.setProps({ datasets })

    it('should render', () => {
      expect(screen).toBeInTheDocument()
    })
    it('should render text explaning that there was no matching dataset found', () => {
      expect(
        screen.getByRole('Typography').innerText.toLocaleLowerCase()
      ).toHaveTextContent('no dataset')
    })
    it('renders a snapshot that matches previous', () => {
      expect(screen).toMatchSnapshot()
    })
  })

  describe('when there is a match for the datasetName in url params', () => {
    useParams.mockImplementation(() => ({
      datasetName: dataset.name
    }))
    render(<DatasetDetailPage />)
    screen.setProps({ datasets })

    it('should render', () => {
      expect(screen).toBeInTheDocument()
    })
    it('does not render \'no dataset\'', () => {
      expect(
        screen.getAllByText('no dataset', { exact: false })
      ).toHaveLength(0)
    })
    it('should render the dataset name', () => {
      expect(
        screen.getByRole('Typography').innerHTML
      ).toContain(dataset.name)
    })
    it('should render the dataset description', () => {
      expect(
        screen.getAllByRole('Typography')[1].innerText
      ).toContain(dataset.description)
    })
    it('should render the dataset time', () => {
      expect(
        screen.getAllByRole('Typography')[2].innerText
      ).toContain(formatUpdatedAt(dataset.updatedAt))
    })
    it('renders a snapshot that matches previous', () => {
      expect(screen).toMatchSnapshot()
    })
  })
})
