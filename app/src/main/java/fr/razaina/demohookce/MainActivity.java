package fr.razaina.demohookce;

import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import fr.razaina.demohookce.service.RunServiceClient;


public class MainActivity extends ActionBarActivity {

    Button launchBtn;
    TextView tvConsole;
    RunServiceClient service;
    public boolean isEnabled;
    private SharedPreferences settings;
    public static final String PREFS_NAME = "HookCE";

    public MainActivity() { this.isEnabled = false;}

    public boolean isEnabled() { return this.settings.getBoolean("isEnabled", false);}

    private void setStatus(boolean value){
        SharedPreferences.Editor editor = this.settings.edit();
        editor.putBoolean("isEnabled", value);
        editor.commit();
    }

    private void startAttack(){
        //MenuItem status = this.menu.findItem(R.id.action_status);
        //status.setTitle(R.string.action_status_disabled);

        setStatus(true);
        tvConsole.setText("------ Starting HookCE ------");
        service.startTask("fr.razaina.demohookce.HookCE");
    }

    private void stopAttack(){
        tvConsole.setText("");
        setStatus(false);
        service.stopTask("fr.razaina.demohookce.HookCE");
    }

    private void launchHookCE()
    {
        if (isEnabled()) {
            stopAttack();
        } else {
            startAttack();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.settings = getSharedPreferences(PREFS_NAME, 0);
        tvConsole = (TextView)findViewById(R.id.tvConsole);
        service = new RunServiceClient(getApplicationContext(), tvConsole);
        service.doBindService();
        launchBtn = (Button)findViewById(R.id.launchBtn);
        launchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.launchHookCE();
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        service.doUnbindService();
    }
}
