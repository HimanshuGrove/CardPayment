package com.grove.cardpayment.activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;
import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.GenericTypeIndicator;
import com.firebase.client.ValueEventListener;
import com.grove.cardpayment.Adapters.CardAdapter;
import com.grove.cardpayment.Models.CardDetails;
import com.grove.cardpayment.R;
import com.grove.cardpayment.Utility.Constants;
import com.grove.cardpayment.Utility.Utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.Key;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import static android.os.Build.VERSION.SDK_INT;
import static com.grove.cardpayment.Utility.Constants.cardHolderName;
import static com.grove.cardpayment.Utility.Constants.cardNo;
import static com.grove.cardpayment.Utility.Constants.expDate;

public class HomeActivity extends AppCompatActivity  implements View.OnClickListener {

    CardAdapter cardAdapter;
    RecyclerView recyCards;
    Button btnAddCard;
    View viewBottom;
    public static String[] permissionsRequired = new String[]{Manifest.permission.CAMERA};
    Firebase firebase;
    RelativeLayout rlLoader,rlEmptyList;
    List<CardDetails> cardDetails=new ArrayList<>();
    String deviceId ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);

        if (SDK_INT > 22) {
            setTheme(R.style.AppTheme);
        } else {
            setTheme(R.style.AppThemeBlack);
        }
        setContentView(R.layout.activity_home);

        recyCards=findViewById(R.id.recyCards);
        btnAddCard=findViewById(R.id.btnAddCard);
        viewBottom=findViewById(R.id.viewBottom);
        rlLoader=findViewById(R.id.rlLoader);
        rlEmptyList=findViewById(R.id.rlEmptyList);
        btnAddCard.setOnClickListener(this);

        initView();
    }

    void initView(){
        cardAdapter=new CardAdapter(this);
        recyCards.setAdapter(cardAdapter);
        deviceId = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        firebase=new Firebase(Constants.CARD_URL+deviceId);
        rlLoader.setVisibility(View.VISIBLE);
        firebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                rlLoader.setVisibility(View.GONE);
                int i = 1;
                cardDetails=new ArrayList<>();
                for (DataSnapshot dataSnapshot1 : dataSnapshot.child("card").getChildren()) {
                    CardDetails card = dataSnapshot1.getValue(CardDetails.class);
                    cardDetails.add(card);
                    i++;
                }
                if(i==1){
                    rlEmptyList.setVisibility(View.VISIBLE);
                }
                cardAdapter.notifyList(cardDetails);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                rlLoader.setVisibility(View.GONE);
            }
        });
    }

    private void popupDisplay() {
        final PopupWindow popupWindow = new PopupWindow(this);
        View view;

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.dialog_scan_card, null);
        final Button btnScanCard=view.findViewById(R.id.btnScanCard);
        Button btnAddManullay=view.findViewById(R.id.btnAddManullay);
        btnScanCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ActivityCompat.checkSelfPermission(HomeActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(permissionsRequired, 102);
                        }else {
                            onScan();
                        }
                    }else {
                        onScan();
                    }
            }
        });
        btnAddManullay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnScanCard.performClick();
            }
        });
        popupWindow.setFocusable(true);
        popupWindow.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(Utils.dpToPx(240));
        popupWindow.setAnimationStyle(R.style.AnimationPopup);

        popupWindow.setContentView(view);
        popupWindow.setBackgroundDrawable(new ColorDrawable(
                android.graphics.Color.TRANSPARENT));

        popupWindow.showAsDropDown(viewBottom, 0,-Utils.dpToPx(240));
        Utils.dimBehind(popupWindow);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnAddCard:
                popupDisplay();
                break;
                default:
                    break;
        }
    }

    public void uploadCard(String cardNumber, String cardholderName, String s){
        Firebase reference = new Firebase(Constants.CARD_URL+deviceId);
        try {
            Map<String, String> map = new HashMap<String, String>();
            map.put(cardNo,Utils.encrypt(cardNumber));
            map.put(expDate,Utils.encrypt(s));
            map.put(cardHolderName,Utils.encrypt(cardholderName));
            reference.child("card").push().setValue(map);
            CardDetails card=new CardDetails();
            card.setCardHolderName(map.get(cardHolderName));
            card.setCardNo(map.get(cardNo));
            card.setExpiryDate(map.get(expDate));
            cardDetails.add(card);
            cardAdapter.notifyList(cardDetails);
            rlEmptyList.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onScan(){
        Intent intent = new Intent(this, CardIOActivity.class)
                .putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true)
                .putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, false)
                .putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, false)
                .putExtra(CardIOActivity.EXTRA_USE_CARDIO_LOGO,false)
                .putExtra(CardIOActivity.EXTRA_REQUIRE_CARDHOLDER_NAME, true);

        startActivityForResult(intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == 101 ) && data != null
                && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
            CreditCard result = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);

            if(Utils.isNetworkAvailable(HomeActivity.this)){
                uploadCard(result.cardNumber,result.cardholderName,result.expiryMonth+"/"+result.expiryYear);
            }else {
                Utils.showToastShort(HomeActivity.this,getString(R.string.check_internet_connection));
            }

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 102 ) {
            if(grantResults!=null && grantResults.length==1){
                onScan();
            }

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    void chkpermission() {

        ActivityCompat.requestPermissions(this, permissionsRequired, 102);
    }

}
