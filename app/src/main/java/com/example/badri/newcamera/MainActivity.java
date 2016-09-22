package com.example.badri.newcamera;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.media.ExifInterface;
import android.media.FaceDetector;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.Manifest;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    Button capture;
    ImageView imageView;
    File myPic,myPic1,myPic2,myPic4,myPic6,myPic8;
    boolean taken=true;
    Uri uri;
    String root;
    static final String photo_taken="photo_taken";
    int PERMISSIONS_REQUEST_ALL=100;
    File[] file;
    File dir,dir2;
    MediaScannerConnection.MediaScannerConnectionClient mediaScannerConnectionClient;
    MediaScannerConnection mediaScannerConnection;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("oncreate");
        setContentView(R.layout.activity_main);
        capture=(Button)findViewById(R.id.capture);
        imageView=(ImageView)findViewById(R.id.imageView);
        root=Environment.getExternalStorageDirectory().toString();
        connectMediaScanner();

        dir=new File(root+"/NewCamera");
        dir2=new File(root+"/payslip");
        dir.mkdir();
        dir2.mkdir();
        System.out.println("is dir "+dir.isDirectory());
        System.out.println("dir 2 "+dir2.isDirectory());


        myPic=new File(dir,"newPic.jpg");
        myPic1=new File(dir2,"PAN_Q80.jpg");
        myPic2=new File(dir2,"PAN.jpg");
        myPic4=new File(dir2,"newPic4.jpg");
        myPic6=new File(dir2,"newPic6.jpg");
        myPic8=new File(dir2,"newPic8.jpg");
        file= new File[]{myPic1, myPic2, myPic4, myPic6, myPic8};


        capture.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {

                String[] mypermission;

                mypermission=permissionsToRequest();
                if(mypermission.length>0)
                {
                    requestPermissions(mypermission,PERMISSIONS_REQUEST_ALL);
                }
                else {
                    uri = Uri.fromFile(myPic);
                    System.out.println("uri " + uri.toString());
                    Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    i.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    MainActivity.this.startActivityForResult(i, 1);
                }

            }
        });


    }
    public void connectMediaScanner()
    {
        mediaScannerConnectionClient= new MediaScannerConnection.MediaScannerConnectionClient() {
            @Override
            public void onMediaScannerConnected() {
                Toast.makeText(MainActivity.this,"Media scanner connected",Toast.LENGTH_LONG).show();

            }

            @Override
            public void onScanCompleted(String s, Uri uri) {

                Toast.makeText(MainActivity.this,"Path scanned "+s,Toast.LENGTH_LONG).show();

            }
        };
        mediaScannerConnection=new MediaScannerConnection(MainActivity.this,mediaScannerConnectionClient);
        mediaScannerConnection.connect();
    }
    public void scanFile(String path)
    {
        mediaScannerConnection.scanFile(path,null);
    }


    public String[] permissionsToRequest()
    {
        List<String> myReq=new ArrayList<String>();
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
        {
            if(checkSelfPermission(Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED)
            {
                myReq.add(Manifest.permission.CAMERA);
            }
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED)
            {
                myReq.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            }
        }


        return myReq.toArray(new String[myReq.size()]);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==PERMISSIONS_REQUEST_ALL)
        {
            uri = Uri.fromFile(myPic);
            System.out.println("uri " + uri.toString());

            Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            i.putExtra(MediaStore.EXTRA_OUTPUT, uri);

            MainActivity.this.startActivityForResult(i, 1);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("on activity result");
        scanFile(root+"/NewCamera/newPic.jpg");
        new SaveImage().execute(root+"/NewCamera/newPic.jpg");






    }
    public class SaveImage extends AsyncTask<String,Integer,Bitmap>{

        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pd=new ProgressDialog(MainActivity.this,ProgressDialog.STYLE_HORIZONTAL);
            pd.setTitle("Starting to Upload Image");
            pd.setCancelable(false);
            pd.incrementProgressBy(1);
            pd.setMax(100);
            pd.show();

        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap bm;
            Log.v("file",strings[0]);
            bm=BitmapFactory.decodeFile(strings[0]);
            float points[]={0.3f,0.5f,0.8f};
            Bitmap bm1;
            for(int i=0;i<3;i++)
            {
                bm1=Bitmap.createScaledBitmap(bm,(int)(bm.getWidth()*points[i]),(int)(bm.getHeight()*points[i]),true);

                String fileName="PAYSLIP_Q40_W"+((int)(bm.getWidth()*points[i]))+"_H"+((int)(bm.getHeight()*points[i]))+".jpg";
                try
                {

                    FileOutputStream fileOutputStream=new FileOutputStream(new File(dir2,fileName));
                    bm1.compress(Bitmap.CompressFormat.JPEG,40,fileOutputStream);
                    fileOutputStream.close();
                }

                catch (FileNotFoundException e)
                {

                } catch (IOException e) {
                    e.printStackTrace();
                }
                scanFile(root+"/payslip/"+fileName);
            }



            return bm;
        }



        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            Bitmap bitmap1=ThumbnailUtils.extractThumbnail(bitmap,300,400);
            imageView.setImageBitmap(bitmap1);
            pd.dismiss();
            Toast.makeText(MainActivity.this,"Success "+bitmap.getByteCount(),Toast.LENGTH_LONG).show();
        }
    }







    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("on destroy");
        mediaScannerConnection.disconnect();
    }

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("on start");
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("on resume");
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("on stop");

    }
}

