import { Action, History, Location } from 'history'
import React, { useEffect, useRef } from 'react'
import { useDispatch, useSelector } from 'react-redux'
import { Router } from 'react-router'
import { Middleware, Reducer, UnknownAction } from 'redux'


// Actions

export type Methods = 'push' | 'replace' | 'go' | 'back' | 'forward'

/**
 * This action type will be dispatched when your history
 * receives a location change.
 */
export const ROUTER_ON_LOCATION_CHANGED = '@@router/ON_LOCATION_CHANGED'

export type LocationChangeAction = {
  type: typeof ROUTER_ON_LOCATION_CHANGED
  payload: {
    location: Location
    action: Action
  }
}

export const onLocationChanged = (location: Location, action: Action): LocationChangeAction => ({
  type: ROUTER_ON_LOCATION_CHANGED,
  payload: { location, action },
})

export function matchLocationChangeAction(action: UnknownAction): action is LocationChangeAction {
  return action.type === ROUTER_ON_LOCATION_CHANGED
}

/**
 * This action type will be dispatched by the history actions below.
 * If you're writing a middleware to watch for navigation events, be sure to
 * look for actions of this type.
 */
export const ROUTER_CALL_HISTORY_METHOD = '@@router/CALL_HISTORY_METHOD'

type UpdateLocationAction<M extends Methods> = {
  type: typeof ROUTER_CALL_HISTORY_METHOD
  payload: {
    method: M
    args: Parameters<History[M]>
    asEffect: boolean
  }
}

function updateLocation<M extends Methods>(method: M, asEffect = true) {
  return (...args: Parameters<History[M]>): UpdateLocationAction<M> => ({
    type: ROUTER_CALL_HISTORY_METHOD,
    payload: { method, args, asEffect },
  })
}

export function matchUpdateLocationActions(action: UnknownAction): action is UpdateLocationActions {
  return action.type === ROUTER_CALL_HISTORY_METHOD
}

/**
 * Pushes a new location onto the history stack, increasing its length by one.
 * If there were any entries in the stack after the current one, they are
 * lost.
 *
 * @param to - The new URL
 * @param state - Data to associate with the new location
 */
export const push = updateLocation('push')
export const pushStraight = updateLocation('push', false)

/**
 * Replaces the current location in the history stack with a new one.  The
 * location that was replaced will no longer be available.
 *
 * @param to - The new URL
 * @param state - Data to associate with the new location
 */
export const replace = updateLocation('replace')
export const replaceStraight = updateLocation('replace', false)

/**
 * Navigates to the next entry in the stack. Identical to go(1).
 */
export const go = updateLocation('go')
export const goStraight = updateLocation('go', false)

/**
 * Goes back one entry in the history stack. Identical to go(-1).
 */
export const back = updateLocation('back')
export const backStraight = updateLocation('back', false)

/**
 * Navigates to the next entry in the stack. Identical to go(1).
 */
export const forward = updateLocation('forward')
export const forwardStraight = updateLocation('forward', false)

export const routerActions = {
  push,
  replace,
  go,
  back,
  forward,
}

export type UpdateLocationActions =
  | ReturnType<typeof push>
  | ReturnType<typeof replace>
  | ReturnType<typeof go>
  | ReturnType<typeof back>
  | ReturnType<typeof forward>

export type RouterActions = LocationChangeAction | UpdateLocationActions

// Middleware

export function createRouterMiddleware(history: History): Middleware {
  return () => next => (action) => {
    if (!matchUpdateLocationActions(action as UnknownAction)) {
      return next(action)
    }

    const updateLocationAction = action as UpdateLocationActions
    const { method, args } = updateLocationAction.payload

    const callHistoryMethod = () => {
      // Typescript is not able to narrow the arguments types correctly, so we need to handle
      // each argument constellation separately
      switch(method) {
        case 'back':
          history.back()
          break
        case 'forward':
          history.forward()
          break
        case 'go':
          history[method](...args)
          break
        case 'push':
          history[method](...args)
          break
        case 'replace':
          history[method](...args)
          break
      }
    }

    if (updateLocationAction.payload.asEffect === true) {
      queueMicrotask(callHistoryMethod)
      return
    }

    callHistoryMethod()
  }
}


// Reducer

export const ROUTER_REDUCER_MAP_KEY = 'router'

export type ReduxRouterState = {
  location: Location
  action: Action
}

export function createRouterReducer(history: History): Reducer<ReduxRouterState> {
  const initialRouterState: ReduxRouterState = {
    location: history.location,
    action: history.action,
  }

  /*
  * This reducer will update the state with the most recent location history
  * has transitioned to.
  */
  return (state = initialRouterState, action: LocationChangeAction | UnknownAction) => {
    return matchLocationChangeAction(action) === true ? action.payload as ReduxRouterState : state
  }
}

export function createRouterReducerMapObject(history: History) {
  return {
    [ROUTER_REDUCER_MAP_KEY]: createRouterReducer(history),
  }
}

export type ReduxRouterSelector<T = any> = (state: T) => ReduxRouterState

export type ReduxRouterStoreState = { [ROUTER_REDUCER_MAP_KEY]: ReduxRouterState }

export function reduxRouterSelector<T extends ReduxRouterStoreState = ReduxRouterStoreState>(state: T): ReduxRouterState {
  return state[ROUTER_REDUCER_MAP_KEY]
}


// Component

export type ReduxRouterProps = {
  history: History
  basename?: string
  children?: React.ReactNode
  routerSelector?: ReduxRouterSelector
}

export function ReduxRouter({ routerSelector = reduxRouterSelector, ...props }: ReduxRouterProps) {
  const dispatch = useDispatch()
  const skipHistoryChange = useRef<boolean>()
  const state = useSelector(routerSelector)

  useEffect(() => {
    return props.history.listen((nextState) => {
      if (skipHistoryChange.current === true) {
        skipHistoryChange.current = false
        return
      }
      dispatch(onLocationChanged(nextState.location, nextState.action))
    })
  }, [ props.history, dispatch ])

  useEffect(() => {
    if (props.history.location !== state.location) {
      dispatch(onLocationChanged(props.history.location, props.history.action))
    }
  }, [])

  useEffect(() => {
    if (skipHistoryChange.current === undefined) {
      skipHistoryChange.current = false
    } else if (props.history.location !== state.location) {
      skipHistoryChange.current = true
      props.history.replace(state.location)
    }
  }, [ state.location, props.history ])

  return (
    <Router
      navigationType={state.action}
      location={state.location}
      basename={props.basename}
      navigator={props.history}
      children={props.children}
    />
  )
}
