export interface PickersArrowSwitcherClasses {
    /** Styles applied to the root element. */
    root: string;
    /** Styles applied to the spacer element. */
    spacer: string;
    /** Styles applied to the button element. */
    button: string;
    /** Styles applied to the previous icon button element. */
    previousIconButton: string;
    /** Styles applied to the next icon button element. */
    nextIconButton: string;
    /** Styles applied to the left icon element. */
    leftArrowIcon: string;
    /** Styles applied to the right icon element. */
    rightArrowIcon: string;
}
export type PickersArrowSwitcherClassKey = keyof PickersArrowSwitcherClasses;
export declare function getPickersArrowSwitcherUtilityClass(slot: string): string;
export declare const pickersArrowSwitcherClasses: Record<"button" | "root" | "spacer" | "previousIconButton" | "nextIconButton" | "leftArrowIcon" | "rightArrowIcon", string>;
