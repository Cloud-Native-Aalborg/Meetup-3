# Meetup-3

## Prerequisites:
* Working Kubernetes Cluster with min. 3 worker nodes
* Nginx Ingress Controller ([https://kubernetes.github.io/ingress-nginx/])
* Storage provisioner + storage class (I have used [https://github.com/kubernetes-incubator/external-storage/tree/master/nfs-client] for this demo)


## 1. Install Elastic Cloud on Kubernetes (ECK)
Elastic is working on a Kubernetes Operator (Currently in beta), to run and manage ElasticSearch, Kibana and Apm Server inside your kubernetes cluster.
More info is available here [Elastic Cloud on Kubernetes (ECK)](https://www.elastic.co/guide/en/cloud-on-k8s/current/index.html) and on there github project [elastic/cloud-on-k8s](https://github.com/elastic/cloud-on-k8s).

To install the operator run this:
```bash
kubectl apply -f https://download.elastic.co/downloads/eck/1.0.0-beta1/all-in-one.yaml
```
This will install and setup the operator in `elastic-system` namespace.

## 2. Create monitoring namespace
Create the namespace to use for monitoring.
```bash
kubectl create namespace monitoring
```
Inside this namespace we will install elasticsearch, kibana, apm server and filebeat.

## 3. Startup the ElasticSearch Cluster
Add ElasticSearch configuration:
```bash
kubectl apply -f k8s/01-monitoring-elasticsearch.yaml
```

Get cluster status using:
```bash
kubectl -n monitoring get elasticsearch
```

Get Pods status
```bash
kubectl -n monitoring get pods --selector='elasticsearch.k8s.elastic.co/cluster-name=monitoring'
```

### Get password for the `elastic` user
A default user named `elastic` is automatically created with the password stored in a Kubernetes secret:
```bash
kubectl -n monitoring get secret monitoring-es-elastic-user -o=jsonpath='{.data.elastic}' | base64 --decode
```

## 4. Startup Kibana
Add Kibana configuration:
```bash
kubectl apply -f k8s/02-monitoring-kibana.yaml
```

## 6. Setup Filebeat (Log Collector)

### 6.1 Added filebeat role to elasticsearch
```
# Run from the developer console in kibana
POST /_security/role/filebeat_writer
{
  "cluster": ["manage_index_templates", "monitor"],
  "indices": [
    {
      "names": [ "filebeat-*" ], 
      "privileges": ["write","create_index"]
    }
  ]
}

GET /_security/role/filebeat_writer
```
### 6.2 Added filebeat user to elasticsearch
```
# Run from the developer console in kibana
POST /_security/user/filebeat_internal
{
  "password" : "filebeat-internal-password",
  "roles" : [ "filebeat_writer"],
  "full_name" : "Internal Filebeat User"
}

GET /_security/user/filebeat_internal
```

### 6.3 Add Filebeat daemonset
```bash
kubectl apply -f k8s/03-monitoring-filebeat.yaml
```

## 7. Setup nginx ingress to do logging in json (Without tracing)

Example of `nginx-configuration` configmap, that can be used to do nginx access/error logging in json.
```yaml
apiVersion: v1
data:
  http-snippet: |-
    map $upstream_connect_time $upstream_connect_time_ {
    default $upstream_connect_time;
    "" -1;
    }
    map $upstream_response_length $upstream_response_length_ {
    default $upstream_response_length;
    "" -1;
    }
    map $upstream_response_time $upstream_response_time_ {
    default $upstream_response_time;
    "" -1;
    }
    map $upstream_status $upstream_status_ {
    default $upstream_status;
    "" -1;
    }
  log-format-escape-json: "true"
  log-format-upstream: '{"app": "nginx-ingress","body_bytes_sent": $body_bytes_sent,"bytes_sent": $bytes_sent,"http_host":
    "$http_host","http_referer": "$http_referer","http_user_agent": "$http_user_agent","msec":
    "$msec","proxy_add_x_forwarded_for": "$proxy_add_x_forwarded_for","proxy_protocol_addr":
    "$proxy_protocol_addr","proxy_upstream_name": "$proxy_upstream_name","remote_addr":
    "$remote_addr","remote_user": "$remote_user","request": "$request","request_length":
    $request_length,"request_method": "$request_method","request_time": $request_time,"request_uri":
    "$request_uri","ssl_protocol": "$ssl_protocol","status": $status,"time_local":
    "$time_local","upstream_addr": "$upstream_addr","upstream_connect_time": $upstream_connect_time_,"upstream_response_length":
    $upstream_response_length_,"upstream_response_time": $upstream_response_time_,"upstream_status":
    $upstream_status_, "server_protocol": "$server_protocol"}'
kind: ConfigMap
metadata:
  labels:
    app.kubernetes.io/name: ingress-nginx
    app.kubernetes.io/part-of: ingress-nginx
  name: nginx-configuration
```

# 

##8. Setup Jaeger from https://github.com/Cloud-Native-Aalborg/Meetup-2/blob/master/k8s/jaeger/jaeger-all-in-one-template.yml
+ ElasticSearch Config
###8.1 Add jaeger role to elasticsearch
```
# Run from the developer console in kibana
POST /_security/role/jaeger
{
    "cluster" : [
      "manage_index_templates",
      "monitor"
    ],
    "indices" : [
      {
        "names" : [
          "*jaeger-service-*",
          "*jaeger-span-*"
        ],
        "privileges" : [
          "all"
        ],
        "field_security" : {
          "grant" : [
            "*"
          ]
        },
        "allow_restricted_indices" : false
      }
    ],
    "applications" : [ ],
    "run_as" : [ ],
    "metadata" : { },
    "transient_metadata" : {
      "enabled" : true
    }
  }
GET /_security/role/jaeger
```

### 8.2 Add jaeger user to elasticsearch
```
# Run from the developer console in kibana
POST /_security/user/jaeger
{
  "password": "jaeger", 
  "roles" : [
    "jaeger"
  ],
  "full_name" : "jaeger",
  "email" : "",
  "metadata" : { }
}
GET /_security/user/jaeger
```
#### 8.3 Enable opentracing on nginx ingress
Example of `nginx-configuration` configmap, that can be used to do nginx access/error logging in json, with tracing enabled.
```yaml
apiVersion: v1
data:
  enable-opentracing: 'true'
  jaeger-collector-host: jaeger-agent.monitoring 	  	
  jaeger-service-name: nginx-ingress
  http-snippet: |-
    map $upstream_connect_time $upstream_connect_time_ {
    default $upstream_connect_time;
    "" -1;
    }
    map $upstream_response_length $upstream_response_length_ {
    default $upstream_response_length;
    "" -1;
    }
    map $upstream_response_time $upstream_response_time_ {
    default $upstream_response_time;
    "" -1;
    }
    map $upstream_status $upstream_status_ {
    default $upstream_status;
    "" -1;
    }
    map $opentracing_context_uber_trace_id $trace_id {
      default "";
      ~(?<traceid>[0-9a-f]*):.* $traceid;
    }
  log-format-escape-json: "true"
  log-format-upstream: '{"body_bytes_sent": $body_bytes_sent,"bytes_sent": $bytes_sent,"http_host":
    "$http_host","http_referer": "$http_referer","http_user_agent": "$http_user_agent","msec":
    "$msec","proxy_add_x_forwarded_for": "$proxy_add_x_forwarded_for","proxy_protocol_addr":
    "$proxy_protocol_addr","proxy_upstream_name": "$proxy_upstream_name","remote_addr":
    "$remote_addr","remote_user": "$remote_user","request": "$request","request_length":
    $request_length,"request_method": "$request_method","request_time": $request_time,"request_uri":
    "$request_uri","ssl_protocol": "$ssl_protocol","status": $status,"time_local":
    "$time_local","upstream_addr": "$upstream_addr","upstream_connect_time": $upstream_connect_time_,"upstream_response_length":
    $upstream_response_length_,"upstream_response_time": $upstream_response_time_,"upstream_status":
    $upstream_status_, "server_protocol": "$server_protocol", "uber_trace_id": "$opentracing_context_uber_trace_id",
    "trace_id":"$trace_id"}'
  server-snippet: |-
    add_header User-Trace-Id $opentracing_context_uber_trace_id;
    add_header Trace-Id $trace_id;
kind: ConfigMap
metadata:
  labels:
    app.kubernetes.io/name: ingress-nginx
    app.kubernetes.io/part-of: ingress-nginx
  name: nginx-configuration
```
