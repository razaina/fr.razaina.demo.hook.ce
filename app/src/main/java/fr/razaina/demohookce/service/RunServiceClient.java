package fr.razaina.demohookce.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;

import fr.razaina.demohookce.R;


public class RunServiceClient {
    private static final String TAG = RunServiceClient.class.toString();
    private Context context=null;
    private TextView console=null;

    public RunServiceClient(Context ctx,TextView console){
        context=ctx;
        this.console=console;
    }

    public Message generateMessage(String name,int id){
        Message msg = null;
        Bundle bundle = new Bundle();
        msg = Message.obtain(null,id,this.hashCode(),0);
        bundle.putString("value",name);
        msg.setData(bundle);
        msg.replyTo=mMessenger;
        return msg;
    }

    public void stopTask(String name){
        Log.d(TAG, "Stop Analyzing");
        Message msg=generateMessage(name,RunService.MSG_STOP_CMD);
        try {

            msg.replyTo=mMessenger;
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void startTask(String name){
        Log.d(TAG, "Starting HookCE");
        Message msg=generateMessage(name,RunService.MSG_LAUNCH_CMD);
        try {

            msg.replyTo=mMessenger;
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** Messenger for communicating with service. */
    Messenger mService = null;
    /** Flag indicating whether we have called bind on the service. */
    boolean mIsBound;

    /**
     * Handler of incoming messages from service.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RunService.MSG_SET_VALUE:
                    console.setText(console.getText()+"\n" + msg.getData().getString("value"));
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

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            mService = new Messenger(service);
            Log.d(TAG,context.getResources().getString(R.string.remote_service_connected));
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            Log.d(TAG,context.getResources().getString(R.string.remote_service_disconnected));
        }
    };

    public void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        context.getApplicationContext().bindService(new Intent(context,
                RunService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    public void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            context.getApplicationContext().unbindService(mConnection);
            mIsBound = false;
        }
    }
}
