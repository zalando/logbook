# Logbook + Scalyr

We primarily use [Scalyr](https://www.scalyr.com/) for log management at Zalando. When we designed and implemented
Logbook we made sure that they both work seamlessly together. The two main aspects that are worth being highlighted are
[JSON log messages](https://www.scalyr.com/help/parsing-logs#valueLists) and 
[association rules](https://www.scalyr.com/help/parsing-logs#association).

The following sample format, meant to be used in a [custom parser](https://www.scalyr.com/help/parsing-logs), shows
both features in action:

```yaml
{
  id: "http",
  format: "$timestamp=$ $severity$ $threadname$ $flowid$ Logbook $http{parse=json}$",
  association: {
    tag: "http", 
    keys: ["httpCorrelation"], 
    store: ["httpUri"], 
    fetch: ["httpUri"]
  }
}
```

`$http{parse=json}$` will instruct Scalyr to parse a the
[JSON output from Logbook](https://github.com/zalando/logbook#json) into the following fields:

```yaml
http: true
httpCorrelation: b7b143c7-a334-4a26-b800-1e97322efebc
httpHeadersAccept: [application/json]
httpHeadersAccept-Encoding: [gzip,deflate]
httpHeadersAuthorization: [XXX]
httpHeadersConnection: [Keep-Alive]
httpHeadersHost: [localhost:9021]
httpHeadersUser-Agent: [Apache-HttpClient/4.5.1 (Java/1.8.0_131)]
httpHeadersX-Flow-ID: [OWgtIWTdlMuKh97U]
httpMethod: GET
httpRemote: 172.31.157.206
httpType: request
httpUri: http://localhost:9021/oauth2/tokeninfo
```

Having all request/response properties indexed and parsed into individual fields allows for extremely powerful queries:

```
# remote POST requests to endpoints containing an admin path segment
$httpOrigin = 'remote' $httpMethod = 'POST' $httpUri matches '.*/admin/.*'

# local requests to the tokeninfo endpoint
$httpOrigin = 'local' $httpUri matches '.*/tokeninfo'

# local responses with a 4xx status code
$httpOrigin = 'local' $httpStatus >= 400 $httpStatus < 500

# remote responses with a 5xx status code
$httpOrigin = `remote` $httpStatus >= 500
```

Queries like this were also the main motivator behind the `origin` and `type` properties of requests and responses
produced by Logbook.

The `association` rule will associate the request and response log lines using 
[Logbook's correlation](https://github.com/zalando/logbook#correlation) feature. The resulting log event for the
response will then contain the `httpUri` field from the corresponding request: 

```yaml
http: true
httpBodyAccess_token: XXX
httpBodyClient_id: stups_coast-cart-service_0b29611e-78cb-454c-98f7-65ed7a95a216
httpBodyExpires_in: 2250
httpBodyGrant_type: password
httpBodyRealm: /services
httpBodyScope: [uid]
httpBodyToken_type: Bearer
httpBodyUid: stups_coast-cart-service
httpCorrelation: b7b143c7-a334-4a26-b800-1e97322efebc
httpHeadersContent-Length: [833]
httpHeadersContent-Type: [application/json]
httpHeadersDate: [Thu, 20 Jul 2017 21:24:00 GMT]
httpStatus: 200
httpType: response
httpUri: http://localhost:9021/oauth2/tokeninfo
```

This allows to query for responses to a specific endpoint that had a 4xx or 5xx status code:

```
$httpType = 'response' $httpUri = 'http://localhost:9021/oauth2/tokeninfo' $httpStatus >= 400
```
