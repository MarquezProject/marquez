import * as React from 'react';
import { OverridableComponent, OverrideProps } from '@mui/types';
import { SxProps } from '@mui/system';
import { Breakpoint, Theme } from '../styles';
type ResponsiveStyleValue<T> = T | Array<T | null> | {
    [key in Breakpoint]?: T | null;
};
export type GridDirection = 'row' | 'row-reverse' | 'column' | 'column-reverse';
export type GridSpacing = number | string;
export type GridWrap = 'nowrap' | 'wrap' | 'wrap-reverse';
export type GridSize = 'auto' | 'grow' | number;
export interface GridBaseProps {
    /**
     * The content of the component.
     */
    children?: React.ReactNode;
    /**
     * The number of columns.
     * @default 12
     */
    columns?: ResponsiveStyleValue<number>;
    /**
     * Defines the horizontal space between the type `item` components.
     * It overrides the value of the `spacing` prop.
     */
    columnSpacing?: ResponsiveStyleValue<GridSpacing>;
    /**
     * If `true`, the component will have the flex *container* behavior.
     * You should be wrapping *items* with a *container*.
     * @default false
     */
    container?: boolean;
    /**
     * Defines the `flex-direction` style property.
     * It is applied for all screen sizes.
     * @default 'row'
     */
    direction?: ResponsiveStyleValue<GridDirection>;
    /**
     * Defines the offset of the grid.
     */
    offset?: ResponsiveStyleValue<number> | undefined;
    /**
     * Defines the vertical space between the type `item` components.
     * It overrides the value of the `spacing` prop.
     */
    rowSpacing?: ResponsiveStyleValue<GridSpacing>;
    /**
     * Defines the space between the type `item` components.
     * It can only be used on a type `container` component.
     * @default 0
     */
    spacing?: ResponsiveStyleValue<GridSpacing> | undefined;
    /**
     * Defines the column size of the grid.
     */
    size?: ResponsiveStyleValue<GridSize> | undefined;
    /**
     * Defines the `flex-wrap` style property.
     * It's applied for all screen sizes.
     * @default 'wrap'
     */
    wrap?: GridWrap;
}
export interface GridTypeMap<AdditionalProps = {}, DefaultComponent extends React.ElementType = 'div'> {
    props: AdditionalProps & GridBaseProps & {
        sx?: SxProps<Theme>;
    };
    defaultComponent: DefaultComponent;
}
export type GridProps<RootComponent extends React.ElementType = GridTypeMap['defaultComponent'], AdditionalProps = {
    component?: React.ElementType;
}> = OverrideProps<GridTypeMap<AdditionalProps, RootComponent>, RootComponent>;
/**
 *
 * Demos:
 *
 * - [Grid version 2](https://mui.com/material-ui/react-grid2/)
 *
 * API:
 *
 * - [PigmentGrid API](https://mui.com/material-ui/api/pigment-grid/)
 */
declare const PigmentGrid: OverridableComponent<GridTypeMap>;
export default PigmentGrid;
