# Hotel Management and Reservation System - Ana Proje Plani

## 1. Yonetici ozeti

Bu proje icin en guvenli ve puan odakli yaklasim iki asamalidir:

1. Donem ortasi tesliminde gereksinim 1-6 tamamlanir. Tek bir Spring Boot uygulamasinda calisan, sinirli fakat gercek bir proof of concept (PoC) hazirlanir. Dort tasarim deseni ayni rezervasyon akisi icinde calisir. Birlesik sinif diyagrami, iki ayrintili sequence diyagrami ve uc mimari alternatif icin alti mimari diyagram sunulur.
2. Donem sonu tesliminde ayni alan modeli uc servise ayrilir: `inventory-service`, `booking-service`, `notification-service`. Kritik ve hemen cevap gerektiren islemler REST ile, bildirimler RabbitMQ ile asenkron yurur. Tum servisler Docker Compose ile baslar; Postman koleksiyonu butun uygulanan endpointleri test eder; GitHub Actions build, test, image olusturma ve yerel Docker ortaminda smoke test yapar.

Bu secim puan cetveliyle uyumludur. Mikroservis uygulamasi zaten gereksinim 7'de zorunludur. Buna ragmen saf asenkron event-driven tasarim rezervasyon onayi ve cakisma kontrolu icin uygun degildir. Bu nedenle genel mimari mikroservistir; RabbitMQ yalnizca gecikmeye toleransli rezervasyon bildirimlerinde kullanilir.

## 2. Kaynak belgedeki kesin talimatlar

### 2.1 Genel ders talimatlari

- Ekipler uc kisiye kadar calisabilir gibi gorunmektedir; belgedeki ifade `teams of three` oldugu icin tam uc kisi gerekip gerekmedigi hocaya sorulmalidir.
- Proje serbest secilmez. Moodle'in atadigi proje uygulanir. Bu ekip icin proje 6, Hotel Management and Reservation System'dir.
- Backend Spring Boot olmak zorundadir.
- Frontend teknolojisi serbesttir. Frontend icin ayrica puan maddesi yoktur.
- Sunum iki asamalidir ve butun ekip uyeleri sunumda bulunmalidir.
- Gereksinim 1-6 donem ortasinda, 7-9 donem sonunda sunulur.
- Ilk teslim atlanip her sey finalde sunulursa puan kesilir. Kesinti miktari belirtilmemistir.
- Belgede kesin sunum tarihi, teslim formati, sunum suresi ve Moodle yukleme kurali bulunmamaktadir.

### 2.2 Hotel sistemi icin zorunlu islevler

| Alan | Istenen kabiliyetler |
|---|---|
| Room Management | Oda ve oda turu olusturma/yetistirme, fiyat ve uygunluk tanimlama, oda durumunu izleme |
| Reservation Management | Uygun oda arama, rezervasyon olusturma/degistirme/iptal, cakisan rezervasyonlari onleme |
| Guest Management | Misafir kaydi ve guncelleme, gecmis/gelecek rezervasyonlar, tercihler |
| Check-in / Check-out | Check-in, check-out, rezervasyonu tamamlama, oda uygunlugunu otomatik guncelleme |
| Payments / Notifications | Simule odeme, rezervasyon onayi, degisiklik ve yaklasan konaklama bildirimleri |

### 2.3 Puan dagilimi ve kanit

| No | Puan | Teslim | Kabul kaniti |
|---:|---:|---|---|
| 1 | 0.50 | En az dort design pattern | Pattern listesi ve her birinin siniflari |
| 2 | 0.50 | Her pattern icin problem, cozum ve basit alternatife ustunluk | Pattern karar tablosu ve kod referanslari |
| 3 | 1.00 | Tek ve kapsamli UML class diagram | `docs/diagrams/class-diagram.puml`; pattern notlari ayni diyagramda |
| 4 | 0.75 | Iki ayrintili sequence diagram | Rezervasyon+odeme ve check-in/check-out akislari |
| 5 | 1.25 | Patternlerin Java PoC uygulamasi | Spring Boot PoC, birlestirilmis akisin testleri ve demo loglari |
| 6 | 2.00 | Monolith, microservices ve baska dagitik stil | Her stil icin component + deployment, data flow, arti/eksi, karsilastirma ve karar |
| 7 | 2.00 | En az iki bagimsiz mikroservis | Uc servis, REST ile iletisim, Docker, README, Postman |
| 8 | 1.00 | En az iki servis arasinda message queue | Booking producer, RabbitMQ, notification consumer, idempotency testi |
| 9 | 1.00 | Basit CI/CD | Build, test, Docker build, Compose up, health ve Newman smoke test |

Toplam 10 puandir.

## 3. Kapsam karari

### 3.1 Must have

