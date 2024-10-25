var xpath = require('xpath')
  , Dom = require('@xmldom/xmldom').DOMParser
  , utils = require('./utils')
  , c14n = require('./c14n-canonicalization')
  , execC14n = require('./exclusive-canonicalization')
  , EnvelopedSignature = require('./enveloped-signature').EnvelopedSignature
  , crypto = require('crypto')
  , fs = require('fs')

exports.SignedXml = SignedXml
exports.FileKeyInfo = FileKeyInfo

/**
 * A key info provider implementation
 *
 */
function FileKeyInfo(file) {
  this.file = file

  this.getKeyInfo = function(key, prefix) {
    prefix = prefix || ''
    prefix = prefix ? prefix + ':' : prefix
    return "<" + prefix + "X509Data></" + prefix + "X509Data>"
  }

  this.getKey = function(keyInfo) {
    return fs.readFileSync(this.file)
  }
}

/**
 * Hash algorithm implementation
 *
 */
function SHA1() {

  this.getHash = function(xml) {
    var shasum = crypto.createHash('sha1')
    shasum.update(xml, 'utf8')
    var res = shasum.digest('base64')
    return res
  }

  this.getAlgorithmName = function() {
    return "http://www.w3.org/2000/09/xmldsig#sha1"
  }
}

function SHA256() {

  this.getHash = function(xml) {
    var shasum = crypto.createHash('sha256')
    shasum.update(xml, 'utf8')
    var res = shasum.digest('base64')
    return res
  }

  this.getAlgorithmName = function() {
    return "http://www.w3.org/2001/04/xmlenc#sha256"
  }
}

function SHA512() {

  this.getHash = function(xml) {
    var shasum = crypto.createHash('sha512')
    shasum.update(xml, 'utf8')
    var res = shasum.digest('base64')
    return res
  }

  this.getAlgorithmName = function() {
    return "http://www.w3.org/2001/04/xmlenc#sha512"
  }
}


/**
 * Signature algorithm implementation
 *
 */
function RSASHA1() {

  /**
  * Sign the given string using the given key
  *
  */
  this.getSignature = function(signedInfo, signingKey, callback) {
    var signer = crypto.createSign("RSA-SHA1")
    signer.update(signedInfo)
    var res = signer.sign(signingKey, 'base64')
    if (callback) callback(null, res)
    return res
  }

  /**
  * Verify the given signature of the given string using key
  *
  */
  this.verifySignature = function(str, key, signatureValue, callback) {
    var verifier = crypto.createVerify("RSA-SHA1")
    verifier.update(str)
    var res = verifier.verify(key, signatureValue, 'base64')
    if (callback) callback(null, res)
    return res
  }

  this.getAlgorithmName = function() {
    return "http://www.w3.org/2000/09/xmldsig#rsa-sha1"
  }

}


/**
 * Signature algorithm implementation
 *
 */
function RSASHA256() {

  /**
  * Sign the given string using the given key
  *
  */
  this.getSignature = function(signedInfo, signingKey, callback) {
    var signer = crypto.createSign("RSA-SHA256")
    signer.update(signedInfo)
    var res = signer.sign(signingKey, 'base64')
    if (callback) callback(null, res)
    return res
  }

  /**
  * Verify the given signature of the given string using key
  *
  */
  this.verifySignature = function(str, key, signatureValue, callback) {
    var verifier = crypto.createVerify("RSA-SHA256")
    verifier.update(str)
    var res = verifier.verify(key, signatureValue, 'base64')
    if (callback) callback(null, res)
    return res
  }

  this.getAlgorithmName = function() {
    return "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256"
  }

}

/**
 * Signature algorithm implementation
 *
 */
function RSASHA512() {

  /**
  * Sign the given string using the given key
  *
  */
  this.getSignature = function(signedInfo, signingKey, callback) {
    var signer = crypto.createSign("RSA-SHA512")
    signer.update(signedInfo)
    var res = signer.sign(signingKey, 'base64')
    if (callback) callback(null, res)
    return res
  }

  /**
  * Verify the given signature of the given string using key
  *
  */
  this.verifySignature = function(str, key, signatureValue, callback) {
    var verifier = crypto.createVerify("RSA-SHA512")
    verifier.update(str)
    var res = verifier.verify(key, signatureValue, 'base64')
    if (callback) callback(null, res)
    return res
  }

  this.getAlgorithmName = function() {
    return "http://www.w3.org/2001/04/xmldsig-more#rsa-sha512"
  }
}

