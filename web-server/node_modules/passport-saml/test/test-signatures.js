const should = require('should'),
      SAML   = require('../lib/passport-saml/index.js').SAML,
      fs     = require('fs'),
      cert   = fs.readFileSync(__dirname + '/static/cert.pem', 'ascii'),
      sinon  = require('sinon');

describe('Signatures', function() {

  const INVALID_ROOT_SIGNATURE = 'Invalid signature on documentElement',
        INVALID_SIGNATURE      = 'Invalid signature',
        createBody             = pathToXml => ({ SAMLResponse: fs.readFileSync(__dirname + '/static/signatures' + pathToXml, 'base64') }),
        tryCatchTest           = ( done, func ) => ( ...args ) => {
          try {
            func(...args);
          }
          catch ( ex ) {
            done(ex);
          }
        },
        testOneResponse        = ( pathToXml, shouldErrorWith, amountOfSignatureChecks = 1 ) => {
          return done => {
            //== Instantiate new instance before every test
            const samlObj              = new SAML({ cert });
            //== Spy on `validateSignature` to be able to count how many times it has been called
            const validateSignatureSpy = sinon.spy(samlObj, 'validateSignature');

            //== Create a body bases on an XML an run the test in `func`
            samlObj.validatePostResponse(createBody(pathToXml), tryCatchTest(done, function( error ) {
              //== Assert error. If the error is `SAML assertion expired` we made it past the certificate validation
              shouldErrorWith ? error.should.eql(new Error(shouldErrorWith)) : error.should.eql(new Error('SAML assertion expired'));
              //== Assert times `validateSignature` was called
              validateSignatureSpy.callCount.should.eql(amountOfSignatureChecks);
              done();
            }));
          };
        };

  describe('Signatures on saml:Response - Only 1 saml:Assertion', () => {
    //== VALID
    it('R1A - both signed => valid', testOneResponse('/valid/response.root-signed.assertion-signed.xml', false, 1));
    it('R1A - root signed => valid', testOneResponse('/valid/response.root-signed.assertion-unsigned.xml', false, 1));
    it('R1A - asrt signed => valid', testOneResponse('/valid/response.root-unsigned.assertion-signed.xml', false, 2));

    //== INVALID
    it('R1A - none signed => error', testOneResponse('/invalid/response.root-unsigned.assertion-unsigned.xml', INVALID_SIGNATURE, 2));
    it('R1A - both signed => error', testOneResponse('/invalid/response.root-signed.assertion-signed.xml', INVALID_SIGNATURE, 2));
    it('R1A - root signed => error', testOneResponse('/invalid/response.root-signed.assertion-unsigned.xml', INVALID_SIGNATURE, 2));
    it('R1A - asrt signed => error', testOneResponse('/invalid/response.root-unsigned.assertion-signed.xml', INVALID_SIGNATURE, 2));
  });

  describe('Signatures on saml:Response - 1 saml:Assertion + 1 saml:Advice containing 1 saml:Assertion', () => {
    //== VALID
    it('R1A1Ad - signed root+asrt+advi => valid', testOneResponse('/valid/response.root-signed.assertion-signed.1advice-signed.xml', false, 1));
    it('R1A1Ad - signed root+asrt => valid', testOneResponse('/valid/response.root-signed.assertion-signed.1advice-unsigned.xml', false, 1));
    it('R1A1Ad - signed asrt+advi => valid', testOneResponse('/valid/response.root-unsigned.assertion-signed.1advice-signed.xml', false, 2));
    it('R1A1Ad - signed root => valid', testOneResponse('/valid/response.root-signed.assertion-unsigned.1advice-unsigned.xml', false, 1));
    it('R1A1Ad - signed asrt => valid', testOneResponse('/valid/response.root-unsigned.assertion-signed.1advice-unsigned.xml', false, 2));

    //== INVALID
    it('R1A1Ad - signed none => error', testOneResponse('/invalid/response.root-unsigned.assertion-unsigned.1advice-unsigned.xml', INVALID_SIGNATURE, 2));
    it('R1A1Ad - signed root+asrt+advi => error', testOneResponse('/invalid/response.root-signed.assertion-signed.1advice-signed.xml', INVALID_SIGNATURE, 2));
    it('R1A1Ad - signed root+asrt => error', testOneResponse('/invalid/response.root-signed.assertion-signed.1advice-unsigned.xml', INVALID_SIGNATURE, 2));
    it('R1A1Ad - signed asrt+advi => error', testOneResponse('/invalid/response.root-unsigned.assertion-signed.1advice-signed.xml', INVALID_SIGNATURE, 2));
    it('R1A1Ad - signed root => error', testOneResponse('/invalid/response.root-signed.assertion-unsigned.1advice-unsigned.xml', INVALID_SIGNATURE, 2));
    it('R1A1Ad - signed asrt => error', testOneResponse('/invalid/response.root-unsigned.assertion-signed.1advice-unsigned.xml', INVALID_SIGNATURE, 2));

  });

  describe('Signatures on saml:Response - 1 saml:Assertion + 1 saml:Advice containing 2 saml:Assertion', () => {
    //== VALID
    it('R1A2Ad - signed root+asrt+advi => error', testOneResponse('/valid/response.root-signed.assertion-signed.2advice-signed.xml', false, 1));
    it('R1A2Ad - signed root+asrt => error', testOneResponse('/valid/response.root-signed.assertion-signed.2advice-unsigned.xml', false, 1));
    it('R1A2Ad - signed root => error', testOneResponse('/valid/response.root-signed.assertion-unsigned.2advice-unsigned.xml', false, 1));

    //== INVALID
    it('R1A2Ad - signed none => error', testOneResponse('/invalid/response.root-unsigned.assertion-unsigned.2advice-unsigned.xml', INVALID_SIGNATURE, 2));
    it('R1A2Ad - signed root+asrt+advi => error', testOneResponse('/invalid/response.root-signed.assertion-signed.2advice-signed.xml', INVALID_SIGNATURE, 2));
    it('R1A2Ad - signed root+asrt => error', testOneResponse('/invalid/response.root-signed.assertion-signed.2advice-unsigned.xml', INVALID_SIGNATURE, 2));
    it('R1A2Ad - signed root => error', testOneResponse('/invalid/response.root-signed.assertion-unsigned.2advice-unsigned.xml', INVALID_SIGNATURE, 2));

  });

});
