# SDT Proje 6: Hotel Management and Reservation System

**Çalışma tarihi:** 20 Ekim 2026  
**Ekip:** iki kişi; isimler ve görev sahipleri sunumdan önce eklenecek  
**Hedef:** 1 Kasım'da başlayan ilk sunumlara 1–6. maddeleri gösterilebilir durumda yetiştirmek; dönem sonunda 7–9. maddeleri tamamlamak.

> Kaynak ayrımı: “Belge gereği” diye işaretlenen hususlar hocanın 13 sayfalık *SDT Project Details* PDF'inin 1, 12 ve 13. sayfalarından gelir. “Bizim kararımız” diye işaretlenen mimari, araç ve öncelikler ekip önerisidir. PDF kesin sunum günü, repo/rapor/slayt dosya biçimi veya zorunlu ön yüz açıklamaz. Kullanıcının verdiği bilgiye göre sınıf sunumları 1 Kasım'da başlar; kendi grubumuzun günü henüz net değil.

## 1. Dersin kesin kuralları ve puan planı

PDF sayfa 1: Proje numarası Moodle'daki quiz ile atanır, serbest seçim yoktur. Quiz'i ekipten yalnız bir üye doldurur ve tüm üyelerin adını verir. En çok üç kişilik ekip mümkündür; bizim ekip iki kişi. Backend **Spring Boot** olmak zorundadır. Ön yüzde başka teknoloji seçilebilir. Tüm ekip üyeleri iki sunumda da hazır bulunmalıdır. İlk aşama 1–6, ikinci aşama 7–9. maddelerdir. İlk aşamayı kaçırıp her şeyi sonda göstermek puan kaybettirir; kesinti miktarı yazmıyor.

| Madde | Puan | Hoca ne istiyor? | İlk aşamada göstereceğimiz kanıt |
|---|---:|---|---|
| 1 | 0,50 | En az dört tasarım kalıbı seçimi | Strategy, Decorator, State, Observer listesinin tasarımdaki rollerini gösterme |
| 2 | 0,50 | Her kalıbın somut problem, çözüm ve basit alternatife üstünlük açıklaması | Aşağıdaki gerekçe tablosu ve kod karşılıkları |
| 3 | 1,00 | Tüm projeyi kapsayan **tek** entegre UML sınıf diyagramı; kalıp sınıfları notlarla işaretlenebilir | `docs/uml/01-sinif-diyagrami.*` |
| 4 | 0,75 | Ana kullanım durumlarını anlatan **iki ayrıntılı** UML sıralama diyagramı | Rezervasyon/ödeme ve giriş/çıkış akışları |
| 5 | 1,25 | Önceki tasarım kalıplarının Java'da çalışan, tasarımla tutarlı bir **proof of concept** uygulaması | `hotel-core` Spring Boot API, fiyat/rezervasyon demo ve testler |
| 6 | 2,00 | Monolit, mikroservis ve başka bir dağıtık stilin incelenmesi; her biri için bileşen, veri akışı, component ve deployment gösterimi; artı/eksi; karşılaştırma ve seçim | Altı mimari diyagram, karşılaştırma tablosu, karar gerekçesi |
| **İlk aşama** | **6,00** | Dönem ortasında sunulur | Rapor, diyagram kaynakları, çalışan PoC, sunum |
| 7 | 2,00 | En az iki bağımsız sorumluluklu mikroservis, aralarında iletişim, uygulanan özellikleri kapsayan Postman collection, Docker ile çalıştırma ve net README | Dönem sonu hedefi: `hotel-core` + `notification-service`, Compose, koleksiyon |
| 8 | 1,00 | En az iki servis arasında kuyrukla asenkron haberleşme | RabbitMQ olay akışı |
| 9 | 1,00 | Otomatik build, test ve en az yerel Docker ortamına deploy eden CI/CD | GitHub Actions veya eşdeğeri pipeline |
| **Toplam** | **10,00** |  |  |

**En kritik yorum:** İlk sunum yalnız rapor değildir; 5. madde nedeniyle çalışan Java kodu gerekir. 6. madde ilk aşamada tek başına 2 puandır, bu yüzden altı mimari görsel ve karşılaştırmayı sona bırakmayacağız. PDF'nin 7. maddesi kapsamı özellikle çekirdek işlevlerle sınırlar; bütün otel işlevlerini üretim seviyesinde kodlamak beklenmez. “Postman all features” ifadesini uygulanan tüm uç noktaları ve ana senaryoları kapsayan koleksiyon olarak yorumlamak güvenli olur.

## 2. Otel sisteminde beklenen işlevler