function HMACSHA1() {
    this.verifySignature = function(str, key, signatureValue) {
        var verifier = crypto.createHmac("SHA1", key);
        verifier.update(str);
        var res = verifier.digest('base64');
        return res === signatureValue;
    };

    this.getAlgorithmName = function() {
        return "http://www.w3.org/2000/09/xmldsig#hmac-sha1";
    };

    this.getSignature = function(signedInfo, signingKey) {
        var verifier = crypto.createHmac("SHA1", signingKey);
        verifier.update(signedInfo);
        var res = verifier.digest('base64');
        return res;
    };
}



/**
 * Extract ancestor namespaces in order to import it to root of document subset
 * which is being canonicalized for non-exclusive c14n.
 *
 * @param {object} doc - Usually a product from `new DOMParser().parseFromString()`
 * @param {string} docSubsetXpath - xpath query to get document subset being canonicalized
 * @param {object} namespaceResolver - xpath namespace resolver
 * @returns {Array} i.e. [{prefix: "saml", namespaceURI: "urn:oasis:names:tc:SAML:2.0:assertion"}]
 */
function findAncestorNs(doc, docSubsetXpath, namespaceResolver){
  var docSubset = xpath.selectWithResolver(docSubsetXpath, doc, namespaceResolver);
  
  if(!Array.isArray(docSubset) || docSubset.length < 1){
    return [];
  }
  
  // Remove duplicate on ancestor namespace
  var ancestorNs = collectAncestorNamespaces(docSubset[0]);
  var ancestorNsWithoutDuplicate = [];
  for(var i=0;i<ancestorNs.length;i++){
    var notOnTheList = true;
    for(var v in ancestorNsWithoutDuplicate){
      if(ancestorNsWithoutDuplicate[v].prefix === ancestorNs[i].prefix){
        notOnTheList = false;
        break;
      }
    }
    
    if(notOnTheList){
      ancestorNsWithoutDuplicate.push(ancestorNs[i]);
    }
  }
  
  // Remove namespaces which are already declared in the subset with the same prefix
  var returningNs = [];
  var subsetAttributes = docSubset[0].attributes;
  for(var j=0;j<ancestorNsWithoutDuplicate.length;j++){
    var isUnique = true;
    for(var k=0;k<subsetAttributes.length;k++){
      var nodeName = subsetAttributes[k].nodeName;
      if(nodeName.search(/^xmlns:/) === -1) continue;
      var prefix = nodeName.replace(/^xmlns:/, "");
      if(ancestorNsWithoutDuplicate[j].prefix === prefix){
        isUnique = false;
        break;
      }
    }
  
    if(isUnique){
      returningNs.push(ancestorNsWithoutDuplicate[j]);
    }
  }
  
  return returningNs;
}



function collectAncestorNamespaces(node, nsArray){
  if(!nsArray){
    nsArray = [];
  }
  
  var parent = node.parentNode;
  
  if(!parent){
    return nsArray;
  }
  
  if(parent.attributes && parent.attributes.length > 0){
    for(var i=0;i<parent.attributes.length;i++){
      var attr = parent.attributes[i];
      if(attr && attr.nodeName && attr.nodeName.search(/^xmlns:/) !== -1){
        nsArray.push({prefix: attr.nodeName.replace(/^xmlns:/, ""), namespaceURI: attr.nodeValue})
      }
    }
  }
  
  return collectAncestorNamespaces(parent, nsArray);
}

/**
* Xml signature implementation
*
* @param {string} idMode. Value of "wssecurity" will create/validate id's with the ws-security namespace
* @param {object} options. Initial configurations
*/
function SignedXml(idMode, options) {
  this.options = options || {};
  this.idMode = idMode
  this.references = []
  this.id = 0
  this.signingKey = null
  this.signatureAlgorithm = this.options.signatureAlgorithm || "http://www.w3.org/2000/09/xmldsig#rsa-sha1";
  this.keyInfoProvider = null
  this.canonicalizationAlgorithm = this.options.canonicalizationAlgorithm || "http://www.w3.org/2001/10/xml-exc-c14n#"
  this.signedXml = ""
  this.signatureXml = ""
  this.signatureNode = null
  this.signatureValue = ""
  this.originalXmlWithIds = ""
  this.validationErrors = []
  this.keyInfo = null
  this.idAttributes = [ 'Id', 'ID', 'id' ];
  if (this.options.idAttribute) this.idAttributes.splice(0, 0, this.options.idAttribute);
  this.implicitTransforms = this.options.implicitTransforms || [];
}

