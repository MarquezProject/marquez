'use client';

import * as React from 'react';
import PropTypes from 'prop-types';
import clsx from 'clsx';
import { styled } from "../zero-styled/index.js";
import memoTheme from "../utils/memoTheme.js";
import rootShouldForwardProp from "../styles/rootShouldForwardProp.js";
import capitalize from "../utils/capitalize.js";
import { isHorizontal } from "../Drawer/Drawer.js";
import { jsx as _jsx } from "react/jsx-runtime";
const SwipeAreaRoot = styled('div', {
  shouldForwardProp: rootShouldForwardProp
})(memoTheme(({
  theme
}) => ({
  position: 'fixed',
  top: 0,
  left: 0,
  bottom: 0,
  zIndex: theme.zIndex.drawer - 1,
  variants: [{
    props: {
      anchor: 'left'
    },
    style: {
      right: 'auto'
    }
  }, {
    props: {
      anchor: 'right'
    },
    style: {
      left: 'auto',
      right: 0
    }
  }, {
    props: {
      anchor: 'top'
    },
    style: {
      bottom: 'auto',
      right: 0
    }
  }, {
    props: {
      anchor: 'bottom'
    },
    style: {
      top: 'auto',
      bottom: 0,
      right: 0
    }
  }]
})));

/**
 * @ignore - internal component.
 */
const SwipeArea = /*#__PURE__*/React.forwardRef(function SwipeArea(props, ref) {
  const {
    anchor,
    classes = {},
    className,
    width,
    style,
    ...other
  } = props;
  const ownerState = props;
  return /*#__PURE__*/_jsx(SwipeAreaRoot, {
    className: clsx('PrivateSwipeArea-root', classes.root, classes[`anchor${capitalize(anchor)}`], className),
    ref: ref,
    style: {
      [isHorizontal(anchor) ? 'width' : 'height']: width,
      ...style
    },
    ownerState: ownerState,
    ...other
  });
});
process.env.NODE_ENV !== "production" ? SwipeArea.propTypes = {
  /**
   * Side on which to attach the discovery area.
   */
  anchor: PropTypes.oneOf(['left', 'top', 'right', 'bottom']).isRequired,
  /**
   * @ignore
   */
  classes: PropTypes.object,
  /**
   * @ignore
   */
  className: PropTypes.string,
  /**
   * @ignore
   */
  style: PropTypes.object,
  /**
   * The width of the left most (or right most) area in `px` where the
   * drawer can be swiped open from.
   */
  width: PropTypes.number.isRequired
} : void 0;
export default SwipeArea;