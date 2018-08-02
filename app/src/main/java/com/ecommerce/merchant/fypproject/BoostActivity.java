package com.ecommerce.merchant.fypproject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ecommerce.merchant.fypproject.adapter.BoostItemAdapter;
import com.ecommerce.merchant.fypproject.adapter.DecimalDigitsInputFilter;
import com.ecommerce.merchant.fypproject.adapter.Product;
import com.ecommerce.merchant.fypproject.adapter.RecyclerViewAdapter;
import com.ecommerce.merchant.fypproject.adapter.UploadProcess;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.AddToCartEvent;
import com.crashlytics.android.answers.Answers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class BoostActivity extends AppCompatActivity {
    private final String AddCartToServerPath = "http://ecommercefyp.000webhostapp.com/retailer/manage_retailer_product.php";

    private Dialog boostDialog;
    private ImageView prodImage;
    private TextView itemName,periodDisplay,totalText,periodLabel,dayUnit;
    private Spinner periodSpinner;
    private EditText priceText,searchText;
    private Button addToCart,checkOut;
    private final DecimalFormat df2 = new DecimalFormat("0");
    private final DecimalFormat df3 = new DecimalFormat("0.00");
    private RecyclerView recyclerView;
    private String uid;
    private ImageView searchBut;
    private View view;
    private int RecyclerViewItemPosition ;
    private BoostItemAdapter ownRecycle;
    private final String MyPostPathOnServer = "http://ecommercefyp.000webhostapp.com/retailer/manage_retailer_product.php";
    private boolean doneLoad=false;
    private int loadIndex=0;
    private List<Product> ListOfProduct;
    private ProgressBar progressBar;
    private RelativeLayout layout;
    private Snackbar snackbar;
    private Snackbar snackbar2;
    private TextView txtnoresult;
    private final ArrayList<String> ProdCodeArrayListForClick = new ArrayList<>();
    private SoftKeyboard softKeyboard;
    private ProgressDialog placeCartDialog;
    private boolean doneplaceorder=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boost);

        doneplaceorder=false;
        Toolbar myposttoolbar = findViewById(R.id.boostToolbar);
        setSupportActionBar(myposttoolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getResources().getString(R.string.boostProduct));

        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        GetFirebaseAuth();
        layout=findViewById(R.id.boostRelative);
        snackbar = Snackbar.make(layout, "Loading more...", Snackbar.LENGTH_SHORT);
        (snackbar.getView()).setBackgroundColor(getResources().getColor(R.color.orange));
        snackbar2 = Snackbar.make(layout, "Reach bottom page", Snackbar.LENGTH_LONG);
        (snackbar2.getView()).setBackgroundColor(getResources().getColor(R.color.scarletRed));


        placeCartDialog= new ProgressDialog(BoostActivity.this);
        placeCartDialog.setCancelable(false);
        placeCartDialog.setTitle(getResources().getString(R.string.addingTocart));
        placeCartDialog.setMessage(getResources().getString(R.string.pleaseWait));
        placeCartDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);




        recyclerView=findViewById(R.id.boostRecycler);
        searchText=findViewById(R.id.searchText);
        searchBut=findViewById(R.id.searctBut);
        progressBar = findViewById(R.id.progressBar);
        txtnoresult= findViewById(R.id.txtNoResult);

        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchText.clearFocus();
                    hideKeyboard(BoostActivity.this);
                    loadIndex=0;
                    JSON_HTTP_CALL();

                    return true;
                }
                return false;
            }
        });

        searchBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchText.clearFocus();
                hideKeyboard(BoostActivity.this);
                loadIndex=0;
                JSON_HTTP_CALL();
            }
        });

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManagerOfrecyclerView = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManagerOfrecyclerView);

        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            final GestureDetector gestureDetector = new GestureDetector(BoostActivity.this, new GestureDetector.SimpleOnGestureListener() {

                @Override public boolean onSingleTapUp(MotionEvent motionEvent) {
                    return true;
                }

            });
            @Override
            public boolean onInterceptTouchEvent(RecyclerView Recyclerview, MotionEvent motionEvent) {

                view = Recyclerview.findChildViewUnder(motionEvent.getX(), motionEvent.getY());

                if(view != null && gestureDetector.onTouchEvent(motionEvent)) {

                    //Getting RecyclerView Clicked Item value.
                    RecyclerViewItemPosition = Recyclerview.getChildAdapterPosition(view);


                    String prodCode = ownRecycle.getListData().get(RecyclerViewItemPosition).getProdCode();
                    Toast.makeText(BoostActivity.this,prodCode,Toast.LENGTH_SHORT).show();
                   // itemUpdateActivity(prodCode);

                    //set all attribute first
                    Glide.with(BoostActivity.this)
                            .asBitmap()
                            .load(ownRecycle.getListData().get(RecyclerViewItemPosition).getProductURL()) // image url
                            .apply(new RequestOptions()
                                    .placeholder(R.drawable.photo) // any placeholder to load at start
                                    .error(R.drawable.photo)  // any image in case of error
                                    .override(1000, 1000) // resizing
                                    .centerCrop()).into(prodImage);



                    itemName.setText(ownRecycle.getListData().get(RecyclerViewItemPosition).getProdName());




                    priceText.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(7,2)});
                    priceText.addTextChangedListener(textWatcher);


                    if(ownRecycle.getListData().get(RecyclerViewItemPosition).getBoostPrice().equalsIgnoreCase("NA")){
                       periodSpinner.setVisibility(View.VISIBLE);
                        periodDisplay.setVisibility(View.INVISIBLE);
                        priceText.setEnabled(true);
                        totalText.setVisibility(View.VISIBLE);
                        checkOut.setVisibility(View.VISIBLE);
                        addToCart.setVisibility(View.VISIBLE);
                        priceText.setText("0.10");
                        periodLabel.setText(getResources().getString(R.string.period));
                        dayUnit.setVisibility(View.VISIBLE);

                    }else{
                        periodSpinner.setVisibility(View.INVISIBLE);
                        periodDisplay.setEnabled(false);
                        periodDisplay.setVisibility(View.VISIBLE);
                        periodDisplay.setTextColor(getResources().getColor(R.color.black));
                        periodDisplay.setText(ownRecycle.getListData().get(RecyclerViewItemPosition).getEndBoostDate());
                        priceText.setEnabled(false);
                        priceText.setTextColor(getResources().getColor(R.color.black));
                        priceText.setText(ownRecycle.getListData().get(RecyclerViewItemPosition).getBoostPrice());
                        totalText.setVisibility(View.GONE);
                        checkOut.setVisibility(View.GONE);
                        addToCart.setVisibility(View.GONE);
                        periodLabel.setText(getResources().getString(R.string.endDate));
                        dayUnit.setVisibility(View.GONE);
                    }
