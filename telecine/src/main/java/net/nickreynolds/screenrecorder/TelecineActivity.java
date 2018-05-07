package net.nickreynolds.screenrecorder;

import android.app.ActivityManager;
import android.app.ActivityManager.TaskDescription;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Spinner;
import android.widget.Switch;
import butterknife.BindColor;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import com.google.android.gms.analytics.HitBuilders;
import dagger.android.AndroidInjection;
import javax.inject.Inject;
import timber.log.Timber;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public final class TelecineActivity extends AppCompatActivity {
  @BindView(R.id.spinner_video_size_percentage) Spinner videoSizePercentageView;
  @BindView(R.id.switch_show_countdown) Switch showCountdownView;
  @BindView(R.id.switch_hide_from_recents) Switch hideFromRecentsView;
  @BindView(R.id.switch_recording_notification) Switch recordingNotificationView;
  @BindView(R.id.switch_stop_recording_on_power) Switch stopRecordingOnPowerView;
  @BindView(R.id.switch_show_touches) Switch showTouchesView;
  @BindView(R.id.container_use_demo_mode) View useDemoModeContainerView;
  @BindView(R.id.switch_use_demo_mode) Switch useDemoModeView;
  @BindView(R.id.launch) View launchView;

  @BindString(R.string.app_name) String appName;
  @BindColor(R.color.primary_normal) int primaryNormal;

  @Inject @VideoSizePercentage IntPreference videoSizePreference;
  @Inject @ShowCountdown BooleanPreference showCountdownPreference;
  @Inject @HideFromRecents BooleanPreference hideFromRecentsPreference;
  @Inject @RecordingNotification BooleanPreference recordingNotificationPreference;
  @Inject @StopOnPower BooleanPreference stopOnPowerPreference;
  @Inject @ShowTouches BooleanPreference showTouchesPreference;
  @Inject @UseDemoMode BooleanPreference useDemoModePreference;

  @Inject Analytics analytics;

  private VideoSizePercentageAdapter videoSizePercentageAdapter;
  private DemoModeHelper.ShowDemoModeSetting showDemoModeSetting;

  @Override protected void onCreate(Bundle savedInstanceState) {
    AndroidInjection.inject(this);
    super.onCreate(savedInstanceState);

    if ("true".equals(getIntent().getStringExtra("crash"))) {
      throw new RuntimeException("Crash! Bang! Pow! This is only a test...");
    }

    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);

    CheatSheet.setup(launchView);

    setTaskDescription(new TaskDescription(appName, rasterizeTaskIcon(), primaryNormal));

    videoSizePercentageAdapter = new VideoSizePercentageAdapter(this);

    videoSizePercentageView.setAdapter(videoSizePercentageAdapter);
    videoSizePercentageView.setSelection(
        VideoSizePercentageAdapter.getSelectedPosition(videoSizePreference.get()));

    showCountdownView.setChecked(showCountdownPreference.get());
    hideFromRecentsView.setChecked(hideFromRecentsPreference.get());
    recordingNotificationView.setChecked(recordingNotificationPreference.get());
    stopRecordingOnPowerView.setChecked(stopOnPowerPreference.get());
    showTouchesView.setChecked(showTouchesPreference.get());
    useDemoModeView.setChecked(useDemoModePreference.get());
    showDemoModeSetting = new DemoModeHelper.ShowDemoModeSetting() {
      @Override public void show() {
        useDemoModeContainerView.setVisibility(VISIBLE);
      }

      @Override public void hide() {
        useDemoModeView.setChecked(false);
        useDemoModeContainerView.setVisibility(GONE);
      }
    };
    DemoModeHelper.showDemoModeSetting(this, showDemoModeSetting);
  }

  @NonNull private Bitmap rasterizeTaskIcon() {
    Drawable drawable = getResources().getDrawable(R.drawable.ic_videocam_white_24dp, getTheme());

    ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
    int size = am.getLauncherLargeIconSize();
    Bitmap icon = Bitmap.createBitmap(size, size, ARGB_8888);

    Canvas canvas = new Canvas(icon);
    drawable.setBounds(0, 0, size, size);
    drawable.draw(canvas);

    return icon;
  }

  @OnClick(R.id.launch) void onLaunchClicked() {
    Timber.d("Attempting to acquire permission to screen capture.");
    CaptureHelper.fireScreenCaptureIntent(this, analytics);
  }

  @OnItemSelected(R.id.spinner_video_size_percentage) void onVideoSizePercentageSelected(
      int position) {
    int newValue = videoSizePercentageAdapter.getItem(position);
    int oldValue = videoSizePreference.get();
    if (newValue != oldValue) {
      Timber.d("Video size percentage changing to %s%%", newValue);
      videoSizePreference.set(newValue);

      analytics.send(new HitBuilders.EventBuilder() //
          .setCategory(Analytics.CATEGORY_SETTINGS)
          .setAction(Analytics.ACTION_CHANGE_VIDEO_SIZE)
          .setValue(newValue)
          .build());
    }
  }

  @OnCheckedChanged(R.id.switch_show_countdown) void onShowCountdownChanged() {
    updateBooleanPreference(showCountdownView, showCountdownPreference, Analytics.ACTION_CHANGE_SHOW_COUNTDOWN);
  }

  @OnCheckedChanged(R.id.switch_hide_from_recents) void onHideFromRecentsChanged() {
    updateBooleanPreference(hideFromRecentsView, hideFromRecentsPreference, Analytics.ACTION_CHANGE_HIDE_RECENTS);
  }

  @OnCheckedChanged(R.id.switch_recording_notification) void onRecordingNotificationChanged() {
    updateBooleanPreference(recordingNotificationView, recordingNotificationPreference, Analytics.ACTION_CHANGE_RECORDING_NOTIFICATION);
  }

  @OnCheckedChanged(R.id.switch_stop_recording_on_power) void onStopOnPowerChanged() {
    updateBooleanPreference(stopRecordingOnPowerView, stopOnPowerPreference, Analytics.ACTION_CHANGE_STOP_RECORDING_ON_POWER);
  }

  @OnCheckedChanged(R.id.switch_show_touches) void onShowTouchesChanged() {
    updateBooleanPreference(showTouchesView, showTouchesPreference, Analytics.ACTION_CHANGE_SHOW_TOUCHES);
  }

  @OnCheckedChanged(R.id.switch_use_demo_mode) void onUseDemoModeChanged() {
    updateBooleanPreference(useDemoModeView, useDemoModePreference, Analytics.ACTION_CHANGE_USE_DEMO_MODE);
  }

  private void updateBooleanPreference(Switch switchView, BooleanPreference preference, String analyticsAction) {
    boolean newValue = switchView.isChecked();
    boolean oldValue = preference.get();
    if (newValue != oldValue) {
      Timber.d("%s preference changing to %s", analyticsAction, newValue);
      preference.set(newValue);

      analytics.send(new HitBuilders.EventBuilder() //
              .setCategory(Analytics.CATEGORY_SETTINGS)
              .setAction(analyticsAction)
              .setValue(newValue ? 1 : 0)
              .build());
    }
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (!CaptureHelper.handleActivityResult(this, requestCode, resultCode, data, analytics)
        && !DemoModeHelper.handleActivityResult(this, requestCode, showDemoModeSetting)) {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }

  @Override protected void onStop() {
    super.onStop();
    if (hideFromRecentsPreference.get() && !isChangingConfigurations()) {
      Timber.d("Removing task because hide from recents preference was enabled.");
      finishAndRemoveTask();
    }
  }
}
