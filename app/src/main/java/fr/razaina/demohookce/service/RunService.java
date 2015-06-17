package fr.razaina.demohookce.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import fr.razaina.demohookce.MainActivity;
import fr.razaina.demohookce.R;
import fr.razaina.demohookce.Task;

public class RunService extends Service {
	
	private static final String TAG = RunService.class.toString();
	/** For showing and hiding our notification. */
	NotificationManager mNM;
	/** Keeps track of all current registered clients. */
	/** Holds last value set by a client. */
	int mValue = 0;

	static final int MSG_LAUNCH_CMD = 1;
	static final int MSG_STOP_CMD = 2;
	static final int MSG_SET_VALUE = 3;

	/**
	 * Handler of incoming messages from clients.
	 */
	class IncomingHandler extends Handler {

		public void sendMessage(Messenger msg,String value){
			Bundle bundle = new Bundle();
			bundle.putString("value", value);
			Message reply = Message.obtain(null,MSG_SET_VALUE,this.hashCode(),0);
			reply.setData(bundle);

			try {
				msg.send(reply);
			} catch (RemoteException e) {
				Log.e(TAG,"Could not send message:"+e.toString());
			}
		}

		public Task findTask(String value){
			try {
				Task inst = (Task) Class.forName(value).newInstance();
				return inst;

			} catch (Exception e) {
				Log.e(TAG,"Could not instantiate class: "+value+": "+e.toString());
			}

			return null;
		}


		@Override
		public void handleMessage(Message msg) {
			String value = msg.getData().getString("value");
			final Messenger messenger = msg.replyTo;

			switch (msg.what) {
			case MSG_SET_VALUE:
				sendMessage(msg.replyTo,value);
				break;
			case MSG_LAUNCH_CMD:
                Task inst = findTask(value);
                showNotification();
				
				inst.start(getApplicationContext(), "", new Task.OnMessageListener() {
                    @Override
                    public synchronized void onMessage(String value) {
                        Log.i(TAG, "onMessage:" + value);
                        sendMessage(messenger, value);
                    }
                });
				
				break;
			case MSG_STOP_CMD:
                inst = findTask(value);

                inst.stop(new Task.OnMessageListener() {
                    @Override
                    public synchronized void onMessage(String value) {
                        Log.i(TAG, "onMessage:" + value);
                        sendMessage(messenger, value);
                    }
                });
                // Cancel the persistent notification.
                mNM.cancel(R.string.remote_service_started);
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	/**
	 * Target we publish for clients to send messages to IncomingHandler.
	 */
	final Messenger mMessenger = new Messenger(new IncomingHandler());

	@Override
	public void onCreate() {
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
	}

	@Override
	public void onDestroy() {
		// Cancel the persistent notification.
		mNM.cancel(R.string.remote_service_started);
	}

	/**
	 * When binding to the service, we return an interface to our messenger
	 * for sending messages to the service.
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	/**
	 * Show a notification while this service is running.
	 */
	private void showNotification() {
		// In this sample, we'll use the same text for the ticker and the expanded notification
		CharSequence text = getText(R.string.remote_service_started);

		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(R.drawable.ic_launcher, text,
				System.currentTimeMillis());

		// The PendingIntent to launch our activity if the user selects this notification
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, MainActivity.class), 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(this, getText(R.string.remote_service_label),
				text, contentIntent);

		// Send the notification.
		// We use a string id because it is a unique number.  We use it later to cancel.
		mNM.notify(R.string.remote_service_started, notification);

	}
}
