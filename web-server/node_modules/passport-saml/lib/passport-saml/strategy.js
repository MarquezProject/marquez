"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.Strategy = exports.AbstractStrategy = void 0;
const passport_strategy_1 = require("passport-strategy");
const node_saml_1 = require("../node-saml");
const url = require("url");
class AbstractStrategy extends passport_strategy_1.Strategy {
    constructor(options, verify) {
        super();
        if (typeof options === "function") {
            throw new Error("Mandatory SAML options missing");
        }
        if (!verify) {
            throw new Error("SAML authentication strategy requires a verify function");
        }
        // Customizing the name can be useful to support multiple SAML configurations at the same time.
        // Unlike other options, this one gets deleted instead of passed along.
        if (options.name) {
            this.name = options.name;
        }
        else {
            this.name = "saml";
        }
        this._verify = verify;
        if (this.constructor.newSamlProviderOnConstruct) {
            this._saml = new node_saml_1.SAML(options);
        }
        this._passReqToCallback = !!options.passReqToCallback;
    }
    authenticate(req, options) {
        if (this._saml == null) {
            throw new Error("Can't get authenticate without a SAML provider defined.");
        }
        options.samlFallback = options.samlFallback || "login-request";
        const validateCallback = ({ profile, loggedOut, }) => {
            if (loggedOut) {
                req.logout();
                if (profile) {
                    if (this._saml == null) {
                        throw new Error("Can't get logout response URL without a SAML provider defined.");
                    }
                    const RelayState = (req.query && req.query.RelayState) || (req.body && req.body.RelayState);
                    return this._saml.getLogoutResponseUrl(profile, RelayState, options, redirectIfSuccess);
                }
                return this.pass();
            }
            const verified = (err, user, info) => {
                if (err) {
                    return this.error(err);
                }
                if (!user) {
                    return this.fail(info, 401);
                }
                this.success(user, info);
            };
            if (this._passReqToCallback) {
                this._verify(req, profile, verified);
            }
            else {
                this._verify(profile, verified);
            }
        };
        const redirectIfSuccess = (err, url) => {
            if (err) {
                this.error(err);
            }
            else {
                this.redirect(url);
            }
        };
        if (req.query && (req.query.SAMLResponse || req.query.SAMLRequest)) {
            const originalQuery = url.parse(req.url).query;
            this._saml
                .validateRedirectAsync(req.query, originalQuery)
                .then(validateCallback)
                .catch((err) => this.error(err));
        }
        else if (req.body && req.body.SAMLResponse) {
            this._saml
                .validatePostResponseAsync(req.body)
                .then(validateCallback)
                .catch((err) => this.error(err));
        }
        else if (req.body && req.body.SAMLRequest) {
            this._saml
                .validatePostRequestAsync(req.body)
                .then(validateCallback)
                .catch((err) => this.error(err));
        }
        else {
            const requestHandler = {
                "login-request": async () => {
                    try {
                        if (this._saml == null) {
                            throw new Error("Can't process login request without a SAML provider defined.");
                        }
                        const RelayState = (req.query && req.query.RelayState) || (req.body && req.body.RelayState);
                        const host = req.headers && req.headers.host;
                        if (this._saml.options.authnRequestBinding === "HTTP-POST") {
                            const data = await this._saml.getAuthorizeFormAsync(RelayState, host);
                            const res = req.res;
                            res.send(data);
                        }
                        else {
                            // Defaults to HTTP-Redirect
                            this.redirect(await this._saml.getAuthorizeUrlAsync(RelayState, host, options));
                        }
                    }
                    catch (err) {
                        this.error(err);
                    }
                },
                "logout-request": async () => {
                    if (this._saml == null) {
                        throw new Error("Can't process logout request without a SAML provider defined.");
                    }
                    try {
                        const RelayState = (req.query && req.query.RelayState) || (req.body && req.body.RelayState);
                        this.redirect(await this._saml.getLogoutUrlAsync(req.user, RelayState, options));
                    }
                    catch (err) {
                        this.error(err);
                    }
                },
            }[options.samlFallback];
            if (typeof requestHandler !== "function") {
                return this.fail(401);
            }
            requestHandler();
        }
    }
    logout(req, callback) {
        if (this._saml == null) {
            throw new Error("Can't logout without a SAML provider defined.");
        }
        const RelayState = (req.query && req.query.RelayState) || (req.body && req.body.RelayState);
        this._saml
            .getLogoutUrlAsync(req.user, RelayState, {})
            .then((url) => callback(null, url))
            .catch((err) => callback(err));
    }
    _generateServiceProviderMetadata(decryptionCert, signingCert) {
        if (this._saml == null) {
            throw new Error("Can't generate service provider metadata without a SAML provider defined.");
        }
        return this._saml.generateServiceProviderMetadata(decryptionCert, signingCert);
    }
    // This is reduntant, but helps with testing
    error(err) {
        super.error(err);
    }
}
exports.AbstractStrategy = AbstractStrategy;
class Strategy extends AbstractStrategy {
    generateServiceProviderMetadata(decryptionCert, signingCert) {
        return this._generateServiceProviderMetadata(decryptionCert, signingCert);
    }
}
exports.Strategy = Strategy;
Strategy.newSamlProviderOnConstruct = true;
//# sourceMappingURL=strategy.js.map