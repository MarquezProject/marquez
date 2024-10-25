"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.signXmlResponse = exports.assertRequired = void 0;
const xml_1 = require("./xml");
function assertRequired(value, error) {
    if (value === undefined || value === null || (typeof value === "string" && value.length === 0)) {
        throw new TypeError(error !== null && error !== void 0 ? error : "value does not exist");
    }
    else {
        return value;
    }
}
exports.assertRequired = assertRequired;
function signXmlResponse(samlMessage, options) {
    const responseXpath = '//*[local-name(.)="Response" and namespace-uri(.)="urn:oasis:names:tc:SAML:2.0:protocol"]';
    return (0, xml_1.signXml)(samlMessage, responseXpath, { reference: responseXpath, action: "append" }, options);
}
exports.signXmlResponse = signXmlResponse;
//# sourceMappingURL=utility.js.map