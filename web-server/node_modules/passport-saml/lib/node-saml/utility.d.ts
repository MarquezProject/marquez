import { SamlSigningOptions } from "./types";
export declare function assertRequired<T>(value: T | null | undefined, error?: string): T;
export declare function signXmlResponse(samlMessage: string, options: SamlSigningOptions): string;
