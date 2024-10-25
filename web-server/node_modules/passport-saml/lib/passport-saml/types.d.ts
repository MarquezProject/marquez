import type * as express from "express";
import * as passport from "passport";
import type { SamlOptions, MandatorySamlOptions } from "../node-saml/types";
export interface AuthenticateOptions extends passport.AuthenticateOptions {
    samlFallback?: "login-request" | "logout-request";
    additionalParams?: Record<string, any>;
}
export interface AuthorizeOptions extends AuthenticateOptions {
    samlFallback?: "login-request" | "logout-request";
}
export interface StrategyOptions {
    name?: string;
    passReqToCallback?: boolean;
}
/**
 * These options are availble for configuring a SAML strategy
 */
export declare type SamlConfig = Partial<SamlOptions> & StrategyOptions & MandatorySamlOptions;
export interface Profile {
    issuer?: string;
    sessionIndex?: string;
    nameID?: string;
    nameIDFormat?: string;
    nameQualifier?: string;
    spNameQualifier?: string;
    ID?: string;
    mail?: string;
    email?: string;
    ["urn:oid:0.9.2342.19200300.100.1.3"]?: string;
    getAssertionXml?(): string;
    getAssertion?(): Record<string, unknown>;
    getSamlResponseXml?(): string;
    [attributeName: string]: unknown;
}
export interface RequestWithUser extends express.Request {
    samlLogoutRequest: any;
    user?: Profile;
}
export declare type VerifiedCallback = (err: Error | null, user?: Record<string, unknown>, info?: Record<string, unknown>) => void;
export declare type VerifyWithRequest = (req: express.Request, profile: Profile | null | undefined, done: VerifiedCallback) => void;
export declare type VerifyWithoutRequest = (profile: Profile | null | undefined, done: VerifiedCallback) => void;
export declare type SamlOptionsCallback = (err: Error | null, samlOptions?: SamlConfig) => void;
interface BaseMultiSamlConfig {
    getSamlOptions(req: express.Request, callback: SamlOptionsCallback): void;
}
export declare type MultiSamlConfig = Partial<SamlConfig> & StrategyOptions & BaseMultiSamlConfig;
export declare class ErrorWithXmlStatus extends Error {
    readonly xmlStatus: string;
    constructor(message: string, xmlStatus: string);
}
export {};
