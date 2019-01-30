import React from 'react';
import ReactDOM from 'react-dom';
import PropTypes from 'prop-types';
import { withStyles } from '@material-ui/core/styles';
import Input from '@material-ui/core/Input';
import OutlinedInput from '@material-ui/core/OutlinedInput';
import FilledInput from '@material-ui/core/FilledInput';
import InputLabel from '@material-ui/core/InputLabel';
import MenuItem from '@material-ui/core/MenuItem';
import FormHelperText from '@material-ui/core/FormHelperText';
import FormControl from '@material-ui/core/FormControl';
import Select from '@material-ui/core/Select';
import axios from 'axios'


const styles = theme => ({
  root: {
    display: 'flex',
    flexWrap: 'wrap',
  },
  formControl: {
    margin: theme.spacing.unit,
    minWidth: 120,
  },
  selectEmpty: {
    marginTop: theme.spacing.unit * 2,
  },
});

class NamespaceSelector extends React.Component {
  state = {
    selectedNamespace: '',
    namespaces: [],
    name: 'ns',
    labelWidth: 0,
  };

  componentDidMount() {
    this.setState({
      labelWidth: ReactDOM.findDOMNode(this.InputLabelRef).offsetWidth,
    });
    axios.get('/api/v1/namespaces/').then((response) => {
      const namespaceList = response.data.namespaces.map(namespace => namespace.name)
      console.log(namespaceList)
      this.setState({namespaces: namespaceList})
    })
  }

  handleChange = event => {
    this.setState({ [event.target.name]: event.target.value });
  };

  render() {
    const { classes } = this.props;

    return (
      <form className={classes.root} autoComplete="off">
        <FormControl variant="outlined" className={classes.formControl}>
          <InputLabel
            ref={ref => {
              this.InputLabelRef = ref;
            }}
            htmlFor="outlined-age-simple"
          >
            Namespace
          </InputLabel>
          <Select
            value={this.state.selectedNamespace}
            onChange={this.handleChange}
            input={
              <OutlinedInput
                labelWidth={this.state.labelWidth}
                name="namespace"
                id="outlined-age-simple"
              />
            }
          >
            <MenuItem value="">
              <em>None</em>
            </MenuItem>
            {
              this.state.namespaces.map(namespace => (
                <MenuItem key={namespace} value={namespace}>{namespace}</MenuItem>
              ))
            }
          </Select>
        </FormControl>
      </form>
    );
  }
}

NamespaceSelector.propTypes = {
  classes: PropTypes.object.isRequired,
};

export default withStyles(styles)(NamespaceSelector);