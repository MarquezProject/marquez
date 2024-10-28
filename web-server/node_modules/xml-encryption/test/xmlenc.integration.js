var assert = require('assert'),
    fs = require('fs'),
    xmlenc = require('../lib');

var crypto = require('crypto');
var xmldom = require('xmldom');
var xpath = require('xpath');

describe('integration', function() {

  it('should decrypt assertion with aes128', function (done) {
    var result = fs.readFileSync(__dirname + '/assertion-sha1-128.xml').toString();

    xmlenc.decrypt(result, { key: fs.readFileSync(__dirname + '/test-cbc128.key')}, function (err, decrypted) {
      // decrypted content should finish with <saml2:Assertion>
      assert.equal(/<\/saml2:Assertion>$/.test(decrypted), true);
      done();
    });
  });

});
