<template>
  <!-- <Bear class="bear" /> -->

  <v-sheet
    style="position: relative; overflow: hidden"
    :elevation="3"
    rounded="lg"
  >
    <OvVideo
      :stream-manager="streamManager"
      style="position: absolute"
    ></OvVideo>
    <div class="menus">
      <Transition name="fade">
        <div
          v-if="communityStore.emoteMap.has(userStore.user.id)"
          :class="emoteSize"
        >
          {{ communityStore.emoteMap.get(userStore.user.id).content }}
        </div>
      </Transition>

      <div class="info">
        <span
          v-if="!communityStore.isVideoActive"
          class="mdi mdi-video-off me-2"
        ></span>
        <span
          v-if="!communityStore.isAudioActive"
          class="mdi mdi-volume-mute me-2"
        ></span>
        <span>{{ userStore.user.name }}</span>
      </div>
    </div>
  </v-sheet>
</template>

<script setup>
import { useUserStore } from "@/stores/user";
import OvVideo from "./OvVideo.vue";
import { useOpenviduStore } from "@/stores/community";
import { computed } from "vue";

const props = defineProps({
  streamManager: Object,
  surface: String,
});

const emoteSize = computed(() => {
  if (props.surface == "surface1") {
    return "emote1";
  } else {
    return "emote2";
  }
});

const userStore = useUserStore();
const communityStore = useOpenviduStore();
</script>

<style scoped>
.menus {
  display: flex;
  flex-direction: column;
  justify-content: end;

  height: 100%;
  width: 100%;
  z-index: 100;
}

.info {
  color: white;
  width: 100%;
  z-index: 100;
  max-width: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  padding: 5px 10px;
}

.emote1 {
  width: 100%;
  font-size: 180px;
  line-height: 220px;
  text-align: center;
  z-index: 100;
}

.emote2 {
  width: 100%;
  font-size: 100px;
  line-height: 70px;
  text-align: center;
  z-index: 100;
}

.fade-enter-active {
  transition: font-size 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.fade-leave-active {
  transition: opacity 7s ease-in-out;
}

.fade-enter-from {
  font-size: 0px;
}

.fade-leave-to {
  opacity: 0;
}
</style>
