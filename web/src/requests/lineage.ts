import { genericFetchWrapper } from './index'

export const fetchLineage = async (nodeId: string) => {
  const lineageQuery = `{
  lineage(nodeId: "${nodeId}") {
    graph {
      id
      type
      data {
        __typename
        ... on Dataset {
          physicalName
        }
        ... on Job {
          type
          latestRun {
            state
          }
        }
      }
      inEdges {
        destination
      }
      outEdges {
        origin
        destination
      }
    }
  }
}`
  // eslint-disable-next-line no-undef
  const url = `${__API_URL_BETA__}/graphql`
  return genericFetchWrapper<any[]>(
    url,
    { method: 'POST', body: JSON.stringify({ query: lineageQuery }) },
    'fetchLineage'
  )
}
