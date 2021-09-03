import { Theme, createStyles, fade, withStyles } from '@material-ui/core'
import InputBase from '@material-ui/core/InputBase'

export const SearchBase = withStyles((theme: Theme) =>
  createStyles({
    input: {
      borderRadius: theme.spacing(4),
      position: 'relative',
      backgroundColor: 'transparent',
      border: `2px solid ${theme.palette.common.white}`,
      fontSize: 16,
      padding: `${theme.spacing(1)}px ${theme.spacing(5)}px`,
      transition: theme.transitions.create(['border-color', 'box-shadow']),
      '&:focus': {
        borderColor: theme.palette.primary.main,
        boxShadow: `${fade(theme.palette.primary.main, 0.25)} 0 0 0 3px`
      }
    }
  })
)(InputBase)
