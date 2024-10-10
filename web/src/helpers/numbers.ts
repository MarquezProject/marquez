export function formatNumber(num: number): string {
  if (num >= 1000 && num < 1_000_000) {
    return (num / 1000).toFixed(0) + 'k'
  } else if (num >= 1_000_000) {
    return (num / 1_000_000).toFixed(0) + 'M'
  } else {
    return num.toString()
  }
}
