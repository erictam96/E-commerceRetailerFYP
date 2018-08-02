package com.ecommerce.merchant.fypproject;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.StrictMode;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ecommerce.merchant.fypproject.adapter.OnItemClick;
import com.ecommerce.merchant.fypproject.adapter.PackItem;
import com.ecommerce.merchant.fypproject.adapter.PackItemAdapter;
import com.ecommerce.merchant.fypproject.adapter.Retailer;
import com.ecommerce.merchant.fypproject.adapter.UploadProcess;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import io.fabric.sdk.android.Fabric;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements  NavigationView.OnNavigationItemSelectedListener,OnItemClick{

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private FirebaseAuth firebaseAuth;
    private TextView orderConfirmNotf,cartNotf;
    private TextView orderPackNotf;
    private TextView drawerShopName;
    private TextView drawerOwnerName;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManagerOfrecyclerView;
    private final String PHPURL="http://ecommercefyp.000webhostapp.com/retailer/manage_retailer_product.php";
    private List<PackItem> packProductList;
    private PackItemAdapter packItemAdapter;
    private RequestQueue requestQueue ;
    private ProgressDialog progressDialog,updatedialog,canceldialog;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private NavigationView navigationView;
    private RelativeLayout drawerHeaderBackground;
    private ImageView drawerProfileImg;
    private String uid;
    private final String HTTP_JSON_URL = "http://ecommercefyp.000webhostapp.com/retailer/read_user_profile.php";
    private Button defaultButton;
    private  boolean doneCount;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        Fabric.with(this, new Answers());

        GetFirebaseAuth();
        Toolbar contenttoolbar = findViewById(R.id.contenttoolbar);
        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout1);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.orange);
        setSupportActionBar(contenttoolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getResources().getString(R.string.ordertopack));
        setupNavigation();

        orderConfirmNotf= navigationView.getMenu().findItem(R.id.orderToConfirmMenuBtn).getActionView().findViewById(R.id.notifyCount);
        cartNotf=navigationView.getMenu().findItem(R.id.boostCartBtn).getActionView().findViewById(R.id.notifyCount);
        orderPackNotf= navigationView.getMenu().findItem(R.id.orderToPackMenuBtn).getActionView().findViewById(R.id.notifyCount);
        drawerHeaderBackground=navigationView.getHeaderView(0).findViewById(R.id.drawerHeaderBackground);
        drawerOwnerName=navigationView.getHeaderView(0).findViewById(R.id.drawerRetailerName);
        drawerShopName=navigationView.getHeaderView(0).findViewById(R.id.drawerRetailerShopName);
        drawerProfileImg=navigationView.getHeaderView(0).findViewById(R.id.drawerProfilePic);



       // orderConfirmNotf=buf.findViewById(R.id.notifyCount);
        orderConfirmNotf.setVisibility(View.INVISIBLE);
        orderPackNotf.setVisibility(View.INVISIBLE);
        cartNotf.setVisibility(View.INVISIBLE);

//
//        Glide.with(this)
//                .load("http://ecommercefyp.000webhostapp.com/retailer/images/MikoWongprofile2018-04-06-18-31-39.jpg") // image url
//                .placeholder(R.drawable.photo) // any placeholder to load at start
//                .error(R.drawable.photo)  // any image in case of error
//                .override(1000, 1000) // resizing
//                .centerCrop().animate( android.R.anim.fade_in )
//                .into(drawerProfileImg);
//        roundLoadImage("http://ecommercefyp.000webhostapp.com/retailer/images/MikoWongcover2018-04-06-18-31-39.jpg");

//https://images.pexels.com/photos/207142/pexels-photo-207142.jpeg?auto=compress&cs=tinysrgb&h=350

