# marketplace-product-parser
Partners (suppliers, marketplaces, B2B clients) send product catalog data not via API, but as files JSON and CSV. Files sent to the input folder.
  The service must periodically scan the folder, accept files, validate them (format, required fields, dates), process them in parallel, and persist results to the database. Successfully processed files move to processed/, failed ones to failed/.
# Tech Stack
- Java 21,
- Maven,
- Spring Boot
- Jackson (jackson-databind) JSON parsing
- OpenCSV (or manual BufferedReader) CSV parsing
- java.nio.file: Files, Path, DirectoryStream
- java.time: Instant, LocalDate, LocalDateTime, ZoneId, Duration
- java.util.concurrent: ExecutorService, CompletableFuture
- Spring @Scheduled, Bean Validation (jakarta.validation)
- Spring Data JPA + H2
- JUnit 5 + Mockito
- Lombok / Records
- SLF4J + Logback
# Data Model
Each file contains a product export from one partner.
JSON format (for example)
```
{
  "partnerId": "PARTNER-A",
  "exportDate": "2026-03-23T10:30:00Z",
  "products": [
    {
      "name": "Apple Fruit",
      "sku": "802999",
      "price": 41238.0,
      "specialPrice": 35000.0,
      "specialFrom": "2026-03-01",
      "specialTo": "2026-04-01",
      "state": "ACTIVE",
      "brand": "Shelf 3",
      "categories": ["Golden apple Bundles"],
      "imageUrl": "https://img.example.com/apple.jpg"
    }
  ]
}
```

CSV format
```
Name,SKU,Price,Special Price,Special From,Special To,State,Brand,Category,Image URL
Banana Fruit,000000001,180.0,,,,ACTIVE,Angel Of M,Fruits,https://img.example.com/banana.jpg
Apple Fruit,802999,41238.0,35000.0,2026-03-01,2026-04-01,ACTIVE,Shelf 3,Golden apple Bundles,https://img.example.com/apple.jpg
Strawberry F,151855,3.77,,,,ACTIVE,,Red apples,https://img.example.com/straw.jpg
Watermelon,966857,8.0,,,,ACTIVE,Aladin,Drinks,https://img.example.com/watermelon.jpg
```
