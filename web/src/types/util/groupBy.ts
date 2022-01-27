// SPDX-License-Identifier: Apache-2.0

export function groupBy<T, K extends keyof T>(list: T[], key: K) {
  const map = new Map<T[K], T[]>()
  list.forEach(item => {
    const itemKey = item[key]
    if (!map.has(itemKey)) {
      map.set(itemKey, list.filter(i => i[key] === item[key]))
    }
  })
  return map
}
