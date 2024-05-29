package fr.ffepgv.sandbox;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;
import fr.ffepgv.sandbox.plugins.audio.AudioPlayerPlugin;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(AudioPlayerPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
