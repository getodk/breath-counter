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
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class BreathCounterActivity extends Activity {

	private static final double TIMER_MAX = 60.0;
	
	// MUST BE LESS THAN TIMER_MAX
	private static final double DESIRED_TIMER_END = 60.0;
	
	private static final double ANSWER_MULTIPLY = TIMER_MAX/DESIRED_TIMER_END;
	
	
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
	
	private final int paddingInt = 0;
	private final int textPaddingInt =0;
	private final int reportButtonInt = 3;

	private static class Counter extends Handler {
		private boolean stop = true;
		private BreathCounterActivity parent;
		
		public Counter(BreathCounterActivity parent) {
			this.parent = parent;
		}
		
		
		@Override
		public void handleMessage(Message msg) {
			parent.updateSeconds();
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
			parent.updateSeconds();
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

		LinearLayout.LayoutParams halfParams = new LinearLayout.LayoutParams(
			    LayoutParams.FILL_PARENT, 0);
		halfParams.weight = .6f;
		halfParams.setMargins(paddingInt, paddingInt, paddingInt, paddingInt);
		
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
		
		mHandler = new Counter(this);
		
		mBreathButton.setLayoutParams(halfParams);
		
		mBreathButton.setPadding(paddingInt, paddingInt, paddingInt, textPaddingInt);
		
		
		mBreathButton.setText("Press Here For Every Breath");
		if (mAnswer != null) {
			disableBreathButton();
		}
		final Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		mBreathButton.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				// Gray out animation
				AlphaAnimation alphaDown = new AlphaAnimation(1.0f, 0.3f);
				AlphaAnimation alphaUp = new AlphaAnimation(0.3f, 1.0f);
			    alphaDown.setDuration(1000);
			    alphaUp.setDuration(500);
			    alphaDown.setFillAfter(true);
			    alphaUp.setFillAfter(true);
			    mBreathButton.startAnimation(alphaUp);
				v.vibrate(125);
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

		
		mResetButton.setText("Press Here to Start Over");
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
		
		mRecordAnswerButton.setText("Press Here to Record Results");
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
		mLinearLayout.addView(mRecordAnswerButton);
		mLinearLayout.addView(mBlankLine);
		mLinearLayout.addView(mResetButton);


		registerForContextMenu(mBreathCountView);
		registerForContextMenu(mTimeView);
		registerForContextMenu(mBreathButton);
		registerForContextMenu(mBreathsPerMinView);
		registerForContextMenu(mRecordAnswerButton);
		registerForContextMenu(mBlankLine);
		registerForContextMenu(mResetButton);


		clearAnswer();
	}

	@Override
	protected void onPause() {
		super.onPause();
		clearAnswer();
	}
	
	private void enableBreathButton() {
		mBreathButton.setBackgroundColor(Color.rgb(153,255,153));
		mBreathButton.setTextColor(Color.BLACK);
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
			if (mSeconds <= DESIRED_TIMER_END) {
				mSeconds += .1;
				setSeconds(mSeconds);
				mHandler.count(100);
			}
			if (mSeconds > (DESIRED_TIMER_END - 0.1)) {
				mHandler.stop();
				enableReturnValueButton();
				MediaPlayer mediaPlayer = MediaPlayer.create(
						BreathCounterActivity.this, R.raw.beep);
				mediaPlayer.start();
				final Vibrator v = (Vibrator) BreathCounterActivity.this
						.getSystemService(Context.VIBRATOR_SERVICE);
				v.vibrate(1000);

				mAnswer = Integer.valueOf(mBreaths);
				// sometimes stops at 59.9 or 60.1
				setSeconds(DESIRED_TIMER_END);
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
			mAnswer = (int) (count * ANSWER_MULTIPLY);
			mBreathsPerMinView.setText("Breaths per Minute: " + mAnswer);
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
