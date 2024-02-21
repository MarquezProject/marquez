// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import { isoParse, timeFormat } from 'd3-time-format'

export const capitalize = (word: string) => {
  if (word.length < 2) {
    return word.toUpperCase()
  }
  return `${word[0].toUpperCase()}${word.slice(1)}`
}

const customTimeFormat = timeFormat('%b %d, %Y %I:%M%p')

export const formatUpdatedAt = (updatedAt: string) => {
  const parsedDate = isoParse(updatedAt)
  if (!parsedDate) {
    return ''
  } else {
    const dateString = customTimeFormat(parsedDate)
    return `${dateString.slice(0, -2)}${dateString.slice(-2).toLowerCase()}`
  }
}

export const fileSize = (data: string) => {
  const size = encodeURI(data).split(/%..|./).length - 1
  return {
    kiloBytes: size / 1024,
    megaBytes: size / 1024 / 1024,
  }
}
