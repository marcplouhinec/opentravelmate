package org.opentravelmate.widget.map;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opentravelmate.R;
import org.opentravelmate.commons.ExceptionListener;
import org.opentravelmate.commons.OnReadyExecutor;
import org.opentravelmate.commons.UIThreadExecutor;
import org.opentravelmate.widget.HtmlLayout;
import org.opentravelmate.widget.HtmlLayoutParams;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.UrlTileProvider;

/**
 * Injected object.
 * 
 * @author Marc Plouhinec
 */
public class NativeMap {
	
	public static final String GLOBAL_OBJECT_NAME = "org_opentravelmate_native_widget_map_nativeMap";
	public static final String SCRIPT_URL = "/native/widget/map/nativeMap.js";
	private static final float DEFAULT_ZOOM = 13;
	private static final double DEFAULT_LATITUDE = 49.6;
	private static final double DEFAULT_LONGITUDE = 6.135;
	private static final int INFO_WINDOW_MARGIN_DIP = 10;
	
	private final String baseUrl;
	private final ExceptionListener exceptionListener;
	private final HtmlLayout htmlLayout;
	private final FragmentManager fragmentManager;
	private final Map<String, GoogleMap> mapByPlaceHolderId = new ConcurrentHashMap<String, GoogleMap>();
	private final SparseArray<com.google.android.gms.maps.model.Marker> gmarkerById =
			new SparseArray<com.google.android.gms.maps.model.Marker>();
	private final Map<String, TileObserver> tileObserverByPlaceHolderId = new HashMap<String, TileObserver>();
	private final UrlMarkerIconLoader markerIconLoader;
	private final Map<com.google.android.gms.maps.model.Marker, Marker> markerByGmarker =
			new HashMap<com.google.android.gms.maps.model.Marker, Marker>();
	private final Map<String, CustomInfoWindowAdapter> infoWindowAdapterByPlaceHolderId =
			new HashMap<String, CustomInfoWindowAdapter>();
	private final Map<String, com.google.android.gms.maps.model.Marker> infoWindowMarkerByPlaceHolderId =
			new HashMap<String, com.google.android.gms.maps.model.Marker>();
	private final int infoWindowMargin;
	private final Map<String, OnReadyExecutor> onReadyExecutorByPlaceHolderId = new HashMap<String, OnReadyExecutor>();
	private final Map<String, MapButtonController> mapButtonControllerByPlaceHolderId = new HashMap<String, MapButtonController>();
	private final SparseArray<com.google.android.gms.maps.model.Polyline> gpolylineById =
			new SparseArray<com.google.android.gms.maps.model.Polyline>();
	private final SparseArray<com.google.android.gms.maps.model.Polygon> gpolygonById =
			new SparseArray<com.google.android.gms.maps.model.Polygon>();
	private final SparseArray<com.google.android.gms.maps.model.TileOverlay> tileOverlayById =
			new SparseArray<com.google.android.gms.maps.model.TileOverlay>();
	
	private final Set<String> invisibleMapPlaceholderIds = new HashSet<String>();
	private final Map<String, Queue<Runnable>> postponedRunnablesByPlaceHolderId = new HashMap<String, Queue<Runnable>>();
	
