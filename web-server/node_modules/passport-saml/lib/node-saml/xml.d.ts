/// <reference types="node" />
import { SamlSigningOptions } from "./types";
export declare const xpath: {
    selectAttributes: (node: Node, xpath: string) => Attr[];
    selectElements: (node: Node, xpath: string) => Element[];
};
export declare const decryptXml: (xml: string, decryptionKey: string | Buffer) => Promise<string>;
/**
 * This function checks that the |signature| is signed with a given |cert|.
 */
export declare const validateXmlSignatureForCert: (signature: Node, certPem: string, fullXml: string, currentNode: Element) => boolean;
interface XmlSignatureLocation {
    reference: string;
    action: "append" | "prepend" | "before" | "after";
}
export declare const signXml: (xml: string, xpath: string, location: XmlSignatureLocation, options: SamlSigningOptions) => string;
export declare const parseDomFromString: (xml: string) => Document;
export declare const parseXml2JsFromString: (xml: string | Buffer) => Promise<any>;
export declare const buildXml2JsObject: (rootName: string, xml: any) => string;
export declare const buildXmlBuilderObject: (xml: Record<string, any>, pretty: boolean) => string;
export {};
