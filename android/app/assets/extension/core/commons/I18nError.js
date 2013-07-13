/**
 * Define an internationalization-friendly error.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */

define(['core/commons/ErrorCode'], function(ErrorCode) {
    'use strict';

    /**
     * Create an internationalization-friendly error.
     *
     * @param {{code: String, i18nArgs: Array.<String>}} options
     * @constructor
     */
    function I18nError(options) {
        /** @type {String} */
        this.code = options.code || ErrorCode.UNKNOWN_ERROR;
        /** @type {Array.<String>} */
        this.i18nArgs = options.i18nArgs || [];
    }

    I18nError.prototype = new Error();
    I18nError.prototype.constructor = I18nError;

    /**
     * @return {string} Error information.
     */
    I18nError.prototype.toString = function() {
        return this.code + '[' + this.i18nArgs.join(',') + ']';
    };

    return I18nError;
});
