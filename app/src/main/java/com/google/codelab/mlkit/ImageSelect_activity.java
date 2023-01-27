package com.google.codelab.mlkit;

import static android.view.View.GONE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.mayank.simplecropview.CropImageView;
import com.mayank.simplecropview.callback.CropCallback;
import com.mayank.simplecropview.callback.LoadCallback;
import com.mayank.simplecropview.callback.SaveCallback;


import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ImageSelect_activity extends AppCompatActivity {
     Button mBtnScanImage;
     Button mBtnGenerateTable;
    private CropImageView mCropView;
    ProgressDialog dialog;
    private static final String KEY_FRAME_RECT = "FrameRect";
    private static final String KEY_SOURCE_URI = "SourceUri";

    private final LoadCallback mLoadCallback = new LoadCallback() {
        @Override
        public void onSuccess() {
//            selectImageCrop.setVisibility(View.VISIBLE);
//            mCropView.setVisibility(GONE);

            mBtnGenerateTable.setVisibility(View.VISIBLE);

            //  mCropView.crop(mSourceUri).execute(mCropCallback);

        }

        @Override
        public void onError(Throwable e) {
        }
    };

    private final SaveCallback mSaveCallback = new SaveCallback() {
        @Override
        public void onSuccess(Uri outputUri) {
            //startResultActivity(outputUri);
            mCropView.setVisibility(GONE);
            mBtnGenerateTable.setVisibility(GONE);

            try {
                runTextRecognition(outputUri);
            } catch (IOException e) {
                e.printStackTrace();
            }


//            startActivity(new Intent(ImageSelect_activity.this, MainActivity.class).putExtra("ImageURI", outputUri.toString()));

        }

        @Override
        public void onError(Throwable e) {

        }
    };


    private void runTextRecognition(Uri imageUri) throws IOException {
        InputImage image = InputImage.fromFilePath(this, imageUri);
        TextRecognizer recognizer = TextRecognition.getClient();
        recognizer.process(image)
                .addOnSuccessListener(
                        new OnSuccessListener<Text>() {


                            @Override
                            public void onSuccess(Text texts) {

                                ArrayList<ArrayList<String>> row = new ArrayList<ArrayList<String>>(tableData(texts));
                                Log.i("TableData", row.toString());
                                Intent intent = new Intent(ImageSelect_activity.this, TableActivity.class);
                                Gson gson = new Gson();
                                ArrayModal modal = new ArrayModal();
                                modal.array = row;
                                String obj = gson.toJson(modal);
                                intent.putExtra("TABLEDATA", obj);
                                mCropView.setVisibility(View.GONE);
                                mBtnGenerateTable.setVisibility(View.GONE);
                                startActivity(intent);

//                                processTextRecognitionResult(texts);

                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                e.printStackTrace();
                            }
                        });
    }


    private ArrayList<ArrayList<String>> tableData(Text texts) {

        ArrayList<ArrayList<String>> row = new ArrayList<ArrayList<String>>();
        ArrayList<Text.TextBlock> column = new ArrayList<Text.TextBlock>();
        int topExpected = 0;
        int bottomExpected = 0;
        for (int i = 0; i < texts.getTextBlocks().size(); i++) {

            if (i == 0) {
                Text.TextBlock firstBlock = texts.getTextBlocks().get(0);

                Log.i("TABLEDATA", firstBlock.getText() + " | ");

                //added expected top and bottom for one row
                topExpected = firstBlock.getBoundingBox().top - 10;
                bottomExpected = firstBlock.getBoundingBox().bottom + 20;

                //added one column to array
                column.add(firstBlock);

            } else {


                Text.TextBlock currentBlockObj = texts.getTextBlocks().get(i);
                Rect currentBlockFrame = currentBlockObj.getBoundingBox();
                int currentBoxTop = currentBlockFrame.top;
                int currentBoxBottom = currentBlockFrame.bottom;
                if (topExpected == 0 && bottomExpected == 0) {
                    topExpected = currentBoxTop - 15;
                    bottomExpected = currentBoxBottom + 20;
                }
                if (currentBoxTop > topExpected && currentBoxBottom < bottomExpected) {
                    Log.i("TABLEDATA", currentBlockObj.getText() + " | ");
                    column.add(currentBlockObj);


                    //added current element's Rect
                    if (i == texts.getTextBlocks().size() - 1) {
                        ArrayList<Integer> leftCoordinates = new ArrayList<Integer>();
                        for (int a = 0; a < column.size(); a++) {
                            leftCoordinates.add(column.get(a).getBoundingBox().left);
                        }
                        ArrayList<Integer> sortedList = bubbleSort(leftCoordinates);

                        ArrayList<String> sortedStrings = new ArrayList<String>();

                        for (int z = 0; z < sortedList.size(); z++) {
                            for (int col = 0; col < column.size(); col++) {
                                int leftVal = column.get(col).getBoundingBox().left;
                                if (sortedList.get(z) == leftVal) {
                                    sortedStrings.add(column.get(col).getText());
                                    break;
                                }
                            }
                        }

                        row.add(sortedStrings);
                    }
                } else {
                    ArrayList<Integer> leftCoordinates = new ArrayList<Integer>();
                    for (int a = 0; a < column.size(); a++) {
                        leftCoordinates.add(column.get(a).getBoundingBox().left);
                    }
                    ArrayList<Integer> sortedList = bubbleSort(leftCoordinates);

                    ArrayList<String> sortedStrings = new ArrayList<String>();

                    for (int z = 0; z < sortedList.size(); z++) {
                        for (int col = 0; col < column.size(); col++) {
                            int leftVal = column.get(col).getBoundingBox().left;
                            if (sortedList.get(z) == leftVal) {
                                sortedStrings.add(column.get(col).getText());
                                break;
                            }
                        }
                    }

                    row.add(sortedStrings);
                    topExpected = 0;
                    bottomExpected = 0;

                    Log.i("TABLEDATA", "---------------------------------");
                    Log.i("TABLEDATA", currentBlockObj.getText() + " | ");
                    column.clear();
                    column.add(currentBlockObj);


                }

            }


        }
        return row;
    }

    // A function to implement bubble sort
    ArrayList<Integer> bubbleSort(ArrayList<Integer> a)  // function to implement bubble sort
    {
        int n = a.size();
        int i, j, temp;
        for (i = 0; i < n; i++) {
            for (j = i + 1; j < n; j++) {
                if (a.get(j) < a.get(i)) {
                    temp = a.get(i);
                    a.set(i, a.get(j));
                    a.set(j, temp);
                }
            }
        }
        return a;
    }

//    private void processTextRecognitionResult(Text texts) {
//        List<Text.TextBlock> blocks = texts.getTextBlocks();
//        if (blocks.size() == 0) {
//            showToast("No text found");
//            return;
//        }
//    }
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


    private Bitmap.CompressFormat mCompressFormat = Bitmap.CompressFormat.JPEG;
    private final CropCallback mCropCallback = new CropCallback() {
        @Override
        public void onSuccess(Bitmap cropped) {
            mCropView.save(cropped)
                    .compressFormat(mCompressFormat)
                    .execute(createSaveUri(), mSaveCallback);
        }

        @Override
        public void onError(Throwable e) {
        }
    };
    private RectF mFrameRect = null;
    private Uri mSourceUri = null;

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

    @Override
    protected void onResume() {
        super.onResume();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_select);
        if (savedInstanceState != null) {
            // restore data
            mFrameRect = savedInstanceState.getParcelable(KEY_FRAME_RECT);
            mSourceUri = savedInstanceState.getParcelable(KEY_SOURCE_URI);
        }

        mBtnScanImage = findViewById(R.id.btnScanImage);
        mBtnGenerateTable = findViewById(R.id.btnGenerateTable);
        mCropView = findViewById(R.id.cropImageView);

        mBtnGenerateTable.setVisibility(GONE);

        mBtnScanImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ImagePicker.with(ImageSelect_activity.this)
                        .compress(1024)
                        .start(101);
            }
        });

        mBtnGenerateTable.setOnClickListener(new View.OnClickListener() {
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
                mCropView.setVisibility(View.VISIBLE);

                mCropView.load(imageUri)
                        .initialFrameRect(mFrameRect)
                        .useThumbnail(true)
                        .execute(mLoadCallback);

            }
        }
    }

    public Uri createSaveUri() {
        return createNewUri(ImageSelect_activity.this, mCompressFormat);
    }
}