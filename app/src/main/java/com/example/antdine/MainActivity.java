package com.example.antdine;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;

    private ImageView imageView;
    private Bitmap firstImageBitmap;
    private Bitmap secondImageBitmap;
    private Button resetButton;
    private int currentImage = 2;
    private static final String FIRST_IMAGE_FILENAME = "first_image.jpg";
    private static final String SECOND_IMAGE_FILENAME = "second_image.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.id_view);
        loadImages();

        resetButton = findViewById(R.id.reset_button);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageView.setImageResource(android.R.drawable.ic_menu_add);
                firstImageBitmap = null;
                secondImageBitmap = null;

                // Delete stored images
                deleteImage(FIRST_IMAGE_FILENAME);
                deleteImage(SECOND_IMAGE_FILENAME);

                Toast.makeText(MainActivity.this, "Reset successful", Toast.LENGTH_SHORT).show();
            }
        });

        Animation scaleDown = new ScaleAnimation(1.0f, 0.0f, 1.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleDown.setDuration(350);
        Animation scaleUp = new ScaleAnimation(1.0f, 0.0f, 1.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleUp.setDuration(350);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.purple));
        }

        imageView.setOnClickListener(v -> {
            if (firstImageBitmap == null) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, PICK_IMAGE);
            } else if (secondImageBitmap == null) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, PICK_IMAGE);
            } else {
                imageView.startAnimation(scaleDown);
                if (currentImage == 1) {
                    imageView.setImageBitmap(secondImageBitmap);
                    currentImage = 2;
                } else {
                    imageView.setImageBitmap(firstImageBitmap);
                    currentImage = 1;
                }
                imageView.startAnimation(scaleUp);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                if (firstImageBitmap == null) {
                    firstImageBitmap = bitmap;
                    saveImage(bitmap, FIRST_IMAGE_FILENAME);
                } else if (secondImageBitmap == null) {
                    secondImageBitmap = bitmap;
                    saveImage(bitmap, SECOND_IMAGE_FILENAME);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveImage(Bitmap bitmap, String filename) {
        try {
            FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
            Toast.makeText(this, "Image saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadImages() {
        firstImageBitmap = loadImage(FIRST_IMAGE_FILENAME);
        secondImageBitmap = loadImage(SECOND_IMAGE_FILENAME);
        if (firstImageBitmap != null) {
            imageView.setImageBitmap(firstImageBitmap);
            currentImage = 1;
        }
    }

    private Bitmap loadImage(String filename) {
        try {
            File file = new File(getFilesDir(), filename);
            FileInputStream fis = new FileInputStream(file);
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(file));
            fis.close();
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void deleteImage(String filename) {
        try {
            deleteFile(filename);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to delete image", Toast.LENGTH_SHORT).show();
        }
    }
}