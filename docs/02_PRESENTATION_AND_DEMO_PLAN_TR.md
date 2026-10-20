# Sunum ve Demo Plani

PDF sunum suresi vermedigi icin bu akis 12-15 dakikalik orta donem ve 10-12 dakikalik final sunumuna gore hazirlanmistir. Gercek sureye gore slayt sayisi azaltilebilir; rubrik sirasi korunmalidir.

## Donem ortasi sunumu: gereksinim 1-6

### Slayt 1 - Problem ve kapsam

- Hotel sisteminin bes fonksiyon grubu.
- En kritik iki risk: cakisan rezervasyon ve degisen fiyat kurallari.
- Spring Boot ve PoC siniri.

### Slayt 2 - Domain kurallari

- `[checkIn, checkOut)` tarih modeli.
- Cakisma formulu.
- Reservation ve Room status gecisleri.

### Slayt 3 - Dort pattern karari

Tek tabloda Strategy, Decorator, Observer, Facade; her biri icin problem ve katilimcilar.

### Slayt 4 - Patternlerin ayni akista etkileşimi

BookingFacade -> Availability -> Pricing Strategy -> Extra-service Decorators -> Payment port -> Reservation Event -> Observers.

### Slayt 5 - Birlesik class diagram

Ayri pattern diyagramlari gostermeyin. Tek diyagram uzerinde renk/not ile dort desen.

### Slayt 6 - Sequence: create and pay reservation

Happy path ve iki `alt`: room unavailable, payment failed.

### Slayt 7 - Sequence: check-in/check-out

Reservation ve room statuslarinin birlikte degisimi ve bildirim.

### Slayt 8 - PoC canli demo

1. Uygunluk aramasi.
2. Rezervasyon ve fiyat sonucu.
3. Confirmation observer logu.
4. Ayni tarihte ikinci rezervasyonun reddi.

### Slayt 9 - Uc mimari

Monolith, REST odakli microservices, event-driven. Her birinden yalnizca temel topology/data flow.

### Slayt 10 - Karsilastirma ve karar

Agirlikli matris. Microservices + selective events karari.

### Slayt 11 - Final plani

Uc servis, RabbitMQ, Docker, Postman ve CI/CD.

## Final sunumu: gereksinim 7-9

### Slayt 1 - Hedef mimari

Inventory, Booking, Notification; ayri veri sahipligi.

### Slayt 2 - Senkron rezervasyon akisi

Hold, payment, confirm/release ve compensation.

### Slayt 3 - Asenkron bildirim akisi

Exchange, routing key, queue, consumer, idempotency ve DLQ.

### Slayt 4 - Docker deployment

Containerlar, portlar, DB'ler, RabbitMQ ve health checks.

### Slayt 5 - Test stratejisi

Unit, PostgreSQL concurrency, RabbitMQ integration ve Postman/Newman.

### Slayt 6 - CI/CD

Build -> test -> image -> Compose -> health -> Newman.

### Slayt 7 - Canli demo

1. `docker compose up` ve health.
2. Reservation happy path.
3. Notification eventually visible.
4. Concurrent conflict.
5. Failed payment compensation veya cancel.

### Slayt 8 - Rubrik kaniti

Gereksinim 7, 8 ve 9 icin repo dosyasi + canli sonuc eslesmesi.

## Demo scripti

Demo oncesi seed verisi hazir olmalidir. Ad hoc veri girmeye zaman harcanmaz.

1. `docker compose ps` ile butun servisleri healthy gosterin.
2. Postman `Setup` klasorunu calistirin.
3. Availability request ile bir oda bulun.
4. Reservation request yapin; response'ta reservation ID, room ID ve total price gosterin.
5. Notification endpointini kisa polling ile kontrol edin; confirmation kaydini gosterin.
6. Ayni oda ve tarihe iki paralel istek atarak birinin 201, digerinin 409 oldugunu gosterin.
7. Check-in ve check-out yapin; oda ve rezervasyon durumlarini gosterin.
8. GitHub Actions'taki basarili pipeline run'ini gosterin.

## Konusma bolumu

### Uye A

- Scope ve domain riskleri.
- Mimari alternatifler ve karar.
- Microservices/Docker/CI.
- Concurrent reservation demo.

### Uye B

- Patternler ve class diagram.
- Sequence diagramlar.
- RabbitMQ/notification.
- Postman ve traceability.

Ikisi de birbirinin bolumunu anlatabilecek kadar prova yapmalidir. Hoca bir sinifi veya mesaji sectiginde diger uye de cevap verebilmelidir.

## Muhtemel hoca sorulari

### Neden Strategy?

Fiyat algoritmalari sik degisir ve ayni rezervasyonda farkli kurallar uygulanabilir. Strategy algoritmalari ayri test eder ve yeni kural eklerken booking akisini degistirmez.

### Neden Observer ve RabbitMQ ikisi birden?

Observer nesne seviyesinde publisher-subscriber iliskisini kanitlar. Finalde ayni kavram process sinirini asar; RabbitMQ servisleri zamansal olarak da ayirir. Dersin design pattern ve architecture style seviyeleri ayridir.

### Neden microservices secildi?

Gereksinim 7 dogrudan microservices ister. Domain icinde notification bagimsizdir. Inventory ise cakisma ve oda durumunu tek sahipte tutar. Uc servis yeterli ayrimi gosterirken iki kisilik ekibin bitirebilecegi olcekte kalir.

### Neden her seyi RabbitMQ ile yapmadiniz?

Guest rezervasyonun kabul veya red sonucunu aninda bekler. Availability/hold kritik ve tutarli cevap gerektirir. Notification gecikmeye toleranslidir, bu nedenle asenkron queue icin daha uygundur.

### Ayni odayi iki kisi ayni anda alirsa ne olur?

Inventory service oda satirini transaction icinde pessimistic write lock ile kilitler, cakismayi kilit altinda kontrol eder ve allocation ekler. Paralel integration testi yalnizca bir istegin basarili oldugunu kanitlar.

### Servislerden biri cokerse ne olur?

Notification kapaliyken RabbitMQ durable queue mesaji saklar. Inventory yoksa booking yeni rezervasyonu onaylamaz ve anlasilir hata doner. Payment sonrasi confirm basarisizligi icin retry/compensation davranisi belgelenir.

### Database neden paylasilmiyor?

Paylasilan tablolar servislerin bagimsiz gelismesini bozar. Her servis kendi verisini sahiplenir; diger servis bu veriye API veya event ile ulasir.

## Yedek demo plani

- Her canli adimin onceden kaydedilmis terminal/Postman ekran goruntusu bulunur.
- Docker image'lari sunumdan once build edilir.
- Internet bagimliligi olmayan local Compose kullanilir.
- Postman environment ID'leri otomatik zincirler.
- RabbitMQ management UI bookmark'i hazirdir.
- Canli demo bozulursa kod, test sonucu ve CI logu uzerinden ayni kanit gosterilir.

