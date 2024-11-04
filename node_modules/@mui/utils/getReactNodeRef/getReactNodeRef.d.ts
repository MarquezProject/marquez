import * as React from 'react';
/**
 * Returns the ref of a React node handling differences between React 19 and older versions.
 * It will return null if the node is not a valid React element.
 *
 * @param element React.ReactNode
 * @returns React.Ref<any> | null
 *
 * @deprecated Use getReactElementRef instead
 */
export default function getReactNodeRef(element: React.ReactNode): React.Ref<any> | null;