| Alan | PDF'deki işlev | İlk prototip | Dönem sonu hedefi |
|---|---|---|---|
| Oda yönetimi | Oda ve oda tipi oluştur/yönet; fiyat, müsaitlik, durum | Oda tipi ve oda ekleme; baz fiyat, kapasite; ACTIVE/OUT_OF_SERVICE | Güncelleme, listeleme, fiziksel durum ayrımı |
| Rezervasyon | Tarihe göre boş oda bul; oluştur, değiştir, iptal et; çakışmayı engelle | Arama, oluşturma, iptal, çakışma testi | Değiştirme ve yeniden fiyatlama; PostgreSQL çakışma kısıtı |
| Misafir | Bilgileri ve tercihleri yönet; geçmiş/gelecek rezervasyonları gör | Kaydet, tercih alanı, rezervasyon sorgusu | Güncelleme, tarihsel filtreleme ve veri doğrulama |
| Giriş/çıkış | Check-in, check-out, rezervasyon tamamlama, müsaitlik güncelleme | Geçerli durum geçişiyle giriş/çıkış | Fiziksel oda durumu ve temizleme politikası netleştirme |
| Ödeme/bildirim | Simüle ödeme, rezervasyon onayı, değişiklik ve yaklaşan konaklama bildirimi | Sahte ödeme başarı/başarısızlık; bellek içi bildirim ve audit gözlemcileri | Ayrı bildirim servisi, RabbitMQ, bildirim kaydı ve hatırlatma |

### Öncelik kuralı

1. **Puan için zorunlu:** Dört kalıp gerçekten çalışsın; tek sınıf ve iki sequence diyagramı kodla tutarlı olsun; üç mimari tam kıyaslansın.
2. **Sistem doğruluğu:** Aynı odaya aynı tarih aralığında iki aktif rezervasyon girmesin. Arama ekranındaki “boş” sonucu rezervasyon anında tekrar doğrulansın.
3. **Genişletme:** İptal/değişiklik, check-in/out, ödeme ve bildirim minimal ama gösterilebilir olsun.
4. **Vakit kalırsa:** Basit web arayüzü, kimlik doğrulama, çoklu otel, gerçek e-posta, gerçek ödeme. Bunlar PDF'de puan maddesi olarak açıkça istenmiyor; çekirdeği riske atmayın.

## 3. Aktörler ve ana kullanım durumları

**Misafir:** uygun oda arar, fiyat görür, rezervasyon ve simüle ödeme yapar, kendi yaklaşan/geçmiş kayıtlarını görür, değişiklik/iptal ister, bildirim alır.  
**Resepsiyon personeli:** oda tiplerini, oda durumunu, rezervasyonları ve misafir bilgisini yönetir; check-in/out yapar.  
**Bildirim servisi:** rezervasyon olaylarını dinler, teslim denemesi ve geçmişini tutar.  
**Sistem saati:** yaklaşan konaklamalara hatırlatma olayı üretir; son aşamada planlı iş olabilir.

Sunumda iki canlı hikâye yeterince güçlüdür: (A) konuk uygun oda bulur, hafta sonu ve ek hizmet fiyatını görür, ödeme ile rezervasyonu onaylar; çakışan ikinci talep 409 alır; (B) resepsiyon onaylı rezervasyona check-in ve check-out uygular; geçersiz durum geçişini sistem reddeder. İptalden sonra aynı tarihin yeniden müsait olduğunun gösterilmesi üçüncü kısa kanıttır.

## 4. İş kuralları ve kabul ölçütleri

### 4.1 Tarih ve çakışma

- Konaklama `[checkIn, checkOut)` aralığıdır: giriş günü dahildir, çıkış günü hariçtir. Örnek: 7–9 Kasım ve 9–11 Kasım aynı odada yan yana bulunabilir.
- `checkIn < checkOut`; geçmiş tarih yasağı sunum politikasına göre seçilir. Testlerde sabit gelecek tarihler kullanılır.
- İki aralık şu koşulda çakışır: `mevcut.checkIn < istek.checkOut && istek.checkIn < mevcut.checkOut`.
- `CONFIRMED` ve `CHECKED_IN` müsaitliği bloke eder. İlk PoC'deki ödeme tek istek içinde simüle edilir; geçici `PENDING_PAYMENT` de işlem sırasında oda üzerinde tutulur. `CANCELLED` ve `PAYMENT_FAILED` bloklamaz. Kalıcı bekleyen ödeme aşaması eklenirse süre sonu/iptal kuralı da eklenmelidir.
- Arama sonucu garanti değildir. Kaydetme anında aynı oda için tekrar kontrol zorunludur. İlk PoC bellek içi kilit ile aynı süreçte iki eşzamanlı isteği sıralar; dönem sonunda PostgreSQL `EXCLUDE` kısıtı son savunma katmanıdır. Bellek içi kilit çoklu kopya/mikroservis için yeterli değildir.

