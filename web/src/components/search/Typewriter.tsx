import React, { useEffect, useState } from 'react'

interface TypewriterProps {
  words: string[]
  typingSpeed?: number
  deletingSpeed?: number
  pauseTime?: number
  repeatCount?: number
}

const Typewriter: React.FC<TypewriterProps> = ({
  words,
  typingSpeed = 150,
  deletingSpeed = 100,
  pauseTime = 1000,
  repeatCount = Infinity,
}) => {
  const [text, setText] = useState('')
  const [isDeleting, setIsDeleting] = useState(false)
  const [wordIndex, setWordIndex] = useState(0)
  const [typingInterval, setTypingInterval] = useState(typingSpeed)
  const [repeatCounter, setRepeatCounter] = useState(0)

  useEffect(() => {
    const handleTyping = () => {
      const currentWord = words[wordIndex]
      if (isDeleting) {
        if (text.length > 0) {
          setText((prev) => prev.slice(0, prev.length - 1))
          setTypingInterval(deletingSpeed)
        } else {
          setIsDeleting(false)
          setWordIndex((prev) => (prev + 1) % words.length)
          setTypingInterval(typingSpeed)
        }
      } else {
        if (text.length < currentWord.length) {
          setText((prev) => prev + currentWord.charAt(text.length))
          setTypingInterval(typingSpeed)
        } else {
          if (repeatCounter < repeatCount - 1) {
            setTimeout(() => {
              setIsDeleting(true)
              setRepeatCounter((prev) => prev + 1)
            }, pauseTime)
          }
        }
      }
    }

    const typingTimeout = setTimeout(handleTyping, typingInterval)
    return () => clearTimeout(typingTimeout)
  }, [
    text,
    isDeleting,
    wordIndex,
    typingInterval,
    typingSpeed,
    deletingSpeed,
    pauseTime,
    words,
    repeatCount,
    repeatCounter,
  ])

  return <span>{text}</span>
}

export default Typewriter
