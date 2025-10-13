```YML
apiVersion: v1
kind: ConfigMap
metadata:
  name: logstash-config
  namespace: ${NAMESPACE}
data:
  logstash.conf: |
    input {
      beats {
        port => ${LOGSTASH_PORT}
      }
    }
    filter {
      # --- 1단계: 로그 형식 감지 및 파싱 ---
      # 우선순위 1: 로그가 JSON 형식인지 확인합니다.
      if [message] =~ /^{.*}$/ {
        json {
          source => "message"
          add_tag => [ "json_parsed" ]
          remove_field => ["message"]
        }
      }
      # 우선순위 2: Spring Boot 기본 로그 형식인지 확인합니다.
      else if "spring" in [kubernetes][container][name] or [message] =~ /^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3}/ {
        grok {
          match => { "message" => "%{TIMESTAMP_ISO8601:log_timestamp}\s+%{LOGLEVEL:log_level}\s+%{NUMBER:pid:int}\s+---\s+\[%{DATA:thread_name}\]\s+%{JAVACLASS:class_name}\s*:\s+%{GREEDYDATA:log_message}" }
          add_tag => [ "grok_parsed_spring" ]
          remove_field => ["message"]
        }
      }
      # 우선순위 3: Nginx access log 형식인지 확인합니다.
      else if "nginx" in [kubernetes][container][name] {
        grok {
          match => { "message" => '%{IPORHOST:clientip} %{USER:ident} %{USER:auth} \[%{HTTPDATE:timestamp}\] "%{WORD:verb} %{DATA:request} HTTP/%{NUMBER:httpversion}" %{NUMBER:response:int} %{NUMBER:bytes:int} "%{DATA:referrer}" "%{DATA:agent}"' }
          add_tag => [ "grok_parsed_nginx" ]
          remove_field => ["message"]
        }
      }
      # 우선순위 4: Python Gunicorn/Uvicorn 로그 형식인지 확인합니다.
      else if [message] =~ /\[\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2} \+\d{4}\]/ {
          grok {
              match => { "message" => '\[%{TIMESTAMP_ISO8601:log_timestamp}\] \[%{NUMBER:pid:int}\] \[%{LOGLEVEL:log_level}\] %{GREEDYDATA:log_message}' }
              add_tag => [ "grok_parsed_python" ]
              remove_field => ["message"]
          }
      }
      # 우선순위 5: 일반적인 Key-Value 형식 로그인지 확인합니다. (예: key1=value1 key2="some value")
      else if [message] =~ /=/ {
        kv {
          source => "message"
          add_tag => [ "kv_parsed" ]
          remove_field => ["message"]
        }
      }

      # --- 2단계: 파싱된 로그 후처리 ---
      # Grok으로 파싱된 로그들의 타임스탬프를 Logstash의 기본 타임스탬프로 설정합니다.
      if "grok_parsed_spring" in [tags] or "grok_parsed_python" in [tags] {
        date {
          match => [ "log_timestamp", "ISO8601" ]
          target => "@timestamp"
          remove_field => ["log_timestamp"]
        }
      } else if "grok_parsed_nginx" in [tags] {
        date {
          match => [ "timestamp", "dd/MMM/yyyy:HH:mm:ss Z" ]
          target => "@timestamp"
          remove_field => ["timestamp"]
        }
      }

      # --- 3단계: 파싱 실패 처리 ---
      # 위의 모든 파싱에 실패한 경우, 나중에 분석할 수 있도록 태그를 추가합니다.
      if "json_parsed" not in [tags] and "grok_parsed_spring" not in [tags] and "grok_parsed_nginx" not in [tags] and "grok_parsed_python" not in [tags] and "kv_parsed" not in [tags] {
        mutate {
          add_tag => ["_parsefailure"]
        }
      }
    }
    output {
      elasticsearch {
        hosts => ["http://host.docker.internal:9200"]
        index => "project-${PROJECT_ID}-logs-%{+YYYY.MM.dd}"
      }
    }
```