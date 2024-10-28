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
        this.cacheKeys = {};
        if (!options) {
            options = {};
        }
        if (!options.keyExpirationPeriodMs) {
            options.keyExpirationPeriodMs = 28800000; // 8 hours
        }
        this.options = options;
        // Expire old cache keys
        const expirationTimer = setInterval(() => {
            const nowMs = new Date().getTime();
            const keys = Object.keys(this.cacheKeys);
            keys.forEach((key) => {
                if (nowMs >= new Date(this.cacheKeys[key].createdAt).getTime() + this.options.keyExpirationPeriodMs) {
                    this.remove(key, () => undefined);
                }
            });
        }, this.options.keyExpirationPeriodMs);
        // we only want this to run if the process is still open; it shouldn't hold the process open (issue #68)
        //   (unref only introduced in node 0.9, so check whether we have it)
        // Skip this in 0.10.34 due to https://github.com/joyent/node/issues/8900
        if (expirationTimer.unref && process.version !== 'v0.10.34')
            expirationTimer.unref();
    }
    /**
     * Store an item in the cache, using the specified key and value.
     * Internally will keep track of the time the item was added to the cache
     * @param id
     * @param value
     */
    save(key, value, callback) {
        if (!this.cacheKeys[key]) {
            this.cacheKeys[key] = {
                createdAt: new Date().getTime(),
                value: value
            };
            callback(null, this.cacheKeys[key]);
        }
        else {
            callback(null, null);
        }
    }
    /**
     * Returns the value of the specified key in the cache
     * @param id
     * @returns {boolean}
     */
    get(key, callback) {
        if (this.cacheKeys[key]) {
            callback(null, this.cacheKeys[key].value);
        }
        else {
            callback(null, null);
        }
    }
    /**
     * Removes an item from the cache if it exists
     * @param key
     */
    remove(key, callback) {
        if (this.cacheKeys[key]) {
            delete this.cacheKeys[key];
            callback(null, key);
        }
        else {
            callback(null, null);
        }
    }
}
exports.CacheProvider = CacheProvider;
//# sourceMappingURL=inmemory-cache-provider.js.map