package fr.ffepgv.sandbox.plugins.audio;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import java.util.HashMap;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.ui.PlayerNotificationManager;

import fr.ffepgv.sandbox.MainActivity;
import fr.ffepgv.sandbox.R;

@UnstableApi public class AudioPlayerService extends Service {
    private static final String TAG = "AudioPlayerService";
    public static final String PLAYBACK_CHANNEL_ID = "playback_channel";
    public static final int PLAYBACK_NOTIFICATION_ID = 1;

    private final IBinder serviceBinder = new AudioPlayerServiceBinder();
    private static boolean isRunning = false;

    private PlayerNotificationManager playerNotificationManager;
    private AudioSource notificationAudioSource;
    private HashMap<String, AudioSource> audioSources = new HashMap<>();

    public boolean createAudioSource(AudioSource audioSource) {
        if (audioSources.containsKey(audioSource.id)) {
            Log.w(TAG, String.format("There is already an audio source with ID %s", audioSource.id));

            return false;
        }

        Log.i(TAG, String.format("Initializing audio source ID %s (%s)", audioSource.id, audioSource.source));

        if (audioSource.useForNotification) {
            notificationAudioSource = audioSource;
        }

        audioSource.setServiceOwner(this);
        audioSources.put(audioSource.id, audioSource);

        return true;
    }

    public void initializeAudioSource(String audioSourceId) {
        getAudioSource(audioSourceId).initialize(this);
    }

    public boolean audioSourceExists(String audioSourceId) {
        return audioSources.containsKey(audioSourceId);
    }

    public void changeAudioSource(String audioSourceId, String newSource) {
        Log.i(TAG, String.format("Changing audio source for ID %s to %s", audioSourceId, newSource));

        getAudioSource(audioSourceId).changeAudioSource(newSource);
    }

    public float getDuration(String audioSourceId) {
        Log.i(TAG, String.format("Getting duration for audio source ID %s", audioSourceId));

        return getAudioSource(audioSourceId).getDuration();
    }

    public float getCurrentTime(String audioSourceId) {
        Log.i(TAG, String.format("Getting current time for audio source ID %s", audioSourceId));

        return getAudioSource(audioSourceId).getCurrentTime();
    }

    @OptIn(markerClass = UnstableApi.class) public void play(String audioSourceId) {
        Log.i(TAG, String.format("Playing audio source ID %s", audioSourceId));

        if (notificationAudioSource.id.equals(audioSourceId)) {
            Log.i(TAG, String.format("Setting notification player to audio source ID %s", audioSourceId));
            playerNotificationManager.setPlayer(getAudioSource(audioSourceId).getPlayer());
        }

        getAudioSource(audioSourceId).play();
    }

    public void pause(String audioSourceId) {
        Log.i(TAG, String.format("Pausing audio source ID %s", audioSourceId));
        getAudioSource(audioSourceId).pause();
    }

    public void seek(String audioSourceId, long timeInSeconds) {
        Log.i(TAG, String.format("Seeking audio source ID %s", audioSourceId));
        getAudioSource(audioSourceId).seek(timeInSeconds);
    }

    public void stop(String audioSourceId) {
        Log.i(TAG, String.format("Stopping audio source ID %s", audioSourceId));

        if (notificationAudioSource.id.equals(audioSourceId)) {
            Log.i(TAG, String.format("Clearing notification for audio source ID %s", audioSourceId));
            clearNotification();
            stopService();
        }

        getAudioSource(audioSourceId).stop();
    }

    public void setVolume(String audioSourceId, float volume) {
        Log.i(TAG, String.format("Setting volume for audio source ID %s", audioSourceId));
        getAudioSource(audioSourceId).setVolume(volume);
    }

    public void setRate(String audioSourceId, float rate) {
        Log.i(TAG, String.format("Setting rate for audio source ID %s", audioSourceId));
        getAudioSource(audioSourceId).setRate(rate);
    }

    public boolean isPlaying(String audioSourceId) {
        return getAudioSource(audioSourceId).isPlaying();
    }

    public void destroyAudioSource(String audioSourceId) throws DestroyNotAllowedException {
        Log.i(TAG, String.format("Destroying audio source ID %s", audioSourceId));

        if (notificationAudioSource != null) {
            if (notificationAudioSource.id.equals(audioSourceId)) {
                if (audioSources.size() > 1) {
                    throw new DestroyNotAllowedException(String.format("Audio source ID %s is the current notification and cannot be destroyed. Destroy other audio sources first.", audioSourceId));
                } else {
                    Log.i(TAG, String.format("Clearing notification while destroying audio source ID %s", audioSourceId));
                    clearNotification();
                }
            }
        }

        AudioSource audioSource = getAudioSource(audioSourceId);
        audioSource.releasePlayer();

        audioSources.remove(audioSourceId);

        if (audioSources.isEmpty()) {
            Log.i(TAG, String.format("Stopping service, audio source ID %s was the last source to be destroyed", audioSourceId));
            stopService();
        }
    }

