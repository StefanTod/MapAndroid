package eu.toolegit.stefan.map;

/**
 * Created by Stefan on 30-May-17.
 */

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class BaseLocation {

    public Double lat;
    public Double longt;
    public String name;

    public BaseLocation(){
        //
    }

    public BaseLocation(Double lat, Double longt, String name){
        this.lat = lat;
        this.longt = longt;
        this.name = name;
    }
}