SignedXml.CanonicalizationAlgorithms = {
  'http://www.w3.org/TR/2001/REC-xml-c14n-20010315': c14n.C14nCanonicalization,
  'http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments': c14n.C14nCanonicalizationWithComments,
  'http://www.w3.org/2001/10/xml-exc-c14n#': execC14n.ExclusiveCanonicalization,
  'http://www.w3.org/2001/10/xml-exc-c14n#WithComments': execC14n.ExclusiveCanonicalizationWithComments,
  'http://www.w3.org/2000/09/xmldsig#enveloped-signature': EnvelopedSignature
}

SignedXml.HashAlgorithms = {
  'http://www.w3.org/2000/09/xmldsig#sha1': SHA1,
  'http://www.w3.org/2001/04/xmlenc#sha256': SHA256,
  'http://www.w3.org/2001/04/xmlenc#sha512': SHA512
}

SignedXml.SignatureAlgorithms = {
  'http://www.w3.org/2000/09/xmldsig#rsa-sha1': RSASHA1,
  'http://www.w3.org/2001/04/xmldsig-more#rsa-sha256': RSASHA256,
  'http://www.w3.org/2001/04/xmldsig-more#rsa-sha512': RSASHA512,
  // Disabled by default due to key confusion concerns.
  // 'http://www.w3.org/2000/09/xmldsig#hmac-sha1': HMACSHA1
}

/**
 * Due to key-confusion issues, its risky to have both hmac
 * and digital signature algos enabled at the same time.
 * This enables HMAC and disables other signing algos.
 */
SignedXml.enableHMAC = function () {
  SignedXml.SignatureAlgorithms = {
    'http://www.w3.org/2000/09/xmldsig#hmac-sha1': HMACSHA1
  }
}

SignedXml.defaultNsForPrefix = {
  ds: 'http://www.w3.org/2000/09/xmldsig#'
};

SignedXml.findAncestorNs = findAncestorNs;

SignedXml.prototype.checkSignature = function(xml, callback) {
  if (callback != null && typeof callback !== 'function') {
    throw new Error("Last paramater must be a callback function")
  }

  this.validationErrors = []
  this.signedXml = xml

  if (!this.keyInfoProvider) {
    var err = new Error("cannot validate signature since no key info resolver was provided")
    if (!callback) {
      throw err
    } else {
      callback(err)
      return
    }
  }

  this.signingKey = this.keyInfoProvider.getKey(this.keyInfo)
  if (!this.signingKey) {
    var err = new Error("key info provider could not resolve key info " + this.keyInfo)
    if (!callback) {
      throw err
    } else {
      callback(err)
      return
    }
  }

  var doc = new Dom().parseFromString(xml)

  if (!this.validateReferences(doc)) {
    if (!callback) {
      return false;
    } else {
      callback(new Error('Could not validate references'))
      return
    }
  }

  if (!callback) {
    //Syncronous flow
    if (!this.validateSignatureValue(doc)) {
      return false
    }
    return true
  } else {
    //Asyncronous flow
    this.validateSignatureValue(doc, function (err, isValidSignature) {
      if (err) {
        this.validationErrors.push("invalid signature: the signature value " +
                                        this.signatureValue + " is incorrect")
        callback(err)
      } else {
        callback(null, isValidSignature)
      }
    })
  }
}

SignedXml.prototype.getCanonSignedInfoXml = function(doc) {
  var signedInfo = utils.findChilds(this.signatureNode, "SignedInfo")
  if (signedInfo.length==0) throw new Error("could not find SignedInfo element in the message")
  
  if(this.canonicalizationAlgorithm === "http://www.w3.org/TR/2001/REC-xml-c14n-20010315"
  || this.canonicalizationAlgorithm === "http://www.w3.org/TR/2001/REC-xml-c14n-20010315#WithComments")
  {
    if(!doc || typeof(doc) !== "object"){
      throw new Error(
        "When canonicalization method is non-exclusive, whole xml dom must be provided as an argument"
      );
    }
  }
  
  /**
   * Search for ancestor namespaces before canonicalization.
   */
  var ancestorNamespaces = [];
  ancestorNamespaces = findAncestorNs(doc, "//*[local-name()='SignedInfo']");
  
  var c14nOptions = {
    ancestorNamespaces: ancestorNamespaces
  };
  return this.getCanonXml([this.canonicalizationAlgorithm], signedInfo[0], c14nOptions)
}

