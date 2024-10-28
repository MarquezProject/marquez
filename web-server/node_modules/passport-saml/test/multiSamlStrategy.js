'use strict';

var sinon = require('sinon');
var should = require( 'should' );
var SamlStrategy = require( '../lib/passport-saml/index.js' ).Strategy;
var MultiSamlStrategy = require( '../multiSamlStrategy' );

function verify () {}

describe('Strategy()', function() {
  it('extends passport Strategy', function() {
    function getSamlOptions () { return {} }
    var strategy = new MultiSamlStrategy({ getSamlOptions: getSamlOptions }, verify);
    strategy.should.be.an.instanceOf(SamlStrategy);
  });

  it('throws if wrong finder is provided', function() {
    function createStrategy (){ return new MultiSamlStrategy({}, verify) };
    should.throws(createStrategy);
   });
});

describe('strategy#authenticate', function() {
  beforeEach(function() {
    this.superAuthenticateStub = sinon.stub(SamlStrategy.prototype, 'authenticate');
  });

  afterEach(function() {
    this.superAuthenticateStub.restore();
  });

  it('calls super with request and auth options', function(done) {
    var superAuthenticateStub = this.superAuthenticateStub;
    function getSamlOptions (req, fn) {
      try {
        fn();
        sinon.assert.calledOnce(superAuthenticateStub);
        done();
      } catch (err2) {
        done(err2);
      }
    };

    var strategy = new MultiSamlStrategy({
      getSamlOptions: getSamlOptions
    }, verify);
    strategy.authenticate();
  });

  it('passes options on to saml strategy', function(done) {
    var passportOptions = {
      passReqToCallback: true,
      authnRequestBinding: 'HTTP-POST',
      getSamlOptions: function (req, fn) {
        try {
          fn();
          strategy._passReqToCallback.should.eql(true);
          strategy._authnRequestBinding.should.eql('HTTP-POST');
          done();
        } catch (err2) {
          done(err2);
        }
      }
    };

    var strategy = new MultiSamlStrategy(passportOptions, verify);
    strategy.authenticate();
  });

  it('uses given options to setup internal saml provider', function(done) {
    var superAuthenticateStub = this.superAuthenticateStub;
    var samlOptions = {
      issuer: 'http://foo.issuer',
      callbackUrl: 'http://foo.callback',
      cert: 'deadbeef',
      host: 'lvh',
      acceptedClockSkewMs: -1,
      identifierFormat:
        'urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress',
      path: '/saml/callback',
      logoutUrl: 'http://foo.slo',
      signatureAlgorithm: 'sha256'
    };

    function getSamlOptions (req, fn) {
      try {
        fn(null, samlOptions);
        sinon.assert.calledOnce(superAuthenticateStub)
        superAuthenticateStub.calledWith(Object.assign(
          {},
          { cacheProvider: 'mock cache provider' },
          samlOptions
        ));
        done();
      } catch (err2) {
        done(err2);
      }
    }

    var strategy = new MultiSamlStrategy(
      { getSamlOptions: getSamlOptions, cacheProvider: 'mock cache provider'},
      verify
    );
    strategy.authenticate();
  });
});

