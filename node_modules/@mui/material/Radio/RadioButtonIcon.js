'use client';

import * as React from 'react';
import PropTypes from 'prop-types';
import RadioButtonUncheckedIcon from "../internal/svg-icons/RadioButtonUnchecked.js";
import RadioButtonCheckedIcon from "../internal/svg-icons/RadioButtonChecked.js";
import rootShouldForwardProp from "../styles/rootShouldForwardProp.js";
import { styled } from "../zero-styled/index.js";
import memoTheme from "../utils/memoTheme.js";
import { jsx as _jsx, jsxs as _jsxs } from "react/jsx-runtime";
const RadioButtonIconRoot = styled('span', {
  shouldForwardProp: rootShouldForwardProp
})({
  position: 'relative',
  display: 'flex'
});
const RadioButtonIconBackground = styled(RadioButtonUncheckedIcon)({
  // Scale applied to prevent dot misalignment in Safari
  transform: 'scale(1)'
});
const RadioButtonIconDot = styled(RadioButtonCheckedIcon)(memoTheme(({
  theme
}) => ({
  left: 0,
  position: 'absolute',
  transform: 'scale(0)',
  transition: theme.transitions.create('transform', {
    easing: theme.transitions.easing.easeIn,
    duration: theme.transitions.duration.shortest
  }),
  variants: [{
    props: {
      checked: true
    },
    style: {
      transform: 'scale(1)',
      transition: theme.transitions.create('transform', {
        easing: theme.transitions.easing.easeOut,
        duration: theme.transitions.duration.shortest
      })
    }
  }]
})));

/**
 * @ignore - internal component.
 */
function RadioButtonIcon(props) {
  const {
    checked = false,
    classes = {},
    fontSize
  } = props;
  const ownerState = {
    ...props,
    checked
  };
  return /*#__PURE__*/_jsxs(RadioButtonIconRoot, {
    className: classes.root,
    ownerState: ownerState,
    children: [/*#__PURE__*/_jsx(RadioButtonIconBackground, {
      fontSize: fontSize,
      className: classes.background,
      ownerState: ownerState
    }), /*#__PURE__*/_jsx(RadioButtonIconDot, {
      fontSize: fontSize,
      className: classes.dot,
      ownerState: ownerState
    })]
  });
}
process.env.NODE_ENV !== "production" ? RadioButtonIcon.propTypes /* remove-proptypes */ = {
  /**
   * If `true`, the component is checked.
   */
  checked: PropTypes.bool,
  /**
   * Override or extend the styles applied to the component.
   */
  classes: PropTypes.object,
  /**
   * The size of the component.
   * `small` is equivalent to the dense radio styling.
   */
  fontSize: PropTypes.oneOf(['small', 'medium'])
} : void 0;
export default RadioButtonIcon;