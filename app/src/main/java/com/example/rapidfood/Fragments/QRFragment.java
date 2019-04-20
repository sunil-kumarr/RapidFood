package com.example.rapidfood.Fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.rapidfood.Adapters.QRAdapter;
import com.example.rapidfood.Models.PackageModel;
import com.example.rapidfood.Models.QRorderModel;
import com.example.rapidfood.R;
import com.example.rapidfood.Utils.EncryptionHelper;
import com.example.rapidfood.Utils.FirebaseInstances;
import com.example.rapidfood.Utils.GenerateUUIDClass;
import com.example.rapidfood.Utils.QRCodeHelper;
import com.example.rapidfood.VendorActivities.ShowQRDataActivity;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class QRFragment extends Fragment implements QRAdapter.qrListener {
    private RecyclerView mQRRecyceler;
    private FirebaseFirestore mFirebaseFirestore;
    private FirebaseInstances mFirebaseInstances;
    private FirebaseAuth mFirebaseAuth;
    private FirestoreRecyclerOptions<PackageModel> mOptions;
    private GenerateUUIDClass mGenerateUUIDClass;
    private FirestoreRecyclerAdapter mQRAdapter;
    private Context mContext;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mQRRecyceler = view.findViewById(R.id.qr_recycler_view);
        FirebaseInstances vInstances = new FirebaseInstances();
        mFirebaseFirestore = vInstances.getFirebaseFirestore();
        mFirebaseAuth=vInstances.getFirebaseAuth();
        mGenerateUUIDClass=new GenerateUUIDClass();
        LinearLayoutManager llm = new LinearLayoutManager(mContext, RecyclerView.VERTICAL, false);
        mQRRecyceler.setLayoutManager(llm);
        mQRRecyceler.setItemAnimator(new DefaultItemAnimator());
        Query qrQuery = mFirebaseFirestore
                .collection("packages");

        mOptions = new FirestoreRecyclerOptions.Builder<PackageModel>()
                .setQuery(qrQuery, PackageModel.class).build();

        mQRAdapter = new QRAdapter(mOptions, mQRRecyceler, this,mContext);
        mQRRecyceler.post(new Runnable() {
            @Override
            public void run() {
                mQRRecyceler.setAdapter(mQRAdapter);
                mQRRecyceler.setItemAnimator(new DefaultItemAnimator());
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_qrcode, container, false);
    }

    @Override
    public void onStop() {
        super.onStop();
        mQRAdapter.stopListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        mQRAdapter.startListening();
    }

    @Override
    public void onCLickQRGen(View v, QRorderModel pQRorderModel) {
        if(mFirebaseAuth.getCurrentUser()!=null) {

            String mobile=mFirebaseAuth.getCurrentUser().getPhoneNumber();
            String uid=mFirebaseAuth.getCurrentUser().getUid();
            String qr_id=mGenerateUUIDClass.generateUniqueKeyUsingUUID();
            pQRorderModel.setUser_mobile(mobile);
            pQRorderModel.setUser_UID(uid);
            pQRorderModel.setQr_id(qr_id);
            pQRorderModel.setOrder_Status("pending");
            pQRorderModel.setPayment_status("pending");
            pQRorderModel.setUsed_qr(false);
            mFirebaseFirestore.collection("generated_qr_code").document(pQRorderModel.getQr_id()).set(pQRorderModel)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> pTask) {
                            if(pTask.isSuccessful()){

                                String qrData=null;
                                try {
                                    Gson vGson=new Gson();
                                    qrData= vGson.toJson(pQRorderModel);
                                    // Toast.makeText(mContext, ""+qrData.toString(), Toast.LENGTH_SHORT).show();
                                } catch (Exception pE) {
                                    pE.printStackTrace();
                                }
                                if(qrData!=null){
                                    String encryptedString= EncryptionHelper.getInstance().encryptionString(qrData).encryptMsg();
                                    setImageBitmap(encryptedString);
                                }
                            }
                        }
                    });


        }
    }


    private void setImageBitmap(String pEncryptedString){
      //  Toast.makeText(mContext, ""+pEncryptedString, Toast.LENGTH_SHORT).show();
       Bitmap vBitmap= QRCodeHelper.newInstance(mContext)
                .setContent(pEncryptedString)
                .setErrorCorrectionLevel(ErrorCorrectionLevel.Q)
                .setMargin(2)
                .getQRCOde();
       if(vBitmap!=null){
           showBottomSheetDialog(vBitmap);
       }

    }
    private void showBottomSheetDialog(Bitmap pBitmap) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_qr_bottom_sheet, null);
        ImageView qrImageView=view.findViewById(R.id.show_qr_code);
        qrImageView.setImageBitmap(pBitmap);
        BottomSheetDialog dialog = new BottomSheetDialog(mContext);
        dialog.setContentView(view);
        dialog.show();
    }
}
