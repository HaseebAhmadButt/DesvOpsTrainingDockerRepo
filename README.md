# Spring Boot CRUD Demo

Simple Spring Boot 3 (Java 17) REST API with full CRUD on a `Product` resource, backed by PostgreSQL. No security.

> **Purpose:** this is a **sample project built to learn how Kubernetes (K8s) works**. The app itself is deliberately trivial; the value is in the manifests (Pod, Deployment, StatefulSet, Service, Ingress, ConfigMap, Secret) and the `kubectl` workflows documented below.

## Run

```bash
mvn spring-boot:run
```

App starts at http://localhost:8081

## Endpoints

| Method | URL                  | Description         |
|--------|----------------------|---------------------|
| GET    | /api/v1/products        | List all products   |
| GET    | /api/v1/products/{id}   | Get one product     |
| POST   | /api/v1/products        | Create a product    |
| PUT    | /api/v1/products/{id}   | Update a product    |
| DELETE | /api/v1/products/{id}   | Delete a product    |

## Try it

```bash
# List all
curl http://localhost:8081/api/v1/products

# Get one
curl http://localhost:8081/api/v1/products/1

# Create
curl -X POST http://localhost:8081/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Webcam","description":"1080p webcam","price":49.99,"quantity":80}'

# Update
curl -X PUT http://localhost:8081/api/v1/products/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Laptop Pro","description":"16-inch laptop","price":1299.99,"quantity":10}'

# Delete
curl -X DELETE http://localhost:8081/api/v1/products/1
```
# Docker Commands Used in this project:
1. Build the Docker image: `docker build -t spring-crud-demo .`
2. Tag the Docker image: `docker tag spring-crud-demo  haseebahmadbutt/spring-crud-demo:1.1.2`
3. Push the Docker image to Docker Hub: `docker push haseebahmadbutt/spring-crud-demo:1.1.2`


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

# Kubernetes Setup

## Architecture

```
Ingress (traefik, host: localhost)
   -> Service demo-crud (ClusterIP :8084)
      -> Deployment demo-crud (3 replicas, container :8081)
            | env from ConfigMap db-config + Secret db-secret
            v
         Service demo-crud-service-db (ClusterIP :5433 -> 5432)
            -> StatefulSet demo-crud-db (postgres:16, 5Gi PVC)
```

## Manifests

| File | Kind | Purpose |
|------|------|---------|
| `configmap.yaml` | ConfigMap `db-config` | `SPRING_DATASOURCE_URL` (`jdbc:postgresql://demo-crud-service-db:5433/testdb`) and `SPRING_DATASOURCE_USERNAME` |
| `secret.yaml` | Secret `db-secret` | `SPRING_DATASOURCE_PASSWORD` |
| `deployment-db.yaml` | StatefulSet `demo-crud-db` | Postgres 16, 1 replica, `PGDATA=/var/lib/postgresql/data/pgdata`, 5Gi PVC (`postgres-storage`) |
| `service-db.yaml` | Service `demo-crud-service-db` | Exposes the DB inside the cluster on port 5433 (target 5432) |
| `deployment.yaml` | Deployment `demo-crud` | 3 replicas of `haseebahmadbutt/spring-crud-demo:1.1.2`; env loaded via `envFrom`; requests 256Mi/250m, limits 512Mi/500m; startup, readiness and liveness probes on `/actuator/health/*` (port 8081) |
| `service.yaml` | Service `demo-crud` | ClusterIP on port 8084 -> container port 8081 |
| `ingress.yaml` | Ingress `demo-crud` | Traefik ingress routing `localhost/` to the `demo-crud` Service |
| `pod.yaml` | Pod `demo-crud-pod` | Standalone Pod, the first learning step (the Deployment re-declares its spec, since a Deployment cannot reference an existing Pod) |

## Deploy (order matters: config and DB first)

```bash
kubectl apply -f configmap.yaml -f secret.yaml
kubectl apply -f deployment-db.yaml -f service-db.yaml
kubectl apply -f deployment.yaml -f service.yaml -f ingress.yaml
kubectl get all
```

Test it:

```bash
curl http://localhost/api/v1/products
# or without ingress
kubectl port-forward svc/demo-crud 8084:8084
curl http://localhost:8084/api/v1/products
```

## Viewing pod logs

```bash
kubectl logs <pod-name>                      # logs of a pod
kubectl logs -f <pod-name>                   # stream (follow) logs
kubectl logs --previous <pod-name>           # logs of the last crashed container (CrashLoopBackOff)
kubectl logs --tail=100 <pod-name>           # last 100 lines
kubectl logs --since=10m <pod-name>          # last 10 minutes
kubectl logs <pod-name> -c <container-name>  # specific container in a multi-container pod

kubectl logs -l app=demo-crud --tail=50 --prefix   # all app pods at once, prefixed by pod name
kubectl logs -f -l app=demo-crud --max-log-requests=5
kubectl logs deployment/demo-crud            # logs of one pod of the deployment
kubectl logs statefulset/demo-crud-db        # database logs
kubectl logs demo-crud-db-0                  # database pod directly
```