    public void setOnAudioReady(String audioSourceId, String callbackId) {
        getAudioSource(audioSourceId).setOnReady(callbackId);
    }

    public void setOnAudioEnd(String audioSourceId, String callbackId) {
        getAudioSource(audioSourceId).setOnEnd(callbackId);
    }

    public void setOnPlaybackStatusChange(String audioSourceId, String callbackId) {
        getAudioSource(audioSourceId).setOnPlaybackStatusChange(callbackId);
    }

    public boolean isRunning() {
        return isRunning;
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, AudioPlayerService.class);
    }

    public class AudioPlayerServiceBinder extends Binder {
        public AudioPlayerService getService() {
            return AudioPlayerService.this;
        }
    }

    @Override
    @OptIn(markerClass = UnstableApi.class)
    public void onCreate() {
        Log.i(TAG, "Service being created");

        Context appContext = getApplicationContext();

        playerNotificationManager = new PlayerNotificationManager.Builder(
                        appContext,
                        PLAYBACK_NOTIFICATION_ID,
                        PLAYBACK_CHANNEL_ID)
                .setMediaDescriptionAdapter(new PlayerNotificationManager.MediaDescriptionAdapter() {
                    public CharSequence getCurrentContentTitle(Player player) {
                        // This method must provide the title of the currently playing content
                        // For example, assuming your AudioSource or player has a method to get the title
                        return player.getCurrentMediaItem() != null ? player.getCurrentMediaItem().mediaMetadata.title : "Unknown Title";
                    }

                    public PendingIntent createCurrentContentIntent(Player player) {
                        // This method should return a PendingIntent that is fired when the user clicks on the notification
                        return PendingIntent.getActivity(
                                appContext,
                                0,
                                new Intent(appContext, MainActivity.class),
                                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                        );
                    }

                    public CharSequence getCurrentContentText(Player player) {
                        // This method should provide additional text such as the artist or album info
                        return player.getCurrentMediaItem() != null ? player.getCurrentMediaItem().mediaMetadata.artist : "Unknown Artist";
                    }

                    public CharSequence getCurrentSubText(Player player) {
                        // This method can provide supplementary information, if applicable
                        return player.getCurrentMediaItem() != null ? player.getCurrentMediaItem().mediaMetadata.albumTitle : null;
                    }

                    @Nullable
                    @Override
                    public Bitmap getCurrentLargeIcon(Player player, PlayerNotificationManager.BitmapCallback callback) {
                        return null;
                    }
                })

                .setNotificationListener(
                        new PlayerNotificationManager.NotificationListener() {
                            @Override
                            public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
                                Log.i(TAG, "Notification cancelled, stopping service");
                                stopService();
                            }

                            @Override
                            public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
                                if (ongoing) {
                                    // Make sure the service will not get destroyed while playing media
                                    Log.i(TAG, "Notification posted, starting foreground");
                                    startForeground(notificationId, notification);
                                } else {
                                    // Make notification cancellable
                                    Log.i(TAG, "Notification posted, stopping foreground");
                                    stopForeground(false);
                                }
                            }
                        }
                )
                .build();

        playerNotificationManager.setUseNextAction(false);
        playerNotificationManager.setUsePreviousAction(false);
        playerNotificationManager.setUsePlayPauseActions(true);
        playerNotificationManager.setSmallIcon(R.drawable.ic_stat_icon_default_foreground);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Service starting");
        isRunning = true;

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Service being destroyed");
        clearNotification();
        playerNotificationManager = null;

        for (AudioSource audioSource : audioSources.values()) {
            audioSource.releasePlayer();
        }

        audioSources.clear();
        isRunning = false;

        super.onDestroy();
    }

    private AudioSource getAudioSource(String id) {
        AudioSource source = audioSources.get(id);

        if (source == null) {
            Log.w(TAG, String.format("Audio source with ID %s was not found.", id));
        }

        return source;
    }

    @OptIn(markerClass = UnstableApi.class) private void clearNotification() {
        if (playerNotificationManager != null) {
            Log.i(TAG, "Notification: Setting player to null.");
            playerNotificationManager.setPlayer(null);
        }
    }

    private void stopService() {
        stopForeground(true);
        stopSelf();
        isRunning = false;
    }
}
