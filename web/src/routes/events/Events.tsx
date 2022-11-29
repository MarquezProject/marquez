// SPDX-License-Identifier: Apache-2.0

import React from 'react'
import * as Redux from 'redux'
import moment from 'moment'
import { Theme, Container, Table, TableBody, TableCell, TableHead, TableRow, Button } from '@material-ui/core'
import { Event } from '../../types/api'
import { formatDatePicker, formatDateAPIQuery } from '../../helpers/time'
import { IState } from '../../store/reducers'
import { MqScreenLoad } from '../../components/core/screen-load/MqScreenLoad'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { theme } from '../../helpers/theme'
import { fetchEvents, resetEvents } from '../../store/actionCreators'
import { formatUpdatedAt, fileSize } from '../../helpers'
import { saveAs } from 'file-saver'
import Box from '@material-ui/core/Box'
import MqDatePicker from '../../components/core/date-picker/MqDatePicker'
import MqEmpty from '../../components/core/empty/MqEmpty'
import MqText from '../../components/core/text/MqText'
import MqJson from '../../components/core/code/MqJson'
import createStyles from '@material-ui/core/styles/createStyles'
import withStyles, { WithStyles } from '@material-ui/core/styles/withStyles'

const styles = (theme: Theme) => {
  return createStyles({
    nav: {
      display: 'flex',
      alignItems: 'center',
      gap: theme.spacing(2)
    },
    type: {
      display: 'flex',
      alignItems: 'center',
      gap: theme.spacing(1)
    },
    status: {
      width: theme.spacing(2),
      height: theme.spacing(2),
      borderRadius: '50%'
    },
    table: {
      marginBottom: '100px',
    },
    row: {
      cursor: 'pointer',
      '&:hover': {
        backgroundColor: theme.palette.action.hover,
      }
    }
  })
}

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

type EventsProps = WithStyles<typeof styles> & StateProps & DispatchProps

const EVENTS_COLUMNS = ['ID', 'STATE', 'NAME', 'NAMESPACE', 'TIME']

function eventTypeColor(type: string) {
  switch (type) {
    case 'START':
      return theme.palette.info.main
    case 'COMPLETE':
      return theme.palette.primary.main
  }
}

class Events extends React.Component<EventsProps, EventsState> {
  pageSize: number

  constructor(props: EventsProps) {
    super(props)
    this.state = {
      page: 1,
      events: [],
      rowExpanded: null,
      pageIsLast: false,
      dateFrom: formatDateAPIQuery(moment().startOf('day').toString()),
      dateTo: formatDateAPIQuery(moment().endOf('day').toString())
    }
    this.pageSize = 20
  }

  componentDidMount() {
    const { dateFrom, dateTo } = this.state
    this.props.fetchEvents(dateFrom, dateTo, this.pageSize)
  }

  componentDidUpdate() {
    const { events: eventsState, page } = this.state
    const { events: eventsProps } = this.props

    if (eventsProps !== eventsState) {
      this.setState({
        events: eventsProps,
        pageIsLast: eventsProps.length < page * this.pageSize ? true : false
      })
    }
  }

  componentWillUnmount() {
    this.props.resetEvents()
  }

  getEvents() {
    const { events, page } = this.state
    return events.slice(0 + ((page - 1) * this.pageSize), this.pageSize + ((page - 1) * this.pageSize))
  }

  pageNavigation() {
    const { events, page } = this.state
    const titlePos = events.length ? `${this.pageSize * page - this.pageSize} - ${events.length}` : `${events.length}`
    return `${page} (${titlePos})`
  }

  handleChangeDatepicker(e: any, direction: 'from' | 'to') {
    const { dateFrom, dateTo } = this.state
    const isDirectionFrom = direction === 'from'
    const keyDate = isDirectionFrom ? 'dateFrom' : 'dateTo'

    this.props.fetchEvents(
      formatDateAPIQuery(isDirectionFrom ? e.toDate() : dateFrom),
      formatDateAPIQuery(isDirectionFrom ? dateTo : e.toDate()),
      this.pageSize
    )

    this.setState({ [keyDate]: formatDatePicker(e.toDate()), page: 1, rowExpanded: null } as any)
  }

