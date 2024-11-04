import * as React from 'react';
/**
 * Returns the ref of a React element handling differences between React 19 and older versions.
 * It will throw runtime error if the element is not a valid React element.
 *
 * @param element React.ReactElement
 * @returns React.Ref<any> | null
 */
export default function getReactElementRef(element: React.ReactElement): React.Ref<any> | null;
