package kr.pianobear.application.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Data
public class Music {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Size(max = 9)
    private String title;
    private String musicXmlRoute;
    private String modifiedMusicXmlRoute;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    private Member user; // 외래 키로 설정하여 기본 제공 악보의 경우 null 가능

    private String musicImg;
    private Boolean favorite;
    private LocalDate uploadDate;
    private String artist;

    @OneToMany(mappedBy = "music", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MusicPractice> practices;

    @OneToMany(mappedBy = "music", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MusicTest> tests;

    @OneToMany(mappedBy = "music", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MusicHighScore> highScores;

    public void setUser(Member user) {
        this.user = user;
    }

    public Member getUser() {
        return user;
    }

    public Music(int id) {
        this.id = id;
    }

    public Music() {
    }
}
