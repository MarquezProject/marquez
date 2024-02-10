// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import React, { ReactElement } from 'react'

import { Box } from '@mui/system'
import { Link as LinkRouter } from 'react-router-dom'
import { THEME_EXTRA } from '../../../helpers/theme'
import { Typography } from '@mui/material'
import { alpha, createTheme } from '@mui/material/styles'
import { useTheme } from '@emotion/react'
import Link from '@mui/material/Link'

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
  overflowHidden?: boolean
  color?: string
  link?: boolean
  href?: string
  linkTo?: string
  font?: 'primary' | 'mono'
  small?: boolean
  bottomMargin?: boolean
  children: ReactElement | (string | ReactElement)[] | string | string[] | number | undefined | null
  onClick?: () => void
}

type MqTextProps = OwnProps

const MqText: React.FC<MqTextProps> = ({
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
  overflowHidden,
  href,
  inverse,
  inline,
  highlight,
  color,
  small,
  onClick,
}) => {
  const theme = createTheme(useTheme())

  const classesObject = {
    root: {
      lineHeight: 1.5,
      fontSize: '.875rem',
      fontFamily: 'Karla, sans-serif',
      margin: 0,
      padding: 0,
      color: theme.palette.common.white,
      fontWeight: 400,
    },
    inline: {
      display: 'inline',
    },
    heading: {
      fontWeight: 700,
      fontSize: '1.125rem',
    },
    subheading: {
      fontWeight: 700,
      fontSize: '.875rem',
    },
    mono: {
      fontFamily: `${'Source Code Pro'}, serif`,
    },
    bold: {
      fontWeight: 700,
    },
    subdued: {
      color: THEME_EXTRA.typography.subdued,
    },
    disabled: {
      color: THEME_EXTRA.typography.disabled,
    },
    link: {
      color: theme.palette.primary.main,
      cursor: 'pointer',
      textDecoration: 'none',
      '&:hover': {
        textDecoration: 'underline',
      },
    },
    label: {
      fontSize: '.625rem',
      textTransform: 'uppercase',
      lineHeight: 1.4,
    },
    highlight: {
      backgroundColor: alpha(theme.palette.primary.main, 0.5),
    },
    bottomMargin: {
      marginBottom: theme.spacing(1),
    },
    inverse: {
      color: theme.palette.common.white,
    },
    small: {
      fontSize: '.625rem',
    },
    paragraph: {
      marginBottom: theme.spacing(2),
    },
    overflowHidden: {
      overflow: 'hidden',
    },
  }

  const conditionalClasses = Object.assign(
    {},
    subdued ? classesObject.subdued : {},
    bold ? classesObject.bold : {},
    label ? classesObject.label : {},
    disabled ? classesObject.disabled : {},
    highlight ? classesObject.highlight : {},
    font === 'mono' ? classesObject.mono : {},
    bottomMargin ? classesObject.bottomMargin : {},
    inverse ? classesObject.inverse : {},
    inline ? classesObject.inline : {},
    small ? classesObject.small : {},
    link ? classesObject.link : {},
    paragraph ? classesObject.paragraph : {},
    subheading ? classesObject.subheading : {},
    overflowHidden ? classesObject.overflowHidden : {}
  )

  const style = {
    color: color && color,
  }

  if (heading) {
    return (
      <Typography
        onClick={onClick}
        variant='h4'
        sx={Object.assign(classesObject.root, classesObject.heading, conditionalClasses)}
        style={style}
      >
        {children}
      </Typography>
    )
  } else if (link && linkTo) {
    return (
      <LinkRouter
        to={linkTo}
        aria-disabled={disabled}
        style={{ textDecoration: 'none' }}
        onClick={onClick}
      >
        <Box
          component='span'
          sx={Object.assign(classesObject.root, classesObject.link, conditionalClasses)}
        >
          {children}
        </Box>
      </LinkRouter>
    )
  } else if (link && href) {
    return (
      <Link
        onClick={onClick}
        href={href}
        target={'_blank'}
        rel='noopener noreferrer'
        sx={Object.assign(classesObject.root, classesObject.link, conditionalClasses)}
      >
        {children}
      </Link>
    )
  } else {
    return (
      <Box
        onClick={onClick}
        sx={Object.assign(classesObject.root, conditionalClasses)}
        style={style}
      >
        {children}
      </Box>
    )
  }
}

export default MqText
