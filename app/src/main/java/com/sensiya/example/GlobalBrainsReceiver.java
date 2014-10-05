package com.sensiya.example;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sensiya.brainssdk.api.BrainsIntent;

/**
 * This is a global brains receiver that is initialized in your manifest.
 * It will receive intents from SDK even if your app is not running.
 */
public class GlobalBrainsReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {

    String action = intent.getAction();
    //You got an event notification from the SDK.
    //One example of what we can do with the specific event:

    if (BrainsIntent.ACTION_LEAVING_WORK.equals(action)){
      //Your user is leaving his work place after a hard work day.
      //Wouldn't it be great to suggest him a nice pub around his work place?
    }
  }
}
