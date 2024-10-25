import { Strategy as PassportStrategy } from "passport-strategy";
import { SAML } from "../node-saml";
import { AuthenticateOptions, RequestWithUser, SamlConfig, VerifyWithoutRequest, VerifyWithRequest } from "./types";
export declare abstract class AbstractStrategy extends PassportStrategy {
    static readonly newSamlProviderOnConstruct: boolean;
    name: string;
    _verify: VerifyWithRequest | VerifyWithoutRequest;
    _saml: SAML | undefined;
    _passReqToCallback?: boolean;
    constructor(options: SamlConfig, verify: VerifyWithRequest);
    constructor(options: SamlConfig, verify: VerifyWithoutRequest);
    authenticate(req: RequestWithUser, options: AuthenticateOptions): void;
    logout(req: RequestWithUser, callback: (err: Error | null, url?: string | null) => void): void;
    protected _generateServiceProviderMetadata(decryptionCert: string | null, signingCert?: string | null): string;
    error(err: Error): void;
}
export declare class Strategy extends AbstractStrategy {
    static readonly newSamlProviderOnConstruct = true;
    generateServiceProviderMetadata(decryptionCert: string | null, signingCert?: string | null): string;
}
