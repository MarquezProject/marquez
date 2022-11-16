// SPDX-License-Identifier: Apache-2.0

import React from 'react'
import * as Redux from 'redux'
import moment from 'moment'
import { Theme, Container, Table, TableBody, TableCell, TableHead, TableRow } from '@material-ui/core'
import { Event } from '../../types/api'
import { IState } from '../../store/reducers'
import { MqScreenLoad } from '../../components/core/screen-load/MqScreenLoad'
import { bindActionCreators } from 'redux'
import { connect } from 'react-redux'
import { theme } from '../../helpers/theme'
import { fetchEvents, resetEvents } from '../../store/actionCreators'
import { formatUpdatedAt } from '../../helpers'
import Box from '@material-ui/core/Box'
import { DateTimePicker } from '@material-ui/pickers';
import MqEmpty from '../../components/core/empty/MqEmpty'
import MqText from '../../components/core/text/MqText'
import MqJson from '../../components/core/code/MqJson'
import createStyles from '@material-ui/core/styles/createStyles'
import withStyles, { WithStyles } from '@material-ui/core/styles/withStyles'

const styles = (theme: Theme) => {
  return createStyles({
    nav: {
      display: 'flex',
      gap: '20px'
    },
    type: {
      display: 'flex',
      alignItems: 'center',
      gap: '5px'
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
  rowExpanded: number | null
  before: string
  after: string
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
  constructor(props: EventsProps) {
    super(props);
    this.state = {
      rowExpanded: null,
      after: this.formatDateQuery(moment().startOf('day').toString()), 
      before: this.formatDateQuery(moment().endOf('day').toString())
    };
  }

  componentDidMount() {
    const { after, before } = this.state
    this.props.fetchEvents(after, before)
  }

  componentWillUnmount() {
    this.props.resetEvents()
  }

  formatDate(val: string) {
    return moment(val).format("YYYY-MM-DDTHH:mm:ss")
  }

  formatDateQuery(val: string) {
    return moment(val).format("YYYY-MM-DDTHH:mm:ss[.000Z]")
  }

  render() {
    const { classes, events, isEventsLoading, isEventsInit } = this.props
    const { rowExpanded } = this.state

    return (
      <Container maxWidth={'lg'} disableGutters>
        <MqScreenLoad loading={isEventsLoading || !isEventsInit}>
          <>
            <Box p={2}>
              <MqText heading>EVENTS</MqText>
              Total: {events.length}
            </Box>
            <Box p={2} className={classes.nav}>
              <DateTimePicker
                label="Date after"
                value={this.formatDate(this.state.after)}
                onChange={(e: any) => {
                  this.props.fetchEvents(this.formatDateQuery(e.toDate()), this.formatDateQuery(this.state.after))
                  this.setState({ after: this.formatDate(e.toDate()) })
                }}
              />

              <DateTimePicker
                label="Date before"
                value={this.formatDate(this.state.before)}
                onChange={(e: any) => {
                  this.props.fetchEvents(this.formatDateQuery(this.state.after), this.formatDateQuery(e.toDate()))
                  this.setState({ before: this.formatDate(e.toDate()) })
                }}
              />
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
                    {events.map((event, key: number) => {
                      return (
                        <React.Fragment key={key}>
                          <TableRow
                            className={classes.row}
                            onClick={() => {
                              this.setState({ rowExpanded: key === rowExpanded ? null : key });
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
                                <MqJson code={event} wrapLongLines={true} showLineNumbers={true} />
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
