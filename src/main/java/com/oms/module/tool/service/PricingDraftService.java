package com.oms.module.tool.service;

import com.oms.module.tool.entity.PricingDraft;
import com.oms.module.tool.repository.PricingDraftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PricingDraftService {

    private final PricingDraftRepository draftRepository;

    @Transactional(readOnly = true)
    public Optional<PricingDraft> getDraftByUsername(String username) {
        return draftRepository.findByUsername(username);
    }

    @Transactional
    public void saveDraft(String username, String draftJson) {
        PricingDraft draft = draftRepository.findByUsername(username)
                .orElse(new PricingDraft());

        draft.setUsername(username);
        draft.setDraftData(draftJson);
        draft.setUpdatedAt(LocalDateTime.now());

        draftRepository.save(draft);
    }

    @Transactional
    public void deleteDraft(String username) {
        draftRepository.findByUsername(username).ifPresent(draftRepository::delete);
    }
}