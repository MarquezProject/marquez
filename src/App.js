import React, { Component } from 'react';
import './App.css';
import SimpleTabs from './components/Tabs'
import NavBar from './components/NavBar'
import NamespaceSelector from './components/NamespaceSelector'


class App extends Component {
  render() {
    return (
      <div>
        <div>
          <NavBar />
        </div>
        <div>
          <NamespaceSelector />
        </div>
        <div>
          <SimpleTabs />
        </div>
      </div>
    );
  }
}

export default App;
