# Inditex Technical Test

### Author: Jorge Martínez Padrón

## How to build and run the app

### Prerequisites

- Java 21 or higher
- Docker Engine and Docker Compose

### Build and run the unit tests

From your terminal, navigate to the project root and run:

```
./gradlew clean build
```

### Run with Docker and Docker Compose

I modified the provided docker-compose.yml to include the app as an additional service
`similar_products_app` based on a custom Dockerfile. To start the application, the mocked product
API server, and the monitoring tools, run:

```
docker compose up -d similar_products_app simulado influxdb grafana
```

To execute the provided test, run:

```
docker compose run --rm k6 run scripts/test.js
```

### Run without Docker and Docker Compose

You can also run the application with Gradle:

```
./gradlew bootRun
```

To start the mocked product API server, and the monitoring tools, run:

```
docker compose up -d simulado influxdb grafana
```

To execute the provided test, run:

```
docker compose run --rm k6 run scripts/test.js
```

## Assumptions and technical decisions

- The `/product/{productId}/similar` endpoint returns 404 Product Not Found when the productId in
  the path does not exist. So, when the `/product/{productId}/similarids` returns 404, I'm assuming
  that the productId from which the similar products were requested was not found. If
  `/product/{productId}` returns 404 or any other error, the `/product/{productId}/similar` will
  return 500 Internal Server Error with a message. This is not clear in the endpoint specification
  or the test description, but in my opinion, it is the most predictable and coherent approach.
- If one of the `/product/{productId}` calls fails, the `/product/{productId}/similar` will
  return 500 Internal Server Error with a message. I also considered returning the list without the
  failed products or explicitly including the missing IDs in the response. However, since this
  wasn't clear in the endpoint specification or the test description, I followed the approach I
  considered more predictable and coherent.
- For every id in the list returned by the `/product/{productId}/similarids` endpoint, I'm making
  a call to `/product/{productId}` in parallel (with virtual threads). The current approach assumes
  that `/product/{productId}` has no rate limiting and is free/inexpensive to call. For a
  production-ready environment, instead of calling the `/product/{productId}` endpoint with possibly
  thousands of requests, we should consider:
    - a bulkhead to limit how many concurrent calls are allowed
    - a rate limiter to limit the number of calls per second
    - a circuit breaker to stop calling the endpoint when the number of failures exceeds a threshold
      and fail quickly
- I included a retry with backoff mechanism to `/product/{productId}/similarids` and
  `/product/{productId}` calls. This will likely impact the results of the provided test,
  especially for the `error` scenario. For a production-ready environment, the values
  configured for the retry with backoff, as well as the read and connect timeouts, should be tuned
  according to the expected behavior of the external service and the `/product/{productId}/similar`
  non-functional requirements.
- I decided to use RestClient with virtual threads and CompletableFuture instead of WebClient.
  WebClient would also have been a good option, but I wanted to explore RestClient. Since the
  solution runs on virtual threads, even though RestClient is blocking, performance should not be
  significantly affected. If I had had more time, I would have compared both approaches and made a
  more informed decision.