![License](https://img.shields.io/github/license/lagunovsky/redux-react-router)
![TypeScript](https://img.shields.io/badge/typescript-%3E%3D4.0.0-blue)
![Tests workflow](https://github.com/lagunovsky/redux-react-router/actions/workflows/publish.yml/badge.svg)

Redux React Router
======================

A Redux binding for React Router

- [Main features](#main-features)
- [Installation](#installation)
- [Usage](#usage)
- - [Examples](#examples)
- - [API](#api)
- [Migrate from Connected React Router](#migrate-from-connected-react-router)

## Main features

- Synchronize router state with redux store through uni-directional flow (i.e. history -> store -> router -> components).
- Supports [React Router v6](https://github.com/ReactTraining/react-router/tree/dev) and [History v5](https://github.com/ReactTraining/history)
- Supports functional component hot reloading while preserving state.
- Dispatching of history methods (`push`, `replace`, `go`, `back`, `forward`) works for both [redux-thunk](https://github.com/gaearon/redux-thunk)
  and [redux-saga](https://github.com/yelouafi/redux-saga).
- Nested children can access routing state such as the current location directly with `react-redux`'s `connect`.
- Supports time traveling in Redux DevTools.
- TypeScript

## Installation

Redux React Router requires **React 16.8, React Redux 6.0, React Router 6.0 or later**.

```shell
npm install --save @lagunovsky/redux-react-router
```

```shell
yarn add @lagunovsky/redux-react-router
```

## Usage

### Examples

Note: the `history` object provided to reducer, middleware, and component must be the same `history` object.

#### `@reduxjs/toolkit`

```typescript jsx
import { createRouterMiddleware, createRouterReducerMapObject, push, ReduxRouter } from '@lagunovsky/redux-react-router'
import { configureStore } from '@reduxjs/toolkit'
import { createBrowserHistory } from 'history'
import React from 'react'
import { createRoot } from 'react-dom/client'
import { Provider, useDispatch } from 'react-redux'
import { Route, Routes } from 'react-router'
import { NavLink } from 'react-router-dom'

const history = createBrowserHistory()
const routerMiddleware = createRouterMiddleware(history)

const store = configureStore({
  reducer: createRouterReducerMapObject(history),
  middleware: (getDefaultMiddleware) => getDefaultMiddleware().prepend(routerMiddleware),
})

function Content() {
  const dispatch = useDispatch()

  const onClickHandler = () => {
    const action = push(Date.now().toString())
    dispatch(action)
  }

  return (
    <>
      <NavLink to={'/'} children={'Home'}/>
      <button type={'button'} onClick={onClickHandler} children={'Rand'}/>
    </>
  )
}

function App() {
  return (
    <Provider store={store}>
      <ReduxRouter history={history}>
        <Routes>
          <Route path={'*'} element={<Content/>}/>
        </Routes>
      </ReduxRouter>
    </Provider>
  )
}
```

#### `redux.createStore` and custom selector

```typescript jsx
import { createRouterMiddleware, createRouterReducer, push, ReduxRouter, ReduxRouterSelector } from '@lagunovsky/redux-react-router'
import { createBrowserHistory } from 'history'
import React from 'react'
import { createRoot } from 'react-dom/client'
import { Provider, useDispatch } from 'react-redux'
import { Route, Routes } from 'react-router'
import { NavLink } from 'react-router-dom'
import { applyMiddleware, combineReducers, compose, createStore } from 'redux'

const history = createBrowserHistory()
const routerMiddleware = createRouterMiddleware(history)

const rootReducer = combineReducers({ navigator: createRouterReducer(history) })

const store = createStore(rootReducer, compose(applyMiddleware(routerMiddleware)))
type State = ReturnType<typeof store.getState>

const routerSelector: ReduxRouterSelector<State> = (state) => state.navigator

function App() {
  return (
    <Provider store={store}>
      <ReduxRouter history={history} routerSelector={routerSelector}>
        <Routes>
          <Route path={'*'} element={<div/>}/>
        </Routes>
      </ReduxRouter>
    </Provider>
  )
}
```

### API

#### Types

```typescript jsx
type ReduxRouterProps = {
  history: History
  basename?: string
  children?: React.ReactNode
  routerSelector?: ReduxRouterSelector 
}
```

```typescript jsx
type ReduxRouterState = {
  location: history.Location
  action: history.Action
}
```

#### Constants

```typescript jsx
const ROUTER_REDUCER_MAP_KEY = 'router'
const ROUTER_CALL_HISTORY_METHOD = '@@router/CALL_HISTORY_METHOD'
const ROUTER_ON_LOCATION_CHANGED = '@@router/ON_LOCATION_CHANGED'
```

#### `createRouterMiddleware(history: History) => Middleware`

A middleware you can apply to your Redux store to capture dispatched actions created by the action creators. 
It will redirect those actions to the provided history instance.

#### `createRouterReducerMapObject(history: History) => {router: Reducer<ReduxRouterState>}`

Creates a reducer map object that stores location updates from history.

#### `createRouterReducer(history: History) => Reducer<ReduxRouterState>`

Creates a reducer function that stores location updates from history.
Note: If you create a reducer with a key other than `ROUTER_REDUCER_MAP_KEY`, 
you must add a selector `(state: State) => ReduxRouterState` to your `<ReduxRouter/>` as `routerSelector` prop.

#### `reduxRouterSelector(state: State): ReduxRouterState`

Selector that returns location updates from history.

### Action creators

Same as [history methods](https://github.com/remix-run/history/blob/dev/docs/navigation.md)

- `push`
- `replace`
- `go` 
- `back`
- `forward`

By default, history methods calls in middleware are wrapped in a `queueMicrotask`. If you want to avoid it, please use the following methods:

- `pushStraight` 
- `replaceStraight` 
- `goStraight` 
- `backStraight` 
- `forwardStraight`

## Migrate from Connected React Router

```diff
- import { connectRouter } from 'connected-react-router'
+ import { createRouterReducer } from '@lagunovsky/redux-react-router'

- export const routerReducer = connectRouter(history)
+ export const routerReducer = createRouterReducer(history)
```

```diff
- import { routerMiddleware } from 'connected-react-router'
+ import { createRouterMiddleware } from '@lagunovsky/redux-react-router'

- export const routerMiddleware = routerMiddleware(history)
+ export const routerMiddleware = createRouterMiddleware(history)
```

```diff
- import { ConnectedRouter } from 'connected-react-router'
+ import { ReduxRouter } from '@lagunovsky/redux-react-router'

- <ConnectedRouter history={history} />
+ <ReduxRouter history={history} />
```

```diff
- import { RouterState } from 'connected-react-router'
+ import { ReduxRouterState } from '@lagunovsky/redux-react-router'
```
