/**
 * Define a collection of markers.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */

define(['underscore', './projectionUtils'], function(_, projectionUtils) {
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
                x: projectionUtils.lngToTileX(2, marker.position.lng),
                y: projectionUtils.latToTileY(2, marker.position.lat)
            };

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
                var tileId = zoom + '_' + projectionUtils.tileYToLat(zoom, markerCornerPositions[i].y) + '_' + projectionUtils.tileXToLng(zoom, markerCornerPositions[i].x);

                // Add the marker to this._markersByTileId
                var markerWithCoords = this._markersByTileId[tileId];
                if (!markerWithCoords) {
                    markerWithCoords = [];
                    this._markersByTileId[tileId] = markerWithCoords;
                }
                markerWithCoords.push({
                    marker: marker,
                    x: markerCornerPositions[i].x,
                    y: markerCornerPositions[i].y
                });

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
        var nearestDistance2 = markerWithCoords[0].x * x + markerWithCoords[0].y * y;
        var nearestMarker = markerWithCoords[0].marker;

        for (var i = 1; i < markerWithCoords.length; i += 1) {
            var distance2 = markerWithCoords[i].x * x + markerWithCoords[i].y * y;
            if (distance2 < nearestDistance2) {
                nearestDistance2 = distance2;
                nearestMarker = markerWithCoords[i].marker;
            }
        }

        return {
            marker: nearestMarker,
            distance: Math.sqrt(nearestDistance2)
        };
    };

    return MarkerRTree;
});
