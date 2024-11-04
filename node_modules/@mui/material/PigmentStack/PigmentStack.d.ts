import * as React from 'react';
import { OverridableComponent, OverrideProps } from '@mui/types';
import { SxProps } from '@mui/system';
import { Breakpoint, Theme } from '../styles';
type ResponsiveStyleValue<T> = T | Array<T | null> | {
    [key in Breakpoint]?: T | null;
};
export interface PigmentStackOwnProps {
    /**
     * The content of the component.
     */
    children?: React.ReactNode;
    /**
     * Defines the `flex-direction` style property.
     * It is applied for all screen sizes.
     * @default 'column'
     */
    direction?: ResponsiveStyleValue<'row' | 'row-reverse' | 'column' | 'column-reverse'>;
    /**
     * Defines the space between immediate children.
     * @default 0
     */
    spacing?: ResponsiveStyleValue<number | string>;
    /**
     * Add an element between each child.
     */
    divider?: React.ReactNode;
    /**
     * The system prop, which allows defining system overrides as well as additional CSS styles.
     */
    sx?: SxProps<Theme>;
}
export interface PigmentStackTypeMap<AdditionalProps = {}, RootComponent extends React.ElementType = 'div'> {
    props: AdditionalProps & PigmentStackOwnProps;
    defaultComponent: RootComponent;
}
export type PigmentStackProps<RootComponent extends React.ElementType = PigmentStackTypeMap['defaultComponent'], AdditionalProps = {}> = OverrideProps<PigmentStackTypeMap<AdditionalProps, RootComponent>, RootComponent> & {
    component?: React.ElementType;
};
/**
 *
 * Demos:
 *
 * - [Stack](https://mui.com/material-ui/react-stack/)
 *
 * API:
 *
 * - [PigmentStack API](https://mui.com/material-ui/api/pigment-stack/)
 */
declare const PigmentStack: OverridableComponent<PigmentStackTypeMap>;
export default PigmentStack;
