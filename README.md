Запуск :            

     sbt -mem 4096 "server/run --mark mark_filename --cell geo_filename"
     или
     java -jar jar_path --mark mark_filename --cell geo_filename

Генерация файлов:

     sbt -mem 4096 "server/run --generate mark_filename geo_filename"
     или
     java -jar jar_path --generate mark_filename geo_filename

Сборка (генерирует geo-service/geo-server/target/scala-2.12/geo-service.jar): 

     sbt server/assembly

# geo-service
Сервис, обрабатывающий запросы о географических метках пользователей

Реализован rest api, доступный на localhost/9090 с эндпоинтами:

- api/mark/locate - принимает POST запрос с текущими координатами пользователя {"lat": Double, "lon": Double}, 
возвращает информацию , находится ли пользователь у своей метки, и на каком расстоянии {"isNear": Boolean, "message": String}

- api/mark/near - принимает POST запрос с координатами гео ячейки {"tile_x" : Int, "tile_y" : Int}, возвращает список 
всех пользователей, находящихся рядом с ней [{"lat": Double, "lon": Double, "id": Int}, ..]

- api/mark/create_mark - принимает POST запрос с телом {"lat": Double, "lon": Double} и создает новую метку пользователя

- api/mark/update_mark - принимает PUT запрос с телом {"lat": Double, "lon": Double, "id": Int} на изменение марки пользователя по указанному id

- api/mark/delete_mark?id=id - DELETE запрос на удаление пользователя c id = id (целое число)

Расстояние между пользователями считается как     
 
    math.sqrt(math.pow(dCos * sin_delta, 2) + math.pow(uCos * dSin - uSin * dCos * cos_delta, 2))
    uSin * dSin + uCos * dCos * cos_delta
    val ad = Math.atan2(y, x)
в приближении, что радиус земли равен 6372795 метрам

Гео сетка заполняется целыми числами , соотносящимися в градусами широты и долготы, а так же натуральным значением ошибки определения точки:

    tile_x  tile_y  distance_error

Пользовательские метки заполняются целым числом, обозначающим id пользователя и натуральными значениями широты и долготы

        id lon lat
Разделителями выступают символы табуляции

## Технические решения
Стэк:
- akka-http
- akka-streams
- apache-ignite

Для чтения файлов и выгрузки их в cache (Ignite) используется akka-streams.Использование стрима позволяет легко построить цепочку парсинга и преобразования данных в объекты UserMark и GeoCell.

В качестве in-memory db используется apache-ignite, который позволяет делать запросы к кешу за константное время:
- добавление пользователя - O(1)
- удаление пользователя - O(1)
- обновление пользователя - O(1)
- определение близости к точке - O(1)
- поиск всех пользователей в окрестности точки - O (log(n))

Текущая реализация гео-сетки упрощена, взяты только целые части (градусы, без минут и секунд), так же сделано предположение о сферичности земли, в следствие чего сделано допущение, что расстояние в метрах за один градус широты/долготы всегда константно и равно 0.000009009.

Всего ячеек при текущем приближении будет 361 * 181 = 65 341, поэтому дабы приблизиться к ~10000 ячеек генерировались ячейки только в широте -45 до 45 и долготе -90 до 90, что в общей сложности дает кол.во ячеек гео сетки = 16 471

##TODO

- улучшить скорость записи данных из файла в кеш (использовать ассинхронную запись, сейчас процесс инициализации занимает ~3 минуты на core i7, 16gb)
- для парсинга файлов использовать парсер-комбинаторы
- переопределить гео сетку с учетом разного размера в метрах за один градус в зависимости от положения
- добавить сохранение состояния в случае падения/завершения: mongo, hadoop (как связку с ignite). Либо использовать обновление файла и akka persistence.
- масштабируемость (кластеризация , которая в Ignite есть из коробки )
- при переопределении сетки может получиться, что пользователь входит в окрестности нескольких точек. В данном случае использовать поиск ближайшего соседа.