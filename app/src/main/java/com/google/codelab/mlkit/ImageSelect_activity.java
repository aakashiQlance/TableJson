package com.google.codelab.mlkit;

import static android.view.View.GONE;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.mayank.simplecropview.CropImageView;
import com.mayank.simplecropview.callback.CropCallback;
import com.mayank.simplecropview.callback.LoadCallback;
import com.mayank.simplecropview.callback.SaveCallback;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageSelect_activity extends AppCompatActivity {

     TextView sleeectimahe;
     TextView tvOkay;
    CropImageView mCropView;
    ImageView selectImageCrop;
    private Bitmap.CompressFormat mCompressFormat = Bitmap.CompressFormat.JPEG;

    private RectF mFrameRect = null;
    private Uri mSourceUri = null;
    private static final String KEY_FRAME_RECT = "FrameRect";
    private static final String KEY_SOURCE_URI = "SourceUri";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_select);

        if (savedInstanceState != null) {
            // restore data
            mFrameRect = savedInstanceState.getParcelable(KEY_FRAME_RECT);
            mSourceUri = savedInstanceState.getParcelable(KEY_SOURCE_URI);
        }


        sleeectimahe= findViewById(R.id.tvText);
        tvOkay= findViewById(R.id.tvOkay);
        mCropView= findViewById(R.id.cropImageView);
        selectImageCrop= findViewById(R.id.selectImageCrop);

        sleeectimahe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImageCrop.setVisibility(GONE);
                mCropView.setVisibility(View.VISIBLE);
                tvOkay.setVisibility(GONE);

                ImagePicker.with(ImageSelect_activity.this)
                        .compress(1024)
                        .maxResultSize(1080,
                                1080)
                        .cameraOnly()
                        .start(101);
            }
        });

        tvOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCropView.crop(mSourceUri).execute(mCropCallback);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK) {
            if (data != null) {
               Uri imageUri = data.getData();
                mCropView.load(imageUri)
                        .initialFrameRect(mFrameRect)
                        .useThumbnail(true)
                        .execute(mLoadCallback);


              /*  mCropView.crop(imageUri)
                        .execute(new CropCallback() {
                            @Override public void onSuccess(Bitmap cropped) {

                                selectImageCrop.setVisibility(View.VISIBLE);
                                mCropView.setVisibility(GONE);

                            }

                            @Override public void onError(Throwable e) {
                            }
                        });*/
            }
        }
    }
    private final LoadCallback mLoadCallback = new LoadCallback() {
        @Override public void onSuccess() {
//            selectImageCrop.setVisibility(View.VISIBLE);
//            mCropView.setVisibility(GONE);
            mCropView.rotateImage(CropImageView.RotateDegrees.ROTATE_90D);

            tvOkay.setVisibility(View.VISIBLE);

          //  mCropView.crop(mSourceUri).execute(mCropCallback);

        }

        @Override public void onError(Throwable e) {
        }
    };

    private final CropCallback mCropCallback = new CropCallback() {
        @Override public void onSuccess(Bitmap cropped) {
            mCropView.save(cropped)
                    .compressFormat(mCompressFormat)
                    .execute(createSaveUri(), mSaveCallback);
        }

        @Override public void onError(Throwable e) {
        }
    };

    private final SaveCallback mSaveCallback = new SaveCallback() {
        @Override public void onSuccess(Uri outputUri) {
            //startResultActivity(outputUri);
            selectImageCrop.setVisibility(View.VISIBLE);
            mCropView.setVisibility(GONE);
            tvOkay.setVisibility(GONE);

            selectImageCrop.setImageURI(outputUri);

            startActivity(new Intent(ImageSelect_activity.this,MainActivity.class).putExtra("ImageURI",outputUri.toString()));

        }

        @Override public void onError(Throwable e) {

        }
    };

    public Uri createSaveUri() {
        return createNewUri(ImageSelect_activity.this, mCompressFormat);
    }

    public static Uri createNewUri(Context context, Bitmap.CompressFormat format) {
        long currentTimeMillis = System.currentTimeMillis();
        Date today = new Date(currentTimeMillis);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String title = dateFormat.format(today);
        String dirPath = getDirPath();
        String fileName = "scv" + title + "." + getMimeType(format);
        String path = dirPath + "/" + fileName;
        File file = new File(path);
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, title);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/" + getMimeType(format));
        values.put(MediaStore.Images.Media.DATA, path);
        long time = currentTimeMillis / 1000;
        values.put(MediaStore.MediaColumns.DATE_ADDED, time);
        values.put(MediaStore.MediaColumns.DATE_MODIFIED, time);
        if (file.exists()) {
            values.put(MediaStore.Images.Media.SIZE, file.length());
        }

        ContentResolver resolver = context.getContentResolver();
        Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        return uri;
    }
    public static String getMimeType(Bitmap.CompressFormat format) {
        switch (format) {
            case JPEG:
                return "jpeg";
            case PNG:
                return "png";
        }
        return "png";
    }

    public static String getDirPath() {
        String dirPath = "";
        File imageDir = null;
        File extStorageDir = Environment.getExternalStorageDirectory();
        if (extStorageDir.canWrite()) {
            imageDir = new File(extStorageDir.getPath() + "/simplecropview");
        }
        if (imageDir != null) {
            if (!imageDir.exists()) {
                imageDir.mkdirs();
            }
            if (imageDir.canWrite()) {
                dirPath = imageDir.getPath();
            }
        }
        return dirPath;
    }
}