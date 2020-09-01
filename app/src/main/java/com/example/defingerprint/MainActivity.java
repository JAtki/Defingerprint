package com.example.defingerprint;

import android.Manifest;
import android.app.role.RoleManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import static android.app.role.RoleManager.ROLE_SMS;

public class MainActivity extends AppCompatActivity {


    private static final String[] perms = {Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.READ_CONTACTS, Manifest.permission.READ_SMS};

    private Switch sw;
    private RoleManager rm;

    /**
     * Compares all permissions in the provided array with the permissions granted to the application.
     * @param permissions An array of permissions to check.
     * @return true if all listed permissions are held by the application, otherwise false.
     */
    private boolean checkAllPermissions(String[] permissions)
    {
        for (String p : permissions)
        {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED)
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Create and run a new thread of GenerateSMSRunnable and notify the user.
     * @param operations The number of messages to be generated by generateSMSRunnable
     */
    private void generateSMS(int operations)
    {
        Toast.makeText(getApplicationContext(), "Generating messages", Toast.LENGTH_LONG).show();
        GenerateSMSRunnable genSMS = new GenerateSMSRunnable(operations, getApplicationContext());
        new Thread(genSMS).start();
    }

    private ActivityResultLauncher<String[]> requestMultiplePermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permsGranted -> {
                if (permsGranted.containsValue(false)) {
                    //user denied one or more permissions
                    Log.i("DEBUG", "PERMISSIONS NOT GRANTED");
                }
            });

    private ActivityResultLauncher<Intent> becomeDefaultHandlerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), callback ->
    {

    });


    /**
     * Called upon instantiation.
     * Configures the user interface, and requests the required permissions
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sw = findViewById(R.id.switch1);
        rm = (RoleManager) getSystemService(ROLE_SERVICE);

        requestMultiplePermissionLauncher.launch(perms);

        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /**
             * Called when pressing the switch in Defingerprint's interface.
             * If the application does not hold the SMS role, and the SMS role is available,
             * requests the SMS role.
             * Once all required permissions are obtained and the SMS role is held,
             * calls generateSMS for 50 operations.
             */
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (checkAllPermissions(perms) && rm.isRoleHeld(ROLE_SMS))
                {
                    if (rm.isRoleHeld(ROLE_SMS))
                    {
                        generateSMS(50);
                    }
                }
                else
                {
                    sw.toggle();
                    if (rm.isRoleAvailable(ROLE_SMS))
                    {
                        Intent intent = rm.createRequestRoleIntent(ROLE_SMS);
                        becomeDefaultHandlerLauncher.launch(intent);
                    }
                }
            }
        });





    }
}