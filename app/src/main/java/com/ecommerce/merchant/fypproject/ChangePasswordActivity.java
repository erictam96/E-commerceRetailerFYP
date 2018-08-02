package com.ecommerce.merchant.fypproject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ecommerce.merchant.fypproject.adapter.UploadProcess;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class ChangePasswordActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private EditText oldPassword,newPassword,confirmPassword;
    private String oldpassowrd,newpassword,email,uid;
    private RelativeLayout relativeLayout;
    private final String ChangePasswordURL ="http://ecommercefyp.000webhostapp.com/retailer/retailer_login.php";
    //private String ChangePasswordURL="http://10.0.2.2/cashierbookPHP/Eric/retailer_login.php";
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        GetFirebaseAuth();
        relativeLayout = findViewById(R.id
                .relativeLayout);
        Toolbar changePasswordToolbar = findViewById(R.id.changepasswordtoolbar);

        setSupportActionBar(changePasswordToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getResources().getString(R.string.chgPwd));
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        Button btnConfirm = findViewById(R.id.btn_confirmChangePassword);
        oldPassword = findViewById(R.id.edt_oldPassword);
        newPassword = findViewById(R.id.edt_newPassword);
        confirmPassword = findViewById(R.id.edt_confirmPassword);

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(oldPassword.getText().toString().isEmpty()){
                    oldPassword.setError(getResources().getString(R.string.emptyMsg));
                }
                if(newPassword.getText().toString().isEmpty()){
                    newPassword.setError(getResources().getString(R.string.emptyMsg));
                }
                if(confirmPassword.getText().toString().isEmpty()){
                    confirmPassword.setError(getResources().getString(R.string.emptyMsg));
                } else if (!newPassword.getText().toString().equals(confirmPassword.getText().toString())){
                    confirmPassword.setError(getResources().getString(R.string.confirmPwdErr));
                }else{
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    Objects.requireNonNull(imm).hideSoftInputFromWindow(relativeLayout.getWindowToken(), 0);
                    oldpassowrd = oldPassword.getText().toString();
                    newpassword = newPassword.getText().toString();
                    ChangePassword();
                }
            }
        });

    }
    private void ChangePassword(){
        class AsyncTaskUploadClass extends AsyncTask<Void, Void, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                // Showing progress dialog at image upload time.
                progressDialog = ProgressDialog.show(ChangePasswordActivity.this, getResources().getString(R.string.chgingPwd), getResources().getString(R.string.plsWait), false, false);
            }

            @Override
            protected String doInBackground(Void... voids) {


                AuthCredential credential = EmailAuthProvider.getCredential(email,oldpassowrd);

                Objects.requireNonNull(firebaseAuth.getCurrentUser()).reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            firebaseAuth.getCurrentUser().updatePassword(newpassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    //FirebaseAuth change password fail
                                    if(!task.isSuccessful()){
                                        progressDialog.dismiss();
                                        Snackbar snackbar_fail = Snackbar
                                                .make(relativeLayout, getResources().getString(R.string.smthgWrg), Snackbar.LENGTH_LONG);
                                        snackbar_fail.show();
                                    }else {
                                        //Update password to server also
                                        UpdateToServer();
                                        progressDialog.dismiss();
                                        Toast.makeText(ChangePasswordActivity.this,getResources().getString(R.string.chgPwdScc),Toast.LENGTH_LONG).show();
                                        finish();
                                    }
                                }
                            });
                        }else {
                            progressDialog.dismiss();
                            Snackbar snackbar_su = Snackbar
                                    .make(relativeLayout, getResources().getString(R.string.authFail), Snackbar.LENGTH_LONG);
                            snackbar_su.show();
                        }
                    }
                });
                return null;
            }

            @Override
            protected void onPostExecute(String string1) {
                super.onPostExecute(string1);
                // Dismiss the progress dialog after done uploading.
            }
        }
        AsyncTaskUploadClass AsyncTaskUploadClassOBJ = new AsyncTaskUploadClass();
        AsyncTaskUploadClassOBJ.execute();
    }

    private void UpdateToServer(){
        class AsyncTaskUploadClass extends AsyncTask<Void, Void, String> {

            @Override
            protected String doInBackground(Void... voids) {
                UploadProcess ProcessClass = new UploadProcess();

                HashMap<String,JSONObject> HashMapParams = new HashMap<>();
                JSONObject JSONFeedback = new JSONObject();

                try {
                    JSONFeedback.put("Uid",uid);
                    JSONFeedback.put("NewPassword",newpassword);
                } catch (JSONException e) {
                    Crashlytics.logException(e);
                    // handle your exception here!
                    e.printStackTrace();
                }

                Log.e("Json",JSONFeedback.toString());
                HashMapParams.put("changepassword", JSONFeedback);

                return ProcessClass.HttpRequestObject(ChangePasswordURL, HashMapParams);
            }

            @Override
            protected void onPostExecute(String string1) {
                super.onPostExecute(string1);
                Log.d("ResponseChange",string1);
            }
        }
        AsyncTaskUploadClass AsyncTaskUploadClassOBJ = new AsyncTaskUploadClass();
        AsyncTaskUploadClassOBJ.execute();
    }

    public  boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId()==android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    private void GetFirebaseAuth(){
        firebaseAuth= FirebaseAuth.getInstance();//get firebase object
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
        }
        else{
            uid = firebaseAuth.getCurrentUser().getUid();
            email = firebaseAuth.getCurrentUser().getEmail();
        }
    }
}
