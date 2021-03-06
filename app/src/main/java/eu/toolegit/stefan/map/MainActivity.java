package eu.toolegit.stefan.map;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.aurelhubert.ahbottomnavigation.notification.AHNotification;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ChildEventListener{


    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Marker mCurrLocationMarker;
    private DatabaseReference mDatabase;
    private HashMap<String, Marker> mMarkers;
    private FirebaseUser mUser;
    private boolean firstTimeZoom = false;
    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotifyMgr;

    private static final int NAV_HEIGHT = 190;
    private static final int INITIAL_ZOOM = 16;
    private static final float ICON_SIZE = 32f;
    private static final float MARKER_COLOUR = BitmapDescriptorFactory.HUE_GREEN;
    private static final float MARKER_COLOUR_ME = BitmapDescriptorFactory.HUE_BLUE;

    private Location userLocation = new Location("UserLocation");
    private Location otherUsersLocation = new Location("otherUserLocation");;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AHBottomNavigation bottomNavigation = (AHBottomNavigation) findViewById(R.id.bottom_navigation);

// Create items
        AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.tab_1, R.drawable.ic_location_on_black_24dp, R.color.colorPrimary);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.tab_2, R.drawable.ic_person_black_24dp, R.color.colorPrimary);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem(R.string.tab_3, R.drawable.ic_settings_black_24dp, R.color.colorPrimary);

// Add items
        bottomNavigation.addItem(item1);
        bottomNavigation.addItem(item2);
        bottomNavigation.addItem(item3);

// Set background color
        bottomNavigation.setDefaultBackgroundColor(Color.parseColor("#D3D3D3"));


// Change colors
        bottomNavigation.setAccentColor(Color.parseColor("#F63D2B"));
        bottomNavigation.setInactiveColor(Color.parseColor("#747474"));
// Force to tint the drawable (useful for font with icon for example)
        bottomNavigation.setForceTint(false);

        bottomNavigation.setTitleState(AHBottomNavigation.TitleState.SHOW_WHEN_ACTIVE);
        bottomNavigation.setCurrentItem(0);




        // Set listeners
        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                Intent in;
                switch (position) {
                    case 0:
                        in = new Intent(getBaseContext(), MainActivity.class);
                        in.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivityIfNeeded(in, 0);
                        break;
                    case 1:
                        in = new Intent(getBaseContext(), ProfileActivity.class);
                        in.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivityIfNeeded(in, 0);
                        break;
                    case 2:
                        in = new Intent(getBaseContext(), SettingsActivity.class);
                        in.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivityIfNeeded(in, 0);
                        break;
                }
                return true;
            }
        });






        // Request location permission.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
               checkLocationPermission();
            }

        mUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mUser == null) {
            throw new NullPointerException("User not logged in");
        }

        mMarkers = new HashMap<>();

        mDatabase = FirebaseDatabase.getInstance().getReference("locations");

        mDatabase.addChildEventListener(this);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment);
        mapFragment.getMapAsync(this);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {


        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        //move map camera
        if (!firstTimeZoom) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(INITIAL_ZOOM));
            firstTimeZoom = true;
        }

        userLocation = location;
        checkNotification();

        updateLocation(mUser.getUid(), latLng, mUser.getDisplayName());

//        // Update location in the database.
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if (user != null) {
//            updateLocation(user.getUid(), latLng);
//        } else {
//            Log.e("AUTH", "USER NOT LOGGED IN");
//            // TODO: Handle appropriately.
//        }
    }


    @Override
     public void onConnected(@Nullable Bundle bundle) {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } else {
            //Log.e("PERMISSIONS", "USER DENIED LOCATION PERMISSION");
            // TODO: Handle the option of the user denying location permission.
        }
    }

    @Override
    public void onConnectionSuspended(int i) { // Do nothing
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // TODO: Handle failed.
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    /**
     * Dispatch onStop() to all fragments.  Ensure all loaders are stopped*/
    @Override
    protected void onStop() {
        super.onStop();
        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient ,this);
        }
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        addMarker(dataSnapshot);

        BaseLocation simpleLocation = dataSnapshot.getValue(BaseLocation.class);
        otherUsersLocation.setLatitude(simpleLocation.lat);
        otherUsersLocation.setLongitude(simpleLocation.longt);

        checkNotification();
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        addMarker(dataSnapshot);

        BaseLocation simpleLocation = dataSnapshot.getValue(BaseLocation.class);
        otherUsersLocation.setLatitude(simpleLocation.lat);
        otherUsersLocation.setLongitude(simpleLocation.longt);

        checkNotification();
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        String uid = dataSnapshot.getKey();
        // Check and remove any duplicates.
        //for (UserLocation loc : mMarkers.keySet()) {
        if (mMarkers.get(dataSnapshot.getKey()) != null) {
            mMarkers.get(dataSnapshot.getKey()).remove();
            //Log.e("EVENT","We made it!");
        }
        //}
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        //Log.v("DATABASE", "onChildMoved");
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        //Log.d("DATABASE", databaseError.getMessage() + databaseError.getDetails());
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }



    private void updateLocation(String userId, LatLng latLng, String name) {
        BaseLocation loc = new BaseLocation(latLng.latitude, latLng.longitude, name);
        mDatabase.child(userId).setValue(loc);

//        UserLocation loc = new UserLocation(latLng.latitude, latLng.longitude);
//        mDatabase.child("users").child(userId).child("location").setValue(loc);
    }

    private void addMarker(DataSnapshot dataSnapshot) {
        BaseLocation simpleLocation = dataSnapshot.getValue(BaseLocation.class);
        //Marker uid = mMarkers.get(dataSnapshot.getKey());
        UserLocation userLocation = new UserLocation(simpleLocation, dataSnapshot.getKey());


        // Check and remove any duplicates.
        //for (UserLocation loc : mMarkers.keySet()) {
            if (mMarkers.get(dataSnapshot.getKey()) != null) {
                mMarkers.remove(dataSnapshot.getKey()).remove();
               // Log.e("EVENT","We made it!");
            }
        //}



        //}

        MarkerOptions newMarker = userLocation.getMarkerOptions(MARKER_COLOUR);
        // Don't place markers for yourself.
        if (dataSnapshot.getKey().equals(mUser.getUid())) {
            newMarker = userLocation.getMarkerOptions(MARKER_COLOUR_ME);
            mMarkers.put(dataSnapshot.getKey(), mMap.addMarker(newMarker));

        }else{
            mMarkers.put(dataSnapshot.getKey(), mMap.addMarker(newMarker));
        }
    }

    public void checkNotification(){
        Float distance = userLocation.distanceTo(otherUsersLocation);
       // Log.e("LOCATION",("Distance between users is " + distance));

         mBuilder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_notifications_active_black_24dp)
                        .setContentTitle("User is near you!")
                        .setContentText("Someone is approximately 10 meters alway! Check who!?");

        if(distance < 10) {
            //Vibrate BZZZ
            Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 500 milliseconds
            v.vibrate(2000);
            // Sets an ID for the notification
            int mNotificationId = 001;
            // Gets an instance of the NotificationManager service
            mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            // Builds the notification and issues it.
            mNotifyMgr.notify(mNotificationId, mBuilder.build());
        }
    }
}