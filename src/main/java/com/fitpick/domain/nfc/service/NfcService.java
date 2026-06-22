package com.fitpick.domain.nfc.service;

import com.fitpick.domain.nfc.dto.NfcTagResponse;

public interface NfcService {
    NfcTagResponse getClothesByTagUid(String tagUid, Long userId);
}
