# Info
- **Стратегии наследования в Hibernate**.
- Простые примеры кода.

### Модуль example1_mapped_superclass
1. **Наследование с _@MappedSuperclass_. Аннотация _@MappedSuperclass_ позволяет вынести общие поля в родительский класс, но при этом
не создавать для него отдельную таблицу. При такой стратегии классы-наследники преобразуются
в независимые таблицы. _@MappedSuperclass_ никак не влияет на структуру в базе — это просто способ
вынести общие поля**.

2. Структура модуля:  
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/example1/00_structure.png)  

3. Создаём родительский класс _Person_:  
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/example1/01_parent_class.png)  
   
_Person_ — не сущность, в него просто вынесены общие поля сущностей _Employee_ и _Customer_.
Класс _Person_ можно сделать **абстрактным**. Этот класс можно было бы назвать _AbstractEntity_.
Ассоциаций (_@ManyToOne_, _@OneToMany_ и т.д.) с ним сделать нельзя!

**GenerationType.SEQUENCE** ("_sequence_" - "_последовательность_") - это специальный тип для генерации
значений из последовательности. Создаёте в базе данных, которая его поддерживает, например Postgres:  
`CREATE SEQUENCE my_seq START WITH 100000;`  
Используете в качестве генератора.

`allocationSize` ("_allocation_" - "_распределение_") указывает Hibernat-у сколько нужно резервировать значений
из последовательности за одно обращение к базе данных. В данном случае на каждое обращение - 1 значение. Чем больше,
тем сильнее снижается нагрузка на базу данных, но больше расходуется последовательность (иногда впустую).

4. Дочерние классы _Employee_ и _Customer_:  
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/example1/02_03_employee_and_customer.png)  

5. Использована БД _Postgres_ в контейнере _Docker_. Настройки контейнера указываем в файле `docker-compose.yaml`:  
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/example1/04_docker_compose.png)  

6. Настройки подключения к БД прописываем в файле `application.yaml`:  
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/example1/05_application.png)  

In Spring/Spring-Boot, SQL database can be initialized in different ways depending on what your stack is.
JPA has features for DDL generation, and these can be set up to run on startup against the database.
This is controlled through two external properties:
- `spring.jpa.generate-ddl` (boolean) switches the feature on and off and is vendor independent.
- `spring.jpa.hibernate.ddl-auto` (enum) is a Hibernate feature that controls the behavior
in a more fine-grained way. See below for more detail.

You can set `spring.jpa.hibernate.ddl-auto` explicitly and the standard Hibernate property values are:
- `create` – Hibernate first drops existing tables, then creates new tables.
- `update` – the object model created based on the mappings (annotations or XML) is compared with the existing schema,
and then Hibernate updates the schema according to the diff. It never deletes the existing tables or columns
even if they are no more required by the application.
- `create-drop` – similar to create, with the addition that Hibernate will drop the database after all operations
are completed. Typically used for unit testing.
- `validate` – Hibernate only validates whether the tables and columns exist, otherwise it throws an exception.
- `none` – this value effectively turns off the DDL generation.

Spring Boot internally defaults this parameter value to `create-drop` if no schema manager has been detected,
otherwise `none` for all other cases.

**В продакшене лучше выставлять значения `false` и `none`!**

7. Главный класс выглядит так:  
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/example1/06_main_class.png)  

Spring allows us to attach custom actions to _bean creation and destruction_. We can, for example, do it
by implementing the _InitializingBean_ and _DisposableBean_ interfaces. A second possibility: the _@PostConstruct_
and _@PreDestroy_ annotations.

**Spring calls methods annotated with @PostConstruct only once, just after the initialization of bean properties**.
Keep in mind that these methods will run even if there is nothing to initialize.
The method annotated with _@PostConstruct_ can have any access level but it can't be static.
One example usage of @PostConstruct is populating a database. During development, for instance,
we might want to create some default users:

    @Component
    public class DbInit {
        
        @Autowired
        private UserRepository userRepository;
    
        @PostConstruct
        private void postConstruct() {
            User admin = new User("admin", "admin password");
            User normalUser = new User("user", "user password");
            userRepository.save(admin, normalUser);
        }
    }

