<template>
  <ion-page>
    <ion-header :translucent="true">
      <ion-toolbar>
        <ion-title>Test player</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content :fullscreen="true">
      <ion-header collapse="condense">
        <ion-toolbar>
          <ion-title size="large">Test player</ion-title>
        </ion-toolbar>
      </ion-header>

      <div id="container">
        <p v-if="error">{{ error }}</p>

        <div style="display: flex; gap: 1rem; margin: 4rem; justify-content: center; align-items: center">
          <button @click="togglePlay()" style="padding: 2rem; background: #00BCD4; color: #fff">
            <span v-if="!playing">Play</span>
            <span v-else>Stop</span>
          </button>
          <button @click="togglePause()" style="padding: 2rem; background: #d4a300; color: #fff" v-if="playing">
            Pause
          </button>
        </div>

        <div style="display: flex; flex-direction: column; gap: 1rem; justify-content: center; align-items: center">
          <div>Statut : {{ statut }}</div>
          <div>Durée : {{ duration }}</div>
          <div>Progression : {{ progress }}</div>
          <div>
            <input type="range" :value="progress" :max="duration" step="1" @change="seek">
          </div>
          <div>
            Vitesse de lecture :
            <div style="display: flex; gap: 0.5rem">
              <a href="" v-for="currentSpeed in availableSpeeds" @click.prevent="setSpeed(currentSpeed)" :style="playingSpeed === currentSpeed ? 'color: red; font-weight: bold': ''">[{{ currentSpeed}}]</a>
            </div>
          </div>
        </div>
      </div>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { IonContent, IonHeader, IonPage, IonTitle, IonToolbar } from '@ionic/vue';
import { AudioPlayer } from "@/services/audio-player";
import { ref } from "vue";

let availableSpeeds = [0.25, 0.5, 1, 1.5, 2];
let error = ref("");
let playing = ref(false);
let paused = ref(false);
let progress = ref(0);
let duration = ref(0);
let statut = ref('stopped');
let playingSpeed = ref(1);
let progressTimeout: any = null;
let durationTimeout: any = null;
const player = AudioPlayer.create({
  audioId: 'sample',
  audioSource: 'https://advise.fr/sample.mp3',
  friendlyTitle: "Test",
  useForNotification: true,
  isBackgroundMusic: false,
  loop: false
}).catch((reason: any) => {
  error.value = reason.toString();
});


const updateDuration = () => {
  AudioPlayer.getDuration({
    audioId: 'sample'
  }).then((result) => {
    if (result.duration <= 0) {
      durationTimeout = setTimeout(() => {
        updateDuration();
      }, 1000);
      duration.value = progress.value;
      return ;
    }
    duration.value = result.duration;
  }).catch((reason: any) => {
    error.value = reason.toString();
  });
}

const updateProgress = () => {
  if (!playing.value) {
    return;
  }
  if (progressTimeout) {
    clearTimeout(progressTimeout);
  }
  AudioPlayer.getCurrentTime({
    audioId: 'sample'
  }).then((result) => {
    progress.value = Math.ceil(result.currentTime);
    progressTimeout = setTimeout(() => {
      updateProgress();
    }, 1000);
  }).catch((reason: any) => {
    error.value = reason.toString();
  });
}

const play = () => {
  if (playing.value) {
    return ;
  }
  AudioPlayer.initialize({
    audioId: 'sample'
  }).then(() => {
    AudioPlayer.onPlaybackStatusChange({
      audioId: 'sample'
    }, (result) => {
      console.log('onPlaybackStatusChange', result.status)
      statut.value = result.status;
      playing.value = statut.value === 'playing';

      if (playing.value) {
          updateDuration();
          updateProgress();
      }
      if (progress.value > 0) {
        setProgress(progress.value);
      }
    }).catch((reason: any) => {
      error.value = reason.toString();
    });


    AudioPlayer.play({
      audioId: 'sample'
    }).catch((reason: any) => {
      error.value = reason.toString();
    });

  }).catch((reason: any) => {
    error.value = reason.toString();
  });
}

const stop = () => {
  AudioPlayer.stop({
    audioId: 'sample'
  }).catch((reason: any) => {
    error.value = reason.toString();
  });
}

const pause = () => {
  AudioPlayer.pause({
    audioId: 'sample'
  }).catch((reason: any) => {
    error.value = reason.toString();
  });
}

const seek = (event: any) => {
  setProgress(parseInt(event.target.value, 10));
}

const togglePause = () => {
  if (statut.value === 'paused') {
    play();
  } else {
    pause();
  }
}

const togglePlay = () => {
  if (playing.value) {
    stop();
  } else {
    play();
  }
}

const setSpeed = (speed: number) => {
  AudioPlayer.setRate({
    audioId: 'sample',
    rate: speed
  }).then(() => {
    playingSpeed.value = speed;
  }).catch((reason: any) => {
    error.value = reason.toString();
  });
}

const setProgress = (progress: number) => {
  AudioPlayer.seek({
    audioId: 'sample',
    timeInSeconds: progress
  }).catch((reason: any) => {
    error.value = reason.toString();
  });
}
</script>

<style scoped>
#container {
  text-align: center;

  position: absolute;
  left: 0;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
}

#container strong {
  font-size: 20px;
  line-height: 26px;
}

#container p {
  font-size: 16px;
  line-height: 22px;

  color: #8c8c8c;

  margin: 0;
}

#container a {
  text-decoration: none;
}
</style>