SignedXml.prototype.getCanonReferenceXml = function(doc, ref, node) {
  /**
   * Search for ancestor namespaces before canonicalization.
   */
  if(Array.isArray(ref.transforms)){  
    ref.ancestorNamespaces = findAncestorNs(doc, ref.xpath, this.namespaceResolver)
  }

  var c14nOptions = {
    inclusiveNamespacesPrefixList: ref.inclusiveNamespacesPrefixList,
    ancestorNamespaces: ref.ancestorNamespaces
  }

  return this.getCanonXml(ref.transforms, node, c14nOptions)
}

SignedXml.prototype.validateSignatureValue = function(doc, callback) {
  var signedInfoCanon = this.getCanonSignedInfoXml(doc)
  var signer = this.findSignatureAlgorithm(this.signatureAlgorithm)
  var res = signer.verifySignature(signedInfoCanon, this.signingKey, this.signatureValue, callback)
  if (!res && !callback) this.validationErrors.push("invalid signature: the signature value " +
                                        this.signatureValue + " is incorrect")
  return res
}

SignedXml.prototype.calculateSignatureValue = function(doc, callback) {
  var signedInfoCanon = this.getCanonSignedInfoXml(doc)
  var signer = this.findSignatureAlgorithm(this.signatureAlgorithm)
  this.signatureValue = signer.getSignature(signedInfoCanon, this.signingKey, callback)
}

SignedXml.prototype.findSignatureAlgorithm = function(name) {
  var algo = SignedXml.SignatureAlgorithms[name]
  if (algo) return new algo()
  else throw new Error("signature algorithm '" + name + "' is not supported");
}

SignedXml.prototype.findCanonicalizationAlgorithm = function(name) {
  var algo = SignedXml.CanonicalizationAlgorithms[name]
  if (algo) return new algo()
  else throw new Error("canonicalization algorithm '" + name + "' is not supported");
}

SignedXml.prototype.findHashAlgorithm = function(name) {
  var algo = SignedXml.HashAlgorithms[name]
  if (algo) return new algo()
  else throw new Error("hash algorithm '" + name + "' is not supported");
}


SignedXml.prototype.validateReferences = function(doc) {
  for (var r in this.references) {
    if (!this.references.hasOwnProperty(r)) continue;

    var ref = this.references[r]

    var uri = ref.uri[0]=="#" ? ref.uri.substring(1) : ref.uri
    var elem = [];

    if (uri=="") {
      elem = xpath.select("//*", doc)
    }
    else if (uri.indexOf("'") != -1) {
      // xpath injection
      throw new Error("Cannot validate a uri with quotes inside it");
    }
    else {
      var elemXpath;
      var num_elements_for_id = 0;
      for (var index in this.idAttributes) {
        if (!this.idAttributes.hasOwnProperty(index)) continue;
        var tmp_elemXpath = "//*[@*[local-name(.)='" + this.idAttributes[index] + "']='" + uri + "']";
        var tmp_elem = xpath.select(tmp_elemXpath, doc)
        num_elements_for_id += tmp_elem.length;
        if (tmp_elem.length > 0) {
          elem = tmp_elem;
          elemXpath = tmp_elemXpath;
        }
      }
      if (num_elements_for_id > 1) {
          throw new Error('Cannot validate a document which contains multiple elements with the ' +
          'same value for the ID / Id / Id attributes, in order to prevent ' +
          'signature wrapping attack.');
      }

      ref.xpath = elemXpath;
    }

    if (elem.length==0) {
      this.validationErrors.push("invalid signature: the signature refernces an element with uri "+
                        ref.uri + " but could not find such element in the xml")
      return false
    }
  
    var canonXml = this.getCanonReferenceXml(doc, ref, elem[0])

    var hash = this.findHashAlgorithm(ref.digestAlgorithm)
    var digest = hash.getHash(canonXml)

    if (!validateDigestValue(digest, ref.digestValue)) {
      this.validationErrors.push("invalid signature: for uri " + ref.uri +
                                " calculated digest is "  + digest +
                                " but the xml to validate supplies digest " + ref.digestValue)

      return false
    }
  }
  return true
}

