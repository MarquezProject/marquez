## xml-crypto

[![Build Status](https://travis-ci.org/yaronn/xml-crypto.png?branch=master)](https://travis-ci.org/yaronn/xml-crypto)

An xml digital signature library for node. Xml encryption is coming soon. Written in pure javascript!

For more information visit [my blog](http://webservices20.blogspot.com/) or [my twitter](https://twitter.com/YaronNaveh).

## Install
Install with [npm](http://github.com/isaacs/npm):

    npm install xml-crypto

A pre requisite it to have [openssl](http://www.openssl.org/) installed and its /bin to be on the system path. I used version 1.0.1c but it should work on older versions too.

## Supported Algorithms

### Canonicalization and Transformation Algorithms

* Canonicalization http://www.w3.org/TR/2001/REC-xml-c14n-20010315
* Canonicalization with comments http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments
* Exclusive Canonicalization http://www.w3.org/2001/10/xml-exc-c14n#
* Exclusive Canonicalization with comments http://www.w3.org/2001/10/xml-exc-c14n#WithComments
* Enveloped Signature transform http://www.w3.org/2000/09/xmldsig#enveloped-signature

### Hashing Algorithms

* SHA1 digests http://www.w3.org/2000/09/xmldsig#sha1
* SHA256 digests http://www.w3.org/2001/04/xmlenc#sha256
* SHA512 digests http://www.w3.org/2001/04/xmlenc#sha512

### Signature Algorithms

* RSA-SHA1 http://www.w3.org/2000/09/xmldsig#rsa-sha1
* RSA-SHA256 http://www.w3.org/2001/04/xmldsig-more#rsa-sha256
* RSA-SHA512 http://www.w3.org/2001/04/xmldsig-more#rsa-sha512

HMAC-SHA1 is also available but it is disabled by default
* HMAC-SHA1 http://www.w3.org/2000/09/xmldsig#hmac-sha1

to enable HMAC-SHA1, do:
```javascript
require( 'xml-crypto' ).SignedXml.enableHMAC();
```
This will enable HMAC and disable digital signature algorithms. Due to key
confusion issues, it is risky to have both HMAC-based and public key digital
signature algorithms enabled at same time.

by default the following algorithms are used:

*Canonicalization/Transformation Algorithm:* Exclusive Canonicalization http://www.w3.org/2001/10/xml-exc-c14n#

*Hashing Algorithm:* SHA1 digest http://www.w3.org/2000/09/xmldsig#sha1

*Signature Algorithm:* RSA-SHA1 http://www.w3.org/2000/09/xmldsig#rsa-sha1

[You are able to extend xml-crypto with custom algorithms.](#customizing-algorithms)


## Signing Xml documents

When signing a xml document you can specify the following properties on a `SignedXml` instance to customize the signature process:

- `sign.signingKey` - **[required]** a `Buffer` or pem encoded `String` containing your private key
- `sign.keyInfoProvider` - **[optional]** a key info provider instance, see [customizing algorithms](#customizing-algorithms) for an implementation example
- `sign.signatureAlgorithm` - **[optional]** one of the supported [signature algorithms](#signature-algorithms). Ex: `sign.signatureAlgorithm = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256"`
- `sign.canonicalizationAlgorithm` - **[optional]** one of the supported [canonicalization algorithms](#canonicalization-and-transformation-algorithms). Ex: `sign.canonicalizationAlgorithm = "http://www.w3.org/2001/10/xml-exc-c14n#WithComments"`

Use this code:

`````javascript
	var SignedXml = require('xml-crypto').SignedXml	  
	  , fs = require('fs')

	var xml = "<library>" +
	            "<book>" +
	              "<name>Harry Potter</name>" +
	            "</book>" +
	          "</library>"

	var sig = new SignedXml()
	sig.addReference("//*[local-name(.)='book']")    
	sig.signingKey = fs.readFileSync("client.pem")
	sig.computeSignature(xml)
	fs.writeFileSync("signed.xml", sig.getSignedXml())

`````

The result will be:


`````xml
	<library>
	  <book Id="_0">
	    <name>Harry Potter</name>
	  </book>
	  <Signature xmlns="http://www.w3.org/2000/09/xmldsig#">
	    <SignedInfo>
	      <CanonicalizationMethod Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#" />
	      <SignatureMethod Algorithm="http://www.w3.org/2000/09/xmldsig#rsa-sha1" />
	      <Reference URI="#_0">
	        <Transforms>
	          <Transform Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#" />
	        </Transforms>
	        <DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1" />
	        <DigestValue>cdiS43aFDQMnb3X8yaIUej3+z9Q=</DigestValue>
	      </Reference>
	    </SignedInfo>
	    <SignatureValue>vhWzpQyIYuncHUZV9W...[long base64 removed]...</SignatureValue>
	  </Signature>
	</library>
`````

Note:

To generate a `<X509Data></X509Data>` element in the signature you must provide a key info implementation, see [customizing algorithms](#customizing-algorithms) for an example.

## Verifying Xml documents

When verifying a xml document you must specify the following properties on a ``SignedXml` instance:

- `sign.keyInfoProvider` - **[required]** a key info provider instance containing your certificate, see [customizing algorithms](#customizing-algorithms) for an implementation example

You can use any dom parser you want in your code (or none, depending on your usage). This sample uses [xmldom](https://github.com/jindw/xmldom) so you should install it first:

    npm install xmldom

Example:

`````javascript
	var select = require('xml-crypto').xpath
	  , dom = require('@xmldom/xmldom').DOMParser
	  , SignedXml = require('xml-crypto').SignedXml
	  , FileKeyInfo = require('xml-crypto').FileKeyInfo  
	  , fs = require('fs')

	var xml = fs.readFileSync("signed.xml").toString()
	var doc = new dom().parseFromString(xml)    

	var signature = select(doc, "//*[local-name(.)='Signature' and namespace-uri(.)='http://www.w3.org/2000/09/xmldsig#']")[0]
	var sig = new SignedXml()
	sig.keyInfoProvider = new FileKeyInfo("client_public.pem")
	sig.loadSignature(signature)
	var res = sig.checkSignature(xml)
	if (!res) console.log(sig.validationErrors)	
`````

if the verification process fails `sig.validationErrors` will have the errors.

In order to protect from some attacks we must check the content we want to use is the one that has been signed:
`````javascript
	var elem = select(doc, "/xpath_to_interesting_element");
	var uri = sig.references[0].uri; // might not be 0 - depending on the document you verify
        var id = (uri[0] === '#') ? uri.substring(1) : uri;
        if (elem.getAttribute('ID') != id && elem.getAttribute('Id') != id && elem.getAttribute('id') != id)
          throw new Error('the interesting element was not the one verified by the signature')
`````

Note:

The xml-crypto api requires you to supply it separately the xml signature ("&lt;Signature&gt;...&lt;/Signature&gt;", in loadSignature) and the signed xml (in checkSignature). The signed xml may or may not contain the signature in it, but you are still required to supply the signature separately.


### Caring for Implicit transform
If you fail to verify signed XML, then one possible cause is that there are some hidden implicit transforms(#).  
(#) Normalizing XML document to be verified. i.e. remove extra space within a tag, sorting attributes, importing namespace declared in ancestor nodes, etc.

The reason for these implicit transform might come from [complex xml signature specification](https://www.w3.org/TR/2002/REC-xmldsig-core-20020212),
which makes XML developers confused and then leads to incorrect implementation for signing XML document.

If you keep failing verification, it is worth trying to guess such a hidden transform and specify it to the option as below:

```javascript
var option = {implicitTransforms: ["http://www.w3.org/TR/2001/REC-xml-c14n-20010315"]}
var sig = new SignedXml(null, option)
sig.keyInfoProvider = new FileKeyInfo("client_public.pem")
sig.loadSignature(signature)
var res = sig.checkSignature(xml)
```

You might find it difficult to guess such transforms, but there are typical transforms you can try.

- http://www.w3.org/TR/2001/REC-xml-c14n-20010315
- http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments
- http://www.w3.org/2001/10/xml-exc-c14n#
- http://www.w3.org/2001/10/xml-exc-c14n#WithComments

## API

### xpath

See [xpath.js](https://github.com/yaronn/xpath.js) for usage. Note that this is actually using
[another library](https://github.com/goto100/xpath) as the underlying implementation.

### SignedXml

The `SignedXml` constructor provides an abstraction for sign and verify xml documents. The object is constructed using `new SignedXml([idMode])` where:

- `idMode` - if the value of `"wssecurity"` is passed it will create/validate id's with the ws-security namespace.

*API*

A `SignedXml` object provides the following methods:

To sign xml documents:

- `addReference(xpath, [transforms], [digestAlgorithm])` - adds a reference to a xml element where:
    - `xpath` - a string containing a XPath expression referencing a xml element
    - `transforms` - an array of [transform algorithms](#canonicalization-and-transformation-algorithms), the referenced element will be transformed for each value in the array
    - `digestAlgorithm` - one of the supported [hashing algorithms](#hashing-algorithms)
- `computeSignature(xml, [options])` - compute the signature of the given xml where:
    - `xml` - a string containing a xml document
    - `options` - an object with the following properties:
        - `prefix` - adds this value as a prefix for the generated signature tags
        - `attrs` - a hash of attributes and values `attrName: value` to add to the signature root node
        - `location` - customize the location of the signature, pass an object with a `reference` key which should contain a XPath expression to a reference node, an `action` key which should contain one of the following values: `append`, `prepend`, `before`, `after`
        - `existingPrefixes` - A hash of prefixes and namespaces `prefix: namespace` that shouldn't be in the signature because they already exist in the xml 
- `getSignedXml()` - returns the original xml document with the signature in it, **must be called only after `computeSignature`**
- `getSignatureXml()` - returns just the signature part, **must be called only after `computeSignature`**
- `getOriginalXmlWithIds()` - returns the original xml with Id attributes added on relevant elements (required for validation), **must be called only after `computeSignature`**

To verify xml documents:

- `loadSignature(signatureXml)` - loads the signature where:
    - `signatureXml` - a string or node object (like an [xml-dom](https://github.com/jindw/xmldom) node) containing the xml representation of the signature
- `checkSignature(xml)` - validates the given xml document and returns true if the validation was successful, `sig.validationErrors` will have the validation errors if any, where:
    - `xml` - a string containing a xml document


### FileKeyInfo

A basic key info provider implementation using `fs.readFileSync(file)`, is constructed using `new FileKeyInfo([file])` where:

- `file` - a path to a pem encoded certificate

See [verifying xml documents](#verifying-xml-documents) for an example usage


## Customizing Algorithms
The following sample shows how to sign a message using custom algorithms.

First import some modules:

`````javascript
	var SignedXml = require('xml-crypto').SignedXml
	  , fs = require('fs')
`````


Now define the extension point you want to implement. You can choose one or more.

A key info provider is used to extract and construct the key and the KeyInfo xml section.
Implement it if you want to create a signature with a KeyInfo section, or you want to read your key in a different way then the default file read option.
`````javascript
	/**/
	function MyKeyInfo() {
	  this.getKeyInfo = function(key, prefix) {
        prefix = prefix || ''
        prefix = prefix ? prefix + ':' : prefix
	    return "<" + prefix + "X509Data></" + prefix + "X509Data>"
	  }
	  this.getKey = function(keyInfo) {
	    //you can use the keyInfo parameter to extract the key in any way you want      
	    return fs.readFileSync("key.pem")
	  }
	}
`````

A custom hash algorithm is used to calculate digests. Implement it if you want a hash other than the default SHA1.

`````javascript
	function MyDigest() {


	  this.getHash = function(xml) {    
	    return "the base64 hash representation of the given xml string"
	  }

	  this.getAlgorithmName = function() {
	    return "http://myDigestAlgorithm"
	  }
	}
`````

A custom signing algorithm. The default is RSA-SHA1
`````javascript
	function MySignatureAlgorithm() {

	  /*sign the given SignedInfo using the key. return base64 signature value*/
	  this.getSignature = function(signedInfo, signingKey) {            
	    return "signature of signedInfo as base64..."
	  }

	  this.getAlgorithmName = function() {
	    return "http://mySigningAlgorithm"
	  }

	}
`````

Custom transformation algorithm. The default is exclusive canonicalization.

`````javascript
	function MyTransformation() {

	  /*given a node (from the xmldom module) return its canonical representation (as string)*/
	  this.process = function(node) {	  	
	  	//you should apply your transformation before returning
	    return node.toString()
	  }

	  this.getAlgorithmName = function() {
	    return "http://myTransformation"
	  }
	}
`````
Custom canonicalization is actually the same as custom transformation. It is applied on the SignedInfo rather than on references.

`````javascript
	function MyCanonicalization() {

	  /*given a node (from the xmldom module) return its canonical representation (as string)*/
	  this.process = function(node) {
	    //you should apply your transformation before returning
	    return "< x/>"
	  }

	   this.getAlgorithmName = function() {
	    return "http://myCanonicalization"
	  }
	}
`````

Now you need to register the new algorithms:

`````javascript
	/*register all the custom algorithms*/

	SignedXml.CanonicalizationAlgorithms["http://MyTransformation"] = MyTransformation
	SignedXml.CanonicalizationAlgorithms["http://MyCanonicalization"] = MyCanonicalization
	SignedXml.HashAlgorithms["http://myDigestAlgorithm"] = MyDigest
	SignedXml.SignatureAlgorithms["http://mySigningAlgorithm"] = MySignatureAlgorithm
`````

Now do the signing. Note how we configure the signature to use the above algorithms:

`````javascript
	function signXml(xml, xpath, key, dest)
	{
	  var sig = new SignedXml()

	  /*configure the signature object to use the custom algorithms*/
	  sig.signatureAlgorithm = "http://mySignatureAlgorithm"
	  sig.keyInfoProvider = new MyKeyInfo()
	  sig.canonicalizationAlgorithm = "http://MyCanonicalization"
	  sig.addReference("//*[local-name(.)='x']", ["http://MyTransformation"], "http://myDigestAlgorithm")

	  sig.signingKey = fs.readFileSync(key)
	  sig.addReference(xpath)    
	  sig.computeSignature(xml)
	  fs.writeFileSync(dest, sig.getSignedXml())
	}

	var xml = "<library>" +
	            "<book>" +
	              "<name>Harry Potter</name>" +
	            "</book>"
	          "</library>"

	signXml(xml,
	  "//*[local-name(.)='book']",
	  "client.pem",
	  "result.xml")
`````

You can always look at the actual code as a sample (or drop me a [mail](mailto:yaronn01@gmail.com)).

## Asynchronous signing and verification

If the private key is not stored locally and you wish to use a signing server or Hardware Security Module (HSM) to sign documents you can create a custom signing algorithm that uses an asynchronous callback.

`````javascript
  function AsyncSignatureAlgorithm() {
    this.getSignature = function (signedInfo, signingKey, callback) {
      var signer = crypto.createSign("RSA-SHA1")
      signer.update(signedInfo)
      var res = signer.sign(signingKey, 'base64')
      //Do some asynchronous things here
      callback(null, res)
    }
    this.getAlgorithmName = function () {
      return "http://www.w3.org/2000/09/xmldsig#rsa-sha1"
    }
  }

  SignedXml.SignatureAlgorithms["http://asyncSignatureAlgorithm"] = AsyncSignatureAlgorithm
  var sig = new SignedXml()
  sig.signatureAlgorithm = "http://asyncSignatureAlgorithm"
  sig.computeSignature(xml, opts, function(err){
    var signedResponse = sig.getSignedXml()
  })
`````

The function `sig.checkSignature` may also use a callback if asynchronous verification is needed.

## X.509 / Key formats
Xml-Crypto internally relies on node's crypto module. This means pem encoded certificates are supported. So to sign an xml use key.pem that looks like this (only the begining of the key content is shown):

	-----BEGIN PRIVATE KEY-----
	MIICdwIBADANBgkqhkiG9w0...
	-----END PRIVATE KEY-----

And for verification use key_public.pem:

	-----BEGIN CERTIFICATE-----
	MIIBxDCCAW6gAwIBAgIQxUSX...
	-----END CERTIFICATE-----

**Converting .pfx certificates to pem**

If you have .pfx certificates you can convert them to .pem using [openssl](http://www.openssl.org/):

	openssl pkcs12 -in c:\certs\yourcert.pfx -out c:\certs\cag.pem

Then you could use the result as is for the purpose of signing. For the purpose of validation open the resulting .pem with a text editor and copy from -----BEGIN CERTIFICATE----- to  -----END CERTIFICATE----- (including) to a new text file and save it as .pem.

## Examples

- [how to sign a root node](#) *coming soon*

###how to add a prefix for the signature###
Use the `prefix` option when calling `computeSignature` to add a prefix to the signature. 
`````javascript
var SignedXml = require('xml-crypto').SignedXml	  
  , fs = require('fs');

var xml = "<library>" +
            "<book>" +
              "<name>Harry Potter</name>" +
            "</book>" +
          "</library>";

var sig = new SignedXml();
sig.addReference("//*[local-name(.)='book']");
sig.signingKey = fs.readFileSync("client.pem");
sig.computeSignature(xml,{
  prefix: 'ds'
});
`````

###how to specify the location of the signature###
Use the `location` option when calling `computeSignature` to move the signature around. 
Set `action` to one of the following: 
- append(default) - append to the end of the xml document
- prepend - prepend to the xml document
- before - prepend to a specific node (use the `referenceNode` property)
- after - append to specific node (use the `referenceNode` property)

`````javascript
var SignedXml = require('xml-crypto').SignedXml	  
  , fs = require('fs');

var xml = "<library>" +
            "<book>" +
              "<name>Harry Potter</name>" +
            "</book>" +
          "</library>";

var sig = new SignedXml();
sig.addReference("//*[local-name(.)='book']");
sig.signingKey = fs.readFileSync("client.pem");
sig.computeSignature(xml,{
  location: { reference: "//*[local-name(.)='book']", action: "after" } //This will place the signature after the book element
});

`````
*more examples coming soon*

## Development
The test framework is [nodeunit](https://github.com/caolan/nodeunit). To run tests use:

    $> npm test

## More information
Visit my [blog](http://webservices20.blogspot.com/) or my [twitter](http://twitter.com/#!/YaronNaveh)


[![Bitdeli Badge](https://d2weczhvl823v0.cloudfront.net/yaronn/xml-crypto/trend.png)](https://bitdeli.com/free "Bitdeli Badge")

## License

This project is licensed under the [MIT License](http://opensource.org/licenses/MIT). See the [LICENSE](LICENSE) file for more info.
