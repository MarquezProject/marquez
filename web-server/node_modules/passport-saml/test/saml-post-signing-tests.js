const fs = require('fs');
const should = require('should');
const samlPostSigning = require('../lib/passport-saml/saml-post-signing');
const signSamlPost = samlPostSigning.signSamlPost;
const signAuthnRequestPost = samlPostSigning.signAuthnRequestPost;

const signingKey = fs.readFileSync(__dirname + '/static/key.pem');

describe('SAML POST Signing', function () {
  it('should sign a simple saml request', function () {
    var xml = '<SAMLRequest><saml2:Issuer xmlns:saml2="urn:oasis:names:tc:SAML:2.0:assertion">http://example.com</saml2:Issuer></SAMLRequest>';
    var result = signSamlPost(xml, '/SAMLRequest', { privateCert: signingKey });
    result.should.match(/<DigestValue>[A-Za-z0-9\/\+\=]+<\/DigestValue>/);
    result.should.match(/<SignatureValue>[A-Za-z0-9\/\+\=]+<\/SignatureValue>/);
  });

  it('should place the Signature element after the Issuer element', function () {
    var xml = '<SAMLRequest><saml2:Issuer xmlns:saml2="urn:oasis:names:tc:SAML:2.0:assertion">http://example.com</saml2:Issuer><SomeOtherElement /></SAMLRequest>';
    var result = signSamlPost(xml, '/SAMLRequest', { privateCert: signingKey });
    result.should.match(/<\/saml2:Issuer><Signature/);
    result.should.match(/<\/Signature><SomeOtherElement/);
  });

  it('should sign and digest with SHA256 when specified', function () {
    var xml = '<SAMLRequest><saml2:Issuer xmlns:saml2="urn:oasis:names:tc:SAML:2.0:assertion">http://example.com</saml2:Issuer></SAMLRequest>';
    var options = {
      signatureAlgorithm: 'sha256',
      digestAlgorithm: 'sha256',
      privateCert: signingKey
    }
    var result = signSamlPost(xml, '/SAMLRequest', options);
    result.should.match(/<SignatureMethod Algorithm="http:\/\/www.w3.org\/2001\/04\/xmldsig-more#rsa-sha256"/);
    result.should.match(/<Transform Algorithm="http:\/\/www.w3.org\/2001\/10\/xml-exc-c14n#"\/>/);
    result.should.match(/<Transform Algorithm="http:\/\/www.w3.org\/2000\/09\/xmldsig#enveloped-signature"\/>/);
    result.should.match(/<DigestMethod Algorithm="http:\/\/www.w3.org\/2001\/04\/xmlenc#sha256"\/>/);
  });

  it('should sign an AuthnRequest', function () {
    var xml = '<AuthnRequest xmlns="urn:oasis:names:tc:SAML:2.0:protocol"><saml2:Issuer xmlns:saml2="urn:oasis:names:tc:SAML:2.0:assertion">http://example.com</saml2:Issuer></AuthnRequest>';
    var result = signAuthnRequestPost(xml, { privateCert: signingKey });
    result.should.match(/<DigestValue>[A-Za-z0-9\/\+\=]+<\/DigestValue>/);
    result.should.match(/<SignatureValue>[A-Za-z0-9\/\+\=]+<\/SignatureValue>/);
  });
});
