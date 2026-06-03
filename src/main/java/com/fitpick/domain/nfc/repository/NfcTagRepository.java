package com.fitpick.domain.nfc.repository;

import com.fitpick.domain.nfc.entity.NfcTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NfcTagRepository extends JpaRepository<NfcTag, Long> {

    // tag_uid로 활성 태그 조회. 비활성(is_active=false) 태그는 조회 안 됨.
    Optional<NfcTag> findByTagUidAndIsActiveTrue(String tagUid);
}
