<template>
  <v-dialog max-width="500">
    <template v-slot:activator="{ props: activatorProps }">
      <v-btn v-bind="activatorProps" text="생성하기"></v-btn>
    </template>

    <template v-slot:default="{ isActive }">
      <v-card title="방 만들기" color="#FFF9E0">
        <div class="create-input">
          <md-outlined-text-field
            label="방 제목"
            v-model="roomSetting.sessionTitle"
          >
          </md-outlined-text-field>
          <md-outlined-text-field label="인원" value="6" readonly="true">
          </md-outlined-text-field>
          <md-outlined-text-field
            label="초대 메시지"
            v-model="roomSetting.invitationMessage"
          >
          </md-outlined-text-field>
          <md-outlined-text-field
            label="설명"
            v-model="roomSetting.description"
          >
          </md-outlined-text-field>
        </div>

        <v-card-actions>
          <v-spacer></v-spacer>

          <v-btn text="생성하기" @click="join"></v-btn>
          <v-btn text="나가기" @click="isActive.value = false"></v-btn>
        </v-card-actions>
      </v-card>
    </template>
  </v-dialog>
</template>
<script lang="ts" setup>
import { useRouter } from "vue-router";
import { ref, onMounted } from "vue";
import { useOpenviduStore, type RoomSetting } from "@/stores/community";

const router = useRouter();
const roomtitle = ref<HTMLInputElement>();
onMounted(() => {});

const roomSetting = ref<RoomSetting>({
  sessionTitle: "",
  invitationMessage: "",
  description: "",
});

const openviduStore = useOpenviduStore();

const join = function (): void {
  console.log(roomSetting.value);

  if (roomSetting.value.sessionTitle == "") {
    alert("방 제목은 꼭 입력해야해요.");
    return;
  }

  openviduStore.createSession(roomSetting.value).then((sessionId: string) => {
    router.push({
      name: "communiting",
      params: { id: sessionId },
    });
  });
};
</script>

<style scoped>
.create-input * {
  width: 80%;
  margin: 20px;
  background-color: white;
  border-radius: 10px;
}

.create-input {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #f5e5d1;
  padding: 50px 0px;
  text-align: center;
}

.btns {
  display: flex;
  justify-content: space-between;
  width: 270px;
  margin: auto;
}
</style>
