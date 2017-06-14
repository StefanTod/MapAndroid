package eu.toolegit.stefan.map;

import android.content.Context;
import android.location.Location;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("eu.toolegit.stefan.map", appContext.getPackageName());
    }

    @Test
    public void checkUsersProximity(){
        Location userLocation = new Location("UserLocation");
        userLocation.setLatitude(51.441);
        userLocation.setLongitude(5.47189);
        Location otherUsersLocation = new Location("otherUserLocation");
        otherUsersLocation.setLatitude(51.441);
        otherUsersLocation.setLongitude(5.471589);


        assertEquals(20.928571701049805,userLocation.distanceTo(otherUsersLocation),0.001);
    }
}
