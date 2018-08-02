package com.ecommerce.merchant.fypproject;

import android.Manifest;
import android.app.AlertDialog;
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
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.ecommerce.merchant.fypproject.adapter.DecimalDigitsInputFilter;
import com.ecommerce.merchant.fypproject.adapter.UploadProcess;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class RegisterProductActivity extends AppCompatActivity implements View.OnClickListener {
        private LinearLayout parentLinearLayout;
        private Button  UploadImageToServer;
        private Button btnVariant;
        private Uri mImageUri;
        private String mImageFileLocation;
        private String uid;
        private ImageView coverImage;
        private ImageView image1;
    private ImageView image2;
    private ImageView image3;
    private ImageView image4;

        private EditText Name;
    private EditText Desc;
    private EditText edtVariant;
    private EditText edtPrice;
    private EditText edtDiscount;
    private EditText edtQuantity;
        private boolean check;
        private final boolean[] checkimage ={false,false,false,false,false};
        private Spinner SpinnerCategory;
    private Spinner Size;
        private final ArrayList<String> ConvertImage= new ArrayList<>();
    private final ArrayList<String> ProductDetails = new ArrayList<>();
        private final ArrayList<JSONObject> VariantList = new ArrayList();
        private final ArrayList<Bitmap> bitmapArray = new ArrayList<>();
        private Integer img;
    private Integer select;
        private static final int RequestPermissionCode  = 1 ;
        private Bitmap bitmapcover;
    private Bitmap bitmap1;
    private Bitmap bitmap2;
    private Bitmap bitmap3;
    private Bitmap bitmap4;
        private ProgressDialog progressDialog ;
        private final String  ProductPathFieldOnServer="addProd";
        //String AddProductPathOnServer = "http://10.0.2.2/cashierbookPHP/Eric/manage_retailer_product.php";
        private final String AddProductPathOnServer = "http://ecommercefyp.000webhostapp.com/retailer/manage_retailer_product.php";
        private FirebaseAuth firebaseAuth;
        private CharSequence[] items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        parentLinearLayout = findViewById(R.id.parentLinear);
        Toolbar toolbar = findViewById(R.id.selltoolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getResources().getString(R.string.registerProdDetail));

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        //Get firebase Instance
        GetFirebaseAuth();

        //Initiative Variable
        coverImage = findViewById(R.id.coverphoto);
        image1 = findViewById(R.id.image1);
        image2 = findViewById(R.id.image2);
        image3 = findViewById(R.id.image3);
        image4 = findViewById(R.id.image4);
        btnVariant = findViewById(R.id.btnAddVariant);
        UploadImageToServer = findViewById(R.id.btnSubmit1);
        Name = findViewById(R.id.edtTitle2);
        Desc = findViewById(R.id.edtDesc2);
        SpinnerCategory = findViewById(R.id.spinnerCategory1);
        Size = findViewById(R.id.spinnerSize);


        //Set listener
        coverImage.setOnClickListener(this);
        image1.setOnClickListener(this);
        image2.setOnClickListener(this);
        image3.setOnClickListener(this);
        image4.setOnClickListener(this);
        btnVariant.setOnClickListener(this);
        UploadImageToServer.setOnClickListener(this);

        RelativeLayout currentRow = (RelativeLayout) parentLinearLayout.getChildAt(0);
        edtPrice = currentRow.findViewById(R.id.edtPrice);
        edtPrice.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(7,2)});
    }

    //Select upload image sources
    private void selectimage(int i){
        select = i;
        img = i;
        switch (select){
            //prompt dialog message with remove button or without remove button
            case 0:
                if(checkimage[0]){
                    items= getResources().getStringArray(R.array.select_array);
                }else{
                    items= getResources().getStringArray(R.array.select_arraywithoutRemove);
                }
                break;
            case 1:
                if(checkimage[1]){
                    items= getResources().getStringArray(R.array.select_array);
                }else{
                    items= getResources().getStringArray(R.array.select_arraywithoutRemove);
                }
                break;
            case 2:
                if(checkimage[2]){
                    items= getResources().getStringArray(R.array.select_array);
                }else{
                    items= getResources().getStringArray(R.array.select_arraywithoutRemove);
                }
                break;
            case 3:
                if(checkimage[3]){
                    items= getResources().getStringArray(R.array.select_array);
                }else{
                    items= getResources().getStringArray(R.array.select_arraywithoutRemove);
                }
                break;
            case 4:
                if(checkimage[4]){
                    items= getResources().getStringArray(R.array.select_array);
                }else{
                    items= getResources().getStringArray(R.array.select_arraywithoutRemove);
                }
                break;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterProductActivity.this);
        builder.setTitle( "Add Image" );
        builder.setItems( items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if(items[i].equals("Camera")){
                    if(!EnableRuntimePermission()) {
                        //Create a temporary file to store captured image
                        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        File photo = null;
                        try {
                            // place where to store camera taken picture
                            photo = createTemporaryFile("temp");
                            photo.delete();
                        } catch (Exception e) {
                            Log.e("ImageCapture", e.toString());
                            Toast.makeText(RegisterProductActivity.this, getResources().getString(R.string.plsChkSD), Toast.LENGTH_LONG).show();
                        }
                        //File provider to generate files URI
                        mImageUri = FileProvider.getUriForFile(getApplicationContext(), "com.ecommerce.merchant.fypproject.fileprovider", Objects.requireNonNull(photo));
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
                        //start camera intent
                        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.selectFromCamera)), img);
                    }
                }else if(items[i].equals( "Gallery" )){
                    //Intent Gallery to get images
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    img+=10;
                    startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.selectFromGallery)), img);
                }else if(items[i].equals( "Cancel" )){
                    dialog.dismiss();
                }else if(items[i].equals("Remove")){
                    //Remove the image from image view
                    switch (select){
                        case 0:
                            bitmapcover=null;
                            checkimage[0]=false;
                            coverImage.setImageDrawable(getResources().getDrawable(R.drawable.photo));
                            break;
                        case 1:
                            bitmap1=null;
                            checkimage[1]=false;
                            image1.setImageDrawable(getResources().getDrawable(R.drawable.photo));
                            break;
                        case 2:
                            bitmap2=null;
                            checkimage[2]=false;
                            image2.setImageDrawable(getResources().getDrawable(R.drawable.photo));
                            break;
                        case 3:
                            bitmap3=null;
                            checkimage[3]=false;
                            image3.setImageDrawable(getResources().getDrawable(R.drawable.photo));
                            break;
                        case 4:
                            bitmap4=null;
                            checkimage[4]=false;
                            image4.setImageDrawable(getResources().getDrawable(R.drawable.photo));
                            break;
                        default:
                            break;
                    }
                }
            }
        } );
        builder.show();
    }

    //Get directory and create a temp jpg file
    private File createTemporaryFile(String part) throws Exception
    {
        File tempDir= Environment.getExternalStorageDirectory();
        tempDir=new File(tempDir.getAbsolutePath()+"/.temp/");
        Log.d("Directory",tempDir.toString());
        if(!tempDir.exists())
        {
            tempDir.mkdirs();
        }
        File image = File.createTempFile(part, ".jpg", tempDir);
        mImageFileLocation = image.getAbsolutePath();
        return image;
    }

    //get the image file from URI then convert into bitmap
    private Bitmap grabImage() throws IOException {
        Uri uri = Uri.fromFile(new File(mImageFileLocation));
        return handleSamplingAndRotationBitmap(getApplicationContext(),uri);
    }

    //After select image sources result
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Uri uri;
               // Uri uri = data.getData();
            switch(requestCode){
                //set the bitmap into imageview
                case 0:
                    try {
                        bitmapcover = grabImage();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    checkimage[0]=true;
                    coverImage.setImageBitmap(bitmapcover);
                    break;
                case 1:
                    try {
                        bitmap1 =grabImage();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    checkimage[1]=true;
                    image1.setImageBitmap(bitmap1);
                    break;
                case 2:
                    try {
                        bitmap2 =grabImage();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    checkimage[2]=true;
                    image2.setImageBitmap(bitmap2);
                    break;
                case 3:
                    try {
                        bitmap3 =grabImage();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    checkimage[3]=true;
                    image3.setImageBitmap(bitmap3);
                    break;
                case 4:
                    try {
                        bitmap4 =grabImage();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    checkimage[4]=true;
                    image4.setImageBitmap(bitmap4);
                    break;
                case 10:
                    uri = data.getData();
                    if (uri != null) {
                        try {

                            bitmapcover = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    checkimage[0]=true;
                    coverImage.setImageBitmap(bitmapcover);
                    break;

                case 11:
                    uri = data.getData();
                    if (uri != null) {
                        try {
                            bitmap1 = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    checkimage[1]=true;
                    image1.setImageBitmap(bitmap1);
                    break;

                case 12:
                    uri = data.getData();
                    if (uri != null) {
                        try {
                            bitmap2 = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    checkimage[2]=true;
                    image2.setImageBitmap(bitmap2);
                    break;

                case 13:
                    uri = data.getData();
                    if (uri != null) {
                        try {
                            bitmap3 = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    checkimage[3]=true;
                    image3.setImageBitmap(bitmap3);
                    break;
                case 14:
                    uri = data.getData();
                    if (uri != null) {
                        try {
                            bitmap4 = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    checkimage[4]=true;
                    image4.setImageBitmap(bitmap4);
                    break;
            }
        }
    }

    //Add product details to the server
    private void UploadToServerFunction() throws JSONException {

        Answers.getInstance().logCustom(new CustomEvent("Product")
                .putCustomAttribute("Type", "Register"));

        for(int a=0;a<bitmapArray.size();a++){
            ByteArrayOutputStream byteArrayOutputStreamObject;
            byteArrayOutputStreamObject = new ByteArrayOutputStream();

            // Converting bitmap image to jpeg format, so by default image will upload in jpeg format.
            bitmapArray.get(a).compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStreamObject);
            byte[] byteArrayVar = byteArrayOutputStreamObject.toByteArray();
            ConvertImage.add(Base64.encodeToString(byteArrayVar, Base64.DEFAULT));
        }
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

            //Put array into arrayList
            VariantList.add(JSONVariant);
        }

        class AsyncTaskUploadClass extends AsyncTask<Void,Void,String> {

            @Override
            protected void onPreExecute() {

                super.onPreExecute();


                // Showing progress dialog at image upload time.
                progressDialog = ProgressDialog.show(RegisterProductActivity.this,getResources().getString(R.string.itemUploading),getResources().getString(R.string.plsWait),false,false);
            }

            @Override
            protected void onPostExecute(String string1) {
                Log.d("Responseeeeeee",string1);
                super.onPostExecute(string1);



                // Dismiss the progress dialog after done uploading.
                progressDialog.dismiss();

                // Printing uploading success message coming from server on android app.
                Toast.makeText(RegisterProductActivity.this,getResources().getString(R.string.itemRegisterSucc),Toast.LENGTH_LONG).show();
                finish();

                Intent intent = new Intent(RegisterProductActivity.this,ItemUpdateActivity.class);
                //Pack Data to Send
                intent.putExtra("prodcode_KEY",string1);
                intent.putExtra("addSerial","yes");
                //open activity
                startActivity(intent);
            }
            @Override
            protected String doInBackground(Void... params) {
                ProductDetails.add(Name.getText().toString());
                ProductDetails.add(SpinnerCategory.getSelectedItem().toString());
                ProductDetails.add(Desc.getText().toString());
                ProductDetails.add(Size.getSelectedItem().toString());
                ProductDetails.add(uid);

                JSONObject JSONProduct = new JSONObject();
                JSONObject JSONVariantAll = new JSONObject();
                JSONObject JSONImage = new JSONObject();
                JSONObject EverythingJSON = new JSONObject();

                for (int i = 0; i < ConvertImage.size(); i++) {
                    try {
                        JSONImage.put("Image:" + String.valueOf(i + 1), ConvertImage.get(i));
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
                        e.printStackTrace();
                        Crashlytics.logException(e);
                        // handle your exception here!
                    }
                }

                for (int i = 0; i < ProductDetails.size(); i++) {
                    try {
                        JSONProduct.put("Details:" + String.valueOf(i + 1), ProductDetails.get(i));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Crashlytics.logException(e);
                        // handle your exception here!
                    }
                }
                try {
                    EverythingJSON.put("details",JSONProduct);
                    EverythingJSON.put("image",JSONImage);
                    EverythingJSON.put("variant",JSONVariantAll);
                } catch (JSONException e) {
                    Crashlytics.logException(e);
                    // handle your exception here!
                    e.printStackTrace();
                }

                UploadProcess imageProcessClass = new UploadProcess();
                HashMap<String, JSONObject> HashMapParams = new HashMap<>();
                HashMapParams.put(ProductPathFieldOnServer, EverythingJSON);
                Log.e("EverythingJSON ",EverythingJSON.toString());

                return imageProcessClass.HttpRequestObject(AddProductPathOnServer, HashMapParams);
            }
        }
        AsyncTaskUploadClass AsyncTaskUploadClassOBJ = new AsyncTaskUploadClass();
        AsyncTaskUploadClassOBJ.execute();
    }

    //Delete variantView
    public void onDelete(View v) {

        parentLinearLayout.removeView((View) v.getParent());
    }
    //Add more variantView

    private void addVariant(){

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = Objects.requireNonNull(inflater).inflate(R.layout.variant_field, null);
        // Add the new row before the add field button.


        parentLinearLayout.addView(rowView, parentLinearLayout.getChildCount() - 1);
        final int childCount = parentLinearLayout.getChildCount();
        for(int c = 0;c<childCount;c++) {
            RelativeLayout currentRow = (RelativeLayout) parentLinearLayout.getChildAt(c);
            edtPrice = currentRow.findViewById(R.id.edtPrice);
            edtPrice.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(7,2)});
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

    @Override
    public void onRequestPermissionsResult(int RC, String per[], int[] PResult) {

        switch (RC) {

            case RequestPermissionCode:

                if (!(PResult.length > 0 && PResult[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(RegisterProductActivity.this,getResources().getString(R.string.camPermissionDenied), Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    public  boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId()==android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        int i;
        switch(view.getId()){
            case R.id.coverphoto:
                i=0;
                selectimage(i);
                break;
            case R.id.image1:
                i=1;
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
                //Validation
                check= false;
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
                        }else if(edtPrice.getText().toString().charAt(0)=='.'){
                            edtPrice.setError("Invalid input");
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
               // getCategory = SpinnerCategory.getSelectedItem().toString();
                if(Name.getText().toString().isEmpty()){
                    Name.setError(getResources().getString(R.string.emptyMsg));
                    check=true;
                }
                if(Desc.getText().toString().isEmpty()){
                    Desc.setError(getResources().getString(R.string.emptyMsg));
                    check=true;
                }
                if(bitmapcover!=null){
                    bitmapArray.add(bitmapcover);
                }
                if(bitmap1!=null){
                    bitmapArray.add(bitmap1);
                }
                if(bitmap2!=null){
                    bitmapArray.add(bitmap2);
                }
                if(bitmap3!=null){
                    bitmapArray.add(bitmap3);
                }
                if(bitmap4!=null){
                    bitmapArray.add(bitmap4);
                }
                if(bitmapArray.isEmpty()){
                    check=true;
                    Toast.makeText(this, getResources().getString(R.string.coverImgMsg), Toast.LENGTH_SHORT).show();
                }
                if(!check){
                    try {
                        UploadToServerFunction();
                    } catch (JSONException e) {
                        Crashlytics.logException(e);
                        // handle your exception here!
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.btnAddVariant:
                addVariant();
                break;
            default:
                break;
        }
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
        }else uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    }

    private boolean EnableRuntimePermission(){
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

}