# دليل التعلم — كيف يعمل NovelHub

اقرأ هذا الملف بجانب الكود. كل قسم يشير إلى الملفات التي تُثبت الفكرة.

> المشروع مقسوم لمجلدين منفصلين: `novelhub-backend` (هذا المجلد) و`novelhub-frontend`. المسارات التي تبدأ بـ `api/` و`context/` و`pages/` موجودة في مجلد الـ Frontend.

**ترتيب القراءة المقترح للكود:**
`entity/` ← `repository/` ← `dto/` ← `service/ChapterService.java` ← `controller/` ← `security/` + `config/SecurityConfig.java` ← `service/PurchaseService.java` ← الـ Frontend (`api/client.js` ثم `context/AuthContext.jsx` ثم الصفحات).

---

## 1) لماذا Spring Boot؟

- **Spring** هو الإطار الأكثر استخدامًا في Java للـ Backend في الشركات، و**Spring Boot** يزيل الإعداد المعقد: تضيف Dependency (مثل `spring-boot-starter-web`) فيعمل Tomcat مدمج، JSON، إعدادات افتراضية جاهزة. لا تحتاج تثبيت Tomcat ولا ملفات XML.
- **Dependency Injection:** أنت لا تكتب `new AuthService(...)`. Spring ينشئ الكائنات (Beans) ويمررها للـ constructor. انظر أي Controller: يستقبل الـ Service في الـ constructor.
- **منظومة كاملة** بنفس الأسلوب: Security, JPA, Validation, Testing.
- ملف `pom.xml` يعرّف المكتبات، و`application.yml` يعرّف الإعدادات (قاعدة البيانات، JWT ...).

## 2) كيف يمشي Request من React إلى Spring Boot؟

مثال: المستخدم يضغط "Read" على فصل.

1. React يستدعي `chapterApi.read(5)` → Axios يرسل `GET http://localhost:8080/api/chapters/5` ومعه الهيدر `Authorization: Bearer <JWT>` (يضيفه الـ interceptor في `api/client.js`).
2. المتصفح يرسل قبلها طلب **CORS preflight** لأن الـ Frontend على Origin مختلف (5173 ≠ 8080). `SecurityConfig.corsConfigurationSource()` يسمح له.
3. Tomcat يستقبل الطلب ويمرره على **Security Filter Chain**: `JwtAuthFilter` ثم قواعد `authorizeHttpRequests`.
4. `DispatcherServlet` يطابق الرابط مع `@GetMapping("/{id}")` في `ChapterController`.
5. الـ Controller يستدعي `ChapterService.read(...)`، والـ Service يستخدم Repositories للوصول للـ DB.
6. النتيجة (DTO) تتحول إلى JSON بواسطة Jackson ويرجع الرد للمتصفح.
7. لو حصل Exception في أي مكان → `GlobalExceptionHandler` يحوله لرد JSON منظم.

## 3) ماذا يفعل Controller؟

هو **بوابة HTTP** فقط: يحدد الرابط والـ method (`@GetMapping`...)، يقرأ المدخلات (`@PathVariable`, `@RequestBody`)، يفعّل الـ Validation (`@Valid`)، **يستدعي Service واحدًا**، ويرجع النتيجة. لا يحتوي منطق عمل ولا كود قاعدة بيانات. جرّب أن تقرأ `ChapterController`: 3 أسطر فعلية.

`@AuthenticationPrincipal UserPrincipal` يعطيك المستخدم الحالي (من الـ JWT) مباشرة كمعامل.

## 4) ماذا يفعل Service؟

هو **قلب المنطق (Business Logic)**: القواعد والقرارات. مثال: "هل يستطيع هذا المستخدم قراءة هذا الفصل؟" في `ChapterService.assertCanRead`، و"هل الشراء مسموح؟" في `PurchaseService.purchase`.

