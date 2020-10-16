import * as React from 'react'
import { shallow } from 'enzyme'
import Toast from '../../components/Toast'

describe('MainContainer Component', () => {
  it('does not render when there is no error', () => {
    const wrapper = shallow(<Toast error='' />)
    expect(wrapper.find('div')).toHaveLength(0)
  })

  it('renders when there is an error', () => {
    const wrapper = shallow(<Toast error='Some error text' />)
    expect(wrapper.find('div')).toHaveLength(1)
  })  

  it('renders snapshots, too', () => {
    const wrapper = shallow(<Toast error='' />)
    expect(wrapper).toMatchSnapshot()
  })
})
