package com.example.myapplication;

import android.content.Context;
import android.util.Log;

import com.example.myapplication.R;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.InputStream;
import java.util.Collections;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleDriveServiceHelper {
    private static GoogleDriveServiceHelper instance;
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private Drive driveService;

    public Drive getDriveService() {
        return driveService;
    }

    public void setDriveService(Drive driveService) {
        this.driveService = driveService;
    }

    private GoogleDriveServiceHelper(Context context) {
        try {

            InputStream inputStream = context.getResources().openRawResource(R.raw.service_account);
            GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream)
                    .createScoped(Collections.singleton(DriveScopes.DRIVE));
            HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

            driveService = new Drive.Builder(
                    new NetHttpTransport(),
                    JSON_FACTORY,
                    requestInitializer
            ).setApplicationName("MyApp").build();
        } catch (Exception e) {
            Log.e("GoogleDriveServiceHelper", "Error initializing Google Drive service", e);
        }
    }

    public static synchronized GoogleDriveServiceHelper getInstance(Context context) {
        if (instance == null) {
            instance = new GoogleDriveServiceHelper(context);
        }
        return instance;
    }

}

