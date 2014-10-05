package com.sensiya.example;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

/**
 * This is a local brains receiver that is initialized in code.
 * It will receive intents from SDK as long as the app is running.
 */
public class LocalBrainsReceiver extends BroadcastReceiver {

  private final Handler mHandler;

  public LocalBrainsReceiver(Handler handler){
    mHandler = handler;
  }

  @Override
  public void onReceive(Context context, Intent intent) {

    Message message = new Message();
    message.obj = intent;
    mHandler.sendMessage(message);
  }
}