	/**
	 * Create a NativeMap object.
	 */
	public NativeMap(ExceptionListener exceptionListener, HtmlLayout htmlLayout, FragmentManager fragmentManager, String baseUrl) {
		this.exceptionListener = exceptionListener;
		this.htmlLayout = htmlLayout;
		this.fragmentManager = fragmentManager;
		this.baseUrl = baseUrl;
		this.markerIconLoader = new UrlMarkerIconLoader(exceptionListener);
		
		DisplayMetrics metrics = htmlLayout.getContext().getResources().getDisplayMetrics();
		infoWindowMargin = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, INFO_WINDOW_MARGIN_DIP, metrics));
	}

	/**
	 * Build the native view object for the current widget.
	 * 
	 * @param jsonLayoutParams
	 */
	@JavascriptInterface
	public void buildView(final String jsonLayoutParams) {
		UIThreadExecutor.execute(new Runnable() {
			@Override public void run() {
				try {
					buildView(HtmlLayoutParams.fromJsonLayoutParams(new JSONObject(jsonLayoutParams)));
				} catch (JSONException e) {
					exceptionListener.onException(false, e);
				}
			}
		});
	}
	
	/**
	 * Update the native view object for the current widget.
	 * 
	 * @param jsonLayoutParams
	 */
	@JavascriptInterface
	public void updateView(final String jsonLayoutParams) {
		UIThreadExecutor.execute(new Runnable() {
			@Override public void run() {
				try {
					HtmlLayoutParams layoutParams = HtmlLayoutParams.fromJsonLayoutParams(new JSONObject(jsonLayoutParams));
					View view = htmlLayout.findViewByPlaceHolderId(layoutParams.id);
					if (view != null) {
						view.setLayoutParams(layoutParams);
						
						// Mark or un-mark the map ID placeholder as invisible if necessary
						if (!layoutParams.visible) {
							invisibleMapPlaceholderIds.add(layoutParams.id);
						} else if (invisibleMapPlaceholderIds.contains(layoutParams.id)) {
							invisibleMapPlaceholderIds.remove(layoutParams.id);
							executedPostponedRunnables(layoutParams.id);
						}
						
						// Change the map buttons position
						MapButtonController mapButtonController = mapButtonControllerByPlaceHolderId.get(layoutParams.id);
						mapButtonController.onWindowResize(layoutParams);
					}
				} catch (JSONException e) {
					exceptionListener.onException(false, e);
				}
			}
		});
	}
	
	/**
	 * Remove the native view object for the current widget.
	 * 
	 * @param id Place holder ID
	 */
	@JavascriptInterface
	public void removeView(final String id) {
		UIThreadExecutor.execute(new Runnable() {
			@Override public void run() {
				View view = htmlLayout.findViewByPlaceHolderId(id);
				if (view != null) {
					htmlLayout.removeView(view);
				}
			}
		});
	}
	
	/**
	 * Build the native view object for the current widget.
	 * 
	 * @param layoutParams
	 */
	@SuppressLint("SetJavaScriptEnabled")
	public void buildView(final HtmlLayoutParams layoutParams) {
		CameraPosition cameraPosition = new CameraPosition.Builder()
			.target(new com.google.android.gms.maps.model.LatLng(DEFAULT_LATITUDE, DEFAULT_LONGITUDE))
			.zoom(DEFAULT_ZOOM)
			.build();
		GoogleMapOptions options = new GoogleMapOptions().camera(cameraPosition);
		
		final RelativeLayout mapLayout = new RelativeLayout(htmlLayout.getContext());
		mapLayout.setLayoutParams(layoutParams);
		htmlLayout.addView(mapLayout);

		GoogleMapFragment mapFragment = GoogleMapFragment.newInstance(options);
		onReadyExecutorByPlaceHolderId.put(layoutParams.id, new OnReadyExecutor());
		mapFragment.setOnGoogleMapFragmentListener(new GoogleMapFragment.OnGoogleMapFragmentListener() {
			@Override public void onMapReady(GoogleMap map) {
				mapByPlaceHolderId.put(layoutParams.id, map);
				MapButtonController mapButtonController = new MapButtonController(htmlLayout, layoutParams, baseUrl, exceptionListener);
				mapButtonControllerByPlaceHolderId.put(layoutParams.id, mapButtonController);
				onReadyExecutorByPlaceHolderId.get(layoutParams.id).setReady(true);
				
				// When the user click on a map button, forward the click event
				final WebView mainWebView = (WebView)htmlLayout.findViewByPlaceHolderId(HtmlLayout.MAIN_WEBVIEW_ID);
				mapButtonController.onButtonClick(new MapButtonController.ClickListener() {
					@Override public void onClick(MapButton mapButton) {
						mainWebView.loadUrl("javascript:(function(){" +
								"    require(['extensions/org/opentravelmate/controller/widget/Widget'], function (Widget) {" +
								"        var map = Widget.findById('" + layoutParams.id + "');" +
								"        map.fireMapButtonClickEvent(" + mapButton.id + ");" +
								"    });" +
								"})();");
					}
				});
			}
		});
		
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.add(mapLayout.getId(), mapFragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}
	
	/**
	 * Add an overlay to the map.
	 * 
	 * @param id
     *     Map place holder ID.
	 * @param jsonTileOverlay
	 *     JSON serialized TileOverlay.
	 */
	@JavascriptInterface
	public void addTileOverlay(final String id, final String jsonTileOverlay) {
		final GoogleMap map = getGoogleMapSync(id);
		
		UIThreadExecutor.execute(postponeWhenMapIsInvisible(id, new Runnable() {
			@Override public void run() {
				try {
					TileOverlay tileOverlay = TileOverlay.fromJsonTileOverlay(new JSONObject(jsonTileOverlay));
					
					TileProvider tileProvider = new UrlPatternTileProvider(tileOverlay.tileUrlPattern, tileOverlay.enableGrayscaleFilter);
					com.google.android.gms.maps.model.TileOverlay gTileOverlay = map.addTileOverlay(new TileOverlayOptions()
						.tileProvider(tileProvider)
						.zIndex(tileOverlay.zIndex));
					tileOverlayById.put(tileOverlay.id, gTileOverlay);
				} catch (JSONException e) {
					exceptionListener.onException(false, e);
				}
			}
		}));
	}
	
	/**
	 * Remove an overlay from the map.
	 * 
	 * @param id
     *     Map place holder ID.
	 * @param jsonTileOverlay
	 *     JSON serialized TileOverlay.
	 */
	@JavascriptInterface
	public void removeTileOverlay(final String id, final String jsonTileOverlay) {
		UIThreadExecutor.execute(postponeWhenMapIsInvisible(id, new Runnable() {
			@Override public void run() {
				try {
					TileOverlay tileOverlay = TileOverlay.fromJsonTileOverlay(new JSONObject(jsonTileOverlay));
					
					com.google.android.gms.maps.model.TileOverlay gTileOverlay = tileOverlayById.get(tileOverlay.id);
					if (gTileOverlay != null) {
						gTileOverlay.remove();
					}
				} catch (JSONException e) {
					exceptionListener.onException(false, e);
				}
			}
		}));
	}
	
	/**
	 * Move the map center to the given location.
	 * 
	 * @param id
	 *     Map place holder ID.
	 * @param jsonCenter
	 *     JSON serialized LatLng.
	 */
	@JavascriptInterface
	public void panTo(final String id, final String jsonCenter) {
		final GoogleMap map = getGoogleMapSync(id);
		
		UIThreadExecutor.execute(postponeWhenMapIsInvisible(id, new Runnable() {
			@Override public void run() {
				try {
					LatLng center = LatLng.fromJsonLatLng(new JSONObject(jsonCenter));
					CameraPosition cameraPosition = new CameraPosition.Builder()
						.target(new com.google.android.gms.maps.model.LatLng(center.lat, center.lng))
						.zoom(map.getCameraPosition().zoom)
						.build();
					map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
				} catch (JSONException e) {
					exceptionListener.onException(false, e);
				}
			}
		}));
	}
	
	/**
	 * Get the map zoom level.
     *
     * @param id
     *     Map place holder ID.
     * @return zoom level
	 */
	@JavascriptInterface
	public float getZoom(final String id) {
		final GoogleMap map = getGoogleMapSync(id);
		
		try {
			Float zoom = UIThreadExecutor.executeSync(new Callable<Float>() {
				@Override public Float call() throws Exception {
					return map.getCameraPosition().zoom;
				}
			}, 100);
			return zoom;
		} catch (Exception e) {
			exceptionListener.onException(false, e);
		}
		
		return 0;
	}
	
	/**
	 * Get the map bounds (South-West and North-East points).
     *
     * @param id
     *     Map place holder ID.
     * @return jsonBounds
     *     JSON serialized {sw: LatLng, ne: LatLng}.
	 */
	@JavascriptInterface
	public String getBounds(final String id) {
		final GoogleMap map = getGoogleMapSync(id);
		
		try {
			JSONObject jsonBounds = UIThreadExecutor.executeSync(new Callable<JSONObject>() {
				@Override public JSONObject call() throws Exception {
					CameraPosition cameraPosition = map.getCameraPosition();
					float zoom = cameraPosition.zoom;
					Point xyCenter = new Point(
							ProjectionUtils.lngToTileX(zoom, cameraPosition.target.longitude),
							ProjectionUtils.latToTileY(zoom, cameraPosition.target.latitude));
					
					View mapView = htmlLayout.findViewByPlaceHolderId(id);
					int mapCanvasWidth = mapView.getWidth();
					int mapCanvasHeight = mapView.getHeight();
					
					Point xyNorthEast = new Point(xyCenter.x + (mapCanvasWidth / 2) / 256, xyCenter.y - (mapCanvasHeight / 2) / 256);
					Point xySouthWest = new Point(xyCenter.x - (mapCanvasWidth / 2) / 256, xyCenter.y + (mapCanvasHeight / 2) / 256);
					
					LatLng northEast = new LatLng(
							ProjectionUtils.tileYToLat(zoom, xyNorthEast.y),
							ProjectionUtils.tileXToLng(zoom, xyNorthEast.x));
					LatLng southWest = new LatLng(
							ProjectionUtils.tileYToLat(zoom, xySouthWest.y),
							ProjectionUtils.tileXToLng(zoom, xySouthWest.x));
					
					JSONObject jsonBounds = new JSONObject();
					JSONObject jsonSW = new JSONObject();
					jsonSW.put("lat", southWest.lat);
					jsonSW.put("lng", southWest.lng);
					jsonBounds.put("sw", jsonSW);
					JSONObject jsonNE = new JSONObject();
					jsonNE.put("lat", northEast.lat);
					jsonNE.put("lng", northEast.lng);
					jsonBounds.put("ne", jsonNE);
				
					return jsonBounds;
				}
			}, 100);
			return jsonBounds.toString(2);
		} catch (Exception e) {
			exceptionListener.onException(false, e);
		}
		
		return "{}";
	}
	
	/**
	 * Move the map to match the given bounds.
	 * 
	 * @param id
	 *     Map place holder ID.
	 * @param jsonBounds
	 *     JSON serialized {sw: LatLng, ne: LatLng}.
	 */
	@JavascriptInterface
	public void panToBounds(final String id, final String jsonBounds) {
		final GoogleMap map = getGoogleMapSync(id);
		
		UIThreadExecutor.execute(postponeWhenMapIsInvisible(id, new Runnable() {
			@Override public void run() {
				try {
					JSONObject jsonObjectBounds = new JSONObject(jsonBounds);
					JSONObject jsonSW = jsonObjectBounds.getJSONObject("sw");
					JSONObject jsonNE = jsonObjectBounds.getJSONObject("ne");
					LatLng southWest = LatLng.fromJsonLatLng(jsonSW);
					LatLng northEast = LatLng.fromJsonLatLng(jsonNE);
					LatLngBounds latLngBounds = new LatLngBounds(
							new com.google.android.gms.maps.model.LatLng(southWest.lat, southWest.lng),
							new com.google.android.gms.maps.model.LatLng(northEast.lat, northEast.lng));
					map.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 10));
				} catch (JSONException e) {
					exceptionListener.onException(false, e);
				}
			}
		}));
	}
	
	/**
	 * Add markers on the map.
	 * 
	 * @param id
	 *     Map place holder ID.
	 * @param jsonMarkers
     *     JSON serialized array of markers.
	 */
	@JavascriptInterface
	public void addMarkers(final String id, final String jsonMarkers) {
		final GoogleMap map = getGoogleMapSync(id);
		UIThreadExecutor.execute(postponeWhenMapIsInvisible(id, new Runnable() {
			@Override public void run() {
				List<Marker> markers;
				try {
					markers = Marker.fromJsonMarkers(new JSONArray(jsonMarkers));
				} catch (JSONException e) {
					exceptionListener.onException(false, e);
					return;
				}
				
				for (final Marker marker : markers) {
					if (marker.icon == null) {
						addMarker(map, marker, null);
					} else if (marker.icon instanceof UrlMarkerIcon) {
						// Find the Marker Icon scale ratio
						View view = htmlLayout.findViewByPlaceHolderId(id);
						HtmlLayoutParams layoutParams = (HtmlLayoutParams)view.getLayoutParams();
						double scaleRatio = view.getWidth() / layoutParams.windowWidth;
						
						// Load the icon
						markerIconLoader.loadIcon(marker.icon, scaleRatio, new UrlMarkerIconLoader.OnIconLoadListener() {
							@Override public void onIconLoad(final Bitmap bitmap) {
								UIThreadExecutor.execute(postponeWhenMapIsInvisible(id, new Runnable() {
									@Override public void run() {
										addMarker(map, marker, bitmap);
									}
								}));
							}
						});
					} else if (marker.icon instanceof VectorMarkerIcon) {
						VectorMarkerIcon icon = (VectorMarkerIcon)marker.icon;
						SVG svgIcon;
						try {
							svgIcon = SVG.getFromString(icon.toSvg());
						} catch (SVGParseException e) {
							exceptionListener.onException(false, e);
							return;
						}
						Bitmap iconBitmap = Bitmap.createBitmap(icon.size.width, icon.size.height, Bitmap.Config.ARGB_8888);
						Canvas canvas = new Canvas(iconBitmap);
						svgIcon.renderToCanvas(canvas);
						addMarker(map, marker, iconBitmap);
					}
				}
			}
		}));
	}
	
	/**
	 * Add the given marker with the given icon to the map.
	 * 
	 * @param map
	 * @param marker
	 * @param markerIcon
	 */
	private void addMarker(final GoogleMap map, final Marker marker, final Bitmap markerIcon) {
		MarkerOptions markerOptions = new MarkerOptions()
			.position(new com.google.android.gms.maps.model.LatLng(marker.position.lat, marker.position.lng))
			.title(marker.title);
		
		if (markerIcon != null) {
			markerOptions.icon(BitmapDescriptorFactory.fromBitmap(markerIcon));
			markerOptions.anchor((float)(marker.icon.anchor.x / marker.icon.size.width), (float)(marker.icon.anchor.y / marker.icon.size.height));
		}
		
		com.google.android.gms.maps.model.Marker gmarker = map.addMarker(markerOptions);
		gmarkerById.put(marker.id, gmarker);
		markerByGmarker.put(gmarker, marker);
	}
	
	/**
	 * Remove markers from the map.
	 * 
	 * @param id
	 *     Map place holder ID.
	 * @param jsonMarkers
     *     JSON serialized array of markers.
	 */
	@JavascriptInterface
	public void removeMarkers(final String id, final String jsonMarkers) {
		UIThreadExecutor.execute(postponeWhenMapIsInvisible(id, new Runnable() {
			@Override public void run() {
				try {
					List<Marker> markers = Marker.fromJsonMarkers(new JSONArray(jsonMarkers));
					for (Marker marker : markers) {
						if (gmarkerById.get(marker.id) != null) {
							gmarkerById.get(marker.id).remove();
							gmarkerById.remove(marker.id);
						}
					}
				} catch (JSONException e) {
					exceptionListener.onException(false, e);
				}
			}
		}));
	}
	
	/**
     * Add a button on the map top-right corner.
     *
     * @param {String} id
     *     Map place holder ID.
     * @param {String} jsonMapButton
     *     JSON serialized MapButton.
     */
	@JavascriptInterface
	public void addMapButton(final String id, final String jsonMapButton) {
		UIThreadExecutor.execute(postponeWhenMapIsInvisible(id, new Runnable() {
			@Override public void run() {
				// Postpone the function execution if the map is not yet ready
				OnReadyExecutor onReadyExecutor = onReadyExecutorByPlaceHolderId.get(id);
				if (!onReadyExecutor.isReady()) {
					onReadyExecutor.execute(this);
					return;
				}
				
				try {
					MapButton mapButton = MapButton.fromJsonMapButton(new JSONObject(jsonMapButton));
					mapButtonControllerByPlaceHolderId.get(id).addButton(mapButton);
				} catch (JSONException e) {
					exceptionListener.onException(false, e);
				}
			}
		}));
	}
	
	/**
     * Update a button on the map.
     *
     * @param {String} id
     *     Map place holder ID.
     * @param {String} jsonMapButton
     *     JSON serialized MapButton.
     */
	@JavascriptInterface
	public void updateMapButton(final String id, final String jsonMapButton) {
		UIThreadExecutor.execute(postponeWhenMapIsInvisible(id, new Runnable() {
			@Override public void run() {
				// Postpone the function execution if the map is not yet ready
				OnReadyExecutor onReadyExecutor = onReadyExecutorByPlaceHolderId.get(id);
				if (!onReadyExecutor.isReady()) {
					onReadyExecutor.execute(this);
					return;
				}
				
				try {
					MapButton mapButton = MapButton.fromJsonMapButton(new JSONObject(jsonMapButton));
					mapButtonControllerByPlaceHolderId.get(id).updateButton(mapButton);
				} catch (JSONException e) {
					exceptionListener.onException(false, e);
				}
			}
		}));
	}
	
	/**
     * Remove a button from the map top-right corner.
     *
     * @param {String} id
     *     Map place holder ID.
     * @param {String} jsonMapButton
     *     JSON serialized MapButton.
     */
	@JavascriptInterface
	public void removeMapButton(final String id, final String jsonMapButton) {
		UIThreadExecutor.execute(postponeWhenMapIsInvisible(id, new Runnable() {
			@Override public void run() {
				// Postpone the function execution if the map is not yet ready
				OnReadyExecutor onReadyExecutor = onReadyExecutorByPlaceHolderId.get(id);
				if (!onReadyExecutor.isReady()) {
					onReadyExecutor.execute(this);
					return;
				}
				
				
				try {
					MapButton mapButton = MapButton.fromJsonMapButton(new JSONObject(jsonMapButton));
					mapButtonControllerByPlaceHolderId.get(id).removeButton(mapButton);
				} catch (JSONException e) {
					exceptionListener.onException(false, e);
				}
			}
		}));
	}
	
	/**
	 * Start observing tiles and forward the TILES_DISPLAYED and TILES_RELEASED events to the
     * map defined by the given place-holder ID.
     * Note: this function does nothing if the tiles are already observed.
     * 
	 * @param id
	 *     Map place holder ID.
	 */
	@JavascriptInterface
	public void observeTiles(final String id) {
		final GoogleMap map = getGoogleMapSync(id);
		
		UIThreadExecutor.execute(postponeWhenMapIsInvisible(id, new Runnable() {
			@Override public void run() {
				TileObserver tileObserver = tileObserverByPlaceHolderId.get(id);
				
				if (tileObserver == null) {
					tileObserver = new TileObserver(map, htmlLayout.findViewByPlaceHolderId(id));
					tileObserverByPlaceHolderId.put(id, tileObserver);
					
					tileObserver.onTilesDisplayed(new TileObserver.TilesListener() {
						@Override public void on(List<TileCoordinates> tileCoordinates) {
							WebView mainWebView = (WebView)htmlLayout.findViewByPlaceHolderId(HtmlLayout.MAIN_WEBVIEW_ID);
							try {
								JSONArray jsonTileCoordinates = TileCoordinates.toJson(tileCoordinates);
								mainWebView.loadUrl("javascript:(function(){" +
										"    require(['extensions/org/opentravelmate/controller/widget/Widget'], function (Widget) {" +
										"        var map = Widget.findById('" + id + "');" +
										"        map.fireTileEvent('TILES_DISPLAYED', " + jsonTileCoordinates.toString(2) + ");" +
										"    });" +
										"})();");
							} catch(JSONException e) {
								exceptionListener.onException(false, e);
							}
						}
					});
					tileObserver.onTilesReleased(new TileObserver.TilesListener() {
						@Override public void on(List<TileCoordinates> tileCoordinates) {
							WebView mainWebView = (WebView)htmlLayout.findViewByPlaceHolderId(HtmlLayout.MAIN_WEBVIEW_ID);
							try {
								JSONArray jsonTileCoordinates = TileCoordinates.toJson(tileCoordinates);
								mainWebView.loadUrl("javascript:(function(){" +
										"    require(['extensions/org/opentravelmate/controller/widget/Widget'], function (Widget) {" +
										"        var map = Widget.findById('" + id + "');" +
										"        map.fireTileEvent('TILES_RELEASED', " + jsonTileCoordinates.toString(2) + ");" +
										"    });" +
										"})();");
							} catch(JSONException e) {
								exceptionListener.onException(false, e);
							}
						}
					});
				}
			}
		}));
	}
	
	/**
	 * Get all the visible tile coordinates.
     * Note: the function observeTiles() must be called before executing this one.
     * 
     * @param id
	 *     Map place holder ID.
	 * @return JSON-serialized Array.<{zoom: Number, x: Number, y: Number}>
	 */
	@JavascriptInterface
	public String getDisplayedTileCoordinates(final String id) {
		try {
			JSONArray jsonDisplayedTileCoordinates = UIThreadExecutor.executeSync(new Callable<JSONArray>() {
				@Override public JSONArray call() throws Exception {
					TileObserver tileObserver = tileObserverByPlaceHolderId.get(id);
					if (tileObserver != null) {
						List<TileCoordinates> displayedTileCoordinates = tileObserver.getDisplayedTileCoordinates();
						return TileCoordinates.toJson(displayedTileCoordinates);
					}
					return new JSONArray();
				}
			}, 100);
			return jsonDisplayedTileCoordinates.toString(2);
		} catch (Exception e) {
			exceptionListener.onException(false, e);
		}
		
		return "[]";
	}
	
	/**
     * Start observing markers and forward the CLICK, MOUSE_ENTER and MOUSE_LEAVE events to the
     * map defined by the given place-holder ID.
     * Note: this function does nothing if the markers are already observed.
     *
     * @param id
     *     Map place holder ID.
     */
	@JavascriptInterface
	public void observeMarkers(final String id) {
		final GoogleMap map = getGoogleMapSync(id);
		
		UIThreadExecutor.execute(postponeWhenMapIsInvisible(id, new Runnable() {
			@Override public void run() {
				// Observe marker click
				map.setOnMarkerClickListener(new CustomMarkerClickListener(id));
				CustomInfoWindowAdapter infoWindowAdapter = new CustomInfoWindowAdapter();
				infoWindowAdapterByPlaceHolderId.put(id, infoWindowAdapter);
				map.setInfoWindowAdapter(infoWindowAdapter);
				
				// Observe InfoWindow click
				map.setOnInfoWindowClickListener(new CustomInfoWindowClickListener(id));
			}
		}));
	}
	
	/**
	 * Forward marker click event to the JS Map object.
	 */
	private class CustomMarkerClickListener implements OnMarkerClickListener {
		
		private final String placeHolderId;

		public CustomMarkerClickListener(String placeHolderId) {
			this.placeHolderId = placeHolderId;
		}

		@Override
		public boolean onMarkerClick(com.google.android.gms.maps.model.Marker gmarker) {
			WebView mainWebView = (WebView)htmlLayout.findViewByPlaceHolderId(HtmlLayout.MAIN_WEBVIEW_ID);
			Marker marker = markerByGmarker.get(gmarker);
			try {
				mainWebView.loadUrl("javascript:(function(){" +
						"    require(['extensions/org/opentravelmate/controller/widget/Widget'], function (Widget) {" +
						"        var map = Widget.findById('" + placeHolderId + "');" +
						"        map.fireMarkerEvent('CLICK', " + marker.toJson().toString(2) + ");" +
						"    });" +
						"})();");
			} catch (JSONException e) {
				exceptionListener.onException(false, e);
			}
			return true;
		}
	}
	
	/**
	 * Forward InfoWindow click event to the JS Map object.
	 */
	private class CustomInfoWindowClickListener implements OnInfoWindowClickListener {
		
		private final String placeHolderId;

		public CustomInfoWindowClickListener(String placeHolderId) {
			this.placeHolderId = placeHolderId;
		}
		
		@Override
		public void onInfoWindowClick(com.google.android.gms.maps.model.Marker gmarker) {
			WebView mainWebView = (WebView)htmlLayout.findViewByPlaceHolderId(HtmlLayout.MAIN_WEBVIEW_ID);
			Marker marker = markerByGmarker.get(gmarker);
			try {
				mainWebView.loadUrl("javascript:(function(){" +
						"    require(['extensions/org/opentravelmate/controller/widget/Widget'], function (Widget) {" +
						"        var map = Widget.findById('" + placeHolderId + "');" +
						"        map.fireInfoWindowClickEvent(" + marker.toJson().toString(2) + ");" +
						"    });" +
						"})();");
			} catch (JSONException e) {
				exceptionListener.onException(false, e);
			}
		}
	}
	
	/**
     * Show the given text in an Info Window on top of the given marker.
     *
     * @param id
     *     Map place holder ID.
     * @param jsonMarker
     *     JSON-serialized marker where to set the Info Window anchor.
     * @param content
     *     Text displayed in the Info Window.
     * @param jsonAnchor
     *     JSON-serialized position of the the InfoWindow-base compared to the marker position.
     *     Examples:
     *       - (0,0) is the marker position.
     *       - (0,1) is on the under of the marker position.
     *       - (-1,0) is on the left of the marker position.
     */
	@JavascriptInterface
	public void showInfoWindow(final String id, final String jsonMarker, final String content, final String jsonAnchor) {
		final GoogleMap map = getGoogleMapSync(id);
		
		UIThreadExecutor.execute(postponeWhenMapIsInvisible(id, new Runnable() {
			@Override public void run() {
				Marker marker;
				Point anchor;
				try {
					marker = Marker.fromJsonMarker(new JSONObject(jsonMarker));
					anchor = "null".equals(jsonAnchor) ? null : Point.fromJsonPoint(new JSONObject(jsonAnchor));
				} catch (JSONException e) {
					exceptionListener.onException(false, e);
					return;
				}
				
				CustomInfoWindowAdapter infoWindowAdapter = infoWindowAdapterByPlaceHolderId.get(id);
				infoWindowAdapter.setContent(content);
				com.google.android.gms.maps.model.Marker gmarker = gmarkerById.get(marker.id);
				
				// Since the InfowWindow object cannot be customized with an achor, it is handled manually with an
				// invisible marker.
				if (anchor == null || marker.icon == null) {
					gmarker.showInfoWindow();
				} else {
					// Remove the existing info window marker if any
					com.google.android.gms.maps.model.Marker existingInfoWindowMarker = infoWindowMarkerByPlaceHolderId.get(id);
					if (existingInfoWindowMarker != null) {
						existingInfoWindowMarker.remove();
						markerByGmarker.remove(existingInfoWindowMarker);
					}
					
					// Create a new info window marker and display it
					MarkerOptions infoWindowMarkerOptions = new MarkerOptions()
						.position(new com.google.android.gms.maps.model.LatLng(marker.position.lat, marker.position.lng))
						.title(marker.title)
						.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_transparent_marker))
						.anchor((float)(anchor.x / marker.icon.size.width), (float)(anchor.y / marker.icon.size.height));
					
					com.google.android.gms.maps.model.Marker infoWindowMarker = map.addMarker(infoWindowMarkerOptions);
					infoWindowMarker.showInfoWindow();
					infoWindowMarkerByPlaceHolderId.put(id, infoWindowMarker);
					markerByGmarker.put(infoWindowMarker, marker);
				}
				
				// Move the camera in order to be able to see the InfoWindow completely
				Rect bounds = infoWindowAdapter.getInfoWindowBounds();
				bounds.right += infoWindowMargin;
				bounds.bottom += infoWindowMargin;
				View mapView = htmlLayout.findViewByPlaceHolderId(id);
				CameraPosition cameraPosition = map.getCameraPosition();
				com.google.android.gms.maps.model.LatLng mapCenter = cameraPosition.target;
				Point mapCenterTileXY = new Point(
						ProjectionUtils.lngToTileX(cameraPosition.zoom, mapCenter.longitude),
						ProjectionUtils.latToTileY(cameraPosition.zoom, mapCenter.latitude));
				Point markerPositionTileXY = new Point(
						ProjectionUtils.lngToTileX(cameraPosition.zoom, marker.position.lng),
						ProjectionUtils.latToTileY(cameraPosition.zoom, marker.position.lat));
				int markerPositionX = (int)Math.round((markerPositionTileXY.x - mapCenterTileXY.x) * 512 + mapView.getWidth() / 2);
				int markerPositionY = (int)Math.round((markerPositionTileXY.y - mapCenterTileXY.y) * 512 + mapView.getHeight() / 2);
				int infoWindowNorthWestX = markerPositionX - bounds.width() / 2;
				int infoWindowNorthWestY = markerPositionY - bounds.height();
				int infoWindowSouthEastX = markerPositionX + bounds.width() / 2;
				int infoWindowSouthEastY = markerPositionY + bounds.height();
				int scrollX = 0;
				int scrollY = 0;
				if (infoWindowNorthWestX < 0) {
					scrollX = infoWindowNorthWestX;
				}
				if (infoWindowNorthWestY < 0) {
					scrollY = infoWindowNorthWestY;
				}
				if (infoWindowSouthEastX > mapView.getWidth()) {
					scrollX = infoWindowSouthEastX - mapView.getWidth();
				}
				if (infoWindowSouthEastY > mapView.getHeight()) {
					scrollY = infoWindowSouthEastY - mapView.getHeight();
				}
				CameraUpdate cameraUpdate = CameraUpdateFactory.scrollBy(scrollX, scrollY);
				map.animateCamera(cameraUpdate, 500, null);
			}
		}));
	}
	
	/**
     * Close the Info Window if any.
     *
     * @param id
     *     Map place holder ID.
     */
	@JavascriptInterface
	public void closeInfoWindow(final String id) {
		UIThreadExecutor.execute(postponeWhenMapIsInvisible(id, new Runnable() {
			@Override public void run() {
				com.google.android.gms.maps.model.Marker existingInfoWindowMarker = infoWindowMarkerByPlaceHolderId.get(id);
				if (existingInfoWindowMarker != null) {
					existingInfoWindowMarker.remove();
					markerByGmarker.remove(existingInfoWindowMarker);
				}
			}
		}));
	}
	
	/**
     * Set the map type.
     *
     * @param id
     *     Map place holder ID.
     * @param mapType
     *     'ROADMAP', 'HYBRID' or 'SATELLITE'.
     */
	@JavascriptInterface
	public void setMapType(final String id, final String mapType) {
		final GoogleMap map = getGoogleMapSync(id);
		
		UIThreadExecutor.execute(postponeWhenMapIsInvisible(id, new Runnable() {
			@Override public void run() {
				int gmapType = GoogleMap.MAP_TYPE_NORMAL;
				if ("SATELLITE".equals(mapType)) {
					gmapType = GoogleMap.MAP_TYPE_SATELLITE;
				} else if ("HYBRID".equals(mapType)) {
					gmapType = GoogleMap.MAP_TYPE_HYBRID;
				}
				map.setMapType(gmapType);
				mapButtonControllerByPlaceHolderId.get(id).setMapType(mapType);
			}
		}));
	}
	
	/**
     * Show the given polylines on the map.
     *
     * @param id
     *     Map place holder ID.
     * @param jsonPolylines
     *     Polylines to add.
     */
	@JavascriptInterface
	public void addPolylines(final String id, final String jsonPolylines) {
		final GoogleMap map = getGoogleMapSync(id);
		
		UIThreadExecutor.execute(postponeWhenMapIsInvisible(id, new Runnable() {
			@Override public void run() {
				List<Polyline> polylines;
				try {
					polylines = Polyline.fromJsonPolylines(new JSONArray(jsonPolylines));
				} catch (JSONException e) {
					exceptionListener.onException(false, e);
					return;
				}
				
				for (Polyline polyline : polylines) {
					com.google.android.gms.maps.model.LatLng[] gpoints =
							new com.google.android.gms.maps.model.LatLng[polyline.path.size()];
					for (int i = 0; i < polyline.path.size(); i++) {
						LatLng latlng = polyline.path.get(i);
						gpoints[i] = new com.google.android.gms.maps.model.LatLng(latlng.lat, latlng.lng);
					}
					
					com.google.android.gms.maps.model.Polyline gpolyline = map.addPolyline(new PolylineOptions()
						.add(gpoints)
						.color(polyline.color)
						.width(polyline.width)
						.zIndex(2000F));
					gpolylineById.put(polyline.id, gpolyline);
				}
			}
		}));
	}
	
	/**
     * Remove the given polylines from the map.
     *
     * @param id
     *     Map place holder ID.
     * @param jsonPolylines
     *     Polylines to remove.
     */
	@JavascriptInterface
	public void removePolylines(final String id, final String jsonPolylines) {
		UIThreadExecutor.execute(postponeWhenMapIsInvisible(id, new Runnable() {
			@Override public void run() {
				List<Polyline> polylines;
				try {
					polylines = Polyline.fromJsonPolylines(new JSONArray(jsonPolylines));
				} catch (JSONException e) {
					exceptionListener.onException(false, e);
					return;
				}
				
				for (Polyline polyline : polylines) {
					com.google.android.gms.maps.model.Polyline gpolyline = gpolylineById.get(polyline.id);
					if (gpolyline != null) {
						gpolyline.remove();
						gpolylineById.remove(polyline.id);
					}
				}
			}
		}));
	}
	
	/**
	 * Add the given polygons on the map.
	 * 
	 * @param id
     *     Map place holder ID.
	 * @param jsonPolygons
     *     Polygons to add.
	 */
	@JavascriptInterface
	public void addPolygons(final String id, final String jsonPolygons) {
		final GoogleMap map = getGoogleMapSync(id);
		
		UIThreadExecutor.execute(postponeWhenMapIsInvisible(id, new Runnable() {
			@Override public void run() {
				List<Polygon> polygons;
				try {
					polygons = Polygon.fromJsonPolygonArray(new JSONArray(jsonPolygons));
				} catch (JSONException e) {
					exceptionListener.onException(false, e);
					return;
				}
				
				for (Polygon polygon : polygons) {
					com.google.android.gms.maps.model.LatLng[] gpoints =
							new com.google.android.gms.maps.model.LatLng[polygon.path.size()];
					for (int i = 0; i < polygon.path.size(); i++) {
						LatLng latlng = polygon.path.get(i);
						gpoints[i] = new com.google.android.gms.maps.model.LatLng(latlng.lat, latlng.lng);
					}
					
					com.google.android.gms.maps.model.Polygon gpolygon = map.addPolygon(new PolygonOptions()
						.add(gpoints)
						.fillColor(polygon.fillColor)
						.strokeColor(polygon.strokeColor)
						.strokeWidth(polygon.strokeWidth)
						.zIndex(1000F));
					gpolygonById.put(polygon.id, gpolygon);
				}
			}
		}));
	}
	
	/**
	 * Remove the given polygons from the map.
	 * 
	 * @param id
     *     Map place holder ID.
	 * @param jsonPolygons
     *     Polygons to remove.
	 */
	@JavascriptInterface
	public void removePolygons(final String id, final String jsonPolygons) {
		UIThreadExecutor.execute(postponeWhenMapIsInvisible(id, new Runnable() {
			@Override public void run() {
				List<Polygon> polygons;
				try {
					polygons = Polygon.fromJsonPolygonArray(new JSONArray(jsonPolygons));
				} catch (JSONException e) {
					exceptionListener.onException(false, e);
					return;
				}
				
				for (Polygon polygon : polygons) {
					com.google.android.gms.maps.model.Polygon gpolygon = gpolygonById.get(polygon.id);
					if (gpolygon != null) {
						gpolygon.remove();
						gpolygonById.remove(polygon.id);
					}
				}
			}
		}));
	}
	
	/**
	 * Get the map. Wait if the map is not ready.
	 * 
	 * @param id
	 *     Map place holder ID.
	 * @return map
	 */
	private GoogleMap getGoogleMapSync(String id) {
		long watchDog = 100;
		GoogleMap googleMap = null;
		while (googleMap == null && watchDog-- > 0) {
			googleMap = mapByPlaceHolderId.get(id);
			try { Thread.sleep(100); } catch (InterruptedException e) { }
		}
		return googleMap;
	}
	
	
	/**
	 * Intercept the execution of a {@link Runnable} in order to check if the map with the given placeholder ID is visible or not.
	 * 
	 * @param runnable Runnable to be executed only when the map is visible
	 * @param id Map placeholder ID
	 * @return Wrapped {@link Runnable}
	 */
	private Runnable postponeWhenMapIsInvisible(final String id, final Runnable runnable) {
		return new Runnable() {
			@Override public void run() {
				if (invisibleMapPlaceholderIds.contains(id)) {
					Queue<Runnable> postponedRunnables = postponedRunnablesByPlaceHolderId.get(id);
					if (postponedRunnables == null) {
						postponedRunnables = new LinkedList<Runnable>();
						postponedRunnablesByPlaceHolderId.put(id, postponedRunnables);
					}
					postponedRunnables.add(runnable);
				} else {
					runnable.run();
				}
			}
		};
	}
	
	/**
	 * Execute all the postponed {@link Runnable}s for the map with the given placeholder ID.
	 * 
	 * @param id Map placeholder ID
	 */
	private void executedPostponedRunnables(String id) {
		final Queue<Runnable> postponedRunnables = postponedRunnablesByPlaceHolderId.get(id);
		if (postponedRunnables == null) { return; }
		
		UIThreadExecutor.execute(new Runnable() {
			@Override public void run() {
				Runnable postponedRunnable;
				while ((postponedRunnable = postponedRunnables.poll()) != null) {
					postponedRunnable.run();
				}
			}
		});
	}
	
	private class CustomInfoWindowAdapter implements InfoWindowAdapter {
		
		private static final int PADDING_RIGHT = 21;
		private static final int PADDING_LEFT = 21;
		private static final int PADDING_TOP = 16;
		private static final int PADDING_BOTTOM = 58;
		
		private String content = "";
		private final TextView textView;
		
		public CustomInfoWindowAdapter() {
			textView = new TextView(htmlLayout.getContext());
		}
		
		@Override
		public View getInfoContents(com.google.android.gms.maps.model.Marker gmarker) {
			textView.setText(content);
			textView.setTextColor(0xFF000000);
			textView.setTypeface(null, Typeface.BOLD);
			return textView;
		}

		@Override
		public View getInfoWindow(com.google.android.gms.maps.model.Marker gmarker) {
			return null;
		}

		public void setContent(String content) {
			this.content = content;
		}
		
		public Rect getInfoWindowBounds() {
			Rect bounds = new Rect();
			Paint textPaint = textView.getPaint();
			textPaint.getTextBounds(content, 0, content.length(), bounds);
			bounds.right += PADDING_LEFT + PADDING_RIGHT;
			bounds.bottom += PADDING_TOP + PADDING_BOTTOM;
			return bounds;
		}
	}
	
	/**
	 * TileProvider based on TileOverlay.tileUrlPattern.
	 */
	private class UrlPatternTileProvider extends UrlTileProvider {
		
		private final String tileUrlPattern;
		private final boolean enableGrayscaleFilter;

		/**
		 * Create a new UrlPatternTileProvider.
		 * 
		 * @param tileUrlPattern
		 * @param enableGrayscaleFilter If true, apply a grayscale filter on the tiles
		 */
		public UrlPatternTileProvider(String tileUrlPattern, boolean enableGrayscaleFilter) {
			super(256, 256);
			this.tileUrlPattern = tileUrlPattern;
			this.enableGrayscaleFilter = enableGrayscaleFilter;
		}

		@Override
		public URL getTileUrl(final int x, final int y, final int zoom) {
			String originalUrl = tileUrlPattern
					.replace("${zoom}", String.valueOf(zoom))
					.replace("${x}", String.valueOf(x))
					.replace("${y}", String.valueOf(y));
			
			// Use the image interceptor in order to cache the tile in the disk
			String url;
			try {
				url = baseUrl + "image/source/" + URLEncoder.encode(originalUrl, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				url = originalUrl;
			}
			if (enableGrayscaleFilter) {
				url += "?filter=grayscale";
			}
			
			try {
				return new URL(url);
			} catch (MalformedURLException e) {
				exceptionListener.onException(false, e);
				return null;
			}
		}
	}
}
