'use client';

var _span;
import * as React from 'react';
import PropTypes from 'prop-types';
import rootShouldForwardProp from "../styles/rootShouldForwardProp.js";
import { styled } from "../zero-styled/index.js";
import memoTheme from "../utils/memoTheme.js";
import { jsx as _jsx } from "react/jsx-runtime";
const NotchedOutlineRoot = styled('fieldset', {
  shouldForwardProp: rootShouldForwardProp
})({
  textAlign: 'left',
  position: 'absolute',
  bottom: 0,
  right: 0,
  top: -5,
  left: 0,
  margin: 0,
  padding: '0 8px',
  pointerEvents: 'none',
  borderRadius: 'inherit',
  borderStyle: 'solid',
  borderWidth: 1,
  overflow: 'hidden',
  minWidth: '0%'
});
const NotchedOutlineLegend = styled('legend', {
  shouldForwardProp: rootShouldForwardProp
})(memoTheme(({
  theme
}) => ({
  float: 'unset',
  // Fix conflict with bootstrap
  width: 'auto',
  // Fix conflict with bootstrap
  overflow: 'hidden',
  // Fix Horizontal scroll when label too long
  variants: [{
    props: ({
      ownerState
    }) => !ownerState.withLabel,
    style: {
      padding: 0,
      lineHeight: '11px',
      // sync with `height` in `legend` styles
      transition: theme.transitions.create('width', {
        duration: 150,
        easing: theme.transitions.easing.easeOut
      })
    }
  }, {
    props: ({
      ownerState
    }) => ownerState.withLabel,
    style: {
      display: 'block',
      // Fix conflict with normalize.css and sanitize.css
      padding: 0,
      height: 11,
      // sync with `lineHeight` in `legend` styles
      fontSize: '0.75em',
      visibility: 'hidden',
      maxWidth: 0.01,
      transition: theme.transitions.create('max-width', {
        duration: 50,
        easing: theme.transitions.easing.easeOut
      }),
      whiteSpace: 'nowrap',
      '& > span': {
        paddingLeft: 5,
        paddingRight: 5,
        display: 'inline-block',
        opacity: 0,
        visibility: 'visible'
      }
    }
  }, {
    props: ({
      ownerState
    }) => ownerState.withLabel && ownerState.notched,
    style: {
      maxWidth: '100%',
      transition: theme.transitions.create('max-width', {
        duration: 100,
        easing: theme.transitions.easing.easeOut,
        delay: 50
      })
    }
  }]
})));

/**
 * @ignore - internal component.
 */
export default function NotchedOutline(props) {
  const {
    children,
    classes,
    className,
    label,
    notched,
    ...other
  } = props;
  const withLabel = label != null && label !== '';
  const ownerState = {
    ...props,
    notched,
    withLabel
  };
  return /*#__PURE__*/_jsx(NotchedOutlineRoot, {
    "aria-hidden": true,
    className: className,
    ownerState: ownerState,
    ...other,
    children: /*#__PURE__*/_jsx(NotchedOutlineLegend, {
      ownerState: ownerState,
      children: withLabel ? /*#__PURE__*/_jsx("span", {
        children: label
      }) : // notranslate needed while Google Translate will not fix zero-width space issue
      _span || (_span = /*#__PURE__*/_jsx("span", {
        className: "notranslate",
        children: "\u200B"
      }))
    })
  });
}
process.env.NODE_ENV !== "production" ? NotchedOutline.propTypes /* remove-proptypes */ = {
  /**
   * The content of the component.
   */
  children: PropTypes.node,
  /**
   * Override or extend the styles applied to the component.
   */
  classes: PropTypes.object,
  /**
   * @ignore
   */
  className: PropTypes.string,
  /**
   * The label.
   */
  label: PropTypes.node,
  /**
   * If `true`, the outline is notched to accommodate the label.
   */
  notched: PropTypes.bool.isRequired,
  /**
   * @ignore
   */
  style: PropTypes.object
} : void 0;