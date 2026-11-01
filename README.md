# Spring Boot CRUD Demo

Simple Spring Boot 3 (Java 17) REST API with full CRUD on a `Product` resource.
No database, no security. Data lives in memory and is pre-loaded with 5 sample products.

## Run

```bash
mvn spring-boot:run
```

App starts at http://localhost:8081

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
curl http://localhost:8081/api/products

# Get one
curl http://localhost:8081/api/products/1

# Create
curl -X POST http://localhost:8081/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Webcam","description":"1080p webcam","price":49.99,"quantity":80}'

# Update
curl -X PUT http://localhost:8081/api/products/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Laptop Pro","description":"16-inch laptop","price":1299.99,"quantity":10}'

# Delete
curl -X DELETE http://localhost:8081/api/products/1
```
# DesvOpsTrainingDockerRepo


# Complete Kubernetes Cluster Components
| Category      | Components                                                                 |
|---------------|----------------------------------------------------------------------------|
| Control Plane | API Server, etcd, Scheduler, Controller Manager, Cloud Controller Manager |
| Worker Node   | Kubelet, Kube-proxy, Container Runtime                                     |
| Workloads     | Pod, Deployment, ReplicaSet, StatefulSet, DaemonSet, Job, CronJob          |
| Networking    | Service, Ingress, Gateway, NetworkPolicy                                   |
| Storage       | PV, PVC, StorageClass                                                      |
| Configuration | ConfigMap, Secret                                                          |
| Security      | ServiceAccount, Role, ClusterRole, RoleBinding                             |
| Scaling       | HPA, VPA, PDB                                                              |

# Workloads defined in this project:
1. `pod.yaml`
2. `deployment.yaml` ==> deployment.yaml can't referrence the external pods. That's why all the configuration of the pod.yaml is re-declared in deployment.yaml. Command to apply the deployment `kubectl apply -f deployment.yaml`
3. `service.yaml`
4. `ingress.yaml`

# Intercommunication between Pods:
1. Create a separate pod: kubectl exec curl-client -- curl -i http://demo-crud:8084/api/products
2. Access the application through new POD: kubectl exec curl-client -- curl -i http://demo-crud:8084/api/products

# Kubectl Deployment Rollout flow
1. Instead of deleting and recreating the Deployment, update its image: `kubectl set image deployment/demo-crud demo-crud=haseebahmadbutt/spring-crud-demo:1.1.1`
2. This tells Kubernetes to deploy the new image. Kubernetes gradually replaces the old Pods with new Pods. This is called a `Rolling Update`.
3. Watch the rollout status: `kubectl rollout status deployment/demo-crud`
4. In simple terms:
- Rollout: Deploy a new version of your application.
- Rollout status: Monitor whether Kubernetes successfully deployed the new version.
- Rollback: Return to the previously deployed version if the new version has problems.
5. One important distinction: Rollback restores the previous Deployment configuration, including its image reference. It does not undo database migrations, external changes, or application data modifications.
6. Also, a successful rollout only confirms that Kubernetes considers the Deployment successfully rolled out. It does not guarantee that every business API works correctly.
7. In your Kubernetes learning roadmap, this exercise teaches you how to deploy application updates safely and recover from bad releases without manually deleting Pods or recreating Deployments.