import * as React from 'react'
import { mount } from 'enzyme'
import Datasets from '../../routes/datasets/Datasets'
import { Tooltip } from '@material-ui/core'
import IconButton from '@material-ui/core/IconButton'

test.skip('DatasetsPagination Component', () => {

    const wrapper = mount(<Datasets />)

    it('should render', () => {
        expect(wrapper.exists()).toBe(true)
    })

    it('should find Tooltip elements containing IconButton elements', () => {
        expect(
          wrapper
            .find(Tooltip)
        ).toContain(IconButton)
    })
})