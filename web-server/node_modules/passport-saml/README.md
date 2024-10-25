# Passport-SAML

[![Build Status](https://github.com/node-saml/passport-saml/workflows/Build%20Status/badge.svg)](https://github.com/node-saml/passport-saml/actions?query=workflow%3ABuild%Status) [![GitHub version](https://badge.fury.io/gh/node-saml%2Fpassport-saml.svg)](https://badge.fury.io/gh/node-saml%2Fpassport-saml) [![npm version](https://badge.fury.io/js/passport-saml.svg)](http://badge.fury.io/js/passport-saml) [![NPM](https://nodei.co/npm/passport-saml.png?downloads=true&downloadRank=true&stars=true)](https://nodei.co/npm/passport-saml/) [![code style: prettier](https://img.shields.io/badge/code_style-prettier-ff69b4.svg?style=flat-square)](https://github.com/prettier/prettier)

This is a [SAML 2.0](http://en.wikipedia.org/wiki/SAML_2.0) authentication provider for [Passport](http://passportjs.org/), the Node.js authentication library.

The code was originally based on Michael Bosworth's [express-saml](https://github.com/bozzltron/express-saml) library.

Passport-SAML has been tested to work with Onelogin, Okta, Shibboleth, [SimpleSAMLphp](http://simplesamlphp.org/) based Identity Providers, and with [Active Directory Federation Services](http://en.wikipedia.org/wiki/Active_Directory_Federation_Services).

## Installation

    $ npm install passport-saml

/

## Usage

The examples utilize the [Feide OpenIdp identity provider](https://openidp.feide.no/). You need an account there to log in with this. You also need to [register your site](https://openidp.feide.no/simplesaml/module.php/metaedit/index.php) as a service provider.

### Configure strategy

The SAML identity provider will redirect you to the URL provided by the `path` configuration.

```javascript
const SamlStrategy = require('passport-saml').Strategy;
[...]

passport.use(new SamlStrategy(
  {
    path: '/login/callback',
    entryPoint: 'https://openidp.feide.no/simplesaml/saml2/idp/SSOService.php',
    issuer: 'passport-saml',
    cert: 'fake cert', // cert must be provided
  },
  function(profile, done) {
    findByEmail(profile.email, function(err, user) {
      if (err) {
        return done(err);
      }
      return done(null, user);
    });
  })
);
```

### Configure strategy for multiple providers

You can pass a `getSamlOptions` parameter to `MultiSamlStrategy` which will be called before the SAML flows. Passport-SAML will pass in the request object so you can decide which configuation is appropriate.

```javascript
const { MultiSamlStrategy } = require('passport-saml');
[...]

passport.use(new MultiSamlStrategy(
  {
    passReqToCallback: true, // makes req available in callback
    getSamlOptions: function(request, done) {
      findProvider(request, function(err, provider) {
        if (err) {
          return done(err);
        }
        return done(null, provider.configuration);
      });
    }
  },
  function(req, profile, done) {
    findByEmail(profile.email, function(err, user) {
      if (err) {
        return done(err);
      }
      return done(null, user);
    });
  })
);
```

The options passed when the `MultiSamlStrategy` is initialized are also passed as default values to each provider.
e.g. If you provide an `issuer` on `MultiSamlStrategy`, this will be also a default value for every provider.
You can override these defaults by passing a new value through the `getSamlOptions` function.

Using multiple providers supports `validateInResponseTo`, but all the `InResponse` values are stored on the same Cache. This means, if you're using the default `InMemoryCache`, that all providers have access to it and a provider might get its response validated against another's request. [Issue Report](!https://github.com/node-saml/passport-saml/issues/334). To amend this you should provide a different cache provider per SAML provider, through the `getSamlOptions` function.

> :warning: **There's a race condition [bug](https://github.com/node-saml/passport-saml/issues/425) in versions < 1.3.3 which makes it vulnerable to DOS attacks**: Please use > 1.3.3 if you want to use this issue

#### The profile object:

The profile object referenced above contains the following:

```typescript
type Profile = {
  issuer?: string;
  sessionIndex?: string;
  nameID?: string;
  nameIDFormat?: string;
  nameQualifier?: string;
  spNameQualifier?: string;
  mail?: string; // InCommon Attribute urn:oid:0.9.2342.19200300.100.1.3
  email?: string; // `mail` if not present in the assertion
  getAssertionXml(): string; // get the raw assertion XML
  getAssertion(): object; // get the assertion XML parsed as a JavaScript object
  getSamlResponseXml(): string; // get the raw SAML response XML
  ID?: string;
} & {
  [attributeName: string]: unknown; // arbitrary `AttributeValue`s
};
```

#### Config parameter details:

- **Core**
- `callbackUrl`: full callbackUrl (overrides path/protocol if supplied)
- `path`: path to callback; will be combined with protocol and server host information to construct callback url if `callbackUrl` is not specified (default: `/saml/consume`)
- `protocol`: protocol for callback; will be combined with path and server host information to construct callback url if `callbackUrl` is not specified (default: `http://`)
- `host`: host for callback; will be combined with path and protocol to construct callback url if `callbackUrl` is not specified (default: `localhost`)
- `entryPoint`: identity provider entrypoint (is required to be spec-compliant when the request is signed)
- `issuer`: issuer string to supply to identity provider
- `audience`: expected saml response Audience (if not provided, Audience won't be verified)
- `cert`: the IDP's public signing certificate used to validate the signatures of the incoming SAML Responses, see [Security and signatures](#security-and-signatures)
- `privateKey`: see [Security and signatures](#security-and-signatures).
- `decryptionPvk`: optional private key that will be used to attempt to decrypt any encrypted assertions that are received
- `signatureAlgorithm`: optionally set the signature algorithm for signing requests, valid values are 'sha1' (default), 'sha256', or 'sha512'
- `digestAlgorithm`: optionally set the digest algorithm used to provide a digest for the signed data object, valid values are 'sha1' (default), 'sha256', or 'sha512'
- `xmlSignatureTransforms`: optionally set an array of signature transforms to be used in HTTP-POST signatures. By default this is `[ 'http://www.w3.org/2000/09/xmldsig#enveloped-signature', 'http://www.w3.org/2001/10/xml-exc-c14n#' ]`
- **Additional SAML behaviors**
- `additionalParams`: dictionary of additional query params to add to all requests; if an object with this key is passed to `authenticate`, the dictionary of additional query params will be appended to those present on the returned URL, overriding any specified by initialization options' additional parameters (`additionalParams`, `additionalAuthorizeParams`, and `additionalLogoutParams`)
- `additionalAuthorizeParams`: dictionary of additional query params to add to 'authorize' requests
- `identifierFormat`: optional name identifier format to request from identity provider (default: `urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress`)
- `wantAssertionsSigned`: if truthy, add `WantAssertionsSigned="true"` to the metadata, to specify that the IdP should always sign the assertions.
- `acceptedClockSkewMs`: Time in milliseconds of skew that is acceptable between client and server when checking `NotBefore` and `NotOnOrAfter` assertion condition validity timestamps. Setting to `-1` will disable checking these conditions entirely. Default is `0`.
- `maxAssertionAgeMs`: Amount of time after which the framework should consider an assertion expired. If the limit imposed by this variable is stricter than the limit imposed by `NotOnOrAfter`, this limit will be used when determining if an assertion is expired.
- `attributeConsumingServiceIndex`: optional `AttributeConsumingServiceIndex` attribute to add to AuthnRequest to instruct the IDP which attribute set to attach to the response ([link](http://blog.aniljohn.com/2014/01/data-minimization-front-channel-saml-attribute-requests.html))
- `disableRequestedAuthnContext`: if truthy, do not request a specific authentication context. This is [known to help when authenticating against Active Directory](https://github.com/node-saml/passport-saml/issues/226) (AD FS) servers.
- `authnContext`: if truthy, name identifier format to request auth context (default: `urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport`); array of values is also supported
- `racComparison`: Requested Authentication Context comparison type. Possible values are 'exact','minimum','maximum','better'. Default is 'exact'.

- `forceAuthn`: if set to true, the initial SAML request from the service provider specifies that the IdP should force re-authentication of the user, even if they possess a valid session.
- `providerName`: optional human-readable name of the requester for use by the presenter's user agent or the identity provider
- `skipRequestCompression`: if set to true, the SAML request from the service provider won't be compressed.
- `authnRequestBinding`: if set to `HTTP-POST`, will request authentication from IDP via HTTP POST binding, otherwise defaults to HTTP Redirect
- `disableRequestAcsUrl`: if truthy, SAML AuthnRequest from the service provider will not include the optional AssertionConsumerServiceURL. Default is falsy so it is automatically included.
- `scoping`: An optional configuration which implements the functionality [explained in the SAML spec paragraph "3.4.1.2 Element <Scoping>"](https://docs.oasis-open.org/security/saml/v2.0/saml-core-2.0-os.pdf). The config object is structured as following:

```javascript
{
  idpList: [ // optional
    {
      entries: [ // required
        {
          providerId: 'yourProviderId', // required for each entry
          name: 'yourName', // optional
          loc: 'yourLoc', // optional
        }
      ],
      getComplete: 'URI to your complete IDP list', // optional
    },
  ],
  proxyCount: 2, // optional
  requesterId: 'requesterId', // optional
}
```

- **InResponseTo Validation**
- `validateInResponseTo`: if truthy, then InResponseTo will be validated from incoming SAML responses
- `requestIdExpirationPeriodMs`: Defines the expiration time when a Request ID generated for a SAML request will not be valid if seen in a SAML response in the `InResponseTo` field. Default is 8 hours.
- `cacheProvider`: Defines the implementation for a cache provider used to store request Ids generated in SAML requests as part of `InResponseTo` validation. Default is a built-in in-memory cache provider. For details see the 'Cache Provider' section.
- **Issuer Validation**
- `idpIssuer`: if provided, then the IdP issuer will be validated for incoming Logout Requests/Responses. For ADFS this looks like `https://acme_tools.windows.net/deadbeef`
- **Passport**
- `passReqToCallback`: if truthy, `req` will be passed as the first argument to the verify callback (default: `false`)
- `name`: Optionally, provide a custom name. (default: `saml`). Useful If you want to instantiate the strategy multiple times with different configurations,
  allowing users to authenticate against multiple different SAML targets from the same site. You'll need to use a unique set of URLs
  for each target, and use this custom name when calling `passport.authenticate()` as well.
- **Logout**
- `logoutUrl`: base address to call with logout requests (default: `entryPoint`)
- `additionalLogoutParams`: dictionary of additional query params to add to 'logout' requests
- `logoutCallbackUrl`: The value with which to populate the `Location` attribute in the `SingleLogoutService` elements in the generated service provider metadata.

### Provide the authentication callback

You need to provide a route corresponding to the `path` configuration parameter given to the strategy:

The authentication callback must be invoked after the `body-parser` middlerware.

```javascript
const bodyParser = require("body-parser");

app.post(
  "/login/callback",
  bodyParser.urlencoded({ extended: false }),
  passport.authenticate("saml", { failureRedirect: "/", failureFlash: true }),
  function (req, res) {
    res.redirect("/");
  }
);
```

### Authenticate requests

Use `passport.authenticate()`, specifying `saml` as the strategy:

```javascript
app.get(
  "/login",
  passport.authenticate("saml", { failureRedirect: "/", failureFlash: true }),
  function (req, res) {
    res.redirect("/");
  }
);
```

...or, if you wish to add or override query string parameters:

```javascript
app.get(
  "/login",
  passport.authenticate("saml", {
    additionalParams: { username: "user@domain.com" },
  }),
  function (req, res) {
    res.redirect("/");
  }
);
```

### generateServiceProviderMetadata( decryptionCert, signingCert )

As a convenience, the strategy object exposes a `generateServiceProviderMetadata` method which will generate a service provider metadata document suitable for supplying to an identity provider. This method will only work on strategies which are configured with a `callbackUrl` (since the relative path for the callback is not sufficient information to generate a complete metadata document).

The `decryptionCert` argument should be a public certificate matching the `decryptionPvk` and is required if the strategy is configured with a `decryptionPvk`.

The `signingCert` argument should be a public certificate matching the `privateKey` and is required if the strategy is configured with a `privateKey`.

The `generateServiceProviderMetadata` method is also available on the `MultiSamlStrategy`, but needs an extra request and a callback argument (`generateServiceProviderMetadata( req, decryptionCert, signingCert, next )`), which are passed to the `getSamlOptions` to retrieve the correct configuration.

## Security and signatures

Passport-SAML uses the HTTP Redirect Binding for its `AuthnRequest`s (unless overridden with the `authnRequestBinding` parameter), and expects to receive the messages back via the HTTP POST binding.

Authentication requests sent by Passport-SAML can be signed using RSA signature with SHA1, SHA256 or SHA512 hashing algorithms.

To select hashing algorithm, use:

```js
...
  signatureAlgorithm: 'sha1' // (default, but not recommended anymore these days)
  signatureAlgorithm: 'sha256', // (preferred - your IDP should support it, otherwise think about upgrading it)
  signatureAlgorithm: 'sha512' // (most secure - check if your IDP supports it)
...
```

To sign them you need to provide a private key in the PEM format via the `privateKey` configuration key.

Formats supported for `privateKey` field are,

1. Well formatted PEM:

```
-----BEGIN PRIVATE KEY-----
<private key contents here delimited at 64 characters per row>
-----END PRIVATE KEY-----

```

```
-----BEGIN RSA PRIVATE KEY-----
<private key contents here delimited at 64 characters per row>
-----END RSA PRIVATE KEY-----

```

(both versions work)
See example from tests of the first version of [well formatted private key](test/static/acme_tools_com.key).

2. Alternativelly a single line private key without start/end lines where all rows are joined into single line:

See example from tests of [singleline private key](test/static/singleline_acme_tools_com.key).

Add it to strategy options like this:

```javascript
privateKey: fs.readFileSync("./privateKey.pem", "utf-8");
```

It is a good idea to validate the signatures of the incoming SAML Responses. For this, you can provide the Identity Provider's public PEM-encoded X.509 signing certificate using the `cert` configuration key. The "BEGIN CERTIFICATE" and "END CERTIFICATE" lines should be stripped out and the certificate should be provided on a single line.

```javascript
cert: "MIICizCCAfQCCQCY8tKaMc0BMjANBgkqh ... W==";
```

If you have a certificate in the binary DER encoding, you can convert it to the necessary PEM encoding like this:

```bash
     openssl x509 -inform der -in my_certificate.cer -out my_certificate.pem
```

If the Identity Provider has multiple signing certificates that are valid (such as during the rolling from an old key to a new key and responses signed with either key are valid) then the `cert` configuration key can be an array:

```javascript
cert: ["MIICizCCAfQCCQCY8tKaMc0BMjANBgkqh ... W==", "MIIEOTCCAyGgAwIBAgIJAKZgJdKdCdL6M ... g="];
```

The `cert` configuration key can also be a function that receives a callback as argument calls back a possible error and a certificate or array of certificates. This allows the Identity Provider to be polled for valid certificates and the new certificate can be used if it is changed:

```javascript
    cert: function(callback) { callback(null,polledCertificates); }
```

## Usage with Active Directory Federation Services

Here is a configuration that has been proven to work with ADFS:

```javascript
  {
    entryPoint: 'https://ad.example.net/adfs/ls/',
    issuer: 'https://your-app.example.net/login/callback',
    callbackUrl: 'https://your-app.example.net/login/callback',
    cert: 'MIICizCCAfQCCQCY8tKaMc0BMjANBgkqh ... W==',
    authnContext: 'http://schemas.microsoft.com/ws/2008/06/identity/authenticationmethod/windows',
    identifierFormat: null
  }
```

Please note that ADFS needs to have a trust established to your service in order for this to work.

For more detailed instructions, see [ADFS documentation](docs/adfs/README.md).

## SAML Response Validation - NotBefore and NotOnOrAfter

If the `NotBefore` or the `NotOnOrAfter` attributes are returned in the SAML response, Passport-SAML will validate them
against the current time +/- a configurable clock skew value. The default for the skew is 0s. This is to account for
differences between the clock time on the client (Node server with Passport-SAML) and the server (Identity provider).

`NotBefore` and `NotOnOrAfter` can be part of either the `SubjectConfirmation` element, or within in the `Assertion/Conditions` element
in the SAML response.

## Subject confirmation validation

When configured (turn `validateInResponseTo` to `true` in the Passport-SAML config), the `InResponseTo` attribute will be validated.
Validation will succeed if Passport-SAML previously generated a SAML request with an id that matches the value of `InResponseTo`.

Also note that `InResponseTo` is validated as an attribute of the top level `Response` element in the SAML response, as well
as part of the `SubjectConfirmation` element.

Previous request id's generated for SAML requests will eventually expire. This is controlled with the `requestIdExpirationPeriodMs` option
passed into the Passport-SAML config. The default is 28,800,000 ms (8 hours). Once expired, a subsequent SAML response
received with an `InResponseTo` equal to the expired id will not validate and an error will be returned.

## Cache Provider

When `InResponseTo` validation is turned on, Passport-SAML will store generated request ids used in SAML requests to the IdP. The implementation
of how things are stored, checked to see if they exist, and eventually removed is from the Cache Provider used by Passport-SAML.

The default implementation is a simple in-memory cache provider. For multiple server/process scenarios, this will not be sufficient as
the server/process that generated the request id and stored in memory could be different than the server/process handling the
SAML response. The `InResponseTo` could fail in this case erroneously.

To support this scenario you can provide an implementation for a cache provider by providing an object with following functions:

```javascript
{
    saveAsync: async function(key, value) {
      // saves the key with the optional value, returns the saved value
    },
    getAsync: async function(key) {
      // returns the value if found, null otherwise
    },
    removeAsync: async function(key) {
      // removes the key from the cache, returns the
      // key removed, null if no key is removed
    }
}
```

Provide an instance of an object which has these functions passed to the `cacheProvider` config option when using Passport-SAML.

## SLO (single logout)

Passport-SAML has built in support for SLO including

- Signature validation
- IdP initiated and SP initiated logouts
- Decryption of encrypted name identifiers in IdP initiated logout
- `Redirect` and `POST` SAML Protocol Bindings

## ChangeLog

See [Releases](https://github.com/node-saml/passport-saml/releases) to find the changes that go into each release.

## FAQ

### Is there an example I can look at?

Gerard Braad has provided an example app at https://github.com/gbraad/passport-saml-example/

## Node Support Policy

We only support [Long-Term Support](https://github.com/nodejs/Release) versions of Node.

We specifically limit our support to LTS versions of Node, not because this package won't work on other versions, but because we have a limited amount of time, and supporting LTS offers the greatest return on that investment.

It's possible this package will work correctly on newer versions of Node. It may even be possible to use this package on older versions of Node, though that's more unlikely as we'll make every effort to take advantage of features available in the oldest LTS version we support.

As each Node LTS version reaches its end-of-life we will remove that version from the `node` `engines` property of our package's `package.json` file. Removing a Node version is considered a breaking change and will entail the publishing of a new major version of this package. We will not accept any requests to support an end-of-life version of Node. Any merge requests or issues supporting an end-of-life version of Node will be closed.

We will accept code that allows this package to run on newer, non-LTS, versions of Node.
