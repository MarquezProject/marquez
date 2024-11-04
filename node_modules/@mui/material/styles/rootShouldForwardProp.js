import slotShouldForwardProp from "./slotShouldForwardProp.js";
const rootShouldForwardProp = prop => slotShouldForwardProp(prop) && prop !== 'classes';
export default rootShouldForwardProp;