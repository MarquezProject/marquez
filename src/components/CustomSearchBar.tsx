import React, { FunctionComponent, useState } from 'react'
import SearchBar from 'material-ui-search-bar'
import { findMatchingEntities } from '../actionCreators'
import {
  withStyles,
  createStyles,
  WithStyles as IWithStyles,
  Theme as ITheme
} from '@material-ui/core/styles'

import { useHistory } from 'react-router-dom'
import * as Redux from 'redux'
import {bindActionCreators} from 'redux'
import {connect} from 'react-redux'

interface IProps {
  findMatchingEntities: typeof findMatchingEntities
  setShowJobs: (bool: boolean) => void
  showJobs: boolean
}

type IAllProps = IWithStyles<typeof styles> & IProps

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

const CustomSearchBar: FunctionComponent<IAllProps> = props => {
  const [search, setSearch] = useState('')
  const { classes } = props
  const history = useHistory()

  const searchChanged = (searchString: string) => {
    setSearch(searchString)
    props.findMatchingEntities(searchString)
    searchString == '' ? props.setShowJobs(false) : props.setShowJobs(true)
  }

  const cancelledSearch = () => {
    setSearch('')
    searchChanged('')
  }

  const onRequestSearch = () => {
    history.push('/')
  }

  return (
    <SearchBar
      className={classes.search}
      value={search}
      onChange={searchChanged}
      onCancelSearch={cancelledSearch}
      onRequestSearch={onRequestSearch}
    />
  )
}

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators({ findMatchingEntities: findMatchingEntities }, dispatch)

export default connect(
  null,
  mapDispatchToProps
)(withStyles(styles)(CustomSearchBar))
