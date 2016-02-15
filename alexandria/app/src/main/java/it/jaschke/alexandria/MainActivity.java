package it.jaschke.alexandria;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.internal.view.SupportMenuItem;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.Arrays;

import it.jaschke.alexandria.api.Callback;


public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, Callback {

    private static final String[] INITIAL_PERMS={
            Manifest.permission.CAMERA,
    };
    private static final int INITIAL_REQUEST = 200;

    private static int SCAN_ACTIVITY = 1;

    private CharSequence title;
    public static boolean IS_TABLET = false;
    private BroadcastReceiver messageReciever;

    public static final String MESSAGE_EVENT = "MESSAGE_EVENT";
    public static final String MESSAGE_KEY = "MESSAGE_EXTRA";

    private String num = "";
    private boolean showSearch = true;
    private boolean mReturningWithResult = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IS_TABLET = isTablet();

            setContentView(R.layout.activity_main);
        

        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, new ListOfBooks())
                .commit();

        messageReciever = new MessageReciever();
        IntentFilter filter = new IntentFilter(MESSAGE_EVENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReciever,filter);

        title = getTitle();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (mReturningWithResult) {
            // Commit your transactions here.
            showSearch = false;
            invalidateOptionsMenu();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, AddBook.newInstance(num))
                    .addToBackStack("addBook")
                    .commit();
        }
        // Reset the boolean flag back to false for next time.
        mReturningWithResult = false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == SCAN_ACTIVITY) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                mReturningWithResult = true;
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
                num = data.getExtras().getString(AddBook.EXTRA_BOOK);
                // Do something with the contact here (bigger example below)
            }
        }
    }

    public void onNavigationDrawerItemSelected(int position) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment nextFragment;

        switch (position){
            default:
            case 0:
                nextFragment = new ListOfBooks();
                break;
            case 1:
                nextFragment = new AddBook();
                break;
            case 2:
                nextFragment = new About();
                break;

        }

        fragmentManager.beginTransaction()
                .replace(R.id.container, nextFragment)
                .addToBackStack((String) title)
                .commit();
    }

    public void setTitle(int titleId) {
        title = getString(titleId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        MenuItem item2 = menu.findItem(R.id.action_about);
        if(showSearch) {
            item.setVisible(true);
            item2.setVisible(true);
            SearchManager searchManager =
                    (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            SupportMenuItem searchItem = (SupportMenuItem) menu.findItem(R.id.action_search);
            SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

            searchView.setOnQueryTextListener(this);
        }else{
            item.setVisible(false);
            item2.setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            ListOfBooks fragment = (ListOfBooks) getSupportFragmentManager().findFragmentById(R.id.container);
            fragment.closeMenu();
        }catch(ClassCastException e){
            e.printStackTrace();
        }
        int id = item.getItemId();
        switch (id){
            case android.R.id.home:
                showSearch = true;
                invalidateOptionsMenu();
                getSupportFragmentManager().popBackStack();
                return true;
            case R.id.action_about:
                showSearch = false;
                invalidateOptionsMenu();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new About())
                        .addToBackStack("About")
                        .commit();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReciever);
        super.onDestroy();
    }

    @Override
    public boolean onQueryTextChange(String query) {
        try {
            ListOfBooks fragment = (ListOfBooks) getSupportFragmentManager().findFragmentById(R.id.container);
            fragment.searchLoader(query);
        }catch(ClassCastException e){}
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String arg0) {
        return false;
    }

    @Override
    public void onItemSelected(String ean) {
        Bundle args = new Bundle();
        args.putString(BookDetail.EAN_KEY, ean);

        BookDetail fragment = new BookDetail();
        fragment.setArguments(args);

        int id = R.id.container;
        if(findViewById(R.id.right_container) != null){
            id = R.id.right_container;
        }
        showSearch = false;
        invalidateOptionsMenu();
        getSupportFragmentManager().beginTransaction()
                .replace(id, fragment)
                .addToBackStack("Book Detail")
                .commit();

    }

    @Override
    public void onAddSelected(){
        showSearch = false;
        invalidateOptionsMenu();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new AddBook())
                .addToBackStack("addBook")
                .commit();
    }

    @Override
    public void onScanSelected(){
        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!canAccessCamera()) {
                requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
            } else {
                Intent i = new Intent(this, ScanActivity.class);
                startActivityForResult(i, SCAN_ACTIVITY);
            }
        }else{
            Intent i = new Intent(this, ScanActivity.class);
            startActivityForResult(i, SCAN_ACTIVITY);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case INITIAL_REQUEST:
                Intent i = new Intent(this, ScanActivity.class);
                startActivityForResult(i, SCAN_ACTIVITY);
                break;
        }
    }

    private class MessageReciever extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getStringExtra(MESSAGE_KEY)!=null){
                Toast.makeText(MainActivity.this, intent.getStringExtra(MESSAGE_KEY), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void goBack(View view){
        getSupportFragmentManager().popBackStack();
    }

    private boolean isTablet() {
        return (getApplicationContext().getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    private boolean canAccessCamera() {
        return(hasPermission(Manifest.permission.CAMERA));
    }

    @TargetApi(23)
    private boolean hasPermission(String perm) {
        return(PackageManager.PERMISSION_GRANTED == checkSelfPermission(perm));
    }

}