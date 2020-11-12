import * as Redux from 'redux'
import { Theme, WithStyles, createStyles, fade } from '@material-ui/core/styles'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { findMatchingEntities } from '../../actionCreators'
import InputBase from '@material-ui/core/InputBase'
import React from 'react'
import SearchIcon from '@material-ui/icons/Search'
import withStyles from '@material-ui/core/styles/withStyles'

const styles = (theme: Theme) => {
  return createStyles({
    search: {
      position: 'relative',
      transition: theme.transitions.create(['background-color']),
      backgroundColor: fade(theme.palette.common.white, 0.15),
      '&:hover': {
        backgroundColor: fade(theme.palette.common.white, 0.25)
      },
      marginRight: theme.spacing(2),
      marginLeft: 0,
      width: '100%',
      [theme.breakpoints.up('sm')]: {
        marginLeft: theme.spacing(3),
        width: 'auto'
      },
      borderRadius: theme.spacing(3),
      border: `1px solid ${theme.palette.secondary.main}`
    },
    searchIcon: {
      padding: theme.spacing(0, 2),
      height: '100%',
      position: 'absolute',
      pointerEvents: 'none',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center'
    },
    inputRoot: {
      color: 'inherit'
    },
    inputInput: {
      padding: theme.spacing(1, 1, 1, 0),
      // vertical padding + font size from searchIcon
      paddingLeft: `calc(1em + ${theme.spacing(4)}px)`,
      width: '100%',
      fontFamily: 'Source Code Pro',
      [theme.breakpoints.up('md')]: {
        width: '30ch'
      }
    }
  })
}

interface OwnProps {
  setShowJobs: (bool: boolean) => void
  showJobs: boolean
}

interface SearchBarState {
  search: string
}

interface DispatchProps {
  findMatchingEntities: typeof findMatchingEntities
}

type SearchBarProps = OwnProps & DispatchProps & WithStyles<typeof styles>

class SearchBar extends React.Component<SearchBarProps, SearchBarState> {
  constructor(props: SearchBarProps) {
    super(props)
    this.state = {
      search: ''
    }
  }

  onSearch = (event: React.ChangeEvent<HTMLInputElement>) => {
    this.setState({ search: event.target.value }, () => {
      this.props.setShowJobs(this.state.search !== '')
      this.props.findMatchingEntities(this.state.search)
    })
  }

  render() {
    const { classes } = this.props
    return (
      <div className={classes.search}>
        <div className={classes.searchIcon}>
          <SearchIcon />
        </div>
        <InputBase
          placeholder='Search…'
          classes={{
            root: classes.inputRoot,
            input: classes.inputInput
          }}
          onChange={this.onSearch}
          inputProps={{ 'aria-label': 'search' }}
        />
      </div>
    )
  }
}

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators({ findMatchingEntities: findMatchingEntities }, dispatch)

export default connect(
  null,
  mapDispatchToProps
)(withStyles(styles)(withStyles(styles)(SearchBar)))
