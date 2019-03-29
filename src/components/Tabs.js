import React from 'react';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import AppBar from '@material-ui/core/AppBar';
import Tabs from '@material-ui/core/Tabs';
import Tab from '@material-ui/core/Tab';
import Typography from '@material-ui/core/Typography';
import MUIDataTable from "mui-datatables";
import TableRow from "@material-ui/core/TableRow";
import TableCell from "@material-ui/core/TableCell";
import axios from 'axios'

import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import FormControl from '@material-ui/core/FormControl';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import InputLabel from '@material-ui/core/InputLabel';
import MenuItem from '@material-ui/core/MenuItem';
import Select from '@material-ui/core/Select';
import Switch from '@material-ui/core/Switch';
import Button from '@material-ui/core/Button';

import JobDetailsDialog from './JobDetailsDialog';


function TabContainer(props) {
  return (
    <Typography component="div" style={{ padding: 8 * 3 }}>
      {props.children}
    </Typography>
  );
}

TabContainer.propTypes = {
  children: PropTypes.node.isRequired,
  selectedNamespace: PropTypes.node.isRequired
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
        namespace: null
      };
  }

  fetchData(namespace) {
    axios.get('/api/v1/namespaces/' + namespace + '/jobs/').then((response) => {
      const jobData = response.data
      const jobRows = jobData.jobs.map(job => [job.name, job.description, job.createdAt])
      this.setState({jobs: jobRows})
    });
    axios.get('/api/v1/namespaces/' + namespace + '/datasets/').then((response) => {
      const datasetData = response.data
      const datasetRows = datasetData.datasets.map(dataset => [dataset.name, dataset.urn, dataset.createdAt])
      this.setState({datasets: datasetRows})
    }); 
  }

  componentDidMount(){
    this.fetchData(this.state.selectedNamespace); 
  }

  componentDidUpdate(prevProps) {
    if(this.props.namespace != prevProps.namespace) {
      this.setState({namespace: this.props.namespace});
      this.fetchData(this.props.namespace);
    }
  }

  handleChange = (event, value) => {
    this.setState({ value });
  };

  handleJobRowClick = (rowData, rowState) => {
    const jobName = rowData[0]
    var jobInfo = {}
    var jobRuns = []
    axios.get('/api/v1/namespaces/' + this.state.namespace + '/jobs/' + jobName + '/runs' ).then((response) => {
      //console.log(response.data);
      jobRuns = response.data;
      this.setState(
        {
          jobDetails: {
            runs: jobRuns
          }
        })
      this.setState({showJobDetails: true});
    });
  }

  handleJobDetailsClose = () => {
    this.setState({showJobDetails: false});
  }

  render() {
    const { classes } = this.props;
    const { value } = this.state;
    const jobColumns = [
        "Name",
        "Description",
        "Created At"
    ];
    const datasetColumns = ["URN", "Created At"];

    const options = {
        filter: true,
        filterType: 'dropdown',
        onRowClick: this.handleJobRowClick
    };

    return (
      <React.Fragment>
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
          }      
        </div>

        {/* Job Details Dialog */}
        <JobDetailsDialog
          open={this.state.showJobDetails}
          onClose={this.handleJobDetailsClose}
          jobDetails={this.state.jobDetails}
        />

        {/* Dataset Details Dialog */}
      </React.Fragment>
    );
  }
}

SimpleTabs.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(SimpleTabs);