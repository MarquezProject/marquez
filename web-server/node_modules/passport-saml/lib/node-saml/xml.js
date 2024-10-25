"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.buildXmlBuilderObject = exports.buildXml2JsObject = exports.parseXml2JsFromString = exports.parseDomFromString = exports.signXml = exports.validateXmlSignatureForCert = exports.decryptXml = exports.xpath = void 0;
const util = require("util");
const xmlCrypto = require("xml-crypto");
const xmlenc = require("xml-encryption");
const xmldom = require("@xmldom/xmldom");
const xml2js = require("xml2js");
const xmlbuilder = require("xmlbuilder");
const types_1 = require("./types");
const algorithms = require("./algorithms");
const selectXPath = (guard, node, xpath) => {
    const result = xmlCrypto.xpath(node, xpath);
    if (!guard(result)) {
        throw new Error("invalid xpath return type");
    }
    return result;
};
const attributesXPathTypeGuard = (values) => {
    return values.every((value) => {
        if (typeof value != "object") {
            return false;
        }
        return typeof value.nodeType === "number" && value.nodeType === value.ATTRIBUTE_NODE;
    });
};
const elementsXPathTypeGuard = (values) => {
    return values.every((value) => {
        if (typeof value != "object") {
            return false;
        }
        return typeof value.nodeType === "number" && value.nodeType === value.ELEMENT_NODE;
    });
};
exports.xpath = {
    selectAttributes: (node, xpath) => selectXPath(attributesXPathTypeGuard, node, xpath),
    selectElements: (node, xpath) => selectXPath(elementsXPathTypeGuard, node, xpath),
};
const decryptXml = async (xml, decryptionKey) => util.promisify(xmlenc.decrypt).bind(xmlenc)(xml, { key: decryptionKey });
exports.decryptXml = decryptXml;
const normalizeNewlines = (xml) => {
    // we can use this utility before passing XML to `xml-crypto`
    // we are considered the XML processor and are responsible for newline normalization
    // https://github.com/node-saml/passport-saml/issues/431#issuecomment-718132752
    return xml.replace(/\r\n?/g, "\n");
};
const normalizeXml = (xml) => {
    // we can use this utility to parse and re-stringify XML
    // `DOMParser` will take care of normalization tasks, like replacing XML-encoded carriage returns with actual carriage returns
    return (0, exports.parseDomFromString)(xml).toString();
};
/**
 * This function checks that the |signature| is signed with a given |cert|.
 */
const validateXmlSignatureForCert = (signature, certPem, fullXml, currentNode) => {
    const sig = new xmlCrypto.SignedXml();
    sig.keyInfoProvider = {
        file: "",
        getKeyInfo: () => "<X509Data></X509Data>",
        getKey: () => Buffer.from(certPem),
    };
    const signatureStr = normalizeNewlines(signature.toString());
    sig.loadSignature(signatureStr);
    // We expect each signature to contain exactly one reference to the top level of the xml we
    //   are validating, so if we see anything else, reject.
    if (sig.references.length != 1)
        return false;
    const refUri = sig.references[0].uri;
    const refId = refUri[0] === "#" ? refUri.substring(1) : refUri;
    // If we can't find the reference at the top level, reject
    const idAttribute = currentNode.getAttribute("ID") ? "ID" : "Id";
    if (currentNode.getAttribute(idAttribute) != refId)
        return false;
    // If we find any extra referenced nodes, reject.  (xml-crypto only verifies one digest, so
    //   multiple candidate references is bad news)
    const totalReferencedNodes = exports.xpath.selectElements(currentNode.ownerDocument, "//*[@" + idAttribute + "='" + refId + "']");
    if (totalReferencedNodes.length > 1) {
        return false;
    }
    // normalize XML to replace XML-encoded carriage returns with actual carriage returns
    fullXml = normalizeXml(fullXml);
    fullXml = normalizeNewlines(fullXml);
    return sig.checkSignature(fullXml);
};
exports.validateXmlSignatureForCert = validateXmlSignatureForCert;
const signXml = (xml, xpath, location, options) => {
    var _a;
    const defaultTransforms = [
        "http://www.w3.org/2000/09/xmldsig#enveloped-signature",
        "http://www.w3.org/2001/10/xml-exc-c14n#",
    ];
    if (!xml)
        throw new Error("samlMessage is required");
    if (!location)
        throw new Error("location is required");
    if (!options)
        throw new Error("options is required");
    if (!(0, types_1.isValidSamlSigningOptions)(options))
        throw new Error("options.privateKey is required");
    const transforms = (_a = options.xmlSignatureTransforms) !== null && _a !== void 0 ? _a : defaultTransforms;
    const sig = new xmlCrypto.SignedXml();
    if (options.signatureAlgorithm != null) {
        sig.signatureAlgorithm = algorithms.getSigningAlgorithm(options.signatureAlgorithm);
    }
    sig.addReference(xpath, transforms, algorithms.getDigestAlgorithm(options.digestAlgorithm));
    sig.signingKey = options.privateKey;
    sig.computeSignature(xml, {
        location,
    });
    return sig.getSignedXml();
};
exports.signXml = signXml;
const parseDomFromString = (xml) => {
    return new xmldom.DOMParser().parseFromString(xml);
};
exports.parseDomFromString = parseDomFromString;
const parseXml2JsFromString = async (xml) => {
    const parserConfig = {
        explicitRoot: true,
        explicitCharkey: true,
        tagNameProcessors: [xml2js.processors.stripPrefix],
    };
    const parser = new xml2js.Parser(parserConfig);
    return parser.parseStringPromise(xml);
};
exports.parseXml2JsFromString = parseXml2JsFromString;
const buildXml2JsObject = (rootName, xml) => {
    const builderOpts = {
        rootName,
        headless: true,
    };
    return new xml2js.Builder(builderOpts).buildObject(xml);
};
exports.buildXml2JsObject = buildXml2JsObject;
const buildXmlBuilderObject = (xml, pretty) => {
    const options = pretty ? { pretty: true, indent: "  ", newline: "\n" } : {};
    return xmlbuilder.create(xml).end(options);
};
exports.buildXmlBuilderObject = buildXmlBuilderObject;
//# sourceMappingURL=xml.js.map