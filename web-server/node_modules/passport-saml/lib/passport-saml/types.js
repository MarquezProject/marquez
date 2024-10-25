"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.ErrorWithXmlStatus = void 0;
class ErrorWithXmlStatus extends Error {
    constructor(message, xmlStatus) {
        super(message);
        this.xmlStatus = xmlStatus;
    }
}
exports.ErrorWithXmlStatus = ErrorWithXmlStatus;
//# sourceMappingURL=types.js.map