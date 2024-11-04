import ownerDocument from "../ownerDocument/index.js";
export default function ownerWindow(node) {
  const doc = ownerDocument(node);
  return doc.defaultView || window;
}