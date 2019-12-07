import * as React from 'react'
import SearchBar from 'material-ui-search-bar'
import { findMatchingEntities } from '../actionCreators'
import {
  withStyles,
  createStyles,
  WithStyles as IWithStyles,
  Theme as ITheme
} from '@material-ui/core/styles'

interface IProps {
  findMatchingEntities: typeof findMatchingEntities
  setShowJobs: (bool: boolean) => void
  showJobs: boolean
}

type IAllProps = IWithStyles<typeof styles> & IProps

interface IState {
  value: string
}

const styles = (_theme: ITheme) => {
  return createStyles({
    search: {
      position: 'fixed',
      bottom: '52vh',
      width: '90%',
      left: '5%',
      zIndex: 4
    }
  })
}

class CustomSearchBar extends React.Component<IAllProps, IState> {
  constructor(props: IAllProps) {
    super(props)
    this.state = { value: '' }
  }

  searchChanged = (searchString: string) => {
    this.setState({ value: searchString })
    this.props.findMatchingEntities(searchString)
    searchString == '' ? this.props.setShowJobs(false) : this.props.setShowJobs(true)
  }

  cancelledSearch = () => {
    this.setState({ value: '' })
    this.searchChanged('')
  }

  render(): React.ReactElement {
    const { classes } = this.props
    return (
      <SearchBar
        className={classes.search}
        value={this.state.value}
        onChange={this.searchChanged}
        onCancelSearch={this.cancelledSearch}
      />
    )
  }
}

export default withStyles(styles)(CustomSearchBar)
