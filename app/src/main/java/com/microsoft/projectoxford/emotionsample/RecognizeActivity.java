//
// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license.
//
// Microsoft Cognitive Services (formerly Project Oxford): https://www.microsoft.com/cognitive-services
//
// Microsoft Cognitive Services (formerly Project Oxford) GitHub:
// https://github.com/Microsoft/Cognitive-Emotion-Android
//
// Copyright (c) Microsoft Corporation
// All rights reserved.
//
// MIT License:
// Permission is hereby granted, free of charge, to any person obtaining
// a copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so, subject to
// the following conditions:
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED ""AS IS"", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
// WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//
package com.microsoft.projectoxford.emotionsample;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.microsoft.projectoxford.emotion.EmotionServiceClient;
import com.microsoft.projectoxford.emotion.EmotionServiceRestClient;
import com.microsoft.projectoxford.emotion.contract.FaceRectangle;
import com.microsoft.projectoxford.emotion.contract.RecognizeResult;
import com.microsoft.projectoxford.emotion.rest.EmotionServiceException;
import com.microsoft.projectoxford.emotionsample.helper.ImageHelper;

import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class RecognizeActivity extends ActionBarActivity {

    // Flag to indicate which task is to be performed.
    public static final int REQUEST_SELECT_IMAGE = 0;

    // The button to select an image
   // public Button mButtonSelectImage;

    // The URI of the image selected to detect.
    public Uri mImageUri;

    // The image selected to detect.
    public Bitmap mBitmap;

    // The edit to show status and result.
    public EditText mEditText;
    //public TextView maxText;

    public  EmotionServiceClient client;

    public String emotion;
    public double max;
    public int c;

    String happyE[]={"\uD83D\uDE00","\uD83D\uDE01","\uD83D\uDE02","\uD83D\uDE02","\uD83D\uDE02"};
    String sadE[]={"\uD83D\uDE14","\uD83D\uDE22","\uD83D\uDE2D","\uD83D\uDE2D","\uD83D\uDE2D"};
    String angerE[]={"\uD83D\uDE21","\uD83D\uDE21","\uD83D\uDE24","\uD83D\uDE24","\uD83D\uDE24"};
    String surpriseE[]={"\uD83D\uDE31","\uD83D\uDE31","\uD83D\uDE31","\uD83D\uDE31","\uD83D\uDE31"};
    String neutralE[]={"\uD83D\uDE11","\uD83D\uDE11","\uD83D\uDE11","\uD83D\uDE11","\uD83D\uDE11"};
    String fearE[]={"\uD83D\uDE28","\uD83D\uDE28","\uD83D\uDE28","\uD83D\uDE28","\uD83D\uDE28"};
    String disgustE[]={"\uD83D\uDE16","\uD83D\uDE16","\uD83D\uDE37","\uD83D\uDE37","\uD83D\uDE37"};
    String contemptE[]={"\uD83D\uDE12","\uD83D\uDE12","\uD83D\uDE12","\uD83D\uDE12","\uD83D\uDE12"};


    TextView numberView;
    EditText txtPhNo;
    TextView value;
    Button contact;
    final static int CONTACTS_CODE = 1000;

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE_PICK_CONTACTS = 1;
    private Uri uriContact;
    private String contactID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognize);


        //contact=(Button)findViewById(R.id.button);
        //numberView=(TextView)findViewById(R.id.textView1);
        //numberView.setText("");
        txtPhNo=(EditText) findViewById(R.id.phoneNo);

        if (client == null) {
            client = new EmotionServiceRestClient(getString(R.string.subscription_key));
        }

        //mButtonSelectImage = (Button) findViewById(R.id.buttonSelectImage);
        mEditText = (EditText) findViewById(R.id.editTextResult);
        c=0;


        //maxText = (TextView) findViewById(R.id.maxText);
    }





    public void onClickSelectContact() {

        // using native contacts selection
        // Intent.ACTION_PICK = Pick an item from the data, returning what was selected.
        startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), REQUEST_CODE_PICK_CONTACTS);
    }


    private void retrieveContactPhoto() {

        Bitmap photo = null;

        try {
            InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(),
                    ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, new Long(contactID)));

            if (inputStream != null) {
                //   photo = BitmapFactory.decodeStream(inputStream);
                //ImageView imageView = (ImageView) findViewById(R.id.img_contact);
                //imageView.setImageBitmap(photo);
            }

            assert inputStream != null;
            inputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void retrieveContactNumber() {

        String contactNumber = null;

        // getting contacts ID
        Cursor cursorID = getContentResolver().query(uriContact,
                new String[]{ContactsContract.Contacts._ID},
                null, null, null);

        if (cursorID.moveToFirst()) {

            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
        }

        cursorID.close();

        //numberView.append("Contact ID: " + contactID);

        // Using the contact ID now we will get contact phone number
        Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

                new String[]{contactID},
                null);

        if (cursorPhone.moveToFirst()) {
            contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }

        cursorPhone.close();

        txtPhNo.setText(contactNumber.toString());
    }

    private void retrieveContactName() {

        String contactName = null;

        // querying contact data store
        Cursor cursor = getContentResolver().query(uriContact, null, null, null, null);

        if (cursor.moveToFirst()) {

            // DISPLAY_NAME = The display name for the contact.
            // HAS_PHONE_NUMBER =   An indicator of whether this contact has at least one phone number.

            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        }

        cursor.close();

        //numberView.append("Contact Name: " + contactName);

    }








    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recognize, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void doRecognize() {
       // mButtonSelectImage.setEnabled(false);

        // Do emotion detection using auto-detected faces.
        try {
            new doRequest(false).execute();
        } catch (Exception e) {
            mEditText.append("Error encountered. Exception is: " + e.toString());
        }

        String faceSubscriptionKey = getString(R.string.faceSubscription_key);
        if (faceSubscriptionKey.equalsIgnoreCase("Please_add_the_face_subscription_key_here")) {
            mEditText.append("\n\nThere is no face subscription key in res/values/strings.xml. Skip the sample for detecting emotions using face rectangles\n");
        } else {
            // Do emotion detection using face rectangles provided by Face API.
            try {
                new doRequest(true).execute();
            } catch (Exception e) {
                mEditText.append("Error encountered. Exception is: " + e.toString());
            }
        }
    }





    /*camer

    Intent i=new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
startActivityForResult(i,0);

protected void onActivityResult(int requestCode, int resultCode,Intent data){

	super.onActivityResult(requestCode,resultCode,data);
	if(resultCode == RESULT_OK){
		Bundle extras = data.getExtras();
		bmp=(Bitmap)extras.get("data");

	}

}

     */
    // Called when the "Select Image" button is clicked.
    public void selectImage(View view) {



        try {
            mEditText.setText("");

            Intent intent;
            intent = new Intent(RecognizeActivity.this, com.microsoft.projectoxford.emotionsample.helper.SelectImageActivity.class);


            //   Intent intent=new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            //startActivityForResult(i,0);

        /*protected void onActivityResult(int requestCode, int resultCode,Intent data){

            super.onActivityResult(requestCode,resultCode,data);
            if(resultCode == RESULT_OK){
                Bundle extras = data.getExtras();
                bmp=(Bitmap)extras.get("data");

            }

        }
          */
            startActivityForResult(intent, 0);
        }catch (Exception e){Toast.makeText(getApplicationContext(), "Error caught", Toast.LENGTH_SHORT).show();}
    }

    // Called when image selection is done.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        try {


            if (requestCode == REQUEST_CODE_PICK_CONTACTS && resultCode == RESULT_OK) {
                // numberView.append("Response: " + data.toString());
                uriContact = data.getData();

                retrieveContactName();
                retrieveContactNumber();
//            retrieveContactPhoto();

            }


            Log.d("RecognizeActivity", "onActivityResult");
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK) {
                        // If image is selected successfully, set the image URI and bitmap.
                        mImageUri = data.getData();

                        //    mBitmap= (Bitmap) data.getExtras().get("data");

                        mBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(
                                mImageUri, getContentResolver());
                        if (mBitmap != null) {
                            //  maxText.setText("not null bro");
                            // Show the image on screen.
                            //ImageView imageView = (ImageView) findViewById(R.id.selectedImage);
                            //imageView.setImageBitmap(mBitmap);

                            // Add detection log.
                            Log.d("RecognizeActivity", "Image: " + mImageUri + " resized to " + mBitmap.getWidth()
                                    + "x" + mBitmap.getHeight());

                            doRecognize();
                        } else {
                            //maxText.setText("null");

                        }
                    }
                    break;
                default:
                    break;
            }
        }catch (Exception e){Toast.makeText(getApplicationContext(), "Error caught", Toast.LENGTH_SHORT).show();}

    }


    public List<RecognizeResult> processWithAutoFaceDetection() throws EmotionServiceException, IOException {
        Log.d("emotion", "Start emotion detection with auto-face detection");

        Gson gson = new Gson();

        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        long startTime = System.currentTimeMillis();
        // -----------------------------------------------------------------------
        // KEY SAMPLE CODE STARTS HERE
        // -----------------------------------------------------------------------

        List<RecognizeResult> result = null;
        //
        // Detect emotion by auto-detecting faces in the image.
        //
        result = this.client.recognizeImage(inputStream);

        String json = gson.toJson(result);
        Log.d("result", json);

        Log.d("emotion", String.format("Detection done. Elapsed time: %d ms", (System.currentTimeMillis() - startTime)));
        // -----------------------------------------------------------------------
        // KEY SAMPLE CODE ENDS HERE
        // -----------------------------------------------------------------------
        return result;
    }

    public List<RecognizeResult> processWithFaceRectangles() throws EmotionServiceException, com.microsoft.projectoxford.face.rest.ClientException, IOException {
        Log.d("emotion", "Do emotion detection with known face rectangles");
        Gson gson = new Gson();

        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        long timeMark = System.currentTimeMillis();
        Log.d("emotion", "Start face detection using Face API");
        FaceRectangle[] faceRectangles = null;
        String faceSubscriptionKey = getString(R.string.faceSubscription_key);
        FaceServiceRestClient faceClient = new FaceServiceRestClient(faceSubscriptionKey);
        Face faces[] = faceClient.detect(inputStream, false, false, null);
        Log.d("emotion", String.format("Face detection is done. Elapsed time: %d ms", (System.currentTimeMillis() - timeMark)));

        if (faces != null) {
            faceRectangles = new FaceRectangle[faces.length];

            for (int i = 0; i < faceRectangles.length; i++) {
                // Face API and Emotion API have different FaceRectangle definition. Do the conversion.
                com.microsoft.projectoxford.face.contract.FaceRectangle rect = faces[i].faceRectangle;
                faceRectangles[i] = new com.microsoft.projectoxford.emotion.contract.FaceRectangle(rect.left, rect.top, rect.width, rect.height);
            }
        }

        List<RecognizeResult> result = null;
        if (faceRectangles != null) {
            inputStream.reset();

            timeMark = System.currentTimeMillis();
            Log.d("emotion", "Start emotion detection using Emotion API");
            // -----------------------------------------------------------------------
            // KEY SAMPLE CODE STARTS HERE
            // -----------------------------------------------------------------------
            result = this.client.recognizeImage(inputStream, faceRectangles);

            String json = gson.toJson(result);
            Log.d("result", json);
            // -----------------------------------------------------------------------
            // KEY SAMPLE CODE ENDS HERE
            // -----------------------------------------------------------------------
            Log.d("emotion", String.format("Emotion detection is done. Elapsed time: %d ms", (System.currentTimeMillis() - timeMark)));
        }
        return result;
    }

    public class doRequest extends AsyncTask<String, String, List<RecognizeResult>> {
        // Store error message
        private Exception e = null;
        private boolean useFaceRectangles = false;

        public doRequest(boolean useFaceRectangles) {
            this.useFaceRectangles = useFaceRectangles;
        }

        @Override
        public List<RecognizeResult> doInBackground(String... args) {

            try {
                if (this.useFaceRectangles == false) {
                    try {
                        return processWithAutoFaceDetection();
                    } catch (Exception e) {
                        this.e = e;    // Store error
                    }
                } else {
                    try {
                        return processWithFaceRectangles();
                    } catch (Exception e) {
                        this.e = e;    // Store error
                    }
                }
                return null;
            }catch (Exception e){return null;}


        }

        @Override
        public void onPostExecute(List<RecognizeResult> result) {
            super.onPostExecute(result);
            // Display based on error existence

            try {

                if (this.useFaceRectangles == false) {
                    //  mEditText.append("\n\nRecognizing emotions with auto-detected face rectangles...\n");
                } else {
                    // mEditText.append("\n\nRecognizing emotions with existing face rectangles from Face API...\n");
                }
                if (e != null) {
                    //mEditText.setText("Error: " + e.getMessage());
                    if (c % 2 == 0)
                        Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    this.e = null;
                } else {
                    if (result.size() == 0) {
                        // mEditText.append("No emotion detected :(");
                        if (c % 2 == 0)
                            Toast.makeText(getApplicationContext(), "No Emotion Detected", Toast.LENGTH_SHORT).show();
                    } else {
                        Integer count = 0;
                        // Covert bitmap to a mutable bitmap by copying it
                        Bitmap bitmapCopy = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
                        Canvas faceCanvas = new Canvas(bitmapCopy);
                        faceCanvas.drawBitmap(mBitmap, 0, 0, null);
                        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                        paint.setStyle(Paint.Style.STROKE);
                        paint.setStrokeWidth(5);
                        paint.setColor(Color.RED);

                        for (RecognizeResult r : result) {
                        /*mEditText.append(String.format("\nFace #%1$d \n", count));
                        mEditText.append(String.format("\t anger: %1$.5f\n", r.scores.anger));
                        mEditText.append(String.format("\t contempt: %1$.5f\n", r.scores.contempt));
                        mEditText.append(String.format("\t disgust: %1$.5f\n", r.scores.disgust));
                        mEditText.append(String.format("\t fear: %1$.5f\n", r.scores.fear));
                        mEditText.append(String.format("\t happiness: %1$.5f\n", r.scores.happiness));
                        mEditText.append(String.format("\t neutral: %1$.5f\n", r.scores.neutral));
                        mEditText.append(String.format("\t sadness: %1$.5f\n", r.scores.sadness));
                        mEditText.append(String.format("\t surprise: %1$.5f\n", r.scores.surprise));
                        mEditText.append(String.format("\t face rectangle: %d, %d, %d, %d", r.faceRectangle.left, r.faceRectangle.top, r.faceRectangle.width, r.faceRectangle.height));*/
                            faceCanvas.drawRect(r.faceRectangle.left,
                                    r.faceRectangle.top,
                                    r.faceRectangle.left + r.faceRectangle.width,
                                    r.faceRectangle.top + r.faceRectangle.height,
                                    paint);
                            count++;

                            max = r.scores.anger;
                            emotion = "";
                            double exp[] = {r.scores.anger, r.scores.contempt, r.scores.disgust, r.scores.fear, r.scores.happiness, r.scores.neutral, r.scores.sadness, r.scores.surprise};
                            for (int i = 0; i < 8; i++) {
                                if (exp[i] > max) {
                                    max = exp[i];
                                }
                            }
                            if (r.scores.anger == max) {
                                emotion = "anger";
                            } else if (r.scores.surprise == max) emotion = "surprise";
                            else if (r.scores.contempt == max) emotion = "contempt";
                            else if (r.scores.fear == max) emotion = "fear";
                            else if (r.scores.disgust == max) emotion = "disgust";
                            else if (r.scores.sadness == max) emotion = "sad";
                            else if (r.scores.neutral == max) emotion = "neutral";
                            else if (r.scores.happiness == max) emotion = "happy";

                            //maxText.setText("face done");
                            //Toast.makeText(getApplicationContext(), emotion, Toast.LENGTH_SHORT).show();
                            //Toast.makeText(getApplicationContext(), "face done", Toast.LENGTH_LONG).show();

                        }
                        //ImageView imageView = (ImageView) findViewById(R.id.selectedImage);
                        //imageView.setImageDrawable(new BitmapDrawable(getResources(), mBitmap));

                        if (c % 2 == 0) {
                            //mEditText.append(emotion + " " + max);

                            //emotion max
                            if (emotion.equals("happy"))
                                mEditText.append(happyE[(int) ((max - 0.5) * 10)]);
                            else if (emotion.equals("sad"))
                                mEditText.append(sadE[(int) ((max - 0.5) * 10)]);
                            else if (emotion.equals("surprise"))
                                mEditText.append(surpriseE[(int) ((max - 0.5) * 10)]);
                            else if (emotion.equals("anger"))
                                mEditText.append(angerE[(int) ((max - 0.5) * 10)]);
                            else if (emotion.equals("neutral"))
                                mEditText.append(neutralE[(int) ((max - 0.5) * 10)]);
                            else if (emotion.equals("fear"))
                                mEditText.append(fearE[(int) ((max - 0.5) * 10)]);
                            else if (emotion.equals("disgust"))
                                mEditText.append(disgustE[(int) ((max - 0.5) * 10)]);
                            else if (emotion.equals("contempt"))
                                mEditText.append(contemptE[(int) ((max - 0.5) * 10)]);
                            else
                                Toast.makeText(getApplicationContext(), "No Emotions Detected!", Toast.LENGTH_LONG).show();


                            //mEditText.append("");
                        }
                        c++;
                    }
                    mEditText.setSelection(0);
                }

                //  mButtonSelectImage.setEnabled(true);
            }catch (Exception e){Toast.makeText(getApplicationContext(), "Error caught", Toast.LENGTH_SHORT).show();}
        }
    }
}
