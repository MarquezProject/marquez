export type SpacingOptions = number | string | Spacing | ((abs: number) => number | string) | ((abs: number | string) => number | string) | ReadonlyArray<string | number>;
export type SpacingArgument = number | string;
export interface Spacing {
    (): string;
    (value: SpacingArgument): string;
    (topBottom: SpacingArgument, rightLeft: SpacingArgument): string;
    (top: SpacingArgument, rightLeft: SpacingArgument, bottom: SpacingArgument): string;
    (top: SpacingArgument, right: SpacingArgument, bottom: SpacingArgument, left: SpacingArgument): string;
}
export default function createSpacing(spacingInput?: SpacingOptions, transform?: Spacing | (() => undefined) | ((abs: number | string) => number | number)): Spacing;
