package fr.razaina.demohookce;

import android.content.Context;
import android.util.Log;

public class Task {
	private static final String TAG=Task.class.toString();
	
	public interface OnMessageListener{
		public abstract void onMessage(String value);
	}
	
	private static String name;
	private Process mProcess=null;
	private boolean mInit=false;
	
	public static String getName() {
		return name;
	}

	public static void setName(String name) {
		Task.name = name;
	}
	
	public void start(Context ctx,Object arguments,OnMessageListener listener){
		if(mProcess!=null){
			Log.e(TAG,"Process already started");
			return;
		}
		
		if(!mInit){
			init(ctx,listener);
		}
		
		return;
	}
	
	public int stop(OnMessageListener listener){

		return 0;
	}	
	
	public boolean isMultiInstantiable()
	{
		return false;
	}

	public void init(Context ctx,OnMessageListener listener){
		mInit=true;
		return;
	}

	public boolean isInitDone(){
		return mInit;
	}
}
