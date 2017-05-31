package eu.toolegit.stefan.map;

/**
 * Created by Stefan on 30-May-17.
 */

import com.firebase.ui.auth.ui.User;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class UserLocation extends BaseLocation {

    public String uid;

    public UserLocation(Double lat, Double longt, String name, String uid) {
        super(lat, longt, name);
        this.uid = uid;
    }

    public UserLocation(BaseLocation baseLocation, String uid) {
        super(baseLocation.lat, baseLocation.longt, baseLocation.name);
        this.uid = uid;
    }

    public LatLng getLatLng() {
        return new LatLng(this.lat, this.longt);
    }

    public MarkerOptions getMarkerOptions(float markerColour) {
        // Create marker
        MarkerOptions newMarker = new MarkerOptions();
        newMarker.position(getLatLng());
        newMarker.title(name);
        newMarker.icon(BitmapDescriptorFactory.defaultMarker(markerColour));

        return newMarker;
    }

    @Override
    public String toString() {
        return "name: " + name +
                "\nlat: " + lat +
                "\nlng: " + longt;
    }
}
