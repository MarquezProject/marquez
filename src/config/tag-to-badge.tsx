import React from 'react'
import LockIcon from '@material-ui/icons/Lock'
import LockOpen from '@material-ui/icons/LockOpen'
import VerifiedUser from '@material-ui/icons/VerifiedUser'
import VerifiedUserOutlined from '@material-ui/icons/VerifiedUserOutlined'

interface ITagToBadge {
  [key: string]: {
    [key: string]: JSX.Element
  }
}

const tagToBadgeConfig: ITagToBadge = {
  default: {
    is_pii: <LockOpen color='disabled' key='is_pii' />,
    is_compliant: <VerifiedUserOutlined color='disabled' key='is_compliant' />
      /* 
      Add new  { key: value } pairs here
    */
  },
  highlighted: {
    is_pii: <LockIcon color='secondary' key='is_pii' />,
    is_compliant: <VerifiedUser color='secondary' key='is_compliant' />
    /* 
      Add new  { key: value } pairs here
    */
  }
}

export default tagToBadgeConfig
