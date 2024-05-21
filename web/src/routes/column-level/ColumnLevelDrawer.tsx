import * as Redux from 'redux'
import { Box } from '@mui/system'
import {
  Chip,
  CircularProgress,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
} from '@mui/material'
import { ColumnLineageGraph, Dataset } from '../../types/api'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { IState } from '../../store/reducers'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { faDatabase } from '@fortawesome/free-solid-svg-icons'
import { fetchDataset } from '../../store/actionCreators'
import { theme } from '../../helpers/theme'
import { useSearchParams } from 'react-router-dom'
import CloseIcon from '@mui/icons-material/Close'
import IconButton from '@mui/material/IconButton'
import MqJsonView from '../../components/core/json-view/MqJsonView'
import MqText from '../../components/core/text/MqText'
import React, { useEffect } from 'react'

const i18next = require('i18next')

const WIDTH = 600

interface StateProps {
  dataset: Dataset
  isDatasetLoading: boolean
  columnLineage: ColumnLineageGraph
}
interface DispatchProps {
  fetchDataset: typeof fetchDataset
}
const ColumnLevelDrawer = ({
  dataset,
  fetchDataset,
  columnLineage,
  isDatasetLoading,
}: StateProps & DispatchProps) => {
  const [searchParams, setSearchParams] = useSearchParams()
  useEffect(() => {
    const dataset = searchParams.get('dataset')
    const namespace = searchParams.get('namespace')
    if (dataset && namespace) {
      fetchDataset(namespace, dataset)
    }
  }, [])

  if (!columnLineage) {
    return null
  }

  return (
    <Box width={`${WIDTH}px`}>
      <Box
        position={'sticky'}
        top={0}
        bgcolor={theme.palette.background.default}
        pt={2}
        zIndex={theme.zIndex.appBar}
        sx={{ borderBottom: 1, borderColor: 'divider', width: '100%' }}
      >
        <Box px={2} pb={2} display={'flex'} justifyContent={'space-between'} alignItems={'center'}>
          <Box display={'flex'} alignItems={'center'}>
            <Box display={'flex'} alignItems={'center'}>
              <Box
                mr={2}
                borderRadius={theme.spacing(1)}
                p={1}
                width={32}
                height={32}
                display={'flex'}
                bgcolor={theme.palette.info.main}
              >
                <FontAwesomeIcon
                  aria-hidden={'true'}
                  title={'Dataset'}
                  icon={faDatabase}
                  width={16}
                  height={16}
                  color={theme.palette.common.white}
                />
              </Box>
            </Box>
            <MqText heading>{searchParams.get('dataset')}</MqText>
          </Box>
          <IconButton
            onClick={() => {
              setSearchParams({})
            }}
          >
            <CloseIcon fontSize={'small'} />
          </IconButton>
        </Box>
      </Box>
      {!dataset || isDatasetLoading ? (
        <Box mt={2} display={'flex'} justifyContent={'center'}>
          <CircularProgress color='primary' />
        </Box>
      ) : (
        <>
          <Box p={2}>
            <MqText subheading>SCHEMA</MqText>
          </Box>
          {dataset.fields.length > 0 && (
            <>
              <Table size='small'>
                <TableHead>
                  <TableRow>
                    <TableCell align='left'>
                      <MqText subheading inline>
                        {i18next.t('dataset_info_columns.name')}
                      </MqText>
                    </TableCell>
                    <TableCell align='left'>
                      <MqText subheading inline>
                        {i18next.t('dataset_info_columns.type')}
                      </MqText>
                    </TableCell>
                    <TableCell align='left'>
                      <MqText subheading inline>
                        {i18next.t('dataset_info_columns.description')}
                      </MqText>
                    </TableCell>
                    <TableCell align='left'></TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {dataset.fields.map((field) => {
                    return (
                      <React.Fragment key={field.name}>
                        <TableRow>
                          <TableCell align='left'>
                            <MqText font={'mono'}>{field.name}</MqText>
                          </TableCell>
                          <TableCell align='left'>
                            <Chip
                              size={'small'}
                              label={<MqText font={'mono'}>{field.type}</MqText>}
                              variant={'outlined'}
                            />
                          </TableCell>
                          <TableCell align='left'>
                            <MqText subdued>{field.description || 'no description'}</MqText>
                          </TableCell>
                        </TableRow>
                      </React.Fragment>
                    )
                  })}
                </TableBody>
              </Table>
            </>
          )}
        </>
      )}
      {dataset && dataset.columnLineage && (
        <>
          <Box p={2}>
            <MqText subheading>FACETS</MqText>
            <MqJsonView data={dataset.columnLineage} />
          </Box>
        </>
      )}
    </Box>
  )
}

const mapStateToProps = (state: IState) => ({
  dataset: state.dataset.result,
  isDatasetLoading: state.dataset.isLoading,
  columnLineage: state.columnLineage.columnLineage,
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      fetchDataset: fetchDataset,
    },
    dispatch
  )
export default connect(mapStateToProps, mapDispatchToProps)(ColumnLevelDrawer)