describe('strategy#logout', function() {
  beforeEach(function() {
    this.superLogoutMock = sinon.stub(SamlStrategy.prototype, 'logout');
  });

  afterEach(function() {
    this.superLogoutMock.restore();
  });

  it('calls super with request and auth options', function(done) {
    var superLogoutMock = this.superLogoutMock;
    function getSamlOptions (req, fn) {
      try {
        fn();
        sinon.assert.calledOnce(superLogoutMock);
        done();
      } catch (err2) {
        done(err2);
      }
    };

    var strategy = new MultiSamlStrategy({ getSamlOptions: getSamlOptions }, verify);
    strategy.logout();
  });

  it('passes options on to saml strategy', function(done) {
    var passportOptions = {
      passReqToCallback: true,
      authnRequestBinding: 'HTTP-POST',
      getSamlOptions: function (req, fn) {
        try {
          fn();
          strategy._passReqToCallback.should.eql(true);
          strategy._authnRequestBinding.should.eql('HTTP-POST');
          done();
        } catch (err2) {
          done(err2);
        }
      }
    };

    var strategy = new MultiSamlStrategy(passportOptions, verify);
    strategy.logout();
  });

  it('uses given options to setup internal saml provider', function(done) {
    var superLogoutMock = this.superLogoutMock;
    var samlOptions = {
      issuer: 'http://foo.issuer',
      callbackUrl: 'http://foo.callback',
      cert: 'deadbeef',
      host: 'lvh',
      acceptedClockSkewMs: -1,
      identifierFormat:
        'urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress',
      path: '/saml/callback',
      logoutUrl: 'http://foo.slo',
      signatureAlgorithm: 'sha256'
    };

    function getSamlOptions (req, fn) {
      try {
        fn(null, samlOptions);
        sinon.assert.calledOnce(superLogoutMock)
        superLogoutMock.calledWith(Object.assign(
          {},
          samlOptions
        ));
        done();
      } catch (err2) {
        done(err2);
      }
    }

    var strategy = new MultiSamlStrategy(
      { getSamlOptions: getSamlOptions },
      verify
    );
    strategy.logout();
  });
});

describe('strategy#generateServiceProviderMetadata', function() {
  beforeEach(function() {
    this.superGenerateServiceProviderMetadata = sinon.stub(SamlStrategy.prototype, 'generateServiceProviderMetadata').returns('My Metadata Result');
  });

  afterEach(function() {
    this.superGenerateServiceProviderMetadata.restore();
  });

  it('calls super with request and generateServiceProviderMetadata options', function(done) {
    var superGenerateServiceProviderMetadata = this.superGenerateServiceProviderMetadata;
    function getSamlOptions (req, fn) {
      try {
        fn();
        sinon.assert.calledOnce(superGenerateServiceProviderMetadata);
        superGenerateServiceProviderMetadata.calledWith('bar', 'baz');
        req.should.eql('foo');
        done();
      } catch (err2) {
        done(err2);
      }
    };


    var strategy = new MultiSamlStrategy({ getSamlOptions: getSamlOptions }, verify);
    strategy.generateServiceProviderMetadata('foo', 'bar', 'baz', function () {});
  });

  it('passes options on to saml strategy', function(done) {
    var passportOptions = {
      passReqToCallback: true,
      authnRequestBinding: 'HTTP-POST',
      getSamlOptions: function (req, fn) {
        try {
          fn();
          strategy._passReqToCallback.should.eql(true);
          strategy._authnRequestBinding.should.eql('HTTP-POST');
          done();
        } catch (err2) {
          done(err2);
        }
      }
    };

    var strategy = new MultiSamlStrategy(passportOptions, verify);
    strategy.generateServiceProviderMetadata('foo', 'bar', 'baz', function () {});
  });

  it('should pass error to callback function', function(done) {
    var passportOptions = {
      getSamlOptions: function (req, fn) {
        fn('My error');
      }
    };

    var strategy = new MultiSamlStrategy(passportOptions, verify);
    strategy.generateServiceProviderMetadata('foo', 'bar', 'baz', function (error, result) {
      try {
        should(error).equal('My error');
        done();
      } catch (err2) {
        done(err2);
      }
    });
  });

  it('should pass result to callback function', function(done) {
    var passportOptions = {
      getSamlOptions: function (req, fn) {
        fn();
      }
    };

    var strategy = new MultiSamlStrategy(passportOptions, verify);
    strategy.generateServiceProviderMetadata('foo', 'bar', 'baz', function (error, result) {
      try {
        should(result).equal('My Metadata Result');
        done();
      } catch (err2) {
        done(err2);
      }
    });
  });
});