### 4.2 Oda durumu ve müsaitlik

Odanın operasyonel durumu (`ACTIVE`, `OUT_OF_SERVICE`) ile bir tarihteki müsaitliği ayrı tutun. `ACTIVE` oda gelecekte belirli günlerde rezerveli olabilir. `OUT_OF_SERVICE` oda tarih ne olursa olsun aramadan çıkar. Fiziksel doluluk (`FREE`, `OCCUPIED`, olası `CLEANING`) check-in/out ile değişir; gelecek tarih müsaitliği rezervasyon dönemlerinden türetilir. Check-out rezervasyonu `COMPLETED` yapar ve fiziksel doluluğu serbest bırakır; rezervasyon geçmişi silinmez.

### 4.3 Fiyat ve ödeme

- Parasal değerler `BigDecimal` ve açık para birimiyle tutulur. Basit prototipte tek para birimi seçilir, örnek `EUR`; küsurat iki hane ile yuvarlanır.
- Baz fiyat: oda tipinin gecelik fiyatı × gece sayısı. `StandardPricingStrategy` ve `SeasonalPricingStrategy` dönem kuralını uygular. PoC kararı: hafta sonu geceleri 1,25 katsayı; hafta içi 1,00. Bu katsayı **bizim demo kuralımızdır**, hocanın verdiği rakam değildir.
- `BreakfastDecorator` gece başına 15, `ParkingDecorator` gece başına 10 ekler; bunlar da **demo fiyatlarıdır**. Aynı gecede birden fazla hizmet bileşebilir.
- Fiyat, ödeme anındaki teklif olarak rezervasyona snapshot kaydedilir; oda tipi baz fiyatı daha sonra değiştiğinde eski rezervasyon ücreti kendiliğinden değişmez.
- Simüle ödeme gerçek kart, banka veya ödeme sağlayıcısı kullanmaz. Başarı ve başarısızlık dalları ayrı gösterilir; başarısız ödeme odayı bloke etmemelidir.
- Dönem sonu `modify` özelliğinde tarih veya ek hizmet değişikliği fiyatı yeniden hesaplar. Tutar değişirse simüle ek tahsilat/iade kaydı ve yeni fiyat snapshot'ı oluşturulur. Bu işlem check-in öncesiyle sınırlı tutulur.

### 4.4 Durum makinesi

| Başlangıç | Eylem | Son | Ret koşulu |
|---|---|---|---|
| PENDING_PAYMENT | simüle ödeme başarılı | CONFIRMED | ödeme tekrar işlenmişse |
| PENDING_PAYMENT | simüle ödeme başarısız | PAYMENT_FAILED | — |
| CONFIRMED | iptal | CANCELLED | giriş gerçekleşmişse |
| CONFIRMED | check-in | CHECKED_IN | tarih/kimlik politikası uygun değilse |
| CHECKED_IN | check-out | COMPLETED | çıkış henüz yapılmamışsa |
| CANCELLED / COMPLETED / PAYMENT_FAILED | çoğu yeni işlem | değişmez | HTTP 409 ve açıklayıcı hata |

PoC'de `State` örüntüsü her izinli/izinsiz geçiş davranışını somut durum sınıfında tanımlar; yalnız `enum + switch` bu örüntü için yeterli kanıt değildir. Eşzamanlılık açısından rezervasyon işlemleri atomik tutulmalıdır.

### 4.5 Bildirim ve gizlilik

`ReservationConfirmed`, `ReservationCancelled`, `ReservationChanged`, `StayReminder` olayları için kimlik, rezervasyon kimliği ve gerekli tarih bilgisi taşınır; tüm misafir profili mesaj kuyruğuna konmaz. Bildirim servisi onay/değişiklik/hatırlatma içeriklerini üretir. PoC'de bir `NotificationObserver` ve bir `AuditObserver` aynı olayı dinleyerek Observer'ın bire-çok davranışını gösterir. Finalde Rabbit tüketicisi bildirimi üretir; `eventId` ile tekrar gelen mesajlar tek teslim kaydına dönüştürülür. Gerçek e-posta zorunlu değildir; konsol veya kayıt üzerinden simülasyon açıkça belirtilir.

## 5. Dört tasarım kalıbı: gerekçe ve kod kanıtı

