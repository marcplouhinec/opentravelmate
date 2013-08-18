/**
 * Compute the Mercator projection between the world coordinates and the screen coordinates.
 * 
 * @author Marc Plouhinec
 */
define(function() {
    var projectionUtils = {
        /**
         * Get the X coordinate of a waypoint (the reference is 1 = tile size).
         *
         * @param {Number} zoom Map zoom level
         * @param {Number} longitude
         * @return {Number} X coordinate
         */
        'lngToTileX': function (zoom, longitude) {
            return Math.pow(2, zoom) * (longitude + 180) / 360;
        },

        /**
         * Get the Y coordinate of a waypoint (the reference is 1 = tile size).
         *
         * @param {Number} zoom Map zoom level
         * @param {Number} latitude
         * @return {Number} Y coordinate
         */
        'latToTileY': function (zoom, latitude) {
            return Math.pow(2, zoom - 1) * (1 - Math.log(Math.tan(Math.PI / 4 + latitude * Math.PI / 360)) / Math.PI);
        },

        /**
         * Get the longitude of a waypoint from its X coordinate.
         *
         * @param {Number} zoom Map zoom level
         * @param {Number} x
         * @return {Number} longitude
         */
        'tileXToLng': function (zoom, x) {
            return x * 360 / Math.pow(2, zoom) - 180;
        },

        /**
         * Get the latitude of a waypoint from its Y coordinate.
         *
         * @param {Number} zoom Map zoom level
         * @param {Number} y
         * @return {Number} latitude
         */
        'tileYToLat': function (zoom, y) {
            return Math.atan(Math.exp(Math.PI * (1 - (2 * y / Math.pow(2, zoom))))) * 360 / Math.PI - Math.PI * 360 / (4 * Math.PI);
        }
    };

    return projectionUtils;
});
