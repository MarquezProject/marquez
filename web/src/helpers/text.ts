export const truncateText = (text: string, maxLength: number) => {
  if (text.length > maxLength) {
    return `${text.substring(0, maxLength)}...`
  }
  return text
}

export const truncateTextFront = (text: string, maxLength: number) => {
  if (text.length > maxLength) {
    return `...${text.substring(text.length - maxLength)}`
  }
  return text
}

export const pluralize = (count: number, singular: string, plural: string) => {
  const noun = count === 1 ? singular : plural
  return `${count} ${noun}`
}
