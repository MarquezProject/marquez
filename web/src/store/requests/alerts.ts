import { API_URL } from '../../globals'
import { genericFetchWrapper } from './index'

export const getAlerts = async (entityType: string, entityUuid: string) => {
  const url = `${API_URL}/alerts?entityType=${entityType}&entityUuid=${entityUuid}`
  return genericFetchWrapper(url, { method: 'GET' }, 'fetchAlerts')
}

export const postAlert = async (entityType: string, entityUuid: string, type: string) => {
  const url = `${API_URL}/alerts?entityType=${entityType}&entityUuid=${entityUuid}&type=${type}`
  return genericFetchWrapper(
    url,
    {
      method: 'POST',
      body: '{}',
      headers: {
        'Content-Type': 'application/json',
      },
    },
    'updateAlert'
  )
}

export const deleteAlert = async (uuid: string) => {
  const url = `${API_URL}/alerts?uuid=${uuid}`
  return genericFetchWrapper(url, { method: 'DELETE' }, 'deleteAlert')
}
