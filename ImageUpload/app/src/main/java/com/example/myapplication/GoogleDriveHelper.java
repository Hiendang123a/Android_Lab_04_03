package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.myapplication.R;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.hdodenhof.circleimageview.CircleImageView;

public class GoogleDriveHelper {
    private static final String SOCIETY_FOLDER_ID = "1z4vhJeKcg-dvVy5gTmHm0b8n2tJDTsja";
    private final Drive driveService;
    private final Context context;
    private static String fileID;

    public String getFileID() {
        return fileID;
    }

    private static final ExecutorService executorService = Executors.newFixedThreadPool(1); // Chạy trên 1 thread nền
    public GoogleDriveHelper(Context context) {
        this.context = context;
        this.driveService = GoogleDriveServiceHelper.getInstance(context).getDriveService();
    }

    public void uploadFileToSociety(Uri fileUri, String mimeType, Runnable onComplete) {
        executorService.execute(() -> {
            try {
                if (driveService == null || fileUri == null) {
                    Log.e("GoogleDriveHelper", "Error: Invalid input parameters.");
                    if (onComplete != null) onComplete.run();
                    return;
                }

                long[] imageSizeHolder = new long[1]; // Lưu kích thước file
                InputStream resizedInputStream = resizeImage(fileUri, imageSizeHolder);
                if (imageSizeHolder[0] <= 0) {
                    Log.e("GoogleDriveHelper", "Error: Unable to resize image.");
                    if (onComplete != null) onComplete.run();
                    return;
                }

                File fileMetadata = new File();
                fileMetadata.setName(UUID.randomUUID().toString() + ".jpg");
                fileMetadata.setParents(Collections.singletonList(SOCIETY_FOLDER_ID));

                InputStreamContent mediaContent = new InputStreamContent(mimeType, resizedInputStream);
                mediaContent.setLength(imageSizeHolder[0]); // Đặt kích thước chính xác

                Drive.Files.Create request = driveService.files().create(fileMetadata, mediaContent);
                MediaHttpUploader uploader = request.getMediaHttpUploader();
                uploader.setChunkSize(MediaHttpUploader.MINIMUM_CHUNK_SIZE * 4);
                uploader.setProgressListener(progress ->
                        Log.d("GoogleDriveHelper", "Uploaded: " + uploader.getNumBytesUploaded() + " bytes"));
                File uploadedFile = request.execute();

                if (uploadedFile != null && uploadedFile.getId() != null) {
                    Log.d("GoogleDriveHelper", "File uploaded successfully: " + uploadedFile.getId());
                    fileID = uploadedFile.getId();
                } else {
                    Log.e("GoogleDriveHelper", "File upload failed.");
                }
            } catch (IOException e) {
                Log.e("GoogleDriveHelper", "IO error: " + e.getMessage(), e);
            } catch (Exception e) {
                Log.e("GoogleDriveHelper", "Unexpected error during upload", e);
            } finally {
                if (onComplete != null) onComplete.run();
            }
        });
    }

    private InputStream resizeImage(Uri uri, long[] imageSizeHolder) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        if (inputStream != null) {
            inputStream.close();
        }

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 800, 800, true);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);

        byte[] byteArray = outputStream.toByteArray();
        imageSizeHolder[0] = byteArray.length;

        return new ByteArrayInputStream(byteArray);
    }
    public void deleteFileFromSociety(String fileID) {
        executorService.execute(() -> {
            try {
                if (driveService == null) {
                    Log.e("GoogleDriveHelper", "Drive service is not initialized.");
                    return;
                }
                driveService.files().delete(fileID).execute();
                Log.d("GoogleDriveHelper", "File deleted successfully with ID: " + fileID);
            } catch (Exception e) {
                Log.e("GoogleDriveHelper", "File deletion failed", e);
            }
        });
    }

    public static void loadImage(Context context, String imageId, CircleImageView imageView) {
        String imageUrl = "https://drive.google.com/uc?id=" + imageId;
        Glide.with(context)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Lưu cache để load nhanh hơn lần sau
                .override(100, 100) // Resize ảnh xuống 300x300 px
                .timeout(10000) // 10 giây
                .into(imageView);
    }
    public static void loadImage(Context context, String imageId, ImageView imageView) {
        String imageUrl = "https://drive.google.com/uc?id=" + imageId;
        Glide.with(context)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Lưu cache để load nhanh hơn lần sau
                .override(100, 100) // Resize ảnh xuống 300x300 px
                .timeout(10000) // 10 giây
                .into(imageView);
    }

}
