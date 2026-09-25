package com.example.app.link;

import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class LinkService {

    private final LinkRepository linkRepository;

    public LinkService(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    public Link create(String originalUrl) {
        String code = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8);

        Link link = new Link(code, originalUrl);

        return linkRepository.save(link);
    }

    public Optional<Link> findByCode(String code) {
        return linkRepository.findByCode(code);
    }
}