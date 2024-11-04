import { styled } from '@mui/material/styles';
import { DIALOG_WIDTH, VIEW_HEIGHT } from "../../constants/dimensions.js";
export const PickerViewRoot = styled('div')({
  overflow: 'hidden',
  width: DIALOG_WIDTH,
  maxHeight: VIEW_HEIGHT,
  display: 'flex',
  flexDirection: 'column',
  margin: '0 auto'
});