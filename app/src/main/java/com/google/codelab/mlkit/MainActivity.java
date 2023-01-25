// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.codelab.mlkit;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.codelab.mlkit.GraphicOverlay.Graphic;
import com.google.gson.Gson;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = "MainActivity";
    /**
     * Number of results to show in the UI.
     */
    private static final int RESULTS_TO_SHOW = 3;
    /**
     * Dimensions of inputs.
     */
    private static final int DIM_IMG_SIZE_X = 224;
    private static final int DIM_IMG_SIZE_Y = 224;
    private final PriorityQueue<Map.Entry<String, Float>> sortedLabels =
            new PriorityQueue<>(
                    RESULTS_TO_SHOW,
                    new Comparator<Map.Entry<String, Float>>() {
                        @Override
                        public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float>
                                o2) {
                            return (o1.getValue()).compareTo(o2.getValue());
                        }
                    });
    private ImageView mImageView;
    private Button mTextButton;
    private Button mFaceButton;
    private TextView mPrintText;
    private Bitmap mSelectedImage;
    private GraphicOverlay mGraphicOverlay;
    // Max width (portrait mode)
    private Integer mImageMaxWidth;
    // Max height (portrait mode)
    private Integer mImageMaxHeight;

    public static Bitmap getBitmapFromAsset(Context context, String filePath) {
        AssetManager assetManager = context.getAssets();

        InputStream is;
        Bitmap bitmap = null;
        try {
            is = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = findViewById(R.id.image_view);

        mTextButton = findViewById(R.id.button_text);
        mFaceButton = findViewById(R.id.button_face);
        mPrintText = findViewById(R.id.printText);

        mGraphicOverlay = findViewById(R.id.graphic_overlay);
        mTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runTextRecognition();
            }
        });
        mFaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runFaceContourDetection();
            }
        });
        Spinner dropdown = findViewById(R.id.spinner);
        String[] items = new String[]{"Test Image 1 (Text)", "Test Image 2 (Face)"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout
                .simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(this);
    }

    private void runTextRecognition() {
        InputImage image = InputImage.fromBitmap(mSelectedImage, 0);
        TextRecognizer recognizer = TextRecognition.getClient();
        mTextButton.setEnabled(false);
        recognizer.process(image)
                .addOnSuccessListener(
                        new OnSuccessListener<Text>() {


                            @Override
                            public void onSuccess(Text texts) {
                                mTextButton.setEnabled(true);


                             /*   ArrayList<ArrayList<String>> row = new ArrayList<ArrayList<String>>();
                                ArrayList<String> column = new ArrayList<String>();
                                StringBuffer strBuffer = new StringBuffer();

                                for (int i = 0; i < texts.getTextBlocks().size(); i++) {

                                    if (i == 0) {
                                        Text.TextBlock block = texts.getTextBlocks().get(0);
                                        String text = block.getText();
                                        Log.i("TABLEDATA", block.getText() + " | ");
                                        strBuffer.append(text + "|");
                                        column.add(text);
                                    } else {
                                        Text.TextBlock blockObj = texts.getTextBlocks().get(i);
                                        Rect blockFrame = blockObj.getBoundingBox();
                                        int currentBoxTop = blockFrame.top;
                                        int currentBoxBottom = blockFrame.bottom;

                                        Text.TextBlock prevBlockObj = texts.getTextBlocks().get(i - 1);
                                        Rect prevBlockFrame = prevBlockObj.getBoundingBox();
                                        int previousBoxTop = prevBlockFrame.top;
                                        int previousBoxBottom = prevBlockFrame.bottom;
                                        int prevTopApproxStart = previousBoxTop - 15;
                                        int prevBottomApproxEnd = previousBoxBottom + 15;

                                        if (currentBoxTop > prevTopApproxStart && currentBoxBottom < prevBottomApproxEnd) {
                                            Log.i("TABLEDATA", blockObj.getText() + " | ");
                                            strBuffer.append(blockObj.getText() + "|");
                                            column.add(blockObj.getText());
                                            if (i == texts.getTextBlocks().size() - 1) {
                                                ArrayList<String> newList = new ArrayList<String>(column);
                                                row.add(newList);
                                            }
                                        } else {
                                            ArrayList<String> newList = new ArrayList<String>(column);
                                            row.add(newList);

                                            Log.i("TABLEDATA", "---------------------------------");
                                            Log.i("TABLEDATA", blockObj.getText() + " | ");
                                            strBuffer.delete(0, strBuffer.length());
                                            strBuffer.append(blockObj.getText() + "|");
                                            column.clear();
                                            column.add(blockObj.getText());
                                        }
                                    }
                                }*/

                                ArrayList<ArrayList<String>> row = new ArrayList<ArrayList<String>>(tableData(texts));
                                Log.i("TableData", row.toString());
                                Intent intent = new Intent(MainActivity.this, TableActivity.class);
                                Gson gson = new Gson();
                                ArrayModal modal = new ArrayModal();
                                modal.array = row;

                                String obj = gson.toJson(modal);
                                intent.putExtra("TABLEDATA", obj);
                                startActivity(intent);

                                processTextRecognitionResult(texts);

                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                mTextButton.setEnabled(true);
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
                topExpected = firstBlock.getBoundingBox().top-10;
                bottomExpected = firstBlock.getBoundingBox().bottom + 20;

                //added one column to array
                column.add(firstBlock);

            } else {



                Text.TextBlock currentBlockObj = texts.getTextBlocks().get(i);
                Rect currentBlockFrame = currentBlockObj.getBoundingBox();
                int currentBoxTop = currentBlockFrame.top;
                int currentBoxBottom = currentBlockFrame.bottom;
                if(topExpected==0 && bottomExpected == 0){
                    topExpected = currentBoxTop-15;
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
                        Log.i("ROWDATA", row.toString());
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
                    topExpected=0;
                    bottomExpected=0;

                    Log.i("TABLEDATA", "---------------------------------");
                    Log.i("TABLEDATA", currentBlockObj.getText() + " | ");
                    column.clear();
                    column.add(currentBlockObj);

                    Log.i("ROWDATA", row.toString());

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

    private void processTextRecognitionResult(Text texts) {
        List<Text.TextBlock> blocks = texts.getTextBlocks();
        if (blocks.size() == 0) {
            showToast("No text found");
            return;
        }
        mGraphicOverlay.clear();
        for (int i = 0; i < blocks.size(); i++) {
            List<Text.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                List<Text.Element> elements = lines.get(j).getElements();
                for (int k = 0; k < elements.size(); k++) {
                    Graphic textGraphic = new TextGraphic(mGraphicOverlay, elements.get(k));
                    mGraphicOverlay.add(textGraphic);

                }
            }
        }
    }

    private void runFaceContourDetection() {
        InputImage image = InputImage.fromBitmap(mSelectedImage, 0);
        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                        .build();

        mFaceButton.setEnabled(false);
        FaceDetector detector = FaceDetection.getClient(options);
        detector.process(image)
                .addOnSuccessListener(
                        new OnSuccessListener<List<Face>>() {
                            @Override
                            public void onSuccess(List<Face> faces) {
                                mFaceButton.setEnabled(true);
                                processFaceContourDetectionResult(faces);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                mFaceButton.setEnabled(true);
                                e.printStackTrace();
                            }
                        });

    }

    private void processFaceContourDetectionResult(List<Face> faces) {
        // Task completed successfully
        if (faces.size() == 0) {
            showToast("No face found");
            return;
        }
        mGraphicOverlay.clear();
        for (int i = 0; i < faces.size(); ++i) {
            Face face = faces.get(i);
            FaceContourGraphic faceGraphic = new FaceContourGraphic(mGraphicOverlay);
            mGraphicOverlay.add(faceGraphic);
            faceGraphic.updateFace(face);
        }
    }

    // Functions for loading images from app assets.

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    // Returns max image width, always for portrait mode. Caller needs to swap width / height for
    // landscape mode.
    private Integer getImageMaxWidth() {
        if (mImageMaxWidth == null) {
            // Calculate the max width in portrait mode. This is done lazily since we need to
            // wait for
            // a UI layout pass to get the right values. So delay it to first time image
            // rendering time.
            mImageMaxWidth = mImageView.getWidth();
        }

        return mImageMaxWidth;
    }

    // Returns max image height, always for portrait mode. Caller needs to swap width / height for
    // landscape mode.
    private Integer getImageMaxHeight() {
        if (mImageMaxHeight == null) {
            // Calculate the max width in portrait mode. This is done lazily since we need to
            // wait for
            // a UI layout pass to get the right values. So delay it to first time image
            // rendering time.
            mImageMaxHeight =
                    mImageView.getHeight();
        }

        return mImageMaxHeight;
    }

    // Gets the targeted width / height.
    private Pair<Integer, Integer> getTargetedWidthHeight() {
        int targetWidth;
        int targetHeight;
        int maxWidthForPortraitMode = getImageMaxWidth();
        int maxHeightForPortraitMode = getImageMaxHeight();
        targetWidth = maxWidthForPortraitMode;
        targetHeight = maxHeightForPortraitMode;
        return new Pair<>(targetWidth, targetHeight);
    }

    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        mGraphicOverlay.clear();
        switch (position) {
            case 0:
                mSelectedImage = getBitmapFromAsset(this, "Please_walk_on_the_grass.jpg");
                break;
            case 1:
                // Whatever you want to happen when the thrid item gets selected
                mSelectedImage = getBitmapFromAsset(this, "grace_hopper.jpg");
                break;
        }
        if (mSelectedImage != null) {
            // Get the dimensions of the View
            Pair<Integer, Integer> targetedSize = getTargetedWidthHeight();

            int targetWidth = targetedSize.first;
            int maxHeight = targetedSize.second;

            // Determine how much to scale down the image
            float scaleFactor =
                    Math.max(
                            (float) mSelectedImage.getWidth() / (float) targetWidth,
                            (float) mSelectedImage.getHeight() / (float) maxHeight);

            Bitmap resizedBitmap =
                    Bitmap.createScaledBitmap(
                            mSelectedImage,
                            (int) (mSelectedImage.getWidth() / scaleFactor),
                            (int) (mSelectedImage.getHeight() / scaleFactor),
                            true);

            mImageView.setImageBitmap(resizedBitmap);
            mSelectedImage = resizedBitmap;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing
    }
}