The above example will first initialize _UserRepository_ and then run _@PostConstruct_ method.

**A method annotated with @PreDestroy runs only once, just before Spring removes our bean from the application context**.
Same as with _@PostConstruct_, the methods annotated with _@PreDestroy_ can have any access level but can't be static.

    @Component
    public class UserRepository {
    
        private DbConnection dbConnection;
        
        @PreDestroy
        public void preDestroy() {
            dbConnection.close();
        }
    }

The purpose of this method should be to release resources or perform any other cleanup tasks
before the bean gets destroyed, for example closing a database connection.

8. Далее настроить подключение к БД на вкладке Database:  
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/example1/07_data_source.png)  

9. Запускаем приложение:  
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/example1/08_app_running.png)  

10. Теперь смотрим созданные таблицы. Сгенерированы 2 независимые таблицы (внешних ключей нет, только первичные).
Они включают поля родительского класса и свои:  
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/example1/09_tables_structure.png)  
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/example1/10_quick_documentation.png)  

11. В итоге сохранять можно только дочерние сущности — они пойдут в независимые таблицы
и будут включать поля родительской сущности. Отдельно _Customer_ не сохранить, это абстракция, в которую
вынесена часть полей.

### Модуль example2_table_per_class
1. **Рассмотрим стратегию наследования _InheritanceType.TABLE_PER_CLASS_. При такой стратегии классы преобразуются
в независимые таблицы. Сохранять можно все сущности (родительские и дочерние) — они пойдут в независимые таблицы**.

Заметим, что при стратегии наследования _InheritanceType.TABLE_PER_CLASS_ нельзя использовать такую стратегию
генерации идентификатора: `@GeneratedValue(strategy = GenerationType.IDENTITY)` - такая стратегия задаёт
автоинкрементное поле. В данном примере используем `@GeneratedValue(strategy = GenerationType.SEQUENCE)` — так можно.

2. Родительский класс _Person_:  
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/example2/01_class_person.png)  

3. Дочерние классы _Employee_ и _Customer_:  
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/example2/02_employee_and_customer.png)  

4. Главный класс выглядит так:  
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/example2/03_main_class.png)  

5. Запускаем приложение:  
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/example2/04_run.png)  

6. Генерируются такие независимые таблицы (внешних ключей нет, только первичные):  
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/example2/05_three_tables.png)    
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/example2/06_tables.png)  

7. Рассмотрим SELECT из родительской сущности. В этом случае выборка сгенерирует сложный SQL.

Создадим метод с нужным запросом в репозитории:  
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/example2/07_repo.png)  

Далее используем этот метод:  
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/example2/08_using_select.png)  

В итоге опять запускаем приложение, и смотрим запрос, который сгенерирует Hibernate:  
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/example2/09_run_and_select.png)  

### Модуль example3_joined
1. **Рассмотрим тип наследования _JOINED_. Аннотация _@Inheritance_ со значением _InheritanceType.JOINED_ говорит
о том, что будет общая таблица для хранения общих данных, плюс данные каждого наследника тоже будут храниться
в отдельной таблице**.

2. Класс _Person_ содержит общие данные, такие как имя (имя есть у всех):  
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/example3/01_class_person.png)  

3. Наследники (_Employee_ и _Customer_):  
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/example3/02_classes_employee_and_customer.png)  

4. Главный класс выглядит так:  
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/example3/03_main_class.png)  

5. Запускаем приложение:  
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/example3/04_run.png)  

6. Схема в базе данных: вышеприведенные классы генерируют такую структуру в базе данных:  
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/example3/05_tables.png)  
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/example3/06_quick_doc.png)  
**Первичные ключи** `employees.id` и `customers.id` являются заодно и **внешними**: они ссылаются на `persons.id`.

