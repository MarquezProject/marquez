import React, { ReactElement, useState, ChangeEvent } from 'react'
import MUISelect from '@material-ui/core/Select'
import MenuItem from '@material-ui/core/MenuItem'
import Box from '@material-ui/core/Box'
import FormControl from '@material-ui/core/FormControl'
import uniq from 'lodash/uniq'
import { withStyles } from '@material-ui/core/styles'
import { capitalize } from '../helpers'
import { IProps } from '../containers/FilterContainer'
import { Namespace } from '../types/api'

const StyledFormControl = withStyles({
  root: {
    margin: '0rem 2rem 0rem 0rem',
    minWidth: '10rem'
  }
})(FormControl)

type IEntity = Namespace | string
interface IFilterDictionary {
  [key: string]: {
    entities: IEntity[]
    accessor: (n: IEntity) => string
    default: any
  }
}

const filterByOptions: { [key: string]: 'namespace' | 'sourceName' } = {
  /* display name: entitiy name on dataset schema */
  namespace: 'namespace',
  source: 'sourceName'
}

const Filters = (props: IProps): ReactElement => {
  const { namespaces, datasets, filterJobs, filterDatasets, showJobs } = props

  const [currentFilter, setCurrentFilter] = useState('all')
  const [currentFilterValue, setCurrentFilterValue] = useState({})
  const [subFilterVisible, setSubFilterVisibility] = useState(false)

  const sources = uniq(datasets.map(d => d.sourceName))
  const filterDictionary: IFilterDictionary = {
    namespace: {
      entities: namespaces,
      accessor: n => (n as Namespace).name,
      default: { name: '' }
    },
    source: {
      entities: sources,
      accessor: (n: string): string => n,
      default: ''
    }
    /* Can add more filters here */
  }

  /* Set the category we will be filtering by */
  const onPrimaryFilterChange = (e: ChangeEvent<HTMLSelectElement>) => {
    const newPrimaryFilter = e.target.value as 'namespace' | 'source' | 'all'
    setCurrentFilter(newPrimaryFilter)
    if (newPrimaryFilter === 'all') {
      setSubFilterVisibility(false)
      filterJobs(newPrimaryFilter)
      filterDatasets(newPrimaryFilter)
    } else {
      const currentFilterConfig = filterDictionary[newPrimaryFilter]
      const currentFilterValue = currentFilterConfig.entities[0] || currentFilterConfig.default
      const currentFilterKey = filterByOptions[newPrimaryFilter]
      setCurrentFilterValue(currentFilterValue)
      setSubFilterVisibility(true)
      filterJobs(currentFilterKey, currentFilterConfig.accessor(currentFilterValue))
      filterDatasets(currentFilterKey, currentFilterConfig.accessor(currentFilterValue))
    }
    showJobs(true)
  }

  /* Filter jobs & datasets by the selected filter value */
  const onSecondaryFilterChange = (e: ChangeEvent<HTMLSelectElement>) => {
    const currentFilterValue = e.target.value as string
    const currentFilterKey = filterByOptions[currentFilter]

    setCurrentFilterValue(currentFilterValue)
    filterJobs(currentFilterKey, filterDictionary[currentFilter].accessor(currentFilterValue))
    filterDatasets(currentFilterKey, filterDictionary[currentFilter].accessor(currentFilterValue))
    showJobs(true)
  }

  return (
    <Box ml='5%' py={2}>
      <StyledFormControl margin='normal'>
        <MUISelect value={currentFilter} renderValue={capitalize} onChange={onPrimaryFilterChange}>
          {Object.keys(filterByOptions).map(o => (
            <MenuItem key={o} value={o}>
              {o}
            </MenuItem>
          ))}
          <MenuItem key='all' value='all'>
            all
          </MenuItem>
        </MUISelect>
      </StyledFormControl>
      {subFilterVisible && (
        <StyledFormControl margin='normal'>
          <MUISelect
            value={currentFilterValue}
            renderValue={filterDictionary[currentFilter].accessor}
            onChange={onSecondaryFilterChange}
          >
            {filterDictionary[currentFilter].entities.map((o: any) => {
              const val = filterDictionary[currentFilter].accessor(o)
              return (
                <MenuItem key={val} value={o}>
                  {val}
                </MenuItem>
              )
            })}
          </MUISelect>
        </StyledFormControl>
      )}
    </Box>
  )
}

export default Filters
