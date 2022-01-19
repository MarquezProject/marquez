// SPDX-License-Identifier: Apache-2.0

import { IconDefinition } from '@fortawesome/fontawesome-svg-core'
import { Theme, createStyles } from '@material-ui/core'
import Box from '@material-ui/core/Box'
import MqSmallIcon from '../small-icon/MqSmallIcon'
import MqText from '../text/MqText'
import React from 'react'
import classNames from 'classnames'
import withStyles, { WithStyles } from '@material-ui/core/styles/withStyles'

interface MqChipProps {
  selected?: boolean
  onSelect?: (label: string) => void
  icon?: IconDefinition
  foregroundColor?: string
  backgroundColor?: string
  text?: string
  value: string
  selectable?: boolean
}

const styles = (theme: Theme) =>
  createStyles({
    root: {
      display: 'inline-block',
      borderRadius: theme.spacing(2),
      padding: '2px 8px',
      cursor: 'pointer',
      userSelect: 'none'
    },
    icon: {
      display: 'inline'
    },
    selected: {
      boxShadow: `0 0 2px 3px ${theme.palette.common.white}`
    }
  })

/**
 * This is a simple button that can be either selected or unselected. Is is configurable with icons and/or text
 */
const MqChip: React.FC<MqChipProps & WithStyles<typeof styles>> = ({
  selected,
  onSelect,
  icon,
  text,
  value,
  classes,
  foregroundColor,
  backgroundColor,
  selectable
}) => {
  return (
    <Box
      id={`chip-${value}`}
      className={classNames(classes.root, selected && classes.selected)}
      onClick={() => {
        if (selectable !== false && onSelect) {
          onSelect(value)
        }
      }}
    >
      {icon && foregroundColor && backgroundColor && (
        <Box display={'inherit'}>
          <MqSmallIcon
            icon={icon}
            shape={'rect'}
            foregroundColor={foregroundColor}
            backgroundColor={backgroundColor}
          />
        </Box>
      )}
      {text && (
        <Box ml={icon ? 1 : 0} display={'inline'}>
          <MqText inline>{text}</MqText>{' '}
        </Box>
      )}
    </Box>
  )
}

export default withStyles(styles)(MqChip)
