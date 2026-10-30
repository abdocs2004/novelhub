# NovelHub Frontend — React + Vite + Tailwind

واجهة المستخدم لمنصة الروايات. مشروع مستقل تمامًا عن الـ Backend (الموجود في مجلد `novelhub-backend` منفصل). الاتصال بينهم بس عن طريق HTTP (الـ API).

## المتطلبات

- **Node.js 20+** (نزّله من nodejs.org، نسخة LTS). اتأكد بـ `node -v`.
- الـ **Backend شغال** على `http://localhost:8080` (شوف `README.md` بتاع `novelhub-backend`).

## التشغيل

```bash
npm install
npm run dev
```

افتح **http://localhost:5173**

لو الـ Backend على عنوان تاني: انسخ `.env.example` إلى `.env` وعدّل:

```
VITE_API_URL=http://localhost:8080/api
```

(بعد تعديل `.env` أعد تشغيل `npm run dev`.)

## حسابات للتجربة

- **أدمن:** يتم إنشاؤه تلقائيًا عند تشغيل الـ Backend باستخدام `ADMIN_EMAIL` و`ADMIN_PASSWORD`
- **مستخدم عادي:** اعمل حساب من صفحة Register.

## الصفحات

| الصفحة | المسار | الوصول |
|---|---|---|
| Home | `/` | الكل |
| Login / Register | `/login` `/register` | الكل |
| Novel Details | `/novels/:novelId` | الكل (تشوف الفصول وأسعارها) |
| Chapter Reading | `/novels/:novelId/chapters/:chapterId` | مسجل دخول (والـ Backend يقرر لو الفصل مسموح) |
| My Library | `/library` | مسجل دخول |
| Admin Dashboard | `/admin` | ADMIN |
| Manage Novels | `/admin/novels` | ADMIN |
| Manage Chapters | `/admin/novels/:novelId/chapters` | ADMIN |

## الهيكل

```
src/
├── api/
│   ├── client.js        # Axios: base URL + إضافة JWT لكل طلب + التعامل مع 401
│   └── index.js         # دوال الـ API (authApi, novelApi, chapterApi, purchaseApi, adminApi)
├── context/AuthContext.jsx   # حالة تسجيل الدخول (user + login/register/logout)
├── hooks/useAsync.js         # تحميل بيانات مع loading/error
├── components/               # Navbar, ProtectedRoute, Cover, ui.jsx
├── pages/                    # الصفحات (admin/ للوحة التحكم)
├── App.jsx                   # الـ Routes
└── index.css                 # ألوان وخطوط Tailwind
```

## إزاي الفرونت بيتكلم مع الباك؟

1. `Login.jsx` ← `authApi.login()` ← `POST /api/auth/login`، والـ Backend يرجع **JWT**.
2. `AuthContext` يحفظ الـ token في `localStorage`.
3. **Axios interceptor** (`api/client.js`) بيضيف `Authorization: Bearer <token>` لكل طلب بعد كده.
4. لو الـ Backend رد **401** (token منتهي) الـ interceptor بيعمل logout.
5. `ProtectedRoute` بيخفي صفحات الأدمن عن غير الأدمن، **لكن ده للـ UX فقط**؛ الحماية الحقيقية في الـ Backend. حتى لو حد فتح رابط الفصل مباشرة، الـ Backend يرد 403 ومفيش نص بيوصله، وصفحة القراءة تعرض شاشة "Chapter locked" مع زر الشراء.

## ملاحظات

- **CORS:** الـ Backend مضبوط يسمح بـ `http://localhost:5173` بس. لو شغلت الـ Frontend على بورت تاني، غيّر `CORS_ALLOWED_ORIGINS` في الـ Backend.
- **Build للنشر:** `npm run build` ← يطلع مجلد `dist/`، وتجربه بـ `npm run preview`.
- الكود JavaScript عادي (مش TypeScript) عشان الفرونت مش تركيزنا هنا.
