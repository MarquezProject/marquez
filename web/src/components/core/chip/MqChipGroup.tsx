// SPDX-License-Identifier: Apache-2.0

import React from 'react'

import { IconDefinition } from '@fortawesome/fontawesome-svg-core'
import { Theme, WithStyles, createStyles } from '@material-ui/core'
import { Undefinable } from '../../../types/util/Nullable'
import Box from '@material-ui/core/Box'
import MqChip from './MqChip'
import withStyles from '@material-ui/core/styles/withStyles'

const styles = (theme: Theme) =>
  createStyles({
    root: {
      border: `1px solid ${theme.palette.primary.main}`,
      backgroundColor: theme.palette.background.paper,
      borderRadius: theme.spacing(2),
      display: 'inline-block',
      padding: '1px'
    }
  })

interface OwnProps {
  chips: {
    icon?: IconDefinition
    foregroundColor?: string
    backgroundColor?: string
    text?: string
    value: string
    selectable?: boolean
  }[]
  initialSelection: string
  onSelect: (label: string) => void
}

interface StateProps {
  selected: Undefinable<string>
}

type MqChipGroupProps = WithStyles<typeof styles> & OwnProps

/**
 * This functions as a standard button group and wraps the <Chip /> component with
 * selection logic and callbacks needed to manage and change state
 */
class MqChipGroup extends React.Component<MqChipGroupProps, StateProps> {
  constructor(props: MqChipGroupProps) {
    super(props)
    this.state = {
      selected: props.initialSelection
    }
  }

  onSelect = (label: string) => {
    this.setState(
      {
        selected: label
      },
      () => {
        this.props.onSelect(label)
      }
    )
  }

  render() {
    const { classes, chips } = this.props
    return (
      <Box className={classes.root}>
        {chips.map(chip => (
          <MqChip
            {...chip}
            onSelect={this.onSelect}
            selected={this.state.selected === chip.value}
            key={chip.value}
            value={chip.value}
          />
        ))}
      </Box>
    )
  }
}

export default withStyles(styles)(MqChipGroup)