//                    periodDisplay=boostDialog.findViewById(R.id.dayLeftText);
//                    periodSpinner=boostDialog.findViewById(R.id.periodSpinner);

                    boostDialog.show();
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView Recyclerview, MotionEvent motionEvent) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1)&&doneLoad) {

                    //Toast.makeText(FindActivity.this,"LAst",Toast.LENGTH_LONG).show();
                    loadIndex+=6;
                    JSON_HTTP_CALL();

                }
            }
        });



        createBoostDialog();


        JSON_HTTP_CALL();

    }


    private void JSON_HTTP_CALL() {
        final String search=searchText.getText().toString();
        doneLoad=false;
        ListOfProduct = new ArrayList<>();
        class AsyncTaskUploadClass extends AsyncTask<Void,Void,String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if(loadIndex==0) {
                    progressBar.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.INVISIBLE);
                }else{
                    snackbar.show();
                }
                // Showing progress dialog at image upload time.
                //progressDialog = ProgressDialog.show(ItemUpdateActivity.this,"Item is Uploading","Please Wait",false,false);
            }
            @Override
            protected void onPostExecute(String response) {
                Log.e("boost Response",response);
                super.onPostExecute(response);
//                if(response.equalsIgnoreCase("[]")&&ownRecycle!=null){
//                    Toast.makeText(MyPostActivity.this,"Reach max result",Toast.LENGTH_LONG).show();
//                    ownRecycle.refreshList();
//                    recyclerView.setVisibility(View.VISIBLE);
//                    txtnoresult.setVisibility(View.INVISIBLE);
//                    progressBar.setVisibility(View.INVISIBLE);
//                    snackbar.dismiss();
//                    snackbar2.show();
//                }else if(response.equalsIgnoreCase("[]")&&ownRecycle==null){
//                    recyclerView.setVisibility(View.INVISIBLE);
//                    txtnoresult.setVisibility(View.VISIBLE);
//                    progressBar.setVisibility(View.INVISIBLE);
//                }else {
//
//
//                    try {
//                        txtnoresult.setVisibility(View.INVISIBLE);
//                        ParseJSonResponse(response);
//                        progressBar.setVisibility(View.INVISIBLE);
//                        recyclerView.setVisibility(View.VISIBLE);
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }



                if(response.equalsIgnoreCase("[]")&&loadIndex!=0){
                    //Toast.makeText(FindActivity.this,"Reach max result",Toast.LENGTH_LONG).show();
                    ownRecycle.refreshList();
                    recyclerView.setVisibility(View.VISIBLE);
                    txtnoresult.setVisibility(View.INVISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                    snackbar.dismiss();
                    snackbar2.show();

                }else if(response.equalsIgnoreCase("[]")&&loadIndex==0){
                    recyclerView.setVisibility(View.INVISIBLE);
                    txtnoresult.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                }else{
                    try {
                        txtnoresult.setVisibility(View.INVISIBLE);
                        ParseJSonResponse(response);
                        progressBar.setVisibility(View.INVISIBLE);
                        recyclerView.setVisibility(View.VISIBLE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Crashlytics.logException(e);
                        // handle your exception here!
                    }
                    Log.d("Response", response.toString());
                }

                // Dismiss the progress dialog after done uploading.
                //progressDialog.dismiss();
            }

            @Override
            protected String doInBackground(Void... voids) {
                UploadProcess imageProcessClass = new UploadProcess();
                HashMap<String, String> HashMapParams = new HashMap<>();
                HashMapParams.put("retailerProd",uid);
                HashMapParams.put("index",Integer.toString(loadIndex));
                HashMapParams.put("searchKey",search);
                return imageProcessClass.HttpRequest(MyPostPathOnServer, HashMapParams);
            }
        }
        AsyncTaskUploadClass AsyncTaskUploadClass = new AsyncTaskUploadClass();
        AsyncTaskUploadClass.execute();
    }

    //get response string and set into recyclerView
    private void ParseJSonResponse(String array) throws JSONException {

        JSONArray jarr = new JSONArray(array);
        ListOfProduct.clear();
        ProdCodeArrayListForClick.clear();

        for(int i = 0; i<jarr.length(); i++) {

            Product product=new Product();
            JSONObject json;
            try {
                json = jarr.getJSONObject(i);
                product.setProdCode(json.getString("prodcode"));
                product.setProdName(json.getString("prodname"));
                product.setProdPrice(Double.parseDouble(json.getString("price")));
                product.setShopName(json.getString("shopname"));
                product.setProdDiscount(Integer.parseInt(json.getString("discount")));
                product.setProductURL(json.getString("imageurl"));
                product.setEndBoostDate(json.getString("endboostdate"));
                product.setBoostPrice(json.getString("boostprice"));

                ProdCodeArrayListForClick.add(json.getString("prodcode"));


            } catch (JSONException e) {
                Crashlytics.logException(e);
                // handle your exception here!
                e.printStackTrace();
            }

            ListOfProduct.add(product);
        }
        RecyclerView.Adapter recyclerViewadapter = new RecyclerViewAdapter(ListOfProduct, this);

        doneLoad=true;


        if(loadIndex==0){
            ownRecycle=new BoostItemAdapter(ListOfProduct, this);

            recyclerViewadapter = ownRecycle;
            recyclerView.setAdapter(recyclerViewadapter);
        }else{
            //recyclerViewadapter.notifyItemRangeChanged(loadIndex,ListOfProduct.size());
            ownRecycle.addList(ListOfProduct);
        }
        if(snackbar.isShown()){
            snackbar.dismiss();
        }



        progressBar.setVisibility(View.GONE);
        // mSwipeRefreshLayout.setRefreshing(false);
    }

    public  boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId()==android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }


    private void createBoostDialog() {
        boostDialog = new Dialog(this, R.style.MaterialDialogSheet);
        boostDialog.setContentView(R.layout.boost_dialog); // your custom view.
        boostDialog.setCancelable(true);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

       Log.e("screen size",Integer.toString(width));


        Objects.requireNonNull(boostDialog.getWindow()).setLayout( Integer.parseInt(df2.format(width*0.8)), ViewGroup.LayoutParams.WRAP_CONTENT);

        boostDialog.getWindow().setGravity(Gravity.CENTER);

        prodImage = boostDialog.findViewById(R.id.prodimage);
        itemName = boostDialog.findViewById(R.id.itemNameText);
        periodDisplay=boostDialog.findViewById(R.id.dayLeftText);
        periodSpinner=boostDialog.findViewById(R.id.periodSpinner);
        priceText=boostDialog.findViewById(R.id.priceText);
        totalText=boostDialog.findViewById(R.id.boostPrice);
        checkOut=boostDialog.findViewById(R.id.checkout);
        addToCart=boostDialog.findViewById(R.id.addToCart);
        periodLabel=boostDialog.findViewById(R.id.periodText);
        dayUnit=boostDialog.findViewById(R.id.dayUnit);


        addToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToCart();
            }
        });

        checkOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToCart();
                Intent intentSetting=new Intent(BoostActivity.this,BoostCartActivity.class);
                intentSetting.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intentSetting);
                finish();
            }
        });

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

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {


        }

        @Override
        public void afterTextChanged(Editable s) {
            try{
                Double x=Double.parseDouble(priceText.getText().toString());
                if(x<.1){
                    priceText.setText("0.10");
                    x=0.1;
                }
                Double total=x*Double.parseDouble(periodSpinner.getSelectedItem().toString());
                totalText.setText("Total: RM"+df3.format(total));

            }catch (Exception e){
                priceText.setText("0.10");
                Double total=0.1*Double.parseDouble(periodSpinner.getSelectedItem().toString());
                totalText.setText("Total: RM"+df3.format(total));
            }

        }
    };


    private void addToCart(){
        //here
        class AsyncTaskUploadClass extends AsyncTask<Void,Void,String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                placeCartDialog.show();
            }

            @Override
            protected void onPostExecute(String response) {
                super.onPostExecute(response);
                Log.e("Response",response);
                doneplaceorder=true;
                boostDialog.dismiss();
                if(placeCartDialog.isShowing()){
                    placeCartDialog.dismiss();
                }

                //Event for crashlytic analysis
                // Answers.getInstance().logAddToCart(new AddToCartEvent());
//                Answers.getInstance().logAddToCart(new AddToCartEvent()
//                        .putItemId(getprodcode)
//                        .putItemName(getProdName)
//                        .putItemType(getProdCategory)
//                        .putItemPrice(new BigDecimal(getProdPrice))
//                        .putCustomAttribute("Quantity",selectedQty.getText().toString()));
//                Toast.makeText(ItemDetailActivity.this,R.string.itemAddedToCart,Toast.LENGTH_SHORT).show();
                // Setting image as transparent after done uploading.
                //imageView.setImageResource(android.R.color.transparent);
            }
            @Override
            protected String doInBackground(Void... params) {
                JSONObject JSONCart = new JSONObject();
                JSONObject EverythingJSON = new JSONObject();


                try{

                    JSONCart.put("rid",uid);
                    JSONCart.put("prodcode",ListOfProduct.get(RecyclerViewItemPosition).getProdCode());
                    JSONCart.put("price",priceText.getText());
                    JSONCart.put("period",periodSpinner.getSelectedItem().toString());

                }catch (Exception e){
                    e.printStackTrace();
                }


                UploadProcess ProcessClass = new UploadProcess();

                HashMap<String,JSONObject> HashMapParams = new HashMap<>();
                HashMapParams.put("retailerCart", JSONCart);
                String FinalData = ProcessClass.HttpRequestObject(AddCartToServerPath, HashMapParams);
                //CartDetails.clear();
                return FinalData;
            }
        }
        AsyncTaskUploadClass AsyncTaskUploadClassOBJ = new AsyncTaskUploadClass();
        AsyncTaskUploadClassOBJ.execute();
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