## Troubleshooting

```bash
kubectl describe pod <pod-name>              # events: probe failures, OOMKilled, image pull errors, scheduling
kubectl get events --sort-by=.lastTimestamp  # recent cluster events
kubectl get pods -w                          # watch status changes live
kubectl exec -it demo-crud-db-0 -- psql -U testuser -d testdb   # connect to the DB
kubectl rollout restart deployment/demo-crud # restart app pods
```

Common causes seen in this project:
- **App `CrashLoopBackOff`**: the JDBC URL must point to the DB *Service* name (`demo-crud-service-db`), not the StatefulSet/pod name.
- **DB `Error`**: pin the Postgres image (`postgres:16`) and set `PGDATA` to a subdirectory of the mounted volume. If a failed run left a bad PVC, delete it: `kubectl delete pvc postgres-storage-demo-crud-db-0`.
- **`OOMKilled` restarts**: a Spring Boot JVM needs far more than 128Mi; use a 512Mi limit.
- **Probes failing**: `/actuator/health/liveness` and `/readiness` require `spring-boot-starter-actuator` and `management.endpoint.health.probes.enabled=true`.

# Intercommunication between Pods:
1. Create a separate pod: kubectl exec curl-client -- curl -i http://demo-crud:8084/api/v1/products
2. Access the application through new POD: kubectl exec curl-client -- curl -i http://demo-crud:8084/api/v1/products

# Kubectl Deployment Rollout flow
1. Instead of deleting and recreating the Deployment, update its image: `kubectl set image deployment/demo-crud demo-crud=haseebahmadbutt/spring-crud-demo:1.1.2`
2. This tells Kubernetes to deploy the new image. Kubernetes gradually replaces the old Pods with new Pods. This is called a `Rolling Update`.
3. Watch the rollout status: `kubectl rollout status deployment/demo-crud`
4. In simple terms:
- Rollout: Deploy a new version of your application.
- Rollout status: Monitor whether Kubernetes successfully deployed the new version.
- Rollback: Return to the previously deployed version if the new version has problems.
5. One important distinction: Rollback restores the previous Deployment configuration, including its image reference. It does not undo database migrations, external changes, or application data modifications.
6. Also, a successful rollout only confirms that Kubernetes considers the Deployment successfully rolled out. It does not guarantee that every business API works correctly.
7. In your Kubernetes learning roadmap, this exercise teaches you how to deploy application updates safely and recover from bad releases without manually deleting Pods or recreating Deployments.


# Kubectl Commands used in this project:
## List workloads
1. kubectl get pods                      # all pods in current namespace
2. kubectl get deployments               # or: kubectl get deploy
3. kubectl get statefulsets              # or: kubectl get sts
4. kubectl get svc                       # services
5. kubectl get pvc                       # persistent volume claims
6. kubectl get configmap,secret          # config objects
7. kubectl get all                       # pods, services, deployments, replicasets, statefulsets

## Useful flags
8. kubectl get pods -o wide              # adds node and pod IP
9. kubectl get pods -w                   # watch for changes live
10. kubectl get pods -l app=demo-crud     # filter by label
11. kubectl get pods -A                   # across all namespaces

## Commands to delete workloads
Pods owned by a Deployment or StatefulSet are recreated immediately when deleted, so which command you want depends on your goal.

## Delete all pods (they'll come back)

```bash
kubectl delete pods --all                 # current namespace
kubectl delete pods --all -n <namespace>  # specific namespace
kubectl delete pods -l app=demo-crud      # only the app pods
```

This is effectively a restart. The Deployment and StatefulSet controllers spin up replacements right away.

## Remove the workloads so the pods stay gone

```bash
kubectl delete deployment demo-crud
kubectl delete statefulset demo-crud-db
```

Or delete everything defined in your manifests:

```bash
kubectl delete -f deployment.yaml
kubectl delete -f deployment-db.yaml
```

Add `-f configmap.yaml -f secret.yaml` too if you want those gone.

## Full cleanup

```bash
kubectl delete pvc postgres-storage-demo-crud-db-0   # the DB's persistent volume
```

Deleting a StatefulSet does **not** delete its PVCs, so your Postgres data survives and will be reattached if you redeploy. Delete the PVC only if you want a fresh database. Check the exact name with `kubectl get pvc`.

## Useful extras

```bash
kubectl delete pods --all --force --grace-period=0   # skip graceful shutdown (stuck pods only)
kubectl delete pod <pod-name>                        # a single pod
kubectl get pods                                     # verify
```

Avoid `kubectl delete all --all -A` or `-A` with delete in general. It can wipe system namespaces like `kube-system` and break the cluster. Stick to your own namespace.