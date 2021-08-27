import {
  FETCH_LINEAGE_FAILED,
  FETCH_LINEAGE_REQUESTED,
  FETCH_LINEAGE_SUCCESS
} from '../constants/ActionTypes'
import { call, put, takeLatest } from 'redux-saga/effects'
import { fetchLineage } from '../requests/lineage'

function* fetchMyLineage(action: any) {
  try {
    const lineage = yield call(fetchLineage, action.payload.nodeId)
    yield put({ type: FETCH_LINEAGE_SUCCESS, lineage: lineage })
  } catch (e) {
    yield put({ type: FETCH_LINEAGE_FAILED, message: e.message })
  }
}

function* myLineageSaga() {
  yield takeLatest(FETCH_LINEAGE_REQUESTED, fetchMyLineage)
}

export default myLineageSaga
