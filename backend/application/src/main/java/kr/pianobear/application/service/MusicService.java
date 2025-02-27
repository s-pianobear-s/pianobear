package kr.pianobear.application.service;

import kr.pianobear.application.dto.MusicDTO;
import kr.pianobear.application.dto.MusicPracticeDTO;
import kr.pianobear.application.model.FileData;
import kr.pianobear.application.model.Member;
import kr.pianobear.application.model.Music;
import kr.pianobear.application.repository.MemberRepository;
import kr.pianobear.application.repository.MusicPracticeRepository;
import kr.pianobear.application.repository.MusicRepository;
import kr.pianobear.application.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MusicService {

    private final MusicRepository musicRepository;
    private final MusicPracticeService musicPracticeService;
    private final FileDataService fileDataService;
    private final MemberRepository memberRepository;
    private final MusicPracticeRepository musicPracticeRepository;
    private final PdfToMusicXmlService pdfToMusicXmlService;
    private final MusicXmlModifierService musicXmlModifierService;
    private final OpenAiService openAiService;

    @Autowired
    public MusicService(MusicRepository musicRepository, MusicPracticeService musicPracticeService, FileDataService fileDataService, MemberRepository memberRepository, MusicPracticeRepository musicPracticeRepository, PdfToMusicXmlService pdfToMusicXmlService, MusicXmlModifierService musicXmlModifierService, OpenAiService openAiService) {
        this.musicRepository = musicRepository;
        this.musicPracticeService = musicPracticeService;
        this.fileDataService = fileDataService;
        this.memberRepository = memberRepository;
        this.musicPracticeRepository = musicPracticeRepository;
        this.pdfToMusicXmlService = pdfToMusicXmlService;
        this.musicXmlModifierService = musicXmlModifierService;
        this.openAiService = openAiService;
    }

    @Transactional
    public MusicDTO processPdfUpload(MultipartFile pdfFile) throws IOException, InterruptedException {
        // PDF 파일 저장
        FileData fileData = fileDataService.savePdfFile(pdfFile);

        // 새로운 Music 엔티티 생성 및 초기화
        Music music = new Music();

        // 파일 이름에서 .pdf 확장자를 제거하고, 첫 9글자만 가져오기
        String title = pdfFile.getOriginalFilename().replace(".pdf", "");
        if (title.length() > 9) {
            title = title.substring(0, 9);
        }
        music.setTitle(title);
        music.setFavorite(false);
        music.setUploadDate(LocalDate.now());

        // 현재 사용자 정보 설정
        String currentUserId = getCurrentUserId();
        Member user = memberRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found with id " + currentUserId));
        music.setUser(user);

        // PDF to MusicXML 변환
        String mxlFilePath = pdfToMusicXmlService.convertPdfToMusicXml(fileData.getPath());
        music.setMusicXmlRoute(mxlFilePath);

        // MusicXML 수정
        String modifiedMxlFilePath = musicXmlModifierService.modifyMusicXml(mxlFilePath);
        music.setModifiedMusicXmlRoute(modifiedMxlFilePath);

        // Music 엔티티를 데이터베이스에 저장
        Music savedMusic = musicRepository.save(music);

        // DTO로 변환하여 반환
        return mapMusicToDTO(savedMusic);
    }

    @Transactional
    public MusicDTO saveMusic(MusicDTO musicDTO) throws IOException {
        validateTitleLength(musicDTO.getTitle());

        Music music = new Music();
        music.setTitle(musicDTO.getTitle());
        music.setArtist(musicDTO.getArtist());
        music.setUploadDate(LocalDate.now());
        music.setFavorite(false);

        String currentUserId = getCurrentUserId();
        Member user = memberRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found with id " + currentUserId));
        music.setUser(user);

        music.setMusicXmlRoute(musicDTO.getMusicXmlRoute());
        music.setModifiedMusicXmlRoute(musicDTO.getModifiedMusicXmlRoute());

        music.setMusicImg(null);

        Music savedMusic = musicRepository.save(music);

        return mapMusicToDTO(savedMusic);
    }

    private void validateTitleLength(String title) {
        if (title.length() >= 9) {
            throw new IllegalArgumentException("Title cannot exceed 9 characters.");
        }
    }

    @Transactional
    public MusicDTO editMusic(int musicId, String title) throws IOException {
        // 기존 Music 엔티티를 ID로 조회
        Music music = musicRepository.findById(musicId)
                .orElseThrow(() -> new RuntimeException("Music not found with id " + musicId));

        // 제목 업데이트
        music.setTitle(title);

        // 변경 사항을 저장
        Music savedMusic = musicRepository.save(music);

        // DTO로 변환하여 반환
        return mapMusicToDTO(savedMusic);
    }

    public MusicDTO fileDataToMusicDTO(FileData fileData) throws IOException {
        // 새로운 Music 엔티티 생성 및 초기화
        Music music = new Music();
        music.setTitle(fileData.getOriginalName().substring(0, fileData.getOriginalName().lastIndexOf(".")));
        music.setFavorite(false);
        music.setUploadDate(LocalDate.now());

        // 현재 사용자 정보 설정
        String currentUserId = getCurrentUserId();
        Member user = memberRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found with id " + currentUserId));
        music.setUser(user);

        // PDF to MusicXML 변환
        music.setMusicXmlRoute(fileData.getPath());

        // MusicXML 수정
        String modifiedMxlFilePath = musicXmlModifierService.modifyMusicXml(fileData.getPath());
        music.setModifiedMusicXmlRoute(modifiedMxlFilePath);

        // Music 엔티티를 저장하지 않고 DTO로 변환하여 반환
        return mapMusicToDTO(music);
    }

    public String getModifiedMusicXmlRoute(int musicId) {
        Optional<Music> optionalMusic = musicRepository.findById(musicId);
        if (optionalMusic.isPresent()) {
            Music music = optionalMusic.get();
            return music.getModifiedMusicXmlRoute();
        } else {
            throw new RuntimeException("Music not found with id " + musicId);
        }
    }

    public MusicDTO generateAndSaveImage(int musicId) throws IOException {
        Optional<Music> optionalMusic = musicRepository.findById(musicId);
        if (optionalMusic.isPresent()) {
            Music music = optionalMusic.get();
            String imageUrl = openAiService.generateImage(music.getTitle());
            music.setMusicImg(imageUrl);
            musicRepository.save(music);
            return mapMusicToDTO(music);
        } else {
            throw new RuntimeException("Music not found with id " + musicId);
        }
    }

    private String getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    private MusicDTO mapMusicToDTO(Music music) {
        MusicDTO musicDTO = new MusicDTO();
        musicDTO.setId(music.getId());
        musicDTO.setTitle(music.getTitle());
        musicDTO.setMusicXmlRoute(music.getMusicXmlRoute());
        musicDTO.setModifiedMusicXmlRoute(music.getModifiedMusicXmlRoute());
        musicDTO.setUserId(music.getUser().getId());
        musicDTO.setMusicImg(music.getMusicImg());
        musicDTO.setFavorite(music.getFavorite());
        musicDTO.setUploadDate(music.getUploadDate());
        musicDTO.setArtist(music.getArtist());
        return musicDTO;
    }

    public String getMusicImgPath(int musicId) {
        Optional<Music> optionalMusic = musicRepository.findById(musicId);
        if (optionalMusic.isPresent()) {
            Music music = optionalMusic.get();
            return music.getMusicImg();
        } else {
            throw new RuntimeException("Music not found with id " + musicId);
        }
    }

    public String getMusicXmlRoute(int musicId) {
        Optional<Music> optionalMusic = musicRepository.findById(musicId);
        if (optionalMusic.isPresent()) {
            Music music = optionalMusic.get();
            return music.getMusicXmlRoute();
        } else {
            throw new RuntimeException("Music not found with id " + musicId);
        }
    }

    public List<MusicDTO> getAllMusic() {
        List<Music> musicList = musicRepository.findAll();
        return musicList.stream().map(this::mapMusicToDTO).collect(Collectors.toList());
    }

    public Optional<MusicDTO> getMusicById(int id) {
        Optional<Music> music = musicRepository.findById(id);
        return music.map(this::mapMusicToDTO);
    }

    @Transactional
    public void deleteMusic(int id) {
        Optional<Music> optionalMusic = musicRepository.findById(id);
        if (optionalMusic.isPresent()) {
            Music music = optionalMusic.get();

            // 연관된 엔티티 삭제 (CascadeType.ALL 설정으로 인해 자동 삭제)
            musicRepository.delete(music);
        } else {
            throw new RuntimeException("Music not found with id " + id);
        }
    }

    @Transactional
    public void favoriteMusic(int id, boolean favorite) {
        Optional<Music> music = musicRepository.findById(id);
        if (music.isPresent()) {
            Music m = music.get();
            m.setFavorite(favorite);
            musicRepository.save(m);
        }
    }

    public List<MusicDTO> searchMusicByTitle(String title) {
        List<Music> musicList = musicRepository.findByTitleContainingIgnoreCase(title);
        return musicList.stream().map(this::mapMusicToDTO).collect(Collectors.toList());
    }

    public List<MusicDTO> searchMusicByArtist(String artist) {
        List<Music> musicList = musicRepository.findByArtistContainingIgnoreCase(artist);
        return musicList.stream().map(this::mapMusicToDTO).collect(Collectors.toList());
    }

    public List<LocalDate> getUploadDates(String userId) {
        List<Music> musicList = musicRepository.findByUserId(userId);
        return musicList.stream().map(Music::getUploadDate).collect(Collectors.toList());
    }

    public boolean getFavoriteStatus(int id) {
        Optional<Music> music = musicRepository.findById(id);
        return music.map(Music::getFavorite).orElse(false);
    }

    public List<MusicDTO> getMusicByUserAndSort(String userId, String sortBy, String direction) {
        List<Music> musicList;

        switch (sortBy.toLowerCase()) {
            case "uploaddate":
                if ("asc".equalsIgnoreCase(direction)) {
                    musicList = musicRepository.findByUserIdOrderByUploadDateAsc(userId);
                } else {
                    musicList = musicRepository.findByUserIdOrderByUploadDateDesc(userId);
                }
                break;
            case "title":
                if ("asc".equalsIgnoreCase(direction)) {
                    musicList = musicRepository.findByUserIdOrderByTitleAsc(userId);
                } else {
                    musicList = musicRepository.findByUserIdOrderByTitleDesc(userId);
                }
                break;
            case "favorite":
                musicList = musicRepository.findByUserIdOrderByFavoriteDesc(userId);
                break;
            case "practicecount":
                musicList = musicPracticeRepository.findTop3ByUserIdOrderByPracticeCountDesc(userId);
                break;
            default:
                throw new IllegalArgumentException("Invalid sort parameter");
        }

        return musicList.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public List<MusicDTO> getTop3Practiced(String userId) {
        List<Music> top3PracticedMusic = musicPracticeRepository.findTop3ByUserIdOrderByPracticeCountDesc(userId);
        return top3PracticedMusic.stream()
                .map(music -> mapToDTO(music))
                .collect(Collectors.toList());
    }

    private MusicDTO mapToDTO(Music music) {
        MusicDTO musicDTO = new MusicDTO();
        musicDTO.setId(music.getId());
        musicDTO.setTitle(music.getTitle());
        musicDTO.setMusicXmlRoute(music.getMusicXmlRoute());
        musicDTO.setModifiedMusicXmlRoute(music.getModifiedMusicXmlRoute());
        musicDTO.setUserId(music.getUser() != null ? music.getUser().getId() : null);
        musicDTO.setMusicImg(music.getMusicImg());
        musicDTO.setFavorite(music.getFavorite());
        musicDTO.setUploadDate(music.getUploadDate());
        musicDTO.setArtist(music.getArtist());
        return musicDTO;
    }

    public List<MusicDTO> getAdminMusic() {
        List<Music> adminMusicList = musicRepository.findByUser_Id("admin");
        return adminMusicList.stream().map(this::mapMusicToDTO).collect(Collectors.toList());
    }

    public List<MusicDTO> getUserMusic() {
        String userId = SecurityUtil.getCurrentUserId();  // 수정된 부분
        List<Music> userMusicList = musicRepository.findByUser_IdAndUserIsNotNull(userId);
        return userMusicList.stream().map(this::mapMusicToDTO).collect(Collectors.toList());
    }

}
