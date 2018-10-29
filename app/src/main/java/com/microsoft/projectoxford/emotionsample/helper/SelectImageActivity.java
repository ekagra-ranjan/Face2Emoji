//
// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license.
//
// Microsoft Cognitive Services (formerly Project Oxford): https://www.microsoft.com/cognitive-services
//
// Microsoft Cognitive Services (formerly Project Oxford) GitHub:
// https://github.com/Microsoft/Cognitive-Emotion-Android

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
package com.microsoft.projectoxford.emotionsample.helper;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.projectoxford.emotion.EmotionServiceRestClient;
import com.microsoft.projectoxford.emotionsample.MainActivity;
import com.microsoft.projectoxford.emotionsample.R;
import com.microsoft.projectoxford.emotionsample.RecognizeActivity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

// The activity for the user to select a image and to detect faces in the image.
public class SelectImageActivity extends RecognizeActivity {
    // Flag to indicate the request of the next task to be performed
    private static final int REQUEST_TAKE_PHOTO = 0;
    private static final int REQUEST_SELECT_IMAGE_IN_ALBUM = 1;

    // The URI of photo taken with camera
    private Uri mUriPhotoTaken;
    //public TextView maxText;
    Button takePhotoButton;




   public Button sendBtn;
   //public  EditText txtMessage;
   public  EditText txtPhNo;
    Button contact;


    final static int CONTACTS_CODE = 1000;

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE_PICK_CONTACTS = 1;
    private Uri uriContact;
    private String contactID;


    // When the activity is created, set all the member variables to initial state.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_image);

        sendBtn=(Button) findViewById(R.id.button_send);
        txtPhNo=(EditText) findViewById(R.id.phoneNo);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        contact=(Button)findViewById(R.id.button);

        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSelectContact();
            }
        });

        if (client == null) {
            client = new EmotionServiceRestClient(getString(R.string.subscription_key));
        }

        //mButtonSelectImage = (Button) findViewById(R.id.buttonSelectImage);
        mEditText = (EditText) findViewById(R.id.editTextResult);
//        maxText.setText("");
        //maxText = (TextView) findViewById(R.id.maxText);

      //  maxText=(TextView)findViewById(R.id.maxText);
        takePhotoButton=(Button)findViewById(R.id.button_take_a_photo);
        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // Toast.makeText(getApplicationContext(), "take photo on click", Toast.LENGTH_SHORT).show();
                takePhoto(v);
            }
        });

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









    // Save the activity state when it's going to stop.
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("ImageUri", mUriPhotoTaken);
          }

    // Recover the saved state when the activity is recreated.
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mUriPhotoTaken = savedInstanceState.getParcelable("ImageUri");
    }

    // Deal with the result of selection of the photos and faces.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {



        if (requestCode == REQUEST_CODE_PICK_CONTACTS && resultCode == RESULT_OK) {
            // numberView.append("Response: " + data.toString());
            uriContact = data.getData();

            retrieveContactName();
            retrieveContactNumber();
//            retrieveContactPhoto();

        }



        switch (requestCode)
        {
            case REQUEST_TAKE_PHOTO:
            case REQUEST_SELECT_IMAGE_IN_ALBUM:
                if (resultCode == RESULT_OK) {
                    Uri imageUri;
                    if (data == null || data.getData() == null) {
                        //imageUri = mUriPhotoTaken;
                        mImageUri = mUriPhotoTaken;
                    } else {
                        //imageUri = data.getData();
                        mImageUri = data.getData();
                    }
                    /*Intent intent = new Intent();
                    intent.setData(imageUri);
                    setResult(RESULT_OK, intent);
                    finish();*/
                    //mImageUri = data.getData();

                    //    mBitmap= (Bitmap) data.getExtras().get("data");

                    mBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(
                            mImageUri, getContentResolver());
                    if (mBitmap != null) {
                        //maxText.setText("not null bro");
                        // Show the image on screen.
                    //    ImageView imageView = (ImageView) findViewById(R.id.selectedImage);
                     //   imageView.setImageBitmap(mBitmap);

                        // Add detection log.
                        Log.d("RecognizeActivity", "Image: " + mImageUri + " resized to " + mBitmap.getWidth()
                                + "x" + mBitmap.getHeight());

                        doRecognize();
                    }
                    else{
                     //   maxText.setText("null");

                    }




                }
                break;
            default:
                break;
        }
    }

    // When the button of "Take a Photo with Camera" is pressed.
    public void takePhoto(View view) {
        //maxText.append(" "+"take photo");
      //  Toast.makeText(getApplicationContext(), "take photo", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager()) != null) {
            // Save the photo taken to a temporary file.
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            try {
                File file = File.createTempFile("IMG_", ".jpg", storageDir);
                mUriPhotoTaken = Uri.fromFile(file);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mUriPhotoTaken);
                startActivityForResult(intent, REQUEST_TAKE_PHOTO);
            } catch (IOException e) {
                setInfo(e.getMessage());
            }
        }

    }

    // When the button of "Select a Photo in Album" is pressed.
    public void selectImageInAlbum(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_SELECT_IMAGE_IN_ALBUM);
        }
    }

    // Set the information panel on screen.
    private void setInfo(String info) {
       // TextView textView = (TextView) findViewById(R.id.info);
       // textView.setText(info);
    }









    //SMS
    protected void sendMessage()
    {
        Log.i("Send SMS","");
        String Phno=txtPhNo.getText().toString();
        String Mess=mEditText.getText().toString();

        try{
            SmsManager smsManager=SmsManager.getDefault();
            smsManager.sendTextMessage(Phno,null,Mess,null,null);
            Toast.makeText(getApplicationContext(),"Sms send",Toast.LENGTH_LONG).show();

        }
        catch (Exception e)
        {
            Toast.makeText(getApplicationContext(),"Sms not send",Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        //mEditText.append(emotion+" "+max);
        //Toast.makeText(getApplicationContext(),"Sms send",Toast.LENGTH_LONG).show();


        //    mEditText.setText("\uD83D\uDE00"+"\uD83D\uDE01"+"\uD83D\uDE02"+"\uD83D\uDE03"+"\uD83D\uDE04"+"\uD83D\uDE05"+"\uD83D\uDE06"+"\uD83D\uDE07"+"\uD83D\uDE08"+"\uD83D\uDE09"+"\uD83D\uDE10"+"\uD83D\uDE11"+"\uD83D\uDE12"+"\uD83D\uDE13"+"\uD83D\uDE14"+"\uD83D\uDE15"+"\uD83D\uDE16"+"\uD83D\uDE17"+"\uD83D\uDE18"+"\uD83D\uDE19"+"\uD83D\uDE20"+"\uD83D\uDE21"+"\uD83D\uDE22"+"\uD83D\uDE23"+"\uD83D\uDE24"+"\uD83D\uDE25"+"\uD83D\uDE26"+"\uD83D\uDE27"+"\uD83D\uDE28"+"\uD83D\uDE29"+"\uD83D\uDE30"+"\uD83D\uDE31"+"\uD83D\uDE32"+"\uD83D\uDE33"+"\uD83D\uDE34"+"\uD83D\uDE35"+"\uD83D\uDE36"+"\uD83D\uDE37"+"\uD83D\uDE38"+"\uD83D\uDE39"+"\uD83D\uDE40"+"\uD83D\uDE41"+"\uD83D\uDE42"+"\uD83D\uDE43"+"\uD83D\uDE44"+"\uD83D\uDE45"+"\uD83D\uDE46"+"\uD83D\uDE47"+"\uD83D\uDE48"+"\uD83D\uDE49"+"\uD83D\uDE50");
    }







}