- Oda turu ve oda olusturma.
- Belirli tarih araliginda uygun oda arama.
- Cakismayi atomik olarak engelleyerek rezervasyon olusturma.
- Oda turu, sezon ve ek hizmetlere gore fiyat hesaplama.
- Misafir kaydi ve misafirin rezervasyon gecmisi.
- Rezervasyon degistirme ve iptal.
- Simule odeme.
- Check-in ve check-out; oda durumunun otomatik degismesi.
- Rezervasyon olaylarindan bildirim olusturma.
- Docker, Postman, RabbitMQ ve CI/CD kanitlari.

### 3.2 Nice to have

- Basit React + TypeScript arayuzu.
- JWT kimlik dogrulama ve STAFF/GUEST rolleri.
- Yaklasan konaklama icin zamanlanmis reminder.
- OpenAPI/Swagger UI.
- Transactional outbox.
- Prometheus/Grafana.

Nice to have maddeleri ancak 1-9 tamamen bittikten sonra yapilmalidir. Frontend, Kubernetes, Kafka, service discovery ve API gateway bu proje olceginde puandan cok risk getirir.

### 3.3 Acik varsayimlar

- Check-in tarihi dahil, check-out tarihi haric tutulur. `[checkIn, checkOut)` modeli arka arkaya iki konaklamaya izin verir.
- Cakisma kosulu: `newCheckIn < existingCheckOut && newCheckOut > existingCheckIn`.
- `CANCELLED`, `COMPLETED`, `PAYMENT_FAILED` kayitlari uygunluk hesabini bloke etmez.
- Oda operasyon durumu `AVAILABLE`, `OCCUPIED`, `CLEANING`, `OUT_OF_SERVICE` olabilir.
- Rezervasyon durumu `PENDING_PAYMENT`, `CONFIRMED`, `CANCELLED`, `CHECKED_IN`, `COMPLETED`, `PAYMENT_FAILED` olabilir.
- Fiyat para birimi RON; `BigDecimal` kullanilir. Float/double kullanilmaz.
- Fiyat bir kez onaylandiktan sonra rezervasyonda snapshot olarak saklanir. Sonraki oda fiyat degisikligi mevcut rezervasyonu degistirmez.
- Odeme gercek para hareketi yapmaz. Provider basari/basarisizlik cevabi simule eder.
- E-posta gonderimi gercek bir dis servis yerine log/in-memory notification kaydi olabilir.

## 4. Aktorler ve ana kullanim senaryolari

### Guest

- Uygun oda arar.
- Misafir profili ve tercihleri olusturur/gunceller.
- Rezervasyon yapar, degistirir veya iptal eder.
- Simule odeme yapar.
- Gecmis ve gelecek rezervasyonlarini gorur.
- Rezervasyon onayi, degisiklik ve reminder bildirimi alir.

### Hotel Staff

- Oda turu, oda, fiyat ve operasyon durumu yonetir.
- Rezervasyonlari gorur ve gerektiğinde degistirir.
- Check-in/check-out yapar.
- Odayi `OCCUPIED`, `CLEANING`, `AVAILABLE` veya `OUT_OF_SERVICE` durumuna alir.

### System / Scheduler

- Oda tahsisini rezervasyon durumuna gore gunceller.
- Rezervasyon olaylarini yayinlar.
- Yaklasan konaklamalari tarar ve reminder olayi olusturur.

## 5. Tasarim desenleri

Dort desen secilir. Dortten fazlasi ek puan getirmedigi icin yalnizca alan problemine acikca uyan desenler kullanilir.

### 5.1 Strategy - fiyatlandirma kurallari

**Problem:** Fiyat oda turu, tarih/sezon, konaklama suresi ve ek hizmetlere gore degisir. Tek bir buyuk `if/else` blogu her yeni kuralda buyur ve test edilmesi zorlasir.

**Katilimcilar:**

- `PricingStrategy`
- `BaseRoomPricingStrategy`
- `SeasonalPricingStrategy`
- `LongStayDiscountStrategy`
- `AdditionalServicesPricingStrategy`
- `PricingContext`

**Neden daha iyi:** Her algoritma ayri test edilir. Yeni bir kampanya mevcut algoritmalari degistirmeden eklenir. Calisma aninda uygun strateji/strateji zinciri secilebilir.

**Maliyet:** Cok sayida kucuk sinif ve strateji siralamasinin belgelenmesi gerekir.

**Demo kaniti:** Ayni oda ve tarih icin standart, yaz sezonu ve uzun konaklama hesaplarini farkli testlerle gostermek.

### 5.2 Observer - rezervasyon olaylari ve bildirimler

**Problem:** Rezervasyon olusturma, degistirme, iptal ve check-in/out sonrasinda bildirim ve audit gibi birden cok islem tetiklenir. `ReservationService` bunlarin somut siniflarini bilirse sikica baglanir.

**Katilimcilar:**

- `ReservationEventPublisher` (Subject)
- `ReservationObserver`
- `GuestNotificationObserver`
- `AuditObserver`
- `ReservationEvent`

