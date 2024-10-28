// This will help generate signing info for test cases.
// Simply fill in the data and run it to get <DigestValue /> and <SignatureValue />.

const crypto = require('crypto')

const private_key = `-----BEGIN PRIVATE KEY-----

-----END PRIVATE KEY-----
`

const cert = `-----BEGIN CERTIFICATE-----

-----END CERTIFICATE-----
`

const saml_message = ``

const signed_info = `<SignedInfo...</SignedInfo>`

const signer = crypto.createSign('RSA-SHA1');
signer.update(signed_info);
signer.end();

const signature = signer.sign(private_key)
const signature_b64 = signature.toString('base64')

const verifier = crypto.createVerify('RSA-SHA1')
verifier.update(signed_info)
verifier.end()

const verified = verifier.verify(cert, signature)

const hash = crypto.createHash('RSA-SHA1')
hash.update(saml_message, 'utf8')
const digest_b64 = hash.digest('base64')

console.log(JSON.stringify({
	signature: signature_b64,
	digest: digest_b64,
	verified: verified,
}, null, 2))
