package org.devcloud.accent;

import java.io.File;
import java.io.IOException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class AudioRecordingActivity extends Activity {
  private static final String AUDIO_RECORDER_FILE_EXT_3GP = "3gp";
  private static final String AUDIO_RECORDER_FILE_EXT_MP4 = "mp4";
  private static final String AUDIO_RECORDER_FOLDER = "AccentWatch";

  private MediaRecorder recorder = null;
  private int currentFormat = 0;
  private int output_formats[] = {
    MediaRecorder.OutputFormat.MPEG_4,
    MediaRecorder.OutputFormat.THREE_GPP
  };
  private String file_exts[] = {
    AUDIO_RECORDER_FILE_EXT_MP4,
    AUDIO_RECORDER_FILE_EXT_3GP
  };

  private NotificationCompat.Builder mBuilder =
      new NotificationCompat.Builder(this)
          .setSmallIcon(R.drawable.ic_launcher)
          .setPriority(Notification.PRIORITY_MIN)
          .setContentTitle("Accent Watch")
          .setContentText("Record something, it's been a while!");

  private Handler handler = new Handler();
  private Runnable runnable = new Runnable() {
    public void run() {
      Log.d("Runnable", "Running the runnable.");
      if (recordingNeeded()) {
        // Sets an ID for the notification
        int mNotificationId = (int) System.currentTimeMillis();
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.cancelAll();
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
      }

      // Check again in a day.
      handler.postDelayed(this, 86400);
    }
  };

  private boolean recordingNeeded() {
    return true;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // After 100 seconds, make sure a recording has been made.
    handler.postDelayed(runnable, 100);
    Log.i("RecordingActivity", handler.toString());

    // Create the layout.
    setContentView(R.layout.main);
    setButtonHandlers();
    enableButtons(false);
  }

  private void setButtonHandlers() {
    ((Button) findViewById(R.id.btnStart)).setOnClickListener(btnClick);
    ((Button) findViewById(R.id.btnStop)).setOnClickListener(btnClick);
    ((Button) findViewById(R.id.btnList)).setOnClickListener(btnClick);
  }

  private void enableButton(int id, boolean isEnable) {
    ((Button) findViewById(id)).setEnabled(isEnable);
  }

  private void enableButtons(boolean isRecording) {
    enableButton(R.id.btnStart, !isRecording);
    enableButton(R.id.btnStop, isRecording);
  }

  static File getDirectory() {
    File path = Environment.getExternalStorageDirectory();
    File folder = new File(path, AUDIO_RECORDER_FOLDER);
    folder.mkdirs();
    return folder;
  }

  private File getFile() {
    String filename = String.format("%s.%s", System.currentTimeMillis(), file_exts[currentFormat]);
    return new File(getDirectory(), filename);
  }

  private void startRecording() {
    File file = getFile();
    recorder = new MediaRecorder();

    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
    recorder.setOutputFormat(output_formats[currentFormat]);
    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
    recorder.setOutputFile(file.getAbsolutePath());

    recorder.setOnErrorListener(errorListener);
    recorder.setOnInfoListener(infoListener);

    try {
      recorder.prepare();
      recorder.start();
      Log.d("AudioRecording", "Started recording to: " + file.getAbsolutePath());
    } catch (IllegalStateException e) {
      e.printStackTrace();
    } catch (IOException e) {
      // Unable to create file, likely because external storage is
      // not currently mounted.
      Log.w("ExternalStorage", "Error writing " + file, e);
    }
  }

  private void stopRecording() {
    if (null != recorder) {
      recorder.stop();
      recorder.reset();
      recorder.release();

      recorder = null;
    }
  }

  private MediaRecorder.OnErrorListener errorListener = new MediaRecorder.OnErrorListener() {
    @Override
    public void onError(MediaRecorder mr, int what, int extra) {
      Toast.makeText(AudioRecordingActivity.this, "Error: " + what + ", " + extra, Toast.LENGTH_SHORT).show();
    }
  };

  private MediaRecorder.OnInfoListener infoListener = new MediaRecorder.OnInfoListener() {
    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
      Toast.makeText(AudioRecordingActivity.this, "Warning: " + what + ", " + extra, Toast.LENGTH_SHORT).show();
    }
  };

  private View.OnClickListener btnClick = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      switch (v.getId()) {
        case R.id.btnStart:
          Toast.makeText(
              AudioRecordingActivity.this,
              "Start Recording",
              Toast.LENGTH_SHORT).show();
          enableButtons(true);
          startRecording();
          break;
        case R.id.btnStop:
          Toast.makeText(
              AudioRecordingActivity.this,
              "Stop Recording",
              Toast.LENGTH_SHORT).show();
          enableButtons(false);
          stopRecording();
          break;
        case R.id.btnList:
          // Log.i("AudioRecording", "Got List Button Press.");
          Intent i = new Intent(getApplicationContext(), RecordingListActivity.class);
          startActivity(i);
          break;
      }
    }
  };
}