**Neden daha iyi:** Publisher kac abone oldugunu ve somut siniflari bilmez. Yeni observer mevcut rezervasyon kodunu degistirmeden eklenir. Bu desen finalde RabbitMQ publish/subscribe modeline dogal gecis saglar.

**Maliyet:** Hangi observer'in ne zaman calistigini izlemek zorlasabilir. Observer hatalari ana islemi bozmamali ve loglanmalidir.

**Demo kaniti:** Bir `ReservationCreated` olayi hem confirmation hem audit observer'ina ulasir; iptal olayinda farkli bildirim metni uretilir.

### 5.3 Decorator - ek hizmet ucretleri

**Problem:** Kahvalti, otopark, spa veya transfer gibi ek hizmetlerin her kombinasyonu icin `RoomWithBreakfastAndParking` benzeri alt siniflar yazmak sinif patlamasi olusturur. Strategy temel tarife algoritmasini secer; secilen ek hizmetlerin dinamik kombinasyonu farkli bir problemdir.

**Katilimcilar:**

- `ChargeComponent`
- `BaseStayCharge`
- `ServiceChargeDecorator`
- `BreakfastChargeDecorator`
- `ParkingChargeDecorator`
- `SpaChargeDecorator`

**Neden daha iyi:** Hizmetler calisma aninda istenen kombinasyonda sarilir. Yeni hizmet mevcut fiyat siniflarini degistirmeden eklenir. Kalitimla her kombinasyon icin ayri sinif yazilmaz.

**Maliyet:** Cok sayida kucuk nesne debug'i zorlastirabilir. Vergi ve indirim gibi siraya duyarli islemlerde decorator sirasi belgelenmelidir. Decorator nesneleri persistence modeli degil, gecici fiyat hesaplama nesneleridir.

**Demo kaniti:** Ayni temel konaklama ucretine kahvalti ve transfer ayri ayri veya birlikte eklenir; `total()` ve fiyat satirlari dogru sonucu verir.

### 5.4 Facade - rezervasyon orkestrasyonu

**Problem:** Controller uygunluk, fiyat, guest, reservation, payment ve event publisher siniflarini dogru sirada cagirmak zorunda kalirsa is kurali API katmanina dagilir.

**Katilimcilar:**

- `BookingFacade`
- `AvailabilityService`
- `PricingContext`
- `ReservationService`
- `PaymentService`
- `CheckInOutService`
- `ReservationEventPublisher`

**Neden daha iyi:** Controller tek bir yuksek seviyeli arayuz gorur. Orkestrasyon ve hata/compensation sirasi tek yerde kalir. Alt servisler yine dogrudan test edilebilir.

**Maliyet:** Facade her seyi yapan bir God Object'e donusmemelidir. Domain kurallari ilgili domain servislerinde kalmalidir.

**Demo kaniti:** `createAndPayReservation()` akisi Facade, Strategy, Decorator ve Observer desenlerini ayni senaryoda calistirir.

### 5.5 Secilmeyen desenler

- `Singleton`: Spring bean scope zaten nesne yasam dongusunu yonetir. Yalnizca bir sinifta `getInstance()` yazmak zayif bir pattern kaniti olur.
- `Builder`: Rezervasyon DTO'su icin kullanilabilir, fakat alan problemine Strategy/Decorator kadar guclu baglanmaz.
- `Adapter`: Gercekten uyumsuz bir harici odeme API'si verilmedigi icin simule provider etrafinda yapay bir adapter kurmak zayif kanit olur. Gercek provider eklenirse `PaymentGateway` portunun arkasinda kullanilabilir.
- `Command`: Create/modify/cancel islemlerini kapsayabilir, fakat undo/queue gereksinimi olmadigi icin gereksiz sinif sayisi olusturur.
- `State`: Rezervasyon durum gecisleri icin mantiklidir. Zaman kalirsa besinci desen olabilir; ana planda enum + acik transition guard kullanmak daha basittir.
- `Repository`: Yararlı bir kurumsal pattern'dir, fakat GoF tasarim deseni yerine dort zorunlu desen arasinda sayilmamalidir.

## 6. Domain modeli ve degismez kurallar

### 6.1 Ana nesneler

| Nesne | Sorumluluk |
|---|---|
| `RoomType` | Ad, kapasite, temel gecelik fiyat |
| `Room` | Oda numarasi, kat, oda turu, operasyon durumu |
| `StayPeriod` | Check-in/check-out tarihleri ve cakisma hesabi |
| `Guest` | Kimlik, iletisim, tercihler |
| `GuestPreference` | Yatak, sigara, kat veya not gibi tercih |
| `Reservation` | Misafir, oda, tarih, fiyat snapshot, durum ve zaman damgalari |
| `AdditionalService` | Kahvalti, havaalani transferi, spa vb. fiyatli hizmet |
| `ReservedService` | Rezervasyona eklenen hizmet ve adet |
| `Payment` | Tutar, provider referansi ve odeme durumu |
| `ReservationEvent` | Olay kimligi, tipi, aggregate kimligi, zaman ve correlation ID |
| `Notification` | Alici, kanal, icerik, durum ve event ID |

