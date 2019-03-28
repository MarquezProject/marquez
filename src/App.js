import React, { Component } from 'react';
import './App.css';
import SimpleTabs from './components/Tabs'
import NavBar from './components/NavBar'
import NamespaceSelector from './components/NamespaceSelector'
import axios from 'axios'

class App extends Component {
  constructor(props) {
    super(props);
    this.state = {
      selectedNamespace: null,
      namespaces: []
    }
    this.nsChangeHandler = this.nsChangeHandler.bind(this);
  }

  nsChangeHandler(selectedNs) {
    this.setState({selectedNamespace: selectedNs})
  }

  componentDidMount() {
    axios.get('/api/v1/namespaces/').then((response) => {
      const namespaceList = response.data.namespaces.map(namespace => namespace.name)
      console.log(namespaceList)
      this.setState({namespaces: namespaceList})
      this.setState({selectedNamespace: namespaceList[0]})
    })
  }

  render() {
    return (
      <div>
        <div>
          <NavBar />
        </div>
        <div>
          <NamespaceSelector 
            namespaces={this.state.namespaces} 
            selectedNamespace={this.state.selectedNamespace}
            onChange={this.nsChangeHandler}
          />
        </div>
        <div>
          <SimpleTabs namespace={this.state.selectedNamespace}/>
        </div>
      </div>
    );
  }
}

export default App;
