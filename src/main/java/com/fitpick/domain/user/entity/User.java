package com.fitpick.domain.user.entity;

import com.fitpick.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class User extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login_id", nullable = false, unique = true, length = 50)
    private String loginId;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 20)
    private String phone;

    @Column
    private Integer height;

    @Column
    private Integer weight;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_group", length = 20)
    private AgeGroup ageGroup;

    @Column(length = 255)
    private String address;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(name = "try_on_image_url", length = 500)
    private String tryOnImageUrl;

    @Column(name = "fcm_token", length = 500)
    private String fcmToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    public static User create(String loginId, String encodedPassword, String name,
                              String phone, Integer height, Integer weight,
                              AgeGroup ageGroup, String address) {
        return User.builder()
                .loginId(loginId)
                .password(encodedPassword)
                .name(name)
                .phone(phone)
                .height(height)
                .weight(weight)
                .ageGroup(ageGroup)
                .address(address)
                .role(Role.CUSTOMER)
                .build();
    }

    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public void updateProfile(String phone, Integer height, Integer weight,
                              AgeGroup ageGroup, String address) {
        if (phone != null) this.phone = phone;
        if (height != null) this.height = height;
        if (weight != null) this.weight = weight;
        if (ageGroup != null) this.ageGroup = ageGroup;
        if (address != null) this.address = address;
    }

    public void updateProfileImageUrl(String url) {
        this.profileImageUrl = url;
    }

    public void updateTryOnImageUrl(String url) {
        this.tryOnImageUrl = url;
    }
}
