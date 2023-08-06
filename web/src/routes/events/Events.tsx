// Copyright 2018-2023 contributors to the Marquez project
// SPDX-License-Identifier: Apache-2.0

import * as Redux from 'redux'
import {
  Button,
  Container,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Tooltip,
  createTheme,
} from '@mui/material'
import { ChevronLeftRounded, ChevronRightRounded } from '@mui/icons-material'
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
import { useTheme } from '@emotion/react'
import Box from '@mui/material/Box'
import IconButton from '@mui/material/IconButton'
import MqDatePicker from '../../components/core/date-picker/MqDatePicker'
import MqEmpty from '../../components/core/empty/MqEmpty'
import MqJsonView from '../../components/core/json-view/MqJsonView'
import MqStatus from '../../components/core/status/MqStatus'
import MqText from '../../components/core/text/MqText'
import React from 'react'
import moment from 'moment'

interface StateProps {
  events: Event[]
  isEventsLoading: boolean
  isEventsInit: boolean
}

interface EventsState {
  events: Event[]
  rowExpanded: number | null
  dateFrom: string
  dateTo: string
  page: number
  pageIsLast: boolean
}

interface DispatchProps {
  fetchEvents: typeof fetchEvents
  resetEvents: typeof resetEvents
}

type EventsProps = StateProps & DispatchProps

const EVENTS_COLUMNS = ['ID', 'STATE', 'NAME', 'NAMESPACE', 'TIME']

const Events: React.FC<EventsProps> = ({
  events,
  isEventsLoading,
  isEventsInit,
  fetchEvents,
  resetEvents,
}) => {
  const pageSize = 20
  const [state, setState] = React.useState<EventsState>({
    page: 1,
    events: [],
    rowExpanded: null,
    pageIsLast: false,
    dateFrom: formatDateAPIQuery(moment().startOf('day').toString()),
    dateTo: formatDateAPIQuery(moment().endOf('day').toString()),
  })

  const mounted = React.useRef<boolean>(false)

  React.useEffect(() => {
    if (!mounted.current) {
      // on mount
      fetchEvents(state.dateFrom, state.dateTo, pageSize)
      mounted.current = true
    } else {
      // on update
      if (events !== state.events) {
        setState({
          ...state,
          events: events,
          pageIsLast: events.length < state.page * pageSize,
        })
      }
    }
  })

  React.useEffect(() => {
    return () => {
      // on unmount
      resetEvents()
    }
  }, [])

  const getEvents = () => {
    return state.events?.slice((state.page - 1) * pageSize, pageSize + (state.page - 1) * pageSize)
  }

  const pageNavigation = () => {
    const titlePos =
      state.events?.length > 0
        ? `${pageSize * state.page - pageSize} - ${state.events.length}`
        : null
    return `${state.page} ${titlePos ? `(${titlePos})` : ''}`
  }

  const handleChangeDatepicker = (e: any, direction: 'from' | 'to') => {
    const isDirectionFrom = direction === 'from'
    const keyDate = isDirectionFrom ? 'dateFrom' : 'dateTo'

    fetchEvents(
      formatDateAPIQuery(isDirectionFrom ? e.toDate() : state.dateFrom),
      formatDateAPIQuery(isDirectionFrom ? state.dateTo : e.toDate()),
      pageSize
    )

    setState({ [keyDate]: formatDatePicker(e.toDate()), page: 1, rowExpanded: null } as any)
  }

  const handleClickPage = (direction: 'prev' | 'next') => {
    const directionPage = direction === 'next' ? state.page + 1 : state.page - 1

    fetchEvents(
      formatDateAPIQuery(state.dateFrom),
      formatDateAPIQuery(state.dateTo),
      pageSize * directionPage
    )
    setState({ ...state, page: directionPage, rowExpanded: null })
  }

  const handleDownloadPayload = (data: Event) => {
    const title = `${data.job.name}-${data.eventType}-${data.run.runId}`
    const blob = new Blob([JSON.stringify(data)], { type: 'application/json' })
    saveAs(blob, `${title}.json`)
  }

  const i18next = require('i18next')
  const theme = createTheme(useTheme())

  return (
    <Container maxWidth={'lg'} disableGutters>
      <MqScreenLoad loading={isEventsLoading || !isEventsInit}>
        <>
          <Box p={2} display={'flex'} justifyContent={'space-between'}>
            <Box>
              <MqText heading>{i18next.t('events_route.title')}</MqText>
              Page: {pageNavigation()}
            </Box>
            {getEvents()?.length > 0 && (
              <Box>
                <Tooltip title={i18next.t('events_route.previous_page')}>
                  <IconButton
                    sx={{
                      marginLeft: theme.spacing(2),
                    }}
                    color='primary'
                    disabled={state.page === 1}
                    onClick={() => handleClickPage('prev')}
                    size='large'
                  >
                    <ChevronLeftRounded />
                  </IconButton>
                </Tooltip>
                <Tooltip title={i18next.t('events_route.next_page')}>
                  <IconButton
                    color='primary'
                    disabled={state.pageIsLast}
                    onClick={() => handleClickPage('next')}
                    size='large'
                  >
                    <ChevronRightRounded />
                  </IconButton>
                </Tooltip>
              </Box>
            )}
          </Box>
          <Box
            p={2}
            sx={{
              display: 'flex',
              alignItems: 'center',
              gap: theme.spacing(3),
            }}
          >
            <MqDatePicker
              label={i18next.t('events_route.from_date')}
              value={formatDatePicker(state.dateFrom)}
              onChange={(e: any) => handleChangeDatepicker(e, 'from')}
            />
            <MqDatePicker
              label={i18next.t('events_route.to_date')}
              value={formatDatePicker(state.dateTo)}
              onChange={(e: any) => handleChangeDatepicker(e, 'to')}
            />
          </Box>
          {state.events?.length === 0 ? (
            <Box p={2}>
              <MqEmpty title={i18next.t('events_route.empty_title')}>
                <MqText subdued>{i18next.t('events_route.empty_body')}</MqText>
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
                    {/* {EVENTS_COLUMNS.map(field => {
                      return ( */}
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
                    {/* )
                    })} */}
                  </TableRow>
                </TableHead>
                <TableBody>
                  {getEvents()?.map((event, key: number) => {
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
                            <MqText>{event.run.runId}</MqText>
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
              <Box display={'flex'} justifyContent={'flex-end'} mb={2}>
                <Tooltip title={i18next.t('events_route.previous_page')}>
                  <IconButton
                    sx={{
                      marginLeft: theme.spacing(2),
                    }}
                    color='primary'
                    disabled={state.page === 1}
                    onClick={() => handleClickPage('prev')}
                    size='large'
                  >
                    <ChevronLeftRounded />
                  </IconButton>
                </Tooltip>
                <Tooltip title={i18next.t('events_route.next_page')}>
                  <IconButton
                    color='primary'
                    disabled={state.pageIsLast}
                    onClick={() => handleClickPage('next')}
                    size='large'
                  >
                    <ChevronRightRounded />
                  </IconButton>
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
