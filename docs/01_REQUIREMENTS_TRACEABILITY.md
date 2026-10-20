# Requirements Traceability ve Teslim Checklist

Bu dosya teslim boyunca canli tutulmalidir. `Durum` degerleri `TODO`, `DOING`, `DONE`, `VERIFIED` olmalidir. `VERIFIED` ancak baska ekip uyesi kaniti kontrol ettikten sonra kullanilir.

| Req | Puan | Zorunlu kanit | Planlanan dosya/demo | Durum |
|---:|---:|---|---|---|
| 1 | 0.50 | En az 4 pattern | Master plan bolum 5; class diagram pattern notlari | TODO |
| 2 | 0.50 | Problem, gerekce, basit alternatife ustunluk | Master plan bolum 5; sunum pattern tablosu | TODO |
| 3 | 1.00 | Tek kapsamli class diagram | `docs/diagrams/class-diagram.puml` + PNG/SVG export | TODO |
| 4 | 0.75 | Iki ayrintili sequence | `sequence-create-reservation.puml`; `sequence-checkin-checkout.puml` | TODO |
| 5 | 1.25 | Java PoC ve pattern etkileşimi | `hotel-patterns-poc`; test raporu; canli rezervasyon demosu | TODO |
| 6 | 2.00 | 3 mimari, data flow, component/deployment, arti/eksi, karsilastirma | 6 architecture `.puml`; master plan bolum 9 | TODO |
| 7 | 2.00 | En az 2 servis, inter-service, Postman, Docker, README | 3 servis; Compose; Postman; README; canli demo | TODO |
| 8 | 1.00 | En az 2 servis arasinda async queue | Booking producer; RabbitMQ; Notification consumer; idempotency testi | TODO |
| 9 | 1.00 | Build, test, deploy yapan CI/CD | `.github/workflows/ci.yml`; basarili run; Newman output | TODO |

## Donem ortasi 1-6 kontrolu

- [ ] Pattern adlari rapor, UML, kod ve sunumda ayni.
- [ ] Dort pattern icin problem, cozum, avantaj, maliyet ve demo kaniti var.
- [ ] Class diagram tek bir sistem tasarimi; dort ayri pattern diyagrami degil.
- [ ] Class diagram domainin Room, Reservation, Guest, Check-in/out, Payment ve Notification alanlarini kapsiyor.
- [ ] Pattern note'lari butun katilimcilara bagli.
- [ ] Sequence 1 happy path, no availability ve payment failure akisini gosteriyor.
- [ ] Sequence 2 check-in ve check-out durum/oda guncellemelerini gosteriyor.
- [ ] PoC Spring Boot ile basliyor.
- [ ] Pattern interaction integration testi var.
- [ ] Cakisan rezervasyon reddediliyor.
- [ ] Monolith component ve deployment diyagramlari var.
- [ ] Microservices component ve deployment diyagramlari var.
- [ ] Event-driven component ve deployment diyagramlari var.
- [ ] Her stil icin data flow, arti ve eksi yazili.
- [ ] Karsilastirma matrisi ve secim gerekcesi otel alanina ozel.
- [ ] Butun ekip uyeleri sunumda hazir.

## Final 7-9 kontrolu

- [ ] Inventory, Booking ve Notification ayri Spring Boot uygulamalari.
- [ ] Her servis ayri build ve container olarak calisiyor.
- [ ] Servisler tablo paylasmiyor.
- [ ] Booking -> Inventory REST cagrisi canli demoda goruluyor.
- [ ] Timeout ve anlasilir hata cevabi var.
- [ ] Booking -> RabbitMQ -> Notification akisi canli demoda goruluyor.
- [ ] Queue durable; routing key ve DLQ belgeli.
- [ ] Duplicate event ikinci bildirim uretmiyor.
- [ ] Dockerfile'lar multi-stage veya kucuk runtime image kullaniyor.
- [ ] Compose health check ve `depends_on` kosullari dogru.
- [ ] `.env.example` var; secret commit edilmemis.
- [ ] README sifirdan calistirma, portlar, env, seed ve test komutlarini iceriyor.
- [ ] Postman koleksiyonu setup, happy path ve error pathleri kapsiyor.
- [ ] Newman CLI'da yesil.
- [ ] GitHub Actions `mvn verify` calistiriyor.
- [ ] GitHub Actions Docker image'lari build ediyor.
- [ ] GitHub Actions Compose ile servisleri baslatip health kontrol ediyor.
- [ ] GitHub Actions Newman smoke testi calistiriyor.
- [ ] Hata halinde container loglari alinabiliyor.
- [ ] Temiz makine provasi tamamlandi.
- [ ] Butun ekip uyeleri final sunumunda hazir.

## Sunum gunu kanit klasoru

```text
submission-evidence/
  report.pdf
  diagrams/
    class-diagram.png
    sequence-create-reservation.png
    sequence-checkin-checkout.png
    architecture-*.png
  postman/
    Hotel-System.postman_collection.json
    Local-Docker.postman_environment.json
  screenshots/
    github-actions-success.png
    docker-compose-ps.png
    rabbitmq-message-flow.png
    concurrent-booking-test.png
  README.md
```

Screenshotlar asil calisan repo ve testlerin yerine gecmez; sunum sirasinda hizli kanit olarak kullanilir.

