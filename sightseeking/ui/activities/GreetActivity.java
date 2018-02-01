package com.recommender.sightseeing.sightseeking.ui.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import com.recommender.sightseeing.sightseeking.R;
import com.recommender.sightseeing.sightseeking.SightSeeker;
import com.recommender.sightseeing.sightseeking.communicationAPI.ApiActions;
import com.recommender.sightseeing.sightseeking.communicationAPI.DatabaseAPI;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


public class GreetActivity extends Activity implements LoaderCallbacks<Cursor> {
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mEmailLoginFormView;
    //  private SignInButton mPlusSignInButton;
    private View mSignOutButtons;
    private View mLoginFormView;

    @Override
    protected void onResume() {
        super.onResume();


    }
    final private  int STATIC_INTEGER_VALUE =0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        // Find the Google+ sign in button.
//        mPlusSignInButton = (SignInButton) findViewById(R.id.plus_sign_in_button);
//        if (supportsGooglePlayServices()) {
//            // Set a listener to connect the user when the G+ button is clicked.
//            mPlusSignInButton.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    signIn();
//                }
//            });
//        } else {
//            // Don't offer G+ sign in if the app's version is too low to support Google Play
//            // Services.
//            mPlusSignInButton.setVisibility(View.GONE);
//            return;
//        }

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.login);
        mEmailView.setText("foo@example.com");

        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.passkey);
        mPasswordView.setText("hello");

        Button mEmailSignInButton = (Button) findViewById(R.id.login_btn);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    attemptLogin();
                } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
//        mEmailLoginFormView = findViewById(R.id.email_login_form);
//        mSignOutButtons = findViewById(R.id.plus_sign_out_buttons);

        Button registerButton = (Button) findViewById(R.id.register_btn);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent intent = new Intent(GreetActivity.this, RegisterActivity.class);
                intent.putExtra("email", mEmailView.getText().toString());

                startActivity(intent);
            }
        });
    }



    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.

            // if every field is corrent  the login will be performed
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    public final Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
    );
    private boolean isEmailValid(String email) {
        return EMAIL_ADDRESS_PATTERN.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

//    @Override
//    protected void onPlusClientSignIn() {
//        //Set up sign out and disconnect buttons.
//        Button signOutButton = (Button) findViewById(R.id.plus_sign_out_button);
//        signOutButton.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                signOut();
//            }
//        });
//        Button disconnectButton = (Button) findViewById(R.id.plus_disconnect_button);
//        disconnectButton.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                revokeAccess();
//            }
//        });
//    }
//
//    @Override
//    protected void onPlusClientBlockingUI(boolean show) {
//        showProgress(show);
//    }
//
//    @Override
//    protected void updateConnectButtonState() {
//        //TODO: Update this logic to also handle the user logged in by email.
//        boolean connected = getPlusClient().isConnected();
//
//        mSignOutButtons.setVisibility(connected ? View.VISIBLE : View.GONE);
//       // mPlusSignInButton.setVisibility(connected ? View.GONE : View.VISIBLE);
//        mEmailLoginFormView.setVisibility(connected ? View.GONE : View.VISIBLE);
//    }

//    @Override
//    protected void onPlusClientRevokeAccess() {
//        // TODO: Access to the user's G+ account has been revoked.  Per the developer terms, delete
//        // any stored user data here.
//    }
//
//    @Override
//    protected void onPlusClientSignOut() {
//
//    }
//
//    /**
//     * Check if the device supports Google Play Services.  It's best
//     * practice to check first rather than handling this as an error case.
//     *
//     * @return whether the device supports Google Play Services
//     */
//    private boolean supportsGooglePlayServices() {
//        return GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) ==
//                ConnectionResult.SUCCESS;
//    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(GreetActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private final String PASSKEY;
        private long user_id;
        private  String[] pieces;

        UserLoginTask(String email, String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
            mEmail = email;
            mPassword = password;

            /*MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(mPassword.getBytes("UTF-8"));*/
            PASSKEY = mPassword;//md.digest().toString();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {

                user_id = Long.parseLong(DatabaseAPI.getResponse(ApiActions.LOGIN, mEmail, PASSKEY));

                if(user_id >=0) {
                    ((SightSeeker) getApplicationContext()).setUser_id(user_id);
                    return true;
                }
            } catch (InterruptedException e) {
                return false;
            } catch (Exception e) {
                e.printStackTrace();
            }


            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) { // if credentials was found in server email and password is correct
                Toast.makeText(getApplicationContext(),"Login Successful", Toast.LENGTH_LONG).show();
                finish();
                Intent intent = new Intent(GreetActivity.this,MainActivity.class);
                GreetActivity.this.startActivity(intent);
            } else { // email is ok but password is incorrect
                mPasswordView.setError(getString(R.string.error_invalid_password));
                mPasswordView.requestFocus();
            }


        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}
