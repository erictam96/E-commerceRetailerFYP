package com.ecommerce.merchant.fypproject;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.ecommerce.merchant.fypproject.adapter.DecimalDigitsInputFilter;
import com.ecommerce.merchant.fypproject.adapter.OnItemClick;
import com.ecommerce.merchant.fypproject.adapter.Product;
import com.ecommerce.merchant.fypproject.adapter.SerialCode;
import com.ecommerce.merchant.fypproject.adapter.SerialCodeAdapter;
import com.ecommerce.merchant.fypproject.adapter.UploadProcess;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ItemUpdateActivity extends AppCompatActivity implements View.OnClickListener,OnItemClick {
    private final String ItemUpdatePathOnServer = "http://ecommercefyp.000webhostapp.com/retailer/manage_retailer_product.php";
    //String ItemUpdatePathOnServer = "http://10.0.2.2/cashierbookPHP/Eric/manage_retailer_product.php";
    private Uri mImageUri;
    private String uid;
    private String prodcode;
    private final String ProductPathFieldOnServer = "updateProd";
    private final ArrayList<Bitmap> bitmapArray = new ArrayList<>();
    private EditText Name,Desc,Price,Quantity,Promo,Variant,edtVariant,edtPrice,edtDiscount,edtQuantity;
    private boolean check;
    private LinearLayout parentLinearLayout;
    private Integer img;
    private Spinner SpinnerCategory,Size;
    private ImageView ImageCover = null,Image1 = null,Image2 = null,Image3=null,Image4=null;
    private Product product;
    private CharSequence[] items;
    private String mImageFileLocation;
    private final ArrayList<String> ImageID = new ArrayList<>();
    private final ArrayList<String> ConvertImage= new ArrayList<>();
    private final ArrayList<String> ProductDetails = new ArrayList<>();
    private final ArrayList<String> getImageIDtoServer = new ArrayList<>();
    private final ArrayList<String> removeImagetoServer = new ArrayList<>();
    private final Bitmap[] bitmapSelect={null,null,null,null,null};
    private ProgressDialog progressDialog,loadingDialog ;
    private static final int RequestPermissionCode  = 1 ;
    private RequestQueue requestQueue ;
    private final ArrayList<JSONObject> VariantList = new ArrayList();
    private boolean isLoad;
    private final boolean[] remove= {false,false,false,false,false};
    private final boolean[] checkimage ={false,false,false,false,false};
    private Dialog serialDialog;
    private RecyclerView serialRecycler;
    private TextView serialText;
    private RecyclerView.LayoutManager layoutManagerOfrecyclerView;
    private View view ;


    private List<SerialCode> serialList;

    @Override
    public void onClick(String value, String prodvar, String prodcode, String rid) {
        if(value.equalsIgnoreCase("refresh")){
            SERIAL_CODE_CALL();
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_update);

        Toolbar myposttoolbar = findViewById(R.id.item_update_toolbar);
        setSupportActionBar(myposttoolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getResources().getString(R.string.updateItem));

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        //INITIALIZE view and instance
        GetFirebaseAuth();



        parentLinearLayout = findViewById(R.id.parentLinear);
        Name = findViewById(R.id.edtTitle2);
        Desc = findViewById(R.id.edtDesc2);
        Price = findViewById(R.id.edtPrice);
        Quantity = findViewById(R.id.edtQuantity);
        Promo = findViewById(R.id.edtDiscount);
        SpinnerCategory = findViewById(R.id.spinnerCategory1);
        Variant= findViewById(R.id.edtVariant);
        Button serialBut = findViewById(R.id.variantSerialBut);
        Size = findViewById(R.id.spinnerSize);

        loadingDialog=new ProgressDialog(this);
        loadingDialog.setCancelable(true);
        loadingDialog.setTitle(R.string.loading);
        loadingDialog.setMessage(getResources().getString(R.string.loadingSerial));
        loadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        ImageCover = findViewById(R.id.coverphoto);
        Image1 = findViewById(R.id.image1);
        Image2 = findViewById(R.id.image2);
        Image3 = findViewById(R.id.image3);
        Image4 = findViewById(R.id.image4);

        Button uploadToServer = findViewById(R.id.btnSubmit1);
        Button deleteFromServer = findViewById(R.id.btnDelete2);
        Button addVariant = findViewById(R.id.btnAddVariant);

        product = new Product();


        //Set OnClick Listener
        ImageCover.setOnClickListener(this);
        Image1.setOnClickListener(this);
        Image2.setOnClickListener(this);
        Image3.setOnClickListener(this);
        Image4.setOnClickListener(this);
        uploadToServer.setOnClickListener(this);
        deleteFromServer.setOnClickListener(this);
        addVariant.setOnClickListener(this);

        serialBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  Toast.makeText(ItemUpdateActivity.this,"serial here",Toast.LENGTH_SHORT).show();
                serialDialog.show();
            }
        });

        RelativeLayout currentRow = (RelativeLayout)parentLinearLayout.getChildAt(0);
        edtPrice = currentRow.findViewById(R.id.edtPrice);
        edtPrice.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(7,2)});

        //RECEIVE DATA from extra
        Intent i = this.getIntent();
        prodcode = Objects.requireNonNull(i.getExtras()).getString("prodcode_KEY");
        serialList=new ArrayList<>();
        SERIAL_CODE_CALL();
        createSerialDialog();
        JSON_HTTP_CALL();

        String addSerial = i.getExtras().getString("addSerial");
        if(addSerial !=null&& addSerial.equalsIgnoreCase("yes")){
            serialDialog.show();

            AlertDialog.Builder builder = new AlertDialog.Builder(ItemUpdateActivity.this);
            builder.setTitle(getResources().getString(R.string.updateItem));
            builder.setMessage(getResources().getString(R.string.serialMsg2));
            builder.setIcon(R.mipmap.ic_edit);
            builder.setPositiveButton(getResources().getString(R.string.insertSerial), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    dialog.dismiss();

                }
            });
            builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finish();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    //Upload data to server database
    private void UploadToServerFunction() throws JSONException {
        Answers.getInstance().logCustom(new CustomEvent("Product")
                .putCustomAttribute("Type", "Update"));

        //Add and compress image to array
        for(int a = 0 ; a<5;a++){
            if(bitmapSelect[a]!=null){
                getImageIDtoServer.add(ImageID.get(a));
                ByteArrayOutputStream byteArrayOutputStreamObject;
                byteArrayOutputStreamObject = new ByteArrayOutputStream();
                // Toast.makeText(ItemUpdateActivity.this,"Change!!!!   "+id,Toast.LENGTH_LONG).show();
                bitmapSelect[a].compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStreamObject);
                byte[] byteArrayVar = byteArrayOutputStreamObject.toByteArray();
                ConvertImage.add(Base64.encodeToString(byteArrayVar, Base64.DEFAULT));
            }else if (bitmapArray.get(a)!=null && remove[a]==true){
                removeImagetoServer.add(ImageID.get(a));
            }
        }

        //get all variant details
        final int childCount = parentLinearLayout.getChildCount();
        for(int c = 0;c<childCount;c++) {
            JSONObject JSONVariant = new JSONObject();
            RelativeLayout currentRow = (RelativeLayout) parentLinearLayout.getChildAt(c);

            edtVariant = currentRow.findViewById(R.id.edtVariant);
            edtPrice = currentRow.findViewById(R.id.edtPrice);
            edtQuantity = currentRow.findViewById(R.id.edtQuantity);
            edtDiscount = currentRow.findViewById(R.id.edtDiscount);

            JSONVariant.put("Title:",edtVariant.getText().toString());
            JSONVariant.put("Price:",edtPrice.getText().toString());
            JSONVariant.put("Quantity:",edtQuantity.getText().toString());
            JSONVariant.put("Discount:",edtDiscount.getText().toString());

            VariantList.add(JSONVariant);
        }

        class AsyncTaskUploadClass extends AsyncTask<Void,Void,String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                // Showing progress dialog at image upload time.
                progressDialog = ProgressDialog.show(ItemUpdateActivity.this,"Item is Uploading","Please Wait",false,false);
            }

            @Override
            protected void onPostExecute(String string1) {
                Log.d("Responseee",string1);
                super.onPostExecute(string1);


                // Dismiss the progress dialog after done uploading.
                progressDialog.dismiss();

                // Printing uploading success message coming from server on android app.
                Toast.makeText(ItemUpdateActivity.this,getResources().getString(R.string.itemRegisterSucc),Toast.LENGTH_LONG).show();
                finish();
            }
            @Override
            protected String doInBackground(Void... params) {
                ProductDetails.add(Name.getText().toString());
                ProductDetails.add(SpinnerCategory.getSelectedItem().toString());
                ProductDetails.add(Desc.getText().toString());
                ProductDetails.add(product.getProdCode());
                ProductDetails.add(Size.getSelectedItem().toString());
                ProductDetails.add(uid);

                JSONObject JSONProduct = new JSONObject();
                JSONObject JSONVariantAll = new JSONObject();
                JSONObject JSONImage = new JSONObject();
                JSONObject JSONImageID = new JSONObject();
                JSONObject JSONImageRemove = new JSONObject();
                JSONObject EverythingJSON = new JSONObject();

                //Loop Array list into object
                for(int i = 0; i< removeImagetoServer.size();i++){
                    try {
                        JSONImageRemove.put("removeID:" + String.valueOf(i + 1), removeImagetoServer.get(i));
                    } catch (JSONException e) {
                        Crashlytics.logException(e);
                        // handle your exception here!
                        e.printStackTrace();
                    }
                }
                for(int i=0;i<VariantList.size();i++){
                    try {
                        JSONVariantAll.put("Variant:"+String.valueOf(i+1),VariantList.get(i));
                    } catch (JSONException e) {
                        Crashlytics.logException(e);
                        // handle your exception here!
                        e.printStackTrace();
                    }
                }

                for (int i = 0; i < ConvertImage.size(); i++) {
                    try {
                        JSONImageID.put("ID:" + String.valueOf(i + 1), getImageIDtoServer.get(i));
                        JSONImage.put("Image:" + String.valueOf(i + 1), ConvertImage.get(i));
                    } catch (JSONException e) {
                        Crashlytics.logException(e);
                        // handle your exception here!
                        e.printStackTrace();
                    }
                }

                for (int i = 0; i < ProductDetails.size(); i++) {
                    try {
                        JSONProduct.put("Details:" + String.valueOf(i + 1), ProductDetails.get(i));
                    } catch (JSONException e) {
                        Crashlytics.logException(e);
                        // handle your exception here!
                        e.printStackTrace();
                    }
                }
                Log.e("JSON",JSONProduct.toString());
                Log.e("JSON",JSONVariantAll.toString());
                try {
                    //Put all object into one object
                    EverythingJSON.put("details",JSONProduct);
                    EverythingJSON.put("variant",JSONVariantAll);
                    EverythingJSON.put("image",JSONImage);
                    EverythingJSON.put("imageid",JSONImageID);
                    EverythingJSON.put("removeid",JSONImageRemove);

                } catch (JSONException e) {
                    Crashlytics.logException(e);
                    // handle your exception here!
                    e.printStackTrace();
                }

                UploadProcess imageProcessClass = new UploadProcess();
                HashMap<String, JSONObject> HashMapParams = new HashMap<>();
                HashMapParams.put(ProductPathFieldOnServer, EverythingJSON);
                return imageProcessClass.HttpRequestObject(ItemUpdatePathOnServer, HashMapParams);
            }
        }
        AsyncTaskUploadClass AsyncTaskUploadClassOBJ = new AsyncTaskUploadClass();
        AsyncTaskUploadClassOBJ.execute();
    }

    //Delete data and image from server database
    private void DeleteFromServerFunction(){
        Answers.getInstance().logCustom(new CustomEvent("Product")
                .putCustomAttribute("Type", "Delete"));

        class  AsyncTaskUploadClass extends AsyncTask<Void,Void,String>{
            @Override
            protected void onPreExecute() {

                super.onPreExecute();
                // Showing progress dialog at delete data time
                progressDialog = ProgressDialog.show(ItemUpdateActivity.this,getResources().getString(R.string.deletingMsg),getResources().getString(R.string.plsWait),false,false);
            }
            @Override
            protected String doInBackground(Void... params) {

                UploadProcess imageProcessClass = new UploadProcess();
                HashMap<String,String> HashMapParams = new HashMap<>();

                HashMapParams.put("deleteProd", prodcode);
                return imageProcessClass.HttpRequest(ItemUpdatePathOnServer, HashMapParams);
            }
            @Override
            protected void onPostExecute(String string1) {
                super.onPostExecute(string1);
               // Dismiss the progress dialog after done uploading.
                progressDialog.dismiss();

                // Printing uploading success message coming from server on android app.
                Toast.makeText(ItemUpdateActivity.this,getResources().getString(R.string.itemDltSucc),Toast.LENGTH_LONG).show();
                MyPostActivity.getInstance().finish();
                Intent returnActivity = new Intent(ItemUpdateActivity.this,MyPostActivity.class);
                startActivity(returnActivity);
                finish();
            }
        }

        AsyncTaskUploadClass AsyncTaskUploadClassOBJ = new AsyncTaskUploadClass();
        AsyncTaskUploadClassOBJ.execute();
    }

    //Select image source: Camera : Gallery and remove image from imageview
    private void selectimage(int i){
        img = i;
        switch (img){
            //select_array with Remove image button
            case 0:
                if(checkimage[0]==true){
                     items= getResources().getStringArray(R.array.select_array);
                }else{
                     items= getResources().getStringArray(R.array.select_arraywithoutRemove);
                }
                break;
            case 1:
                if(checkimage[1]==true){
                    items= getResources().getStringArray(R.array.select_array);
                }else{
                    items= getResources().getStringArray(R.array.select_arraywithoutRemove);
                }
                break;
            case 2:
                if(checkimage[2]==true){
                    items= getResources().getStringArray(R.array.select_array);
                }else{
                    items= getResources().getStringArray(R.array.select_arraywithoutRemove);
                }
                break;
            case 3:
                if(checkimage[3]==true){
                    items= getResources().getStringArray(R.array.select_array);
                }else{
                    items= getResources().getStringArray(R.array.select_arraywithoutRemove);
                }
                break;
            case 4:
                if(checkimage[4]==true){
                    items= getResources().getStringArray(R.array.select_array);
                }else{
                    items= getResources().getStringArray(R.array.select_arraywithoutRemove);
                }
                break;
        }
        //Show dialog box for select camera,gallery,remove,cancel
        AlertDialog.Builder builder = new AlertDialog.Builder(ItemUpdateActivity.this);
        builder.setTitle( getResources().getString(R.string.addImg) );
        builder.setItems( items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if(items[i].equals("Camera")){
                    if(!EnableRuntimePermission()){
                    Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    //Create a temp jpg file to store full size captured image
                    File photo=null;
                    try
                    {
                        // place where to store camera taken picture
                        photo = createTemporaryFile();
                        photo.delete();
                    }
                    catch(Exception e)
                    {
                        Log.e("ImageCapture", e.toString());
                        Toast.makeText(ItemUpdateActivity.this, getResources().getString(R.string.plsChkSD),Toast.LENGTH_LONG).show();
                    }
                    //File provider is to generate files URI and it is declare at manifest and xml folder
                    mImageUri = FileProvider.getUriForFile(getApplicationContext(),"com.ecommerce.merchant.fypproject.fileprovider", Objects.requireNonNull(photo));
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                    //start camera intent
                    startActivityForResult(Intent.createChooser(intent,getResources().getString(R.string.selectFromCamera)),img);
                    }
                }else if(items[i].equals( "Gallery" )){
                    //Intent to open gallery to select image
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    img+=10;
                    startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.selectFromGallery)), img);
                }else if (items[i].equals("Remove")){
                    //remove images and set boolean for check any changes
                    switch (img){
                        case 0:
                            bitmapSelect[0] = null;
                            remove[0] = true;
                            checkimage[0]= false;
                            ImageCover.setImageDrawable(getResources().getDrawable(R.drawable.photo));
                            break;
                        case 1:
                            bitmapSelect[1] = null;
                            remove[1] = true;
                            checkimage[1]= false;
                            Image1.setImageDrawable(getResources().getDrawable(R.drawable.photo));
                            break;
                        case 2:
                            bitmapSelect[2] = null;
                            remove[2] = true;
                            checkimage[2]= false;
                            Image2.setImageDrawable(getResources().getDrawable(R.drawable.photo));
                            break;
                        case 3:
                            bitmapSelect[3] = null;
                            remove[3] = true;
                            checkimage[3]= false;
                            Image3.setImageDrawable(getResources().getDrawable(R.drawable.photo));
                            break;
                        case 4:
                            bitmapSelect[4] = null;
                            remove[4] = true;
                            checkimage[4]= false;
                            Image4.setImageDrawable(getResources().getDrawable(R.drawable.photo));
                            break;
                        default:
                            break;
                    }
                }
                else if(items[i].equals( "Cancel" )){
                    dialog.dismiss();
                }
            }
        } );
        builder.show();
    }

    //Function to find path and create a temporary temp.jpg
    private File createTemporaryFile() throws Exception {
        File tempDir= Environment.getExternalStorageDirectory();
        tempDir=new File(tempDir.getAbsolutePath()+"/.temp/");
        if(!tempDir.exists())
        {
            tempDir.mkdirs();
        }
        File image = File.createTempFile("temp", ".jpg", tempDir);
        mImageFileLocation = image.getAbsolutePath();
        return image;
    }

    //Convert the image file to bitmap
    private Bitmap grabImage() throws IOException {
        Uri uri = Uri.fromFile(new File(mImageFileLocation));
        return handleSamplingAndRotationBitmap(getApplicationContext(),uri);
    }

    //select image result
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Uri uri;
            switch (requestCode) {

                //After convert into bitmap then set into image view
                case 0:
                    try {
                        bitmapSelect[0] = grabImage();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ImageCover.setImageBitmap(bitmapSelect[0]);
                    remove[0] = false;
                    checkimage[0]=true;
                    break;
                case 1:
                    try {
                        bitmapSelect[1] = grabImage();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Image1.setImageBitmap(bitmapSelect[1]);
                    remove[1] = false;
                    checkimage[1]=true;
                    break;
                case 2:
                    try {
                        bitmapSelect[2] = grabImage();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Image2.setImageBitmap(bitmapSelect[2]);
                    remove[2] = false;
                    checkimage[2]=true;
                    break;
                case 3:
                    try {
                        bitmapSelect[3] = grabImage();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Image3.setImageBitmap(bitmapSelect[3]);
                    remove[3] = false;
                    checkimage[3]=true;
                    break;
                case 4:
                    try {
                        bitmapSelect[4] = grabImage();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Image4.setImageBitmap(bitmapSelect[4]);
                    remove[4] = false;
                    checkimage[4]=true;
                    break;
                case 10:
                    uri = data.getData();
                    if (uri != null) {
                        try {
                            bitmapSelect[0] = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        bitmapSelect[0] =(Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                    }
                    ImageCover.setImageBitmap(bitmapSelect[0]);
                    remove[0] = false;
                    checkimage[0]=true;
                    break;
                case 11:
                    uri = data.getData();
                    if (uri != null) {
                        try {
                            bitmapSelect[1] = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        bitmapSelect[1] =(Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                    }
                    Image1.setImageBitmap(bitmapSelect[1]);
                    remove[1] = false;
                    checkimage[1]=true;
                    break;
                case 12:
                    uri = data.getData();
                    if (uri != null) {
                        try {
                            bitmapSelect[2] = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        bitmapSelect[2] =(Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                    }
                    Image2.setImageBitmap(bitmapSelect[2]);
                    remove[2] = false;
                    checkimage[2]=true;
                    break;
                case 13:
                    uri = data.getData();
                    if (uri != null) {
                        try {
                            bitmapSelect[3] = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        bitmapSelect[3] =(Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                    }
                    Image3.setImageBitmap(bitmapSelect[3]);
                    remove[3] = false;
                    checkimage[3]=true;
                    break;
                case 14:
                    uri = data.getData();
                    if (uri != null) {
                        try {
                            bitmapSelect[4] = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        bitmapSelect[4] =(Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
                    }
                    Image4.setImageBitmap(bitmapSelect[4]);
                    remove[4] = false;
                    checkimage[4]=true;
                    break;
            }
        }
    }

    //retrieve details from server
    private void JSON_HTTP_CALL() {
        isLoad=false;
        class AsyncTaskUploadClass extends AsyncTask<Void,Void,String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog = ProgressDialog.show(ItemUpdateActivity.this,getResources().getString(R.string.loadItemDetail),getResources().getString(R.string.plsWait),false,true);
                progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if(!isLoad){
                            finish();
                        }
                    }
                });
                // Showing progress dialog at image upload time.
                //progressDialog = ProgressDialog.show(ItemUpdateActivity.this,"Item is Uploading","Please Wait",false,false);
            }
            @Override
            protected void onPostExecute(String response) {
                Log.d("Responseee",response);
                super.onPostExecute(response);
                try {
                    ParseJSonResponse(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected String doInBackground(Void... voids) {
                UploadProcess imageProcessClass = new UploadProcess();
                HashMap<String, String> HashMapParams = new HashMap<>();
                HashMapParams.put("prodCode", prodcode);
                return imageProcessClass.HttpRequest(ItemUpdatePathOnServer, HashMapParams);
            }
        }
        AsyncTaskUploadClass AsyncTaskUploadClass = new AsyncTaskUploadClass();
        AsyncTaskUploadClass.execute();
    }

    //get response string and set into recyclerView
    private void ParseJSonResponse(String array) throws JSONException {
        Log.e("array",array);
        JSONArray jarr = new JSONArray(array);
        JSONArray json;
        json = jarr.getJSONArray(0);
        JSONObject productJson;
        //Set product details text
        productJson = json.getJSONObject(0);
        product.setProdCode(productJson.getString("prodcode"));
        Name.setText(productJson.getString("prodname"));
        Desc.setText(productJson.getString("proddesc"));
        final CharSequence[] item2 = getResources().getStringArray(R.array.size_array);
        Size.setSelection(item2.length - 1);
        for (int j = 0; j < item2.length; j++) {
            if (item2[j].toString().equals(productJson.getString("prodsize"))) {
                Size.setSelection(j);
            }
        }
        final CharSequence[] item = getResources().getStringArray(R.array.target_array);
        SpinnerCategory.setSelection(item.length - 1);
        for (int j = 0; j < item.length; j++) {
            if (item[j].toString().equals(productJson.getString("prodcategory"))) {
                SpinnerCategory.setSelection(j);
            }
        }
        json = jarr.getJSONArray(1);
        //Set first variant details
            productJson = json.getJSONObject(0);
            Variant.setText(productJson.getString("prodvariant"));
            Price.setText(productJson.getString("prodprice"));
            Quantity.setText(productJson.getString("prodquantity"));
            Promo.setText(productJson.getString("proddiscount"));

        for(int i =1; i<json.length();i++) {
            productJson = json.getJSONObject(i);
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View rowView = inflater.inflate(R.layout.variant_field, null);
            // Add the new row before the add field button.

            rowView.findViewById(R.id.btnDeleteVariant).setTag(productJson.getString("prodvariant"));
            parentLinearLayout.addView(rowView, parentLinearLayout.getChildCount()-1);

            RelativeLayout currentRow = (RelativeLayout) parentLinearLayout.getChildAt(i-1);
            //Set variant details by row
            edtVariant = currentRow.findViewById(R.id.edtVariant);

            edtPrice = currentRow.findViewById(R.id.edtPrice);
            edtQuantity = currentRow.findViewById(R.id.edtQuantity);
            edtDiscount = currentRow.findViewById(R.id.edtDiscount);

            edtVariant.setText(productJson.getString("prodvariant"));
            edtPrice.setText(productJson.getString("prodprice"));
            edtQuantity.setText(productJson.getString("prodquantity"));
            edtDiscount.setText(productJson.getString("proddiscount"));
        }

        json = jarr.getJSONArray(2);
        int count = 0;
        for (int i = 0; i < json.length(); i++) {
            //Add image into imageview
            productJson = json.getJSONObject(i);
            try {
                switch (count) {
                    case 0:
                        ImageID.add(productJson.getString("imageid"));
                        bitmapArray.add(getBitmapfromUrl(productJson.getString("imageurl")));
                        if(bitmapArray.get(0)!=null){
                            checkimage[0] = true;
                            ImageCover.setImageBitmap(bitmapArray.get(0));
                        }
                        break;
                    case 1:
                        ImageID.add(productJson.getString("imageid"));
                        bitmapArray.add(getBitmapfromUrl(productJson.getString("imageurl")));
                        if(bitmapArray.get(1)!=null) {
                            checkimage[1] = true;
                            Image1.setImageBitmap(bitmapArray.get(1));
                        }
                        break;
                    case 2:
                        ImageID.add(productJson.getString("imageid"));
                        bitmapArray.add(getBitmapfromUrl(productJson.getString("imageurl")));
                        if(bitmapArray.get(2)!=null) {
                            checkimage[2] = true;
                            Image2.setImageBitmap(bitmapArray.get(2));
                        }
                        break;
                    case 3:
                        ImageID.add(productJson.getString("imageid"));
                        bitmapArray.add(getBitmapfromUrl(productJson.getString("imageurl")));
                        if(bitmapArray.get(3)!=null) {
                            checkimage[3] = true;
                            Image3.setImageBitmap(bitmapArray.get(3));
                        }
                        break;
                    case 4:
                        ImageID.add(productJson.getString("imageid"));
                        bitmapArray.add(getBitmapfromUrl(productJson.getString("imageurl")));
                        if(bitmapArray.get(4)!=null) {
                            checkimage[4] = true;
                            Image4.setImageBitmap(bitmapArray.get(4));
                        }
                        break;
                    default:
                        break;
                }
                count++;
            } catch (JSONException e) {
                Crashlytics.logException(e);
                // handle your exception here!
                e.printStackTrace();
            }
        }
        isLoad=true;
        progressDialog.dismiss();
    }

    //Add more variantView
    private void addVariant(){
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = Objects.requireNonNull(inflater).inflate(R.layout.variant_field, null);


        // Add the new row before the add field button.
        rowView.findViewById(R.id.btnDeleteVariant).setTag("0null");

        parentLinearLayout.addView(rowView, parentLinearLayout.getChildCount() - 1);
    }

    //Delete variantView
    public void onDelete(final View v) {
        String tag = v.getTag().toString();
        Toast.makeText(ItemUpdateActivity.this, tag, Toast.LENGTH_SHORT).show();
        if(v.getTag().toString().equals("0null")){
            parentLinearLayout.removeView((View) v.getParent());
        }else{
            AlertDialog.Builder builder = new AlertDialog.Builder(ItemUpdateActivity.this);
            builder.setTitle(getResources().getString(R.string.deleteVar));
            builder.setMessage(getResources().getString(R.string.deleteVarMsg));
            builder.setIcon(R.mipmap.ic_erase);
            builder.setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    if(!(v.getTag().toString().equals("0null"))){
                        deleteVariantFromServer(v.getTag().toString());
                    }
                    parentLinearLayout.removeView((View) v.getParent());
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void deleteVariantFromServer(final String variant){
        class AsyncTaskUploadClass extends AsyncTask<Void,Void,String> {

            @Override
            protected void onPostExecute(String string1) {
                Log.d("Responseee",string1);
                super.onPostExecute(string1);
            }

            @Override
            protected String doInBackground(Void... params) {
                UploadProcess imageProcessClass = new UploadProcess();
                HashMap<String, String> HashMapParams = new HashMap<>();
                HashMapParams.put("DeleteVariant", variant);
                HashMapParams.put("VariantProdCode",prodcode);
                return imageProcessClass.HttpRequest(ItemUpdatePathOnServer, HashMapParams);
            }
        }
        AsyncTaskUploadClass AsyncTaskUploadClassOBJ = new AsyncTaskUploadClass();
        AsyncTaskUploadClassOBJ.execute();
    }

    @Override
    public void onClick(View view) {
        int i;
        switch (view.getId()) {
            case R.id.coverphoto:
                i = 0;
                selectimage(i);
                break;
            case R.id.image1:
                i = 1;
                selectimage(i);
                break;
            case R.id.image2:
                i = 2;
                selectimage(i);
                break;
            case R.id.image3:
                i = 3;
                selectimage(i);
                break;
            case R.id.image4:
                i = 4;
                selectimage(i);
                break;
            case R.id.btnSubmit1:
                 check=false;
                final int childCount = parentLinearLayout.getChildCount();
                for(int c = 0;c<childCount;c++) {
                    RelativeLayout currentRow = (RelativeLayout)parentLinearLayout.getChildAt(c);
                    edtVariant = currentRow.findViewById(R.id.edtVariant);
                    edtPrice = currentRow.findViewById(R.id.edtPrice);
                    edtQuantity = currentRow.findViewById(R.id.edtQuantity);
                    edtDiscount = currentRow.findViewById(R.id.edtDiscount);
                    if (edtPrice.getText().toString().isEmpty()) {
                        edtPrice.setError(getResources().getString(R.string.emptyMsg));
                        check = true;
                    }
                    if (edtQuantity.getText().toString().isEmpty()) {
                        edtQuantity.setError(getResources().getString(R.string.emptyMsg));
                        check = true;
                    }
                    if (edtDiscount.getText().toString().isEmpty()) {
                        edtDiscount.setError(getResources().getString(R.string.emptyMsg));
                        check = true;
                    }
                    if (edtVariant.getText().toString().isEmpty()) {
                        edtVariant.setError(getResources().getString(R.string.emptyMsg));
                        check = true;
                    }
                }
                if(remove[0]==true){
                    check=true;
                    Toast.makeText(this, getResources().getString(R.string.coverImgMsg), Toast.LENGTH_SHORT).show();
                }


                if(Desc.getText().toString().isEmpty()){
                    Desc.setError(getResources().getString(R.string.emptyMsg));
                    check=true;
                }
                if(Name.getText().toString().isEmpty()){
                    Name.setError(getResources().getString(R.string.emptyMsg));
                    check=true;
                }

                if(check==false){
                    try {
                        UploadToServerFunction();
                    } catch (JSONException e) {
                        Crashlytics.logException(e);
                        // handle your exception here!
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.btnDelete2:
                DeleteFromServerFunction();
                break;
            case R.id.btnAddVariant:
                addVariant();
                break;
            default:
                break;
        }
    }
    //Convert imageUrl to Bitmap function
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

    /**
     * This method is responsible for solving the rotation issue if exist. Also scale the images to
     * 1024x1024 resolution
     *
     * @param context       The current context
     * @param selectedImage The Image URI
     * @return Bitmap image results
     * @throws IOException
     */
    private static Bitmap handleSamplingAndRotationBitmap(Context context, Uri selectedImage)
            throws IOException {
        int MAX_HEIGHT = 1024;
        int MAX_WIDTH = 1024;

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream imageStream = context.getContentResolver().openInputStream(selectedImage);
        BitmapFactory.decodeStream(imageStream, null, options);
        Objects.requireNonNull(imageStream).close();

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        imageStream = context.getContentResolver().openInputStream(selectedImage);
        Bitmap img = BitmapFactory.decodeStream(imageStream, null, options);

        img = rotateImageIfRequired(img, selectedImage);
        return img;
    }

    /**
     * Calculate an inSampleSize for use in a {@link BitmapFactory.Options} object when decoding
     * bitmaps using the decode* methods from {@link BitmapFactory}. This implementation calculates
     * the closest inSampleSize that will result in the final decoded bitmap having a width and
     * height equal to or larger than the requested width and height. This implementation does not
     * ensure a power of 2 is returned for inSampleSize which can be faster when decoding but
     * results in a larger bitmap which isn't as useful for caching purposes.
     *
     * @param options   An options object with out* params already populated (run through a decode*
     *                  method with inJustDecodeBounds==true
     * @param reqWidth  The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return The value to be used for inSampleSize
     */
    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
            // with both dimensions larger than or equal to the requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down further
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }

    /**
     * Rotate an image if required.
     *
     * @param img           The image bitmap
     * @param selectedImage Image URI
     * @return The resulted Bitmap after manipulation
     */
    private static Bitmap rotateImageIfRequired(Bitmap img, Uri selectedImage) throws IOException {

        ExifInterface ei = new ExifInterface(selectedImage.getPath());
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    private boolean EnableRuntimePermission() {
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
            return true;
        }else{
            return false;
        }
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
        }else uid = firebaseAuth.getCurrentUser().getUid();
    }

    @Override
    public void onRequestPermissionsResult(int RC, String per[], int[] PResult) {
     //Result after grant permission
        switch (RC) {

            case RequestPermissionCode:

                if (!(PResult.length > 0 && PResult[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(ItemUpdateActivity.this, getResources().getString(R.string.camPermissionDenied), Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }


    private void createSerialDialog(){
        serialDialog = new Dialog(ItemUpdateActivity.this, R.style.MaterialDialogSheet);
        serialDialog.setContentView(R.layout.serial_dialog); // your custom view.
        serialDialog.setCancelable(true);
        Objects.requireNonNull(serialDialog.getWindow()).setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        serialDialog.getWindow().setGravity(Gravity.BOTTOM);

        TextView serialVariant = serialDialog.findViewById(R.id.serialVariantLabel);
        serialRecycler=serialDialog.findViewById(R.id.serialRecycler);
        ImageButton serialExit = serialDialog.findViewById(R.id.serialExit);
        serialText=serialDialog.findViewById(R.id.serialText);
        Button newSerialBut = serialDialog.findViewById(R.id.newSerialBut);


        newSerialBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serialText.setEnabled(false);
                INSERT_SERIAL_CODE_CALL();
                SERIAL_CODE_CALL();
            }
        });



        serialRecycler.setHasFixedSize(true);
        layoutManagerOfrecyclerView = new LinearLayoutManager(ItemUpdateActivity.this);
        serialRecycler.setLayoutManager(layoutManagerOfrecyclerView);


        serialExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serialDialog.dismiss();
            }
        });
    }


    private void SERIAL_CODE_CALL(){
        class AsyncTaskUploadClass extends AsyncTask<Void,Void,String> {
            @Override
            protected void onPreExecute() {
            //    progressDialog.show();
                loadingDialog.show();
                super.onPreExecute();
                // canceldialog.show();
            }

            @Override
            protected void onPostExecute(String response) {
                super.onPostExecute(response);
                Log.e("Serial response",response);
                try{
                    SERIAL_ParseJSonResponse(response);
                }catch (Exception e){
                    Log.e("Serial response",e.toString());
                }

                // JSON_HTTP_CALL();
                //canceldialog.dismiss();
                //Toast.makeText(OrderConfirmActivity.this,"Done cancel order",Toast.LENGTH_SHORT).show();

            }

            @Override
            protected String doInBackground(Void... params) {
                UploadProcess imageProcessClass = new UploadProcess();

                HashMap<String,String> HashMapParams = new HashMap<>();

                Log.d("retailer id",uid);
                HashMapParams.put("fetchSerial",prodcode);

                return imageProcessClass.HttpRequest(ItemUpdatePathOnServer, HashMapParams);
            }
        }
        AsyncTaskUploadClass AsyncTaskUploadClass = new AsyncTaskUploadClass();
        AsyncTaskUploadClass.execute();
    }

    private void SERIAL_ParseJSonResponse(String array) throws JSONException {
        // product = new Product();
        JSONArray jarr = new JSONArray(array);//lv 1 array

        serialList=new ArrayList<>();
        for(int a=0;a<jarr.length();a++){
            SerialCode x=new SerialCode();
            JSONObject json;
            json=jarr.getJSONObject(a);

            x.setSerial(json.getString("serial"));
            x.setSerialID(json.getString("serialID"));

            serialList.add(x);

            Log.e("added list",json.getString("serial"));
        }

        RecyclerView.Adapter recyclerViewadapter = new SerialCodeAdapter(serialList, ItemUpdateActivity.this, this);
        serialRecycler.setAdapter(recyclerViewadapter);
        loadingDialog.dismiss();
       // progressDialog.dismiss();
    }

    private void INSERT_SERIAL_CODE_CALL(){
        class AsyncTaskUploadClass extends AsyncTask<Void,Void,String> {

            @Override
            protected void onPostExecute(String response) {
                super.onPostExecute(response);
                Toast.makeText(ItemUpdateActivity.this,response,Toast.LENGTH_SHORT).show();
                Answers.getInstance().logCustom(new CustomEvent("Product")
                        .putCustomAttribute("Type", "Add Serial Number"));
                serialText.setText("");
                serialText.setEnabled(true);
                // JSON_HTTP_CALL();
                //canceldialog.dismiss();
                //Toast.makeText(OrderConfirmActivity.this,"Done cancel order",Toast.LENGTH_SHORT).show();

            }

            @Override
            protected String doInBackground(Void... params) {
                UploadProcess imageProcessClass = new UploadProcess();

                HashMap<String,String> HashMapParams = new HashMap<>();

                Log.d("retailer id",uid);
                HashMapParams.put("insertSerialProdcode",prodcode);
                HashMapParams.put("insertSerial",serialText.getText().toString());
                return imageProcessClass.HttpRequest(ItemUpdatePathOnServer, HashMapParams);
            }
        }
        AsyncTaskUploadClass AsyncTaskUploadClass = new AsyncTaskUploadClass();
        AsyncTaskUploadClass.execute();
    }

}

