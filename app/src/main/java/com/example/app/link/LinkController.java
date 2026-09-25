package com.example.app.link;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/links")
public class LinkController {

    private final LinkService linkService;

    public LinkController(LinkService linkService) {
        this.linkService = linkService;
    }

    @PostMapping
    public LinkResponse create(@RequestBody CreateLinkRequest request) {
        Link link = linkService.create(request.originalUrl());

        return LinkResponse.from(link);
    }

    @GetMapping("/{code}")
    public ResponseEntity<LinkResponse> findByCode(@PathVariable String code) {
        return linkService.findByCode(code)
                .map(LinkResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}