- `@Transactional`: كل الـ Service method تُنفذ في Transaction واحدة (نجاح كامل أو تراجع كامل).
- الـ Service هو من يحول Entity → DTO، بينما الـ Session مفتوح (مهم بسبب الـ Lazy Loading، انظر القسم 8).

## 5) ماذا يفعل Repository؟

طبقة الوصول للبيانات. أنت تكتب **interface فقط**، و Spring Data JPA ينشئ التنفيذ:

```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);   // يُبنى الاستعلام من اسم الدالة
}
```
- `JpaRepository` يعطيك `save / findById / findAll / delete / count`.
- أسماء الدوال تتحول لـ SQL: `existsByUserIdAndChapterIdAndStatus(...)`.
- للاستعلامات الأعقد تستخدم `@Query` بـ JPQL (انظر `PurchaseRepository`، و`join fetch` لتفادي مشكلة N+1).

## 6) كيف يعمل JPA / Hibernate؟

- **JPA** مواصفة، و**Hibernate** هو التنفيذ الفعلي. تربط class بجدول: `@Entity` ← صف في الجدول، حقل ← عمود.
- عند تشغيل التطبيق مع `ddl-auto: update` ينشئ Hibernate الجداول من الـ Entities.
- **Persistence Context:** داخل الـ `@Transactional`، الكائنات التي تحملها من الـ DB تُسمى "managed". لو غيّرت حقلًا فيها، Hibernate يكتشف التغيير وينفذ `UPDATE` عند الـ commit **بدون `save()`**. شاهد `NovelService.update`.
- **Lazy Loading:** `@ManyToOne(fetch = LAZY)` يعني: لا تحمّل الرواية مع الفصل إلا عند طلبها. لو طلبتها خارج الـ Transaction تحصل `LazyInitializationException`. لذلك نحول لـ DTO داخل الـ Service، ونضبط `open-in-view: false`.
- **N+1 problem:** لو جلبت 50 عملية شراء ثم لكل واحدة استعلام للفصل والرواية = 101 استعلام. الحل في `PurchaseRepository.findByUser`: `join fetch`.

## 7) كيف يتم الاتصال بـ PostgreSQL؟

1. مكتبة `postgresql` (JDBC driver) في `pom.xml`.
2. `application.yml`: `spring.datasource.url / username / password`.
3. Spring Boot ينشئ **DataSource** مع **Connection Pool (HikariCP)** تلقائيًا: مجموعة اتصالات جاهزة تُعاد استخدامها بدل فتح اتصال لكل طلب.
4. Hibernate يكتشف الـ Dialect (PostgreSQL) ويولّد SQL مناسب.

جرب `spring.jpa.show-sql: true` لترى الـ SQL الذي يولّده Hibernate لكل استعلام. من أفضل طرق التعلم.

## 8) كيف ترتبط الـ Entities ببعضها؟

```
Novel 1 ────< Chapter          (Chapter.novel  = @ManyToOne  ← عمود novel_id هو الـ Foreign Key)
User  1 ────< Purchase >──── 1 Chapter
```
- **Owning side vs inverse side:** الطرف الذي فيه `@JoinColumn` (الـ FK) هو المالك (`Chapter.novel`). الطرف الآخر `Novel.chapters` يستخدم `mappedBy = "novel"` ولا ينشئ عمودًا.
- `cascade = ALL` + `orphanRemoval = true` في `Novel.chapters`: حفظ/حذف الرواية يحفظ/يحذف فصولها (استخدمناه في `DataInitializer` لحفظ رواية مع 3 فصول بأمر `save` واحد).
- `Purchase` كيان مستقل (وليس `@ManyToMany`) لأن العلاقة تحمل بيانات: السعر، الحالة، التاريخ.
- لماذا `BigDecimal` للسعر؟ لأن `double` يخطئ في الكسور العشرية (0.1 + 0.2)، ولا نقبل ذلك مع المال.
- `@Enumerated(EnumType.STRING)`: نخزن اسم القيمة ("ADMIN") وليس رقمها، حتى لا تفسد البيانات لو غيّرنا ترتيب الـ enum.

