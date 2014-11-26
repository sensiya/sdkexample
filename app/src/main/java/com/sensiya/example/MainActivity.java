package com.sensiya.example;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sensiya.brainssdk.api.BrainsAPI;
import com.sensiya.brainssdk.api.BrainsAPICallback;
import com.sensiya.brainssdk.api.BrainsIntent;
import com.sensiya.brainssdk.api.UserData;

import java.util.Locale;

public class MainActivity extends Activity {

  private static final String TAG = "MainActivity";

  private BrainsIntentHandler mHandler = new BrainsIntentHandler();
  private LocalBrainsReceiver mLocalBrainsReceiver = new LocalBrainsReceiver(mHandler);

  private TextView mBrainsIntentTextView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mBrainsIntentTextView = (TextView) findViewById(R.id.txt_intent_info);

    BrainsAPI.start(getApplicationContext(), new BrainsAPICallback() {
      @Override
      public void onConnected() {
        Log.d(TAG, "SDK connected");
      }

      @Override
      public void onError(Error error) {
        Log.e(TAG, "Error in SDK. code: " + error.getCode() + " message: " + error.getMessage());
      }

      @Override
      public void onDisconnected() {
        Log.d(TAG, "SDK disconnected");
      }

      @Override
      public void onUserDataReady(final UserData user) {
        Log.d(TAG, "User data ready");
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            populateGeneralInfo(user);
            checkHomeAndWorkLocation();
          }
        });
      }
    });

    registerLocalBrainsReceiver();
  }

  private void registerLocalBrainsReceiver() {

    IntentFilter filter = new IntentFilter();
    filter.addAction(BrainsIntent.ACTION_USER_ACTIVITY_CHANGED);

    registerReceiver(mLocalBrainsReceiver, filter);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    unregisterReceiver(mLocalBrainsReceiver);
  }

  private void populateGeneralInfo(UserData userData){

    StringBuilder infoText = new StringBuilder();
    infoText.append(String.format("Gender: %s Confidence: %.2f \r\n", userData.getGenderString(userData.getGender()), userData.getGenderConfidence()));
    infoText.append(String.format("Age range: %s Confidence: %.2f \r\n", userData.getAgeRange(), userData.getAgeRangeConfidence()));
    infoText.append(String.format("Estimated age: %s \r\n", userData.getEstimatedAge()));

    TextView generalInfoTV = (TextView) findViewById(R.id.txt_general_info);
    generalInfoTV.setText(infoText.toString());
  }

  private void checkHomeAndWorkLocation(){
    Button showHomeButton = (Button) findViewById(R.id.btn_home_location);
    Location homeLocation = BrainsAPI.LocationServices.getHomeLocation();
    boolean hasHomeLocation = !(homeLocation == null);
    showHomeButton.setEnabled(hasHomeLocation);

    Button showWorkButton = (Button) findViewById(R.id.btn_work_location);
    Location workLocation = BrainsAPI.LocationServices.getWorkLocation();
    boolean hasWorkLocation = !(workLocation == null);
    showWorkButton.setEnabled(hasWorkLocation);

    if(!hasHomeLocation || !hasWorkLocation){
      findViewById(R.id.txt_identifying_location).setVisibility(View.VISIBLE);
    }
  }

  public void showHomeLocation(View view) {
    showLocation(BrainsAPI.LocationServices.getHomeLocation());
  }

  public void showWorkLocation(View view) {
    showLocation(BrainsAPI.LocationServices.getWorkLocation());
  }

  private void showLocation(Location locationData){
    try {
      if (locationData == null){
        Toast.makeText(this, "Location not present", Toast.LENGTH_SHORT).show();
        return;
      }

      String uri = String.format(Locale.ENGLISH, "geo:%f,%f", locationData.getLatitude(), locationData.getLongitude());
      Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
      startActivity(intent);
    } catch (Exception e) {
      Log.e(TAG, "Failed to show location");
    }
  }

  private class BrainsIntentHandler extends Handler {

    @Override
    public void handleMessage(Message msg) {

      Intent intent = (Intent) msg.obj;

      if (BrainsIntent.ACTION_USER_ACTIVITY_CHANGED.equals(intent.getAction())) {
        handleUserActivityIntent(intent);
      } else {
        showIntentArrived(intent);
      }
    }

    private void showIntentArrived(Intent intent) {
      mBrainsIntentTextView.setText(String.format("%s", getCleanActionString(intent.getAction())));
    }

    private void handleUserActivityIntent(Intent intent){

      int activityType = intent.getIntExtra(BrainsIntent.EXTRA_ACTIVITY_TYPE, BrainsIntent.ACTIVITY_UNKNOWN);
      int activityConfidence = intent.getIntExtra(BrainsIntent.EXTRA_ACTIVITY_CONFIDENCE, 0);
      String activityTypeString = getActivityString(activityType);

      mBrainsIntentTextView.setText(String.format("User activity changed. \r\n %s (%3d%%)", activityTypeString, activityConfidence));
    }

    private String getCleanActionString(String action){
      String[] split = action.split("\\.");
      if (split != null && split.length > 0) {
        return split[split.length - 1];
      }

      return "unknown action";
    }

    private String getActivityString(int activityType){
      switch (activityType){
        case BrainsIntent.ACTIVITY_STILL:
          return "Still";

        case BrainsIntent.ACTIVITY_WALKING:
          return "Walking";

        case BrainsIntent.ACTIVITY_RUNNING:
          return "Running";

        case BrainsIntent.ACTIVITY_JUMPING:
          return "Jumping";

        case BrainsIntent.ACTIVITY_CYCLING:
          return "Cycling";

        case BrainsIntent.ACTIVITY_DRIVING:
          return "Driving";

        default:
          return "Unknown";
      }
    }
  }
}
