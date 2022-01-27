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
