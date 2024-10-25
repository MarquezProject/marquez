/// <reference types="node" />
import * as querystring from "querystring";
import { CacheProvider as InMemoryCacheProvider } from "./inmemory-cache-provider";
import { ParsedQs } from "qs";
import { SamlOptions } from "./types";
import { AuthenticateOptions, AuthorizeOptions, Profile, SamlConfig } from "../passport-saml/types";
interface NameID {
    value: string | null;
    format: string | null;
}
declare class SAML {
    options: SamlOptions;
    cacheProvider: InMemoryCacheProvider;
    constructor(ctorOptions: SamlConfig);
    initialize(ctorOptions: SamlConfig): SamlOptions;
    private getCallbackUrl;
    _generateUniqueID(): string;
    private generateInstant;
    private signRequest;
    private generateAuthorizeRequestAsync;
    _generateLogoutRequest(user: Profile): Promise<string>;
    _generateLogoutResponse(logoutRequest: Profile): string;
    _requestToUrlAsync(request: string | null | undefined, response: string | null, operation: string, additionalParameters: querystring.ParsedUrlQuery): Promise<string>;
    _getAdditionalParams(RelayState: string, operation: string, overrideParams?: querystring.ParsedUrlQuery): querystring.ParsedUrlQuery;
    getAuthorizeUrlAsync(RelayState: string, host: string | undefined, options: AuthorizeOptions): Promise<string>;
    getAuthorizeFormAsync(RelayState: string, host?: string): Promise<string>;
    getLogoutUrlAsync(user: Profile, RelayState: string, options: AuthenticateOptions & AuthorizeOptions): Promise<string>;
    getLogoutResponseUrl(samlLogoutRequest: Profile, RelayState: string, options: AuthenticateOptions & AuthorizeOptions, callback: (err: Error | null, url?: string | null) => void): void;
    private getLogoutResponseUrlAsync;
    _certToPEM(cert: string): string;
    private certsToCheck;
    validateSignature(fullXml: string, currentNode: Element, certs: string[]): boolean;
    validatePostResponseAsync(container: Record<string, string>): Promise<{
        profile?: Profile | null;
        loggedOut?: boolean;
    }>;
    private validateInResponseTo;
    validateRedirectAsync(container: ParsedQs, originalQuery: string | null): Promise<{
        profile?: Profile | null;
        loggedOut?: boolean;
    }>;
    private hasValidSignatureForRedirect;
    private validateSignatureForRedirect;
    private verifyLogoutRequest;
    private verifyLogoutResponse;
    private verifyIssuer;
    private processValidlySignedAssertionAsync;
    private checkTimestampsValidityError;
    private checkAudienceValidityError;
    validatePostRequestAsync(container: Record<string, string>): Promise<{
        profile?: Profile;
        loggedOut?: boolean;
    }>;
    _getNameIdAsync(self: SAML, doc: Node): Promise<NameID>;
    generateServiceProviderMetadata(decryptionCert: string | null, signingCert?: string | null): string;
    _keyToPEM(key: string | Buffer): typeof key extends string | Buffer ? string | Buffer : Error;
    /**
     * Process max age assertion and use it if it is more restrictive than the NotOnOrAfter age
     * assertion received in the SAMLResponse.
     *
     * @param maxAssertionAgeMs Max time after IssueInstant that we will accept assertion, in Ms.
     * @param notOnOrAfter Expiration provided in response.
     * @param issueInstant Time when response was issued.
     * @returns {*} The expiration time to be used, in Ms.
     */
    private processMaxAgeAssertionTime;
    /**
     * Convert a date string to a timestamp (in milliseconds).
     *
     * @param dateString A string representation of a date
     * @param label Descriptive name of the date being passed in, e.g. "NotOnOrAfter"
     * @throws Will throw an error if parsing `dateString` returns `NaN`
     * @returns {number} The timestamp (in milliseconds) representation of the given date
     */
    private dateStringToTimestamp;
}
export { SAML };
