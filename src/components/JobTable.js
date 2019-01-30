import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Paper from '@material-ui/core/Paper';

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

let id = 0;
function createData(name, description, createdAt) {
  id += 1;
  return { name, description, createdAt };
}

const rows = [
  createData('Job 1', 'Job 1 Description', '2019-01-29T23:19:03+0000'),
  createData('Job 2', 'Job 2 Description', '2019-01-29T23:19:03+0000'),
  createData('Job 3', 'Job 3 Description', '2019-01-29T23:19:03+0000')
];

function SimpleTable(props) {
  const { classes, jobs } = props;

  return (
    <Paper className={classes.root}>
      <Table className={classes.table}>
        <TableHead>
          <TableRow>
            <TableCell>Name</TableCell>
            <TableCell align="right">Description</TableCell>
            <TableCell align="right">Created At</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {rows.map(job => (
            <TableRow key={job.id}>
              <TableCell component="th" scope="row">
                {job.name}
              </TableCell>
              <TableCell align="right">{job.description}</TableCell>
              <TableCell align="right">{job.createdAt}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </Paper>
  );
}

SimpleTable.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(SimpleTable);