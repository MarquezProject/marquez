import _extends from "@babel/runtime/helpers/esm/extends";
import { useRtl } from '@mui/system/RtlProvider';
import { useIsLandscape } from "../useIsLandscape.js";

/**
 * Props used to create the layout of the views.
 * Those props are exposed on all the pickers.
 */

/**
 * Prepare the props for the view layout (managed by `PickersLayout`)
 */
export const usePickerLayoutProps = ({
  props,
  propsFromPickerValue,
  propsFromPickerViews,
  wrapperVariant
}) => {
  const {
    orientation
  } = props;
  const isLandscape = useIsLandscape(propsFromPickerViews.views, orientation);
  const isRtl = useRtl();
  const layoutProps = _extends({}, propsFromPickerViews, propsFromPickerValue, {
    isLandscape,
    isRtl,
    wrapperVariant,
    disabled: props.disabled,
    readOnly: props.readOnly
  });
  return {
    layoutProps
  };
};