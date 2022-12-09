// SPDX-License-Identifier: Apache-2.0

import * as Redux from 'redux'
import { Box, FormControl, MenuItem, Select, WithStyles, createStyles } from '@material-ui/core'
import { IState } from '../../store/reducers'
import { MqInputBase } from '../core/input-base/MqInputBase'
import { Namespace } from '../../types/api'
import { Nullable } from '../../types/util/Nullable'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { selectNamespace } from '../../store/actionCreators'
import { theme } from '../../helpers/theme'
import MqText from '../core/text/MqText'
import React from 'react'
import withStyles from '@material-ui/core/styles/withStyles'

const styles = () =>
  createStyles({
    formControl: {
      minWidth: '140px'
    }
  })

interface OwnProps {}

interface StateProps {
  namespaces: Namespace[]
  selectedNamespace: Nullable<string>
}

interface DispatchProps {
  selectNamespace: typeof selectNamespace
}

type NamespaceSelectProps = WithStyles<typeof styles> & OwnProps & StateProps & DispatchProps

class NamespaceSelect extends React.Component<NamespaceSelectProps, StateProps> {
  render() {
    const { classes, namespaces, selectedNamespace } = this.props
    if (selectedNamespace) {
      return (
        <FormControl variant='outlined' className={classes.formControl}>
          <Box position={'relative'}>
            <Box position={'absolute'} left={12} top={9}>
              <MqText color={theme.palette.primary.main} font={'mono'}>
                ns
              </MqText>
            </Box>
          </Box>
          <Select
            labelId='namespace-label'
            id='namespace-select'
            value={selectedNamespace}
            onChange={event => {
              this.props.selectNamespace(event.target.value as string)
            }}
            label='Namespace'
            input={<MqInputBase />}
          >
            {namespaces.map(namespace => (
              <MenuItem key={namespace.name} value={namespace.name}>
                {namespace.name}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
      )
    } else return null
  }
}

const mapStateToProps = (state: IState) => ({
  namespaces: state.namespaces.result,
  selectedNamespace: state.namespaces.selectedNamespace
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      selectNamespace: selectNamespace
    },
    dispatch
  )

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(withStyles(styles)(NamespaceSelect))
