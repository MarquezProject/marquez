type RecordPropertyNames<T> = {
    [K in keyof T]: T[K] extends Function ? never : T[K] extends Record<string, any> ? K : never;
}[keyof T];
export type ExtractTypographyTokens<T> = {
    [K in RecordPropertyNames<T>]: string;
};
export default function prepareTypographyVars<T extends Record<string, any>>(typography: T): ExtractTypographyTokens<T>;
export {};
