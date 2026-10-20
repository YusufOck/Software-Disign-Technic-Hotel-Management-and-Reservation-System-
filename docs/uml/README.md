# Hotel Management System: UML diyagram dizini

Bu klasorde her diyagramin iki duzenlenebilir kaynagi bulunur:

- **PlantUML (.puml)**: UML sinif, sequence, bilesen ve dagitim diyagramlarinin kaynaklari. Sunum/PDF icin PNG ciktisi rendered/ altindadir.
- **Mermaid (.mmd)**: Markdown ve Mermaid Live Editor gibi araclarda duzenlenebilen esdeger kaynak.

01 tum hedef sistemin tek, butunlesik sinif diyagramidir. 02 ve 03 ana akislarin sequence diyagramlaridir. 04–09, gereksinim #6 icin uc mimari secenegin bilesen ve dagitim diyagramlaridir. **06–07 hedef final mikroservis mimarisini gosterir; diyagram, servisin su anda uygulanmis oldugu iddiasi degildir.**

| No | Icerik | PlantUML | Mermaid | PNG |
|---|---|---|---|---|
| 01 | Tum sistemin sinif diyagrami | [01-sinif-diyagrami.puml](01-sinif-diyagrami.puml) | [01-sinif-diyagrami.mmd](01-sinif-diyagrami.mmd) | [01-sinif-diyagrami.png](rendered/01-sinif-diyagrami.png) |
| 02 | Rezervasyon ve odeme sequence | [02-rezervasyon-ve-odeme.puml](02-rezervasyon-ve-odeme.puml) | [02-rezervasyon-ve-odeme.mmd](02-rezervasyon-ve-odeme.mmd) | [02-rezervasyon-ve-odeme.png](rendered/02-rezervasyon-ve-odeme.png) |
| 03 | Check-in ve check-out sequence | [03-checkin-checkout.puml](03-checkin-checkout.puml) | [03-checkin-checkout.mmd](03-checkin-checkout.mmd) | [03-checkin-checkout.png](rendered/03-checkin-checkout.png) |
| 04 | Monolit bilesen | [04-monolit-bilesen.puml](04-monolit-bilesen.puml) | [04-monolit-bilesen.mmd](04-monolit-bilesen.mmd) | [04-monolit-bilesen.png](rendered/04-monolit-bilesen.png) |
| 05 | Monolit dagitim | [05-monolit-dagitim.puml](05-monolit-dagitim.puml) | [05-monolit-dagitim.mmd](05-monolit-dagitim.mmd) | [05-monolit-dagitim.png](rendered/05-monolit-dagitim.png) |
| 06 | Secilen mikroservis bilesen | [06-mikroservis-bilesen.puml](06-mikroservis-bilesen.puml) | [06-mikroservis-bilesen.mmd](06-mikroservis-bilesen.mmd) | [06-mikroservis-bilesen.png](rendered/06-mikroservis-bilesen.png) |
| 07 | Secilen mikroservis dagitim | [07-mikroservis-dagitim.puml](07-mikroservis-dagitim.puml) | [07-mikroservis-dagitim.mmd](07-mikroservis-dagitim.mmd) | [07-mikroservis-dagitim.png](rendered/07-mikroservis-dagitim.png) |
| 08 | Olay gudumlu CQRS + saga bilesen | [08-olay-gudumlu-bilesen.puml](08-olay-gudumlu-bilesen.puml) | [08-olay-gudumlu-bilesen.mmd](08-olay-gudumlu-bilesen.mmd) | [08-olay-gudumlu-bilesen.png](rendered/08-olay-gudumlu-bilesen.png) |
| 09 | Olay gudumlu CQRS + saga dagitim | [09-olay-gudumlu-dagitim.puml](09-olay-gudumlu-dagitim.puml) | [09-olay-gudumlu-dagitim.mmd](09-olay-gudumlu-dagitim.mmd) | [09-olay-gudumlu-dagitim.png](rendered/09-olay-gudumlu-dagitim.png) |

Mikroservis alternatifinde hotel-core oda, misafir, rezervasyon, fiyat, konaklama ve simule odemenin sahibidir; notification-service bildirim isleme ve kaydinin sahibidir. Her servis kendi veritabanina yazar. RabbitMQ rezervasyon olaylarini asenkron olarak aktarir. Olay gudumlu alternatif, ayrica CQRS okuma projeksiyonu ve odeme sagasi eklediginden daha fazla dagitik tutarlilik yonetimi ister.

PlantUML kaynaklarini yeniden PNG'ye cevirmek icin PlantUML JAR ile docs/uml klasorunu render edin. Kaynaklari once PlantUML --check-syntax ile, Mermaid surumlerini de Mermaid parser ile dogrulayin.
