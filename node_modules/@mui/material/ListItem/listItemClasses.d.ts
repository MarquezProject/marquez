export interface ListItemClasses {
    /** Styles applied to the (normally root) `component` element. May be wrapped by a `container`. */
    root: string;
    /** Styles applied to the container element if `children` includes `ListItemSecondaryAction`. */
    container: string;
    /** Styles applied to the component element if dense. */
    dense: string;
    /** Styles applied to the component element if `alignItems="flex-start"`. */
    alignItemsFlexStart: string;
    /** Styles applied to the inner `component` element if `divider={true}`. */
    divider: string;
    /** Styles applied to the inner `component` element unless `disableGutters={true}`. */
    gutters: string;
    /** Styles applied to the root element unless `disablePadding={true}`. */
    padding: string;
    /** Styles applied to the component element if `children` includes `ListItemSecondaryAction`. */
    secondaryAction: string;
}
export type ListItemClassKey = keyof ListItemClasses;
export declare function getListItemUtilityClass(slot: string): string;
declare const listItemClasses: ListItemClasses;
export default listItemClasses;
