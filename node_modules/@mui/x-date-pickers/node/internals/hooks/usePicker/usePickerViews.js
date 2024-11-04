"use strict";

var _interopRequireDefault = require("@babel/runtime/helpers/interopRequireDefault").default;
var _interopRequireWildcard = require("@babel/runtime/helpers/interopRequireWildcard").default;
Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.usePickerViews = void 0;
var _extends2 = _interopRequireDefault(require("@babel/runtime/helpers/extends"));
var _objectWithoutPropertiesLoose2 = _interopRequireDefault(require("@babel/runtime/helpers/objectWithoutPropertiesLoose"));
var React = _interopRequireWildcard(require("react"));
var _useEnhancedEffect = _interopRequireDefault(require("@mui/utils/useEnhancedEffect"));
var _useEventCallback = _interopRequireDefault(require("@mui/utils/useEventCallback"));
var _useViews = require("../useViews");
var _timeUtils = require("../../utils/time-utils");
const _excluded = ["className", "sx"];
/**
 * Props used to handle the views that are common to all pickers.
 */

/**
 * Props used to handle the views of the pickers.
 */

/**
 * Props used to handle the value of the pickers.
 */

/**
 * Manage the views of all the pickers:
 * - Handles the view switch
 * - Handles the switch between UI views and field views
 * - Handles the focus management when switching views
 */
const usePickerViews = ({
  props,
  propsFromPickerValue,
  additionalViewProps,
  autoFocusView,
  rendererInterceptor,
  fieldRef
}) => {
  const {
    onChange,
    open,
    onClose
  } = propsFromPickerValue;
  const {
    view: inView,
    views,
    openTo,
    onViewChange,
    viewRenderers,
    timezone
  } = props;
  const propsToForwardToView = (0, _objectWithoutPropertiesLoose2.default)(props, _excluded);
  const {
    view,
    setView,
    defaultView,
    focusedView,
    setFocusedView,
    setValueAndGoToNextView
  } = (0, _useViews.useViews)({
    view: inView,
    views,
    openTo,
    onChange,
    onViewChange,
    autoFocus: autoFocusView
  });
  const {
    hasUIView,
    viewModeLookup
  } = React.useMemo(() => views.reduce((acc, viewForReduce) => {
    let viewMode;
    if (viewRenderers[viewForReduce] != null) {
      viewMode = 'UI';
    } else {
      viewMode = 'field';
    }
    acc.viewModeLookup[viewForReduce] = viewMode;
    if (viewMode === 'UI') {
      acc.hasUIView = true;
    }
    return acc;
  }, {
    hasUIView: false,
    viewModeLookup: {}
  }), [viewRenderers, views]);
  const timeViewsCount = React.useMemo(() => views.reduce((acc, viewForReduce) => {
    if (viewRenderers[viewForReduce] != null && (0, _timeUtils.isTimeView)(viewForReduce)) {
      return acc + 1;
    }
    return acc;
  }, 0), [viewRenderers, views]);
  const currentViewMode = viewModeLookup[view];
  const shouldRestoreFocus = (0, _useEventCallback.default)(() => currentViewMode === 'UI');
  const [popperView, setPopperView] = React.useState(currentViewMode === 'UI' ? view : null);
  if (popperView !== view && viewModeLookup[view] === 'UI') {
    setPopperView(view);
  }
  (0, _useEnhancedEffect.default)(() => {
    // Handle case of `DateTimePicker` without time renderers
    if (currentViewMode === 'field' && open) {
      onClose();
      setTimeout(() => {
        fieldRef?.current?.setSelectedSections(view);
        // focusing the input before the range selection is done
        // calling it outside of timeout results in an inconsistent behavior between Safari And Chrome
        fieldRef?.current?.focusField(view);
      });
    }
  }, [view]); // eslint-disable-line react-hooks/exhaustive-deps

  (0, _useEnhancedEffect.default)(() => {
    if (!open) {
      return;
    }
    let newView = view;

    // If the current view is a field view, go to the last popper view
    if (currentViewMode === 'field' && popperView != null) {
      newView = popperView;
    }

    // If the current view is not the default view and both are UI views
    if (newView !== defaultView && viewModeLookup[newView] === 'UI' && viewModeLookup[defaultView] === 'UI') {
      newView = defaultView;
    }
    if (newView !== view) {
      setView(newView);
    }
    setFocusedView(newView, true);
  }, [open]); // eslint-disable-line react-hooks/exhaustive-deps

  const layoutProps = {
    views,
    view: popperView,
    onViewChange: setView
  };
  return {
    hasUIView,
    shouldRestoreFocus,
    layoutProps,
    renderCurrentView: () => {
      if (popperView == null) {
        return null;
      }
      const renderer = viewRenderers[popperView];
      if (renderer == null) {
        return null;
      }
      const rendererProps = (0, _extends2.default)({}, propsToForwardToView, additionalViewProps, propsFromPickerValue, {
        views,
        timezone,
        onChange: setValueAndGoToNextView,
        view: popperView,
        onViewChange: setView,
        focusedView,
        onFocusedViewChange: setFocusedView,
        showViewSwitcher: timeViewsCount > 1,
        timeViewsCount
      });
      if (rendererInterceptor) {
        return rendererInterceptor(viewRenderers, popperView, rendererProps);
      }
      return renderer(rendererProps);
    }
  };
};
exports.usePickerViews = usePickerViews;