  handleClickPage(direction: 'prev' | 'next') {
    const { dateFrom, dateTo, page } = this.state
    const directionPage = direction === 'next' ? page + 1 : page - 1

    this.props.fetchEvents(formatDateAPIQuery(dateFrom), formatDateAPIQuery(dateTo), this.pageSize * directionPage)
    this.setState({ page: directionPage, rowExpanded: null })
  }

  handleDownloadPayload(data: Event) {
    let title = `${data.job.name}-${data.eventType}-${data.run.runId}`
    let blob = new Blob([JSON.stringify(data)], { type: 'application/json' })
    saveAs(blob, `${title}.json`)
  }

  render() {
    const { classes, isEventsLoading, isEventsInit } = this.props
    const { events, rowExpanded, page, dateFrom, dateTo, pageIsLast } = this.state

    return (
      <Container maxWidth={'lg'} disableGutters>
        <MqScreenLoad loading={isEventsLoading || !isEventsInit}>
          <>
            <Box p={2}>
              <MqText heading>EVENTS</MqText>
              Page: {this.pageNavigation()}
            </Box>
            <Box p={2} className={classes.nav}>
              <MqDatePicker
                label="From date"
                value={formatDatePicker(dateFrom)}
                onChange={(e: any) => this.handleChangeDatepicker(e, 'from')}
              />
              <MqDatePicker
                label="To date"
                value={formatDatePicker(dateTo)}
                onChange={(e: any) => this.handleChangeDatepicker(e, 'to')}
              />
              <Button
                variant='outlined'
                color='primary'
                disabled={page === 1}
                onClick={() => this.handleClickPage('prev')}
              >
                Previous page
              </Button>
              <Button
                variant='outlined'
                color='primary'
                disabled={pageIsLast}
                onClick={() => this.handleClickPage('next')}
              >
                Next page
              </Button>
            </Box>
            {events.length === 0 ? (
              <Box p={2}>
                <MqEmpty title={'No events found'}>
                  <MqText subdued>
                    Try changing dates or consulting our documentation to add events.
                  </MqText>
                </MqEmpty>
              </Box>
            ) : (
              <>
                <Table className={classes.table} size='small'>
                  <TableHead>
                    <TableRow>
                      {EVENTS_COLUMNS.map(field => {
                        return (
                          <TableCell key={field} align='left'>
                            <MqText subheading>{field}</MqText>
                          </TableCell>
                        )
                      })}
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {this.getEvents().map((event, key: number) => {
                      return (
                        <React.Fragment key={key}>
                          <TableRow
                            className={classes.row}
                            onClick={() => {
                              this.setState({ rowExpanded: key === rowExpanded ? null : key })
                            }}
                          >
                            <TableCell align='left'>
                              <MqText>{event.run.runId}</MqText>
                            </TableCell>
                            <TableCell align='left'>
                              <Box className={classes.type}>
                                <Box
                                  className={classes.status}
                                  style={{ backgroundColor: eventTypeColor(event.eventType) }}
                                />
                                <MqText>{event.eventType}</MqText>
                              </Box>
                            </TableCell>
                            <TableCell align='left'>
                              {event.job.name}
                            </TableCell>
                            <TableCell align='left'>
                              <MqText> {event.job.namespace} </MqText>
                            </TableCell>
                            <TableCell align='left'>
                              <MqText>{formatUpdatedAt(event.eventTime)}</MqText>
                            </TableCell>
                          </TableRow>
                          {rowExpanded === key &&
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
                                          onClick={() => this.handleDownloadPayload(event)}
                                        >
                                          Download payload
                                        </Button>
                                      </div>
                                    </MqEmpty>
                                  </Box>
                                ) : (
                                  <MqJson code={event} wrapLongLines={true} showLineNumbers={true} />
                                )}
                              </TableCell>
                            </TableRow>
                          }
                        </React.Fragment>
                      )
                    })}
                  </TableBody>
                </Table>
              </>
            )}
          </>
        </MqScreenLoad>
      </Container>
    )
  }
}

const mapStateToProps = (state: IState) => ({
  events: state.events.result,
  isEventsLoading: state.events.isLoading,
  isEventsInit: state.events.init
})

const mapDispatchToProps = (dispatch: Redux.Dispatch) =>
  bindActionCreators(
    {
      fetchEvents: fetchEvents,
      resetEvents: resetEvents
    },
    dispatch
  )

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(withStyles(styles)(Events))
