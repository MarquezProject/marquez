export interface TabClasses {
    /** Styles applied to the root element. */
    root: string;
    /** Styles applied to the root element if both `icon` and `label` are provided. */
    labelIcon: string;
    /** Styles applied to the root element if the parent [`Tabs`](/material-ui/api/tabs/) has `textColor="inherit"`. */
    textColorInherit: string;
    /** Styles applied to the root element if the parent [`Tabs`](/material-ui/api/tabs/) has `textColor="primary"`. */
    textColorPrimary: string;
    /** Styles applied to the root element if the parent [`Tabs`](/material-ui/api/tabs/) has `textColor="secondary"`. */
    textColorSecondary: string;
    /** State class applied to the root element if `selected={true}` (controlled by the Tabs component). */
    selected: string;
    /** State class applied to the root element if `disabled={true}` (controlled by the Tabs component). */
    disabled: string;
    /** Styles applied to the root element if `fullWidth={true}` (controlled by the Tabs component). */
    fullWidth: string;
    /** Styles applied to the root element if `wrapped={true}`. */
    wrapped: string;
    /** Styles applied to the `icon` HTML element if both `icon` and `label` are provided.
     * @deprecated Use `icon` class instead. See [Migrating from deprecated APIs](/material-ui/migration/migrating-from-deprecated-apis/) for more details
     */
    iconWrapper: string;
    /** Styles applied to the `icon` HTML element if both `icon` and `label` are provided. */
    icon: string;
}
export type TabClassKey = keyof TabClasses;
export declare function getTabUtilityClass(slot: string): string;
declare const tabClasses: TabClasses;
export default tabClasses;