function validateDigestValue(digest, expectedDigest) {
  var buffer, expectedBuffer;

  var majorVersion = /^v(\d+)/.exec(process.version)[1];

  if (+majorVersion >= 6) {
    buffer = Buffer.from(digest, 'base64');
    expectedBuffer = Buffer.from(expectedDigest, 'base64');
  } else {
    // Compatibility with Node < 5.10.0
    buffer = new Buffer(digest, 'base64');
    expectedBuffer = new Buffer(expectedDigest, 'base64');
  }

  if (typeof buffer.equals === 'function') {
    return buffer.equals(expectedBuffer);
  }

  // Compatibility with Node < 0.11.13
  if (buffer.length !== expectedBuffer.length) {
    return false;
  }

  for (var i = 0; i < buffer.length; i++) {
    if (buffer[i] !== expectedBuffer[i]) {
      return false;
    }
  }

  return true;
}

SignedXml.prototype.loadSignature = function(signatureNode) {
  if (typeof signatureNode === 'string') {
    this.signatureNode = signatureNode = new Dom().parseFromString(signatureNode);
  } else {
    this.signatureNode = signatureNode;
  }

  this.signatureXml = signatureNode.toString();

  var nodes = xpath.select(".//*[local-name(.)='CanonicalizationMethod']/@Algorithm", signatureNode)
  if (nodes.length==0) throw new Error("could not find CanonicalizationMethod/@Algorithm element")
  this.canonicalizationAlgorithm = nodes[0].value

  this.signatureAlgorithm =
    utils.findFirst(signatureNode, ".//*[local-name(.)='SignatureMethod']/@Algorithm").value

  this.references = []
  var references = xpath.select(".//*[local-name(.)='SignedInfo']/*[local-name(.)='Reference']", signatureNode)
  if (references.length == 0) throw new Error("could not find any Reference elements")

  for (var i in references) {
    if (!references.hasOwnProperty(i)) continue;

    this.loadReference(references[i])
  }

  this.signatureValue =
    utils.findFirst(signatureNode, ".//*[local-name(.)='SignatureValue']/text()").data.replace(/\r?\n/g, '')

  this.keyInfo = xpath.select(".//*[local-name(.)='KeyInfo']", signatureNode)
}

/**
 * Load the reference xml node to a model
 *
 */
SignedXml.prototype.loadReference = function(ref) {
  var nodes = utils.findChilds(ref, "DigestMethod")
  if (nodes.length==0) throw new Error("could not find DigestMethod in reference " + ref.toString())
  var digestAlgoNode = nodes[0]

  var attr = utils.findAttr(digestAlgoNode, "Algorithm")
  if (!attr) throw new Error("could not find Algorithm attribute in node " + digestAlgoNode.toString())
  var digestAlgo = attr.value

  nodes = utils.findChilds(ref, "DigestValue")
  if (nodes.length==0) throw new Error("could not find DigestValue node in reference " + ref.toString())
  if (nodes[0].childNodes.length==0 || !nodes[0].firstChild.data)
  {
    throw new Error("could not find the value of DigestValue in " + nodes[0].toString())
  }
  var digestValue = nodes[0].firstChild.data

  var transforms = []
  var inclusiveNamespacesPrefixList;
  nodes = utils.findChilds(ref, "Transforms")
  if (nodes.length!=0) {
    var transformsNode = nodes[0]
    var transformsAll = utils.findChilds(transformsNode, "Transform")
    for (var t in transformsAll) {
      if (!transformsAll.hasOwnProperty(t)) continue;

      var trans = transformsAll[t]
      transforms.push(utils.findAttr(trans, "Algorithm").value)
    }

    var inclusiveNamespaces = utils.findChilds(trans, "InclusiveNamespaces")
    if (inclusiveNamespaces.length > 0) {
      //Should really only be one prefix list, but maybe there's some circumstances where more than one to lets handle it
      for (var i = 0; i<inclusiveNamespaces.length; i++) {
        if (inclusiveNamespacesPrefixList) {
          inclusiveNamespacesPrefixList = inclusiveNamespacesPrefixList + " " + inclusiveNamespaces[i].getAttribute('PrefixList');
        } else {
          inclusiveNamespacesPrefixList = inclusiveNamespaces[i].getAttribute('PrefixList');
        }
      }
    }
  }

  var hasImplicitTransforms = (Array.isArray(this.implicitTransforms) && this.implicitTransforms.length > 0);
  if(hasImplicitTransforms){
    this.implicitTransforms.forEach(function(t){
      transforms.push(t);
    });
  }
  
/**
 * DigestMethods take an octet stream rather than a node set. If the output of the last transform is a node set, we
 * need to canonicalize the node set to an octet stream using non-exclusive canonicalization. If there are no
 * transforms, we need to canonicalize because URI dereferencing for a same-document reference will return a node-set.
 * See:
 * https://www.w3.org/TR/xmldsig-core1/#sec-DigestMethod
 * https://www.w3.org/TR/xmldsig-core1/#sec-ReferenceProcessingModel
 * https://www.w3.org/TR/xmldsig-core1/#sec-Same-Document
 */
  if (transforms.length === 0 || transforms[transforms.length - 1] === "http://www.w3.org/2000/09/xmldsig#enveloped-signature") {
      transforms.push("http://www.w3.org/TR/2001/REC-xml-c14n-20010315")
  }

  this.addReference(null, transforms, digestAlgo, utils.findAttr(ref, "URI").value, digestValue, inclusiveNamespacesPrefixList, false)
}

