<template>
  <ion-page>
    <ion-header :translucent="true">
      <ion-toolbar>
        <ion-title>Blank</ion-title>
      </ion-toolbar>
    </ion-header>

    <ion-content :fullscreen="true">
      <ion-header collapse="condense">
        <ion-toolbar>
          <ion-title size="large">Blank</ion-title>
        </ion-toolbar>
      </ion-header>

      <div id="container">
        <strong>Ready to create an app 2?</strong>
        <p>Start with Ionic <a target="_blank" rel="noopener noreferrer" href="https://ionicframework.com/docs/components">UI Components</a></p>
      </div>

      {{ error }}
      <button @click="play()" style="padding: 2rem; margin: 4rem; background: #00BCD4; color: #fff">
        <span v-if="!playing">Play</span>
        <span v-else>Stop</span>
      </button>
    </ion-content>
  </ion-page>
</template>

<script setup lang="ts">
import { IonContent, IonHeader, IonPage, IonTitle, IonToolbar } from '@ionic/vue';
import { AudioPlayer } from "@/services/audio-player";
import { ref } from "vue";

let error = ref("");
let playing = ref(false);
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


const play = () => {
  playing.value = true;
  AudioPlayer.initialize({
    audioId: 'sample'
  }).then(() => {
    AudioPlayer.play({
      audioId: 'sample'
    }).catch((reason: any) => {
      error.value = reason.toString();
    });

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
