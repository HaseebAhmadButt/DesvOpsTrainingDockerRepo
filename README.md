# Spring Boot CRUD Demo

Simple Spring Boot 3 (Java 17) REST API with full CRUD on a `Product` resource.
No database, no security. Data lives in memory and is pre-loaded with 5 sample products.

## Run

```bash
mvn spring-boot:run
```

App starts at http://localhost:8080

## Endpoints

| Method | URL                  | Description         |
|--------|----------------------|---------------------|
| GET    | /api/products        | List all products   |
| GET    | /api/products/{id}   | Get one product     |
| POST   | /api/products        | Create a product    |
| PUT    | /api/products/{id}   | Update a product    |
| DELETE | /api/products/{id}   | Delete a product    |

## Try it

```bash
# List all
curl http://localhost:8080/api/products

# Get one
curl http://localhost:8080/api/products/1

# Create
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Webcam","description":"1080p webcam","price":49.99,"quantity":80}'

# Update
curl -X PUT http://localhost:8080/api/products/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Laptop Pro","description":"16-inch laptop","price":1299.99,"quantity":10}'

# Delete
curl -X DELETE http://localhost:8080/api/products/1
```
# DesvOpsTrainingDockerRepo
