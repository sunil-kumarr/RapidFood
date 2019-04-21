package com.example.rapidfood.Activites;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.rapidfood.Models.UserProfileModel;
import com.example.rapidfood.R;
import com.example.rapidfood.Utils.FirebaseInstances;
import com.example.rapidfood.Utils.ImageUtil;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;


public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore mFirebaseFirestore;
    private FirebaseStorage mFirebaseStorage;
    private FirebaseInstances mFirebaseInstances;
    private FirebaseUser mFirebaseUser;
    private String image;
    private ConstraintLayout mMainContainer;
    private Uri imageUri;
    private Toolbar mToolbar;
    private ImageView mUploadImage;
    private TextView mImageUrl;
    private EditText mUserNameEDT, mUserEmailEDt, mUserMobileEDt;
    private ImageView mUserImage;
    private Button mUpadteProfileBtn;
    private UserProfileModel mUserModel;
    private boolean mImageSelected = false;
    private Map<String, Object> getUser;
    private static final String TAG = "ProfileActivity";
    private ImageUtil mImageUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mToolbar = findViewById(R.id.toolbar_profile);
        setSupportActionBar(mToolbar);
        ActionBar vActionBar = getSupportActionBar();
        if (vActionBar != null) {
            vActionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_black_24dp);
            vActionBar.setDisplayHomeAsUpEnabled(true);
            vActionBar.setDisplayShowHomeEnabled(true);
        }

        mUserEmailEDt = findViewById(R.id.edtEmailUser);
        mUserNameEDT = findViewById(R.id.edtNameUser);
        mUpadteProfileBtn = findViewById(R.id.btn_save_profile);
        mUserImage = findViewById(R.id.profile_image_user);
        mUserMobileEDt = findViewById(R.id.edtMobileUser);
        mMainContainer = findViewById(R.id.main_layout_holder);
        mUploadImage = findViewById(R.id.upload_image);
        mImageUrl = findViewById(R.id.image_url_id);

        mFirebaseInstances = new FirebaseInstances();
        mFirebaseStorage = mFirebaseInstances.getFirebaseStorage();
        mFirebaseFirestore = mFirebaseInstances.getFirebaseFirestore();
        mFirebaseAuth = mFirebaseInstances.getFirebaseAuth();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        mImageUtil = new ImageUtil();

        mUpadteProfileBtn.setOnClickListener(this);

        mUserImage.setOnClickListener(this);

        mUploadImage.setOnClickListener(this);
        mUserModel = new UserProfileModel();

    }

    private boolean EmptyString(View v) {
        EditText localEditText = (EditText) v;
        if (localEditText.getText().toString().equals("") || localEditText.getText().toString().equals("null")) {
            localEditText.setError("Field cannot be Empty");
            return false;
        }
        return true;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Result code is RESULT_OK only if the user selects an Image
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK)
            switch (requestCode) {
                case 2973:
                    //data.getData returns the content URI for the selected Image
                    imageUri = data.getData();
                    if (imageUri != null)
                        image = mImageUtil.FilePathNameExtractor(imageUri);
                    if (image != null) {
                        mUserImage.setVisibility(View.VISIBLE);
                        mUploadImage.setVisibility(View.GONE);
                        Picasso.get()
                                .load(imageUri)
                                .into(mUserImage);
                        mImageSelected = true;
                    }

                    break;
            }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser != null && !mImageSelected) {
            mFirebaseFirestore.collection("users").document(mFirebaseUser.getUid())
                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot pDocumentSnapshot) {
                    if (pDocumentSnapshot.exists()) {
                        UserProfileModel mUserModel;
                        mUserModel = pDocumentSnapshot.get("user_profile_data", UserProfileModel.class);
                        if (mUserModel != null) {
                            mUserImage.setVisibility(View.VISIBLE);
                            Picasso.get()
                                    .load(mUserModel.getProfileimage())
                                    .into(mUserImage);
                            mImageUrl.setText(mUserModel.getProfileimage());
                            mUploadImage.setVisibility(View.GONE);
                            mUserNameEDT.setText(mUserModel.getUsername());
                            mUserEmailEDt.setText(mUserModel.getEmailAddress());
                        }
                    }
                }
            });
            mUserMobileEDt.setText(mFirebaseUser.getPhoneNumber());
            mUserMobileEDt.setEnabled(false);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.upload_image:
                AlertDialog vBuilder = new AlertDialog.Builder(this)
                        .setCancelable(true)
                        .setIcon(R.drawable.ic_photo_library_blue_24dp)
                        .setMessage("SELECT IMAGE FROM YOUR DEVICE.")
                        .setTitle("UPLOAD IMAGE")
                        .setPositiveButton("OPEN GALLERY", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mImageUtil.pickFromGallery(ProfileActivity.this);
                            }
                        }).create();
                vBuilder.show();
                break;
            case R.id.btn_save_profile:
                AlertDialog vUpdateDialog = new AlertDialog.Builder(this)
                        .setCancelable(true)
                        .setIcon(R.drawable.profile)
                        .setMessage("ARE YOU SURE YOU WANT TO UPDATE YOUR PROFILE?")
                        .setTitle("UPDATE PROFILE")
                        .setPositiveButton("Yes,Sure.", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                updateToFireStore();
                            }
                        }).create();
                vUpdateDialog.show();

                break;
        }
    }

    private ProgressDialog mProgressDialog;

    private void updateToFireStore() {

        String name = mUserNameEDT.getText().toString();
        String email = "No email address";
        email = mUserEmailEDt.getText().toString();
        if (EmptyString(mUserNameEDT)) {

            mProgressDialog = new ProgressDialog(this);
            mUserModel.setUsername(name);
            mUserModel.setEmailAddress(email);

            mUserModel.setMobile(mFirebaseUser.getPhoneNumber());
            if (mImageSelected) {
                mProgressDialog.setMax(100);
                mProgressDialog.setTitle("Uploading....");
                mProgressDialog.setCancelable(false);
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.show();
                UploadImageToFirebase(imageUri);
            }
            addItemToFireStore();
        }
    }

    private void addItemToFireStore() {
        // Toast.makeText(this, "IMAGE" + mUserModel.getProfileimage(), Toast.LENGTH_SHORT).show();
        if (mUserModel.getProfileimage() == null) {
            mUserModel.setProfileimage(mImageUrl.getText().toString());
        }
        DocumentReference user = mFirebaseFirestore.collection("users").document(mFirebaseUser.getUid());
        user.update("user_profile_data", mUserModel).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void pVoid) {
                mProgressDialog.dismiss();
                Snackbar.make(mMainContainer, "Profile updated", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void UploadImageToFirebase(Uri pImageUri) {
        //  Toast.makeText(this, "Upload called", Toast.LENGTH_SHORT).show();
        final StorageReference ref = mFirebaseStorage.getReference().child("user_profile/" + image);
        ref.putFile(pImageUri).
                addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot pTaskSnapshot) {

                        double progress = (100.0 * pTaskSnapshot.getBytesTransferred()) / pTaskSnapshot.getTotalByteCount();

                        String s = new DecimalFormat("##").format(progress);
                        mProgressDialog.setProgressNumberFormat(s);
                        mProgressDialog.setProgress((int) progress);
                    }
                })
                .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        // Forward any exceptions
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        return ref.getDownloadUrl();
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(@NonNull Uri downloadUri) {
                        mUserModel.setProfileimage(downloadUri.toString());
                        addItemToFireStore();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {

                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            this.finish();
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }
}
