package com.example.aadharapp;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import Module.QRGeoModel;
import Module.QRURLScanner;
import Module.QRVCardModel;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView scannerView;
    private TextView txtview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtview = findViewById(R.id.txtview);
        scannerView = findViewById(R.id.zxingscan);


        // Requesting Permission

        Dexter.withActivity(this)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(new PermissionListener() {
                @Override
                public void onPermissionGranted(PermissionGrantedResponse response) {
                    scannerView.setResultHandler(MainActivity.this);
                    scannerView.startCamera();
                }

                @Override
                public void onPermissionDenied(PermissionDeniedResponse response) {
                    Toast.makeText(MainActivity.this, "Give Permission", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                }
            }).check();

    }

    @Override
    protected void onDestroy() {
        scannerView.stopCamera();
        super.onDestroy();
    }

    @Override
    public void handleResult(Result rawResult) {

        processRawResult(rawResult.getText());

    }

    private void processRawResult(String text) {

        if(text.startsWith("BEGIN:")){
            String[] tokens = text.split("\n");
            QRVCardModel qrvCardModel = new QRVCardModel();
            for (int i=0;i<tokens.length;i++){
                if(tokens[i].startsWith("BEGIN:")){
                    qrvCardModel.setType(tokens[i].substring("BEGIN:".length()));   // Remove BEGIN: to get Type
                }
                else if(tokens[i].startsWith("N:")) {
                    qrvCardModel.setName(tokens[i].substring("N:".length()));
                }

                else if(tokens[i].startsWith("ORG:")) {
                    qrvCardModel.setOrg(tokens[i].substring("ORG:".length()));
                }

                else if(tokens[i].startsWith("TEL:")) {
                    qrvCardModel.setTel(tokens[i].substring("TEL:".length()));
                }

                else if(tokens[i].startsWith("URL:")) {
                    qrvCardModel.setUrl(tokens[i].substring("URL:".length()));
                }

                else if(tokens[i].startsWith("EMAIL:")){
                    qrvCardModel.setEmail(tokens[i].substring("EMAIL:".length()));
                }

                else if(tokens[i].startsWith("ADR")){
                    qrvCardModel.setAddress(tokens[i].substring("ADR:".length()));
                }

                else if(tokens[i].startsWith("NOTE:")){
                    qrvCardModel.setNote(tokens[i].substring("NOTE:".length()));
                }

                else if(tokens[i].startsWith("SUMMARY:")){
                    qrvCardModel.setSummary(tokens[i].substring("SUMMARY:".length()));
                }

                else if(tokens[i].startsWith("DTSTART:")){
                    qrvCardModel.setDtstart(tokens[i].substring("DTSTART:".length()));
                }

                else if(tokens[i].startsWith("DTEND:")){
                    qrvCardModel.setDtend(tokens[i].substring("DTEND:".length()));
                }

                // Try to show
                txtview.setText(qrvCardModel.getType());


            }

        }

        else if(text.startsWith("http://") ||
                text.startsWith("https://") ||
                text.startsWith("wwww.")){

            QRURLScanner qrurlScanner = new QRURLScanner(text);
            txtview.setText(qrurlScanner.getURL());

        }

        else if(text.startsWith("geo:")){
            QRGeoModel qrGeoModel = new QRGeoModel();
            String delims = "[ , ?q= ]+";
            String tokens[] = text.split(delims);

            for(int i=0;i<tokens.length;i++){

                if(tokens[i].startsWith(" geo:" )){
                    qrGeoModel.setLat(tokens[i].substring("geo:".length()));
                }
            }
            qrGeoModel.setLat(tokens[0].substring("geo:".length()));
            qrGeoModel.setLng(tokens[1]);
            qrGeoModel.setGeo_lace(tokens[2]);

            txtview.setText(qrGeoModel.getLat() + "/" + qrGeoModel.getLng());

        }

        else{
            txtview.setText(text);
        }

        scannerView.resumeCameraPreview(MainActivity.this);

    }
}
