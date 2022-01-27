// SPDX-License-Identifier: Apache-2.0

import moment from 'moment'

function addLeadingZero(number: number) {
  if (number.toString().length === 1) {
    return `0${number.toString()}`
  }
  return number
}

export function stopWatchDuration(durationMs: number) {
  const duration = moment.duration(durationMs, 'ms')
  if (duration.asMilliseconds() === 0) {
    return '0'
  }

  if (duration.asHours() > 1) {
    return `${duration.hours()}h ${addLeadingZero(duration.seconds())}s`
  }
  if (duration.asSeconds() > 1) {
    return `${duration.minutes()}m ${addLeadingZero(duration.seconds())}s`
  } else {
    return `${duration.milliseconds()} ms`
  }
}