## 9) كيف يعمل JWT؟

**JWT** = ثلاثة أجزاء `header.payload.signature`.

1. `POST /auth/login` بالإيميل والباسورد → السيرفر يتحقق → يولد token في `JwtService.generateToken` يحتوي: `sub` (الإيميل)، `role`، `iat`، `exp`.
2. السيرفر **يوقّع** الـ token بمفتاح سري (`app.jwt.secret`). لا يخزن أي Session.
3. الـ Frontend يحفظ الـ token ويرسله مع كل طلب: `Authorization: Bearer <token>`.
4. `JwtAuthFilter` يتحقق: هل التوقيع صحيح؟ هل انتهت الصلاحية؟ (`JwtService.extractEmail`). لو تم تعديل حرف في الـ payload يفشل التحقق.

نقاط مهمة:
- الـ payload **مشفّر Base64 فقط وليس Encryption**: يستطيع أي شخص قراءته (جرب لصق token في jwt.io). لا تضع فيه أسرارًا.
- **Stateless**: أي نسخة من السيرفر تقدر تتحقق من الـ token بنفس الـ secret، فيسهل التوسع.
- الضعف: لا يمكن "إلغاء" token قبل انتهائه بسهولة (لذلك تُستخدم مدة قصيرة + Refresh Token في الأنظمة الحقيقية).
- في مشروعنا الـ Filter يقرأ المستخدم من الـ DB في كل طلب، فلو تغير الـ role أو حُذف المستخدم يظهر أثر ذلك فورًا.

## 10) كيف تعمل Spring Security؟

Spring Security = **سلسلة Filters** قبل وصول الطلب للـ Controller. عندنا (في `SecurityConfig`):

1. `JwtAuthFilter` (مكتوب بيدنا): يحول الـ token إلى "مستخدم مصادق عليه" داخل `SecurityContext`.
2. **قواعد الصلاحيات** (من الأعلى للأسفل، أول قاعدة تطابق تنجح):
   ```java
   .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
   .requestMatchers(GET, "/api/novels", "/api/novels/**").permitAll()
   .requestMatchers("/api/admin/**").hasRole("ADMIN")
   .anyRequest().authenticated()
   ```
3. فشل الصلاحيات: **401** (لست مسجل دخول) أو **403** (مسجل لكن غير مسموح) عبر `RestSecurityExceptionHandler`.

مصطلحات:
- **Authentication** = من أنت؟ (login + JWT). **Authorization** = ماذا مسموح لك؟ (roles + قواعد الفصول).
- `PasswordEncoder` (BCrypt): نخزن hash مملّح، لا الباسورد نفسه. لا يمكن عكسه.
- `SessionCreationPolicy.STATELESS`: لا Session ولا Cookie، لذلك عطّلنا CSRF (CSRF خطر الـ cookies).
- `UserDetailsService` هو ما يخبر Spring كيف يجد المستخدم (`CustomUserDetailsService`).
- `hasRole("ADMIN")` يبحث عن authority اسمها `ROLE_ADMIN` (نضيف البادئة في `UserPrincipal`).

## 11) كيف يتم التحقق من صلاحية المستخدم لقراءة الفصل؟

هذا أهم جزء. الكود في `ChapterService.read` + `assertCanRead`:

| الخطوة | من يفحصها | النتيجة عند الفشل |
|---|---|---|
| 1. هل المستخدم مسجل دخول؟ | Spring Security (`anyRequest().authenticated()`) | 401 |
| 2. هل الفصل مجاني؟ (`price == 0`) | `ChapterService` | يكمل ويرجع المحتوى |
| 3. هل هو أدمن؟ | `ChapterService` | يكمل ويرجع المحتوى |
| 4. هل يوجد `Purchase` بحالة SUCCESS لهذا (user, chapter)؟ | `PurchaseRepository.existsBy...` | — |
| 5. لا | `throw ChapterLockedException` | **403 + `CHAPTER_LOCKED` بدون محتوى** |

