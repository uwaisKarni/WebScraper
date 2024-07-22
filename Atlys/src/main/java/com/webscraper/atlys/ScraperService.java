package com.webscraper.atlys;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.webscraper.atlys.domain.Product;


import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScraperService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> scrape(String url, int maxPages, String proxyString) throws IOException, InterruptedException {
        List<Product> products = new ArrayList<>();
        Proxy proxy = null;
        
        if (proxyString != null && !proxyString.isEmpty()) {
            String[] proxyParts = proxyString.split(":");
            if (proxyParts.length == 2) {
                String proxyHost = proxyParts[0];
                int proxyPort = Integer.parseInt(proxyParts[1]);
                proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            }
        }

        for (int i = 1; i <= maxPages; i++) {
            String pageUrl = url + "?page=" + i;
            Document doc;
            try {
                if (proxy != null) {
                    doc = Jsoup.connect(pageUrl).timeout(10 * 1000).proxy(proxy).get();
                } else {
                    doc = Jsoup.connect(pageUrl).timeout(10 * 1000).get();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Thread.sleep(5000); // Retry after 5 seconds
                i--; // Decrement the page count to retry the same page
                continue;
            }

            Elements items = doc.select(".product");
            Long id=0L;
            for (Element item : items) {
                String title = item.select(".product a").text();//item.select("h2.woocommerce-loop-product__title a").text();
                String priceString = item.select("span.price").text();
                String[] prices = priceString.split(" ");
               String price = null;
              for (String p : prices) {
             if (p.matches(".*\\d+.*")) { // Check if the string contains a number
             price = p.replace("₹", "").replace(",", "");
             break;
              }
           }
                String imageUrl = item.select("a.woocommerce-LoopProduct-link img").attr("src");

                // Download image and save it locally
                String imagePath = downloadImage(imageUrl, title);

                Product product = new Product(++id, title, Double.valueOf(price), imagePath);
                products.add(product);

                // Save to database if new or updated
                List<Product> existingProduct = productRepository.findByTitle(title);
                if (!existingProduct.isEmpty()) {
                    boolean productUpdated = false;
                    for (Product currProduct : existingProduct) {
                        if (currProduct.getPrice()!= Double.valueOf(price)) {
                            currProduct.setPrice(Double.valueOf(price));
                            productRepository.save(currProduct);
                            productUpdated = true;
                        }
                    }
                    if (!productUpdated) {
                        productRepository.save(product);
                    }
                }
            }
        }
        saveToJson(products);
        return products;
    }

    @Cacheable("products")
    public List<Product> getCachedProducts() {
        return productRepository.findAll();
    }

    private String downloadImage(String imageUrl, String title) throws IOException {
        
        return "/path/to/image.jpg";
    }

    private void saveToJson(List<Product> products) throws IOException {
        try (FileWriter file = new FileWriter("products.json")) {
            file.write(products.toString());
        }
    }
}