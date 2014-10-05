/**
 * Observe the tile life-cycles (displayed, released).
 *
 * @author marc.plouhinec@gmail.com (Marc Plouhinec)
 */

define(['jquery', 'lodash', './../../../extensions/org/opentravelmate/controller/widget/map/projectionUtils'], function($, _, projectionUtils) {
    'use strict';

    /**
     * Create a new TileObserver.
     *
     * @param gmap
     * @constructor
     */
    function TileObserver(gmap) {
        var self = this;

        /**
         * @private
         */
        this._gmap = gmap;

        /**
         * @type {{zoom: Number, x: Number, y: Number}}
         * @private
         */
        this._currentCenteredTileCoordinates = this._getCenteredTileCoordinates();

        /**
         * @type {Object.<String, {zoom: Number, x: Number, y: Number}>}
         * @private
         */
        this._currentlyDisplayedTileCoordinateById = this._getDisplayedTileCoordinateById();

        /**
         * @private
         */
        this._tileListeners = {
            /**
             * @type {Array.<function(tileCoordinates: Array.<{zoom: Number, x: Number, y: Number}>)>}
             */
            tilesDisplayed: [],
            /**
             * @type {Array.<function(tileCoordinates: Array.<{zoom: Number, x: Number, y: Number}>)>}
             */
            tilesReleased: []
        };

        // Listen to the map moves
        google.maps.event.addListener(gmap, 'center_changed', function() {
            self._handleCameraChange();
        });
        google.maps.event.addListener(gmap, 'zoom_changed', function() {
            self._handleCameraChange();
        });
    };

    /**
     * Register a listener for the TILES_DISPLAYED event.
     *
     * @param {function(tileCoordinates: Array.<{zoom: Number, x: Number, y: Number}>)} listener
     */
    TileObserver.prototype.onTilesDisplayed = function(listener) {
        this._tileListeners.tilesDisplayed.push(listener);
    };

    /**
     * Register a listener for the TILES_RELEASED event.
     *
     * @param {function(tileCoordinates: Array.<{zoom: Number, x: Number, y: Number}>)} listener
     */
    TileObserver.prototype.onTilesReleased = function(listener) {
        this._tileListeners.tilesReleased.push(listener);
    };

    /**
     * Get the displayed tiles coordinates.
     *
     * @return {Array.<{zoom: Number, x: Number, y: Number}>}
     */
    TileObserver.prototype.getDisplayedTileCoordinates = function() {
        return _.values(this._currentlyDisplayedTileCoordinateById);
    };

    /**
     * Compute all the displayed tile coordinates.
     *
     * @return {Object.<String, {zoom: Number, x: Number, y: Number}>}
     * @private
     */
    TileObserver.prototype._getDisplayedTileCoordinateById = function() {
        var zoom = this._gmap.getZoom();
        var latlngCenter = this._gmap.getCenter();
        var xyCenter = {
            x: projectionUtils.lngToTileX(zoom, latlngCenter.lng()),
            y: projectionUtils.latToTileY(zoom, latlngCenter.lat())
        };
        var mapCanvas = this._gmap.getDiv();
        var mapCanvasWidth = $(mapCanvas).width();
        var mapCanvasHeight = $(mapCanvas).height();

        var xyNorthEast = { x: xyCenter.x - (mapCanvasWidth / 2) / 256, y: xyCenter.y - (mapCanvasHeight / 2) / 256 };
        var xySouthWest = { x: xyCenter.x + (mapCanvasWidth / 2) / 256, y: xyCenter.y + (mapCanvasHeight / 2) / 256 };
        var tileNorthEast = { zoom: zoom, x: Math.floor(xyNorthEast.x), y: Math.floor(xyNorthEast.y) };
        var tileSouthWest = { zoom: zoom, x: Math.floor(xySouthWest.x), y: Math.floor(xySouthWest.y) };

        // Take all the tiles from the north-east to the south-east and include the adjacents ones outside of the view-port.
        var displayedTileCoordinateById = {};
        for (var y = tileNorthEast.y - 1; y <= tileSouthWest.y + 1; y++) {
            for (var x = tileNorthEast.x - 1; x <= tileSouthWest.x + 1; x++) {
                displayedTileCoordinateById[zoom + '_' + x + '_' + y] = { zoom: zoom, x: x, y: y };
            }
        }

        return displayedTileCoordinateById;
    };

    /**
     * Get the coordinates of the tile in the center.
     *
     * @returns {{zoom: Number, x: Number, y: Number}}
     * @private
     */
    TileObserver.prototype._getCenteredTileCoordinates = function() {
        var zoom = this._gmap.getZoom();
        var latlngCenter = this._gmap.getCenter();
        return {
            zoom : zoom,
            x: Math.floor(projectionUtils.lngToTileX(zoom, latlngCenter.lng())),
            y: Math.floor(projectionUtils.latToTileY(zoom, latlngCenter.lat()))
        };
    };

    /**
     * Handle map center position and zoom change.
     *
     * @private
     */
    TileObserver.prototype._handleCameraChange = function() {
        var self = this;

        // Check if the centered tile has changed
        var centeredTileCoordinates = this._getCenteredTileCoordinates();
        var hasChanged =
            this._currentCenteredTileCoordinates.zoom !== centeredTileCoordinates.zoom ||
            this._currentCenteredTileCoordinates.x !== centeredTileCoordinates.x ||
            this._currentCenteredTileCoordinates.y !== centeredTileCoordinates.y;

        if (hasChanged) {
            this._currentCenteredTileCoordinates = centeredTileCoordinates;

            // Compare the currently displayed tiles with the previous ones
            var displayedTileCoordinateById = this._getDisplayedTileCoordinateById();
            var newTileCoordinates = _.reduce(displayedTileCoordinateById, function(memo, tileCoordinates, tileId) {
                if (!self._currentlyDisplayedTileCoordinateById[tileId]) {
                    memo.push(tileCoordinates);
                }
                return memo;
            }, []);
            var removedTileCoordinates = _.reduce(this._currentlyDisplayedTileCoordinateById, function(memo, tileCoordinates, tileId) {
                if (!displayedTileCoordinateById[tileId]) {
                    memo.push(tileCoordinates);
                }
                return memo;
            }, []);
            this._currentlyDisplayedTileCoordinateById = displayedTileCoordinateById;

            // Call the listeners
            if (newTileCoordinates.length > 0) {
                _.each(this._tileListeners.tilesDisplayed, function(listener) {
                    listener(newTileCoordinates);
                });
            }
            if (removedTileCoordinates.length > 0) {
                _.each(this._tileListeners.tilesReleased, function(listener) {
                    listener(removedTileCoordinates);
                });
            }
        }
    };

    return TileObserver;
});
