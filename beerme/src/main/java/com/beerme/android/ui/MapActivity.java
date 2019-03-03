package com.beerme.android.ui;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
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
        StatusFilterDialog.StatusFilterListener, GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap;
    private ClusterManager<MarkerItem> mClusterManager;

    @Override
//    protected void onCreate(Bundle savedInstanceState) {
    public void onPermissionGranted() {
        setContentView(R.layout.activity_map);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);

        if (mapFragment == null) {
            throw new IllegalStateException("MapActivity.onCreate(): null mapFragment");
        }

        mapFragment.getMapAsync(this);
    }

    private Cluster<MarkerItem> clickedCluster;
    private MarkerItem clickedItem;

    @Override
    public void onMapReady(@NonNull final GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

            mClusterManager = new ClusterManager<>(this, mMap);
            mMap.setOnCameraIdleListener(mClusterManager);
            mMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());
            mMap.setOnInfoWindowClickListener(this);

            mClusterManager.getClusterMarkerCollection().setOnInfoWindowAdapter(new ClusterInfoWindowAdapter());
            mClusterManager.getMarkerCollection().setOnInfoWindowAdapter(new ItemInfoWindowAdapter(this));

            mMap.setOnMarkerClickListener(mClusterManager);

            mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MarkerItem>() {
                @Override
                public boolean onClusterClick(Cluster<MarkerItem> cluster) {
                    clickedCluster = cluster;
                    return false;
                }
            });

            mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MarkerItem>() {
                @Override
                public boolean onClusterItemClick(MarkerItem item) {
                    clickedItem = item;
                    return false;
                }
            });

            final int statusFilter = SharedPref.read(SharedPref.Pref.STATUS_FILTER, 0);
            loadBreweryMarkers(statusFilter);
        }
    }

    private void loadBreweryMarkers(final int statusFilter) {
        BreweryListViewModel breweryListViewModel = ViewModelProviders.of(this).get(BreweryListViewModel.class);

        breweryListViewModel.getBreweryList().observe(this, new Observer<List<Brewery>>() {
            @Override
            public void onChanged(List<Brewery> breweries) {
                MarkerItem item;

                for (Brewery brewery : breweries) {
                    if ((brewery.status & statusFilter) != 0) {
                        item = new MarkerItem(brewery);
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
        if (mapOrList.equals(MapOrListDialog.LIST)) {
            startActivity(new Intent(this, BreweryListActivity.class));
            this.finish();
        }
    }

    @Override
    public void onStatusFilterChanged(int statusFilter) {
        mMap.clear();
        mClusterManager.clearItems();
        loadBreweryMarkers(statusFilter);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        final Brewery brewery = clickedItem.getBrewery();
        Intent intent = new Intent(this, BreweryActivity.class);
        intent.putExtra("brewery", brewery);
        startActivity(intent);
    }

    public class MarkerItem implements ClusterItem {
        private final Brewery mBrewery;
        private final LatLng mPosition;

        MarkerItem(@NonNull final Brewery brewery) {
            mBrewery = brewery;
            mPosition = new LatLng(mBrewery.latitude, mBrewery.longitude);
        }

        public Brewery getBrewery() {
            return mBrewery;
        }

        @Override
        public LatLng getPosition() {
            return mPosition;
        }

        @Override
        public String getTitle() {
            return mBrewery.name;
        }

        @Override
        public String getSnippet() {
            return mBrewery.address;
        }

        @Override
        @NonNull
        public String toString() {
            return "MarkerItem{" +
                    "mPosition=" + getPosition() +
                    ", mTitle='" + getTitle() + '\'' +
                    ", mSnippet='" + getSnippet() + '\'' +
                    '}';
        }
    }

    class ClusterInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            java.util.Collection<MarkerItem> collection = clickedCluster.getItems();

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

            if (same) {
                // TODO: Uncluster all the markers in a "same" cluster.
                Toast.makeText(MapActivity.this, collection.size() + " breweries listed at this location.", Toast.LENGTH_LONG).show();
            } else {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        clickedCluster.getPosition(),
                        (float) Math.floor(mMap.getCameraPosition().zoom + 1)),
                        300, null
                );
            }
            return null;
        }
    }

    class ItemInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private final Activity mActivity;

        ItemInfoWindowAdapter(@NonNull Activity activity) {
            mActivity = activity;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            View view = LayoutInflater.from(mActivity).inflate(R.layout.brewery_info_window, null);
            TextView nameView = view.findViewById(R.id.name_view);
            TextView addressView = view.findViewById(R.id.address_view);
            TextView hoursView = view.findViewById(R.id.hours_view);
            LinearLayout servicesView = view.findViewById(R.id.services_layout);

            nameView.setText(marker.getTitle());
            addressView.setText(marker.getSnippet());

            Brewery brewery = clickedItem.getBrewery();

            String hours = brewery.hours;
            if (TextUtils.isEmpty(hours)) {
                hoursView.setVisibility(View.GONE);
            } else {
                hoursView.setVisibility(View.VISIBLE);
                hoursView.setText(clickedItem.getBrewery().hours);
            }

            brewery.showServiceIcons(mActivity, servicesView);

            return view;
        }
    }
}