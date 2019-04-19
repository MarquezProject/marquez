import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';

import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogTitle from '@material-ui/core/DialogTitle';
import Button from '@material-ui/core/Button';

import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';

const styles = theme => ({
    root: {
      width: '100%',
      marginTop: theme.spacing.unit * 3,
      overflowX: 'auto',
    },
    table: {
      minWidth: 700,
    },
  });

function JobRunsTable(props) {
    const { classes } = props;
    return (
        <Table>
            <TableHead>
                <TableRow>
                    <TableCell>RUN ARGUMENTS</TableCell>
                    <TableCell align="right">FINAL STATE</TableCell>
                </TableRow>
            </TableHead>
            <TableBody>
                {
                    props.runs.map(row => (
                        <TableRow key={row.runId}>
                            <TableCell component="th" scope="row">
                                {row.runArgs}
                            </TableCell>
                            <TableCell align="right">{row.runState}</TableCell>
                        </TableRow>
                    ))
                }
            </TableBody>
        </Table>
    )
}

function JobDetailsDialog(props) {
    return (
        <Dialog
            fullWidth="sm"
            maxWidth="sm"
            open={props.open}
            onClose={props.onClose}
            aria-labelledby="max-width-dialog-title"
        >
            <DialogTitle id="max-width-dialog-title">Job Details</DialogTitle>
            <DialogContent>
                <JobRunsTable runs={props.jobDetails.runs} />
            </DialogContent>
            <DialogActions>
                <Button onClick={props.onClose} color="primary">
                    Close
                </Button>
            </DialogActions>
        </Dialog>
    );
}

JobDetailsDialog.propTypes = {
    open: PropTypes.bool.isRequired,
    onClose: PropTypes.func.isRequired,
    jobDetails: PropTypes.object.isRequired
}

JobDetailsDialog.defaultProps = {
    open: false,
    onClose: function(){},
    jobDetails: null
}

export default withStyles(styles)(JobDetailsDialog);