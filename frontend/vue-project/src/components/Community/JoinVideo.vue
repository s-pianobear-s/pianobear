<template>
  <div :class="props.videos">
    <UserVideo
      :stream-manager="publisher"
      v-if="publisher"
      :class="surface"
      :surface="surface"
    ></UserVideo>

    <SubVideo
      :class="surface"
      :surface="surface"
      v-for="sub in subscribers"
      :key="sub.stream.connection.connectionId"
      :stream-manager="sub"
    ></SubVideo>
  </div>
</template>
<script lang="ts" setup>
import Bear from "@/components/Community/Bear.vue";
import { ref, defineProps } from "vue";
import { useOpenviduStore } from "@/stores/community";
import UserVideo from "./UserVideo.vue";
import SubVideo from "./SubVideo.vue";
import { storeToRefs } from "pinia";

const openviduStore = useOpenviduStore();

const { subscribers, publisher } = storeToRefs(openviduStore);

const props = defineProps({
  videos: String,
  surface: String,
  bearCheck: String,
});
</script>
<style scoped>
.surface1 {
  position: relative;
  height: 291px;
  width: 300px;
  margin: 10px;
}
.videos1 {
  display: flex;
  flex-wrap: wrap;
  background-color: #fff8d8;
  border-radius: 20px;
  width: 960px;
  margin: 10px 10px 0px 80px;
  justify-content: center;
  box-shadow: 0px 3px 5px rgba(0, 0, 0, 0.2);
}
.surface2 {
  position: relative;
  height: 140px;
  width: 150px;
  margin: 10px;
}
.videos2 {
  display: flex;
  background-color: #fff8d8;
  border-radius: 20px;
  width: 60%;
  box-shadow: 0px 1px 5px rgba(0, 0, 0, 0.2);
}

.menus {
  display: flex;
  justify-content: right;
  padding: 2%;
  opacity: 0.7;
  z-index: 100;
}
.bear {
  position: absolute;
  width: 205px;
  height: 205px;
  z-index: 0;
  top: -30px;
  left: -36px;
}
.video {
  position: absolute;
  border-radius: 5px;
  width: 100%;
  height: 100%;
  background-color: white;
  border: 1px solid black;
}
</style>
