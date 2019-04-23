package com.example.rapidfood.Activites;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rapidfood.Models.SubscriptionModel;
import com.example.rapidfood.Models.SubscriptionTransactionModel;
import com.example.rapidfood.R;
import com.example.rapidfood.Utils.FirebaseInstances;
import com.example.rapidfood.Utils.GenerateUUIDClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class GooglePayActivity extends AppCompatActivity {
    private static final int TEZ_REQUEST_CODE = 123;
    ConstraintLayout mPAymentSuccess, mPaymentFailure;
    ConstraintLayout mOrder;
    private String tr;
    TextView msubcost, msubval, mdetails, msubname, msubcoupon_Value, mTotalCost;
    FirebaseInstances mFirebaseInstances;
    FirebaseAuth mFirebaseAuth;
    FirebaseFirestore mFirebaseFirestore;
    String sub_name;
    private static final String GOOGLE_TEZ_PACKAGE_NAME = "com.google.android.apps.nbu.paisa.user";

    private static final String TAG = "GooglePayActivity";
    private PreferenceManager mPreferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_pay_subscription_layout);
        mFirebaseInstances = new FirebaseInstances();
        mFirebaseAuth = mFirebaseInstances.getFirebaseAuth();
        mFirebaseFirestore = mFirebaseInstances.getFirebaseFirestore();
        mPreferenceManager = new PreferenceManager(this);

        sub_name = getIntent().getStringExtra("sub_name");

        mOrder = findViewById(R.id.main_layout_holder);
        mPAymentSuccess = findViewById(R.id.payment_success);
        mPaymentFailure = findViewById(R.id.payment_failure);
        RelativeLayout payButton = findViewById(R.id.googlepay_button);
        msubcost = findViewById(R.id.subscription_cost);
        msubval = findViewById(R.id.subscription_validity);
        mdetails = findViewById(R.id.subscription_detail);
        msubcoupon_Value = findViewById(R.id.subscription_coupon_offer);
        msubname = findViewById(R.id.subscription_type);
        mTotalCost = findViewById(R.id.subscription_total_cost);


        getSubDetails(sub_name);
        GenerateUUIDClass vGenerateUUIDClass = new GenerateUUIDClass();
        tr = vGenerateUUIDClass.generateUniqueKeyUsingUUID();
        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFirebaseAuth.getCurrentUser() != null) {
                    mFirebaseFirestore.collection("subscribed_user")
                            .document(mFirebaseAuth.getCurrentUser().getUid())
                            .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> pTask) {
                            if (!pTask.isSuccessful()) {
                                Snackbar.make(mOrder, "ALREADY SUBSCRIBED USER!", Snackbar.LENGTH_LONG).show();
                            } else {
                                MakePaymentGooglePay();
                            }
                        }
                    });

                }
            }
        });
    }

    private void MakePaymentGooglePay() {
        String amountt = mTotalCost.getText().toString();
        if (amountt != null) {
            StringBuilder vStringBuilder = new StringBuilder();
            vStringBuilder.append(amountt)
                    .append(".00");
            String am = vStringBuilder.toString();
            Uri uri = new Uri.Builder()
                    .scheme("upi")
                    .authority("pay")
                    .appendQueryParameter("pa", "par78vesh.kr@oksbi")
                    .appendQueryParameter("pn", "sunil kumar")
                    .appendQueryParameter("mc", "1234")
                    .appendQueryParameter("tr", tr)
                    .appendQueryParameter("tn", "rapidfood subscription")
                    .appendQueryParameter("am", "1")
                    .appendQueryParameter("cu", "INR")
                    .appendQueryParameter("url", "https://test.merchant.website")
                    .build();
            PackageManager packageManager = getApplication().getPackageManager();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            intent.setPackage(GOOGLE_TEZ_PACKAGE_NAME);
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, TEZ_REQUEST_CODE);
            } else {
                AlertDialog.Builder vBuilder = new AlertDialog.Builder(GooglePayActivity.this)
                        .setCancelable(true)
                        .setIcon(R.drawable.ic_google_pay_mark_800_gray)
                        .setMessage("Google Pay not installed!!")
                        .setTitle("Google Pay not found!");
                vBuilder.create().show();
            }


        }
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mPreferenceManager.setTransIdTemp(tr);
        // Toast.makeText(this, "Stop Called", Toast.LENGTH_SHORT).show();
    }

    void getSubDetails(String pSub_name) {
        mFirebaseFirestore.collection("subscriptions").document(pSub_name).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> pTask) {
                        if (pTask.isSuccessful()) {
                            DocumentSnapshot vSnapshot = pTask.getResult();
                            SubscriptionModel vModel = vSnapshot.toObject(SubscriptionModel.class);
                            if (vModel != null) {
                                msubcost.setText(String.format("%s", vModel.getPrice()));
                                mdetails.setText(String.format("**%s", vModel.getDetails()));
                                msubval.setText(vModel.getDuration());
                                msubname.setText(vModel.getType());
                                Picasso.get().load(vModel.getImagesub())
                                        .fit()
                                        .into((ImageView) findViewById(R.id.sub_image));
                                msubcoupon_Value.setText(String.format("%s", vModel.getCoupon()));
                                long total_bill = (Long.parseLong(vModel.getPrice()) - Long.parseLong(vModel.getCoupon()));
                                // Toast.makeText(GooglePayActivity.this, "" + Long.toString(total_bill), Toast.LENGTH_SHORT).show();
                                mTotalCost.setText(String.format("%s", Long.toString(total_bill)));
                            }
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == TEZ_REQUEST_CODE) {
            String s_name = msubname.getText().toString();
            String s_cost = msubcost.getText().toString();
            String s_duration = msubval.getText().toString();
            String s_coupon = msubcoupon_Value.getText().toString();
            String s_total = mTotalCost.getText().toString();
            String uid = null;
            String mob = null;
            if (mFirebaseAuth.getCurrentUser() != null) {
                mob = mFirebaseAuth.getCurrentUser().getPhoneNumber();
                uid = mFirebaseAuth.getCurrentUser().getUid();
            }
            tr = mPreferenceManager.getT_ID();
            SubscriptionTransactionModel vModel=new SubscriptionTransactionModel();
            vModel.setSubname(s_name);
            vModel.setDuration(s_duration);
            vModel.setSubcost(s_cost);
            vModel.setSubcoupon(s_coupon);
            vModel.setUid(uid);
            vModel.setMobile(mob);
            vModel.setTotal_paid(s_total);
            vModel.setTransaction_id(tr);
            vModel.setPayment_status(data.getStringExtra("Status"));
            vModel.setVerified("pending");

            if (data.getStringExtra("Status").equals("SUCCESS")) {
                mOrder.setVisibility(View.GONE);
                mPAymentSuccess.setVisibility(View.VISIBLE);

            } else {
                mOrder.setVisibility(View.GONE);
                mPaymentFailure.setVisibility(View.VISIBLE);
            }
            mFirebaseFirestore.collection("sub_transaction_data").document(tr).set(vModel);
        }
    }

    public void goBackHome(View view) {
        startActivity(new Intent(GooglePayActivity.this, MainActivity.class));
        finish();
    }
}

