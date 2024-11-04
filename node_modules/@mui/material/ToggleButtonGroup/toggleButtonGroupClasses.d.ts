export interface ToggleButtonGroupClasses {
    /** Styles applied to the root element. */
    root: string;
    /** State class applied to the root element if `selected={true}`. */
    selected: string;
    /** Styles applied to the root element if `orientation="horizontal"`. */
    horizontal: string;
    /** Styles applied to the root element if `orientation="vertical"`. */
    vertical: string;
    /** State class applied to the root element if `disabled={true}`. */
    disabled: string;
    /** Styles applied to the children. */
    grouped: string;
    /** Styles applied to the children if `orientation="horizontal"`.
     *  @deprecated Combine the [.MuiToggleButtonGroup-horizontal](/material-ui/api/toggle-button-group/#toggle-button-group-classes-horizontal) and [.MuiToggleButtonGroup-grouped](/material-ui/api/toggle-button-group/#toggle-button-group-classes-grouped) classes instead. See [Migrating from deprecated APIs](/material-ui/migration/migrating-from-deprecated-apis/) for more details.
     */
    groupedHorizontal: string;
    /** Styles applied to the children if `orientation="vertical"`.
     * @deprecated Combine the [.MuiToggleButtonGroup-vertical](/material-ui/api/toggle-button-group/#toggle-button-group-classes-vertical) and [.MuiToggleButtonGroup-grouped](/material-ui/api/toggle-button-group/#toggle-button-group-classes-grouped) classes instead. See [Migrating from deprecated APIs](/material-ui/migration/migrating-from-deprecated-apis/) for more details.
     */
    groupedVertical: string;
    /** Styles applied to the root element if `fullWidth={true}`. */
    fullWidth: string;
    /** Styles applied to the first button in the toggle button group. */
    firstButton: string;
    /** Styles applied to the last button in the toggle button group. */
    lastButton: string;
    /** Styles applied to buttons in the middle of the toggle button group. */
    middleButton: string;
}
export type ToggleButtonGroupClassKey = keyof ToggleButtonGroupClasses;
export declare function getToggleButtonGroupUtilityClass(slot: string): string;
declare const toggleButtonGroupClasses: ToggleButtonGroupClasses;
export default toggleButtonGroupClasses;
