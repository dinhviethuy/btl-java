package com.fullnestjob.modules.subscribers.service;

import com.fullnestjob.modules.shared.dto.PaginationDtos.MetaDTO;
import com.fullnestjob.modules.shared.dto.PaginationDtos.PageResultDTO;
import com.fullnestjob.modules.shared.dto.PaginationDtos.PaginationQueryDTO;
import com.fullnestjob.modules.subscribers.dto.SubscribersDtos.CreateSubscribersBodyDTO;
import com.fullnestjob.modules.subscribers.dto.SubscribersDtos.SubscribersDetailDTO;
import com.fullnestjob.modules.subscribers.dto.SubscribersDtos.UpdateSubscribersBodyDTO;
import com.fullnestjob.modules.subscribers.entity.Subscriber;
import com.fullnestjob.modules.subscribers.repo.SubscriberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SubscribersService {
    private final SubscriberRepository subscriberRepository;

    public SubscribersService(SubscriberRepository subscriberRepository) {
        this.subscriberRepository = subscriberRepository;
    }

    public PageResultDTO<SubscribersDetailDTO> find(PaginationQueryDTO query) {
        int current = query != null && query.getCurrent() != null ? query.getCurrent() : 1;
        int pageSize = query != null && query.getPageSize() != null ? query.getPageSize() : 10;
        Page<Subscriber> page = subscriberRepository.findAll(PageRequest.of(current - 1, pageSize));
        PageResultDTO<SubscribersDetailDTO> res = new PageResultDTO<>();
        res.result = page.getContent().stream().map(this::toDetail).collect(Collectors.toList());
        MetaDTO meta = new MetaDTO();
        meta.current = current;
        meta.pageSize = pageSize;
        meta.total = (int) page.getTotalElements();
        meta.pages = page.getTotalPages();
        res.meta = meta;
        return res;
    }

    public Map<String, Object> findSkills(String email) {
        Subscriber s = subscriberRepository.findByEmail(email).orElse(null);
        return Map.of("skills", s != null ? s.getSkills() : java.util.Collections.emptyList());
    }

    @Transactional
    public SubscribersDetailDTO create(CreateSubscribersBodyDTO body, String email) {
        Subscriber exist = subscriberRepository.findByEmail(email).orElse(null);
        if (exist == null) {
            Subscriber s = new Subscriber();
            s.setEmail(email);
            s.setName(body.name);
            s.setSkills(body.skills);
            return toDetail(subscriberRepository.save(s));
        } else {
            // Nếu đã tồn tại thì cập nhật thông tin
            if (body.name != null) exist.setName(body.name);
            if (body.skills != null) exist.setSkills(body.skills);
            return toDetail(subscriberRepository.save(exist));
        }
    }

    @Transactional
    public SubscribersDetailDTO update(UpdateSubscribersBodyDTO body, String email) {
        java.util.Optional<Subscriber> opt = subscriberRepository.findByEmail(email);
        // Nếu skills rỗng => xoá đăng ký
        if (body.skills != null && body.skills.isEmpty()) {
            Subscriber s = opt.orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND, "Subscriber not found"));
            SubscribersDetailDTO dto = toDetail(s);
            subscriberRepository.delete(s);
            return dto;
        }
        if (opt.isEmpty()) {
            // Không tồn tại thì đăng ký mới (upsert)
            Subscriber s = new Subscriber();
            s.setEmail(email);
            s.setName(body.name);
            s.setSkills(body.skills);
            return toDetail(subscriberRepository.save(s));
        } else {
            Subscriber s = opt.get();
            if (body.name != null) s.setName(body.name);
            if (body.skills != null) s.setSkills(body.skills);
            if (body.email != null) s.setEmail(body.email);
            return toDetail(subscriberRepository.save(s));
        }
    }

    @Transactional
    public void delete(String email) {
        Subscriber s = subscriberRepository.findByEmail(email)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Subscriber not found"));
        subscriberRepository.delete(s);
    }

    private SubscribersDetailDTO toDetail(Subscriber s) {
        SubscribersDetailDTO dto = new SubscribersDetailDTO();
        dto._id = s.get_id();
        dto.name = s.getName();
        dto.email = s.getEmail();
        dto.skills = s.getSkills();
        dto.createdAt = s.getCreatedAt();
        dto.updatedAt = s.getUpdatedAt();
        dto.deletedAt = s.getDeletedAt();
        return dto;
    }
}


