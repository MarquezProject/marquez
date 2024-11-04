import _formatMuiErrorMessage from "@mui/utils/formatMuiErrorMessage";
export default function makeStyles() {
  throw new Error(process.env.NODE_ENV !== "production" ? 'MUI: makeStyles is no longer exported from @mui/material/styles.\n' + 'You have to import it from @mui/styles.\n' + 'See https://mui.com/r/migration-v4/#mui-material-styles for more details.' : _formatMuiErrorMessage(14));
}