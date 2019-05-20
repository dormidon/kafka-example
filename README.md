# kafka-example

## Настройка окружения

Запуск всего
```commandline
docker-compose -f docker/all.yml up
```

Остановка всего
```commandline
docker-compose -f docker/all.yml down
```

## [kafka](https://kafka.apache.org/)
Kafka - золотая корова проекта. 
Доступна на сети хоста на порту `9092`. 
При старте контейнера создается два топика:
  * `messages` - содержит события отправки сообщений
  * `read` - содержит события прочтения пользователями сообщений
  (для сброса кэша прочитанных сообщений)
  
Наблюдать за состояниями топиков можно через `kafka-console-consumer`.   
За `messages`:
```commandline
docker run --rm --network host wurstmeister/kafka:0.10.2.0 kafka-console-consumer.sh --topic messages --from-beginning --bootstrap-server localhost:9092
```
За `read`:
```commandline
docker run --rm --network host wurstmeister/kafka:0.10.2.0 kafka-console-consumer.sh --topic read --from-beginning --bootstrap-server localhost:9092
```

## [kafka-manager](https://github.com/yahoo/kafka-manager)
UI кластера Kafka. 
После запуска доступен по [http://localhost:9000](http://localhost:9000).
Для добавления в него поднятого рядом нашего "кластера" Kafka делаем следующее.
В меню `Cluster -> Add Cluster` в поле `Cluster Name` указываем произвольное имя кластера, 
а в поле `Cluster Zookeeper Host` передаем `zookeeper:2181`. Версия кластера не столь важна.
После добавления на [главной странице](http://localhost:9000) в списке кластеров появляется наш
кластер, и мы можем в зайти посмотреть на него.

## [adminer](https://github.com/TimWolla/docker-adminer)
UI СУБД, в нашем случае удобно через него смотреть на состояние инстанса PostgreSQL.
После запуска доступен по [http://localhost:8081/](http://localhost:8081/).
Для добавления в него поднятого рядом нашего инстанса PostgreSQL заполняем поля следующим образом:
  * `System` = `PostgreSQL`
  * `Server`
  
  В консоли выполняем
  ```commandline
  docker network inspect docker_default
  ```
  В выхлопе находим раздел, соответсвующий контейнеру `postgres`.
  Например, он может выглядеть так
  ```json
"830b8d68eb90499946336da7301ef51fe005bfdee7985255c1f982840194074e": {
   "Name": "postgres",
   "EndpointID": "6535e8cc305ca0a89bb743ed81697ede3179c9959d9ef476171e89da0cc7270d",
   "MacAddress": "02:42:c0:a8:30:03",
   "IPv4Address": "192.168.48.3/20",
   "IPv6Address": ""
}
```
  Берем адрес из значение поля `IPv4Address`, в примере это `192.168.48.3`, и указываем его в качестве
  сервера.
  * `Username` = `channel`
  * `Password` = `password`
  * `Database` = `channel`

## Работа с приложением

