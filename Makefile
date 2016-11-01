DATA_PATH = $(shell pwd)/data/Reviews.csv

all: build run runTranslate

build:
	sbt parser/assembly

buildTranslator:
	sbt translator/assembly

buildMockApi:
	sbt api/assembly

runMockApi:
	java -jar api/target/scala-2.11/amazon-fine-foods-assembly-1.0.jar

runTranslator:
	java -jar translator/target/scala-2.11/amazon-fine-foods-assembly-1.0.jar

run:
	spark-submit --class "it.sevenbits.amazonfinefoods.App" --master local[4] parser/target/scala-2.11/amazon-fine-foods-assembly-1.0.jar $(DATA_PATH)

runTranslate:
	spark-submit --class "it.sevenbits.amazonfinefoods.App" --master local[4] parser/target/scala-2.11/amazon-fine-foods-assembly-1.0.jar $(DATA_PATH) translate=true