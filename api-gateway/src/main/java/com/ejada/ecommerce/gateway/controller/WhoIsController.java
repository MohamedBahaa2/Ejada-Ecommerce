package com.ejada.ecommerce.gateway.controller;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class WhoIsController {
    private final DiscoveryClient discoveryClient;

    WhoIsController(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @GetMapping("/whois/{name}")
    List<String> whois(@PathVariable String name) {
        return discoveryClient.getInstances(name).stream()
                .map(i -> i.getHost() + ":" + i.getPort())
                .toList();
    }
}