SignedXml.prototype.addReference = function(xpath, transforms, digestAlgorithm, uri, digestValue, inclusiveNamespacesPrefixList, isEmptyUri) {
  this.references.push({
    "xpath": xpath,
    "transforms": transforms ? transforms : ["http://www.w3.org/2001/10/xml-exc-c14n#"] ,
    "digestAlgorithm": digestAlgorithm ? digestAlgorithm : "http://www.w3.org/2000/09/xmldsig#sha1",
    "uri": uri,
    "digestValue": digestValue,
    "inclusiveNamespacesPrefixList": inclusiveNamespacesPrefixList,
    "isEmptyUri": isEmptyUri
  });
}

/**
 * Compute the signature of the given xml (usign the already defined settings)
 *
 * Options:
 *
 * - `prefix` {String} Adds a prefix for the generated signature tags
 * - `attrs` {Object} A hash of attributes and values `attrName: value` to add to the signature root node
 * - `location` {{ reference: String, action: String }}
 * - `existingPrefixes` {Object} A hash of prefixes and namespaces `prefix: namespace` already in the xml
 *   An object with a `reference` key which should
 *   contain a XPath expression, an `action` key which
 *   should contain one of the following values:
 *   `append`, `prepend`, `before`, `after`
 *
 */
SignedXml.prototype.computeSignature = function(xml, opts, callback) {
  if (typeof opts === 'function' && callback == null) {
    callback = opts
  }

  if (callback != null && typeof callback !== 'function') {
    throw new Error("Last paramater must be a callback function")
  }

  var doc = new Dom().parseFromString(xml),
      xmlNsAttr = "xmlns",
      signatureAttrs = [],
      location,
      attrs,
      prefix,
      currentPrefix;

  var validActions = ["append", "prepend", "before", "after"];

  opts = opts || {};
  prefix = opts.prefix;
  attrs = opts.attrs || {};
  location = opts.location || {};
  var existingPrefixes = opts.existingPrefixes || {};

  this.namespaceResolver = {
    lookupNamespaceURI: function(prefix) {       
      return existingPrefixes[prefix];
    }
  }

  // defaults to the root node
  location.reference = location.reference || "/*";
  // defaults to append action
  location.action = location.action || "append";

  if (validActions.indexOf(location.action) === -1) {
    var err = new Error("location.action option has an invalid action: " + location.action +
      ", must be any of the following values: " + validActions.join(", "));
    if (!callback) {
      throw err;
    } else {
      callback(err, null)
      return
    }
  }

  // automatic insertion of `:`
  if (prefix) {
    xmlNsAttr += ":" + prefix;
    currentPrefix = prefix + ":";
  } else {
    currentPrefix = "";
  }

  Object.keys(attrs).forEach(function(name) {
    if (name !== "xmlns" && name !== xmlNsAttr) {
      signatureAttrs.push(name + "=\"" + attrs[name] + "\"");
    }
  });

  // add the xml namespace attribute
  signatureAttrs.push(xmlNsAttr + "=\"http://www.w3.org/2000/09/xmldsig#\"");

  var signatureXml = "<" + currentPrefix + "Signature " + signatureAttrs.join(" ") + ">"

  signatureXml += this.createSignedInfo(doc, prefix);
  signatureXml += this.getKeyInfo(prefix)
  signatureXml += "</" + currentPrefix + "Signature>"

  this.originalXmlWithIds = doc.toString()

  var existingPrefixesString = ""
  Object.keys(existingPrefixes).forEach(function(key) {
    existingPrefixesString += "xmlns:" + key + '="' + existingPrefixes[key] + '" '
  });

  // A trick to remove the namespaces that already exist in the xml
  // This only works if the prefix and namespace match with those in te xml
  var dummySignatureWrapper = "<Dummy " + existingPrefixesString + ">" + signatureXml + "</Dummy>"
  var xml = new Dom().parseFromString(dummySignatureWrapper)
  var signatureDoc = xml.documentElement.firstChild;

  var referenceNode = xpath.select(location.reference, doc);

  if (!referenceNode || referenceNode.length === 0) {
    var err = new Error("the following xpath cannot be used because it was not found: " + location.reference);
    if (!callback) {
      throw err
    } else {
      callback(err, null)
      return
    }
  }

  referenceNode = referenceNode[0];

  if (location.action === "append") {
    referenceNode.appendChild(signatureDoc);
  } else if (location.action === "prepend") {
    referenceNode.insertBefore(signatureDoc, referenceNode.firstChild);
  } else if (location.action === "before") {
    referenceNode.parentNode.insertBefore(signatureDoc, referenceNode);
  } else if (location.action === "after") {
    referenceNode.parentNode.insertBefore(signatureDoc, referenceNode.nextSibling);
  }

  this.signatureNode = signatureDoc
  var signedInfoNode = utils.findChilds(this.signatureNode, "SignedInfo")
  if (signedInfoNode.length == 0) {
    var err = new Error("could not find SignedInfo element in the message")
    if (!callback) {
      throw err
    } else {
      callback(err)
      return
    }
  }
  signedInfoNode = signedInfoNode[0];

  if (!callback) {
    //Synchronous flow
    this.calculateSignatureValue(doc)
    signatureDoc.insertBefore(this.createSignature(prefix), signedInfoNode.nextSibling)
    this.signatureXml = signatureDoc.toString()
    this.signedXml = doc.toString()
  } else {
    var self = this
    //Asynchronous flow
    this.calculateSignatureValue(doc, function(err, signature) {
      if (err) {
        callback(err)
      } else {
        self.signatureValue = signature
        signatureDoc.insertBefore(self.createSignature(prefix), signedInfoNode.nextSibling)
        self.signatureXml = signatureDoc.toString()
        self.signedXml = doc.toString()
        callback(null, self)
      }
    })
  }
}

