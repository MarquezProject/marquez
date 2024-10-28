var assert = require('assert');
var fs = require('fs');
var should = require('should');
var sinon = require('sinon');
var xmlenc = require('../lib');
var xpath = require('xpath');

describe('encrypt', function() {
  let consoleSpy = null;
  beforeEach(function() {
    consoleSpy = sinon.spy(console, 'warn');
  });

  afterEach(function() {
    consoleSpy.restore();
  });

  var algorithms = [{
    name: 'aes-256-cbc',
    encryptionOptions: {
      encryptionAlgorithm: 'http://www.w3.org/2001/04/xmlenc#aes256-cbc',
      keyEncryptionAlgorithm: 'http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p'
    }
  }, {
    name: 'aes-128-cbc',
    encryptionOptions: {
      encryptionAlgorithm: 'http://www.w3.org/2001/04/xmlenc#aes128-cbc',
      keyEncryptionAlgorithm: 'http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p'
    }
  }, {
    name: 'aes-256-gcm',
    encryptionOptions: {
      encryptionAlgorithm: 'http://www.w3.org/2009/xmlenc11#aes256-gcm',
      keyEncryptionAlgorithm: 'http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p'
    }
  }, {
    name: 'aes-128-gcm',
    encryptionOptions: {
      encryptionAlgorithm: 'http://www.w3.org/2009/xmlenc11#aes128-gcm',
      keyEncryptionAlgorithm: 'http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p'
    }
  }, {
    name: 'des-ede3-cbc',
    encryptionOptions: {
      encryptionAlgorithm: 'http://www.w3.org/2001/04/xmlenc#tripledes-cbc',
      keyEncryptionAlgorithm: 'http://www.w3.org/2001/04/xmlenc#rsa-1_5'
    }
  }];

  algorithms.forEach(function (algorithm) {
    describe(algorithm.name, function () {
      it('should encrypt and decrypt xml', function (done) {
        _shouldEncryptAndDecrypt('content to encrypt', algorithm.encryptionOptions, done);
      });

      it('should encrypt and decrypt xml with utf8 chars', function (done) {
        _shouldEncryptAndDecrypt('Gnügge Gnügge Gnügge Gnügge Gnügge Gnügge Gnügge Gnügge Gnügge Gnügge', algorithm.encryptionOptions, done);
      });
    });
  });

  function _shouldEncryptAndDecrypt(content, options, done) {
    // cert created with:
    // openssl req -x509 -new -newkey rsa:2048 -nodes -subj '/CN=auth0.auth0.com/O=Auth0 LLC/C=US/ST=Washington/L=Redmond' -keyout auth0.key -out auth0.pem
    // pub key extracted from (only the RSA public key between BEGIN PUBLIC KEY and END PUBLIC KEY)
    // openssl x509 -in "test-auth0.pem" -pubkey

    options.rsa_pub = fs.readFileSync(__dirname + '/test-auth0_rsa.pub'),
    options.pem = fs.readFileSync(__dirname + '/test-auth0.pem'),
    options.key = fs.readFileSync(__dirname + '/test-auth0.key'),
    options.warnInsecureAlgorithm = false;

    xmlenc.encrypt(content, options, function(err, result) {
      xmlenc.decrypt(result, { key: fs.readFileSync(__dirname + '/test-auth0.key'), warnInsecureAlgorithm: false}, function (err, decrypted) {
        assert.equal(decrypted, content);
        done();
      });
    });
  }

  describe('des-ede3-cbc fails', function() {
    it('should fail encryption when disallowEncryptionWithInsecureAlgorithm is set', function(done) {
      const options = {
      rsa_pub: fs.readFileSync(__dirname + '/test-auth0_rsa.pub'),
      pem: fs.readFileSync(__dirname + '/test-auth0.pem'),
      key: fs.readFileSync(__dirname + '/test-auth0.key'),
      disallowEncryptionWithInsecureAlgorithm: true,
      encryptionAlgorithm: 'http://www.w3.org/2001/04/xmlenc#tripledes-cbc',
      keyEncryptionAlgorithm: 'http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p'
    }
      xmlenc.encrypt('encrypt me', options, function(err, result) {
        assert(err);
        assert(!result);
        //should not pop up warns due to options.warnInsecureAlgorithm = false;
        consoleSpy.called.should.equal(false);
        done();
      });
    });

    it('should fail decryption when disallowDecryptionWithInsecureAlgorithm is set', function(done) {
      const options = {
      rsa_pub: fs.readFileSync(__dirname + '/test-auth0_rsa.pub'),
      pem: fs.readFileSync(__dirname + '/test-auth0.pem'),
      key: fs.readFileSync(__dirname + '/test-auth0.key'),
      encryptionAlgorithm: 'http://www.w3.org/2001/04/xmlenc#tripledes-cbc',
      keyEncryptionAlgorithm: 'http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p'
      }
      xmlenc.encrypt('encrypt me', options, function(err, result) {
        xmlenc.decrypt(result,
          { key: fs.readFileSync(__dirname + '/test-auth0.key'),
            disallowDecryptionWithInsecureAlgorithm: true},
          function (err, decrypted) {
            assert(err);
            assert(!decrypted);
            done();
        });
      });
    });
  });

  describe('rsa-1.5 fails', function() {
    it('should fail encryption when disallowEncryptionWithInsecureAlgorithm is set', function(done) {
      const options = {
      rsa_pub: fs.readFileSync(__dirname + '/test-auth0_rsa.pub'),
      pem: fs.readFileSync(__dirname + '/test-auth0.pem'),
      key: fs.readFileSync(__dirname + '/test-auth0.key'),
      disallowEncryptionWithInsecureAlgorithm: true,
      encryptionAlgorithm: 'http://www.w3.org/2001/04/xmlenc#aes256-cbc',
      keyEncryptionAlgorithm: 'http://www.w3.org/2001/04/xmlenc#rsa-1_5'
    }
      xmlenc.encrypt('encrypt me', options, function(err, result) {
        assert(err);
        assert(!result);
        done();
      });
    });

    it('should fail decryption when disallowDecryptionWithInsecureAlgorithm is set', function(done) {
      const options = {
      rsa_pub: fs.readFileSync(__dirname + '/test-auth0_rsa.pub'),
      pem: fs.readFileSync(__dirname + '/test-auth0.pem'),
      key: fs.readFileSync(__dirname + '/test-auth0.key'),
      encryptionAlgorithm: 'http://www.w3.org/2001/04/xmlenc#aes256-cbc',
      keyEncryptionAlgorithm: 'http://www.w3.org/2001/04/xmlenc#rsa-1_5'
      }
      xmlenc.encrypt('encrypt me', options, function(err, result) {
        xmlenc.decrypt(result,
          { key: fs.readFileSync(__dirname + '/test-auth0.key'),
            disallowDecryptionWithInsecureAlgorithm: true},
          function (err, decrypted) {
            assert(err);
            assert(!decrypted);
            done();
        });
      });
    });
  });


  it('should encrypt and decrypt keyinfo', function (done) {
    var options = {
      rsa_pub: fs.readFileSync(__dirname + '/test-auth0_rsa.pub'),
      pem: fs.readFileSync(__dirname + '/test-auth0.pem'),
      keyEncryptionAlgorithm: 'http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p'
    };

    var plaintext = 'The quick brown fox jumps over the lazy dog';

    xmlenc.encryptKeyInfo(plaintext, options, function(err, encryptedKeyInfo) {
      if (err) return done(err);

      var decryptedKeyInfo = xmlenc.decryptKeyInfo(
        encryptedKeyInfo,
        {key: fs.readFileSync(__dirname + '/test-auth0.key')}
      );
      assert.equal(decryptedKeyInfo.toString(), plaintext);

      done();
    });
  });

  it('should decrypt xml with odd padding (aes256-cbc)', function (done) {
    var encryptedContent = fs.readFileSync(__dirname + '/test-cbc256-padding.xml').toString()
    xmlenc.decrypt(encryptedContent, { key: fs.readFileSync(__dirname + '/test-auth0.key')}, function(err, decrypted) {
      assert.ifError(err);
      assert.equal(decrypted, 'content');
      done();
    });
  });

  it('should catch error if padding length > 16', function (done) {
    var encryptedContent = fs.readFileSync(__dirname + '/test-padding-length.xml').toString();
    xmlenc.decrypt(encryptedContent, { key: fs.readFileSync(__dirname + '/test-auth0.key')}, function(err, decrypted) {
      assert(err);
      done();
    });
  });

  it('should fail encrypt when disallowEncryptionWithInsecureAlgorithm is set', function (done) {
    var options = {
      rsa_pub: fs.readFileSync(__dirname + '/test-auth0_rsa.pub'),
      pem: fs.readFileSync(__dirname + '/test-auth0.pem'),
      keyEncryptionAlgorithm: 'http://www.w3.org/2001/04/xmlenc#rsa-1_5',
      disallowEncryptionWithInsecureAlgorithm: true
    };

    var plaintext = 'The quick brown fox jumps over the lazy dog';
    xmlenc.encryptKeyInfo(plaintext, options, function(err, encryptedKeyInfo) {
      assert(err);
      done();
    });
  });

  it('should encrypt and fail decrypt due to insecure algorithms', function (done) {
    var options = {
      rsa_pub: fs.readFileSync(__dirname + '/test-auth0_rsa.pub'),
      pem: fs.readFileSync(__dirname + '/test-auth0.pem'),
      keyEncryptionAlgorithm: 'http://www.w3.org/2001/04/xmlenc#rsa-1_5'
    };

    var plaintext = 'The quick brown fox jumps over the lazy dog';

    xmlenc.encryptKeyInfo(plaintext, options, function(err, encryptedKeyInfo) {
      if (err) return done(err);
      consoleSpy.called.should.equal(true);
      assert.throws(
        function(){xmlenc.decryptKeyInfo(
          encryptedKeyInfo,
          {key: fs.readFileSync(__dirname + '/test-auth0.key'),
          disallowDecryptionWithInsecureAlgorithm: true})},
        Error,
        "Error thrown due to disallowing insecure algorithms.");

      done();
    });
  });
});
