import * as React from 'react'
const styles = require('./Toast.css')

interface IProps {
  error: string
  success: string
}

const Toast = (props: IProps) =>
  props.error || props.success ? (
    <div
      className={`${styles.container} ${
        props.error ? styles.error : styles.success
      } shadow animated faster bounceInUp`}
    >
      <p>{props.error || props.success}</p>
    </div>
  ) : null

export default Toast
