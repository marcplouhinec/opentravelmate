/**
 * Define a collection of markers.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */

define(['./projectionUtils'], function(projectionUtils) {
    'use strict';

    /**
     * @const
     * @type {{width: number, height: number}}
     */
    var DEFAULT_MARKER_SIZE = {
        width: 22,
        height: 40
    };

    /**
     * @const
     * @type {{x: number, y: number}}
     */
    var DEFAULT_MARKER_ANCHOR = {
        x: 11,
        y: 40
    };

    /**
     * @const
     * @type {number}
     */
    var MIN_ZOOM = 2;

    /**
     * @const
     * @type {number}
     */
    var MAX_ZOOM = 21;

    /**
     * Create a new MarkerRTree.
     *
     * @constructor
     */
    function MarkerRTree() {
        /**
         * Contains all the markers grouped by tiles.
         *
         * @type {Object.<Number, Object.<String, Array>>}
         * @private
         */
        this._markersByTileIdByZoom = {};
    };

    /**
     * Add a marker in the collection.
     *
     * @param marker
     */
    MarkerRTree.prototype.addMarker = function(marker) {
        var markerSize = marker.icon ? marker.icon.size : DEFAULT_MARKER_SIZE;
        var markerAnchor = marker.icon ? marker.icon.anchor : DEFAULT_MARKER_ANCHOR;

        // For each zoom level, group the marker per tile
        for (var zoom = MIN_ZOOM; zoom <= MAX_ZOOM; zoom += 1) {
            var markerPositionXY = {
                x: projectionUtils.lngToTileX(2, marker.position.lng),
                y: projectionUtils.latToTileY(2, marker.position.lat)
            };

            var markersByTileId = this._markersByTileIdByZoom[zoom];
            if (!markersByTileId) {
                markersByTileId = {};
                this._markersByTileIdByZoom[zoom] = markersByTileId;
            }

            var markerCornerPositions = [
                {
                    x: markerPositionXY.x - markerAnchor.x / 256,
                    y: markerPositionXY.y - markerAnchor.y / 256
                }, {
                    x: markerPositionXY.x - (markerAnchor.x + markerSize.width) / 256,
                    y: markerPositionXY.y - markerAnchor.y / 256
                }, {
                    x: markerPositionXY.x - (markerAnchor.x + markerSize.width) / 256,
                    y: markerPositionXY.y - (markerAnchor.y + markerSize.height) / 256
                }, {
                    x: markerPositionXY.x - markerAnchor.x / 256,
                    y: markerPositionXY.y - (markerAnchor.y + markerSize.height) / 256
                }
            ];

            for (var i = 0; i < 4; i += 1) {
                var tileId = zoom + '_' + projectionUtils.tileYToLat(zoom, markerCornerPositions.y) + '_' + projectionUtils.tileXToLng(zoom, markerCornerPositions.x);
                var markers = markersByTileId[tileId];
                if (!markers) {
                    markers = [];
                    markersByTileId[tileId] = markers;
                }
                markers.push(marker);
            }
        }
    };

    /**
     * Remove a marker from the collection.
     *
     * @param marker
     */
    MarkerRTree.prototype.removeMarker = function(marker) {
        // TODO
    };

    /**
     * Get the nearest marker to the given point.
     *
     * @param {Number} zoom
     * @param {Number} x
     * @param {Number} y
     * @return {{marker: Object, distance: Number}} Marker and distance in pixels
     */
    MarkerRTree.prototype.getNearestMarkerAndDistance = function(zoom, x, y) {
        return null; // TODO
    };

    return MarkerRTree;
});