### 6.2 Temel invariants

1. `checkIn` bugunden once olamaz ve `checkIn < checkOut` olmalidir.
2. Oda kapasitesi misafir sayisini karsilamalidir.
3. `OUT_OF_SERVICE` oda rezerve edilemez.
4. Ayni oda icin bloklayan iki tahsis cakistirilamaz.
5. Yalnizca `PENDING_PAYMENT` rezervasyon odenebilir.
6. Basarili odeme rezervasyonu `CONFIRMED` yapar; basarisiz odeme tahsisi serbest birakir.
7. Yalnizca `CONFIRMED` rezervasyon check-in olabilir.
8. Yalnizca `CHECKED_IN` rezervasyon check-out olabilir.
9. Iptal edilen rezervasyon oda tahsisini serbest birakir ve event yayinlar.
10. Ayni `eventId` notification tarafinda ikinci kez islenmez.

## 7. UML teslimleri

### 7.1 Tek kapsamli class diagram

`docs/diagrams/class-diagram.puml` tek diyagramdir. Domain varliklari, application servisleri, repository arayuzleri ve dort pattern ayni tasarimda bulunur. Her pattern notu ilgili katilimcilara baglanir. Sunum icin PNG/SVG export edilir; kaynak `.puml` repoda kalir.

### 7.2 Sequence diagram 1 - rezervasyon, odeme, onay

`docs/diagrams/sequence-create-reservation.puml` su kanitlari verir:

- Uygunluk ve cakisma kontrolu.
- Strategy ile fiyat hesabi.
- Facade orkestrasyonu.
- Strategy ile temel fiyat ve Decorator ile ek hizmet toplami.
- Observer ile confirmation ve audit.
- Uygun oda yok ve odeme basarisiz `alt` akislari.

### 7.3 Sequence diagram 2 - check-in/check-out

`docs/diagrams/sequence-checkin-checkout.puml` su kanitlari verir:

- Durum gecisi guard'lari.
- Oda durumunun otomatik `OCCUPIED` ve daha sonra `CLEANING/AVAILABLE` olmasi.
- Rezervasyonun `CHECKED_IN` ve `COMPLETED` olmasi.
- Olay ve bildirim akisi.
- Gecersiz durum icin hata akisi.

## 8. Gereksinim 5 icin donem ortasi PoC

### 8.1 PoC siniri

Tek Spring Boot uygulamasi ve PostgreSQL/H2 ile su akisi calisir:

1. Oda turu, oda ve misafir seed edilir.
2. Guest tarih ve oda turu ile arama yapar.
3. `BookingFacade` uygunlugu kontrol eder.
4. `PricingContext` fiyat kurallarini uygular.
5. Rezervasyon `PENDING_PAYMENT` kaydedilir.
6. `PaymentGateway` portu uzerinden simule odeme yapilir.
7. Basarida rezervasyon `CONFIRMED` olur.
8. `ReservationEventPublisher` confirmation ve audit observer'larini calistirir.
9. Ikinci cakisan istek reddedilir.

PoC'de tum CRUD ekranlarini tamamlamak gerekmez. Calisan kod ile patternlerin birbiriyle etkilestigi kanitlanmalidir.

### 8.2 Minimum endpointler

- `POST /api/room-types`
- `POST /api/rooms`
- `POST /api/guests`
- `GET /api/rooms/availability?checkIn=&checkOut=&roomTypeId=`
- `POST /api/reservations`
- `PATCH /api/reservations/{id}`
- `POST /api/reservations/{id}/cancel`
- `POST /api/reservations/{id}/check-in`
- `POST /api/reservations/{id}/check-out`
- `GET /api/guests/{id}/reservations`

### 8.3 PoC testleri

- Her pricing strategy icin birim testi.
- Decorator kombinasyon ve fiyat satiri testleri.
- Publisher'in iki observer'i cagirdigi test.
- Facade happy path testi.
- Gecersiz tarih ve gecersiz status transition testleri.
- Ayni oda/tarih icin cakisma testi.

## 9. Uc mimari alternatif

### 9.1 Modular monolith

**Yapi:** Tek Spring Boot process; `room`, `guest`, `reservation`, `payment`, `notification` modulleri; tek PostgreSQL. Moduller controller/application/domain/infrastructure katmanlarina ayrilir.

**Data flow:** Client REST -> controller -> BookingFacade -> domain services/repositories -> database; notification ayni process icinde Observer ile calisir.

**Artilar:** Kurulum ve debug kolay; tek transaction ile cakisma kontrolu; az operasyon maliyeti; iki kisilik ekip icin hizli.

**Eksiler:** Tek deployment; bir modulun yukunu ayri olceklendirmek zor; hata tum uygulamayi etkileyebilir; gereksinim 7'yi karsilamaz.

**Diyagramlar:** `architecture-monolith-component.puml`, `architecture-monolith-deployment.puml`.

### 9.2 Microservices

