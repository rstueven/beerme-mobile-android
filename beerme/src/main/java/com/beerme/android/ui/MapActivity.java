package com.beerme.android.ui;

import android.content.Intent;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.beerme.android.R;
import com.beerme.android.db.Brewery;
import com.beerme.android.db.BreweryListViewModel;
import com.beerme.android.util.SharedPref;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

public class MapActivity extends LocationActivity
        implements OnMapReadyCallback, MapOrListDialog.MapOrListListener,
        StatusFilterDialog.StatusFilterListener {

    private GoogleMap mMap;
    private ClusterManager<MarkerItem> mClusterManager;

    @Override
//    protected void onCreate(Bundle savedInstanceState) {
    public void onPermissionGranted() {
        Log.d("beerme", "MapActivity.onPermissionGranted()");
        setContentView(R.layout.activity_map);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);

        if (mapFragment == null) {
            throw new IllegalStateException("MapActivity.onCreate(): null mapFragment");
        }

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull final GoogleMap googleMap) {
        Log.d("beerme", "MapActivity.onMapReady()");
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

            mClusterManager = new ClusterManager<>(this, mMap);
            mMap.setOnCameraIdleListener(mClusterManager);
            mMap.setOnMarkerClickListener(mClusterManager);

            mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MarkerItem>() {
                @Override
                public boolean onClusterClick(Cluster<MarkerItem> cluster) {
                    java.util.Collection<MarkerItem> collection = cluster.getItems();

                    LatLng pos;
                    LatLng loc = collection.iterator().next().getPosition();
                    double lat = loc.latitude;
                    double lng = loc.longitude;
                    boolean same = true;

                    for (MarkerItem m : collection) {
                        pos = m.mPosition;
                        if (pos.latitude != lat || pos.longitude != lng) {
                            same = false;
                            break;
                        }
                    }

                    // TODO: Uncluster all the markers in a "same" cluster.
                    Log.d("beerme", "SAME: " + same);
                    if (same) {
                        Toast.makeText(MapActivity.this, collection.size() + " breweries listed at this location.", Toast.LENGTH_LONG).show();
                    } else {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                cluster.getPosition(),
                                (float) Math.floor(googleMap.getCameraPosition().zoom + 1)),
                                300, null
                        );
                    }
                    return true;
                }
            });

//        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
//            @Override
//            public boolean onMarkerClick(Marker marker) {
//                Log.d("beerme", "onMarkerClick(" + marker.getTitle() + ")");
//                return false;
//            }
//        });
            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    Log.d("beerme", "getInfoWindow(" + marker.getTitle() + ")");
                    Log.d("beerme", marker.toString());
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    Log.d("beerme", "getInfoContents(" + marker.getTitle() + ")");
                    View view = LayoutInflater.from(MapActivity.this).inflate(R.layout.brewery_info_window, null);
                    TextView name = view.findViewById(R.id.name);
                    TextView address = view.findViewById(R.id.address);

                    name.setText(marker.getTitle());
                    address.setText(marker.getSnippet());

                    return view;
                }
            });

            final int statusFilter = SharedPref.read(SharedPref.Pref.STATUS_FILTER, 0);
            loadBreweryMarkers(statusFilter);
        }
    }

    private void loadBreweryMarkers(final int statusFilter) {
        Log.d("beerme", "MapActivity.loadBreweryMarkers(" + statusFilter + ")");
        BreweryListViewModel breweryListViewModel = ViewModelProviders.of(this).get(BreweryListViewModel.class);

        breweryListViewModel.getBreweryList().observe(this, new Observer<List<Brewery>>() {
            @Override
            public void onChanged(List<Brewery> breweries) {
                Log.d("beerme", "MapActivity.observe()");
                MarkerItem item;

                for (Brewery brewery : breweries) {
                    if ((brewery.status & statusFilter) != 0) {
                        item = new MarkerItem(brewery.latitude, brewery.longitude, brewery.name, brewery.address);
                        mClusterManager.addItem(item);
                    }
                }

                mClusterManager.cluster();
            }
        });
    }

    @Override
    protected void onLocationUpdated(Location location) {
//        Log.d("beerme", "MapActivity.onLocationUpdated()");
//        if (location != null && mMap != null) {
//            Log.d("beerme", location.toString());
//            LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
//        }
    }

    @Override
    public void onMapOrListChanged(@NonNull String mapOrList) {
        Log.d("beerme", "MapActivity.onMapOrListChanged(" + mapOrList + ")");
        if (mapOrList.equals(MapOrListDialog.LIST)) {
            startActivity(new Intent(this, BreweryListActivity.class));
            this.finish();
        }
    }

    @Override
    public void onStatusFilterChanged(int statusFilter) {
        Log.d("beerme", "MapActivity.onStatusFilterChanged(" + statusFilter + ")");
        mMap.clear();
        mClusterManager.clearItems();
        loadBreweryMarkers(statusFilter);
    }

    public class MarkerItem implements ClusterItem {
        private final LatLng mPosition;
        private final String mTitle;
        private final String mSnippet;

        MarkerItem(double lat, double lng, String title, String snippet) {
            mPosition = new LatLng(lat, lng);
            mTitle = title;
            mSnippet = snippet;
        }

        @Override
        public LatLng getPosition() {
            return mPosition;
        }

        @Override
        public String getTitle() {
            return mTitle;
        }

        @Override
        public String getSnippet() {
            return mSnippet;
        }

        @Override
        @NonNull
        public String toString() {
            return "MarkerItem{" +
                    "mPosition=" + mPosition +
                    ", mTitle='" + mTitle + '\'' +
                    ", mSnippet='" + mSnippet + '\'' +
                    '}';
        }
    }
}