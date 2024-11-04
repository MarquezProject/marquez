'use client';

import * as React from 'react';
import PropTypes from 'prop-types';
import clsx from 'clsx';
import composeClasses from '@mui/utils/composeClasses';
import capitalize from "../utils/capitalize.js";
import { styled } from "../zero-styled/index.js";
import memoTheme from "../utils/memoTheme.js";
import { useDefaultProps } from "../DefaultPropsProvider/index.js";
import StepperContext from "../Stepper/StepperContext.js";
import StepContext from "../Step/StepContext.js";
import { getStepConnectorUtilityClass } from "./stepConnectorClasses.js";
import { jsx as _jsx } from "react/jsx-runtime";
const useUtilityClasses = ownerState => {
  const {
    classes,
    orientation,
    alternativeLabel,
    active,
    completed,
    disabled
  } = ownerState;
  const slots = {
    root: ['root', orientation, alternativeLabel && 'alternativeLabel', active && 'active', completed && 'completed', disabled && 'disabled'],
    line: ['line', `line${capitalize(orientation)}`]
  };
  return composeClasses(slots, getStepConnectorUtilityClass, classes);
};
const StepConnectorRoot = styled('div', {
  name: 'MuiStepConnector',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [styles.root, styles[ownerState.orientation], ownerState.alternativeLabel && styles.alternativeLabel, ownerState.completed && styles.completed];
  }
})({
  flex: '1 1 auto',
  variants: [{
    props: {
      orientation: 'vertical'
    },
    style: {
      marginLeft: 12 // half icon
    }
  }, {
    props: {
      alternativeLabel: true
    },
    style: {
      position: 'absolute',
      top: 8 + 4,
      left: 'calc(-50% + 20px)',
      right: 'calc(50% + 20px)'
    }
  }]
});
const StepConnectorLine = styled('span', {
  name: 'MuiStepConnector',
  slot: 'Line',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [styles.line, styles[`line${capitalize(ownerState.orientation)}`]];
  }
})(memoTheme(({
  theme
}) => {
  const borderColor = theme.palette.mode === 'light' ? theme.palette.grey[400] : theme.palette.grey[600];
  return {
    display: 'block',
    borderColor: theme.vars ? theme.vars.palette.StepConnector.border : borderColor,
    variants: [{
      props: {
        orientation: 'horizontal'
      },
      style: {
        borderTopStyle: 'solid',
        borderTopWidth: 1
      }
    }, {
      props: {
        orientation: 'vertical'
      },
      style: {
        borderLeftStyle: 'solid',
        borderLeftWidth: 1,
        minHeight: 24
      }
    }]
  };
}));
const StepConnector = /*#__PURE__*/React.forwardRef(function StepConnector(inProps, ref) {
  const props = useDefaultProps({
    props: inProps,
    name: 'MuiStepConnector'
  });
  const {
    className,
    ...other
  } = props;
  const {
    alternativeLabel,
    orientation = 'horizontal'
  } = React.useContext(StepperContext);
  const {
    active,
    disabled,
    completed
  } = React.useContext(StepContext);
  const ownerState = {
    ...props,
    alternativeLabel,
    orientation,
    active,
    completed,
    disabled
  };
  const classes = useUtilityClasses(ownerState);
  return /*#__PURE__*/_jsx(StepConnectorRoot, {
    className: clsx(classes.root, className),
    ref: ref,
    ownerState: ownerState,
    ...other,
    children: /*#__PURE__*/_jsx(StepConnectorLine, {
      className: classes.line,
      ownerState: ownerState
    })
  });
});
process.env.NODE_ENV !== "production" ? StepConnector.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * Override or extend the styles applied to the component.
   */
  classes: PropTypes.object,
  /**
   * @ignore
   */
  className: PropTypes.string,
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: PropTypes.oneOfType([PropTypes.arrayOf(PropTypes.oneOfType([PropTypes.func, PropTypes.object, PropTypes.bool])), PropTypes.func, PropTypes.object])
} : void 0;
export default StepConnector;