| Kalıp | Somut ihtiyaç | Tasarımdaki sınıflar | Basit alternatif ve neden yetersiz? | Canlı kanıt |
|---|---|---|---|---|
| Strategy | Oda tipi ve konaklama gününe göre farklı tarifeler | `PricingStrategy`, `StandardPricingStrategy`, `SeasonalPricingStrategy`, `PricingService` | Tek büyük `if/switch` fiyat kuralı değiştikçe büyür ve testleri birbirine bağlar | Hafta içi ve hafta sonu tekliflerinin farklı çıkması |
| Decorator | Kahvaltı ve park ücretlerini seçime göre ekleme | `PriceComponent`, `BaseRoomPrice`, `PriceDecorator`, `BreakfastDecorator`, `ParkingDecorator` | Her hizmet kombinasyonu için ayrı sınıf/metot kombinasyon sayısını patlatır | Aynı rezervasyonda iki ek hizmetin üst üste fiyatlanması |
| State | Rezervasyonun ödeme, onay, giriş, çıkış, iptal adımlarındaki izinli geçişler | `ReservationState` ve somut durum sınıfları; `Reservation` bağlamı | Çok sayıda dağınık `if(status==...)` zamanla tutarsız geçişlere yol açar | Onaysız check-in ve tamamlanmış rezervasyonda iptal reddi |
| Observer | Rezervasyon olayı bildirim ve audit gibi birden çok yan etki doğurur | `BookingEventPublisher`, `BookingEventListener`, `NotificationObserver`, `AuditObserver` | Servisten doğrudan iki somut sınıfı çağırmak yeni tüketici eklerken rezervasyon kodunu değiştirir | Tek `ReservationConfirmed` olayı iki dinleyiciye gider |

Kalıplar **tek akışta birlikte** çalışır: oda ve tarih doğrulanır → Strategy baz fiyatı seçer → Decorator ek hizmetleri ekler → ödeme sonucuna göre State geçişi olur → Observer onay ve audit olayı üretir. UML'de dört ayrı bağımsız örnek diyagram değil, bu sınıfların tüm domain ile ilişkili olduğu **tek sınıf diyagramı** sunulur. Raporun tek kaynak diyagramı `docs/uml/01-sinif-diyagrami.mmd` / `.puml` çiftidir; farklı biçimler aynı tasarımın alternatif düzenleme kaynaklarıdır.

## 6. UML teslimi ve iki sequence'in kapsamı

**Tek sınıf diyagramı:** `Guest`, `RoomType`, `Room`, `Reservation`, `Payment`, `ExtraService`, `NotificationRecord`, uygulama servisleri, repository arayüzleri ve dört kalıbın rol sınıflarını aynı görselde gösterir. Çok büyük boyutta okunmaz hale gelmemesi için A3/yatay çıktı veya yüksek çözünürlüklü ek tercih edin; parçaları ayrı pattern diyagramlarına bölmeyin. Gösterimde sınıf sorumluluğu, önemli operasyonlar, çokluklar, kalıtım/uygulama ilişkisi ve pattern notları bulunmalı.

**Sequence 1, oda arama–rezervasyon–ödeme:** Misafir arama yapar, oda/tarih seçer; servis kritik bölümde müsaitliği tekrar kontrol eder; Strategy ve Decorator fiyatı hesaplar; simüle ödeme başarılı/başarısız olur; State geçer; Observer iki dinleyiciye haber verir. `alt` blokları 409 çakışma ve ödeme başarısızlığı içerir.

**Sequence 2, check-in–check-out:** Resepsiyon onaylı rezervasyonu seçer; State izin kontrolü, tarih/oda durumu, fiziksel doluluk güncellemesi ve olay yayımı görülür. Çıkışta `COMPLETED` ve `FREE` olur. Geçersiz geçiş dalı gösterilir.

**Kural:** Diyagramdaki sınıf, metod ve mesaj adları kod tesliminden önce gerçek prototip isimleriyle son kez eşlenmelidir. Finalde `notification-service` ayrı süreç olduğunda aynı modelde paket/sınır notu eklenir; sınıf ilişkileri kalıp kanıtı olarak kalır.

## 7. Üç mimari değerlendirmesi (6. madde)

Her seçenek için ayrı **component** ve ayrı **deployment** UML kaynağı `docs/uml/04`–`09` dosyalarındadır. Bileşen çizimi sorumluluk ve veri akışını, deployment çizimi çalıştığı süreç/düğüm ve altyapıyı açıklar.

### A. Modüler monolit

Tek Spring Boot çalıştırılabilir uygulama içinde `room`, `booking`, `guest`, `payment`, `notification` modülleri. Bir PostgreSQL veritabanı. REST isteği tek süreçte katmanlar arası doğrudan metot çağrılarıyla ilerler; transaction ve güçlü tutarlılık kolaydır. Paket sınırları konursa modüler yapı gelecekte ayrılabilir. Tek deploy birimi olduğu için bildirim hatası veya sürüm değişikliği tüm uygulamayı etkileyebilir; farklı parçaları bağımsız ölçekleyemezsiniz.

