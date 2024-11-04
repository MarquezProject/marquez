import { getPath } from '@mui/system/style';
import { alpha } from '@mui/system/colorManipulator';
const getTextDecoration = ({
  theme,
  ownerState
}) => {
  const transformedColor = ownerState.color;
  const color = getPath(theme, `palette.${transformedColor}`, false) || ownerState.color;
  const channelColor = getPath(theme, `palette.${transformedColor}Channel`);
  if ('vars' in theme && channelColor) {
    return `rgba(${channelColor} / 0.4)`;
  }
  return alpha(color, 0.4);
};
export default getTextDecoration;