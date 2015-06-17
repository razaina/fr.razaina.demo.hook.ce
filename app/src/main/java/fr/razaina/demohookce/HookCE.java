package fr.razaina.demohookce;

import android.content.Context;
import android.util.Log;

import java.io.InputStream;


public class HookCE extends Task {
	private static String TAG = HookCE.class.toString();

	public int init(Context ctx){

		InputStream in = ctx.getResources().openRawResource(R.raw.libdemohookce);
		RootHelpers.dropBinary(in, ctx.getFilesDir().getAbsolutePath() + "/", "libdemohookce.so");
		
		in = ctx.getResources().openRawResource(R.raw.needle);
        RootHelpers.dropBinary(in, ctx.getFilesDir().getAbsolutePath() + "/", "needle");

        in = ctx.getResources().openRawResource(R.raw.busybox);
        RootHelpers.dropBinary(in, ctx.getFilesDir().getAbsolutePath() + "/", "busybox");

		return 0;
	}
	
	@Override
	public void start(Context ctx,Object args,Task.OnMessageListener listener) {
        init(ctx);
		Log.i(TAG,"Spawning HookCE process: ["+ctx.getFilesDir().getAbsolutePath()+"]");
		
		String[] cmd = {
				"echo 'killing services'",
				"kill -9 $(./busybox pidof needle)",
                "cd "+ctx.getFilesDir().getAbsolutePath(),
                "./needle -L caisseepargne:libdemohookce.so -vfp $(./busybox pidof zygote)",
				"exit"
				};
		try {
			RootHelpers.runAsRootAsync(cmd,listener);
		} catch (Exception e) {
			Log.d(TAG,e.toString());
		}
	}

    @Override
    public int stop(Task.OnMessageListener listener){
        Log.i(TAG,"Stopping HookCE");

        String[] cmd = {
                "echo 'killing services' ",
                "kill -9 $(./busybox pidof needle)",
                "echo 'done' ",
                "exit"
        };
        try {
            RootHelpers.runAsRootAsync(cmd,listener);
        } catch (Exception e) {
            Log.d(TAG,e.toString());
            return 1;
        }
        return 0;
    }
}
