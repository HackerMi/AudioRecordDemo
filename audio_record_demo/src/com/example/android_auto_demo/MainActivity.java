package com.example.android_auto_demo;


import java.io.FileOutputStream;
import java.io.IOException;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity {

    private AudioRecordThread mAudioRecordThread;
    private static String TAG = "DisplaySourceService";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_start) {
			createAudioRecord();
			return true;
		}
		if (id == R.id.action_stop) {
			releaseAudioRecord();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

    private void createAudioRecord() {
        releaseAudioRecord();

        mAudioRecordThread = new AudioRecordThread();
        mAudioRecordThread.start();
    }

    private void releaseAudioRecord() {
        if (mAudioRecordThread != null) {
            mAudioRecordThread.quit();
            mAudioRecordThread = null;
        }
    }

    private final class AudioRecordThread extends Thread {
        private final int kSampleRate = 48000;
        private final int kChannelMode = AudioFormat.CHANNEL_IN_STEREO;  
        private final int kEncodeFormat = AudioFormat.ENCODING_PCM_16BIT;  
        private final int kFrameSize = 2048;  
        private String filePath = "/sdcard/voice.pcm";
        private boolean isRecording = false;
        @Override
        public void run() {
        	// 根据/system/etc/audio_policy.conf配置AudioRecord
            int minBufferSize = AudioRecord.getMinBufferSize(kSampleRate, kChannelMode,  
                    kEncodeFormat);  
            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.REMOTE_SUBMIX,  
                    kSampleRate, kChannelMode, kEncodeFormat, minBufferSize * 2); 
            
            isRecording = true;           

            FileOutputStream os = null;  
            recorder.startRecording();  
            try {  
                os = new FileOutputStream(filePath);  
                byte[] buffer = new byte[kFrameSize];  
                int num = 0;  
                while (isRecording) {  
                    num = recorder.read(buffer, 0, kFrameSize);  
                    Log.d(TAG, "buffer = " + buffer.toString() + ", num = " + num);  
                    os.write(buffer, 0, num);  
                }  
      
                Log.d(TAG, "exit loop");  
                os.close();  
            } catch (IOException e) {  
                e.printStackTrace();  
                Log.e(TAG, "Dump PCM to file failed");  
            }  
            recorder.stop();  
            recorder.release();  
            recorder = null;  
            Log.d(TAG, "clean up");  
        }
        
        public void quit() {
            isRecording = false;
        }
    }
}
