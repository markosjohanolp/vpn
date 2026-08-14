package com.markos.vpn;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.LinearLayout;

public class MainActivity extends Activity {
    private Button btnConnect;
    private TextView txtStatus;
    private boolean isVpnRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(android.view.Gravity.CENTER);
        layout.setBackgroundColor(0xFF0D1117);

        TextView title = new TextView(this);
        title.setText("VPN Secure");
        title.setTextSize(28);
        title.setTextColor(0xFFFFFFFF);
        layout.addView(title);

        btnConnect = new Button(this);
        btnConnect.setText("تشغيل VPN");
        btnConnect.setTextSize(22);
        btnConnect.setBackgroundColor(0xFF1E88E5);
        layout.addView(btnConnect);

        txtStatus = new TextView(this);
        txtStatus.setText("غير متصل");
        txtStatus.setTextSize(18);
        txtStatus.setTextColor(0xFFFF0000);
        layout.addView(txtStatus);

        setContentView(layout);

        btnConnect.setOnClickListener(v -> {
            if (!isVpnRunning) {
                isVpnRunning = true;
                btnConnect.setText("إيقاف VPN");
                txtStatus.setText("متصل ✓");
                txtStatus.setTextColor(0xFF00FF00);
            } else {
                isVpnRunning = false;
                btnConnect.setText("تشغيل VPN");
                txtStatus.setText("غير متصل");
                txtStatus.setTextColor(0xFFFF0000);
            }
        });
    }
}
