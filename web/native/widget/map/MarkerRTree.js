/**
 * Define a collection of markers.
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */

define([
    'underscore', './../../../extensions/core/widget/map/projectionUtils',
    './../../../extensions/core/utils/geometryUtils'
], function(_, projectionUtils, geometryUtils) {
    'use strict';

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
     * @param {{width: Number, height: Number}} defaultMarkerSize
     *     Default icon size when a marker has no custom icon.
     * @param {{x: Number, y: Number}} defaultMarkerAnchor
     *     Default icon anchor when a marker has no custom icon.
     * @constructor
     */
    function MarkerRTree(defaultMarkerSize, defaultMarkerAnchor) {
        /**
         * Contains all the markers grouped by tiles.
         *
         * @type {Object.<Number, Array<{marker: Object, x: Number, y: Number, corners: {nw: {x: Number, y: Number}, se: {x: Number, y: Number}}}>>}
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

        /**
         * @type {{width: Number, height: Number}}
         * @private
         */
        this._defaultMarkerSize = defaultMarkerSize;

        /**
         * @type {{x: Number, y: Number}}
         * @private
         */
        this._defaultMarkerAnchor = defaultMarkerAnchor;
    };

    /**
     * Add a marker in the collection.
     *
     * @param marker
     */
    MarkerRTree.prototype.addMarker = function(marker) {
        var markerSize = marker.icon ? marker.icon.size : this._defaultMarkerSize;
        var markerAnchor = marker.icon ? marker.icon.anchor : this._defaultMarkerAnchor;

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
                y: markerPositionXY.y,
                corners: {
                    nw: markerCornerPositions[0],
                    se: markerCornerPositions[2]
                }
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

        // Find the nearest marker by its position
        var nearestDistance2 = Math.pow(markerWithCoords[0].x - x, 2) + Math.pow(markerWithCoords[0].y - y, 2);
        var markerWithCoord = markerWithCoords[0];

        for (var i = 1; i < markerWithCoords.length; i += 1) {
            var distance2 = Math.pow(markerWithCoords[i].x - x, 2) + Math.pow(markerWithCoords[i].y - y, 2);
            if (distance2 < nearestDistance2) {
                nearestDistance2 = distance2;
                markerWithCoord = markerWithCoords[i];
            }
        }

        // Compute the distance between the given point and the marker border
        var distance = geometryUtils.getDistanceBetweenPointAndRectangle({
            x: x,
            y: y
        }, {
            xMin: markerWithCoord.corners.nw.x,
            yMin: markerWithCoord.corners.nw.y,
            xMax: markerWithCoord.corners.se.x,
            yMax: markerWithCoord.corners.se.y
        });

        return {
            marker: markerWithCoord.marker,
            distance: distance * 256
        };
    };

    return MarkerRTree;
});
