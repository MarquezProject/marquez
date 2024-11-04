'use client';

import * as React from 'react';
import PropTypes from 'prop-types';
import clsx from 'clsx';
import integerPropType from '@mui/utils/integerPropType';
import composeClasses from '@mui/utils/composeClasses';
import Paper from "../Paper/index.js";
import capitalize from "../utils/capitalize.js";
import LinearProgress from "../LinearProgress/index.js";
import { styled } from "../zero-styled/index.js";
import memoTheme from "../utils/memoTheme.js";
import { useDefaultProps } from "../DefaultPropsProvider/index.js";
import slotShouldForwardProp from "../styles/slotShouldForwardProp.js";
import { getMobileStepperUtilityClass } from "./mobileStepperClasses.js";
import { jsxs as _jsxs, jsx as _jsx } from "react/jsx-runtime";
const useUtilityClasses = ownerState => {
  const {
    classes,
    position
  } = ownerState;
  const slots = {
    root: ['root', `position${capitalize(position)}`],
    dots: ['dots'],
    dot: ['dot'],
    dotActive: ['dotActive'],
    progress: ['progress']
  };
  return composeClasses(slots, getMobileStepperUtilityClass, classes);
};
const MobileStepperRoot = styled(Paper, {
  name: 'MuiMobileStepper',
  slot: 'Root',
  overridesResolver: (props, styles) => {
    const {
      ownerState
    } = props;
    return [styles.root, styles[`position${capitalize(ownerState.position)}`]];
  }
})(memoTheme(({
  theme
}) => ({
  display: 'flex',
  flexDirection: 'row',
  justifyContent: 'space-between',
  alignItems: 'center',
  background: (theme.vars || theme).palette.background.default,
  padding: 8,
  variants: [{
    props: ({
      position
    }) => position === 'top' || position === 'bottom',
    style: {
      position: 'fixed',
      left: 0,
      right: 0,
      zIndex: (theme.vars || theme).zIndex.mobileStepper
    }
  }, {
    props: {
      position: 'top'
    },
    style: {
      top: 0
    }
  }, {
    props: {
      position: 'bottom'
    },
    style: {
      bottom: 0
    }
  }]
})));
const MobileStepperDots = styled('div', {
  name: 'MuiMobileStepper',
  slot: 'Dots',
  overridesResolver: (props, styles) => styles.dots
})({
  variants: [{
    props: {
      variant: 'dots'
    },
    style: {
      display: 'flex',
      flexDirection: 'row'
    }
  }]
});
const MobileStepperDot = styled('div', {
  name: 'MuiMobileStepper',
  slot: 'Dot',
  shouldForwardProp: prop => slotShouldForwardProp(prop) && prop !== 'dotActive',
  overridesResolver: (props, styles) => {
    const {
      dotActive
    } = props;
    return [styles.dot, dotActive && styles.dotActive];
  }
})(memoTheme(({
  theme
}) => ({
  variants: [{
    props: {
      variant: 'dots'
    },
    style: {
      transition: theme.transitions.create('background-color', {
        duration: theme.transitions.duration.shortest
      }),
      backgroundColor: (theme.vars || theme).palette.action.disabled,
      borderRadius: '50%',
      width: 8,
      height: 8,
      margin: '0 2px'
    }
  }, {
    props: {
      variant: 'dots',
      dotActive: true
    },
    style: {
      backgroundColor: (theme.vars || theme).palette.primary.main
    }
  }]
})));
const MobileStepperProgress = styled(LinearProgress, {
  name: 'MuiMobileStepper',
  slot: 'Progress',
  overridesResolver: (props, styles) => styles.progress
})({
  variants: [{
    props: {
      variant: 'progress'
    },
    style: {
      width: '50%'
    }
  }]
});
const MobileStepper = /*#__PURE__*/React.forwardRef(function MobileStepper(inProps, ref) {
  const props = useDefaultProps({
    props: inProps,
    name: 'MuiMobileStepper'
  });
  const {
    activeStep = 0,
    backButton,
    className,
    LinearProgressProps,
    nextButton,
    position = 'bottom',
    steps,
    variant = 'dots',
    ...other
  } = props;
  const ownerState = {
    ...props,
    activeStep,
    position,
    variant
  };
  let value;
  if (variant === 'progress') {
    if (steps === 1) {
      value = 100;
    } else {
      value = Math.ceil(activeStep / (steps - 1) * 100);
    }
  }
  const classes = useUtilityClasses(ownerState);
  return /*#__PURE__*/_jsxs(MobileStepperRoot, {
    square: true,
    elevation: 0,
    className: clsx(classes.root, className),
    ref: ref,
    ownerState: ownerState,
    ...other,
    children: [backButton, variant === 'text' && /*#__PURE__*/_jsxs(React.Fragment, {
      children: [activeStep + 1, " / ", steps]
    }), variant === 'dots' && /*#__PURE__*/_jsx(MobileStepperDots, {
      ownerState: ownerState,
      className: classes.dots,
      children: [...new Array(steps)].map((_, index) => /*#__PURE__*/_jsx(MobileStepperDot, {
        className: clsx(classes.dot, index === activeStep && classes.dotActive),
        ownerState: ownerState,
        dotActive: index === activeStep
      }, index))
    }), variant === 'progress' && /*#__PURE__*/_jsx(MobileStepperProgress, {
      ownerState: ownerState,
      className: classes.progress,
      variant: "determinate",
      value: value,
      ...LinearProgressProps
    }), nextButton]
  });
});
process.env.NODE_ENV !== "production" ? MobileStepper.propTypes /* remove-proptypes */ = {
  // ┌────────────────────────────── Warning ──────────────────────────────┐
  // │ These PropTypes are generated from the TypeScript type definitions. │
  // │    To update them, edit the d.ts file and run `pnpm proptypes`.     │
  // └─────────────────────────────────────────────────────────────────────┘
  /**
   * Set the active step (zero based index).
   * Defines which dot is highlighted when the variant is 'dots'.
   * @default 0
   */
  activeStep: integerPropType,
  /**
   * A back button element. For instance, it can be a `Button` or an `IconButton`.
   */
  backButton: PropTypes.node,
  /**
   * Override or extend the styles applied to the component.
   */
  classes: PropTypes.object,
  /**
   * @ignore
   */
  className: PropTypes.string,
  /**
   * Props applied to the `LinearProgress` element.
   */
  LinearProgressProps: PropTypes.object,
  /**
   * A next button element. For instance, it can be a `Button` or an `IconButton`.
   */
  nextButton: PropTypes.node,
  /**
   * Set the positioning type.
   * @default 'bottom'
   */
  position: PropTypes.oneOf(['bottom', 'static', 'top']),
  /**
   * The total steps.
   */
  steps: integerPropType.isRequired,
  /**
   * The system prop that allows defining system overrides as well as additional CSS styles.
   */
  sx: PropTypes.oneOfType([PropTypes.arrayOf(PropTypes.oneOfType([PropTypes.func, PropTypes.object, PropTypes.bool])), PropTypes.func, PropTypes.object]),
  /**
   * The variant to use.
   * @default 'dots'
   */
  variant: PropTypes.oneOf(['dots', 'progress', 'text'])
} : void 0;
export default MobileStepper;