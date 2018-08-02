package com.ecommerce.merchant.fypproject;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ecommerce.merchant.fypproject.adapter.UploadProcess;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class EditProfileActivity extends AppCompatActivity {
//kk
private EditText editOwnerName;
    private EditText editOwnerContact;
    private EditText editShopDescription;
    private ImageButton coverPictureButtton;
    private ImageButton profilePictureButton;
    private ImageButton erasename;
    private ImageButton erasecontact;
    private ImageButton erasedesc;
    private Toolbar editProfileToolbar;
    private Button editProfileSubmitButton;
    private final Integer REQUEST_CAMERA=7;
    private final Integer SELECT_FILE=1888;
    private Bitmap bitmap;
    private Bitmap coverBitMap;
    private Bitmap profileBitMap;
    private static final int RequestPermissionCode  = 1 ;
    private boolean isProfile=false;
    private boolean check=true;
    private ProgressDialog progressDialog ;
    private final String serverOwnerName="ROwnerName";
    private final String serverOwnerContact="RContact";
    private final String serverShopDescription="RDesc";
    private final String profileImageFileParameter="ProfilePhotoData";
    private final String coverImageFileParameter="CoverPhotoData";
    private String retailerEmail;
    private String profURL;
    private String coverURL;
    private String ownername;
    private String ownercontact;
    private String shopdescription;
    private String getOwnerName;
    private String getOwnerContact;
    private String getShopDescription;
    //String updateProfilePath="http://ecommercefyp.000webhostapp.com/retailer/update_user_profile.php";
    private final String PHPURL = "http://ecommercefyp.000webhostapp.com/retailer/manage_retailer_product.php";
    //private String PHPURL = "http://10.0.2.2/cashierbookPHP/Eric/manage_retailer_product.php";
    private Bundle activityBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        editProfileToolbar= findViewById(R.id.editProfileToolbar);
        editOwnerName= findViewById(R.id.editOwnerName);
        editOwnerContact= findViewById(R.id.editOwnerContact);
        editShopDescription= findViewById(R.id.editShopDescription);
        coverPictureButtton= findViewById(R.id.coverPictureButton);
        profilePictureButton= findViewById(R.id.profilePictureButton);
        editProfileSubmitButton= findViewById(R.id.editProfileSubmitButton);
        erasename=findViewById(R.id.erase_name);
        erasecontact=findViewById(R.id.erase_contact);
        erasedesc=findViewById(R.id.erase_desc);
//ghj

        activityBundle=getIntent().getExtras();
        retailerEmail= Objects.requireNonNull(activityBundle).getString("retailerEmail");
        profURL=activityBundle.getString("profURL");
        coverURL=activityBundle.getString("coverURL");
        ownercontact=activityBundle.getString("ownercontact");
        ownername=activityBundle.getString("ownername");
        shopdescription=activityBundle.getString("shopdesctt");

        editOwnerContact.setText(ownercontact);
        editOwnerName.setText(ownername);
        editShopDescription.setText(shopdescription);


        setSupportActionBar(editProfileToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Profile");

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        EnableRuntimePermissionToAccessCamera();

        Glide.with(EditProfileActivity.this)
                .asBitmap()
                .load(coverURL) // image url
                .apply(new RequestOptions()
                .placeholder(R.drawable.photo) // any placeholder to load at start
                .error(R.drawable.photo)  // any image in case of error
                .override(1000, 1000) // resizing
                .centerCrop()).into(coverPictureButtton);
//                .into(new SimpleTarget<Bitmap>(300,300) {
//                    @Override
//                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
//                       coverPictureButtton.setImageBitmap(resource);
//                       coverBitMap=resource;
//                    }
//                });

        //coverPictureButtton
        Glide.with(EditProfileActivity.this)
                .asBitmap()
                .load(profURL) // image url
                .apply(new RequestOptions()
                .placeholder(R.drawable.photo) // any placeholder to load at start
                .error(R.drawable.photo)  // any image in case of error
                .override(500, 500) // resizing
                .centerCrop()).into(profilePictureButton);

                editProfileSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getOwnerName = editOwnerName.getText().toString();
                getOwnerContact = editOwnerContact.getText().toString();
                getShopDescription= editShopDescription.getText().toString();



                //Validate input are correct and only update once at a time.
                if(!getOwnerName.equalsIgnoreCase("")&&!getOwnerContact.equalsIgnoreCase("")&&!getShopDescription.equalsIgnoreCase("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                    builder.setTitle(getResources().getString(R.string.profileUpdate));
                    builder.setMessage(getResources().getString(R.string.profileUpdateMsg));
                    builder.setIcon(R.mipmap.ic_edit);
                    builder.setPositiveButton(getResources().getString(R.string.update), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            dialog.dismiss();
                            ImageUploadToServerFunction();
                        }
                    });
                    builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();

                    check = false;
                }else{


                    if(getOwnerName.trim().equalsIgnoreCase("")){
                        editOwnerName.setError(getResources().getString(R.string.ownerEmpty));
                    }
                    if(getOwnerContact.trim().equalsIgnoreCase("")){
                        editOwnerContact.setError(getResources().getString(R.string.ownerContactEmpty));
                    }
                   // if(getOwnerEmail.trim().equalsIgnoreCase("")){
                       // editOwnerEmail.setError("Owner email cannot be blank");
                    //}
                    if(getShopDescription.trim().equalsIgnoreCase("")){
                        editShopDescription.setError(getResources().getString(R.string.ownerShopEmpty));
                    }

                }

            }
        });

        erasedesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editShopDescription.setText("");
            }
        });
        erasecontact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editOwnerContact.setText("");
            }
        });
        erasename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editOwnerName.setText("");
            }
        });



    }

    private void ImageUploadToServerFunction(){
        //final String user = "1610458";
        ByteArrayOutputStream byteArrayOutputStreamObjectOfProfile ;
        ByteArrayOutputStream byteArrayOutputStreamObjectOfCover ;

        byteArrayOutputStreamObjectOfProfile=new ByteArrayOutputStream() ;
        byteArrayOutputStreamObjectOfCover = new ByteArrayOutputStream();

        String coverString,profileString;

        if(coverBitMap!=null){
            coverBitMap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStreamObjectOfCover);
            byte[] byteArrayVarOfCover = byteArrayOutputStreamObjectOfCover.toByteArray();
            coverString=Base64.encodeToString(byteArrayVarOfCover, Base64.DEFAULT);
        }else{
            coverString="";
        }

        if(profileBitMap!=null){
            profileBitMap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStreamObjectOfProfile);
            byte[] byteArrayVarOfProfile = byteArrayOutputStreamObjectOfProfile.toByteArray();
            profileString=Base64.encodeToString(byteArrayVarOfProfile, Base64.DEFAULT);
        }else{
            profileString="";
        }


        final String ConvertImageOfProfile = profileString;
        final String ConvertImageOfCover = coverString;


        class AsyncTaskUploadClass extends AsyncTask<Void,Void,String> {

            @Override
            protected void onPreExecute() {

                super.onPreExecute();

                // Showing progress dialog at image upload time.
                progressDialog = ProgressDialog.show(EditProfileActivity.this,getResources().getString(R.string.profileUpdate2),getResources().getString(R.string.plsWait),false,false);
            }

            @Override
            protected void onPostExecute(String string1) {

                super.onPostExecute(string1);

                // Dismiss the progress dialog after done uploading.
                progressDialog.dismiss();

                // Printing uploading success message coming from server on android app.
                Toast.makeText(EditProfileActivity.this,string1,Toast.LENGTH_LONG).show();
                Log.e("Response ", string1);
                finish();
                // Setting image as transparent after done uploading.
                //imageView.setImageResource(android.R.color.transparent);

            }
            @Override
            protected String doInBackground(Void... params) {

                UploadProcess imageProcessClass = new UploadProcess();

                HashMap<String,String> HashMapParams = new HashMap<>();

                JSONObject objectDetail = new JSONObject();
                JSONArray array = new JSONArray();
                try {

                    objectDetail.put(serverOwnerName, getOwnerName);
                    objectDetail.put(serverOwnerContact, getOwnerContact);
                    objectDetail.put(serverShopDescription, getShopDescription);
                    objectDetail.put(profileImageFileParameter, ConvertImageOfProfile);
                    objectDetail.put(coverImageFileParameter, ConvertImageOfCover);
                    objectDetail.put("retailerEmail", retailerEmail);


                    array.put(objectDetail);
                    array.toString();
                    Log.e("sendProfileData",array.toString());
                }catch (Exception e){
                    Log.e("editprofileError",e.toString());
                }


//                HashMapParams.put(serverOwnerName, getOwnerName);
//                HashMapParams.put(serverOwnerContact, getOwnerContact);
//               // HashMapParams.put(serverOwnerEmail, getOwnerEmail);
//                HashMapParams.put(serverShopDescription, getShopDescription);
//
//                HashMapParams.put(profileImageFileParameter, ConvertImageOfProfile);
//                HashMapParams.put(coverImageFileParameter, ConvertImageOfCover);
//
//                Log.d("file string",ConvertImageOfCover);
                HashMapParams.put("updateRetailerProfile",array.toString());

                return imageProcessClass.HttpRequest(PHPURL, HashMapParams);
            }
        }
        AsyncTaskUploadClass AsyncTaskUploadClassOBJ = new AsyncTaskUploadClass();

        AsyncTaskUploadClassOBJ.execute();
    }


    public void selectimage (View v){
        final CharSequence[] items=getResources().getStringArray(R.array.select_arraywithoutRemove);
        final View receivedID=v;
        AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
        builder.setTitle( getResources().getString(R.string.addImg));
        builder.setItems( items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if(items[i].equals("Camera")){
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    isProfile = R.id.profilePictureButton == receivedID.getId();

                    startActivityForResult(Intent.createChooser(intent,getResources().getString(R.string.selectFromCamera)),REQUEST_CAMERA);
                    //if(v.getId()==R.id.profilePictureButton){

                    //}


                }else if(items[i].equals( "Gallery" )){
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    isProfile = R.id.profilePictureButton == receivedID.getId();


                    startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.selectFromGallery)), SELECT_FILE);
                }else if(items[i].equals( "Cancel" )){
                    dialog.dismiss();
                }
            }
        } );
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null){
                try {
                    Uri uri = data.getData();
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    if(isProfile){
                        profilePictureButton.setImageBitmap(bitmap);
                        profileBitMap=bitmap;
                    }else{
                        coverPictureButtton.setImageBitmap(bitmap);
                        coverBitMap=bitmap;
                    }


                } catch (IOException e) {

                    e.printStackTrace();
                }
            }else{
                bitmap = (Bitmap) Objects.requireNonNull(Objects.requireNonNull(data).getExtras()).get("data");
                if(isProfile){
                    profilePictureButton.setImageBitmap(bitmap);
                    profileBitMap=bitmap;
                }else{
                    coverPictureButtton.setImageBitmap(bitmap);
                    coverBitMap=bitmap;
                }
            }

        }
        if (requestCode == SELECT_FILE && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            try {

                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                if(isProfile){
                    profilePictureButton.setImageBitmap(bitmap);
                    profileBitMap=bitmap;
                }else{
                    coverPictureButtton.setImageBitmap(bitmap);
                    coverBitMap=bitmap;
                }

            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }

    private void EnableRuntimePermissionToAccessCamera(){

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        !=PackageManager.PERMISSION_GRANTED&& (ContextCompat.checkSelfPermission(
                this,Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED)) {
            // Permission is not granted, then request for permission
            ActivityCompat.requestPermissions( this,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE}, RequestPermissionCode);
        }
    }



    @Override
    public void onRequestPermissionsResult(int RC, String per[], int[] PResult) {

        switch (RC) {

            case RequestPermissionCode:

                if (!(PResult.length > 0 && PResult[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(EditProfileActivity.this,getResources().getString(R.string.camPermissionDenied), Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    public  boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId()==android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }
}