### B. Alan sınırları olan mikroservis

`hotel-core` odalar, misafirler, fiyat, rezervasyon, ödeme simülasyonu ve **çakışma değişmezinin tek sahibidir**. `notification-service` yalnız bildirim üretimi, teslim kaydı ve sorgusunun sahibidir. İki bağımsız Spring Boot imajı/süreci ve kendi veri sahipliği vardır; `hotel-core` rezervasyon olayını RabbitMQ'ya yayınlar, bildirim servisi tüketir. Zorunlu iş akışı tek servis/veritabanında kaldığı için çift rezervasyon riski dağıtık işlemle çözülmez. Bildirim geç gelebilir veya tekrarlanabilir; idempotence gerekir. İki kişilik ekip için altyapı yükü monolitten yüksektir ama #7–9'u doğrudan kanıtlar.

### C. Olay merkezli dağıtık düzen ve CQRS

Komut tarafı rezervasyonları kabul edip olay yayımlar; ayrı müsaitlik okuma projeksiyonu, fiyat/ödeme tüketicisi ve bildirim tüketicisi olaylardan kendi görünümünü günceller. Okuma ve yazma modelleri ayrılır; yeni tüketiciler eklemek kolaylaşır ve olay geçmişi izlenebilir. Buna karşılık müsaitlik projeksiyonu anlık gerçeğin gerisinde kalabilir; rezervasyon komutundaki güçlü çakışma koruması yine tek sahipte gerekir. Mesaj tekrarı, sırası, hata kurtarma ve gerekirse saga iki kişilik dönem projesi için maliyetlidir. Bu alternatif de mikroservislerle kurulabilir; ayrım burada **birincil iletişim ve okuma modeli**nin olay merkezli olmasıdır.

| Ölçüt | Modüler monolit | İki servisli mikroservis | Olay merkezli CQRS |
|---|---|---|---|
| İlk sürümü yapma hızı | En iyi | Orta | En yavaş |
| Rezervasyon tutarlılığı | Tek DB ile kolay | Core tek DB sahibi olduğu için güçlü | Komut tarafında güçlü, projeksiyonlarda gecikmeli |
| Bildirimi bağımsız çalıştırma | Hayır | Evet | Evet |
| Dağıtık hata/izleme yükü | Düşük | Orta | Yüksek |
| Dersin 7–9. maddelerini karşılama | Tek başına karşılamaz | Doğrudan karşılar | Karşılayabilir fakat gereksiz karmaşık |
| İki kişi ve kısa süre için uygunluk | İlk PoC için çok uygun | Dönem sonu hedefi için uygun | Düşük |

**Karar:** İlk prototipi modüler Spring Boot çekirdeği olarak kurup sınırları baştan ayırıyoruz. Dönem sonunda `notification-service` ayrılır; hedef teslim mimarisi iki servisli mikroservistir. Gerçek tek otelli küçük bir ürün için monolit daha ekonomik olabilirdi; dersin mikroservis, kuyruk ve CI/CD isterleri bu proje bağlamında iki servisli çözümü doğru teslim tercihi yapıyor. Tam olay merkezli CQRS'yi uygulamıyoruz; gerekli olmadığı için karmaşıklığı artırır.

## 8. Önerilen teknoloji ve nedenleri

| Katman/araç | Karar | Gerekçe |
|---|---|---|
| Dil/JDK | Java 21, Eclipse Temurin | İki bilgisayarda aynı LTS sürümü; makinedeki mevcut Java 25 uyumlu olsa da takım standardı gerekir |
| Backend | Spring Boot 4.1.1, Spring Web MVC, Validation | PDF'nin zorunlu backend koşulu; Java 21 ile güncel sürüm uyumlu |
| Derleme | Maven Wrapper (`mvnw`) | Global Maven kurulu değilse bile repo komutuyla sabit sürüm ve tekrarlanabilir build |
| İlk PoC saklama | Bellek içi repository | Kalıp ve akışların hızlı gösterimi; bu sürüm kalıcı veri/çoklu süreç garantisi vermez |
| Final DB | PostgreSQL + Flyway migration | Kalıcı veri ve `daterange` + `EXCLUDE` ile DB düzeyinde çakışma koruması |
| Servis iletişimi | Spring AMQP + RabbitMQ | #8 için açık kuyruk kanıtı; booking olayları doğal asenkron aday |
| Konteyner | Docker Compose | İki servis, Postgres, RabbitMQ tek komutla; healthcheck ile hazır olma denetimi |
| API demo | Postman collection + environment | #7 açıkça Postman ister; ID değişkenleri ve beklenen durum kodlarıyla tekrar oynatılabilir demo |
| CI/CD | GitHub Actions | Otomatik Maven build/test, image build, Compose deploy ve smoke test |
| Diyagram | Mermaid ve PlantUML kaynakları | Ekipçe kolay düzenleme; hocaya yüksek çözünürlüklü çıktı üretilebilir |
| Ön yüz | İlk aşamada Postman/OpenAPI; gerekiyorsa küçük Thymeleaf arayüzü | UI puanı açık yazılmıyor. Çalışan API ve ders maddeleri tamamlanmadan ayrı React işine girilmez |

