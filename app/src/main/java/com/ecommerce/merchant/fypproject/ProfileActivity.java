package com.ecommerce.merchant.fypproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ecommerce.merchant.fypproject.adapter.ImageAdapter;
import com.ecommerce.merchant.fypproject.adapter.Retailer;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity  {

    private TextView shopNameDisplay,shopAddressDisplay,shopOwnerNameDispaly,shopOwnerContactDisplay,
            shopOwnerEmailDisplay,shopRegistrationCodeDisplay,shopDescriptionDisplay ;

    //ImageView coverPic;
    //ImageView profileImage;
    private ImageView profilePicture,coverPicture;
    private List<Retailer> RetailerDataList;
    private String uid;

    private RoundedBitmapDrawable roundBitmap;

    //database column name of retailer table

    private String profileURL;
    private String coverURL;
    private ProgressDialog dialog ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar myposttoolbar = findViewById(R.id.profileToolbar);
       // profileImage= (ImageView)findViewById(R.id.profileImageView);
        Button editProfileButton= findViewById(R.id.editProfileButton);
        Button changePasswordButton= findViewById(R.id.changePasswordButton);
        dialog= ProgressDialog.show(ProfileActivity.this, "",getResources().getString(R.string.plsWait), true);

        profilePicture=findViewById(R.id.ProfileVolleyImageView);
        coverPicture=findViewById(R.id.CoverVolleyImageView);

        GetFirebaseAuth();
       // coverPic=(ImageView)findViewById(R.id.coverPhoto);
        shopNameDisplay= findViewById(R.id.shopNameDisplayText);
        shopAddressDisplay= findViewById(R.id.shopAddressDisplayText);
        shopOwnerNameDispaly= findViewById(R.id.shopOwnerNameDisplayText);
        shopOwnerContactDisplay= findViewById(R.id.shopOwnerContactDisplayText);
        shopRegistrationCodeDisplay= findViewById(R.id.shopRegistrationCodeDisplayText);
        shopOwnerEmailDisplay= findViewById(R.id.shopOwnerEmailDisplayText);
        shopDescriptionDisplay = findViewById(R.id.shopDescriptionDisplayText);



        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(ProfileActivity.this,EditProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("retailerEmail",shopOwnerEmailDisplay.getText().toString());
                intent.putExtra("coverURL",coverURL);
                intent.putExtra("profURL",profileURL);
                intent.putExtra("ownername",shopOwnerNameDispaly.getText().toString());
                intent.putExtra("ownercontact",shopOwnerContactDisplay.getText().toString());
                intent.putExtra("shopdesctt",shopDescriptionDisplay.getText().toString());
                startActivity(intent);

            }
        });


        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(ProfileActivity.this,ChangePasswordActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("retailerEmail",shopOwnerEmailDisplay.getText());


                startActivity(intent);
            }
        });

        setSupportActionBar(myposttoolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getResources().getString(R.string.profile));

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        dialog.show();
        JSON_HTTP_CALL();

    }

    @Override
    protected void onResume() {
        super.onResume();
        JSON_HTTP_CALL();
    }

    public  boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId()==android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }


    private void fetchUserData(List<Retailer> retailer){
        Glide.with(ProfileActivity.this)
                .load(retailer.get(0).getProfilePicURL()) // image url
                .apply(new RequestOptions()
                .placeholder(R.drawable.photo) // any placeholder to load at start
                .error(R.drawable.photo)  // any image in case of error
                .override(400, 400) // resizing
                .centerCrop())
                .into(profilePicture);
        profileURL=retailer.get(0).getProfilePicURL();
        Glide.with(ProfileActivity.this)
                .load(retailer.get(0).getCoverPicURL()) // image url
                .apply(new RequestOptions()
                .placeholder(R.drawable.photo) // any placeholder to load at start
                .error(R.drawable.photo)  // any image in case of error
                .override(1000, 1000) // resizing
                .centerCrop())
                .into(coverPicture);
        coverURL=retailer.get(0).getCoverPicURL();
        shopNameDisplay.setText(retailer.get(0).getRetailerShopName());
        shopAddressDisplay.setText(retailer.get(0).getRetailerAddress()) ;
        shopOwnerNameDispaly.setText(retailer.get(0).getRetailerOwnerName());
        shopOwnerContactDisplay.setText(retailer.get(0).getRetailerContact());
        shopOwnerEmailDisplay.setText(retailer.get(0).getRetailerEmail());
        shopRegistrationCodeDisplay.setText(retailer.get(0).getRetailerRegistrationCode());
        shopDescriptionDisplay.setText(retailer.get(0).getRetailerShopDescription());
    }


    private void JSON_HTTP_CALL() {
        RetailerDataList = new ArrayList<>();
        final String user = "000009";
        String HTTP_JSON_URL = "http://ecommercefyp.000webhostapp.com/retailer/read_user_profile.php";
        StringRequest RequestOfJSonArray = new StringRequest(Request.Method.POST, HTTP_JSON_URL,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            ParseJSonResponse(response);
                        } catch (JSONException e) {
                            Crashlytics.logException(e);
                            // handle your exception here!
                            e.printStackTrace();
                            //progressBar.setVisibility(View.GONE);
                            //txtnoresult.setVisibility(View.VISIBLE);
                        }
                        Log.d("Response ", response);

                        dialog.dismiss();
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


        RequestQueue requestQueue = Volley.newRequestQueue(ProfileActivity.this);
        requestQueue.add(RequestOfJSonArray);
    }

    private void ParseJSonResponse(String array) throws JSONException {
        String processedString=array.substring(array.indexOf("["),array.lastIndexOf("]")+1);
        JSONArray jarr = new JSONArray(processedString);
        for(int i = 0; i<jarr.length(); i++) {

            Retailer retailer = new Retailer();

            JSONObject json;
            try {

                json = jarr.getJSONObject(i);

                String json_retalierShopName = "RShopName";
                retailer.setRetailerShopName(json.getString(json_retalierShopName));
                String json_retalierShopAddress = "RAddr";
                retailer.setRetailerAddress(json.getString(json_retalierShopAddress));
                String json_retalierOwnerName = "ROwnerName";
                retailer.setRetailerOwnerName(json.getString(json_retalierOwnerName));
                String json_retalierContact = "RContact";
                retailer.setRetailerContact(json.getString(json_retalierContact));
                String json_retailerEmail = "REmail";
                retailer.setRetailerEmail(json.getString(json_retailerEmail));
                String json_retalierShopDescription = "RDesc";
                retailer.setRetailerShopDescription(json.getString(json_retalierShopDescription));
                String json_retailerCoverPicURL = "coverPicURL";
                String coverStringURL=json.getString(json_retailerCoverPicURL).replace(" ","%20");
                retailer.setCoverPicURL(coverStringURL);
                String json_retailerProfilePicURL = "profilePicURL";
                String profileStringURL=json.getString(json_retailerProfilePicURL).replace(" ","%20");
                retailer.setProfilePicURL(profileStringURL);
                String json_retailerRegistrationCode = "RRegCode";
                retailer.setRetailerRegistrationCode(json.getString(json_retailerRegistrationCode));

            } catch (JSONException e) {
                Crashlytics.logException(e);
                // handle your exception here!
                e.printStackTrace();
            }

            RetailerDataList.add(retailer);
            //System.out.print(RetailerDataList.get(0).toString());
        }
        fetchUserData(RetailerDataList);
    }

    public ImageLoader loadImage(String picURL){
        ImageLoader imageLoader;
        imageLoader = ImageAdapter.getInstance(this).getImageLoader();

         imageLoader.get(picURL, new ImageLoader.ImageListener() {
             @Override
             public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {

             }

             @Override
             public void onErrorResponse(VolleyError error) {

             }
         });
        return imageLoader;
    }
    public ImageLoader roundLoadImage(String picURL){
        ImageLoader imageLoader;
        imageLoader = ImageAdapter.getInstance(this).getImageLoader();

        imageLoader.get(picURL, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                roundBitmap= RoundedBitmapDrawableFactory.create(getResources(),response.getBitmap());
                roundBitmap.setCircular(true);

            }

            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        return imageLoader;
    }

    private void GetFirebaseAuth(){
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
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
        }else uid= firebaseAuth.getCurrentUser().getUid();
    }
}
