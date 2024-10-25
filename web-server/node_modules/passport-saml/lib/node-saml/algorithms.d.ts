/// <reference types="node" />
import * as crypto from "crypto";
export declare function getSigningAlgorithm(shortName?: string): string;
export declare function getDigestAlgorithm(shortName?: string): string;
export declare function getSigner(shortName?: string): crypto.Signer;