**Yapi:** Inventory, Booking ve Notification servisleri. Her servis kendi verisine sahiptir. Booking, Inventory'yi REST ile cagirir. Booking olaylari RabbitMQ'ya yollar; Notification tuketir.

**Data flow:** Client -> Booking -> Inventory hold -> payment -> Inventory confirm -> Booking DB -> RabbitMQ -> Notification.

**Artilar:** Final gereksinimini dogrudan karsilar; net servis sorumlulugu; notification bagimsiz deploy/scale; arizalar daha iyi izole edilir.

**Eksiler:** Dagitik transaction yok; timeout/retry/idempotency gerekir; Docker ve log takibi daha karmasik; servis sozlesmeleri yonetilmelidir.

**Diyagramlar:** `architecture-microservices-component.puml`, `architecture-microservices-deployment.puml`.

### 9.3 Event-driven distributed architecture

**Yapi:** Command/API componentleri olaylari event bus'a yazar; availability, pricing, reservation, payment, notification ve read-model consumer'lari asenkron tepki verir. Projections sorgu modelini olusturur.

**Data flow:** Command -> event bus -> bir veya daha fazla consumer -> yeni events -> read model.

**Artilar:** Dusuk coupling; yeni consumer eklemek kolay; notification ve analytics icin iyi; ani yukleri queue tamponlar.

**Eksiler:** Rezervasyon kabulunde aninda ve kesin cevap zor; eventual consistency; event ordering, duplicate ve schema version sorunlari; demo ve debug daha zor.

**Diyagramlar:** `architecture-event-driven-component.puml`, `architecture-event-driven-deployment.puml`.

### 9.4 Karsilastirma

Puanlama 1 zayif, 5 guclu olacak sekildedir.

| Kriter | Agirlik | Monolith | Microservices | Event-driven |
|---|---:|---:|---:|---:|
| Rubrik 7-9 ile dogrudan uyum | 25% | 1 | 5 | 4 |
| Cakisma/transaction sadeligi | 20% | 5 | 3 | 2 |
| Iki kisilik ekip icin uygulanabilirlik | 20% | 5 | 4 | 2 |
| Bagimsiz deployment ve fault isolation | 15% | 1 | 5 | 5 |
| Bildirim/asenkron islere uygunluk | 10% | 2 | 4 | 5 |
| Demo ve degerlendirme acikligi | 10% | 5 | 5 | 3 |
| Agirlikli sonuc | 100% | 3.35 | 4.35 | 3.40 |

**Karar:** En uygun ana stil microservices'tir. Kritik uygunluk/hold islemi senkron REST ile kalir; bildirim asenkron RabbitMQ ile yapilir. Bu, microservices ve EDA'nin uygun parcalarini birlestirir fakat raporda uc alternatif ayri degerlendirilir.

## 10. Hedef mikroservis mimarisi

### 10.1 Inventory Service

**Sorumluluk:** Oda katalogu, oda turleri, operasyon durumu, tarih bazli hold/allocation ve cakisma onleme.

**Sahip oldugu tablolar:** `room_type`, `room`, `room_allocation`, `flyway_schema_history`.

**Ana endpointler:**

- `POST /room-types`
- `POST /rooms`
- `PATCH /rooms/{id}/status`
- `GET /availability`
- `POST /allocations/holds`
- `POST /allocations/{id}/confirm`
- `DELETE /allocations/{id}`

### 10.2 Booking Service

**Sorumluluk:** Guest, reservation lifecycle, fiyatlandirma, simulated payment, check-in/out orkestrasyonu ve event publish.

**Sahip oldugu tablolar:** `guest`, `guest_preference`, `reservation`, `reserved_service`, `payment`, istege bagli `outbox_event`.

**Kullandigi patternler:** Strategy, Decorator ve Facade; local PoC'de Observer. Finalde Observer'in process disi karsiligi RabbitMQ publish/subscribe olur.

### 10.3 Notification Service

**Sorumluluk:** Reservation eventlerini tuketme; confirmation/change/cancellation/reminder kaydi; ayni event'i ikinci kez islememe.

**Sahip oldugu tablolar:** `notification`, `processed_event`.

**Ana kanit endpointi:** `GET /notifications?guestId=`. Gercek e-posta zorunlu degildir.

### 10.4 Senkron ve asenkron sinir

| Islem | Iletisim | Gerekce |
|---|---|---|
| Uygun oda arama | Booking/Client -> Inventory REST | Kullanici aninda cevap bekler |
| Oda hold/confirm/release | Booking -> Inventory REST | Cakisma sonucu rezervasyon cevabini belirler |
| Payment | Booking icinde `PaymentGateway` portu | Simule provider ve basit PoC; Adapter olarak puanlanmaz |
| Confirmation/change/cancel notification | Booking -> RabbitMQ -> Notification | Gecikmeye toleransli, ana islemi bloklamamali |
| Reminder | Booking scheduler -> RabbitMQ -> Notification | Asenkron toplu is |

