import { SxProps, SystemProps } from '@mui/system';
import { OverridableComponent, OverrideProps } from '@mui/types';
import { Theme, Breakpoint } from '../styles';
export type Grid2Slot = 'root';
type ResponsiveStyleValue<T> = T | Array<T | null> | {
    [key in Breakpoint]?: T | null;
};
export type GridDirection = 'row' | 'row-reverse' | 'column' | 'column-reverse';
export type GridSpacing = number | string;
export type GridWrap = 'nowrap' | 'wrap' | 'wrap-reverse';
export type GridSize = 'auto' | 'grow' | number | false;
export type GridOffset = 'auto' | number;
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
     * Defines the offset value for the type `item` components.
     */
    offset?: ResponsiveStyleValue<GridOffset>;
    /**
     * @internal
     * The level of the grid starts from `0` and increases when the grid nests
     * inside another grid. Nesting is defined as a container Grid being a direct
     * child of a container Grid.
     *
     * ```js
     * <Grid container> // level 0
     *   <Grid container> // level 1
     *     <Grid container> // level 2
     * ```
     *
     * Only consecutive grid is considered nesting. A grid container will start at
     * `0` if there are non-Grid container element above it.
     *
     * ```js
     * <Grid container> // level 0
     *   <div>
     *     <Grid container> // level 0
     * ```
     *
     * ```js
     * <Grid container> // level 0
     *   <Grid>
     *     <Grid container> // level 0
     * ```
     */
    unstable_level?: number;
    /**
     * Defines the vertical space between the type `item` components.
     * It overrides the value of the `spacing` prop.
     */
    rowSpacing?: ResponsiveStyleValue<GridSpacing>;
    /**
     * Defines the size of the the type `item` components.
     */
    size?: ResponsiveStyleValue<GridSize>;
    /**
     * Defines the space between the type `item` components.
     * It can only be used on a type `container` component.
     * @default 0
     */
    spacing?: ResponsiveStyleValue<GridSpacing> | undefined;
    /**
     * Defines the `flex-wrap` style property.
     * It's applied for all screen sizes.
     * @default 'wrap'
     */
    wrap?: GridWrap;
}
export interface Grid2TypeMap<P = {}, D extends React.ElementType = 'div'> {
    props: P & GridBaseProps & {
        sx?: SxProps<Theme>;
    } & SystemProps<Theme>;
    defaultComponent: D;
}
export type Grid2Props<D extends React.ElementType = Grid2TypeMap['defaultComponent'], P = {
    component?: React.ElementType;
}> = OverrideProps<Grid2TypeMap<P, D>, D>;
/**
 *
 * Demos:
 *
 * - [Grid version 2](https://mui.com/material-ui/react-grid2/)
 *
 * API:
 *
 * - [Grid2 API](https://mui.com/material-ui/api/grid-2/)
 */
declare const Grid2: OverridableComponent<Grid2TypeMap>;
export default Grid2;
