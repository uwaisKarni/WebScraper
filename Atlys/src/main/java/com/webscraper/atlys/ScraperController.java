
package com.webscraper.atlys;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.webscraper.atlys.domain.Product;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ScraperController {

    @Autowired
    private ScraperService scraperService;

    @GetMapping("/scrape")
    public ResponseEntity<List<Product>> scrape(
            @RequestParam String url,
            @RequestParam(required = false, defaultValue = "3") int maxPages,
            @RequestParam(required = false, defaultValue = "") String proxy) {
        try {
            List<Product> products = scraperService.scrape(url, maxPages, proxy);
            return ResponseEntity.ok(products);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
}