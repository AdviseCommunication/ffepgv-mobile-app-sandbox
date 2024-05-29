package fr.ffepgv.sandbox.plugins.audio;

import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;

import androidx.annotation.OptIn;
import androidx.media3.common.Player;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.util.UnstableApi;

public class PlayerEventListener implements Player.Listener {
    private static final String TAG = "PlayerEventListener";

    private AudioPlayerPlugin plugin;
    private AudioSource audioSource;

    @OptIn(markerClass = UnstableApi.class)
    public PlayerEventListener(AudioPlayerPlugin plugin, AudioSource audioSource) {
        this.plugin = plugin;
        this.audioSource = audioSource;
    }

    @Override
    public void onIsPlayingChanged(boolean isPlaying) {
        String status = "stopped";

        if (audioSource.isInitialized()) {
            if (
                    audioSource.getPlayer().getPlaybackState() == Player.STATE_READY
                            && !audioSource.getPlayer().getPlayWhenReady()
                            && !audioSource.isStopped()) {
                status = "paused";
                audioSource.setIsPlaying(false);
            } else if (isPlaying || audioSource.isPlaying()) {
                status = "playing";
                audioSource.setIsPlaying(true);
                audioSource.setIsStopped(false);
            }
        }

        makeCall(audioSource.onPlaybackStatusChangeCallbackId, new JSObject().put("status", status));
    }

    @Override
    public void onPlaybackStateChanged(int playbackState) {
        if (playbackState == Player.STATE_READY) {
            makeCall(audioSource.onReadyCallbackId);
        }

        if (playbackState == Player.STATE_ENDED) {
            audioSource.stopThroughService();

            makeCall(audioSource.onEndCallbackId);
        }
    }

    @Override
    public void onPlayerError(PlaybackException error) {
        Log.e(TAG, "Playback error: " + error.getMessage());
        // Handle additional error processing here
    }

    private void makeCall(String callbackId) {
        makeCall(callbackId, new JSObject());
    }

    private void makeCall(String callbackId, JSObject data) {
        if (callbackId == null) {
            return;
        }

        PluginCall call = plugin.getBridge().getSavedCall(callbackId);

        if (call == null) {
            return;
        }

        if (data.length() == 0) {
            call.resolve();
        } else {
            call.resolve(data);
        }
    }
}