### 10.5 Rezervasyon akisi ve compensation

1. Booking, Inventory'den oda hold ister. Inventory bu islemi transaction icinde atomik yapar.
2. Hold basarisizsa `409 ROOM_NOT_AVAILABLE` doner.
3. Booking rezervasyonu `PENDING_PAYMENT` kaydeder ve simulated payment calistirir.
4. Odeme basariliysa hold confirm edilir ve reservation `CONFIRMED` olur.
5. Odeme basarisizsa hold release edilir ve reservation `PAYMENT_FAILED` olur.
6. Confirm sonrasi `reservation.created.v1` olayi publish edilir.
7. Notification consumer event ID'yi benzersiz kaydeder; duplicate mesaj etkisiz kalir.

### 10.6 Cakisma guvencesi

Yalnizca uygulama seviyesinde `exists overlap` sorgusu yapmak race condition uretir. Guvenli PoC yaklasimi:

- Inventory transaction'i oda satirini `SELECT ... FOR UPDATE` / JPA pessimistic write ile kilitler.
- Kilit altinda cakisan aktif allocation aranir.
- Cakisma yoksa allocation eklenir ve transaction commit edilir.
- Iki paralel test ayni oda/tarih icin calistirilir; tam birisi basarili, digeri `409` olmalidir.

PostgreSQL `daterange` exclusion constraint daha guclu ikinci savunma olabilir, fakat ders projesi icin Flyway + pessimistic lock daha kolay aciklanir ve test edilir.

### 10.7 RabbitMQ sozlesmesi

- Exchange: `hotel.booking.events`
- Type: `topic`
- Queue: `notification.reservation-events`
- Routing keys: `reservation.created.v1`, `reservation.modified.v1`, `reservation.cancelled.v1`, `reservation.checked-in.v1`, `reservation.checked-out.v1`, `reservation.reminder.v1`
- Dead letter exchange: `hotel.booking.events.dlx`
- Retry: sinirli retry, sonra DLQ.

Event envelope:

```json
{
  "eventId": "uuid",
  "eventType": "reservation.created.v1",
  "occurredAt": "2026-10-20T12:00:00Z",
  "correlationId": "uuid",
  "reservationId": "uuid",
  "guestId": "uuid",
  "roomId": "uuid",
  "payloadVersion": 1,
  "payload": {}
}
```

## 11. Teknoloji ve araclar

| Alan | Secim | Neden |
|---|---|---|
| Dil | Java 21 LTS | Modern Java, uzun destek, Spring Boot ile uyum |
| Backend | Spring Boot, Spring Web, Validation | PDF'de Spring Boot zorunlu |
| Persistence | Spring Data JPA + PostgreSQL | Transaction, locking ve gercek DB davranisi |
| Migration | Flyway | Tekrarlanabilir sema ve demo |
| HTTP client | Spring `RestClient` | Az ek bagimlilikla senkron servis cagrisi |
| Queue | RabbitMQ + Spring AMQP | Kurulumu ve routing modeli Kafka'dan daha basit; bildirim use case'i icin yeterli |
| Build | Maven multi-module | Takim icin okunabilir ve CI'da kolay |
| Test | JUnit 5, Mockito, Testcontainers, Awaitility | Unit, PostgreSQL/RabbitMQ integration ve async bekleme |
| API test | Postman + Newman | Gereksinim 7 ve CI smoke test |
| Packaging | Dockerfile + Docker Compose | Gereksinim 7 ve yerel deployment |
| CI/CD | GitHub Actions | Repo ile dogal entegrasyon ve kolay kanit |
| Diyagram | PlantUML | UML semantigi, Git diff ve tekrarlanabilir export |
| API belge | springdoc OpenAPI | Demo ve endpoint anlasilirligi, istege bagli |
| Observability | Spring Boot Actuator + structured logs | Health check ve servisler arasi correlation |
| Frontend | React + TypeScript + Vite, sonradan | Rubrik bittikten sonra sunum degeri icin |

Surumler `pom.xml` icinde sabitlenmeli; final teslimden once desteklenen Spring Boot patch surumu secilmelidir.

## 12. Onerilen repository yapisi

```text
hotel-management-system/
  README.md
  pom.xml
  docker-compose.yml
  .env.example
  .github/workflows/ci.yml
  docs/
    00_PROJECT_MASTER_PLAN_TR.md
    01_REQUIREMENTS_TRACEABILITY.md
    02_PRESENTATION_AND_DEMO_PLAN_TR.md
    diagrams/
      class-diagram.puml
      sequence-create-reservation.puml
      sequence-checkin-checkout.puml
      architecture-monolith-component.puml
      architecture-monolith-deployment.puml
      architecture-microservices-component.puml
      architecture-microservices-deployment.puml
      architecture-event-driven-component.puml
      architecture-event-driven-deployment.puml
  hotel-patterns-poc/
    src/main/java/...
    src/test/java/...
  services/
    inventory-service/
      Dockerfile
      pom.xml
      src/main/...
      src/test/...
    booking-service/
      Dockerfile
      pom.xml
      src/main/...
      src/test/...
    notification-service/
      Dockerfile
      pom.xml
      src/main/...
      src/test/...
  postman/
    Hotel-System.postman_collection.json
    Local-Docker.postman_environment.json
```

