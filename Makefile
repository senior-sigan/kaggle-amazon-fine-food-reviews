DATA_PATH = $(shell pwd)/data/Reviews.csv

all: build run

build:
	sbt package

run:
	spark-submit --class "it.sevenbits.amazonfinefoods.App" --master local[4] target/scala-2.11/amazon-fine-foods-assembly-1.0.jar $(DATA_PATH)
