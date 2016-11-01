DATA_PATH = $(shell pwd)/data/Reviews.csv

all: build run

build:
	sbt parser/assembly

run:
	spark-submit --class "it.sevenbits.amazonfinefoods.App" --master local[4] parser/target/scala-2.11/amazon-fine-foods-assembly-1.0.jar $(DATA_PATH)

startRabbitMQ:
  docker run --hostname localhost --rm --name some-rabbit -p 8080:15672 rabbitmq:3-management