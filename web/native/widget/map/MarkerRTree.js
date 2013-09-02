/**
 * Define a collection of markers.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */

define(['underscore', './../../../extensions/core/widget/map/projectionUtils'], function(_, projectionUtils) {
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
         * @type {Object.<Number, Array<{marker: Object, x: Number, y: Number}>>}
         * @private
         */
        this._markersByTileId = {};

        /**
         * Contains all the tiles grouped by marker.
         *
         * @type {Object.<String, Array.<String>>}
         * @private
         */
        this._tileIdsByMarkerId = {};
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
                x: projectionUtils.lngToTileX(zoom, marker.position.lng),
                y: projectionUtils.latToTileY(zoom, marker.position.lat)
            };

            var markerCornerPositions = [
                {
                    x: markerPositionXY.x - markerAnchor.x / 256,
                    y: markerPositionXY.y - markerAnchor.y / 256
                }, {
                    x: markerPositionXY.x - markerAnchor.x / 256 + markerSize.width / 256,
                    y: markerPositionXY.y - markerAnchor.y / 256
                }, {
                    x: markerPositionXY.x - markerAnchor.x / 256 + markerSize.width / 256,
                    y: markerPositionXY.y - markerAnchor.y / 256 + markerSize.height / 256
                }, {
                    x: markerPositionXY.x - markerAnchor.x / 256,
                    y: markerPositionXY.y - markerAnchor.y / 256 + markerSize.height / 256
                }
            ];

            /** @type {Object.<String, Boolean>} */
            var tileIdSet = {};
            for (var i = 0; i < 4; i += 1) {
                var tileId = [zoom, Math.floor(markerCornerPositions[i].x), Math.floor(markerCornerPositions[i].y) ].join('_');
                tileIdSet[tileId] = true;
            }

            var markerWithCoord = {
                marker: marker,
                x: markerPositionXY.x,
                y: markerPositionXY.y
            };
            for (var tileId in tileIdSet) {
                // Add the marker to this._markersByTileId
                var markerWithCoords = this._markersByTileId[tileId];
                if (!markerWithCoords) {
                    markerWithCoords = [];
                    this._markersByTileId[tileId] = markerWithCoords;
                }
                markerWithCoords.push(markerWithCoord);

                // Add the tile to this._tileIdsByMarkerId
                var tileIds = this._tileIdsByMarkerId[String(marker.id)];
                if (!tileIds) {
                    tileIds = [];
                    this._tileIdsByMarkerId[String(marker.id)] = tileIds;
                }
                tileIds.push(tileId);
            }
        }
    };

    /**
     * Remove a marker from the collection.
     *
     * @param marker
     */
    MarkerRTree.prototype.removeMarker = function(marker) {
        // Find the tileIds where the marker is located
        var tileIds = this._tileIdsByMarkerId[String(marker.id)];
        if (!tileIds) {
            return;
        }

        // Delete the marker entry from this._tileIdsByMarkerId
        delete this._tileIdsByMarkerId[String(marker.id)];

        // Remove the marker from this._markersByTileId
        for (var i = 0; i < tileIds.length; i += 1) {
            var tileId = tileIds[i];
            var markerWithCoords = this._markersByTileId[tileId];

            this._markersByTileId[tileId] = _.filter(markerWithCoords, function (markerWithCoord) {
                return markerWithCoord.marker.id !== marker.id;
            });
            if (this._markersByTileId[tileId].length === 0) {
                delete this._markersByTileId[tileId];
            }
        }
    };

    /**
     * Get the nearest marker to the given point.
     *
     * @param {Number} zoom
     * @param {Number} x
     * @param {Number} y
     * @return {{marker: Object, distance: Number}|null} Marker and distance in pixels
     */
    MarkerRTree.prototype.getNearestMarkerWithDistance = function(zoom, x, y) {
        // Get all the markers in the given tile
        var markerWithCoords = this._markersByTileId[Math.floor(zoom) + '_' + Math.floor(x) + '_' + Math.floor(y)];
        if (!markerWithCoords || markerWithCoords.length === 0) {
            return null;
        }

        // Find the nearest marker
        var nearestDistance2 = Math.pow(markerWithCoords[0].x - x, 2) + Math.pow(markerWithCoords[0].y - y, 2);
        var nearestMarker = markerWithCoords[0].marker;

        for (var i = 1; i < markerWithCoords.length; i += 1) {
            var distance2 = Math.pow(markerWithCoords[i].x - x, 2) + Math.pow(markerWithCoords[i].y - y, 2);
            if (distance2 < nearestDistance2) {
                nearestDistance2 = distance2;
                nearestMarker = markerWithCoords[i].marker;
            }
        }

        return {
            marker: nearestMarker,
            distance: Math.sqrt(nearestDistance2) * 256
        };
    };

    return MarkerRTree;
});