7. Были созданы 2 сущности и сохранены в БД. В консоли видно, что
генерируется 4 оператора _insert_ (ещё заполняется общая таблица):
!!!  ДОБАВИТЬ СКРИН !!!!!!!

8. Запрос, который сгенерирует Hibernate, для получения списка всех людей:  
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/example3/07_select.png)  

### Модуль example4_single_table
1. **Рассмотрим стратегию наследования _SINGLE_TABLE_. Стратегия наследования указывается в аннотации _@Inheritance_
родительского класса _Person_. В данном случае пишем просто _@Inheritance_, потому что _InheritanceType.SINGLE_TABLE_
является стратегией по умолчанию. Но можно было бы в аннотации явно
указать _@Inheritance(strategy = InheritanceType.SINGLE_TABLE)_. Результат был бы тот же. Как понятно из названия,
все сущности теперь будут храниться в одной таблице. Этот способ самый эффективный с точки зрения производительности,
так как с одной таблицей работать быстрее, чем с несколькими**.

2. Родительский класс:  
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/example4/01_class_person.png)  

3. Наследники:  
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/example4/02_employee_and_customer.png)  

4. Главный класс:  
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/example4/03_main_class.png)  

5. Запускаем приложение:  
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/example4/04_run.png)  

6. Схема в базе данных. В итоге генерируется такая схема, состоящая из одной таблицы:  
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/example4/05_db.png)  
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/example4/06_structure.png)  
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/example4/07_quick_doc.png)  
Как видим, все сущности хранятся в одной таблице _person_. Но как же подклассы различаются?
С помощью **столбца-дискриминатора DTYPE**: при генерации схемы Hibernate добавляет
столбец DTYPE — это столбец-дискриминатор. Он показывает, к какому классу принадлежит
сущность: _Customer_ или _Employee_.

7. Были созданы 2 сущности и сохранены в БД. В консоли видно, что генерируется два оператора _insert_:  
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/example4/08_insert.png)  
тогда как в предыдущем примере было 4 оператора _insert_ — ещё заполнялась общая таблица,
что гораздо менее эффективно.

8. **Недостаток — запрет _@NotNull_**: в таблице _person_ есть как общие для всех сущностей
столбцы — поля класса _Person_, так и столбцы подклассов _Customer_ и _Employee_. Столбцы подклассов
не всегда заполнены: если заказчик _Customer_, то столбцы, зарезервированные под _Employee_, остаются пустыми.
И наоборот. Поэтому невозможно ограничить значение столбцов наследников ограничением _@NotNull_.
Если _NotNull constraint_ непременно нужен, то надо использовать
другую стратегию, например **_InheritanceType.JOINED_**.

9. **Поиск всех людей**: поиск **полиморфичен**, то есть мы в _HQL_ выбираем из класса _Person_, но полученные
люди имеют конкретный тип _Employee_ либо _Customer_:  
`List<Person> persons = personRepository.findAll();`  
Приведён поиск методом `findAll()` _JpaRepository_, но под капотом там _HQL_.

При этом генерируется SQL SELECT из одной таблицы person (других нет):  
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/example4/09_query.png)  

10. **Итог: стратегия наследования _SINGLE_TABLE_ — самая простая и эффективная.
Единственный её недостаток — невозможность использовать _ограничение NotNull_ для столбцов подклассов**.

### Использованные источники:
- [Наследование с @MappedSuperclass](https://sysout.ru/nasledovanie-s-mappedsuperclass/)
- [Наследование InheritanceType.TABLE_PER_CLASS](https://sysout.ru/nasledovanie-inheritancetype-table_per_class/)
- [Наследование сущностей с помощью InheritanceType.JOINED](https://sysout.ru/nasledovanie-sushhnostej-s-pomoshhyu-joined-table-primer-na-hibernate-i-spring-boot/)
- [Наследование сущностей с помощью InheritanceType.SINGLE_TABLE](https://sysout.ru/nasledovanie-sushhnostej-s-pomoshhyu-single-table-primer-na-hibernate-i-spring-boot/)
