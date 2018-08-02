package com.ecommerce.merchant.fypproject.adapter;

import android.Manifest;
import android.app.Activity;
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
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ecommerce.merchant.fypproject.R;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class OrderConfirmationAdapter extends RecyclerView.Adapter<OrderConfirmationAdapter.ViewHolder> {
    private final List<OrderConfirm> dataAdapters;
    private final Context context;
    private static final int RequestPermissionCode  = 1 ;
    private OrderConfirmationAdapter.ViewHolder currentView;
    private final ArrayList<String> ConvertImage= new ArrayList<>();
    private final ArrayList<String> OrderConfirmDetail= new ArrayList<>();
    private final String PHPURL = "http://ecommercefyp.000webhostapp.com/retailer/manage_retailer_product.php";
    //String PHPURL = "http://10.0.2.2/cashierbookPHP/Eric/manage_retailer_product.php";
    private Dialog viewPhotoDialog;
    private ProgressDialog progressDialog ;
    private ImageView img1, img2;
    private final int REQUEST_CAMERA_1 = 100, REQUEST_CAMERA_2 = 200;
    private String mImageFileLocation;
    private final Bitmap[] bitmap = {null,null};
    private int clickposition = 0;
    private final OnItemClick mCallback;
    //private OnItemClick mCallback;


    public OrderConfirmationAdapter(List<OrderConfirm> getDataAdapter, Context context, OnItemClick listener) {

        super();
        this.dataAdapters = getDataAdapter;
        this.context = context;
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        mCallback=listener;


    }

    @Override
    public OrderConfirmationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_confirm_card, parent, false);

        OrderConfirmationAdapter.ViewHolder viewHolder = new OrderConfirmationAdapter.ViewHolder(view);

        createViewPhotoDialog();
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final OrderConfirmationAdapter.ViewHolder Viewholder, final int position) {
        final OrderConfirm dataAdapterOBJ = dataAdapters.get(position);

        Viewholder.custname.setText(dataAdapterOBJ.getCustName());
        Viewholder.orderdate.setText(dataAdapterOBJ.getOrderDate());
        Viewholder.itemname.setText(dataAdapterOBJ.getItemName());
        Viewholder.variant.setText(dataAdapterOBJ.getVariant());
        Viewholder.qty.setText(dataAdapterOBJ.getQty());

        Viewholder.snapPhotoBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickposition=Viewholder.getAdapterPosition();

                viewPhotoDialog.show();


            }
        });

        Viewholder.outOfStockBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(context)
                        .setMessage(context.getResources().getString(R.string.outOfStockMsg))
                        .setCancelable(false)
                        .setPositiveButton(context.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //System.exit(0);
                                String custid=dataAdapterOBJ.getCustid();
                                String orderdate=dataAdapterOBJ.getOrderDate();
                                mCallback.onClick("refresh",null,null,null);
                                JSON_HTTP_CALL(custid,orderdate);
                                Answers.getInstance().logCustom(new CustomEvent("Confirm Order")
                                        .putCustomAttribute("Status","Out of Stock"));

                            }
                        })
                        .setNegativeButton(context.getResources().getString(R.string.no), null)
                        .show();
