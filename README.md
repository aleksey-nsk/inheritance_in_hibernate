# Info
- **Стратегии наследования в Hibernate**.
- Простые примеры кода.

### Модуль example1_mapped_superclass
1. **Наследование с @MappedSuperclass**.

2. Аннотация _@MappedSuperclass_ позволяет вынести общие поля в родительский класс, но при этом
не создавать для него отдельную таблицу. При такой стратегии классы-наследники преобразуются
в независимые таблицы. _@MappedSuperclass_ никак не влияет на структуру в базе — это просто способ
вынести общие поля.

3. Создаём родительский класс _Person_:  
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/01_parent_class.png)  
   
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
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/02_03_employee_and_customer.png)  

5. Использована БД _Postgres_ в контейнере _Docker_. Настройки контейнера указываем в файле `docker-compose.yaml`:  
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/04_docker_compose.png)  

6. Настройки подключения к БД прописываем в файле `application.yaml`:  
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/05_application.png)  

In Spring/Spring-Boot, SQL database can be initialized in different ways depending on what your stack is.
JPA has features for DDL generation, and these can be set up to run on startup against the database.
This is controlled through two external properties:
- `spring.jpa.generate-ddl` (boolean) switches the feature on and off and is vendor independent.
- `spring.jpa.hibernate.ddl-auto` (enum) is a Hibernate feature that controls the behavior
in a more fine-grained way. See below for more detail.

Hibernate property values are:
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

7. Главный класс выглядит так:  
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/06_main_class.png)  

С помощью методов помеченных **@PostConstruct** и **@PreDestroy** выполняются операции
перед инициализацией и уничтожением компонента. Есть 3 способа определить операции, выполняемые
до того, как контейнер Spring инициализирует бины и уничтожит их:
- первый способ: с помощью _@PostConstruct_ и _@PreDestroy_. **Здесь используем этот способ!** 
- второй способ: путём определения методов _init-method_ и _destory-method_ в xml.
- третий способ: реализация интерфейсов _InitializingBean_ и _DisposableBean_ с помощью bean-компонентов.

8. Далее настроить подключение к БД на вкладке Database:  
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/07_data_source.png)  

9. Запускаем приложение:  
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/08_app_running.png)  

10. Теперь смотрим созданные таблицы. Сгенерированы 2 независимые таблицы (внешних ключей нет, только первичные).
Они включают поля родительского класса и свои:  
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/09_tables_structure.png)  
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/10_quick_documentation.png)  

11. В итоге сохранять можно только дочерние сущности — они пойдут в независимые таблицы
и будут включать поля родительской сущности. Отдельно _Customer_ не сохранить, это абстракция, в которую
вынесена часть полей.
