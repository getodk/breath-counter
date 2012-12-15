package change.uw.breathcounter;

import java.text.DecimalFormat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BreathCounterActivity extends Activity {

	private TextView mBreathCountView;
	private TextView mTimeView;
	private TextView mBreathsPerMinView;
	private TextView mBlankLine;

	private Double mSeconds;
	private int mBreaths;

	private final int mAnswerFontsize = 23;

	private Integer mAnswer;

	private Counter mHandler;

	private Button mBreathButton;

	private Button mResetButton;
	
	private Button mRecordAnswerButton;

	private LinearLayout mLinearLayout;

	private class Counter extends Handler {
		boolean stop = true;

		@Override
		public void handleMessage(Message msg) {
			updateSeconds();
		}

		public void count(long delayMillis) {
			if (!stop) {
				this.removeMessages(0);
				sendMessageDelayed(obtainMessage(0), delayMillis);
			}
		}

		public void stop() {
			stop = true;
		}

		public void start() {
			stop = false;
			updateSeconds();
		}

		public boolean running() {
			return !stop;
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mLinearLayout = (LinearLayout) findViewById(R.id.mainlayout);

		mSeconds = 0.0;
		mBreaths = 0;

		mBreathCountView = new TextView(this);
		mTimeView = new TextView(this);
		mBreathsPerMinView = new TextView(this);
		mBlankLine = new TextView(this);

		mBreathCountView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,
				mAnswerFontsize);
		mTimeView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
		mBreathsPerMinView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,
				mAnswerFontsize);
		mBreathsPerMinView.setPadding(10, 70, 10, 70);

		mBreathButton = new Button(this);
		mResetButton = new Button(this);
		mRecordAnswerButton = new Button(this);
		
		mHandler = new Counter();
		
		mBreathButton.setText("Press per Breath");
		if (mAnswer != null) {
			disableBreathButton();
		}
		final Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		mBreathButton.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				v.vibrate(75);
				mBreaths++;
				setBreaths(mBreaths);
				if (mAnswer == null && !mHandler.running()) {
					mHandler.start();
					enableResetButton();
				}
			}

		});
		mBreathButton.setPadding(10, 100, 10, 100);
		mBreathButton.setTextSize(28);

		
		mResetButton.setText("Reset Counter");
		if (!mHandler.running()) {
			disableResetButton();
		}
		mResetButton.setPadding(0, 50, 0, 50);
		mResetButton.setTextSize(21);
		mResetButton.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				clearAnswer();
			}

		});
		
		mRecordAnswerButton.setText("Record Count");
		if (mAnswer == null || mAnswer == -1) {
			disableReturnValueButton();
		}
		mRecordAnswerButton.setPadding(0, 50, 0, 50);
		mRecordAnswerButton.setTextSize(21);
		mRecordAnswerButton.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				sendAnswerBackToApp();
			}

		});

		mLinearLayout.addView(mBreathCountView);
		mLinearLayout.addView(mTimeView);
		mLinearLayout.addView(mBreathButton);
		mLinearLayout.addView(mBreathsPerMinView);
		mLinearLayout.addView(mResetButton);
		mLinearLayout.addView(mBlankLine);
		mLinearLayout.addView(mRecordAnswerButton);

		registerForContextMenu(mBreathCountView);
		registerForContextMenu(mTimeView);
		registerForContextMenu(mBreathButton);
		registerForContextMenu(mBreathsPerMinView);
		registerForContextMenu(mResetButton);
		registerForContextMenu(mBlankLine);
		registerForContextMenu(mRecordAnswerButton);

		clearAnswer();
	}

	private void enableBreathButton() {
		mBreathButton.setTextColor(Color.rgb(0x08,0x8A,0x29));
		mBreathButton.setEnabled(true);
	}

	private void disableBreathButton() {
		mBreathButton.setTextColor(Color.DKGRAY);
		mBreathButton.setEnabled(false);
	}

	private void enableResetButton() {
		mResetButton.setBackgroundColor(Color.rgb(0xA9, 0xE2, 0xF3));
		mResetButton.setEnabled(true);
	}
	
	private void disableResetButton() {
		mResetButton.setBackgroundColor(Color.DKGRAY);
		mResetButton.setEnabled(false);
	}

	private void enableReturnValueButton() {
		mRecordAnswerButton.setBackgroundColor(Color.rgb(0xF5, 0xF6, 0xCE));
		mRecordAnswerButton.setEnabled(true);
	}

	private void disableReturnValueButton() {
		mRecordAnswerButton.setBackgroundColor(Color.DKGRAY);
		mRecordAnswerButton.setEnabled(false);
	}
	
	private void updateSeconds() {
		if (mHandler.running()) {
			if (mSeconds <= 60.0) {
				mSeconds += .1;
				setSeconds(mSeconds);
				mHandler.count(100);
			}
			if (mSeconds > 59.9) {
				mHandler.stop();
				enableReturnValueButton();
				MediaPlayer mediaPlayer = MediaPlayer.create(
						BreathCounterActivity.this, R.raw.beep);
				mediaPlayer.start();
				final Vibrator v = (Vibrator) BreathCounterActivity.this
						.getSystemService(Context.VIBRATOR_SERVICE);
				v.vibrate(500);

				mAnswer = Integer.valueOf(mBreaths);
				// sometimes stops at 59.9 or 60.1
				setSeconds(60.0);
				setAnswer(mAnswer);
				while (mediaPlayer.isPlaying()) {
				}
				mediaPlayer.release();
				mediaPlayer = null;
			}
		}
	}

	private void setSeconds(double count) {
		mSeconds = count;
		DecimalFormat df = new DecimalFormat("0.0");
		mTimeView.setText("Seconds: " + df.format(mSeconds));
	}

	private void setBreaths(int count) {
		mBreaths = count;
		mBreathCountView.setText("Breaths: " + mBreaths);
	}

	private void setAnswer(int count) {
		if (count == -1) {
			mBreathsPerMinView.setText("Breaths per Minute: ");
			mAnswer = null;
			enableBreathButton();
		} else {
			mBreathsPerMinView.setText("Breaths per Minute: " + count);
			mAnswer = count;
			disableBreathButton();
			enableReturnValueButton();
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, v.getId(), 0, "Reset Count");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		createClearDialog();
		return super.onContextItemSelected(item);
	}

	private void createClearDialog() {
		AlertDialog mAlertDialog = new AlertDialog.Builder(this).create();
		mAlertDialog.setIcon(android.R.drawable.ic_dialog_alert);

		mAlertDialog.setTitle("Reset count?");
		mAlertDialog.setMessage("Are you sure you want to reset the count?");

		DialogInterface.OnClickListener quitListener = new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int i) {
				switch (i) {
				case DialogInterface.BUTTON1: // yes
					clearAnswer();
					break;
				case DialogInterface.BUTTON2: // no
					break;
				}
			}
		};
		mAlertDialog.setCancelable(false);
		mAlertDialog.setButton("Reset", quitListener);
		mAlertDialog.setButton2("Do NOT Reset", quitListener);
		mAlertDialog.show();
	}

	private void clearAnswer() {
		mHandler.stop();
		disableResetButton();
		disableReturnValueButton();
		setSeconds(0.0);
		setBreaths(0);
		setAnswer(-1);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			sendAnswerBackToApp();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	private void sendAnswerBackToApp() {
		Intent intent = new Intent();
		intent.putExtra("value", mAnswer);
		setResult(RESULT_OK, intent);
		finish();
	}

}
