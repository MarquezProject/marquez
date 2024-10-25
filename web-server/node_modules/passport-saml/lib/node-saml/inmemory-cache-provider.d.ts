/**
 * Simple in memory cache provider.  To be used to store state of requests that needs
 * to be validated/checked when a response is received.
 *
 * This is the default implementation of a cache provider used by Passport-SAML.  For
 * multiple server instances/load balanced scenarios (I.e. the SAML request could have
 * been generated from a different server/process handling the SAML response) this
 * implementation will NOT be sufficient.
 *
 * The caller should provide their own implementation for a cache provider as defined
 * in the config options for Passport-SAML.
 * @param options
 * @constructor
 */
export interface CacheItem {
    value: string;
    createdAt: number;
}
interface CacheProviderOptions {
    keyExpirationPeriodMs: number;
}
export declare class CacheProvider {
    cacheKeys: Record<string, CacheItem>;
    options: CacheProviderOptions;
    constructor(options: Partial<CacheProviderOptions>);
    /**
     * Store an item in the cache, using the specified key and value.
     * Internally will keep track of the time the item was added to the cache
     * @param id
     * @param value
     */
    saveAsync(key: string, value: string): Promise<CacheItem | null>;
    /**
     * Returns the value of the specified key in the cache
     * @param id
     * @returns {boolean}
     */
    getAsync(key: string): Promise<string | null>;
    /**
     * Removes an item from the cache if it exists
     * @param key
     */
    removeAsync(key: string): Promise<string | null>;
}
export {};
