# Info
- Стратегии наследования в Hibernate.
- Простые примеры кода.

### Модуль example1_mapped_superclass
- Наследование с **@MappedSuperclass**.

- Аннотация @MappedSuperclass позволяет вынести общие поля в родительский класс, но при этом
не создавать для него отдельную таблицу. При такой стратегии классы-наследники преобразуются
в независимые таблицы. @MappedSuperclass никак не влияет на структуру в базе — это просто способ
вынести общие поля.

- Создаём родительский класс Person:
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/01_parent_class.png)   
Person — не сущность, в него просто вынесены общие поля сущностей Employee и Customer.
Класс Person можно сделать абстрактным. Этот класс можно было бы назвать AbstractEntity.
Ассоциаций (@ManyToOne, @OneToMany и т.д.) с ним сделать нельзя!

GenerationType.SEQUENCE ("sequence" - "последовательность") - это специальный тип
для генерации значений из последовательности. Создаёте в базе данных, которая его поддерживает, например Postgres:
CREATE SEQUENCE my_seq START WITH 100000;                                                             
Используете в качестве генератора.

`allocationSize` указывает Hibernat-у сколько нужно резервировать значений из последовательности
за одно обращение к базе данных. В данном случае на каждое обращение - 1 раз. Чем больше, тем сильнее
снижается нагрузка на базу данных, но больше расходуется последовательность (иногда впустую).

- Дочерние классы Employee и Customer:
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/02_employee.png)
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/03_customer.png)

- Использована БД Postgres в контейнере Docker. Настройки контейнера указываем в файле docker-compose.yaml:
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/04_docker_compose.png)

- Настройки подключения к БД прописываем в файле application.yaml:
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/05_application.png)

      generate-ddl: true
      hibernate:
          ddl-auto: create



In Spring/Spring-Boot, SQL database can be initialized in different ways depending on what your stack is.

JPA has features for DDL generation, and these can be set up to run on startup against the database. This is controlled through two external properties:

    spring.jpa.generate-ddl (boolean) switches the feature on and off and is vendor independent.
    spring.jpa.hibernate.ddl-auto (enum) is a Hibernate feature that controls the behavior in a more fine-grained way. See below for more detail.

Hibernate property values are: create, update, create-drop, validate and none:

    create – Hibernate first drops existing tables, then creates new tables
    update – the object model created based on the mappings (annotations or XML) is compared with the existing schema, and then Hibernate updates the schema according to the diff. It never deletes the existing tables or columns even if they are no more required by the application
    create-drop – similar to create, with the addition that Hibernate will drop the database after all operations are completed. Typically used for unit testing
    validate – Hibernate only validates whether the tables and columns exist, otherwise it throws an exception
    none – this value effectively turns off the DDL generation

Spring Boot internally defaults this parameter value to create-drop if no schema manager has been detected, otherwise none for all other cases.


- Главный класс выглядит так:
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/06_main_class.png)

С помощью методов Spring @PostConstruct и @PreDestroy выполнить операцию перед инициализацией и уничтожением компонента

    @PostConstruct
	public void  init(){
	    System.out.println("I am init method, using @PostConstrut");
	}

    @PreDestroy
	public void  dostory(){
	    System.out.println("I am destory method, using @PreDestroy");
	}

Есть три способа определить операции, выполняемые до того, как контейнер Spring инициализирует бины и уничтожит их:
Первый вид: с помощью методов @PostConstruct и @PreDestroy для выполнения операции перед инициализацией и уничтожением компонента
Второе: путем определения методов init-method и destory-method в xml
Третье: реализация интерфейсов InitializingBean и DisposableBean с помощью bean-компонентов.

Следующее демонстрирует использование @PostConstruct и @PreDestory
1: Определите соответствующий класс реализации: 

- Далее настроить подключение к БД на вкладке Database:
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/07_data_source.png)

- Запускаю приложение:
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/08_app_running.png)

- Теперь смотрим структуру созданных таблиц:
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/09_tables_structure.png)

и их содержимое:
![](https://github.com/aleksey-nsk/inheritance_in_hibernate/blob/master/screenshots/10_tables_contain.png)
