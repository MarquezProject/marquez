// SPDX-License-Identifier: Apache-2.0

import { JSONTree } from 'react-json-tree'
import { MqInputBase, MqInputBaseProps } from '../input-base/MqInputBase'
import { createTheme } from '@mui/material'
import { useTheme } from '@emotion/react'
import React from 'react'

interface OwnProps {
  data: object
  searchable?: boolean
  placeholder?: string
}

interface StateProps {
  search: string
}

type JsonViewProps = OwnProps

const InputSearchJsonView: React.FC<MqInputBaseProps> = (props) => {
  const theme = createTheme(useTheme())

  return (
    <MqInputBase
      {...props}
      sx={{
        ...props.sx,
        padding: `${theme.spacing(1)} ${theme.spacing(2)}`,
      }}
    />
  )
}

const MqJsonView: React.FC<JsonViewProps> = ({
  data,
  searchable = false,
  placeholder = 'Search',
}) => {
  const [state, setState] = React.useState<StateProps>({
    search: '',
  })

  const onSearch = (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setState({ search: event.target.value })
  }

  const theme = createTheme(useTheme())

  return (
    <>
      {searchable && (
        <InputSearchJsonView
          sx={{
            marginBottom: theme.spacing(2),
          }}
          onChange={(event) => onSearch(event)}
          value={state.search}
          autoComplete={'off'}
          id={'json-view'}
          placeholder={placeholder}
        />
      )}
      <JSONTree
        data={data}
        theme={'rjv_white'}
        collectionLimit={2}
        // highlightSearch={search} // TODO find a solution to do this
      />
    </>
  )
}

export default MqJsonView
