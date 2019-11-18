import * as React from 'react'
import SearchBar from 'material-ui-search-bar'
import { findMatchingEntities } from '../actionCreators'

interface IProps {
  findMatchingEntities: typeof findMatchingEntities
  customClassName: string
  showJobs: (bool: boolean) => void
}

interface IState {
  value: string
}

class CustomSearchBar extends React.Component<IProps, IState> {
  constructor(props: IProps) {
    super(props)
    this.state = { value: '' }
  }

  searchChanged = (searchString: string) => {
    this.setState({ value: searchString })
    this.props.findMatchingEntities(searchString)
    searchString == '' ? this.props.showJobs(false) : this.props.showJobs(true)
  }

  cancelledSearch = () => {
    this.setState({ value: '' })
    this.searchChanged('')
  }

  render(): React.ReactElement {
    return (
      <SearchBar
        className={this.props.customClassName}
        value={this.state.value}
        onChange={this.searchChanged}
        onCancelSearch={this.cancelledSearch}
      />
    )
  }
}

export default CustomSearchBar
