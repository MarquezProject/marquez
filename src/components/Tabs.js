import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import AppBar from '@material-ui/core/AppBar';
import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';
import Typography from '@material-ui/core/Typography';
import DatasetTable from './DatasetTable';
import MUIDataTable from "mui-datatables";
import TableRow from "@material-ui/core/TableRow";
import TableCell from "@material-ui/core/TableCell";
import axios from 'axios'

function TabContainer(props) {
  return (
    <Typography component="div" style={{ padding: 8 * 3 }}>
      {props.children}
    </Typography>
  );
}

TabContainer.propTypes = {
  children: PropTypes.node.isRequired,
};

const styles = theme => ({
  root: {
    flexGrow: 1,
    backgroundColor: theme.palette.background.paper,
  },
});

class SimpleTabs extends React.Component {
  constructor(props) {
      super(props);
      this.state = {
        value: 0,
        jobs: [],
        datasets: [],
      };
  }

  componentDidMount(){
    axios.get('/api/v1/namespaces/demo/jobs/').then((response) => {
        const jobData = response.data
        const jobRows = jobData.jobs.map(job => [job.name, job.description, job.createdAt])
        this.setState({jobs: jobRows})
    })
    axios.get('/api/v1/namespaces/demo/datasets/').then((response) => {
      const datasetData = response.data
      const datasetRows = datasetData.datasets.map(dataset => [dataset.name, dataset.urn, dataset.createdAt])
      this.setState({datasets: datasetRows})
    })
  }

  handleChange = (event, value) => {
    this.setState({ value });
  };

  render() {
    const { classes } = this.props;
    const { value } = this.state;
    const jobColumns = ["Name", "Description", "Created At"];
    const datasetColumns = ["Name", "urn", "Created At"];

    const options = {
        filter: true,
        filterType: 'dropdown',
        expandableRows: true,
        renderExpandableRow: (rowData, rowMeta) => {
          const colSpan = rowData.length + 1;
          return (
            <TableRow>
              <TableCell colSpan={colSpan}>
                Custom expandable row option. Data: {JSON.stringify(rowData)}
              </TableCell>
            </TableRow>
          );
        }   
    };

    return (
      <div className={classes.root}>
        <AppBar position="static">
          <Tabs value={value} onChange={this.handleChange}>
            <Tab label="Jobs" />
            <Tab label="Datasets" />
          </Tabs>
        </AppBar>
        {value === 0 && 
        <TabContainer>
            <MUIDataTable 
                title={"Jobs"}
                data={this.state.jobs}
                columns={jobColumns}
                options={options}
            />
        </TabContainer>
        }
        {value === 1 && 
        <TabContainer>
            <MUIDataTable 
                title={"Datasets"}
                data={this.state.datasets}
                columns={datasetColumns}
                options={options}
            />
        </TabContainer>
        }      </div>
    );
  }
}

SimpleTabs.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(SimpleTabs);