SignedXml.prototype.getKeyInfo = function(prefix) {
  var res = ""
  var currentPrefix

  currentPrefix = prefix || ''
  currentPrefix = currentPrefix ? currentPrefix + ':' : currentPrefix

  if (this.keyInfoProvider) {
    res += "<" + currentPrefix + "KeyInfo>"
    res += this.keyInfoProvider.getKeyInfo(this.signingKey, prefix)
    res += "</" + currentPrefix + "KeyInfo>"
  }
  return res
}

/**
 * Generate the Reference nodes (as part of the signature process)
 *
 */
SignedXml.prototype.createReferences = function(doc, prefix) {

  var res = ""

  prefix = prefix || ''
  prefix = prefix ? prefix + ':' : prefix

  for (var n in this.references) {
    if (!this.references.hasOwnProperty(n)) continue;

    var ref = this.references[n]
      , nodes = xpath.selectWithResolver(ref.xpath, doc, this.namespaceResolver)

    if (nodes.length==0) {
      throw new Error('the following xpath cannot be signed because it was not found: ' + ref.xpath)
    }

    for (var h in nodes) {
      if (!nodes.hasOwnProperty(h)) continue;

      var node = nodes[h]
      if (ref.isEmptyUri) {
        res += "<" + prefix + "Reference URI=\"\">"
      }
      else {
        var id = this.ensureHasId(node);
        ref.uri = id
        res += "<" + prefix + "Reference URI=\"#" + id + "\">"
      }
      res += "<" + prefix + "Transforms>"
      for (var t in ref.transforms) {
        if (!ref.transforms.hasOwnProperty(t)) continue;

        var trans = ref.transforms[t]
        var transform = this.findCanonicalizationAlgorithm(trans)
        res += "<" + prefix + "Transform Algorithm=\"" + transform.getAlgorithmName() + "\" />"
      }

      var canonXml = this.getCanonReferenceXml(doc, ref, node)

      var digestAlgorithm = this.findHashAlgorithm(ref.digestAlgorithm)
      res += "</" + prefix + "Transforms>"+
             "<" + prefix + "DigestMethod Algorithm=\"" + digestAlgorithm.getAlgorithmName() + "\" />"+
              "<" + prefix + "DigestValue>" + digestAlgorithm.getHash(canonXml) + "</" + prefix + "DigestValue>"+
              "</" + prefix + "Reference>"
    }
  }

  return res
}

