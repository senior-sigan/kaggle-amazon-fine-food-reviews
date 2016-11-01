# Amazon Fine Foods

### Popular words, most commented products and most active users.

```bash
make build run
```

### Translating reviews.

Start rabbitMQ:

```bash
docker run --hostname localhost --rm --name some-rabbit -p 8080:15672 rabbitmq:3-management
```

Run review parser and queue supplier

```bash
make build runTranslate
```

```bash
make buildTranslator buildMockApi
```

Run mock api

```bash
make runMockApi
```

Run translate worker(queue consumer)

```bash
make runTranslator
```