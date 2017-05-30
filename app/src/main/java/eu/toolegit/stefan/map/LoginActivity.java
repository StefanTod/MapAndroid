package eu.toolegit.stefan.map;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    /**
     * Response code for signing in.
     */
    private static final int RC_SIGN_IN = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Assign the FirebaseAuth
            /*
      Firebase authentication instance that keeps track
      of the current user status and more.
     */
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // Check if the user is signed in yet.
        if (auth.getCurrentUser() != null) {
            // User is already signed in.
            Log.v("AUTH", "Logged in as " + auth.getCurrentUser().getEmail());

            // TODO: Check if user profile data is complete, if not, start the details activity anyway.
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        } else {
//            // User is not yet signed in, start the FirebaseUI intent by calling
//            // createSignInIntentBuilder. The response of this intent is handled
//            // in .onActivityResult() with the request code RC_SIGN_IN.
//            startActivityForResult(
//                    // Get an instance of AuthUI based on the default app
//                    AuthUI.getInstance().createSignInIntentBuilder().build(),
//                    RC_SIGN_IN);

            startActivityForResult(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setProviders(Arrays.asList(
                    new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build()))
                    .build(), RC_SIGN_IN);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == ResultCodes.OK) {
                Intent i = new Intent(this, MainActivity.class);
                startActivity(i);
                finish();
                return;
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    //showSnackbar(R.string.sign_in_cancelled);
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    //showSnackbar(R.string.no_internet_connection);
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    //showSnackbar(R.string.unknown_error);
                    return;
                }
            }

            //showSnackbar(R.string.unknown_sign_in_response);
            return;
        }
    }
}