Donem ortasi sonrasinda PoC silinmemeli. `midterm-v1` etiketi veya release'i ile sunulan durum dondurulmalidir. Final servisleri ayni repoda gelistirilir.

## 13. Test stratejisi

### Unit

- Her Strategy algoritmasi.
- `StayPeriod.overlaps()` sinir degerleri.
- Rezervasyon status transition guard'lari.
- Decorator tekli ve coklu ek hizmet kombinasyonlari.
- Observer dispatch.
- Facade'in dogru sirada alt servisleri cagirmasi.

### Integration

- PostgreSQL Testcontainers ile repository ve locking.
- Iki paralel allocation istegi; yalnizca bir basari.
- Booking -> Inventory REST icin WireMock veya calisan container.
- RabbitMQ Testcontainers ile publish/consume.
- Duplicate event idempotency.

### End-to-end

- `docker compose up -d --build`.
- Actuator health endpointleri.
- Newman ile Postman koleksiyonu.
- Created -> notification eventually visible akisi.
- Cakisan ikinci rezervasyonda 409.

### Postman klasorleri

1. Setup - room types, rooms, guests.
2. Availability.
3. Reservation happy path.
4. Reservation conflicts and validation errors.
5. Modify/cancel.
6. Check-in/check-out.
7. Guest history/preferences.
8. Notifications and async verification.
9. Health checks.

Her request en az status code, response schema ve onemli ID/environment variable testine sahip olmalidir.

## 14. CI/CD tasarimi

GitHub Actions akisi:

1. Checkout.
2. Java kurulumu ve Maven cache.
3. `mvn -B verify` ile unit/integration test.
4. Uc servis icin Docker image build.
5. `docker compose up -d`.
6. Health endpointleri hazir olana kadar sinirli bekleme.
7. Newman smoke collection.
8. Hata halinde Compose loglarini artifact olarak yukleme.
9. Her durumda `docker compose down -v`.

Bu pipeline hosted runner'in kendi Docker ortaminda deploy eder. Belgedeki `at least to a local Docker environment` ifadesi icin bu yorum README'de acikca yazilmalidir. Hoca kendi makinesinde deploy bekliyorsa self-hosted runner gerekip gerekmedigi sorulmalidir.

## 15. Yol haritasi

Bugun 20 Ekim 2026. PDF kesin tarih vermedigi icin ilk teslim 2 Kasim'da code freeze olacak sekilde planlanmistir. Moodle tarihi farkliysa takvim ayni gorev sirasi korunarak kaydirilir.

### Donem ortasi: 20 Ekim - 2 Kasim

| Tarih | Cikti | Birincil sahip | Review |
|---|---|---|---|
| 20 Ekim | Rubrik, scope, varsayimlar, repo yapisi | Uye A | Uye B |
| 21 Ekim | Dort pattern karari ve katilimcilar | Uye B | Uye A |
| 22-23 Ekim | Birlesik class diagram | Uye B | Uye A |
| 24 Ekim | Iki sequence diagram | Uye B | Uye A |
| 25-27 Ekim | Spring Boot PoC ve pattern implementasyonu | Uye A | Uye B |
| 28 Ekim | Unit + conflict testleri | Ikisi | Ikisi |
| 29-30 Ekim | Uc mimari analizi ve alti diyagram | Uye A | Uye B |
| 31 Ekim | Rubrik traceability, README, exportlar | Uye B | Uye A |
| 1 Kasim | Temiz makine provasi ve sunum provasi | Ikisi | Ikisi |
| 2 Kasim | Code freeze, release/tag, yedek | Ikisi | Ikisi |

### Final asama: goreli haftalar

| Hafta | Cikti |
|---:|---|
| 1 | Inventory ve Booking servis iskeletleri, ayri DB migrationlari |
| 2 | Availability/hold/confirm REST akisi ve concurrency testi |
| 3 | Booking lifecycle, simulated payment portu, check-in/out |
| 4 | RabbitMQ eventleri, Notification consumer, idempotency/DLQ |
| 5 | Docker Compose, Postman/Newman, README |
| 6 | GitHub Actions CI/CD, clean-machine test, final demo provasi |

Gercek final tarihinden en az yedi gun once feature freeze yapilmalidir.

## 16. Iki kisilik is bolumu

### Uye A - backend ve altyapi lideri

- Spring Boot PoC.
- Inventory/Booking servisleri.
- DB locking ve concurrency testi.
- Docker Compose ve GitHub Actions.
- Mimari deployment diyagramlari.

### Uye B - domain tasarim ve kalite lideri

