import { SamlSigningOptions } from "./types";
export declare function signSamlPost(samlMessage: string, xpath: string, options: SamlSigningOptions): string;
export declare function signAuthnRequestPost(authnRequest: string, options: SamlSigningOptions): string;
