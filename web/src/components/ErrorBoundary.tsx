/**
 * ErrorBoundary Component
 *
 * This component provides an error boundary for React components. It catches JavaScript errors
 * anywhere in the child component tree, logs those errors, and displays a fallback UI.
 *
 * Author: Jonathan Moraes
 * Created: 2025-02-19
 * Reason: To handle errors gracefully in the application and provide a fallback UI.
 */

import React, { ReactNode } from 'react'

interface ErrorBoundaryProps {
  children: ReactNode
}

interface ErrorBoundaryState {
  hasError: boolean
}

class ErrorBoundary extends React.Component<ErrorBoundaryProps, ErrorBoundaryState> {
  constructor(props: ErrorBoundaryProps) {
    super(props)
    this.state = { hasError: false }
  }

  static getDerivedStateFromError() {
    return { hasError: true }
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    console.error('Error caught by boundary:', error, errorInfo)
  }

  render() {
    if (this.state.hasError) {
      return <h1>Something went wrong.</h1>
    }
    return this.props.children
  }
}

export default ErrorBoundary