Bu sürüm seçimi 20 Ekim 2026 tarihli resmi [Spring Boot sistem gereksinimleri](https://docs.spring.io/spring-boot/system-requirements.html) ile kontrol edildi. Flyway + PostgreSQL için Spring Boot'un [veri başlatma kılavuzu](https://docs.spring.io/spring-boot/how-to/data-initialization.html), kuyruk için [Spring AMQP kılavuzu](https://docs.spring.io/spring-boot/reference/messaging/amqp.html), Docker hazır olma sırası için [Compose belgeleri](https://docs.docker.com/compose/how-tos/startup-order), PostgreSQL aralık kısıtı için [resmi range types belgeleri](https://www.postgresql.org/docs/current/rangetypes.html) temel alınır. Kurulu ortamda Java 25 görüldü; Docker CLI mevcut ama Docker Desktop motoru çalışmıyordu; global `mvn` komutu bulunmadı. Bu tespit yalnız mevcut bilgisayar içindir; Maven Wrapper bu yüzden özellikle önemlidir.

### Final PostgreSQL güvenlik ağı

Örnek migration düşüncesi (tablo/sütun isimleri kodla netleştirilmeli):

```sql
CREATE EXTENSION IF NOT EXISTS btree_gist;
ALTER TABLE reservation
  ADD CONSTRAINT valid_stay CHECK (check_in < check_out);
ALTER TABLE reservation
  ADD CONSTRAINT no_active_room_overlap
  EXCLUDE USING gist (
    room_id WITH =,
    daterange(check_in, check_out, '[)') WITH &&
  ) WHERE (status IN ('CONFIRMED', 'CHECKED_IN'));
```

Uygulama, anlaşılır 409 vermek için önce kontrol eder; bu DB kısıtı iki eşzamanlı isteğin yarışında son korumadır. `room_id` veri tipi için GiST eşitlik operatörü migration testinde doğrulanır. Durum `PENDING_PAYMENT` kalıcı tutulacaksa kısıtın durum filtresi ve TTL politikası birlikte gözden geçirilir. `PaymentFailed` ya da `Cancelled` kaydı geçmiş olarak saklanır ancak takvimi bloke etmez.

## 9. Hedef repo yapısı

```text
Project/
├─ README.md
├─ hotel-core/                  # ilk aşama Spring Boot PoC; finalde rezervasyonun tek sahibi
│  ├─ pom.xml, mvnw, .mvn/
│  ├─ src/main/java/.../{api,domain,pricing,booking,events,infra}/
│  └─ src/test/java/...
├─ notification-service/        # dönem sonu: Rabbit consumer ve bildirim geçmişi
├─ docs/
│  ├─ SDT_HOTEL_PROJE_PLANI_TR.md
│  ├─ UML/ veya uml/            # tek bir klasör adı seçilecek; bu depoda `uml/`
│  ├─ api/                      # Postman collection/environment, API örnekleri
│  └─ presentation/             # 1. aşama sunum dosyaları
├─ compose.yaml                 # dönem sonu
└─ .github/workflows/ci.yml     # dönem sonu
```

Paketler `api` (DTO/controller), `application` (use case), `domain` (iş kuralı/pattern), `infrastructure` (in-memory, sonra JPA/Rabbit) yönünde tutulur. Controller içinde fiyat/çakışma hesabı yapılmaz. Domain nesneleri HTTP ya da Rabbit ayrıntısı bilmez. Finalde her servis diğer servisin tablosuna doğrudan bağlanmaz.

## 10. Uç nokta ve test planı

**İlk PoC API sözleşmesi** (kod tamamlandığında README ile doğrulanır):

| İşlem | HTTP | Beklenen cevap |
|---|---|---|
| Oda tipi oluştur | `POST /api/room-types` | 201, kimlik ve fiyat |
| Oda oluştur | `POST /api/rooms` | 201 |
| Odayı servis dışı yap | `PATCH /api/rooms/{id}/status` | 200 |
| Misafir oluştur | `POST /api/guests` | 201 |
| Müsaitlik ara | `GET /api/rooms/availability?checkIn=...&checkOut=...&capacity=...` | 200, uygun odalar |
| Rezervasyon + simüle ödeme | `POST /api/reservations` | Başarılıysa 201 CONFIRMED; çakışmada 409; ödeme başarısızlığında bloke etmeyen sonuç |
| Rezervasyon oku/listele | `GET /api/reservations/{id}`, `GET /api/reservations?guestId=...` | 200 |
| İptal | `POST /api/reservations/{id}/cancel` | 200 CANCELLED veya 409 |
| Giriş/çıkış | `POST /api/reservations/{id}/check-in`, `/check-out` | 200 veya geçersiz geçişte 409 |
| Bildirim/audit | `GET /api/notifications`, `GET /api/audit` | Olayın iki gözlemciye gittiğine kanıt |

**Dönem sonu ekleri:** `PATCH /api/reservations/{id}` ile tarih/hizmet değişimi; konuk ve oda güncelleme; bildirim servisi `GET /api/notifications`; event ID sorgusu, hatırlatma simülasyonu. Gerçek uygulanmış tüm endpointler Postman collection'da olmalı. Koleksiyonun sırası veriyi kendi üretmelidir: tip → oda → misafir → arama → rezervasyon → çakışma 409 → iptal → tekrar arama → yeni rezervasyon → check-in → check-out → bildirim kaydı. Kaydedilen kimlikler environment değişkenleriyle sonraki isteğe aktarılır.

**Anlamlı testler:** aynı odada aynı tarihe iki talep (bir başarı/bir 409), bitiş günüyle başlayan yeni rezervasyon (başarı), iptalden sonra tekrar rezervasyon (başarı), servis dışı oda (arama dışı), boş/ters tarih aralığı (400), oda kapasitesi, iki ek hizmetli fiyat, ödeme başarısızlığı, state geçiş reddi, guest geçmiş/gelecek listesi. Finalde PostgreSQL üzerinde paralel iki istekle DB kısıtının gerçekten çalıştığı integration test gerekir. İçinde yalnız metodu tekrar eden testler puan için güçlü kanıt değildir.

## 11. İki kişilik görev dağılımı ve 1 Kasım takvimi

Önerilen görev sahipliği isim verilmeden A/B olarak yazıldı. Her önemli çıktıyı diğer kişi gözden geçirir; sunumda ikisi de kod ve diyagramı açıklayabilir.

| Tarih | Üye A | Üye B | Ortak çıkış ölçütü |
|---|---|---|---|
| **20–21 Ekim** | Git/Java/Maven Wrapper, Spring Boot iskeleti, API kontratı | Gereksinim matrisi, dört kalıp gerekçesi, tek UML taslağı | PDF maddeleri iş listesine bağlı; ilk uygulama derlenir |
| **22–24 Ekim** | Oda/misafir/rezervasyon uç noktaları; Strategy/Decorator | State/Observer akış ve iki sequence, test senaryoları | Rezervasyon demo akışı uçtan uca çalışır |
| **25–27 Ekim** | Çakışma, iptal, check-in/out, simüle ödeme, testler | Üç mimarinin 6 diyagramı, artı/eksi ve seçim metni | 1–6 puanlarının her biri somut dosyayla kanıtlanır |
| **28–29 Ekim** | Kod gözden geçirme, hata düzeltme, temiz kurulum denemesi | Rapor son düzen, sunum ve konuşma notları | Temiz ortamdan kurulum + canlı demo prova edilir |
| **30–31 Ekim** | Alternatif demo veri/istekleri ve yedek ekran görüntüsü | Slayt son kontrol, süre prova | İki kişi de 1–6 maddelerini anlatabilir; repo dondurulur |
| **1 Kasım ve sonrası** | Dersin belirlediği grubumuzun sunumu | Sunum ve soru yanıtlama | Tarih Moodle/hocadan teyit edilir |

**Dönem sonu için önerilen sıra:** (1) PostgreSQL/Flyway ve gerçek eşzamanlılık testi, (2) `notification-service`, RabbitMQ ve event idempotence, (3) Compose, README ve Postman, (4) CI build/test/Compose deploy/smoke, (5) hatırlatma ve isteğe bağlı UI. Böylece ilk aşamadaki domain mantığı korunur, riskli altyapı işleri birer birer doğrulanır.

### Son sunumda gösterilecek çalışma sırası

1. `docker compose up --build --wait`; iki Spring uygulaması, Postgres ve RabbitMQ sağlıklı.
2. Postman collection, oda/misafir oluşturur ve rezervasyon yapar; ikinci çakışan talep 409 alır.
3. RabbitMQ üzerinden `ReservationConfirmed` olayı `notification-service`e gider; bildirim geçmişi endpoint'inde aynı event ID görülür.
4. Aynı event tekrar verildiğinde tek kayıt kaldığı gösterilir (idempotence).
5. GitHub Actions build + test + image build + Compose deploy + API smoke job çıktıları açılır. GitHub-hosted runner üzerindeki Compose kurulumu job bitince silinir; kalıcı sunucu yayını iddiasında bulunmayız. Hoca “local Docker environment” için laptopu kastederse aynı Compose komutu yerelde de çalışır.

## 12. Sunum planı ve soru hazırlığı

**Önerilen 10 slayt / 10–12 dakika** (ders süresi başka açıklanırsa uyarlanır):

1. Proje 6, ekip, kapsam ve PDF maddeleri.
2. Misafir ve personelin temel akışları; POC sınırı.
3. Çakışma kuralı ve `[giriş, çıkış)` örneği.
4. Dört tasarım kalıbı: problem → kod rolü → beklenen etki.
5. Tek entegre sınıf diyagramı; yalnız önemli ilişkileri sözlü takip etme.
6. Sequence 1: arama, rezervasyon, ödeme, bildirim, 409 dalı.
7. Sequence 2: giriş ve çıkış durumu.
8. Üç mimari, component/deployment diyagramları ve kıyaslama.
9. Neden ilk PoC modüler; neden final mikroservis; veri sahipliği.
10. Canlı API demo, test sonucu, dönem sonu planı.

**Soru cevapları hazır olsun:** “Dört pattern kodda nerede?”, “Fiyatın belirli tarihte değiştiğini nasıl gösteriyorsunuz?”, “İki kişi aynı odayı aynı anda rezerve ederse ne olur?”, “Arama sonucunu neden rezervasyon sırasında tekrar kontrol ediyorsunuz?”, “Neden Observer ile Rabbit aynı şey değil?”, “Mikroservisler aynı DB'yi kullanıyor mu?”, “Neden üçüncü stil mikroservisten farklı?”, “CI'da deploy hangi Docker ortamına yapılıyor?”

## 13. Tamamlama kontrol listesi

### İlk sunum (1–6)

- [ ] Moodle'da proje 6 ve iki ekip üyesi kaydı doğrulandı; sunum günümüz öğrenildi.
- [ ] Dört kalıbın her biri gerekçe, UML rolü ve çalışan kod ile eşleşiyor.
- [ ] Tek entegre sınıf diyagramı okunabilir çıktıya aktarıldı.
- [ ] İki sequence diyagramındaki çağrılar gerçek API/use case ile tutarlı.
- [ ] Monolit, mikroservis ve olay merkezli üçüncü seçenek için toplam 3 component + 3 deployment diyagramı var.
- [ ] Her mimarinin bileşenleri, veri akışı, artı/eksi ve karşılaştırmalı seçim raporda var.
- [ ] `hotel-core` temiz ortamda tek komutla derlenip test ediliyor; canlı demo tekrarlanıyor.
- [ ] İki kişi de tüm bölümlerde soru yanıtlayabiliyor.

### Dönem sonu (7–9)

- [ ] İki bağımsız Spring Boot servis var; sorumluluk ve veri sahipliği README'de açık.
- [ ] RabbitMQ mesajı gerçekten iki servis arasında gidiyor; tekrarlı teslim davranışı test edildi.
- [ ] Docker Compose bir komutla kuruyor; healthcheck ve `.env.example` hazır.
- [ ] Postman collection uygulanmış tüm endpointleri ve hata dallarını kapsıyor.
- [ ] CI build, test ve en az Docker ortamına deploy/smoke adımlarını otomatik koşuyor.
- [ ] README sıfırdan kurulum, portlar, demo, test ve temizlik komutlarını açıklıyor.

## Kaynaklar

- Ders PDF'si: *SDT Project Details*, sayfa 1 ve 12–13 (bu repo dışında yerel kaynak dosya).
- [Spring Boot system requirements](https://docs.spring.io/spring-boot/system-requirements.html)
- [Spring Boot AMQP/RabbitMQ](https://docs.spring.io/spring-boot/reference/messaging/amqp.html)
- [Spring Boot database initialization / Flyway](https://docs.spring.io/spring-boot/how-to/data-initialization.html)
- [PostgreSQL range types and exclusion constraints](https://www.postgresql.org/docs/current/rangetypes.html)
- [Docker Compose startup order](https://docs.docker.com/compose/how-tos/startup-order)
- [GitHub Actions Java with Maven](https://docs.github.com/en/actions/tutorials/build-and-test-code/java-with-maven)
- [GitHub-hosted runner lifecycle](https://docs.github.com/en/actions/how-tos/manage-runners/github-hosted-runners/use-github-hosted-runners)
- [Postman export documentation](https://learning.postman.com/docs/getting-started/importing-and-exporting/exporting-data)
