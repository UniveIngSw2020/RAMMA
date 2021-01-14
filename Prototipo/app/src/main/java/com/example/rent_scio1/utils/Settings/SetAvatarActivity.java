package com.example.rent_scio1.utils.Settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.rent_scio1.Client.MapsActivityClient;
import com.example.rent_scio1.Client.SettingsCustomer;
import com.example.rent_scio1.R;
import com.example.rent_scio1.Trader.MapsActivityTrader;
import com.example.rent_scio1.Trader.SettingsTrader;
import com.example.rent_scio1.utils.UserClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class SetAvatarActivity extends AppCompatActivity {

    private static final int RESULT_LOAD_IMAGE = 1;

    Bitmap bitmapImage;

    ImageView imageView;

    boolean isDefault=true;

    boolean changed=false;

    private Bitmap getBitmap(int drawableRes) {
        Drawable drawable = ContextCompat.getDrawable(this, drawableRes);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private void loadPrev() throws IOException {

        FirebaseStorage storage = FirebaseStorage.getInstance();

        StorageReference storageRef = storage.getReference();

        StorageReference avatarRef = storageRef.child("users/"+UserClient.getUser().getUser_id()+"/avatar.jpg");

        File localFile = File.createTempFile( UserClient.getUser().getUser_id(), "jpg");


        avatarRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {

            bitmapImage = BitmapFactory.decodeFile(localFile.getPath());
            imageView.setImageBitmap(bitmapImage);
            isDefault=false;

        }).addOnFailureListener(exception -> {

            if(UserClient.getUser().getTrader()){
                bitmapImage=getBitmap(R.drawable.negozio_vettorizzato);
            }
            else{
                bitmapImage=getBitmap(R.drawable.ic_logo_vettorizzato);
            }
            imageView.setImageBitmap( bitmapImage );
            isDefault=true;
        });

    }

    private void deletePrev() {

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference avatarRef = storageRef.child("users/"+UserClient.getUser().getUser_id()+"/avatar.jpg");

        avatarRef.delete();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_avatar);


        imageView=(ImageView)findViewById(R.id.avatar);

        //prendi l'avatar settato precedentemente
        try {
            loadPrev();
        } catch (IOException e) {
            e.printStackTrace();
        }

        initViews();

        Intent intent;
        if(UserClient.getUser().getTrader()){
            intent = new Intent(getApplicationContext(), MapsActivityTrader.class);
        }
        else{
            intent = new Intent(getApplicationContext(), MapsActivityClient.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        /*elimina avatar*/
        Button delete_avatar = findViewById(R.id.elimina_avatar);
        delete_avatar.setOnClickListener(v -> {

            if(UserClient.getUser().getTrader()){
                imageView.setImageBitmap(getBitmap(R.drawable.negozio_vettorizzato));
            }
            else {
                imageView.setImageBitmap(getBitmap(R.drawable.ic_logo_vettorizzato));
            }

            if(!isDefault){
                changed=true;
            }

            bitmapImage=null;
            isDefault=true;

        });


        //setto il comportamento del bottone conferma
        Button buttonConfirm =  findViewById(R.id.confirm_changes_picture);

        buttonConfirm.setOnClickListener(v -> {

            if(!isDefault) {

                //upload su db
                uploadImage(o -> {
                    Toast.makeText(getApplicationContext(),"avatar personalizzato cambiato",Toast.LENGTH_LONG).show();
                    startActivity(intent);

                });
            }
            else {
                Toast.makeText(getApplicationContext(),"avatar di default ripristinato",Toast.LENGTH_LONG).show();
                deletePrev();
                startActivity(intent);
            }
        });

    }

    public void initViews(){
        Toolbar toolbar_settings = findViewById(R.id.toolbar_settings_avatar);
        setSupportActionBar(toolbar_settings);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_settings_avatar_client, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.add_one_image) {

            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            photoPickerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(photoPickerIntent, RESULT_LOAD_IMAGE);

        } else if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return true;
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

                isDefault=false;
                changed=true;

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

    @Override
    public void onBackPressed() {

        if(changed) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("ATTENZIONE:");
            builder.setMessage("Sei sicuro di voler uscire dalla schermata senza confermare i cambiamenti?\n");

            builder.setPositiveButton("Sì", (dialog, id) ->{
                dialog.dismiss();
                finish();
            });
            builder.setNegativeButton("No", (dialog, id) ->{
                dialog.dismiss();
            });
            builder.create().show();
        }
        else{
            finish();
        }
    }

}