- Pattern gerekceleri ve UML class diagram.
- Sequence/component diyagramlari.
- Notification service ve RabbitMQ consumer.
- Postman koleksiyonu ve README.
- Sunum akisi ve traceability.

### Ortak sorumluluk

- Her PR diger kisi tarafindan review edilir.
- Ikisi de butun sistemi calistirip anlatabilmelidir.
- Her sunumdan once roller degistirilerek en az iki prova yapilir.
- Bir kisinin kodunu digeri aciklayamiyorsa gorev tamamlanmis sayilmaz.

## 17. Riskler ve onlemler

| Risk | Etki | Onlem |
|---|---|---|
| Kesin teslim tarihi bilinmiyor | Ilk teslimi kacirma | Moodle/hocadan hemen dogrula; 2 Kasim internal freeze |
| Pattern diyagram ve kod isimleri farkli | 1-5 puanlarinda guven kaybi | Sinif adlarini tek kaynaktan kullan; traceability tablosu |
| Cakisma kontrolu race condition | Ana domain kuralinin ihlali | DB transaction + pessimistic lock + paralel integration test |
| Cok fazla servis | Bitmeyen final | Uc servisle sinirla; gateway/discovery/Kubernetes ekleme |
| Queue duplicate delivery | Cift bildirim | `eventId` unique constraint ve idempotent consumer |
| Docker yalnizca bir bilgisayarda calisiyor | Demo riski | Diger ekip uyesinin temiz makinesinde README'yi uygula |
| CI sadece build ediyor | Req 9 eksik | Compose deploy + health + Newman adimlarini pipeline'a ekle |
| Frontend zaman aliyor | Rubrik maddeleri yarim kalir | UI'yi stretch goal yap; Swagger/Postman ile demo |
| Event-driven ile microservices ayni anlatiliyor | Req 6 karsilastirmasi zayif | Microservices alternatifini REST odakli, EDA'yi event bus/choreography odakli goster |

## 18. Hocaya sorulacak sorular

1. Donem ortasi ve final sunumunun kesin tarih/saat ve yukleme son tarihi nedir?
2. `teams of three` tam uc kisi mi, en fazla uc kisi mi demektir? Iki kisilik ekip kabul ediliyor mu?
3. Sunum suresi ve teslim formati nedir? PDF rapor, kaynak repo ve diyagram exportlari birlikte mi isteniyor?
4. Gereksinim 6 icin her mimari alternatifte hem component hem deployment diagram bekleniyor mu? Biz guvenli olarak ikisini de hazirlayacagiz.
5. Gereksinim 5 PoC ile gereksinim 7 mikroservis uygulamasi ayni repoda evrilebilir mi?
6. Postman'daki `all features` ifadesi tum proje tanimindaki islevler mi, yoksa uygulanan PoC endpointlerinin tamami mi?
7. CI/CD deploy adiminda GitHub-hosted runner uzerindeki Docker Compose yeterli mi?

## 19. En yuksek puan stratejisi

- Rubrik disi parlak ozelliklerden once dokuz satirin her birine somut dosya ve demo kaniti baglayin.
- Patternleri ayri oyuncak ornekler olarak gostermeyin. Tek rezervasyon akisi Strategy, Decorator, Facade ve Observer'i birlikte calistirsin.
- Class diagram ile Java sinif adlari ayni olsun.
- Her architecture icin component ve deployment ayri olsun; data flow oklarla gosterilsin.
- Mimari karsilastirmayi genel ezberle degil, otel alanindaki cakisma, bildirim, ekip boyutu ve rubrik uzerinden yapin.
- Cakisan iki paralel rezervasyon demosu teknik olarak en guclu kanittir.
- RabbitMQ mesajini publish edip consumer logu/notification kaydi gosterin; Req 8'i sadece config dosyasiyla birakmayin.
- Pipeline'in gercekten container baslatip Postman/Newman testi yaptigini loglarla kanitlayin.
- README adimlarini teslimden once diger bilgisayarda sifirdan deneyin.
- Sunumda once problem ve karar, sonra diyagram, sonra calisan kanit sirasi izleyin.

## 20. Definition of Done

Proje ancak su kosullarda tamamlanmis sayilir:

- `docs/01_REQUIREMENTS_TRACEABILITY.md` icindeki tum satirlar kanit dosyasina baglidir.
- Dort pattern ayni Java akisi icinde calisir ve test edilir.
- Tek class diagram ve iki sequence diagram export edilip okunabilirligi kontrol edilir.
- Uc mimari icin toplam alti diyagram vardir.
- `docker compose up -d --build` temiz makinede calisir.
- Postman/Newman tum uygulanan ozelliklerde yesildir.
- Paralel conflict testi yalnizca bir rezervasyonu kabul eder.
- RabbitMQ producer/consumer ve duplicate-event testi yesildir.
- GitHub Actions build, test, deploy ve smoke test adimlarini tamamlar.
- Iki ekip uyesi de sistemi bastan sona aciklayabilir ve sunumda hazirdir.

