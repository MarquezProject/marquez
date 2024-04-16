// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import * as Redux from 'redux'
import {
  Button,
  Chip,
  Container,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Tooltip,
  createTheme,
} from '@mui/material'
import { ChevronLeftRounded, ChevronRightRounded, Refresh } from '@mui/icons-material'
import { Event } from '../../types/api'
import { IState } from '../../store/reducers'
import { MqScreenLoad } from '../../components/core/screen-load/MqScreenLoad'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { eventTypeColor } from '../../helpers/nodes'
import { fetchEvents, resetEvents } from '../../store/actionCreators'
import { fileSize, formatUpdatedAt } from '../../helpers'
import { formatDateAPIQuery, formatDatePicker } from '../../helpers/time'
import { saveAs } from 'file-saver'
import { useSearchParams } from 'react-router-dom'
import { useTheme } from '@emotion/react'
import Box from '@mui/material/Box'
import CircularProgress from '@mui/material/CircularProgress/CircularProgress'
import IconButton from '@mui/material/IconButton'
import MqCopy from '../../components/core/copy/MqCopy'
import MqDatePicker from '../../components/core/date-picker/MqDatePicker'
import MqEmpty from '../../components/core/empty/MqEmpty'
import MqJsonView from '../../components/core/json-view/MqJsonView'
import MqStatus from '../../components/core/status/MqStatus'
import MqText from '../../components/core/text/MqText'
import React, { useEffect, useRef } from 'react'
import moment from 'moment'

interface StateProps {
  events: Event[]
  totalCount: number
  isEventsLoading: boolean
  isEventsInit: boolean
}

interface EventsState {
  events: Event[]
  rowExpanded: number | null
  dateFrom: string
  dateTo: string
  page: number
}

interface DispatchProps {
  fetchEvents: typeof fetchEvents
  resetEvents: typeof resetEvents
}

type EventsProps = StateProps & DispatchProps

const EVENTS_COLUMNS = ['ID', 'STATE', 'NAME', 'NAMESPACE', 'TIME']

const PAGE_SIZE = 20