//http://ecommercefyp.000webhostapp.com/retailer/images/MikoWongcover2018-04-06-18-31-39.jpg

        recyclerView=findViewById(R.id.packItemRecycle);
        TextView txtview = findViewById(R.id.textView7);

        recyclerView.setHasFixedSize(true);

        layoutManagerOfrecyclerView = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManagerOfrecyclerView);

        progressDialog=new ProgressDialog(this);
        progressDialog.setCancelable(true);
        progressDialog.setTitle(getResources().getString(R.string.loading));
        progressDialog.setMessage(getResources().getString(R.string.prepareCustomerOrder));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);


        updatedialog=new ProgressDialog(this);
        updatedialog.setCancelable(false);
        updatedialog.setTitle(getResources().getString(R.string.updating));
        updatedialog.setMessage(getResources().getString(R.string.updatingOrderList));
        updatedialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        canceldialog=new ProgressDialog(this);
        canceldialog.setCancelable(false);
        canceldialog.setTitle(getResources().getString(R.string.cancelOrder));
        canceldialog.setMessage(getResources().getString(R.string.cancelorderMsg));
        canceldialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
              //  progressDialog.show();
                //if(getTarcUserOnlyAlertStatus()){
                    RETAILER_JSON_HTTP_CALL();
                    JSON_HTTP_CALL();
             //   }
               // JSON_HTTP_CALL();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        if(getTarcUserOnlyAlertStatus()){
            RETAILER_JSON_HTTP_CALL();
            JSON_HTTP_CALL();
        }
        showTarcUserOnlyNotice();
    }


    @Override
    public void onClick(String value, final String prodvar, final String prodcode, final String rid) {
        if(value.equalsIgnoreCase("updating")){
            updatedialog.show();
        }
        if(value.equalsIgnoreCase("done update")){
            JSON_HTTP_CALL();
            updatedialog.dismiss();
            Toast.makeText(this,getResources().getString(R.string.doneUpdateOrder),Toast.LENGTH_SHORT).show();
        }
        if(value.equalsIgnoreCase("cancel")){
            new AlertDialog.Builder(this)
                    .setMessage(getResources().getString(R.string.cancelOrderWarning))
                    .setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            JSON_CANCEL(prodvar,prodcode,rid);
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.no), null).show();

        }

    }



    private void setupNavigation() {
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mDrawerLayout = findViewById(R.id.drawerLayout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                Notification_JSON_HTTP_CALL();
               // Toast.makeText(MainActivity.this,"opened",Toast.LENGTH_SHORT).show();
            }
        };

        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }
    @Override
    public boolean onNavigationItemSelected(final MenuItem item) {
        switch (item.getItemId()){
            case R.id.orderToConfirmMenuBtn:
                mDrawerLayout.closeDrawers();
                Intent orderConfirmIntent=new Intent(MainActivity.this,OrderConfirmActivity.class);
                orderConfirmIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(orderConfirmIntent);
                return true;

            case R.id.orderToPackMenuBtn:
                mDrawerLayout.closeDrawers();
                JSON_HTTP_CALL();
                return true;
            case  R.id.registerNewProductsMenuBtn:
                mDrawerLayout.closeDrawers();
                Intent intent=new Intent(MainActivity.this,RegisterProductActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            case R.id.manageProductsMenuBtn:
                mDrawerLayout.closeDrawers();
                Intent myIntent6 = new Intent(MainActivity.this,MyPostActivity.class);
                myIntent6.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(myIntent6);
                return true;

            case R.id.viewSalesReportMenuBtn:
                mDrawerLayout.closeDrawers();
                Intent intentSalesReport=new Intent(MainActivity.this,DailyReportActivity.class);
                intentSalesReport.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intentSalesReport);
                return true;
            case R.id.settingMenuBtn:
                mDrawerLayout.closeDrawers();
                Intent intentSetting=new Intent(MainActivity.this,SettingActivity.class);
                intentSetting.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intentSetting);
                return true;

            case R.id.aboutUsMenuBtn:
                mDrawerLayout.closeDrawers();
                Intent intentAboutUs=new Intent(MainActivity.this,AboutUs.class);
                intentAboutUs.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intentAboutUs);
                return true;

            case R.id.profileMenuBtn:
                mDrawerLayout.closeDrawers();
                Intent intentProfile=new Intent(MainActivity.this,ProfileActivity.class);
                intentProfile.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(intentProfile);
                return true;

            case R.id.boostProductsBtn:
                mDrawerLayout.closeDrawers();
                Intent intentBoost=new Intent(MainActivity.this,BoostActivity.class);
                intentBoost.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(intentBoost);
                return true;

            case R.id.boostCartBtn:
                mDrawerLayout.closeDrawers();
                Intent intentBoostCart=new Intent(MainActivity.this,BoostCartActivity.class);
                intentBoostCart.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(intentBoostCart);
                return true;

            case R.id.deliveryStatusBtn:
                mDrawerLayout.closeDrawers();
                Intent intentDeliveryStatus=new Intent(MainActivity.this,DeliveryStatusActivity.class);
                intentDeliveryStatus.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(intentDeliveryStatus);
                return true;

            case R.id.viewCancelReportBtn:
                mDrawerLayout.closeDrawers();
                Intent intentCancelReport=new Intent(MainActivity.this,CancelReportActivity.class);
                intentCancelReport.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(intentCancelReport);
                return true;

        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences.Editor editor = getSharedPreferences("STATUS", MODE_PRIVATE).edit();
        editor.putBoolean("PACKSTATUS", false);
        editor.apply();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.notification_icon) {
            Intent myIntent5 = new Intent(MainActivity.this, NotificationActivity.class);
            startActivity(myIntent5);
        }
        else if (item.getItemId()==R.id.chat_icon){
            Intent myIntent5 = new Intent(MainActivity.this, ChatListActivity.class);
            startActivity(myIntent5);
        }
        return mToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_icon, menu);
        getMenuInflater().inflate(R.menu.notification_icon, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //retrieve all retailer's orders
    private void JSON_HTTP_CALL() {
        progressDialog.show();

        //finalChkOutBtn.setEnabled(false);
        packProductList=new ArrayList<>();
        packItemAdapter=null;
        StringRequest RequestOfJSonArray = new StringRequest(Request.Method.POST, PHPURL,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            ParseJSonResponse(response);
                        } catch (JSONException e) {
                            Crashlytics.logException(e);
                            // handle your exception here!
                            e.printStackTrace();
                        }
                        Log.d("Response", response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Log.d("Error.Response1", error.toString());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("fetchOrder", uid);
                return params;
            }
        };


        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(RequestOfJSonArray);
    }

    private void ParseJSonResponse(String array) throws JSONException {
        // product = new Product();
        JSONArray jarr = new JSONArray(array);//lv 1 array

        for(int a=0;a<jarr.length();a++){
            PackItem x=new PackItem();
            JSONObject json;
            json=jarr.getJSONObject(a);

            x.setOrderId(json.getString("orderid"));
            x.setItemName(json.getString("itemname"));
            x.setVariant(json.getString("variant"));
            x.setQty(json.getString("qty"));
            x.setDate(json.getString("date"));
            x.setProdcode(json.getString("prodcode"));

            packProductList.add(x);
        }

        packItemAdapter= new PackItemAdapter( packProductList, MainActivity.this,this);
        recyclerView.setAdapter(packItemAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });
        progressDialog.dismiss();
    }
    //Cancel the order
    private void JSON_CANCEL(final String prodvar, final String prodcode, final String rid){
        class AsyncTaskUploadClass extends AsyncTask<Void,Void,String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                canceldialog.show();
            }

            @Override
            protected void onPostExecute(String response) {
                super.onPostExecute(response);
                Log.e("Response",response);

                JSON_HTTP_CALL();
                canceldialog.dismiss();
                Toast.makeText(MainActivity.this,getResources().getString(R.string.doneCancel),Toast.LENGTH_SHORT).show();

            }

            @Override
            protected String doInBackground(Void... params) {
                UploadProcess imageProcessClass = new UploadProcess();

                HashMap<String,String> HashMapParams = new HashMap<>();

                HashMapParams.put("prodvariant",prodvar);
                HashMapParams.put("prodcode",prodcode);
                HashMapParams.put("retailerid",rid);

                return imageProcessClass.HttpRequest(PHPURL, HashMapParams);
            }
        }
        AsyncTaskUploadClass AsyncTaskUploadClass = new AsyncTaskUploadClass();
        AsyncTaskUploadClass.execute();

    }

    private void roundLoadImage(String picURL){

                    Bitmap imgBitmap=getBitmapfromUrl(picURL);

                    int value;
                    if(Objects.requireNonNull(imgBitmap).getHeight()<=imgBitmap.getWidth()){
                        value = imgBitmap.getHeight();
                    }else{
                        value = imgBitmap.getWidth();
                    }
                    Bitmap finalBitmap;
                    finalBitmap= Bitmap.createBitmap(imgBitmap,0,0,value,value);

                    Bitmap lastBitmap;
                    lastBitmap = Bitmap.createScaledBitmap(finalBitmap,1600,500,true);
                    //Bitmap cropImg=Bitmap.createBitmap(imgBitmap,0,0,imgBitmap.getHeight(),imgBitmap.getWidth());
                    Drawable d = new BitmapDrawable(getResources(), lastBitmap);

                    drawerHeaderBackground.setBackground(d);





    }

    private Bitmap getBitmapfromUrl(String imageUrl) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try
        {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);

        } catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;

        }
    }

    private void RETAILER_JSON_HTTP_CALL() {
        //RetailerDataList = new ArrayList<>();
        String HTTP_JSON_URL = "http://ecommercefyp.000webhostapp.com/retailer/read_user_profile.php";
        StringRequest RequestOfJSonArray = new StringRequest(Request.Method.POST, HTTP_JSON_URL,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            RETAILER_ParseJSonResponse(response);
                        } catch (JSONException e) {
                            Crashlytics.logException(e);
                            // handle your exception here!
                            e.printStackTrace();
                            //progressBar.setVisibility(View.GONE);
                            //txtnoresult.setVisibility(View.VISIBLE);
                        }
                        Log.d("Response ", response);

                       // dialog.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Log.d("Error.Response1", error.toString());
                    }
                }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("retailerID", uid);//userid to send to php
                return params;
            }
        };


        requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(RequestOfJSonArray);
    }

    private void RETAILER_ParseJSonResponse(String array) throws JSONException {
Log.e("retailer return",array);
       // String processedString=array.substring(array.indexOf("["),array.lastIndexOf("]")+1);
        JSONArray jarr = new JSONArray(array);
        for(int i = 0; i<jarr.length(); i++) {

            Retailer retailer = new Retailer();

            JSONObject json;
            try {

                json = jarr.getJSONObject(i);

                drawerShopName.setText(json.getString("RShopName"));
                drawerOwnerName.setText(json.getString("ROwnerName"));
                Glide.with(this)
                        .load(json.getString("profilePicURL")) // image url
                        .transition(GenericTransitionOptions.with( android.R.anim.fade_in) )
                        .apply(new RequestOptions()
                        .placeholder(R.drawable.photo) // any placeholder to load at start
                        .error(R.drawable.photo)  // any image in case of error
                        .override(1000, 1000) // resizing
                        .centerCrop())

                        .into(drawerProfileImg);
                roundLoadImage(json.getString("coverPicURL"));

            } catch (JSONException e) {
                Crashlytics.logException(e);
                // handle your exception here!
                e.printStackTrace();
            }

        }
        //fetchUserData(RetailerDataList);
    }

    private void Notification_JSON_HTTP_CALL() {

        if (firebaseAuth.getCurrentUser()== null) {
            finish();
            startActivity(new Intent(getApplicationContext(), SplashActivity.class));
            //go to MAIN activity
        }

        //progressDialog.show();
        class AsyncTaskUploadClass extends AsyncTask<Void,Void,String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                orderConfirmNotf.setText("...");
                orderConfirmNotf.setVisibility(View.VISIBLE);
                cartNotf.setText("...");
                cartNotf.setVisibility(View.VISIBLE);
                orderPackNotf.setText("...");
                orderPackNotf.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(String response) {
                super.onPostExecute(response);
                try {
                    if(response.equalsIgnoreCase("[]")){
                        orderConfirmNotf.setVisibility(View.INVISIBLE);
                        orderPackNotf.setVisibility(View.INVISIBLE);
                        cartNotf.setVisibility(View.INVISIBLE);
                    }else{
                        Notification_ParseJSonResponse(response);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("Response notf count", response);
            }

            @Override
            protected String doInBackground(Void... params) {
                UploadProcess ProcessClass = new UploadProcess();

                HashMap<String,String> HashMapParams = new HashMap<>();
                //params.put("custID", firebaseAuth.getCurrentUser().getUid().toString());
                HashMapParams.put("getNotfCounter",uid);
                return ProcessClass.HttpRequest(PHPURL, HashMapParams);
            }
        }
        AsyncTaskUploadClass AsyncTaskUploadClass = new AsyncTaskUploadClass();
        AsyncTaskUploadClass.execute();

    }

    private void GetFirebaseAuth(){
        firebaseAuth=FirebaseAuth.getInstance();//get firebase object
        if(firebaseAuth.getCurrentUser()==null){
            Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
            try {
                FirebaseInstanceId.getInstance().deleteInstanceId();
            } catch (IOException e) {
                e.printStackTrace();
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            Toast.makeText(this,getResources().getString(R.string.sessionexp),Toast.LENGTH_LONG).show();
        }else uid = firebaseAuth.getCurrentUser().getUid();
    }

    //get response string and set into recyclerView
    private void Notification_ParseJSonResponse(String array) throws JSONException {

        JSONArray jarr = new JSONArray(array);//lv 1 array

        JSONObject json;
        json = jarr.getJSONObject(0);

        if(Integer.parseInt(json.getString("ordconfirmnum"))>0){
            orderConfirmNotf.setVisibility(View.VISIBLE);
            orderConfirmNotf.setText(json.getString("ordconfirmnum"));
        }else{
            orderConfirmNotf.setVisibility(View.INVISIBLE);
        }
        if(Integer.parseInt(json.getString("ordpacknum"))>0){
            orderPackNotf.setVisibility(View.VISIBLE);
            orderPackNotf.setText(json.getString("ordpacknum"));
        }else{
            orderPackNotf.setVisibility(View.INVISIBLE);
        }

        if(Integer.parseInt(json.getString("cartnum"))>0){
            cartNotf.setVisibility(View.VISIBLE);
            cartNotf.setText(json.getString("cartnum"));
        }else{
            cartNotf.setVisibility(View.INVISIBLE);
        }
    }


    private void showTarcUserOnlyNotice(){
        View mView = getLayoutInflater().inflate(R.layout.alert_taruseronly, null);
        final CheckBox mCheckBox = mView.findViewById(R.id.chkTarcUserOnly);
        TextView TandC=mView.findViewById(R.id.TandClink);
        TextView privacyPolicy=mView.findViewById(R.id.privacyPolicyLink);
        AlertDialog mBuilder = new AlertDialog.Builder(this)
                .setIcon(R.drawable.error)
                .setTitle(getResources().getString(R.string.importantNotice))
                .setMessage(getResources().getString(R.string.importantNoticeMsg))
                .setCancelable(false)
                .setView(mView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        if(!getTarcUserOnlyAlertStatus()){
                            finish();
                        }
                    }
                })
                .create();
        mBuilder.setOnShowListener(new DialogInterface.OnShowListener() {
            private static final int AUTO_DISMISS_MILLIS = 8000;

            @Override
            public void onShow(final DialogInterface dialog) {
                defaultButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                defaultButton.setEnabled(false);
                final CharSequence positiveButtonText = defaultButton.getText();
                new CountDownTimer(AUTO_DISMISS_MILLIS, 100) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        defaultButton.setText(String.format(
                                Locale.getDefault(), "%s (%d)",
                                positiveButtonText,
                                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)  //add one so it never displays zero

                        ));
                        doneCount=false;
                    }

                    @Override
                    public void onFinish() {
                        if (((AlertDialog) dialog).isShowing()) {
                            defaultButton.setText("OK");
                            doneCount=true;
                            //defaultButton.setEnabled(true);
                        }
                        if(mCheckBox.isChecked()&&doneCount){
                            defaultButton.setEnabled(true);
                            storeDialogStatus(true);
                        }
                    }
                }.start();
            }
        });
        mBuilder.show();

        mBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(getTarcUserOnlyAlertStatus()){
                    RETAILER_JSON_HTTP_CALL();
                    JSON_HTTP_CALL();
                }
            }
        });

        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(compoundButton.isChecked()&&doneCount){
                    defaultButton.setEnabled(true);
                    storeDialogStatus(true);
                }else{
                    defaultButton.setEnabled(false);
                    storeDialogStatus(false);
                    //finish();
                }
            }
        });
        if(getTarcUserOnlyAlertStatus()){
            mBuilder.hide();
        }else{
            mBuilder.show();
        }

        TandC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, TermOfServicesActivity.class);
                startActivity(myIntent);

            }
        });

        privacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, PrivacyPolicy.class);
                startActivity(myIntent);
            }
        });
    }

    //Store preference of the alert dialog status
    private void storeDialogStatus(boolean isChecked){
        SharedPreferences mSharedPreferences = getSharedPreferences("TarcUserOnly", MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putBoolean("status", isChecked);
        mEditor.putString("email",uid);
        mEditor.apply();
    }

    //Check Preference whether to show the alert message for only TAR user
    private boolean getTarcUserOnlyAlertStatus(){
        SharedPreferences mSharedPreferences = getSharedPreferences("TarcUserOnly", MODE_PRIVATE);
        String user=mSharedPreferences.getString("email","");

        if(uid.equalsIgnoreCase(user)){
            return mSharedPreferences.getBoolean("status", false);
        }else {
            return false;
        }


    }
}
