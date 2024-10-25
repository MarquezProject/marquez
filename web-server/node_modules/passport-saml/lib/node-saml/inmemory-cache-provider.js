"use strict";
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
Object.defineProperty(exports, "__esModule", { value: true });
exports.CacheProvider = void 0;
class CacheProvider {
    constructor(options) {
        var _a;
        this.cacheKeys = {};
        this.options = {
            ...options,
            keyExpirationPeriodMs: (_a = options === null || options === void 0 ? void 0 : options.keyExpirationPeriodMs) !== null && _a !== void 0 ? _a : 28800000, // 8 hours,
        };
        // Expire old cache keys
        const expirationTimer = setInterval(() => {
            const nowMs = new Date().getTime();
            const keys = Object.keys(this.cacheKeys);
            keys.forEach((key) => {
                if (nowMs >=
                    new Date(this.cacheKeys[key].createdAt).getTime() + this.options.keyExpirationPeriodMs) {
                    this.removeAsync(key);
                }
            });
        }, this.options.keyExpirationPeriodMs);
        // we only want this to run if the process is still open; it shouldn't hold the process open (issue #68)
        expirationTimer.unref();
    }
    /**
     * Store an item in the cache, using the specified key and value.
     * Internally will keep track of the time the item was added to the cache
     * @param id
     * @param value
     */
    async saveAsync(key, value) {
        if (!this.cacheKeys[key]) {
            this.cacheKeys[key] = {
                createdAt: new Date().getTime(),
                value: value,
            };
            return this.cacheKeys[key];
        }
        else {
            return null;
        }
    }
    /**
     * Returns the value of the specified key in the cache
     * @param id
     * @returns {boolean}
     */
    async getAsync(key) {
        if (this.cacheKeys[key]) {
            return this.cacheKeys[key].value;
        }
        else {
            return null;
        }
    }
    /**
     * Removes an item from the cache if it exists
     * @param key
     */
    async removeAsync(key) {
        if (this.cacheKeys[key]) {
            delete this.cacheKeys[key];
            return key;
        }
        else {
            return null;
        }
    }
}
exports.CacheProvider = CacheProvider;
//# sourceMappingURL=inmemory-cache-provider.js.map