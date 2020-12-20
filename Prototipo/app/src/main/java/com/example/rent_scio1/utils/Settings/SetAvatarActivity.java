package com.example.rent_scio1.utils.Settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.rent_scio1.Client.SettingsCustomer;
import com.example.rent_scio1.R;
import com.example.rent_scio1.utils.UserClient;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class SetAvatarActivity extends AppCompatActivity {

    private static final int RESULT_LOAD_IMAGE = 1;
    Bitmap bitmapImage;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_avatar);

        imageView=(ImageView)findViewById(R.id.avatar);

        //setto il comportamento del bottone carica
        Button buttonLoadImage = findViewById(R.id.buttonLoadPicture);
        buttonLoadImage.setOnClickListener(arg0 -> {

            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, RESULT_LOAD_IMAGE);
        });

        //setto il comportamento del bottone conferma
        Button buttonConfirm =  findViewById(R.id.confirm_changes_picture);
        buttonConfirm.setOnClickListener(v -> {

            if(bitmapImage!=null) {

                //aggiorno l'oggetto
                UserClient.getUser().setAvatar(bitmapImage);

                uploadImage(o -> {
                    Intent intent = new Intent(getApplicationContext(), SettingsCustomer.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                });


            }
            else{
                Toast.makeText(getApplicationContext(),"Carica prima un'immagine!",Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            try {

                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);

                bitmapImage = BitmapFactory.decodeStream(imageStream);

                imageView.setImageBitmap(bitmapImage);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Qualcosa è andato storto... scegli un'altra immagine", Toast.LENGTH_LONG).show();
            }

        }else {
            Toast.makeText(this, "Non hai selezionato nulla",Toast.LENGTH_LONG).show();
        }
    }

    public void uploadImage(OnSuccessListener listener) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        FirebaseStorage storage = FirebaseStorage.getInstance();

        StorageReference storageRef = storage.getReference();

        StorageReference avatarRef = storageRef.child("users/"+UserClient.getUser().getUser_id()+"/avatar.jpg");


        UploadTask uploadTask = avatarRef.putBytes(data);
        uploadTask.addOnFailureListener(exception ->
                Toast.makeText(SetAvatarActivity.this, "Qualcosa è andato storto... scegli un'altra immagine", Toast.LENGTH_LONG).show())
                .addOnSuccessListener(listener);

    }



}