لماذا هذا آمن؟
- الفحص **قبل** بناء الـ Response: كائن `ChapterContentResponse` لا يُنشأ أصلًا لو لا يوجد صلاحية.
- **قائمة الفصول** (`GET /novels/{id}`) تستخدم DTO آخر `ChapterSummaryResponse` **بدون حقل content**. لا يوجد أي طريق آخر يعيد النص.
- الـ `userId` يؤخذ من الـ JWT، **لا من الـ request body** (لا يستطيع المستخدم ادعاء هوية غيره).
- الـ `unlocked` الذي يراه React مجرد تسهيل للـ UI؛ لو غيّره المستخدم في الـ DevTools يبقى الـ Backend يرفض.

تجربة: افتح DevTools ونفّذ طلب `GET /api/chapters/<id مدفوع>` بمستخدم لم يشتر. سترى 403 ولا شيء من النص.

## 12) كيف يتم تنفيذ عملية الشراء؟

`POST /api/purchases/{chapterId}` ← `PurchaseController` ← `PurchaseService.purchase` (داخل `@Transactional`):

1. جلب الفصل (404 لو غير موجود).
2. لو مجاني → 400 (لا شيء ليُشترى).
3. لو المستخدم يملكه فعلًا → 409.
4. **السعر يؤخذ من الـ DB** (`chapter.getPrice()`) وليس من الـ Frontend (لأن العميل يمكنه إرسال أي رقم).
5. `paymentService.charge(...)`.
6. حفظ `Purchase` بحالة `SUCCESS` (أو `FAILED`)، مع نسخة من السعر وقت الشراء.
7. الرد: `201` عند النجاح أو `402` عند الفشل.

**كيف نستبدل الـ Mock ببوابة حقيقية؟** `PurchaseService` يعتمد على **interface** اسمه `PaymentService`، وليس على `MockPaymentService`. لإضافة Stripe مثلًا:

```java
@Service
@ConditionalOnProperty(name = "app.payment.provider", havingValue = "stripe")
public class StripePaymentService implements PaymentService { ... }
```
ثم `app.payment.provider: stripe`. لا تغيير في `PurchaseService` ولا الـ Controller ولا الـ Frontend. هذا مبدأ **Dependency Inversion** عمليًا.

---

## أخطاء شائعة ستقابلها

| الرسالة | السبب |
|---|---|
| `Connection refused` / `password authentication failed` | PostgreSQL غير مشغل أو بيانات `application.yml` خطأ |
| `WeakKeyException` عند التشغيل | `JWT_SECRET` أقل من 32 بايت |
| CORS error في المتصفح | `CORS_ALLOWED_ORIGINS` لا يطابق عنوان الـ Frontend بالضبط |
| `LazyInitializationException` | قراءة علاقة `LAZY` خارج `@Transactional` |
| 401 دائمًا | الهيدر يجب أن يكون `Authorization: Bearer <token>` (مع مسافة) |
| 403 لأدمن | تأكد أن العمود `role` قيمته `ADMIN` بالضبط |

## تمارين مقترحة (بالترتيب)

1. أضف `show-sql: true` وراقب الاستعلامات عند فتح صفحة رواية.
2. أضف حقل `createdAt` للفصل وأظهره في الـ Frontend.
3. اكتب اختبار `MockMvc`: مستخدم بدون شراء يحصل على 403 من `/api/chapters/{id}`.
4. أضف Pagination لـ `GET /api/novels` (`Pageable`).
5. أضف تقييمات للروايات (`Review`): Entity جديد وعلاقة `@ManyToOne` مع User و Novel.
6. اجعل `MockPaymentService` يفشل أحيانًا وشاهد كيف يتصرف `PurchaseService` وحالة `FAILED`.
7. استبدل Mock بـ Stripe Checkout + Webhook + حالة `PENDING`.