const Events: React.FC<EventsProps> = ({
  events,
  totalCount,
  isEventsLoading,
  isEventsInit,
  fetchEvents,
  resetEvents,
}) => {
  const [searchParams, setSearchParams] = useSearchParams()
  const [state, setState] = React.useState<EventsState>({
    page: 0,
    events: [],
    rowExpanded: null,
    dateFrom:
      searchParams.get('dateFrom') || formatDateAPIQuery(moment().startOf('day').toString()),
    dateTo: searchParams.get('dateTo') || formatDateAPIQuery(moment().endOf('day').toString()),
  })

  const mounted = useRef<boolean>(false)

  useEffect(() => {
    if (!mounted.current) {
      // on mount
      fetchEvents(state.dateFrom, state.dateTo, PAGE_SIZE, state.page * PAGE_SIZE)
      mounted.current = true
    } else {
      // on update
      if (events !== state.events) {
        setState({
          ...state,
          events: events,
        })
      }
    }
  })

  useEffect(() => {
    if (!searchParams.get('dateFrom') && !searchParams.get('dateTo')) {
      setSearchParams({
        dateFrom: formatDateAPIQuery(moment().startOf('day').toString()),
        dateTo: formatDateAPIQuery(moment().endOf('day').toString()),
      })
    }
  }, [])

  useEffect(() => {
    return () => {
      // on unmount
      resetEvents()
    }
  }, [])

  const handleChangeDatepicker = (e: any, direction: 'from' | 'to') => {
    const isDirectionFrom = direction === 'from'
    const keyDate = isDirectionFrom ? 'dateFrom' : 'dateTo'

    fetchEvents(
      formatDateAPIQuery(isDirectionFrom ? e.toDate() : state.dateFrom),
      formatDateAPIQuery(isDirectionFrom ? state.dateTo : e.toDate()),
      PAGE_SIZE,
      state.page * PAGE_SIZE
    )

    const params: { [key: string]: string } = {}
    searchParams.forEach((value, key) => (params[key] = value))
    setSearchParams({ ...params, [keyDate]: formatDateAPIQuery(e.toDate()) })
    setState({
      ...state,
      [keyDate]: formatDatePicker(e.toDate()),
      page: 0,
      rowExpanded: null,
    } as any)
  }

  const handleClickPage = (direction: 'prev' | 'next') => {
    const directionPage = direction === 'next' ? state.page + 1 : state.page - 1

    fetchEvents(
      formatDateAPIQuery(state.dateFrom),
      formatDateAPIQuery(state.dateTo),
      PAGE_SIZE,
      directionPage * PAGE_SIZE
    )
    // reset page scroll
    window.scrollTo(0, 0)
    setState({ ...state, page: directionPage, rowExpanded: null })
  }

  const handleDownloadPayload = (data: Event) => {
    const title = `${data.job.name}-${data.eventType}-${data.run.runId}`
    const blob = new Blob([JSON.stringify(data)], { type: 'application/json' })
    saveAs(blob, `${title}.json`)
  }

  const refresh = () => {
    const dateFrom =
      searchParams.get('dateFrom') || formatDateAPIQuery(moment().startOf('day').toString())
    const dateTo =
      searchParams.get('dateTo') || formatDateAPIQuery(moment().endOf('day').toString())
    fetchEvents(dateFrom, dateTo, PAGE_SIZE, state.page * PAGE_SIZE)
  }

  const i18next = require('i18next')
  const theme = createTheme(useTheme())

  return (
    <Container maxWidth={'lg'} disableGutters>
      <MqScreenLoad loading={!isEventsInit}>
        <>
          <Box p={2} display={'flex'} justifyContent={'space-between'}>
            <Box>
              <Box display={'flex'} alignItems={'center'}>
                <MqText heading>{i18next.t('events_route.title')}</MqText>
                <Chip
                  size={'small'}
                  variant={'outlined'}
                  color={'primary'}
                  sx={{ marginLeft: 1 }}
                  label={totalCount + ' total'}
                ></Chip>
              </Box>
            </Box>
            <Tooltip title={'Refresh'}>
              <IconButton
                color={'primary'}
                size={'small'}
                onClick={() => {
                  refresh()
                }}
              >
                <Refresh fontSize={'small'} />
              </IconButton>
            </Tooltip>
          </Box>
          <Box
            p={2}
            sx={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              gap: theme.spacing(3),
            }}
          >
            <Box display={'flex'}>
              <MqDatePicker
                label={i18next.t('events_route.from_date')}
                value={formatDatePicker(state.dateFrom)}
                onChange={(e: any) => handleChangeDatepicker(e, 'from')}
              />
              <Box sx={{ marginLeft: theme.spacing(2) }} />
              <MqDatePicker
                label={i18next.t('events_route.to_date')}
                value={formatDatePicker(state.dateTo)}
                onChange={(e: any) => handleChangeDatepicker(e, 'to')}
              />
            </Box>
            {isEventsLoading && <CircularProgress size={16} />}
          </Box>
          {state.events?.length === 0 ? (
            <Box p={2}>
              <MqEmpty title={i18next.t('events_route.empty_title')}>
                <>
                  <MqText subdued>{i18next.t('events_route.empty_body')}</MqText>
                  <Button
                    color={'primary'}
                    size={'small'}
                    onClick={() => {
                      refresh()
                    }}
                  >
                    Refresh
                  </Button>
                </>
              </MqEmpty>
            </Box>
          ) : (
            <>
              <Table
                sx={{
                  marginBottom: theme.spacing(2),
                }}
                size='small'
              >
                <TableHead>
                  <TableRow>
                    <TableCell align='left'>
                      <MqText subheading>{i18next.t('events_columns.id')}</MqText>
                    </TableCell>
                    <TableCell align='left'>
                      <MqText subheading>{i18next.t('events_columns.state')}</MqText>
                    </TableCell>
                    <TableCell align='left'>
                      <MqText subheading>{i18next.t('events_columns.name')}</MqText>
                    </TableCell>
                    <TableCell align='left'>
                      <MqText subheading>{i18next.t('events_columns.namespace')}</MqText>
                    </TableCell>
                    <TableCell align='left'>
                      <MqText subheading>{i18next.t('events_columns.time')}</MqText>
                    </TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {events.map((event, key: number) => {
                    return (
                      <React.Fragment key={key}>
                        <TableRow
                          sx={{
                            cursor: 'pointer',
                            '&:hover': {
                              backgroundColor: theme.palette.action.hover,
                            },
                          }}
                          onClick={() => {
                            setState({
                              ...state,
                              rowExpanded: key === state.rowExpanded ? null : key,
                            })
                          }}
                        >
                          <TableCell align='left'>
                            <Box display={'flex'} alignItems={'center'}>
                              <MqText font={'mono'}>{event.run.runId}</MqText>
                              <MqCopy string={event.run.runId} />
                            </Box>
                          </TableCell>
                          <TableCell align='left'>
                            <MqStatus
                              color={eventTypeColor(event.eventType)}
                              label={event.eventType}
                            />
                          </TableCell>
                          <TableCell align='left'>{event.job.name}</TableCell>
                          <TableCell align='left'>
                            <MqText> {event.job.namespace} </MqText>
                          </TableCell>
                          <TableCell align='left'>
                            <MqText>{formatUpdatedAt(event.eventTime)}</MqText>
                          </TableCell>
                        </TableRow>
                        {state.rowExpanded === key && (
                          <TableRow>
                            <TableCell colSpan={EVENTS_COLUMNS.length}>
                              {fileSize(JSON.stringify(event)).kiloBytes > 500 ? (
                                <Box p={2}>
                                  <MqEmpty title={'Payload is too big for render'}>
                                    <div>
                                      <MqText subdued>
                                        Please click on button and download payload as file
                                      </MqText>
                                      <br />
                                      <Button
                                        variant='outlined'
                                        color='primary'
                                        onClick={() => handleDownloadPayload(event)}
                                      >
                                        Download payload
                                      </Button>
                                    </div>
                                  </MqEmpty>
                                </Box>
                              ) : (
                                <MqJsonView data={event} />
                              )}
                            </TableCell>
                          </TableRow>
                        )}
                      </React.Fragment>
                    )
                  })}
                </TableBody>
              </Table>
              <Box display={'flex'} justifyContent={'flex-end'} alignItems={'center'} mb={2}>
                <MqText subdued>
                  <>
                    {PAGE_SIZE * state.page + 1} -{' '}
                    {Math.min(PAGE_SIZE * (state.page + 1), totalCount)} of {totalCount}
                  </>
                </MqText>
                <Tooltip title={i18next.t('events_route.previous_page')}>
                  <span>
                    <IconButton
                      sx={{
                        marginLeft: theme.spacing(2),
                      }}
                      color='primary'
                      disabled={state.page === 0}
                      onClick={() => handleClickPage('prev')}
                      size='large'
                    >
                      <ChevronLeftRounded />
                    </IconButton>
                  </span>
                </Tooltip>
                <Tooltip title={i18next.t('events_route.next_page')}>
                  <span>
                    <IconButton
                      color='primary'
                      onClick={() => handleClickPage('next')}
                      size='large'
                      disabled={state.page === Math.ceil(totalCount / PAGE_SIZE) - 1}
                    >
                      <ChevronRightRounded />
                    </IconButton>
                  </span>
                </Tooltip>
              </Box>
            </>
          )}
        </>
      </MqScreenLoad>
    </Container>
  )
}

const mapStateToProps = (state: IState) => ({
  events: state.events?.result,
  totalCount: state.events.totalCount,
  isEventsLoading: state.events?.isLoading,
  isEventsInit: state.events?.init,
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      fetchEvents: fetchEvents,
      resetEvents: resetEvents,
    },
    dispatch
  )

export default connect(mapStateToProps, mapDispatchToProps)(Events)
