import { AbstractStrategy } from "./strategy";
import type { Request } from "express";
import { AuthenticateOptions, MultiSamlConfig, RequestWithUser, SamlConfig, VerifyWithoutRequest, VerifyWithRequest } from "./types";
export declare class MultiSamlStrategy extends AbstractStrategy {
    static readonly newSamlProviderOnConstruct = false;
    _options: SamlConfig & MultiSamlConfig;
    constructor(options: MultiSamlConfig, verify: VerifyWithRequest);
    constructor(options: MultiSamlConfig, verify: VerifyWithoutRequest);
    authenticate(req: RequestWithUser, options: AuthenticateOptions): void;
    logout(req: RequestWithUser, callback: (err: Error | null, url?: string | null | undefined) => void): void;
    generateServiceProviderMetadata(req: Request, decryptionCert: string | null, signingCert: string | null, callback: (err: Error | null, metadata?: string) => void): void;
    error(err: Error): void;
}