//                String custid=dataAdapterOBJ.getCustid();
//                String orderdate=dataAdapterOBJ.getOrderDate();
//                mCallback.onClick("refresh",null,null,null);
//                JSON_HTTP_CALL(custid,orderdate);
            }
        });

        Glide.with(context)
                .load(dataAdapterOBJ.getImgurl()) // image url
                .transition(GenericTransitionOptions.with(android.R.anim.fade_in))
                .apply(new RequestOptions()
                .placeholder(R.drawable.photo) // any placeholder to load at start
                .error(R.drawable.photo)  // any image in case of error
                .override(500, 500) // resizing
                .centerCrop())
                .into(Viewholder.img);
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView custname;
        final TextView orderdate;
        final TextView itemname;
        final TextView variant;
        final TextView qty;
        final Button snapPhotoBut;
        final Button outOfStockBut;
        final ImageView img;

        ViewHolder(View itemView) {

            super(itemView);

            custname = itemView.findViewById(R.id.customerNameText);
            orderdate = itemView.findViewById(R.id.orderDateText);
            itemname = itemView.findViewById(R.id.orderItemNameText);
            variant = itemView.findViewById(R.id.orderItemVariantText);
            qty = itemView.findViewById(R.id.orderItemQtyText);
            snapPhotoBut = itemView.findViewById(R.id.snapPhotoBut);
            outOfStockBut = itemView.findViewById(R.id.outOfStockBut);
            img = itemView.findViewById(R.id.orderItemImg);
        }
    }

    @Override
    public int getItemCount() {
        return dataAdapters.size();
    }


    private void createViewPhotoDialog() {
        viewPhotoDialog = new Dialog(context, R.style.MaterialDialogSheet);
        viewPhotoDialog.setContentView(R.layout.upload_photo_dialog); // your custom view.
        viewPhotoDialog.setCancelable(true);
        Objects.requireNonNull(viewPhotoDialog.getWindow()).setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        viewPhotoDialog.getWindow().setGravity(Gravity.CENTER);

        img1 = viewPhotoDialog.findViewById(R.id.realImg1);
        img2 = viewPhotoDialog.findViewById(R.id.realImg2);
        Button uploadBut = viewPhotoDialog.findViewById(R.id.uploadPhotobut);


        img1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //image 1 to upload
                    openCamera(REQUEST_CAMERA_1);
            }
        });

        img2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //image 2 to upload
                openCamera(REQUEST_CAMERA_2);
            }
        });

        uploadBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //upload photo function
                if (bitmap[0]!=null){

                    uploadToServer();
                }else{
                    Toast.makeText(context,context.getResources().getString(R.string.insertPhoto),Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void uploadToServer(){
        for(int a=0;a<2;a++){
            if(bitmap[a]!=null){
                ByteArrayOutputStream byteArrayOutputStreamObject;
                byteArrayOutputStreamObject = new ByteArrayOutputStream();

                // Converting bitmap image to jpeg format, so by default image will upload in jpeg format.
                bitmap[a].compress(Bitmap.CompressFormat.JPEG, 40, byteArrayOutputStreamObject);
                byte[] byteArrayVar = byteArrayOutputStreamObject.toByteArray();
                ConvertImage.add(Base64.encodeToString(byteArrayVar, Base64.DEFAULT));
            }
        }
        class AsyncTaskUploadClass extends AsyncTask<Void,Void,String> {

            @Override
            protected void onPreExecute() {

                super.onPreExecute();

                // Showing progress dialog at image upload time.adasassdsa
                progressDialog = ProgressDialog.show(context,context.getResources().getString(R.string.imgUploading),context.getResources().getString(R.string.plsWait),false,false);
            }

            @Override
            protected void onPostExecute(String string1) {
                Log.d("Responseee",string1);
                super.onPostExecute(string1);
                Answers.getInstance().logCustom(new CustomEvent("Confirm Order")
                .putCustomAttribute("Status",ConvertImage.size()+" Photo Uploaded"));
                // Dismiss the progress dialog after done uploading.
                progressDialog.dismiss();

                // Printing uploading success message coming from server on android app.
                Toast.makeText(context,context.getResources().getString(R.string.uploadScc),Toast.LENGTH_LONG).show();
                viewPhotoDialog.dismiss();
                mCallback.onClick("refresh",null,null,null);
                mCallback.onClick("done refresh",null,null,null);
            }
            @Override
            protected String doInBackground(Void... params) {
                OrderConfirmDetail.add(dataAdapters.get(clickposition).getCustid());
                OrderConfirmDetail.add(dataAdapters.get(clickposition).getOrderDate());

                JSONObject JSONOrderConfirmDetail = new JSONObject();
                JSONObject JSONOrderConfirmImage = new JSONObject();
                JSONObject EverythingJSON = new JSONObject();
                try {
                    JSONOrderConfirmDetail.put("custID",dataAdapters.get(clickposition).getCustid());
                    JSONOrderConfirmDetail.put("expiredDate",dataAdapters.get(clickposition).getOrderDate());
                } catch (JSONException e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                    // handle your exception here!
                }
                for (int i = 0; i < ConvertImage.size(); i++) {
                    try {
                        JSONOrderConfirmImage.put("Image:" + String.valueOf(i + 1), ConvertImage.get(i));
                    } catch (JSONException e) {
                        Crashlytics.logException(e);
                        // handle your exception here!
                        e.printStackTrace();
                    }
                }

                try {
                    EverythingJSON.put("details",JSONOrderConfirmDetail);
                    EverythingJSON.put("prodcode",dataAdapters.get(clickposition).getProdcode());
                    EverythingJSON.put("prodvariant",dataAdapters.get(clickposition).getVariant());
                    EverythingJSON.put("image",JSONOrderConfirmImage);
                } catch (JSONException e) {
                    Crashlytics.logException(e);
                    // handle your exception here!
                    e.printStackTrace();
                }

                UploadProcess imageProcessClass = new UploadProcess();
                HashMap<String, JSONObject> HashMapParams = new HashMap<>();
                HashMapParams.put("confirmOrderImage", EverythingJSON);
                Log.e("EverythingJSON ",EverythingJSON.toString());

                return imageProcessClass.HttpRequestObject(PHPURL, HashMapParams);
            }
        }
        AsyncTaskUploadClass AsyncTaskUploadClassOBJ = new AsyncTaskUploadClass();
        AsyncTaskUploadClassOBJ.execute();


    }

    private void openCamera(int CameraCode) {
        if(!EnableRuntimePermission()){
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            File photo = null;
            try {
                // place where to store camera taken picture
                photo = createTemporaryFile();
                photo.delete();
            } catch (Exception e) {
                Log.e("ImageCapture", e.toString());
                Toast.makeText(context, context.getResources().getString(R.string.plsChkSD), Toast.LENGTH_LONG).show();
            }
            //File provider to generate files URI
            String authorities = context.getApplicationContext().getPackageName() + ".fileprovider";
            Log.d("photo", Objects.requireNonNull(photo).toString());
            Uri mImageUri = FileProvider.getUriForFile(context, authorities, photo);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
            //start camera intent

            ((Activity) context).startActivityForResult(Intent.createChooser(intent, context.getResources().getString(R.string.selectFromCamera)), CameraCode);
        }
    }

    //Get directory and create a temp jpg file
    private File createTemporaryFile() throws Exception {
        File tempDir = Environment.getExternalStorageDirectory();
        tempDir = new File(tempDir.getAbsolutePath() + "/.temp/");
        Log.d("Directory", tempDir.toString());
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        File image = File.createTempFile("temp", ".jpg", tempDir);
        mImageFileLocation = image.getAbsolutePath();
        return image;
    }

    //get URI from image path then return into a bitmap with resize and rotate
    private Bitmap grabImage() throws IOException {
        Uri uri = Uri.fromFile(new File(mImageFileLocation));
        return handleSamplingAndRotationBitmap(context,uri);
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

    public void onActivityResult (int requestCode, int resultCode, Intent data) throws IOException {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CAMERA_1:
                     bitmap[0] = grabImage();
                    img1.setImageBitmap(bitmap[0]);
                    break;
                case REQUEST_CAMERA_2:
                    bitmap[1] = grabImage();
                    img2.setImageBitmap(bitmap[1]);
                    break;
                default:
                    break;
                }
            }

        }

    private void JSON_HTTP_CALL(final String custid, final String orderdate){
        StringRequest RequestOfJSonArray = new StringRequest(Request.Method.POST, PHPURL,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        mCallback.onClick("done refresh",null,null,null);

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
                params.put("outofstock", orderdate);
                params.put("custid",custid);


                return params;
            }
        };


        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(RequestOfJSonArray);
    }

    private boolean EnableRuntimePermission(){
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        !=PackageManager.PERMISSION_GRANTED&& (ContextCompat.checkSelfPermission(
                                context,Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED)) {
            // Permission is not granted, then request for permission
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE}, RequestPermissionCode);
            return true;
        }

        else{
            return false;
        }
        //check permission is granted or not
//        if (ActivityCompat.shouldShowRequestPermissionRationale((Actisvity) context, Manifest.permission.CAMERA)&&
//                ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.WRITE_EXTERNAL_STORAGE)&&
//                ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.READ_EXTERNAL_STORAGE))
//        {
    }
}


