import isHostComponent from "./isHostComponent.js";
const shouldSpreadAdditionalProps = Slot => {
  return !Slot || !isHostComponent(Slot);
};
export default shouldSpreadAdditionalProps;