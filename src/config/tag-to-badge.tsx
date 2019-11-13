import React from 'react'
import LockIcon from '@material-ui/icons/Lock'
import VerifiedUser from '@material-ui/icons/VerifiedUser'

interface ITagToBadge {
  [key: string]: JSX.Element
}

const tagToBadgeConfig: ITagToBadge = {
  is_pii: <LockIcon color='secondary' key='is_pii' />,
  is_compliant: <VerifiedUser color='secondary' key='is_compliant' />
  /* 
    Add new  { key: value } pairs here
  */
}

export default tagToBadgeConfig