SignedXml.prototype.getCanonXml = function(transforms, node, options) {
  options = options || {};
  options.defaultNsForPrefix = options.defaultNsForPrefix || SignedXml.defaultNsForPrefix;
  options.signatureNode = this.signatureNode;

  var canonXml = node.cloneNode(true) // Deep clone

  for (var t in transforms) {
    if (!transforms.hasOwnProperty(t)) continue;

    var transform = this.findCanonicalizationAlgorithm(transforms[t])
    canonXml = transform.process(canonXml, options);
    //TODO: currently transform.process may return either Node or String value (enveloped transformation returns Node, exclusive-canonicalization returns String).
    //This eitehr needs to be more explicit in the API, or all should return the same.
    //exclusive-canonicalization returns String since it builds the Xml by hand. If it had used xmldom it would inccorectly minimize empty tags
    //to <x/> instead of <x></x> and also incorrectly handle some delicate line break issues.
    //enveloped transformation returns Node since if it would return String consider this case:
    //<x xmlns:p='ns'><p:y/></x>
    //if only y is the node to sign then a string would be <p:y/> without the definition of the p namespace. probably xmldom toString() should have added it.
  }
  return canonXml.toString()
}

/**
 * Ensure an element has Id attribute. If not create it with unique value.
 * Work with both normal and wssecurity Id flavour
 */
SignedXml.prototype.ensureHasId = function(node) {
  var attr

  if (this.idMode=="wssecurity") {
    attr = utils.findAttr(node,
      "Id",
      "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd")
  }
  else {
    for (var index in this.idAttributes) {
      if (!this.idAttributes.hasOwnProperty(index)) continue;

      attr = utils.findAttr(node, this.idAttributes[index], null);
      if (attr) break;
    }
  }

  if (attr) return attr.value

  //add the attribute
  var id = "_" + this.id++

  if (this.idMode=="wssecurity") {
    node.setAttributeNS("http://www.w3.org/2000/xmlns/",
      "xmlns:wsu",
      "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd")
    node.setAttributeNS("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
      "wsu:Id",
      id)
  }
  else {
   node.setAttribute("Id", id)
  }

  return id
}

/**
 * Create the SignedInfo element
 *
 */
SignedXml.prototype.createSignedInfo = function(doc, prefix) {
  var transform = this.findCanonicalizationAlgorithm(this.canonicalizationAlgorithm)
  var algo = this.findSignatureAlgorithm(this.signatureAlgorithm)
  var currentPrefix

  currentPrefix = prefix || ''
  currentPrefix = currentPrefix ? currentPrefix + ':' : currentPrefix

  var res = "<" + currentPrefix + "SignedInfo>"
  res += "<" + currentPrefix + "CanonicalizationMethod Algorithm=\"" + transform.getAlgorithmName() + "\" />" +
          "<" + currentPrefix + "SignatureMethod Algorithm=\"" + algo.getAlgorithmName() + "\" />"

  res += this.createReferences(doc, prefix)
  res += "</" + currentPrefix + "SignedInfo>"
  return res
}

/**
 * Create the Signature element
 *
 */
SignedXml.prototype.createSignature = function(prefix) {
  var xmlNsAttr = 'xmlns'

  if (prefix) {
    xmlNsAttr += ':' + prefix;
    prefix += ':';
  } else {
    prefix = '';
  }

  var signatureValueXml = "<" + prefix + "SignatureValue>" + this.signatureValue + "</" + prefix + "SignatureValue>"
  //the canonicalization requires to get a valid xml node.
  //we need to wrap the info in a dummy signature since it contains the default namespace.
  var dummySignatureWrapper = "<" + prefix + "Signature " + xmlNsAttr + "=\"http://www.w3.org/2000/09/xmldsig#\">" +
                              signatureValueXml +
                              "</" + prefix + "Signature>"

  var doc = new Dom().parseFromString(dummySignatureWrapper)
  return doc.documentElement.firstChild;
}


SignedXml.prototype.getSignatureXml = function() {
  return this.signatureXml
}

SignedXml.prototype.getOriginalXmlWithIds = function() {
  return this.originalXmlWithIds
}

SignedXml.prototype.getSignedXml = function() {
  return this.signedXml
}
