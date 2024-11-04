import * as React from 'react';
import { PickersSectionListProps } from './PickersSectionList.types';
export declare const PickersSectionListRoot: import("@emotion/styled").StyledComponent<import("@mui/system").MUIStyledCommonProps<import("@mui/material").Theme>, Pick<React.DetailedHTMLProps<React.HTMLAttributes<HTMLDivElement>, HTMLDivElement>, keyof React.ClassAttributes<HTMLDivElement> | keyof React.HTMLAttributes<HTMLDivElement>>, {}>;
export declare const PickersSectionListSection: import("@emotion/styled").StyledComponent<import("@mui/system").MUIStyledCommonProps<import("@mui/material").Theme>, Pick<React.DetailedHTMLProps<React.HTMLAttributes<HTMLSpanElement>, HTMLSpanElement>, keyof React.ClassAttributes<HTMLSpanElement> | keyof React.HTMLAttributes<HTMLSpanElement>>, {}>;
export declare const PickersSectionListSectionSeparator: import("@emotion/styled").StyledComponent<import("@mui/system").MUIStyledCommonProps<import("@mui/material").Theme>, Pick<React.DetailedHTMLProps<React.HTMLAttributes<HTMLSpanElement>, HTMLSpanElement>, keyof React.ClassAttributes<HTMLSpanElement> | keyof React.HTMLAttributes<HTMLSpanElement>>, {}>;
export declare const PickersSectionListSectionContent: import("@emotion/styled").StyledComponent<import("@mui/system").MUIStyledCommonProps<import("@mui/material").Theme>, Pick<React.DetailedHTMLProps<React.HTMLAttributes<HTMLSpanElement>, HTMLSpanElement>, keyof React.ClassAttributes<HTMLSpanElement> | keyof React.HTMLAttributes<HTMLSpanElement>>, {}>;
type PickersSectionListComponent = ((props: PickersSectionListProps & React.RefAttributes<HTMLDivElement>) => React.JSX.Element) & {
    propTypes?: any;
};
/**
 * Demos:
 *
 * - [Custom field](https://mui.com/x/react-date-pickers/custom-field/)
 *
 * API:
 *
 * - [PickersSectionList API](https://mui.com/x/api/date-pickers/pickers-section-list/)
 */
declare const PickersSectionList: PickersSectionListComponent;
export { PickersSectionList };
