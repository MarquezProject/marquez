// SPDX-License-Identifier: Apache-2.0

import React, { ReactElement } from 'react'

import { Link } from 'react-router-dom'
import { THEME_EXTRA } from '../../../helpers/theme'
import { Theme, WithStyles, createStyles } from '@material-ui/core'
import { alpha } from '@material-ui/core/styles'
import classNames from 'classnames'
import withStyles from '@material-ui/core/styles/withStyles'

const styles = (theme: Theme) =>
  createStyles({
    root: {
      lineHeight: 1.5,
      fontSize: '.875rem',
      fontFamily: `${'Karla'}, sans-serif`,
      margin: 0,
      padding: 0,
      color: theme.palette.common.white,
      fontWeight: 400
    },
    inline: {
      display: 'inline'
    },
    heading: {
      fontWeight: 700,
      fontSize: '1.125rem'
    },
    subheading: {
      fontWeight: 700,
      fontSize: '.875rem'
    },
    mono: {
      fontFamily: `${'Source Code Pro'}, serif`
    },
    bold: {
      fontWeight: 700
    },
    subdued: {
      color: THEME_EXTRA.typography.subdued
    },
    disabled: {
      color: THEME_EXTRA.typography.disabled
    },
    link: {
      color: theme.palette.primary.main,
      cursor: 'pointer',
      textDecoration: 'none',
      '&:hover': {
        textDecoration: 'underline'
      }
    },
    label: {
      fontSize: '.625rem',
      textTransform: 'uppercase',
      lineHeight: 1.4
    },
    highlight: {
      backgroundColor: alpha(theme.palette.primary.main, 0.5)
    },
    bottomMargin: {
      marginBottom: theme.spacing(1)
    },
    inverse: {
      color: theme.palette.common.white
    },
    small: {
      fontSize: '.625rem'
    },
    paragraph: {
      marginBottom: theme.spacing(2)
    }
  })

interface OwnProps {
  heading?: boolean
  subheading?: boolean
  bold?: boolean
  disabled?: boolean
  subdued?: boolean
  label?: boolean
  inline?: boolean
  inverse?: boolean
  highlight?: boolean
  paragraph?: boolean
  color?: string
  link?: boolean
  href?: string
  linkTo?: string
  font?: 'primary' | 'mono'
  small?: boolean
  bottomMargin?: boolean
  children: ReactElement | (string | ReactElement)[] | string | string[] | number | undefined | null
}

type MqTextProps = WithStyles<typeof styles> & OwnProps

class MqText extends React.Component<MqTextProps> {
  render() {
    const {
      classes,
      heading,
      subheading,
      bold,
      disabled,
      label,
      font,
      bottomMargin,
      subdued,
      children,
      link,
      linkTo,
      paragraph,
      href,
      inverse,
      inline,
      highlight,
      color,
      small
    } = this.props

    const conditionalClasses = classNames(
      subdued && classes.subdued,
      bold && classes.bold,
      label && classes.label,
      disabled && classes.disabled,
      highlight && classes.highlight,
      font === 'mono' && classes.mono,
      bottomMargin && classes.bottomMargin,
      inverse && classes.inverse,
      inline && classes.inline,
      small && classes.small,
      link && classes.link,
      paragraph && classes.paragraph,
      subheading && classes.subheading
    )

    const style = {
      color: color && color
    }

    if (heading) {
      return (
        <h4 className={classNames(classes.root, classes.heading, conditionalClasses)} style={style}>
          {children}
        </h4>
      )
    } else if (link && linkTo) {
      return (
        <Link
          to={linkTo}
          aria-disabled={disabled}
          className={classNames(classes.root, classes.link, conditionalClasses)}
        >
          {children}
        </Link>
      )
    } else if (link && href) {
      return (
        <a
          href={href}
          target={'_blank'}
          rel='noopener noreferrer'
          className={classNames(classes.root, classes.link, conditionalClasses)}
        >
          {children}
        </a>
      )
    } else {
      return (
        <div className={classNames(classes.root, conditionalClasses)} style={style}>
          {children}
        </div>
      )
    }
  }
}

export default withStyles(styles)(MqText)
