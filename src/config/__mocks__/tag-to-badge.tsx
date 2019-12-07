import React from 'react'

const fakeTagToBadgeMap = {
  default: {
    tag_a: (
      <span color='default' key='tag_a'>
        Pretend Badge
      </span>
    ),
    tag_b: (
      <span color='default' key='tag_b'>
        Pretend Badge
      </span>
    )
  },
  highlighted: {
    tag_a: (
      <span color='highlighted' key='tag_a'>
        Pretend Badge
      </span>
    ),
    tag_b: (
      <span color='highlighted' key='tag_b'>
        Pretend Badge
      </span>
    )
  }
}

export default fakeTagToBadgeMap
