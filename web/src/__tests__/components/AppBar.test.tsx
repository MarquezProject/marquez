// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

describe('AppBar Test', () => {
  // TODO: There's an issue with rendering this component in jest

  // render(<MemoryRouter><AppBar /></MemoryRouter>)
  test.skip('Should render', () => {
    expect(screen).toBeInTheDocument()
  })

  // const componentText = screen.render().text()
  test.skip('should render the dataset name', () => {
    expect(componentText).toContain('MARQUEZ')
  })
})
