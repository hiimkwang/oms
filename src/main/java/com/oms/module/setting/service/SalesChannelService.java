package com.oms.module.setting.service;

import com.oms.module.setting.entity.SalesChannel;
import com.oms.module.setting.repository.SalesChannelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesChannelService {
    private final SalesChannelRepository salesChannelRepository;

    public List<SalesChannel> findAll() {
        return salesChannelRepository.findAll();
    }
}