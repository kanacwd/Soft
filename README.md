# Student Complaint Resolution System (SCRS)

## Описание
Комплексная система управления жалобами студентов для AIU. Система позволяет студентам подавать жалобы, персоналу обрабатывать их, а администраторам управлять всем процессом.

## Технологии
- **Backend**: Spring Boot 3.2.0, Java 17, Spring Security, JWT
- **Frontend**: HTML, CSS, JavaScript
- **База данных**: H2 (in-memory), PostgreSQL (production)
- **Сборка**: Maven

## Структура проекта
```
/
├── backend/              # Spring Boot приложение
│   ├── src/main/java/    # Java код
│   └── src/main/resources/ # Конфигурация
├── frontend/             # Статические файлы
│   ├── pages/           # HTML страницы
│   ├── js/              # JavaScript файлы
│   └── css/             # CSS стили
└── .gitignore           # Git игнор файл
```

## Запуск в IntelliJ IDEA

### 1. Импорт проекта
1. Откройте IntelliJ IDEA
2. Выберите "Open" (не "New Project")
3. Выберите папку `/Users/mac/Desktop/soft`
4. Выберите "Trust Project"

### 2. Настройка проекта
1. Убедитесь, что выбран Java 17 SDK:
   - File → Project Structure → Project SDK → 17+
2. Maven автоматически определит зависимости из `backend/pom.xml`

### 3. Запуск Backend
1. Откройте терминал в IntelliJ IDEA
2. Перейдите в папку backend: `cd backend`
3. Выполните: `mvn spring-boot:run`
4. Backend запустится на http://localhost:8080

### 4. Запуск Frontend
1. Откройте новый терминал в IntelliJ IDEA
2. Перейдите в папку frontend: `cd frontend`
3. Выполните: `python3 -m http.server 8081`
4. Frontend запустится на http://localhost:8081

### 5. Доступ к приложению
- Откройте браузер и перейдите на http://localhost:8081

## API Endpoints

### Аутентификация
- `POST /api/auth/register` - Регистрация пользователя
- `POST /api/auth/login` - Вход в систему

### Управление жалобами
- `GET /api/complaints` - Получить все жалобы
- `POST /api/complaints` - Создать жалобу
- `PUT /api/complaints/{id}` - Обновить жалобу
- `DELETE /api/complaints/{id}` - Удалить жалобу

### Голосование
- `POST /api/votes/complaints/{id}` - Голосовать за жалобу
- `DELETE /api/votes/complaints/{id}` - Удалить голос

### Администрирование
- `GET /api/admin/users` - Получить всех пользователей
- `GET /api/admin/departments` - Получить все отделы
- `POST /api/admin/departments` - Создать отдел

## Конфигурация

### JWT Settings
JWT секретный ключ настраивается в `backend/src/main/resources/application.properties`:
```properties
jwt.secret=MyVerySecureAndLongJWTSecretKeyThatIsAtLeast512BitsLongForSecurityPurposesAndComplianceWithJWTStandards123456789012345678901234567890123456789012345678901234567890
jwt.expiration=86400000
```

### База данных
Для разработки используется H2 (in-memory). Для production настройте PostgreSQL в `application-prod.properties`.

## Роли пользователей
- **STUDENT** - Студенты могут подавать жалобы и голосовать
- **STAFF** - Персонал может обрабатывать жалобы
- **ADMIN** - Администраторы могут управлять системой

## Разработка

### Сборка проекта
```bash
cd backend
mvn clean package
```

### Тестирование
```bash
cd backend
mvn test
```

### Логи
Логи настраиваются через `logback-spring.xml`. По умолчанию включен DEBUG уровень для `com.aiu.scrs`.

## Поддержка
Для решения проблем проверьте:
1. Логи в консоли IntelliJ IDEA
2. Настройки портов (8080 для backend, 8081 для frontend)
3. Правильность Java версии (17+)
4. Наличие всех зависимостей Maven

## Лицензия
AIU Internal Use
