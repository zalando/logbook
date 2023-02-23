# Changelog

## [2.15.0](https://github.com/zalando/logbook/tree/2.15.0) (2023-02-23)

[Full Changelog](https://github.com/zalando/logbook/compare/2.14.0...2.15.0)

**Fixed bugs:**

- LogbookFilter \(servlet\) is not working due to lack of jakarta.\* support [\#1401](https://github.com/zalando/logbook/issues/1401)
- Allow replacing Default logging Keys  [\#1380](https://github.com/zalando/logbook/issues/1380)
- JsonHttpLogFormatter generated invalid json [\#1370](https://github.com/zalando/logbook/issues/1370)
- Not able to write logs into File [\#1348](https://github.com/zalando/logbook/issues/1348)
- Log level in test with @DynamicPropertySource [\#1339](https://github.com/zalando/logbook/issues/1339)
- The body of request and response shown as {...} without details [\#1317](https://github.com/zalando/logbook/issues/1317)
- FastJsonHttpLogFormatter adds "headers" and "body" twice [\#1268](https://github.com/zalando/logbook/issues/1268)
- logbook-spring-webflux: LogbookExchangeFilterFunction does not log bodies of chunked responses [\#1219](https://github.com/zalando/logbook/issues/1219)
- Logbook do not write in logs in Unix Environment [\#1197](https://github.com/zalando/logbook/issues/1197)
- How to obfuscate JSON responses by URL? [\#1185](https://github.com/zalando/logbook/issues/1185)

**Closed issues:**

- Add custom header to each request intercepted by Zalando Logbook [\#1421](https://github.com/zalando/logbook/issues/1421)
- Support for Spring Boot 3 [\#1382](https://github.com/zalando/logbook/issues/1382)
- can logbook log custom Filter log [\#1358](https://github.com/zalando/logbook/issues/1358)
- Questions: Possible to ignore Outgoing response based on HTTP status code ? [\#1355](https://github.com/zalando/logbook/issues/1355)
- Gradle use documentation [\#1328](https://github.com/zalando/logbook/issues/1328)
- Passing APM trace information into log contexts [\#1325](https://github.com/zalando/logbook/issues/1325)
- Json format in Spring Boot Logs  [\#1300](https://github.com/zalando/logbook/issues/1300)
- Missing response log for GET request for Webflux integration [\#1200](https://github.com/zalando/logbook/issues/1200)
- How to log spring principal in webflux ? [\#1098](https://github.com/zalando/logbook/issues/1098)

**Merged pull requests:**

- Bump httpcore from 4.4.15 to 4.4.16 [\#1431](https://github.com/zalando/logbook/pull/1431) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump feign-core from 11.8 to 12.1 [\#1430](https://github.com/zalando/logbook/pull/1430) ([dependabot[bot]](https://github.com/apps/dependabot))
- Update README.md [\#1429](https://github.com/zalando/logbook/pull/1429) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump versions-maven-plugin from 2.9.0 to 2.15.0 [\#1428](https://github.com/zalando/logbook/pull/1428) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump wiremock-jre8 from 2.28.0 to 2.35.0 [\#1427](https://github.com/zalando/logbook/pull/1427) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump reactor-netty from 1.1.2 to 1.1.3 [\#1425](https://github.com/zalando/logbook/pull/1425) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump netty-bom from 4.1.87.Final to 4.1.89.Final [\#1424](https://github.com/zalando/logbook/pull/1424) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump maven-javadoc-plugin from 3.3.2 to 3.5.0 [\#1423](https://github.com/zalando/logbook/pull/1423) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump jacoco-maven-plugin from 0.8.7 to 0.8.8 [\#1422](https://github.com/zalando/logbook/pull/1422) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump maven-resources-plugin from 3.2.0 to 3.3.0 [\#1419](https://github.com/zalando/logbook/pull/1419) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump httpclient from 4.5.13 to 4.5.14 [\#1414](https://github.com/zalando/logbook/pull/1414) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump assertj-core from 3.22.0 to 3.24.2 [\#1412](https://github.com/zalando/logbook/pull/1412) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump maven-enforcer-plugin from 3.0.0 to 3.2.1 [\#1411](https://github.com/zalando/logbook/pull/1411) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump lombok from 1.18.22 to 1.18.26 [\#1410](https://github.com/zalando/logbook/pull/1410) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump jmh.version from 1.34 to 1.36 [\#1408](https://github.com/zalando/logbook/pull/1408) ([dependabot[bot]](https://github.com/apps/dependabot))
- End of support Spring Boot 1 and Spring Famework 4  [\#1407](https://github.com/zalando/logbook/pull/1407) ([kasmarian](https://github.com/kasmarian))
- Bump kotlin.version from 1.8.0 to 1.8.10 [\#1406](https://github.com/zalando/logbook/pull/1406) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump nexus-staging-maven-plugin from 1.6.12 to 1.6.13 [\#1405](https://github.com/zalando/logbook/pull/1405) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump junit.version from 5.8.2 to 5.9.2 [\#1404](https://github.com/zalando/logbook/pull/1404) ([dependabot[bot]](https://github.com/apps/dependabot))
- Updating the dependencies and making the build pass [\#1400](https://github.com/zalando/logbook/pull/1400) ([DaniloVeljovic](https://github.com/DaniloVeljovic))
- Update README.md [\#1374](https://github.com/zalando/logbook/pull/1374) ([SAlavizadeh](https://github.com/SAlavizadeh))
- Fix issue 1112: apache http-client5 decompression [\#1373](https://github.com/zalando/logbook/pull/1373) ([SimplicialCycle](https://github.com/SimplicialCycle))
- 1360-NPE-at-FeignLogbookLogger [\#1362](https://github.com/zalando/logbook/pull/1362) ([hgabor83](https://github.com/hgabor83))
- String value pattern updated in PrimitiveJsonPropertyBodyFilter \(StackOverflowError fix\) [\#1343](https://github.com/zalando/logbook/pull/1343) ([dicody](https://github.com/dicody))
- CODEOWNERS: remove @AlexanderYastrebov [\#1338](https://github.com/zalando/logbook/pull/1338) ([AlexanderYastrebov](https://github.com/AlexanderYastrebov))
- \[fix\] Java URI cannot handle netty URIs [\#1337](https://github.com/zalando/logbook/pull/1337) ([bomgar](https://github.com/bomgar))
- Preserve case of reason phrase from the actual server response [\#1320](https://github.com/zalando/logbook/pull/1320) ([andersjaensson](https://github.com/andersjaensson))
- Bump spring-boot-starter-webflux from 2.6.4 to 2.7.8 in /logbook-spring-boot-webflux-autoconfigure [\#1311](https://github.com/zalando/logbook/pull/1311) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump guava from 31.0.1-jre to 31.1-jre [\#1292](https://github.com/zalando/logbook/pull/1292) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump nexus-staging-maven-plugin from 1.6.8 to 1.6.12 [\#1290](https://github.com/zalando/logbook/pull/1290) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump spring-framework-bom from 5.3.14 to 5.3.16 [\#1289](https://github.com/zalando/logbook/pull/1289) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump httpclient5 from 5.1.2 to 5.1.3 [\#1288](https://github.com/zalando/logbook/pull/1288) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump slf4j.version from 1.7.32 to 1.7.36 [\#1287](https://github.com/zalando/logbook/pull/1287) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump spring-boot-starter-webflux from 2.6.1 to 2.6.4 [\#1286](https://github.com/zalando/logbook/pull/1286) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump versions-maven-plugin from 2.8.1 to 2.9.0 [\#1284](https://github.com/zalando/logbook/pull/1284) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump duplicate-finder-maven-plugin from 1.5.0 to 1.5.1 [\#1283](https://github.com/zalando/logbook/pull/1283) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump spring-boot.version from 2.6.1 to 2.6.4 [\#1282](https://github.com/zalando/logbook/pull/1282) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump mockito.version from 4.1.0 to 4.3.1 [\#1281](https://github.com/zalando/logbook/pull/1281) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump maven-javadoc-plugin from 3.3.1 to 3.3.2 [\#1280](https://github.com/zalando/logbook/pull/1280) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump netty-bom from 4.1.72.Final to 4.1.74.Final [\#1279](https://github.com/zalando/logbook/pull/1279) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump json-path.version from 2.6.0 to 2.7.0 [\#1277](https://github.com/zalando/logbook/pull/1277) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump spring-security-web from 5.6.0 to 5.6.2 [\#1276](https://github.com/zalando/logbook/pull/1276) ([dependabot[bot]](https://github.com/apps/dependabot))
- 1268 fix json duplicated keys [\#1269](https://github.com/zalando/logbook/pull/1269) ([nhmarujo](https://github.com/nhmarujo))
- Bump assertj-core from 3.21.0 to 3.22.0 [\#1264](https://github.com/zalando/logbook/pull/1264) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump logback-classic from 1.2.9 to 1.2.10 [\#1260](https://github.com/zalando/logbook/pull/1260) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump jmh.version from 1.33 to 1.34 [\#1259](https://github.com/zalando/logbook/pull/1259) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump feign-core from 11.7 to 11.8 [\#1258](https://github.com/zalando/logbook/pull/1258) ([dependabot[bot]](https://github.com/apps/dependabot))
- \#1255 Support for JDK HTTP server [\#1256](https://github.com/zalando/logbook/pull/1256) ([phejl](https://github.com/phejl))
- Bump dependency-check-maven from 6.5.0 to 6.5.1 [\#1250](https://github.com/zalando/logbook/pull/1250) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump jackson.version from 2.13.0 to 2.13.1 [\#1249](https://github.com/zalando/logbook/pull/1249) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump spring-framework-bom from 5.3.13 to 5.3.14 [\#1248](https://github.com/zalando/logbook/pull/1248) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump logback-classic from 1.2.8 to 1.2.9 [\#1247](https://github.com/zalando/logbook/pull/1247) ([dependabot[bot]](https://github.com/apps/dependabot))
- Feature/dependency updates [\#1245](https://github.com/zalando/logbook/pull/1245) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump reactor-netty from 1.0.13 to 1.0.14 [\#1243](https://github.com/zalando/logbook/pull/1243) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump kotlin.version from 1.6.0 to 1.6.10 [\#1242](https://github.com/zalando/logbook/pull/1242) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump logback-classic from 1.2.7 to 1.2.8 [\#1241](https://github.com/zalando/logbook/pull/1241) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump httpasyncclient from 4.1.4 to 4.1.5 [\#1237](https://github.com/zalando/logbook/pull/1237) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump netty-codec-http from 4.1.70.Final to 4.1.71.Final in /logbook-netty [\#1234](https://github.com/zalando/logbook/pull/1234) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump httpcore from 4.4.14 to 4.4.15 [\#1233](https://github.com/zalando/logbook/pull/1233) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump junit.version from 5.8.1 to 5.8.2 [\#1232](https://github.com/zalando/logbook/pull/1232) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump mockito.version from 4.0.0 to 4.1.0 [\#1230](https://github.com/zalando/logbook/pull/1230) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump ktor.version from 1.6.4 to 1.6.7 [\#1229](https://github.com/zalando/logbook/pull/1229) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump netty-bom from 4.1.69.Final to 4.1.70.Final [\#1228](https://github.com/zalando/logbook/pull/1228) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump okhttp from 4.9.2 to 4.9.3 [\#1227](https://github.com/zalando/logbook/pull/1227) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump kotlin.version from 1.5.31 to 1.6.0 [\#1226](https://github.com/zalando/logbook/pull/1226) ([dependabot[bot]](https://github.com/apps/dependabot))
- Upgraded Spring [\#1225](https://github.com/zalando/logbook/pull/1225) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump dependency-check-maven from 6.4.1 to 6.5.0 [\#1224](https://github.com/zalando/logbook/pull/1224) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump netty-codec-http from 4.1.69.Final to 4.1.70.Final [\#1223](https://github.com/zalando/logbook/pull/1223) ([dependabot[bot]](https://github.com/apps/dependabot))
- Fix NPE in json replace filter [\#1218](https://github.com/zalando/logbook/pull/1218) ([sokomishalov](https://github.com/sokomishalov))
- Bump httpclient5 from 5.1 to 5.1.2 [\#1215](https://github.com/zalando/logbook/pull/1215) ([dependabot[bot]](https://github.com/apps/dependabot))
- Add microsoft vendor-specific content types to be excluded by default [\#1213](https://github.com/zalando/logbook/pull/1213) ([sokomishalov](https://github.com/sokomishalov))
- Bump logback-classic from 1.2.5 to 1.2.7 [\#1211](https://github.com/zalando/logbook/pull/1211) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump reactor-netty from 1.0.11 to 1.0.13 [\#1210](https://github.com/zalando/logbook/pull/1210) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump feign-core from 11.6 to 11.7 [\#1207](https://github.com/zalando/logbook/pull/1207) ([dependabot[bot]](https://github.com/apps/dependabot))
- logbook-webflux fixes [\#1201](https://github.com/zalando/logbook/pull/1201) ([sokomishalov](https://github.com/sokomishalov))
- Release refs/heads/release/2.14.0 [\#1194](https://github.com/zalando/logbook/pull/1194) ([github-actions[bot]](https://github.com/apps/github-actions))
- wrap client http response body in buffered input stream to support mark/reset [\#1041](https://github.com/zalando/logbook/pull/1041) ([noffke](https://github.com/noffke))

## [2.14.0](https://github.com/zalando/logbook/tree/2.14.0) (2021-10-12)

[Full Changelog](https://github.com/zalando/logbook/compare/2.13.0...2.14.0)

**Fixed bugs:**

- Issues with setting log level [\#1183](https://github.com/zalando/logbook/issues/1183)
- null pointer exception [\#1177](https://github.com/zalando/logbook/issues/1177)
- request body inconsistently removed when using writeBoth [\#1172](https://github.com/zalando/logbook/issues/1172)
- JSON Path filtering is not working with max-body-size [\#1157](https://github.com/zalando/logbook/issues/1157)
- Only include zalando loogbook dependencies in logbook-bom [\#1086](https://github.com/zalando/logbook/issues/1086)

**Closed issues:**

- Logging DeferredResult Body [\#1175](https://github.com/zalando/logbook/issues/1175)
- Logging both request and response in a single line [\#1170](https://github.com/zalando/logbook/issues/1170)
- Grab TransactionId or CorrelationId from header to use as correlationId [\#1166](https://github.com/zalando/logbook/issues/1166)
- Dynamic path parameters replacing of PathFilters [\#1164](https://github.com/zalando/logbook/issues/1164)
- Dynamic value replacing of QueryFilters  [\#1155](https://github.com/zalando/logbook/issues/1155)
- sitemesh3 decorator content is not logged by logbook. [\#1083](https://github.com/zalando/logbook/issues/1083)
- Response should be mapped to it's original Request in some form, Response path should be filterable [\#1067](https://github.com/zalando/logbook/issues/1067)

**Merged pull requests:**

- Bump dependency-check-maven from 6.3.1 to 6.4.1 [\#1193](https://github.com/zalando/logbook/pull/1193) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump netty-codec-http from 4.1.68.Final to 4.1.69.Final [\#1192](https://github.com/zalando/logbook/pull/1192) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump netty-bom from 4.1.68.Final to 4.1.69.Final [\#1191](https://github.com/zalando/logbook/pull/1191) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump mockito.version from 3.12.4 to 4.0.0 [\#1189](https://github.com/zalando/logbook/pull/1189) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump lombok from 1.18.20 to 1.18.22 [\#1188](https://github.com/zalando/logbook/pull/1188) ([dependabot[bot]](https://github.com/apps/dependabot))
- Adjusted workflow job [\#1187](https://github.com/zalando/logbook/pull/1187) ([whiskeysierra](https://github.com/whiskeysierra))
- Fixed coveralls usage in workflow [\#1186](https://github.com/zalando/logbook/pull/1186) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump ktor.version from 1.6.3 to 1.6.4 [\#1184](https://github.com/zalando/logbook/pull/1184) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump okhttp from 4.9.1 to 4.9.2 [\#1182](https://github.com/zalando/logbook/pull/1182) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump jackson.version from 2.12.5 to 2.13.0 [\#1181](https://github.com/zalando/logbook/pull/1181) ([dependabot[bot]](https://github.com/apps/dependabot))
- Change the test when the body is unwrapped array [\#1179](https://github.com/zalando/logbook/pull/1179) ([ismail2ov](https://github.com/ismail2ov))
- Bump guava from 30.1.1-jre to 31.0.1-jre [\#1176](https://github.com/zalando/logbook/pull/1176) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump spring-boot.version from 2.5.4 to 2.5.5 [\#1171](https://github.com/zalando/logbook/pull/1171) ([dependabot[bot]](https://github.com/apps/dependabot))
- Make some HttpMessage methods default [\#1169](https://github.com/zalando/logbook/pull/1169) ([sokomishalov](https://github.com/sokomishalov))
- Bump junit.version from 5.7.2 to 5.8.1 [\#1167](https://github.com/zalando/logbook/pull/1167) ([dependabot[bot]](https://github.com/apps/dependabot))
- Dynamic path parameters replacing of PathFilters [\#1165](https://github.com/zalando/logbook/pull/1165) ([ismail2ov](https://github.com/ismail2ov))
- Fix when request body is empty string [\#1163](https://github.com/zalando/logbook/pull/1163) ([ismail2ov](https://github.com/ismail2ov))
- Fix PathNotFoundException when body is unwrapped Array [\#1162](https://github.com/zalando/logbook/pull/1162) ([ismail2ov](https://github.com/ismail2ov))
- Fix JsonPathBodyFilterBuilder NP exception when the element value is … [\#1161](https://github.com/zalando/logbook/pull/1161) ([ismail2ov](https://github.com/ismail2ov))
- Only include zalando loogbook dependencies in logbook-bom [\#1160](https://github.com/zalando/logbook/pull/1160) ([aschugunov](https://github.com/aschugunov))
- Bump assertj-core from 3.20.2 to 3.21.0 [\#1159](https://github.com/zalando/logbook/pull/1159) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump kotlin-stdlib from 1.5.30 to 1.5.31 [\#1158](https://github.com/zalando/logbook/pull/1158) ([dependabot[bot]](https://github.com/apps/dependabot))
- QueryFilter dynamic value replacing implementation and testing \(resolves \#1155 \) [\#1156](https://github.com/zalando/logbook/pull/1156) ([atrujillofalcon](https://github.com/atrujillofalcon))
- OpenFeign support [\#1154](https://github.com/zalando/logbook/pull/1154) ([sanyarnd](https://github.com/sanyarnd))
- Bump spring-framework-bom from 5.3.9 to 5.3.10 [\#1153](https://github.com/zalando/logbook/pull/1153) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump reactor-netty from 1.0.10 to 1.0.11 [\#1152](https://github.com/zalando/logbook/pull/1152) ([dependabot[bot]](https://github.com/apps/dependabot))
- Dedicated spring-webflux module [\#1150](https://github.com/zalando/logbook/pull/1150) ([sokomishalov](https://github.com/sokomishalov))
- Bump junit.version from 5.7.2 to 5.8.0 [\#1149](https://github.com/zalando/logbook/pull/1149) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump netty-codec-http from 4.1.67.Final to 4.1.68.Final [\#1147](https://github.com/zalando/logbook/pull/1147) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump netty-bom from 4.1.67.Final to 4.1.68.Final [\#1146](https://github.com/zalando/logbook/pull/1146) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump maven-javadoc-plugin from 3.3.0 to 3.3.1 [\#1144](https://github.com/zalando/logbook/pull/1144) ([dependabot[bot]](https://github.com/apps/dependabot))
- Release refs/heads/release/2.13.0 [\#1143](https://github.com/zalando/logbook/pull/1143) ([github-actions[bot]](https://github.com/apps/github-actions))
- Update README.md replaceBody filters example [\#1079](https://github.com/zalando/logbook/pull/1079) ([SpiReCZ](https://github.com/SpiReCZ))

## [2.13.0](https://github.com/zalando/logbook/tree/2.13.0) (2021-09-07)

[Full Changelog](https://github.com/zalando/logbook/compare/2.12.0...2.13.0)

**Fixed bugs:**

- Path filter doesn't take the end of the path expression into account [\#1140](https://github.com/zalando/logbook/issues/1140)
- Hot to get correlation id inside spring boot controller/service on slf4j? [\#1135](https://github.com/zalando/logbook/issues/1135)
- Dependencies clash around com.jayway.jsonpath and excluded minidev library [\#1074](https://github.com/zalando/logbook/issues/1074)

**Closed issues:**

- Dynamic value replacing of JsonPath filters [\#1127](https://github.com/zalando/logbook/issues/1127)
- add current time field for request and response log [\#1122](https://github.com/zalando/logbook/issues/1122)
- Micronaut integration [\#1106](https://github.com/zalando/logbook/issues/1106)
- User can not add AsyncListener which can do something  before log write [\#1002](https://github.com/zalando/logbook/issues/1002)

**Merged pull requests:**

- Check that all filter parts have been used when running out of input string [\#1142](https://github.com/zalando/logbook/pull/1142) ([skjolber](https://github.com/skjolber))
- Bump dependency-check-maven from 6.2.2 to 6.3.1 [\#1138](https://github.com/zalando/logbook/pull/1138) ([dependabot[bot]](https://github.com/apps/dependabot))
- Updated ktor to 1.6.3 [\#1136](https://github.com/zalando/logbook/pull/1136) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump jackson.version from 2.12.4 to 2.12.5 [\#1130](https://github.com/zalando/logbook/pull/1130) ([dependabot[bot]](https://github.com/apps/dependabot))
- JsonPath dynamic value replacing implementation and testing \( resolves \#1127 \) [\#1128](https://github.com/zalando/logbook/pull/1128) ([atrujillofalcon](https://github.com/atrujillofalcon))
- Bump mockito.version from 3.11.2 to 3.12.4 [\#1126](https://github.com/zalando/logbook/pull/1126) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump kotlin-maven-plugin from 1.5.21 to 1.5.30 [\#1124](https://github.com/zalando/logbook/pull/1124) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump kotlin-stdlib from 1.5.21 to 1.5.30 [\#1123](https://github.com/zalando/logbook/pull/1123) ([dependabot[bot]](https://github.com/apps/dependabot))
- Update maven wrapper version to 3.8.2 [\#1121](https://github.com/zalando/logbook/pull/1121) ([DanielFran](https://github.com/DanielFran))
- Bump spring-boot.version from 2.5.3 to 2.5.4 [\#1118](https://github.com/zalando/logbook/pull/1118) ([dependabot[bot]](https://github.com/apps/dependabot))
- Release refs/heads/release/2.12.0 [\#1117](https://github.com/zalando/logbook/pull/1117) ([github-actions[bot]](https://github.com/apps/github-actions))
- logbook-ktor test and double-read fixes [\#1116](https://github.com/zalando/logbook/pull/1116) ([sokomishalov](https://github.com/sokomishalov))

## [2.12.0](https://github.com/zalando/logbook/tree/2.12.0) (2021-08-17)

[Full Changelog](https://github.com/zalando/logbook/compare/2.11.0...2.12.0)

**Fixed bugs:**

- Unknown spring property secure-filter.enabled [\#1109](https://github.com/zalando/logbook/issues/1109)
- `test` scope of guava in BOM [\#1084](https://github.com/zalando/logbook/issues/1084)
- Type "x-www-form-urlencoded" Request Parameters Not Available [\#1077](https://github.com/zalando/logbook/issues/1077)
- For remote request  write response body before request body. Relevant for Netty  [\#1071](https://github.com/zalando/logbook/issues/1071)

**Closed issues:**

- LogbookClientHttpRequestInterceptor throws IOException in Spring [\#1093](https://github.com/zalando/logbook/issues/1093)
- Apache HttpClient 5.0 [\#1008](https://github.com/zalando/logbook/issues/1008)
- Please add CHANGELOG.md :\) [\#944](https://github.com/zalando/logbook/issues/944)

**Merged pull requests:**

- Bump spring-security-web from 5.5.1 to 5.5.2 [\#1115](https://github.com/zalando/logbook/pull/1115) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump netty-bom from 4.1.66.Final to 4.1.67.Final [\#1114](https://github.com/zalando/logbook/pull/1114) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump netty-codec-http from 4.1.66.Final to 4.1.67.Final [\#1113](https://github.com/zalando/logbook/pull/1113) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump reactor-netty from 1.0.9 to 1.0.10 [\#1111](https://github.com/zalando/logbook/pull/1111) ([dependabot[bot]](https://github.com/apps/dependabot))
- Add `logbook.secure-filter.enabled` to the additional-spring-configuration-metadata.json [\#1110](https://github.com/zalando/logbook/pull/1110) ([sokomishalov](https://github.com/sokomishalov))
- Bump jmh.version from 1.32 to 1.33 [\#1108](https://github.com/zalando/logbook/pull/1108) ([dependabot[bot]](https://github.com/apps/dependabot))
- fix code snippet in README.md [\#1107](https://github.com/zalando/logbook/pull/1107) ([agebhar1](https://github.com/agebhar1))
- logbook-ktor modules [\#1105](https://github.com/zalando/logbook/pull/1105) ([sokomishalov](https://github.com/sokomishalov))
- Bump maven-enforcer-plugin from 3.0.0-M3 to 3.0.0 [\#1104](https://github.com/zalando/logbook/pull/1104) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump logback-classic from 1.2.4-groovyless to 1.2.5 [\#1103](https://github.com/zalando/logbook/pull/1103) ([dependabot[bot]](https://github.com/apps/dependabot))
- Added first changelog draft [\#1102](https://github.com/zalando/logbook/pull/1102) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump logback-classic from 1.2.4 to 1.2.4-groovyless [\#1101](https://github.com/zalando/logbook/pull/1101) ([dependabot[bot]](https://github.com/apps/dependabot))
- Possible fix for \#1071 [\#1100](https://github.com/zalando/logbook/pull/1100) ([sokomishalov](https://github.com/sokomishalov))
- Bump spring-boot.version from 2.5.2 to 2.5.3 [\#1099](https://github.com/zalando/logbook/pull/1099) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump slf4j.version from 1.7.31 to 1.7.32 [\#1097](https://github.com/zalando/logbook/pull/1097) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump logback-classic from 1.2.3 to 1.2.4 [\#1096](https://github.com/zalando/logbook/pull/1096) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump netty-codec-http from 4.1.65.Final to 4.1.66.Final [\#1095](https://github.com/zalando/logbook/pull/1095) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump netty-bom from 4.1.65.Final to 4.1.66.Final [\#1094](https://github.com/zalando/logbook/pull/1094) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump spring-framework-bom from 5.3.8 to 5.3.9 [\#1092](https://github.com/zalando/logbook/pull/1092) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump kotlin-stdlib from 1.5.20 to 1.5.21 [\#1091](https://github.com/zalando/logbook/pull/1091) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump reactor-netty from 1.0.8 to 1.0.9 [\#1090](https://github.com/zalando/logbook/pull/1090) ([dependabot[bot]](https://github.com/apps/dependabot))
- Replace deprecated methods of Netty HttpClient and HttpServer [\#1089](https://github.com/zalando/logbook/pull/1089) ([robeatoz](https://github.com/robeatoz))
- Bump jackson.version from 2.12.3 to 2.12.4 [\#1088](https://github.com/zalando/logbook/pull/1088) ([dependabot[bot]](https://github.com/apps/dependabot))
- \#1084 Guava test scope [\#1085](https://github.com/zalando/logbook/pull/1085) ([i8r](https://github.com/i8r))
- Bump apiguardian-api from 1.1.1 to 1.1.2 [\#1082](https://github.com/zalando/logbook/pull/1082) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump kotlin-stdlib from 1.5.10 to 1.5.20 [\#1081](https://github.com/zalando/logbook/pull/1081) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump spring-boot.version from 2.5.1 to 2.5.2 [\#1080](https://github.com/zalando/logbook/pull/1080) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump reactor-netty from 1.0.7 to 1.0.8 [\#1078](https://github.com/zalando/logbook/pull/1078) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump spring-security-web from 5.5.0 to 5.5.1 [\#1076](https://github.com/zalando/logbook/pull/1076) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump mockito.version from 3.11.1 to 3.11.2 [\#1075](https://github.com/zalando/logbook/pull/1075) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump slf4j.version from 1.7.30 to 1.7.31 [\#1073](https://github.com/zalando/logbook/pull/1073) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump assertj-core from 3.20.1 to 3.20.2 [\#1072](https://github.com/zalando/logbook/pull/1072) ([dependabot[bot]](https://github.com/apps/dependabot))
- Release/2.11.0 [\#1070](https://github.com/zalando/logbook/pull/1070) ([whiskeysierra](https://github.com/whiskeysierra))

## [2.11.0](https://github.com/zalando/logbook/tree/2.11.0) (2021-06-17)

[Full Changelog](https://github.com/zalando/logbook/compare/2.10.0...2.11.0)

**Fixed bugs:**

- Error using bodyFilter with jsonPath  [\#1049](https://github.com/zalando/logbook/issues/1049)

**Merged pull requests:**

- Release/2.10.0 [\#1069](https://github.com/zalando/logbook/pull/1069) ([whiskeysierra](https://github.com/whiskeysierra))
- Do not fail on missing JSON Path \(fixes \#1049, fixes \#1056\) [\#1066](https://github.com/zalando/logbook/pull/1066) ([PascalSchumacher](https://github.com/PascalSchumacher))
- Apache http client 5 [\#1058](https://github.com/zalando/logbook/pull/1058) ([sokomishalov](https://github.com/sokomishalov))

## [2.10.0](https://github.com/zalando/logbook/tree/2.10.0) (2021-06-17)

[Full Changelog](https://github.com/zalando/logbook/compare/2.9.0...2.10.0)

**Fixed bugs:**

- Leased connections are not released [\#1059](https://github.com/zalando/logbook/issues/1059)
- www-url-form-encoded parameters are not handed in Spring project [\#1051](https://github.com/zalando/logbook/issues/1051)
- Unable to read request body from ServletRequest because InputStream is not cached anymore with latest version [\#974](https://github.com/zalando/logbook/issues/974)

**Closed issues:**

- Need help with bodyFilter [\#1056](https://github.com/zalando/logbook/issues/1056)
- How can i disbale to log default params & can i rename "correlation" field [\#1029](https://github.com/zalando/logbook/issues/1029)
- Allow filtering of objects and arrays inside the body [\#1003](https://github.com/zalando/logbook/issues/1003)
- HttpLogWriter take log level as configuration [\#999](https://github.com/zalando/logbook/issues/999)

**Merged pull requests:**

- Bump assertj-core from 3.20.0 to 3.20.1 [\#1068](https://github.com/zalando/logbook/pull/1068) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump assertj-core from 3.19.0 to 3.20.0 [\#1065](https://github.com/zalando/logbook/pull/1065) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump mockito.version from 3.10.0 to 3.11.1 [\#1064](https://github.com/zalando/logbook/pull/1064) ([dependabot[bot]](https://github.com/apps/dependabot))
- "lombokify" logbook-httpclient a bit [\#1063](https://github.com/zalando/logbook/pull/1063) ([sokomishalov](https://github.com/sokomishalov))
- Bump dependency-check-maven from 6.1.6 to 6.2.2 [\#1062](https://github.com/zalando/logbook/pull/1062) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump spring-boot.version from 2.5.0 to 2.5.1 [\#1061](https://github.com/zalando/logbook/pull/1061) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump spring-framework-bom from 5.3.7 to 5.3.8 [\#1060](https://github.com/zalando/logbook/pull/1060) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump json-path.version from 2.5.0 to 2.6.0 [\#1054](https://github.com/zalando/logbook/pull/1054) ([dependabot[bot]](https://github.com/apps/dependabot))
- Close original entity stream in order to avoid connection leak [\#1052](https://github.com/zalando/logbook/pull/1052) ([brannstrom](https://github.com/brannstrom))
- Bump jmh.version from 1.31 to 1.32 [\#1050](https://github.com/zalando/logbook/pull/1050) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump kotlin-stdlib from 1.5.0 to 1.5.10 [\#1047](https://github.com/zalando/logbook/pull/1047) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump maven-javadoc-plugin from 3.2.0 to 3.3.0 [\#1046](https://github.com/zalando/logbook/pull/1046) ([dependabot[bot]](https://github.com/apps/dependabot))
- Updated Maven to version 3.8.1 [\#1045](https://github.com/zalando/logbook/pull/1045) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump spring-boot.version from 2.4.5 to 2.5.0 [\#1044](https://github.com/zalando/logbook/pull/1044) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump netty-codec-http from 4.1.63.Final to 4.1.65.Final [\#1043](https://github.com/zalando/logbook/pull/1043) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump netty-bom from 4.1.64.Final to 4.1.65.Final [\#1042](https://github.com/zalando/logbook/pull/1042) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump spring-security-web from 5.4.6 to 5.5.0 [\#1040](https://github.com/zalando/logbook/pull/1040) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump netty-bom from 4.1.63.Final to 4.1.64.Final [\#1038](https://github.com/zalando/logbook/pull/1038) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump junit.version from 5.7.1 to 5.7.2 [\#1037](https://github.com/zalando/logbook/pull/1037) ([dependabot[bot]](https://github.com/apps/dependabot))
- Logbook-json: Remove json-smart dependency. [\#1036](https://github.com/zalando/logbook/pull/1036) ([PascalSchumacher](https://github.com/PascalSchumacher))
- Bump mockito.version from 3.9.0 to 3.10.0 [\#1035](https://github.com/zalando/logbook/pull/1035) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump jmh.version from 1.30 to 1.31 [\#1034](https://github.com/zalando/logbook/pull/1034) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump spring-framework-bom from 5.3.6 to 5.3.7 [\#1033](https://github.com/zalando/logbook/pull/1033) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump reactor-netty from 1.0.6 to 1.0.7 [\#1032](https://github.com/zalando/logbook/pull/1032) ([dependabot[bot]](https://github.com/apps/dependabot))
- Release/2.9.0 [\#1031](https://github.com/zalando/logbook/pull/1031) ([whiskeysierra](https://github.com/whiskeysierra))

## [2.9.0](https://github.com/zalando/logbook/tree/2.9.0) (2021-05-11)

[Full Changelog](https://github.com/zalando/logbook/compare/2.8.0...2.9.0)

**Merged pull requests:**

- Made JsonPathBodyFilter mergeable [\#1030](https://github.com/zalando/logbook/pull/1030) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump maven-gpg-plugin from 1.6 to 3.0.1 [\#1028](https://github.com/zalando/logbook/pull/1028) ([dependabot[bot]](https://github.com/apps/dependabot))
- Release/2.8.0 [\#1027](https://github.com/zalando/logbook/pull/1027) ([whiskeysierra](https://github.com/whiskeysierra))
- Add JsonPath body filters [\#1022](https://github.com/zalando/logbook/pull/1022) ([vdsirotkin](https://github.com/vdsirotkin))

## [2.8.0](https://github.com/zalando/logbook/tree/2.8.0) (2021-05-08)

[Full Changelog](https://github.com/zalando/logbook/compare/2.7.0...2.8.0)

**Merged pull requests:**

- Applied code style [\#1026](https://github.com/zalando/logbook/pull/1026) ([whiskeysierra](https://github.com/whiskeysierra))
- create support for absent header condition [\#1025](https://github.com/zalando/logbook/pull/1025) ([Mikhail-Polivakha](https://github.com/Mikhail-Polivakha))
- Bump jmh.version from 1.29 to 1.30 [\#1024](https://github.com/zalando/logbook/pull/1024) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump jacoco-maven-plugin from 0.8.6 to 0.8.7 [\#1023](https://github.com/zalando/logbook/pull/1023) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump spring-framework-bom from 5.3.5 to 5.3.6 [\#1021](https://github.com/zalando/logbook/pull/1021) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump spring-security-web from 5.4.5 to 5.4.6 [\#1020](https://github.com/zalando/logbook/pull/1020) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump jersey-bom from 2.33 to 2.34 [\#1019](https://github.com/zalando/logbook/pull/1019) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump jackson.version from 2.12.2 to 2.12.3 [\#1018](https://github.com/zalando/logbook/pull/1018) ([dependabot[bot]](https://github.com/apps/dependabot))
- Release/2.7.0 [\#1016](https://github.com/zalando/logbook/pull/1016) ([whiskeysierra](https://github.com/whiskeysierra))

## [2.7.0](https://github.com/zalando/logbook/tree/2.7.0) (2021-05-03)

[Full Changelog](https://github.com/zalando/logbook/compare/2.6.2...2.7.0)

**Fixed bugs:**

- Body in response is filtered even though no related configuration is present [\#1011](https://github.com/zalando/logbook/issues/1011)
- Empty or truncated servlet http response on async request   [\#954](https://github.com/zalando/logbook/issues/954)

**Merged pull requests:**

- Bump dependency-check-maven from 6.1.5 to 6.1.6 [\#1015](https://github.com/zalando/logbook/pull/1015) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Upgrade to GitHub-native Dependabot [\#1014](https://github.com/zalando/logbook/pull/1014) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump kotlin-stdlib from 1.4.32 to 1.5.0 [\#1012](https://github.com/zalando/logbook/pull/1012) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring-boot.version from 2.4.4 to 2.4.5 [\#1009](https://github.com/zalando/logbook/pull/1009) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump reactor-netty from 1.0.5 to 1.0.6 [\#1006](https://github.com/zalando/logbook/pull/1006) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bumped netty [\#1001](https://github.com/zalando/logbook/pull/1001) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump mockito.version from 3.8.0 to 3.9.0 [\#1000](https://github.com/zalando/logbook/pull/1000) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Logbook-netty more extractions [\#997](https://github.com/zalando/logbook/pull/997) ([sokomishalov](https://github.com/sokomishalov))
- Bump netty-codec-http from 4.1.62.Final to 4.1.63.Final [\#996](https://github.com/zalando/logbook/pull/996) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump lombok from 1.18.18 to 1.18.20 [\#995](https://github.com/zalando/logbook/pull/995) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump dependency-check-maven from 6.1.4 to 6.1.5 [\#994](https://github.com/zalando/logbook/pull/994) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Release/2.6.2 [\#993](https://github.com/zalando/logbook/pull/993) ([whiskeysierra](https://github.com/whiskeysierra))

## [2.6.2](https://github.com/zalando/logbook/tree/2.6.2) (2021-04-01)

[Full Changelog](https://github.com/zalando/logbook/compare/2.6.1...2.6.2)

**Closed issues:**

- Is there anyway to put a condition based on the Response instead of the Request? [\#990](https://github.com/zalando/logbook/issues/990)
- how to record request and response at the same line ? [\#987](https://github.com/zalando/logbook/issues/987)

**Merged pull requests:**

- fix: README PathFilter based on property name typo [\#992](https://github.com/zalando/logbook/pull/992) ([brnhrdt](https://github.com/brnhrdt))
- Fix CommonsLogFormatSink class name in the README [\#991](https://github.com/zalando/logbook/pull/991) ([salomvary](https://github.com/salomvary))
- Bump netty-codec-http from 4.1.60.Final to 4.1.62.Final [\#989](https://github.com/zalando/logbook/pull/989) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump dependency-check-maven from 6.1.3 to 6.1.4 [\#988](https://github.com/zalando/logbook/pull/988) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump jmh.version from 1.28 to 1.29 [\#986](https://github.com/zalando/logbook/pull/986) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- capture response where servlet code programmatically starts async dispatch [\#985](https://github.com/zalando/logbook/pull/985) ([noffke](https://github.com/noffke))
- Bump kotlin-stdlib from 1.4.31 to 1.4.32 [\#984](https://github.com/zalando/logbook/pull/984) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump dependency-check-maven from 6.1.2 to 6.1.3 [\#983](https://github.com/zalando/logbook/pull/983) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring-boot.version from 2.4.3 to 2.4.4 [\#982](https://github.com/zalando/logbook/pull/982) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Fixed release script [\#981](https://github.com/zalando/logbook/pull/981) ([whiskeysierra](https://github.com/whiskeysierra))
- Release/2.6.1 [\#980](https://github.com/zalando/logbook/pull/980) ([whiskeysierra](https://github.com/whiskeysierra))

## [2.6.1](https://github.com/zalando/logbook/tree/2.6.1) (2021-03-18)

[Full Changelog](https://github.com/zalando/logbook/compare/2.6.0...2.6.1)

**Fixed bugs:**

- logbook-netty in spring-cloud-gateway: response body not logged [\#977](https://github.com/zalando/logbook/issues/977)

**Merged pull requests:**

- fix: Logback configuration snippet typo [\#979](https://github.com/zalando/logbook/pull/979) ([stephanedaviet](https://github.com/stephanedaviet))
- log response body when netty passes in a ByteBuf instead of a HttpContent [\#978](https://github.com/zalando/logbook/pull/978) ([noffke](https://github.com/noffke))
- Bump spring-framework-bom from 5.3.4 to 5.3.5 [\#976](https://github.com/zalando/logbook/pull/976) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump reactor-netty from 1.0.4 to 1.0.5 [\#975](https://github.com/zalando/logbook/pull/975) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump netty-codec-http from 4.1.59.Final to 4.1.60.Final [\#973](https://github.com/zalando/logbook/pull/973) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump dependency-check-maven from 6.1.1 to 6.1.2 [\#972](https://github.com/zalando/logbook/pull/972) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Release/2.6.0 [\#971](https://github.com/zalando/logbook/pull/971) ([whiskeysierra](https://github.com/whiskeysierra))

## [2.6.0](https://github.com/zalando/logbook/tree/2.6.0) (2021-03-07)

[Full Changelog](https://github.com/zalando/logbook/compare/2.4.2...2.6.0)

**Fixed bugs:**

- java.lang.IllegalStateException: Content has not been provided [\#941](https://github.com/zalando/logbook/issues/941)

**Security fixes:**

- \[Security\] Bump netty-codec-http from 4.1.58.Final to 4.1.59.Final [\#951](https://github.com/zalando/logbook/pull/951) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))

**Closed issues:**

- Add cookie masking support [\#964](https://github.com/zalando/logbook/issues/964)
- Provide a way to enable partial logging of traffic [\#938](https://github.com/zalando/logbook/issues/938)
- logbook configuration file [\#935](https://github.com/zalando/logbook/issues/935)

**Merged pull requests:**

- Release 2.5.0 [\#970](https://github.com/zalando/logbook/pull/970) ([whiskeysierra](https://github.com/whiskeysierra))
- Release 2.4.2 [\#969](https://github.com/zalando/logbook/pull/969) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump jackson.version from 2.12.1 to 2.12.2 [\#968](https://github.com/zalando/logbook/pull/968) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump jmh.version from 1.27 to 1.28 [\#967](https://github.com/zalando/logbook/pull/967) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump kotlin-stdlib from 1.4.30 to 1.4.31 [\#966](https://github.com/zalando/logbook/pull/966) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Add cookie value replacer function [\#965](https://github.com/zalando/logbook/pull/965) ([nyilmaz](https://github.com/nyilmaz))
- Bump mockito.version from 3.7.7 to 3.8.0 [\#962](https://github.com/zalando/logbook/pull/962) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring-boot.version from 2.4.2 to 2.4.3 [\#960](https://github.com/zalando/logbook/pull/960) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring-security-web from 5.4.4 to 5.4.5 [\#959](https://github.com/zalando/logbook/pull/959) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring-framework-bom from 5.3.3 to 5.3.4 [\#958](https://github.com/zalando/logbook/pull/958) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump reactor-netty from 1.0.3 to 1.0.4 [\#957](https://github.com/zalando/logbook/pull/957) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump dependency-check-maven from 6.1.0 to 6.1.1 [\#956](https://github.com/zalando/logbook/pull/956) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring-security-web from 5.4.2 to 5.4.4 [\#955](https://github.com/zalando/logbook/pull/955) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump netty-codec-http from 4.1.58.Final to 4.1.59.Final in /logbook-netty [\#952](https://github.com/zalando/logbook/pull/952) ([dependabot[bot]](https://github.com/apps/dependabot))
- Release/2.4.2 [\#950](https://github.com/zalando/logbook/pull/950) ([whiskeysierra](https://github.com/whiskeysierra))

## [2.4.2](https://github.com/zalando/logbook/tree/2.4.2) (2021-02-08)

[Full Changelog](https://github.com/zalando/logbook/compare/2.5.0...2.4.2)

**Closed issues:**

- Request params in body are erased by logbook [\#945](https://github.com/zalando/logbook/issues/945)

**Merged pull requests:**

- Bump rest-client-driver from 2.0.0 to 2.0.1 [\#949](https://github.com/zalando/logbook/pull/949) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Fix empty response body when using Spring RestTemplate with LogbookClientHttpRequestInterceptor [\#948](https://github.com/zalando/logbook/pull/948) ([thowimmer](https://github.com/thowimmer))
- Bump junit.version from 5.7.0 to 5.7.1 [\#947](https://github.com/zalando/logbook/pull/947) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump kotlin-stdlib from 1.4.21-2 to 1.4.30 [\#946](https://github.com/zalando/logbook/pull/946) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Release/2.5.0 [\#943](https://github.com/zalando/logbook/pull/943) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump okhttp from 4.9.0 to 4.9.1 [\#942](https://github.com/zalando/logbook/pull/942) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump lombok from 1.18.16 to 1.18.18 [\#940](https://github.com/zalando/logbook/pull/940) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump dependency-check-maven from 6.0.5 to 6.1.0 [\#939](https://github.com/zalando/logbook/pull/939) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))

## [2.5.0](https://github.com/zalando/logbook/tree/2.5.0) (2021-02-02)

[Full Changelog](https://github.com/zalando/logbook/compare/2.4.1...2.5.0)

**Fixed bugs:**

- json tests aren't windows friendly [\#924](https://github.com/zalando/logbook/issues/924)
- doc | document mistake in Example Configuration. [\#923](https://github.com/zalando/logbook/issues/923)
- logbook 2.0.0-RC.5 \> when logging multi part requests Spring cannot find anymore correctly multi part [\#911](https://github.com/zalando/logbook/issues/911)
- ClassNotFoundException: javax.servlet.Filter thrown during Spring Reactive Web application startup [\#881](https://github.com/zalando/logbook/issues/881)

**Closed issues:**

- Separate host/path from uri  [\#915](https://github.com/zalando/logbook/issues/915)
- Allow inheritance across the library [\#869](https://github.com/zalando/logbook/issues/869)
- Post body empty when using Spring MVC @RequestParam to handle a application/x-www-form-urlencoded form POST [\#864](https://github.com/zalando/logbook/issues/864)
- Spring boot starter for reactive web environments [\#740](https://github.com/zalando/logbook/issues/740)
- Add request/response logging to Spring's RestTemplate [\#134](https://github.com/zalando/logbook/issues/134)

**Merged pull requests:**

- Bump assertj-core from 3.18.1 to 3.19.0 [\#937](https://github.com/zalando/logbook/pull/937) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito.version from 3.7.0 to 3.7.7 [\#934](https://github.com/zalando/logbook/pull/934) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring-boot.version from 2.4.1 to 2.4.2 [\#933](https://github.com/zalando/logbook/pull/933) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump netty-codec-http from 4.1.57.Final to 4.1.58.Final [\#932](https://github.com/zalando/logbook/pull/932) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump netty-codec-http from 4.1.56.Final to 4.1.57.Final [\#931](https://github.com/zalando/logbook/pull/931) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring-framework-bom from 5.3.2 to 5.3.3 [\#930](https://github.com/zalando/logbook/pull/930) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump reactor-netty from 1.0.2 to 1.0.3 [\#929](https://github.com/zalando/logbook/pull/929) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Fixes \#923 [\#928](https://github.com/zalando/logbook/pull/928) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump jackson.version from 2.12.0 to 2.12.1 [\#927](https://github.com/zalando/logbook/pull/927) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump dependency-check-maven from 6.0.4 to 6.0.5 [\#926](https://github.com/zalando/logbook/pull/926) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- use system line separator [\#925](https://github.com/zalando/logbook/pull/925) ([nhomble](https://github.com/nhomble))
- Bump mockito.version from 3.6.28 to 3.7.0 [\#922](https://github.com/zalando/logbook/pull/922) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump faux-pas from 0.8.0 to 0.9.0 [\#921](https://github.com/zalando/logbook/pull/921) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump dependency-check-maven from 6.0.3 to 6.0.4 [\#920](https://github.com/zalando/logbook/pull/920) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump logstash-logback-encoder from 6.5 to 6.6 [\#919](https://github.com/zalando/logbook/pull/919) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump kotlin-stdlib from 1.4.21 to 1.4.21-2 [\#918](https://github.com/zalando/logbook/pull/918) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- adds LogbookClientHttpRequestInterceptor for spring RestTemplate [\#917](https://github.com/zalando/logbook/pull/917) ([nhomble](https://github.com/nhomble))
- adds host and path to structured log formats [\#916](https://github.com/zalando/logbook/pull/916) ([nhomble](https://github.com/nhomble))
- Bump apiguardian-api from 1.1.0 to 1.1.1 [\#914](https://github.com/zalando/logbook/pull/914) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump jersey-bom from 2.32 to 2.33 [\#913](https://github.com/zalando/logbook/pull/913) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump netty-codec-http from 4.1.55.Final to 4.1.56.Final [\#912](https://github.com/zalando/logbook/pull/912) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Fixed spring versions [\#910](https://github.com/zalando/logbook/pull/910) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump spring-framework-bom from 5.3.1 to 5.3.2 [\#909](https://github.com/zalando/logbook/pull/909) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump json-path-assert from 2.4.0 to 2.5.0 [\#908](https://github.com/zalando/logbook/pull/908) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump jmh.version from 1.26 to 1.27 [\#907](https://github.com/zalando/logbook/pull/907) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump netty-codec-http from 4.1.54.Final to 4.1.55.Final [\#906](https://github.com/zalando/logbook/pull/906) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump kotlin-stdlib from 1.4.20 to 1.4.21 [\#905](https://github.com/zalando/logbook/pull/905) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump reactor-netty from 1.0.1 to 1.0.2 [\#904](https://github.com/zalando/logbook/pull/904) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring-security-web from 5.4.1 to 5.4.2 [\#902](https://github.com/zalando/logbook/pull/902) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Configured maven to avoid Connection Reset issues [\#900](https://github.com/zalando/logbook/pull/900) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump httpcore from 4.4.13 to 4.4.14 [\#899](https://github.com/zalando/logbook/pull/899) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump logstash-logback-encoder from 6.4 to 6.5 [\#898](https://github.com/zalando/logbook/pull/898) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump jackson.version from 2.11.3 to 2.12.0 [\#897](https://github.com/zalando/logbook/pull/897) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito.version from 3.6.0 to 3.6.28 [\#896](https://github.com/zalando/logbook/pull/896) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Prepare next release [\#894](https://github.com/zalando/logbook/pull/894) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump kotlin-stdlib from 1.4.10 to 1.4.20 [\#893](https://github.com/zalando/logbook/pull/893) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring-boot.version from 2.3.5.RELEASE to 2.4.0 [\#892](https://github.com/zalando/logbook/pull/892) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump assertj-core from 3.18.0 to 3.18.1 [\#891](https://github.com/zalando/logbook/pull/891) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump reactor-netty from 1.0.0 to 1.0.1 [\#890](https://github.com/zalando/logbook/pull/890) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump netty-codec-http from 4.1.53.Final to 4.1.54.Final [\#889](https://github.com/zalando/logbook/pull/889) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Changed default Servlet charset to ISO-8859-1 [\#887](https://github.com/zalando/logbook/pull/887) ([whiskeysierra](https://github.com/whiskeysierra))
- Release/2.4.1 [\#886](https://github.com/zalando/logbook/pull/886) ([whiskeysierra](https://github.com/whiskeysierra))
- Spring webflux autoconfiguration [\#863](https://github.com/zalando/logbook/pull/863) ([sokomishalov](https://github.com/sokomishalov))

## [2.4.1](https://github.com/zalando/logbook/tree/2.4.1) (2020-11-09)

[Full Changelog](https://github.com/zalando/logbook/compare/2.4.0...2.4.1)

**Fixed bugs:**

- JSON in formatted style instead of one string [\#885](https://github.com/zalando/logbook/issues/885)

**Closed issues:**

- Logging of SOAP requests/responses [\#883](https://github.com/zalando/logbook/issues/883)
- logbook-springboot: Make it possible to disable body filters with a property [\#809](https://github.com/zalando/logbook/issues/809)
- How to add custom BodyReplacer when using spring-boot-stater? [\#749](https://github.com/zalando/logbook/issues/749)

**Merged pull requests:**

- Fix ClassNotFoundException when LogbookAutoConfiguration is processed [\#882](https://github.com/zalando/logbook/pull/882) ([jstanik](https://github.com/jstanik))
- Bump dependency-check-maven from 6.0.2 to 6.0.3 [\#880](https://github.com/zalando/logbook/pull/880) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Release/2.4.0 [\#879](https://github.com/zalando/logbook/pull/879) ([whiskeysierra](https://github.com/whiskeysierra))

## [2.4.0](https://github.com/zalando/logbook/tree/2.4.0) (2020-11-03)

[Full Changelog](https://github.com/zalando/logbook/compare/2.3.0...2.4.0)

**Fixed bugs:**

- using logbook with spring boot zuul as gateway [\#857](https://github.com/zalando/logbook/issues/857)

**Closed issues:**

- Can I change strategy in runtime? [\#877](https://github.com/zalando/logbook/issues/877)
- ResponseFilter to skip body logging only for certain URLs [\#875](https://github.com/zalando/logbook/issues/875)

**Merged pull requests:**

- Externalize a handler to write json fields [\#878](https://github.com/zalando/logbook/pull/878) ([rjmveloso](https://github.com/rjmveloso))
- Bump spring-boot.version from 2.3.4.RELEASE to 2.3.5.RELEASE [\#876](https://github.com/zalando/logbook/pull/876) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump reactor-netty from 0.9.12.RELEASE to 1.0.0 [\#874](https://github.com/zalando/logbook/pull/874) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito.version from 3.5.15 to 3.6.0 [\#873](https://github.com/zalando/logbook/pull/873) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump assertj-core from 3.17.2 to 3.18.0 [\#872](https://github.com/zalando/logbook/pull/872) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Unifies SECURITY.md to point to the Zalando security form [\#868](https://github.com/zalando/logbook/pull/868) ([bocytko](https://github.com/bocytko))
- Bump mockito.version from 3.5.13 to 3.5.15 [\#867](https://github.com/zalando/logbook/pull/867) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump lombok from 1.18.14 to 1.18.16 [\#866](https://github.com/zalando/logbook/pull/866) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Replaced Hamcrest with AssertJ [\#865](https://github.com/zalando/logbook/pull/865) ([whiskeysierra](https://github.com/whiskeysierra))
- Release/2.3.0 [\#862](https://github.com/zalando/logbook/pull/862) ([whiskeysierra](https://github.com/whiskeysierra))

## [2.3.0](https://github.com/zalando/logbook/tree/2.3.0) (2020-10-14)

[Full Changelog](https://github.com/zalando/logbook/compare/2.2.1...2.3.0)

**Fixed bugs:**

- Can logbook work with log4j2? [\#854](https://github.com/zalando/logbook/issues/854)
- 打印日志中文乱码 \(Print log Chinese garbled\) [\#800](https://github.com/zalando/logbook/issues/800)

**Closed issues:**

- Have a method in JSONBodyFilter to provide a replacement function apart from replacement string [\#852](https://github.com/zalando/logbook/issues/852)
- Provide function in JSON Body filter to obfuscate values rather than replacing completely. [\#851](https://github.com/zalando/logbook/issues/851)
- Provide ability to enable/disable Logbook in runtime [\#845](https://github.com/zalando/logbook/issues/845)

**Merged pull requests:**

- Inlined replacement function implementations [\#861](https://github.com/zalando/logbook/pull/861) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump netty-codec-http from 4.1.52.Final to 4.1.53.Final [\#860](https://github.com/zalando/logbook/pull/860) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Suppressed CVE-2020-5421 [\#859](https://github.com/zalando/logbook/pull/859) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump jmh.version from 1.25.2 to 1.26 [\#858](https://github.com/zalando/logbook/pull/858) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump lombok from 1.18.12 to 1.18.14 [\#856](https://github.com/zalando/logbook/pull/856) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Add feature in JsonBodyFilters to replace value by custom function. [\#855](https://github.com/zalando/logbook/pull/855) ([dnandola-wyze](https://github.com/dnandola-wyze))
- Bump httpclient from 4.5.12 to 4.5.13 [\#850](https://github.com/zalando/logbook/pull/850) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump jackson.version from 2.11.2 to 2.11.3 [\#848](https://github.com/zalando/logbook/pull/848) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Release/2.2.1 [\#847](https://github.com/zalando/logbook/pull/847) ([whiskeysierra](https://github.com/whiskeysierra))

## [2.2.1](https://github.com/zalando/logbook/tree/2.2.1) (2020-09-29)

[Full Changelog](https://github.com/zalando/logbook/compare/2.2.0...2.2.1)

**Fixed bugs:**

- ChunkingSink does not write Requests/Responses without body [\#842](https://github.com/zalando/logbook/issues/842)
- BodyFilter can affect request body processed by Spring Boot! [\#839](https://github.com/zalando/logbook/issues/839)

**Closed issues:**

- LogstashLogbackSink: Replace log.trace with call to HttpLogWriter.write\(\) [\#838](https://github.com/zalando/logbook/issues/838)
- Partial masking [\#755](https://github.com/zalando/logbook/issues/755)

**Merged pull requests:**

- Fixes chunking of empty bodies. [\#846](https://github.com/zalando/logbook/pull/846) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump dependency-check-maven from 6.0.1 to 6.0.2 [\#844](https://github.com/zalando/logbook/pull/844) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump jersey-bom from 2.31 to 2.32 [\#843](https://github.com/zalando/logbook/pull/843) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito.version from 3.5.11 to 3.5.13 [\#841](https://github.com/zalando/logbook/pull/841) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Enabled no-response bot [\#840](https://github.com/zalando/logbook/pull/840) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump mockito.version from 3.5.10 to 3.5.11 [\#837](https://github.com/zalando/logbook/pull/837) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring-boot.version from 2.3.3.RELEASE to 2.3.4.RELEASE [\#836](https://github.com/zalando/logbook/pull/836) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump jacoco-maven-plugin from 0.8.5 to 0.8.6 [\#835](https://github.com/zalando/logbook/pull/835) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump reactor-netty from 0.9.11.RELEASE to 0.9.12.RELEASE [\#834](https://github.com/zalando/logbook/pull/834) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump dependency-check-maven from 5.3.2 to 6.0.1 [\#833](https://github.com/zalando/logbook/pull/833) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump okhttp from 4.8.1 to 4.9.0 [\#832](https://github.com/zalando/logbook/pull/832) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump duplicate-finder-maven-plugin from 1.4.0 to 1.5.0 [\#831](https://github.com/zalando/logbook/pull/831) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump junit.version from 5.6.2 to 5.7.0 [\#830](https://github.com/zalando/logbook/pull/830) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump kotlin-stdlib from 1.4.0 to 1.4.10 [\#829](https://github.com/zalando/logbook/pull/829) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring-security-web from 5.3.4.RELEASE to 5.4.0 [\#828](https://github.com/zalando/logbook/pull/828) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump netty-codec-http from 4.1.51.Final to 4.1.52.Final [\#827](https://github.com/zalando/logbook/pull/827) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito.version from 3.5.2 to 3.5.10 [\#825](https://github.com/zalando/logbook/pull/825) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump jmh.version from 1.25 to 1.25.2 [\#824](https://github.com/zalando/logbook/pull/824) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito.version from 3.5.0 to 3.5.2 [\#819](https://github.com/zalando/logbook/pull/819) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump kotlin-stdlib from 1.3.72 to 1.4.0 [\#818](https://github.com/zalando/logbook/pull/818) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito.version from 3.4.6 to 3.5.0 [\#817](https://github.com/zalando/logbook/pull/817) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Release refs/heads/release/2.2.0 [\#816](https://github.com/zalando/logbook/pull/816) ([github-actions[bot]](https://github.com/apps/github-actions))

## [2.2.0](https://github.com/zalando/logbook/tree/2.2.0) (2020-08-15)

[Full Changelog](https://github.com/zalando/logbook/compare/2.1.4...2.2.0)

**Closed issues:**

- \(Spring Boot\) Access to NativeWebRequest in Sink [\#806](https://github.com/zalando/logbook/issues/806)
- Workin Logbook example? [\#795](https://github.com/zalando/logbook/issues/795)

**Merged pull requests:**

- Extract common part of if [\#815](https://github.com/zalando/logbook/pull/815) ([mrk-andreev](https://github.com/mrk-andreev))
- Suppressed CVE-2020-15824 [\#814](https://github.com/zalando/logbook/pull/814) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump spring-boot.version from 2.3.2.RELEASE to 2.3.3.RELEASE [\#813](https://github.com/zalando/logbook/pull/813) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump jmh.version from 1.24 to 1.25 [\#812](https://github.com/zalando/logbook/pull/812) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Add logstash into spring autoconfiguration and provide logstash raw marker config possibility [\#811](https://github.com/zalando/logbook/pull/811) ([pbouillet](https://github.com/pbouillet))
- Bump maven-resources-plugin from 3.1.0 to 3.2.0 [\#810](https://github.com/zalando/logbook/pull/810) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump reactor-netty from 0.9.10.RELEASE to 0.9.11.RELEASE [\#808](https://github.com/zalando/logbook/pull/808) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump versions-maven-plugin from 2.7 to 2.8.1 [\#807](https://github.com/zalando/logbook/pull/807) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump okhttp from 4.8.0 to 4.8.1 [\#805](https://github.com/zalando/logbook/pull/805) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Release refs/heads/release/2.1.4 [\#804](https://github.com/zalando/logbook/pull/804) ([github-actions[bot]](https://github.com/apps/github-actions))

## [2.1.4](https://github.com/zalando/logbook/tree/2.1.4) (2020-08-06)

[Full Changelog](https://github.com/zalando/logbook/compare/2.1.3...2.1.4)

**Closed issues:**

- Support for JDK 11 [\#776](https://github.com/zalando/logbook/issues/776)

**Merged pull requests:**

- Ensure target bytebuf has enough space [\#803](https://github.com/zalando/logbook/pull/803) ([pbouillet](https://github.com/pbouillet))
- Bump spring-security-web from 5.3.3.RELEASE to 5.3.4.RELEASE [\#802](https://github.com/zalando/logbook/pull/802) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump jmh.version from 1.23 to 1.24 [\#801](https://github.com/zalando/logbook/pull/801) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Release refs/heads/release/2.1.3 [\#799](https://github.com/zalando/logbook/pull/799) ([github-actions[bot]](https://github.com/apps/github-actions))

## [2.1.3](https://github.com/zalando/logbook/tree/2.1.3) (2020-08-03)

[Full Changelog](https://github.com/zalando/logbook/compare/2.1.2...2.1.3)

**Fixed bugs:**

- Missing setter for FormRequestMode in compiled/released JAR [\#797](https://github.com/zalando/logbook/issues/797)

**Merged pull requests:**

- Remove final from formRequestMode property. Fixes \#797 [\#798](https://github.com/zalando/logbook/pull/798) ([jpmaas](https://github.com/jpmaas))
- Bump jackson.version from 2.11.1 to 2.11.2 [\#796](https://github.com/zalando/logbook/pull/796) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Release refs/heads/release/2.1.2 [\#794](https://github.com/zalando/logbook/pull/794) ([github-actions[bot]](https://github.com/apps/github-actions))

## [2.1.2](https://github.com/zalando/logbook/tree/2.1.2) (2020-08-02)

[Full Changelog](https://github.com/zalando/logbook/compare/2.1.1...2.1.2)

**Fixed bugs:**

- JsonBodyFilters.replaceJsonStringProperty not replacing nested properties [\#787](https://github.com/zalando/logbook/issues/787)
- org.zalando.logbook.httpclient.LocalRequest replaces body of HttpEntityEnclosingRequest incorrectly [\#775](https://github.com/zalando/logbook/issues/775)
- Sensitive information in the body of OAuth2 requests not filtered by default [\#762](https://github.com/zalando/logbook/issues/762)
- What is ExcludeTest for? [\#754](https://github.com/zalando/logbook/issues/754)
- Latest logbook-netty is not part of logbook-bom [\#741](https://github.com/zalando/logbook/issues/741)

**Closed issues:**

- Document micronaut support [\#748](https://github.com/zalando/logbook/issues/748)

**Merged pull requests:**

- Updated docs to promote support for Micronaut [\#793](https://github.com/zalando/logbook/pull/793) ([whiskeysierra](https://github.com/whiskeysierra))
- Refactored ExcludeTest to correctly use new setup [\#792](https://github.com/zalando/logbook/pull/792) ([whiskeysierra](https://github.com/whiskeysierra))
- Extracted copying HTTP entity into reusable method [\#791](https://github.com/zalando/logbook/pull/791) ([whiskeysierra](https://github.com/whiskeysierra))
- Enabled oauth request filter \(form\) by default [\#790](https://github.com/zalando/logbook/pull/790) ([whiskeysierra](https://github.com/whiskeysierra))
- Fixed incorrect logbook-netty version in bom [\#789](https://github.com/zalando/logbook/pull/789) ([whiskeysierra](https://github.com/whiskeysierra))
- Release refs/heads/release/2.1.1 [\#788](https://github.com/zalando/logbook/pull/788) ([github-actions[bot]](https://github.com/apps/github-actions))

## [2.1.1](https://github.com/zalando/logbook/tree/2.1.1) (2020-08-01)

[Full Changelog](https://github.com/zalando/logbook/compare/2.1.0...2.1.1)

**Fixed bugs:**

- PrimitiveJsonPropertyBodyFilter regexp is incorrect [\#780](https://github.com/zalando/logbook/issues/780)
- I can not find logs in console using spring boot app, I have just installed `logbook-spring-boot-starter` and add `logging.level.org.zalando.logbook.Logbook=TRACE` to my common.properties file [\#768](https://github.com/zalando/logbook/issues/768)

**Merged pull requests:**

- Bump mockito.version from 3.4.4 to 3.4.6 [\#786](https://github.com/zalando/logbook/pull/786) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump Paguro from 3.1.2 to 3.1.4 [\#785](https://github.com/zalando/logbook/pull/785) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito.version from 3.3.3 to 3.4.4 [\#784](https://github.com/zalando/logbook/pull/784) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring-boot.version from 2.3.1.RELEASE to 2.3.2.RELEASE [\#783](https://github.com/zalando/logbook/pull/783) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Nested properties are now filtered correctly [\#782](https://github.com/zalando/logbook/pull/782) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump reactor-netty from 0.9.6.RELEASE to 0.9.10.RELEASE [\#781](https://github.com/zalando/logbook/pull/781) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump okhttp from 4.4.1 to 4.8.0 [\#779](https://github.com/zalando/logbook/pull/779) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump netty-codec-http from 4.1.48.Final to 4.1.51.Final [\#777](https://github.com/zalando/logbook/pull/777) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump logstash-logback-encoder from 6.3 to 6.4 [\#773](https://github.com/zalando/logbook/pull/773) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump jackson.version from 2.10.3 to 2.11.1 [\#771](https://github.com/zalando/logbook/pull/771) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bumped spring versions [\#770](https://github.com/zalando/logbook/pull/770) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump maven-shade-plugin from 3.2.2 to 3.2.4 [\#765](https://github.com/zalando/logbook/pull/765) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump jersey-bom from 2.30.1 to 2.31 [\#764](https://github.com/zalando/logbook/pull/764) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump kotlin-stdlib from 1.3.71 to 1.3.72 [\#747](https://github.com/zalando/logbook/pull/747) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump junit.version from 5.6.1 to 5.6.2 [\#745](https://github.com/zalando/logbook/pull/745) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Release refs/heads/release/2.1.0 [\#739](https://github.com/zalando/logbook/pull/739) ([github-actions[bot]](https://github.com/apps/github-actions))
- Bump kotlin-stdlib from 1.3.70 to 1.3.71 [\#738](https://github.com/zalando/logbook/pull/738) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump dependency-check-maven from 5.3.1 to 5.3.2 [\#737](https://github.com/zalando/logbook/pull/737) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring-boot.version from 2.2.5.RELEASE to 2.2.6.RELEASE [\#736](https://github.com/zalando/logbook/pull/736) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump reactor-netty from 0.9.5.RELEASE to 0.9.6.RELEASE [\#735](https://github.com/zalando/logbook/pull/735) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))

## [2.1.0](https://github.com/zalando/logbook/tree/2.1.0) (2020-03-28)

[Full Changelog](https://github.com/zalando/logbook/compare/2.0.0...2.1.0)

**Fixed bugs:**

- Security Vulnerability in DefaultCorrolationId.java [\#709](https://github.com/zalando/logbook/issues/709)
- StackOverflow for JsonBodyFilter [\#689](https://github.com/zalando/logbook/issues/689)
- StackOverflowError for requests with long property values containing escaped double quotes after updating from RC.6 to RC.8 [\#686](https://github.com/zalando/logbook/issues/686)
- replaceJsonStringProperty BodyFilter not working for values with quotation marks [\#462](https://github.com/zalando/logbook/issues/462)

**Closed issues:**

- Obfuscating cookies? [\#705](https://github.com/zalando/logbook/issues/705)
- "Required request body is missing" when POSTing form [\#701](https://github.com/zalando/logbook/issues/701)
- Add in the HTTP Method + path to the outgoing response log [\#700](https://github.com/zalando/logbook/issues/700)
- JavaDoc of JsonHttpLogFormatter isn't in line with method signatures [\#691](https://github.com/zalando/logbook/issues/691)
- Micronaut configuration [\#629](https://github.com/zalando/logbook/issues/629)
- Optimize body filters [\#493](https://github.com/zalando/logbook/issues/493)
- Proper support for ERROR dispatch [\#492](https://github.com/zalando/logbook/issues/492)
- Support to Spring Webflux \(reactive handlers\) [\#331](https://github.com/zalando/logbook/issues/331)
- Re-evaluate and optimize regular expressions [\#304](https://github.com/zalando/logbook/issues/304)
- HTTP log not displaying Remote Address [\#598](https://github.com/zalando/logbook/issues/598)
- Provide fast alternative for JSON compacting [\#446](https://github.com/zalando/logbook/issues/446)
- Logbook HttpRequest/HttpResponse TCK [\#441](https://github.com/zalando/logbook/issues/441)
- Cache request/response headers [\#425](https://github.com/zalando/logbook/issues/425)

**Merged pull requests:**

- Bump junit.version from 5.6.0 to 5.6.1 [\#734](https://github.com/zalando/logbook/pull/734) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump netty-codec-http from 4.1.47.Final to 4.1.48.Final [\#733](https://github.com/zalando/logbook/pull/733) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito.version from 3.3.0 to 3.3.3 [\#732](https://github.com/zalando/logbook/pull/732) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump maven-javadoc-plugin from 3.1.1 to 3.2.0 [\#731](https://github.com/zalando/logbook/pull/731) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump netty-codec-http from 4.1.46.Final to 4.1.47.Final [\#730](https://github.com/zalando/logbook/pull/730) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump dependency-check-maven from 5.3.0 to 5.3.1 [\#729](https://github.com/zalando/logbook/pull/729) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump okhttp from 4.4.0 to 4.4.1 [\#728](https://github.com/zalando/logbook/pull/728) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump httpclient from 4.5.11 to 4.5.12 [\#727](https://github.com/zalando/logbook/pull/727) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring-security-web from 5.2.2.RELEASE to 5.3.0.RELEASE [\#726](https://github.com/zalando/logbook/pull/726) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump kotlin-stdlib from 1.3.61 to 1.3.70 [\#725](https://github.com/zalando/logbook/pull/725) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump jackson.version from 2.10.2 to 2.10.3 [\#724](https://github.com/zalando/logbook/pull/724) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump netty-codec-http from 4.1.45.Final to 4.1.46.Final [\#723](https://github.com/zalando/logbook/pull/723) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring-boot.version from 2.2.4.RELEASE to 2.2.5.RELEASE [\#722](https://github.com/zalando/logbook/pull/722) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump reactor-netty from 0.9.4.RELEASE to 0.9.5.RELEASE [\#721](https://github.com/zalando/logbook/pull/721) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Fixed HttpHeaders support [\#720](https://github.com/zalando/logbook/pull/720) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump mockito.version from 3.2.4 to 3.3.0 [\#719](https://github.com/zalando/logbook/pull/719) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump jersey-bom from 2.30 to 2.30.1 [\#718](https://github.com/zalando/logbook/pull/718) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump okhttp from 4.3.1 to 4.4.0 [\#717](https://github.com/zalando/logbook/pull/717) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Fixed typo \(I guess :\)\) [\#716](https://github.com/zalando/logbook/pull/716) ([N4zroth](https://github.com/N4zroth))
- Switched to immutable, persistent data structures for HTTP headers [\#715](https://github.com/zalando/logbook/pull/715) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump maven-shade-plugin from 3.2.1 to 3.2.2 [\#714](https://github.com/zalando/logbook/pull/714) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Added ability to merge/optimize body filters  [\#713](https://github.com/zalando/logbook/pull/713) ([whiskeysierra](https://github.com/whiskeysierra))
- Fixed stack overflow for huge properties with many escaped double quotes [\#712](https://github.com/zalando/logbook/pull/712) ([whiskeysierra](https://github.com/whiskeysierra))
- Fixed old code example in javadoc [\#711](https://github.com/zalando/logbook/pull/711) ([whiskeysierra](https://github.com/whiskeysierra))
- Made form request mode configurable via configuration properties [\#710](https://github.com/zalando/logbook/pull/710) ([whiskeysierra](https://github.com/whiskeysierra))
- Added Netty modules [\#708](https://github.com/zalando/logbook/pull/708) ([whiskeysierra](https://github.com/whiskeysierra))
- Added Cookie/Set-Cookie header filter [\#707](https://github.com/zalando/logbook/pull/707) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump lombok from 1.18.10 to 1.18.12 [\#706](https://github.com/zalando/logbook/pull/706) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring-security-bom from 5.2.1.RELEASE to 5.2.2.RELEASE [\#704](https://github.com/zalando/logbook/pull/704) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Fix inline code Markdown link in README [\#703](https://github.com/zalando/logbook/pull/703) ([markrmullan](https://github.com/markrmullan))
- Disabled coverage step for forks [\#702](https://github.com/zalando/logbook/pull/702) ([whiskeysierra](https://github.com/whiskeysierra))
- Use @Configuration\(proxyBeanMethods=false\) to avoid unnecessary gener… [\#699](https://github.com/zalando/logbook/pull/699) ([PascalSchumacher](https://github.com/PascalSchumacher))
- Release refs/heads/release/2.0.0 [\#698](https://github.com/zalando/logbook/pull/698) ([github-actions[bot]](https://github.com/apps/github-actions))

## [2.0.0](https://github.com/zalando/logbook/tree/2.0.0) (2020-01-23)

[Full Changelog](https://github.com/zalando/logbook/compare/2.0.0-RC.8...2.0.0)

**Fixed bugs:**

- Logbook BOM is forcing Spring version in a project using it [\#562](https://github.com/zalando/logbook/issues/562)
- SecureLogbookFilter always drops request body [\#439](https://github.com/zalando/logbook/issues/439)
- ServletInputStreamAdapter do not implement ServletInputStream abstract class methods for servlet 3.1 and above [\#355](https://github.com/zalando/logbook/issues/355)
- Chunking should only affect the body [\#322](https://github.com/zalando/logbook/issues/322)
- OutOfMemoryError produced by huge response, that must be excluded. [\#315](https://github.com/zalando/logbook/issues/315)
- Conflict with Spring Boot ErrorPageFilter [\#211](https://github.com/zalando/logbook/issues/211)

**Closed issues:**

- Release Version 2.0 [\#549](https://github.com/zalando/logbook/issues/549)
- Spring Boot login page issue [\#460](https://github.com/zalando/logbook/issues/460)
- Separate auto configuration from starter [\#450](https://github.com/zalando/logbook/issues/450)
- Rename compound to composite [\#447](https://github.com/zalando/logbook/issues/447)
- Provide ready-to-use strategies [\#440](https://github.com/zalando/logbook/issues/440)
- Remove ability to configure log level/category [\#435](https://github.com/zalando/logbook/issues/435)
- Logbook: JSON module [\#373](https://github.com/zalando/logbook/issues/373)
- Pretty-print json body [\#372](https://github.com/zalando/logbook/issues/372)
- Remove compacting functionality from json formatter [\#361](https://github.com/zalando/logbook/issues/361)
- SecurityStrategy logs twice [\#334](https://github.com/zalando/logbook/issues/334)
- Use org.slf4j.event.Level [\#321](https://github.com/zalando/logbook/issues/321)
- Introduce strategy pattern [\#296](https://github.com/zalando/logbook/issues/296)
- Allow non-text-based formats [\#295](https://github.com/zalando/logbook/issues/295)

**Merged pull requests:**

- Bump jmh.version from 1.22 to 1.23 [\#697](https://github.com/zalando/logbook/pull/697) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring-boot-dependencies from 2.2.3.RELEASE to 2.2.4.RELEASE [\#696](https://github.com/zalando/logbook/pull/696) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump junit-bom from 5.5.2 to 5.6.0 [\#695](https://github.com/zalando/logbook/pull/695) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump junit-jupiter.version from 5.5.2 to 5.6.0 [\#694](https://github.com/zalando/logbook/pull/694) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump httpclient from 4.5.10 to 4.5.11 [\#693](https://github.com/zalando/logbook/pull/693) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring-boot-dependencies from 2.2.2.RELEASE to 2.2.3.RELEASE [\#692](https://github.com/zalando/logbook/pull/692) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump dependency-check-maven from 5.2.4 to 5.3.0 [\#690](https://github.com/zalando/logbook/pull/690) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump httpcore from 4.4.12 to 4.4.13 [\#688](https://github.com/zalando/logbook/pull/688) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring.version from 5.2.2.RELEASE to 5.2.3.RELEASE [\#687](https://github.com/zalando/logbook/pull/687) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump jersey-bom from 2.29.1 to 2.30 [\#685](https://github.com/zalando/logbook/pull/685) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Release refs/heads/release/2.0.0-RC.8 [\#684](https://github.com/zalando/logbook/pull/684) ([github-actions[bot]](https://github.com/apps/github-actions))

## [2.0.0-RC.8](https://github.com/zalando/logbook/tree/2.0.0-RC.8) (2020-01-09)

[Full Changelog](https://github.com/zalando/logbook/compare/2.0.0-RC.7...2.0.0-RC.8)

**Fixed bugs:**

- StackOverflowError for requests with long properties after updating from RC.6 to RC.7 [\#681](https://github.com/zalando/logbook/issues/681)
- Share version between mockito-core and mockito-junit-jupiter [\#676](https://github.com/zalando/logbook/issues/676)
- AsyncDispatchTest is flaky [\#672](https://github.com/zalando/logbook/issues/672)
- Duplicate logs when REST API returns HTTP Status Code 401 or 403. [\#656](https://github.com/zalando/logbook/issues/656)

**Merged pull requests:**

- Bump okhttp from 4.3.0 to 4.3.1 [\#683](https://github.com/zalando/logbook/pull/683) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Fixed stackoverflow for large json values [\#682](https://github.com/zalando/logbook/pull/682) ([whiskeysierra](https://github.com/whiskeysierra))
- Fixed flaky test [\#680](https://github.com/zalando/logbook/pull/680) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump jackson.version from 2.10.1 to 2.10.2 [\#679](https://github.com/zalando/logbook/pull/679) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump okhttp from 4.2.2 to 4.3.0 [\#678](https://github.com/zalando/logbook/pull/678) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump maven-source-plugin from 3.2.0 to 3.2.1 [\#677](https://github.com/zalando/logbook/pull/677) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito-junit-jupiter from 3.2.0 to 3.2.4 [\#675](https://github.com/zalando/logbook/pull/675) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump slf4j.version from 1.7.29 to 1.7.30 [\#674](https://github.com/zalando/logbook/pull/674) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito-core from 3.2.0 to 3.2.4 [\#673](https://github.com/zalando/logbook/pull/673) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Release refs/heads/release/2.0.0-RC.7 [\#671](https://github.com/zalando/logbook/pull/671) ([github-actions[bot]](https://github.com/apps/github-actions))

## [2.0.0-RC.7](https://github.com/zalando/logbook/tree/2.0.0-RC.7) (2019-12-18)

[Full Changelog](https://github.com/zalando/logbook/compare/2.0.0-RC.6...2.0.0-RC.7)

**Fixed bugs:**

- Incorrect configuration for default Request and Response filters [\#669](https://github.com/zalando/logbook/issues/669)
- org.zalando.logbook.okhttp.LocalRequest doesn't compile? [\#667](https://github.com/zalando/logbook/issues/667)
- logbook doesn't work with Spring boot logback access [\#663](https://github.com/zalando/logbook/issues/663)
- JsonBodyFilters doesn't read arrays [\#659](https://github.com/zalando/logbook/issues/659)
- Unable to log HTTP Response for Multipart Requests [\#657](https://github.com/zalando/logbook/issues/657)
- All null JSON fields get masked [\#653](https://github.com/zalando/logbook/issues/653)
- Can't get unauthorized requests to filter out [\#640](https://github.com/zalando/logbook/issues/640)
- Response is not getting logged for Jax-RS async apis [\#621](https://github.com/zalando/logbook/issues/621)

**Closed issues:**

- Predicate version for QueryFilter missing [\#658](https://github.com/zalando/logbook/issues/658)
- Make replaceJsonStringProperty regexp more general [\#652](https://github.com/zalando/logbook/issues/652)
- Bean to override the corresponding ID [\#646](https://github.com/zalando/logbook/issues/646)

**Merged pull requests:**

- Fixed wrong default configuration in README [\#670](https://github.com/zalando/logbook/pull/670) ([whiskeysierra](https://github.com/whiskeysierra))
- Updated build badge [\#668](https://github.com/zalando/logbook/pull/668) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump logstash-logback-encoder from 6.2 to 6.3 [\#666](https://github.com/zalando/logbook/pull/666) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring-boot-dependencies from 2.2.1.RELEASE to 2.2.2.RELEASE [\#665](https://github.com/zalando/logbook/pull/665) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Added logback-access to alternatives [\#664](https://github.com/zalando/logbook/pull/664) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump spring.version from 5.2.1.RELEASE to 5.2.2.RELEASE [\#662](https://github.com/zalando/logbook/pull/662) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Made correlation id customizable [\#661](https://github.com/zalando/logbook/pull/661) ([whiskeysierra](https://github.com/whiskeysierra))
- Added query replace predicate support [\#660](https://github.com/zalando/logbook/pull/660) ([whiskeysierra](https://github.com/whiskeysierra))
- Added property predicate to JSON filters [\#655](https://github.com/zalando/logbook/pull/655) ([whiskeysierra](https://github.com/whiskeysierra))
- Unrelated null properties are left unchanged [\#654](https://github.com/zalando/logbook/pull/654) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump mockito-core from 3.1.0 to 3.2.0 [\#651](https://github.com/zalando/logbook/pull/651) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito-junit-jupiter from 3.1.0 to 3.2.0 [\#650](https://github.com/zalando/logbook/pull/650) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump kotlin-stdlib from 1.3.60 to 1.3.61 [\#649](https://github.com/zalando/logbook/pull/649) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring-test-junit5 from 1.3.0 to 1.5.0 [\#648](https://github.com/zalando/logbook/pull/648) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump maven-enforcer-plugin from 3.0.0-M2 to 3.0.0-M3 [\#647](https://github.com/zalando/logbook/pull/647) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Switched from Travis to Github Actions [\#645](https://github.com/zalando/logbook/pull/645) ([whiskeysierra](https://github.com/whiskeysierra))
- Release 2.0.0-RC.6 [\#644](https://github.com/zalando/logbook/pull/644) ([whiskeysierra](https://github.com/whiskeysierra))
- Create release-pull-request.yml [\#643](https://github.com/zalando/logbook/pull/643) ([whiskeysierra](https://github.com/whiskeysierra))
- Create ci.yml [\#642](https://github.com/zalando/logbook/pull/642) ([whiskeysierra](https://github.com/whiskeysierra))
- Async dispatch support [\#624](https://github.com/zalando/logbook/pull/624) ([whiskeysierra](https://github.com/whiskeysierra))

## [2.0.0-RC.6](https://github.com/zalando/logbook/tree/2.0.0-RC.6) (2019-11-20)

[Full Changelog](https://github.com/zalando/logbook/compare/2.0.0-RC.5...2.0.0-RC.6)

**Fixed bugs:**

- Can not handle multipart/form-data [\#637](https://github.com/zalando/logbook/issues/637)
- Request parameters not obtained in springboot2.2.0 [\#627](https://github.com/zalando/logbook/issues/627)
- Response is not getting logged for Jax-RS async apis [\#620](https://github.com/zalando/logbook/issues/620)

**Closed issues:**

- Logbook HttpRequest/HttpResponse TCK [\#441](https://github.com/zalando/logbook/issues/441)

**Merged pull requests:**

- Create release.yml [\#641](https://github.com/zalando/logbook/pull/641) ([whiskeysierra](https://github.com/whiskeysierra))
- Bugfix/multipart [\#639](https://github.com/zalando/logbook/pull/639) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump kotlin-stdlib from 1.3.50 to 1.3.60 [\#638](https://github.com/zalando/logbook/pull/638) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump dependency-check-maven from 5.2.2 to 5.2.4 [\#636](https://github.com/zalando/logbook/pull/636) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump jackson.version from 2.10.0 to 2.10.1 [\#635](https://github.com/zalando/logbook/pull/635) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring-boot-dependencies from 2.2.0.RELEASE to 2.2.1.RELEASE [\#634](https://github.com/zalando/logbook/pull/634) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring.version from 5.2.0.RELEASE to 5.2.1.RELEASE [\#633](https://github.com/zalando/logbook/pull/633) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring-security-bom from 5.2.0.RELEASE to 5.2.1.RELEASE [\#632](https://github.com/zalando/logbook/pull/632) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump maven-source-plugin from 3.1.0 to 3.2.0 [\#631](https://github.com/zalando/logbook/pull/631) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump jmh.version from 1.21 to 1.22 [\#630](https://github.com/zalando/logbook/pull/630) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump slf4j.version from 1.7.28 to 1.7.29 [\#628](https://github.com/zalando/logbook/pull/628) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump duplicate-finder-maven-plugin from 1.3.0 to 1.4.0 [\#626](https://github.com/zalando/logbook/pull/626) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring-boot-dependencies from 2.1.9.RELEASE to 2.2.0.RELEASE [\#623](https://github.com/zalando/logbook/pull/623) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump hamcrest from 2.1 to 2.2 [\#622](https://github.com/zalando/logbook/pull/622) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Release 2.0.0-RC.5 [\#619](https://github.com/zalando/logbook/pull/619) ([whiskeysierra](https://github.com/whiskeysierra))

## [2.0.0-RC.5](https://github.com/zalando/logbook/tree/2.0.0-RC.5) (2019-10-14)

[Full Changelog](https://github.com/zalando/logbook/compare/2.0.0-RC.4...2.0.0-RC.5)

**Fixed bugs:**

- Logbook removes body from response [\#603](https://github.com/zalando/logbook/issues/603)

**Closed issues:**

- HTTP log not displaying Remote Address [\#598](https://github.com/zalando/logbook/issues/598)

**Merged pull requests:**

- Bump jacoco-maven-plugin from 0.8.4 to 0.8.5 [\#618](https://github.com/zalando/logbook/pull/618) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Fixed missing flush for writer [\#617](https://github.com/zalando/logbook/pull/617) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump okhttp from 4.2.1 to 4.2.2 [\#616](https://github.com/zalando/logbook/pull/616) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring-boot-dependencies from 2.1.8.RELEASE to 2.1.9.RELEASE [\#615](https://github.com/zalando/logbook/pull/615) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump okhttp from 4.2.0 to 4.2.1 [\#614](https://github.com/zalando/logbook/pull/614) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
-  Release 2.0.0-RC.4  [\#613](https://github.com/zalando/logbook/pull/613) ([whiskeysierra](https://github.com/whiskeysierra))

## [2.0.0-RC.4](https://github.com/zalando/logbook/tree/2.0.0-RC.4) (2019-10-02)

[Full Changelog](https://github.com/zalando/logbook/compare/2.0.0-RC.3...2.0.0-RC.4)

**Fixed bugs:**

- Exception while running with Zalando Riptide [\#592](https://github.com/zalando/logbook/issues/592)

**Closed issues:**

- Enable log request body and not log response body when status \< 400 [\#577](https://github.com/zalando/logbook/issues/577)

**Merged pull requests:**

- Bump mockito-junit-jupiter from 3.0.0 to 3.1.0 [\#612](https://github.com/zalando/logbook/pull/612) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito-core from 3.0.0 to 3.1.0 [\#611](https://github.com/zalando/logbook/pull/611) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring-security-bom from 5.1.6.RELEASE to 5.2.0.RELEASE [\#610](https://github.com/zalando/logbook/pull/610) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring.version from 5.1.9.RELEASE to 5.2.0.RELEASE [\#609](https://github.com/zalando/logbook/pull/609) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Adding Remote in HTTP Incoming Request log [\#608](https://github.com/zalando/logbook/pull/608) ([anirudh708](https://github.com/anirudh708))
- Bump jackson.version from 2.10.0.pr2 to 2.10.0 [\#606](https://github.com/zalando/logbook/pull/606) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump dependency-check-maven from 5.2.1 to 5.2.2 [\#605](https://github.com/zalando/logbook/pull/605) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump lombok from 1.18.8 to 1.18.10 [\#602](https://github.com/zalando/logbook/pull/602) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump jersey-bom from 2.29 to 2.29.1 [\#601](https://github.com/zalando/logbook/pull/601) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump httpclient from 4.5.9 to 4.5.10 [\#600](https://github.com/zalando/logbook/pull/600) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump okhttp from 4.1.1 to 4.2.0 [\#599](https://github.com/zalando/logbook/pull/599) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump logstash-logback-encoder from 6.1 to 6.2 [\#597](https://github.com/zalando/logbook/pull/597) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump junit-jupiter.version from 5.5.1 to 5.5.2 [\#596](https://github.com/zalando/logbook/pull/596) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump junit-bom from 5.5.1 to 5.5.2 [\#595](https://github.com/zalando/logbook/pull/595) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump okhttp from 4.1.0 to 4.1.1 [\#594](https://github.com/zalando/logbook/pull/594) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring-boot-dependencies from 2.1.7.RELEASE to 2.1.8.RELEASE [\#593](https://github.com/zalando/logbook/pull/593) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump httpcore from 4.4.11 to 4.4.12 [\#591](https://github.com/zalando/logbook/pull/591) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Updated maven [\#590](https://github.com/zalando/logbook/pull/590) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump jackson.version from 2.10.0.pr1 to 2.10.0.pr2 [\#589](https://github.com/zalando/logbook/pull/589) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump kotlin-stdlib from 1.3.41 to 1.3.50 [\#588](https://github.com/zalando/logbook/pull/588) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
-  Release 2.0.0-RC.3 [\#587](https://github.com/zalando/logbook/pull/587) ([whiskeysierra](https://github.com/whiskeysierra))
- Streaming / more low-level JSON processing [\#584](https://github.com/zalando/logbook/pull/584) ([skjolber](https://github.com/skjolber))
- Added fast json formatter [\#580](https://github.com/zalando/logbook/pull/580) ([whiskeysierra](https://github.com/whiskeysierra))

## [2.0.0-RC.3](https://github.com/zalando/logbook/tree/2.0.0-RC.3) (2019-08-23)

[Full Changelog](https://github.com/zalando/logbook/compare/2.0.0-RC.2...2.0.0-RC.3)

**Fixed bugs:**

- PUT method with no body [\#585](https://github.com/zalando/logbook/issues/585)

**Merged pull requests:**

- GH-585 Fixed PUT without body [\#586](https://github.com/zalando/logbook/pull/586) ([whiskeysierra](https://github.com/whiskeysierra))
- Release 2.0.0-RC.2 [\#583](https://github.com/zalando/logbook/pull/583) ([whiskeysierra](https://github.com/whiskeysierra))

## [2.0.0-RC.2](https://github.com/zalando/logbook/tree/2.0.0-RC.2) (2019-08-21)

[Full Changelog](https://github.com/zalando/logbook/compare/2.0.0-RC.1...2.0.0-RC.2)

**Fixed bugs:**

- Obfuscation not working on okhttpclient request with strategy [\#581](https://github.com/zalando/logbook/issues/581)
- Logbook did not work with spring-boot 2.1.6 [\#559](https://github.com/zalando/logbook/issues/559)
- Logbook.strategy property is not defined in spring-configuration-metadata.json [\#552](https://github.com/zalando/logbook/issues/552)
- Using JDK 11 getting NoClassDefFoundError [\#518](https://github.com/zalando/logbook/issues/518)
- BOM missing logbook-logstash for 2.0.0-RC.1 [\#502](https://github.com/zalando/logbook/issues/502)

**Security fixes:**

- \[Security\] Bump jackson.version from 2.9.9 to 2.10.0.pr1 [\#563](https://github.com/zalando/logbook/pull/563) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))

**Closed issues:**

- Learning as I go with springboot [\#560](https://github.com/zalando/logbook/issues/560)
- Noob example springboot [\#557](https://github.com/zalando/logbook/issues/557)
- Add support for Apache Common Log Format [\#553](https://github.com/zalando/logbook/issues/553)
- Can we save logs into the DB? [\#539](https://github.com/zalando/logbook/issues/539)
- response is not logged if http status is 200/ok [\#531](https://github.com/zalando/logbook/issues/531)
- Filter Logs by status code [\#521](https://github.com/zalando/logbook/issues/521)
- Spring Boot Starter : override bean cause cyclic error [\#517](https://github.com/zalando/logbook/issues/517)
- Travis build fails on Oracle JDK [\#514](https://github.com/zalando/logbook/issues/514)
- Logstash format produces unparseable JSON when strategy is body-only-if-status-at-least [\#505](https://github.com/zalando/logbook/issues/505)
- Inject Logback logger into log writer, rather than a static field [\#504](https://github.com/zalando/logbook/issues/504)
- Support for filtering path [\#499](https://github.com/zalando/logbook/issues/499)
- Access to the logging filter to add additional functionality [\#494](https://github.com/zalando/logbook/issues/494)
- Add a way to replace non String Json Properties [\#478](https://github.com/zalando/logbook/issues/478)
- Provide fast alternative for JSON compacting [\#446](https://github.com/zalando/logbook/issues/446)

**Merged pull requests:**

- Filtering is now preserved when discarding body [\#582](https://github.com/zalando/logbook/pull/582) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump okhttp from 4.0.1 to 4.1.0 [\#579](https://github.com/zalando/logbook/pull/579) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump slf4j.version from 1.7.27 to 1.7.28 [\#578](https://github.com/zalando/logbook/pull/578) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Disabled tests/checks for faster releases [\#576](https://github.com/zalando/logbook/pull/576) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump spring-boot-dependencies from 2.1.6.RELEASE to 2.1.7.RELEASE [\#575](https://github.com/zalando/logbook/pull/575) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump slf4j.version from 1.7.26 to 1.7.27 [\#574](https://github.com/zalando/logbook/pull/574) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Added emojis to issue template names [\#573](https://github.com/zalando/logbook/pull/573) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump spring-security-bom from 5.1.5.RELEASE to 5.1.6.RELEASE [\#572](https://github.com/zalando/logbook/pull/572) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump dependency-check-maven from 5.2.0 to 5.2.1 [\#571](https://github.com/zalando/logbook/pull/571) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring.version from 5.1.8.RELEASE to 5.1.9.RELEASE [\#570](https://github.com/zalando/logbook/pull/570) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Added support for Common Log Format [\#569](https://github.com/zalando/logbook/pull/569) ([whiskeysierra](https://github.com/whiskeysierra))
- Fixed typo in README.md [\#568](https://github.com/zalando/logbook/pull/568) ([bocytko](https://github.com/bocytko))
- Removed direct dependency to junit-platform [\#567](https://github.com/zalando/logbook/pull/567) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump junit-jupiter.version from 5.5.0 to 5.5.1 [\#566](https://github.com/zalando/logbook/pull/566) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump junit-platform.version from 1.5.0 to 1.5.1 [\#565](https://github.com/zalando/logbook/pull/565) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump dependency-check-maven from 5.1.1 to 5.2.0 [\#564](https://github.com/zalando/logbook/pull/564) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump dependency-check-maven from 5.1.0 to 5.1.1 [\#561](https://github.com/zalando/logbook/pull/561) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump okhttp from 4.0.0 to 4.0.1 [\#558](https://github.com/zalando/logbook/pull/558) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito-junit-jupiter from 2.28.2 to 3.0.0 [\#556](https://github.com/zalando/logbook/pull/556) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito-core from 2.28.2 to 3.0.0 [\#555](https://github.com/zalando/logbook/pull/555) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Added spring metadata for logbook.strategy [\#554](https://github.com/zalando/logbook/pull/554) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump maven-javadoc-plugin from 3.1.0 to 3.1.1 [\#551](https://github.com/zalando/logbook/pull/551) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Properly suppressed Spring Security vulnerability [\#550](https://github.com/zalando/logbook/pull/550) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump dependency-check-maven from 4.0.2 to 5.1.0 [\#548](https://github.com/zalando/logbook/pull/548) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump junit-jupiter.version from 5.4.2 to 5.5.0 [\#547](https://github.com/zalando/logbook/pull/547) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump junit-platform.version from 1.4.2 to 1.5.0 [\#546](https://github.com/zalando/logbook/pull/546) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Add note about Spring Boot Starter dependency to README [\#545](https://github.com/zalando/logbook/pull/545) ([marcelstoer](https://github.com/marcelstoer))
- Reuse Jackson JsonFactory, as it is thread safe [\#544](https://github.com/zalando/logbook/pull/544) ([skjolber](https://github.com/skjolber))
- Bump okhttp from 3.14.2 to 4.0.0 [\#543](https://github.com/zalando/logbook/pull/543) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump jersey-bom from 2.28 to 2.29 [\#542](https://github.com/zalando/logbook/pull/542) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring-boot-dependencies from 2.1.5.RELEASE to 2.1.6.RELEASE [\#540](https://github.com/zalando/logbook/pull/540) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Added labels to issue templates [\#538](https://github.com/zalando/logbook/pull/538) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump logstash-logback-encoder from 6.0 to 6.1 [\#537](https://github.com/zalando/logbook/pull/537) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring.version from 5.1.7.RELEASE to 5.1.8.RELEASE [\#535](https://github.com/zalando/logbook/pull/535) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump httpclient from 4.5.8 to 4.5.9 [\#534](https://github.com/zalando/logbook/pull/534) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump apiguardian-api from 1.0.0 to 1.1.0 [\#532](https://github.com/zalando/logbook/pull/532) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Added primitive json number replacement filter [\#530](https://github.com/zalando/logbook/pull/530) ([whiskeysierra](https://github.com/whiskeysierra))
- Improve JsonMediaType performance [\#529](https://github.com/zalando/logbook/pull/529) ([skjolber](https://github.com/skjolber))
- Added fast compactor alternative [\#528](https://github.com/zalando/logbook/pull/528) ([whiskeysierra](https://github.com/whiskeysierra))
- Fixed incorrect group id in README [\#527](https://github.com/zalando/logbook/pull/527) ([whiskeysierra](https://github.com/whiskeysierra))
- Formatting correcting dependency of the logbook-json in Readme.md [\#526](https://github.com/zalando/logbook/pull/526) ([LimaIsaias](https://github.com/LimaIsaias))
- Unit tests and benchmarks for JsonMediaType [\#525](https://github.com/zalando/logbook/pull/525) ([skjolber](https://github.com/skjolber))
- Improve performance of DefaultHttpLogFormatter [\#524](https://github.com/zalando/logbook/pull/524) ([skjolber](https://github.com/skjolber))
- Bump maven-shade-plugin from 2.2 to 3.2.1 [\#523](https://github.com/zalando/logbook/pull/523) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump logstash-logback-encoder from 5.3 to 6.0 [\#522](https://github.com/zalando/logbook/pull/522) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito-junit-jupiter from 2.27.5 to 2.28.2 [\#520](https://github.com/zalando/logbook/pull/520) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito-core from 2.27.0 to 2.28.2 [\#519](https://github.com/zalando/logbook/pull/519) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito-junit-jupiter from 2.27.0 to 2.27.5 [\#516](https://github.com/zalando/logbook/pull/516) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Move travis-ci to openjdk8 [\#515](https://github.com/zalando/logbook/pull/515) ([skjolber](https://github.com/skjolber))
- Support for filtering request path [\#513](https://github.com/zalando/logbook/pull/513) ([skjolber](https://github.com/skjolber))
- Jackson-based body filter for JSON [\#512](https://github.com/zalando/logbook/pull/512) ([skjolber](https://github.com/skjolber))
- JMH module [\#511](https://github.com/zalando/logbook/pull/511) ([skjolber](https://github.com/skjolber))
- Bump maven-source-plugin from 3.0.1 to 3.1.0 [\#510](https://github.com/zalando/logbook/pull/510) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump okhttp from 3.14.1 to 3.14.2 [\#509](https://github.com/zalando/logbook/pull/509) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Adjust logging of empty or filtered body [\#508](https://github.com/zalando/logbook/pull/508) ([skjolber](https://github.com/skjolber))
- Bump spring-boot-dependencies from 2.1.4.RELEASE to 2.1.5.RELEASE [\#507](https://github.com/zalando/logbook/pull/507) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump jackson.version from 2.9.8 to 2.9.9 [\#506](https://github.com/zalando/logbook/pull/506) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Added logstash to bom [\#503](https://github.com/zalando/logbook/pull/503) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump jacoco-maven-plugin from 0.8.3 to 0.8.4 [\#501](https://github.com/zalando/logbook/pull/501) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring.version from 5.1.6.RELEASE to 5.1.7.RELEASE [\#500](https://github.com/zalando/logbook/pull/500) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Release 2.0.0-RC.1 [\#498](https://github.com/zalando/logbook/pull/498) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump lombok from 1.18.6 to 1.18.8 [\#497](https://github.com/zalando/logbook/pull/497) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump maven-compiler-plugin from 3.8.0 to 3.8.1 [\#496](https://github.com/zalando/logbook/pull/496) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump maven-surefire-plugin from 2.22.1 to 2.22.2 [\#495](https://github.com/zalando/logbook/pull/495) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))

## [2.0.0-RC.1](https://github.com/zalando/logbook/tree/2.0.0-RC.1) (2019-04-26)

[Full Changelog](https://github.com/zalando/logbook/compare/2.0.0-RC.0...2.0.0-RC.1)

**Fixed bugs:**

- Spring-Boot: Missing logs on HTTP 500 [\#488](https://github.com/zalando/logbook/issues/488)

**Closed issues:**

- Documentation for setting up body filter with Spring Boot Auto-configuration [\#489](https://github.com/zalando/logbook/issues/489)
- Cache request/response headers [\#425](https://github.com/zalando/logbook/issues/425)

**Merged pull requests:**

- Added caching support for headers [\#491](https://github.com/zalando/logbook/pull/491) ([whiskeysierra](https://github.com/whiskeysierra))
- Updated docs [\#490](https://github.com/zalando/logbook/pull/490) ([whiskeysierra](https://github.com/whiskeysierra))
-  Release 2.0.0-RC.0 [\#487](https://github.com/zalando/logbook/pull/487) ([whiskeysierra](https://github.com/whiskeysierra))

## [2.0.0-RC.0](https://github.com/zalando/logbook/tree/2.0.0-RC.0) (2019-04-16)

[Full Changelog](https://github.com/zalando/logbook/compare/1.13.0...2.0.0-RC.0)

**Fixed bugs:**

- SecurityStrategy collects response even if path is filtered [\#480](https://github.com/zalando/logbook/issues/480)
- replaceJsonStringProperty BodyFilter not working for values with quotation marks [\#462](https://github.com/zalando/logbook/issues/462)
- Obfuscation, inclue, exlude properties couldn't set from configuration [\#445](https://github.com/zalando/logbook/issues/445)

**Closed issues:**

- Is it possible to Log downstream calls made by the application [\#448](https://github.com/zalando/logbook/issues/448)
- is it possible to enable /disable filter dynamically [\#436](https://github.com/zalando/logbook/issues/436)
- Logging external calls [\#430](https://github.com/zalando/logbook/issues/430)
- Using JDK 11 causes NoClassDefFoundError [\#423](https://github.com/zalando/logbook/issues/423)
- Unable to start application because MBean `httpLogger` is already registered [\#410](https://github.com/zalando/logbook/issues/410)
- Exclude request/response body  [\#400](https://github.com/zalando/logbook/issues/400)

**Merged pull requests:**

- Improved tests [\#486](https://github.com/zalando/logbook/pull/486) ([whiskeysierra](https://github.com/whiskeysierra))
- Minor code style issues [\#485](https://github.com/zalando/logbook/pull/485) ([whiskeysierra](https://github.com/whiskeysierra))
- Fixed CVE-2019-3795 [\#484](https://github.com/zalando/logbook/pull/484) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump mockito-core from 2.26.0 to 2.27.0 [\#483](https://github.com/zalando/logbook/pull/483) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump okhttp from 3.14.0 to 3.14.1 [\#482](https://github.com/zalando/logbook/pull/482) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito-junit-jupiter from 2.26.0 to 2.27.0 [\#481](https://github.com/zalando/logbook/pull/481) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Suppressed false positive CVE-2018-1258 [\#479](https://github.com/zalando/logbook/pull/479) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump mockito-junit-jupiter from 2.25.1 to 2.26.0 [\#477](https://github.com/zalando/logbook/pull/477) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring-boot-dependencies from 2.1.3.RELEASE to 2.1.4.RELEASE [\#476](https://github.com/zalando/logbook/pull/476) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump junit-jupiter.version from 5.4.1 to 5.4.2 [\#475](https://github.com/zalando/logbook/pull/475) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump junit-platform.version from 1.4.1 to 1.4.2 [\#474](https://github.com/zalando/logbook/pull/474) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito-core from 2.25.1 to 2.26.0 [\#473](https://github.com/zalando/logbook/pull/473) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring-security-bom from 5.1.4.RELEASE to 5.1.5.RELEASE [\#472](https://github.com/zalando/logbook/pull/472) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring.version from 5.1.5.RELEASE to 5.1.6.RELEASE [\#471](https://github.com/zalando/logbook/pull/471) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump httpclient from 4.5.7 to 4.5.8 [\#470](https://github.com/zalando/logbook/pull/470) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Moved JSON-related body filters to json module [\#469](https://github.com/zalando/logbook/pull/469) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump mockito-core from 2.25.0 to 2.25.1 [\#468](https://github.com/zalando/logbook/pull/468) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito-junit-jupiter from 2.25.0 to 2.25.1 [\#467](https://github.com/zalando/logbook/pull/467) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump junit-jupiter.version from 5.4.0 to 5.4.1 [\#466](https://github.com/zalando/logbook/pull/466) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump junit-platform.version from 1.4.0 to 1.4.1 [\#465](https://github.com/zalando/logbook/pull/465) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump okhttp from 3.13.1 to 3.14.0 [\#464](https://github.com/zalando/logbook/pull/464) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Separated Auto Configuration from Starter [\#461](https://github.com/zalando/logbook/pull/461) ([whiskeysierra](https://github.com/whiskeysierra))
- Removed Default interfaces [\#459](https://github.com/zalando/logbook/pull/459) ([whiskeysierra](https://github.com/whiskeysierra))
- Initial support for logstash-logback-encoder [\#458](https://github.com/zalando/logbook/pull/458) ([skjolber](https://github.com/skjolber))
- Bump mockito-junit-jupiter from 2.24.5 to 2.25.0 [\#457](https://github.com/zalando/logbook/pull/457) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito-core from 2.24.5 to 2.25.0 [\#456](https://github.com/zalando/logbook/pull/456) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump javax.servlet-api from 3.1.0 to 4.0.1 [\#455](https://github.com/zalando/logbook/pull/455) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump maven-javadoc-plugin from 3.0.1 to 3.1.0 [\#454](https://github.com/zalando/logbook/pull/454) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Fixed servlet 3.1 feaures [\#453](https://github.com/zalando/logbook/pull/453) ([whiskeysierra](https://github.com/whiskeysierra))
- Removed customizable logger name and level [\#452](https://github.com/zalando/logbook/pull/452) ([whiskeysierra](https://github.com/whiskeysierra))
- Switched from compound to composite [\#451](https://github.com/zalando/logbook/pull/451) ([whiskeysierra](https://github.com/whiskeysierra))
- Chunking is now limited to the body itself [\#449](https://github.com/zalando/logbook/pull/449) ([whiskeysierra](https://github.com/whiskeysierra))
- Added default strategy implementations [\#444](https://github.com/zalando/logbook/pull/444) ([whiskeysierra](https://github.com/whiskeysierra))
- New logbook-json module [\#443](https://github.com/zalando/logbook/pull/443) ([whiskeysierra](https://github.com/whiskeysierra))
- Extracted compacting from JsonHttpLogFormatter [\#442](https://github.com/zalando/logbook/pull/442) ([whiskeysierra](https://github.com/whiskeysierra))
- Add a Gitter chat badge to README.md [\#438](https://github.com/zalando/logbook/pull/438) ([gitter-badger](https://github.com/gitter-badger))
- 2.x Redesign [\#437](https://github.com/zalando/logbook/pull/437) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump slf4j.version from 1.7.25 to 1.7.26 [\#434](https://github.com/zalando/logbook/pull/434) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito-junit-jupiter from 2.24.0 to 2.24.5 [\#433](https://github.com/zalando/logbook/pull/433) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito-core from 2.24.0 to 2.24.5 [\#432](https://github.com/zalando/logbook/pull/432) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring-boot-dependencies from 2.1.2.RELEASE to 2.1.3.RELEASE [\#431](https://github.com/zalando/logbook/pull/431) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring-security-bom from 5.1.3.RELEASE to 5.1.4.RELEASE [\#429](https://github.com/zalando/logbook/pull/429) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring.version from 5.1.4.RELEASE to 5.1.5.RELEASE [\#428](https://github.com/zalando/logbook/pull/428) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump lombok from 1.18.4 to 1.18.6 [\#427](https://github.com/zalando/logbook/pull/427) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump Hamcrest version from 2.0.0.0 to 2.1 [\#426](https://github.com/zalando/logbook/pull/426) ([PascalSchumacher](https://github.com/PascalSchumacher))

## [1.13.0](https://github.com/zalando/logbook/tree/1.13.0) (2019-02-10)

[Full Changelog](https://github.com/zalando/logbook/compare/1.12.1...1.13.0)

**Closed issues:**

- HeaderFilters.eachHeader\(removeHeaders [\#417](https://github.com/zalando/logbook/issues/417)
- Using different Header, Body and Query filters for requests/responses [\#413](https://github.com/zalando/logbook/issues/413)
- Doubt about conditional usage. [\#411](https://github.com/zalando/logbook/issues/411)

**Merged pull requests:**

- Add javax activation as a dependency to logbook-servlet [\#424](https://github.com/zalando/logbook/pull/424) ([acet](https://github.com/acet))
- Bump junit-jupiter.version from 5.3.2 to 5.4.0 [\#422](https://github.com/zalando/logbook/pull/422) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump junit-platform-commons from 1.3.2 to 1.4.0 [\#421](https://github.com/zalando/logbook/pull/421) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump okhttp from 3.12.1 to 3.13.1 [\#420](https://github.com/zalando/logbook/pull/420) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito-junit-jupiter from 2.23.4 to 2.24.0 [\#419](https://github.com/zalando/logbook/pull/419) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito-core from 2.23.4 to 2.24.0 [\#418](https://github.com/zalando/logbook/pull/418) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump jersey-bom from 2.27 to 2.28 [\#416](https://github.com/zalando/logbook/pull/416) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump httpclient from 4.5.6 to 4.5.7 [\#415](https://github.com/zalando/logbook/pull/415) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump jacoco-maven-plugin from 0.8.2 to 0.8.3 [\#414](https://github.com/zalando/logbook/pull/414) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump httpcore from 4.4.10 to 4.4.11 [\#412](https://github.com/zalando/logbook/pull/412) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring-boot-dependencies from 2.1.1.RELEASE to 2.1.2.RELEASE [\#409](https://github.com/zalando/logbook/pull/409) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring-security-bom from 5.1.2.RELEASE to 5.1.3.RELEASE [\#408](https://github.com/zalando/logbook/pull/408) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring.version from 5.1.3.RELEASE to 5.1.4.RELEASE [\#407](https://github.com/zalando/logbook/pull/407) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))

## [1.12.1](https://github.com/zalando/logbook/tree/1.12.1) (2019-01-03)

[Full Changelog](https://github.com/zalando/logbook/compare/1.12.0...1.12.1)

**Merged pull requests:**

- add okhttp and okhttp2 modules to the bom [\#406](https://github.com/zalando/logbook/pull/406) ([davidwcarlson](https://github.com/davidwcarlson))

## [1.12.0](https://github.com/zalando/logbook/tree/1.12.0) (2019-01-03)

[Full Changelog](https://github.com/zalando/logbook/compare/1.11.2...1.12.0)

**Closed issues:**

- Why cannot merge request and response to one log record? [\#403](https://github.com/zalando/logbook/issues/403)
- Spring Boot - body "skipped" [\#396](https://github.com/zalando/logbook/issues/396)
- Log request/response  not showing up [\#393](https://github.com/zalando/logbook/issues/393)

**Merged pull requests:**

- Suppressed false positive CVE-2018-8088 for slf4j-api [\#405](https://github.com/zalando/logbook/pull/405) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump dependency-check-maven from 4.0.0 to 4.0.2 [\#404](https://github.com/zalando/logbook/pull/404) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump okhttp from 3.12.0 to 3.12.1 [\#402](https://github.com/zalando/logbook/pull/402) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump jackson.version from 2.9.7 to 2.9.8 [\#399](https://github.com/zalando/logbook/pull/399) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring-security-bom from 5.1.1.RELEASE to 5.1.2.RELEASE [\#398](https://github.com/zalando/logbook/pull/398) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Add an Intercepter for okhttp v2.x as used by the official kubernetes… [\#397](https://github.com/zalando/logbook/pull/397) ([davidwcarlson](https://github.com/davidwcarlson))
- Bump spring-boot-dependencies from 2.1.0.RELEASE to 2.1.1.RELEASE [\#395](https://github.com/zalando/logbook/pull/395) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring.version from 5.1.2.RELEASE to 5.1.3.RELEASE [\#394](https://github.com/zalando/logbook/pull/394) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump junit-platform-commons from 1.3.1 to 1.3.2 [\#392](https://github.com/zalando/logbook/pull/392) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump junit-jupiter.version from 5.3.1 to 5.3.2 [\#391](https://github.com/zalando/logbook/pull/391) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump dependency-check-maven from 3.3.4 to 4.0.0 [\#390](https://github.com/zalando/logbook/pull/390) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito-core from 2.23.0 to 2.23.4 [\#389](https://github.com/zalando/logbook/pull/389) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito-junit-jupiter from 2.23.0 to 2.23.4 [\#388](https://github.com/zalando/logbook/pull/388) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump okhttp from 3.11.0 to 3.12.0 [\#387](https://github.com/zalando/logbook/pull/387) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))

## [1.11.2](https://github.com/zalando/logbook/tree/1.11.2) (2018-11-18)

[Full Changelog](https://github.com/zalando/logbook/compare/1.11.1...1.11.2)

**Fixed bugs:**

- logbook-okhttp: Unzip gziped responses before logging? [\#384](https://github.com/zalando/logbook/issues/384)

**Closed issues:**

- Log Request/Response after Filter [\#385](https://github.com/zalando/logbook/issues/385)
- Please add simple example with restTemplate [\#383](https://github.com/zalando/logbook/issues/383)
- POST request body not being logged in Spring Boot [\#382](https://github.com/zalando/logbook/issues/382)
- Configurable location for .log files. [\#380](https://github.com/zalando/logbook/issues/380)

**Merged pull requests:**

- Added OkHttp gzip support [\#386](https://github.com/zalando/logbook/pull/386) ([whiskeysierra](https://github.com/whiskeysierra))

## [1.11.1](https://github.com/zalando/logbook/tree/1.11.1) (2018-11-01)

[Full Changelog](https://github.com/zalando/logbook/compare/1.11.0...1.11.1)

**Closed issues:**

- How to use custom Intercepters with order [\#371](https://github.com/zalando/logbook/issues/371)
- logbook.include and logbook.exclude not working [\#370](https://github.com/zalando/logbook/issues/370)

**Merged pull requests:**

- Update bill of materials \(BOM\) to define version for each dependency [\#379](https://github.com/zalando/logbook/pull/379) ([rainermueller](https://github.com/rainermueller))
- Bumped spring versions [\#378](https://github.com/zalando/logbook/pull/378) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump lombok from 1.18.2 to 1.18.4 [\#376](https://github.com/zalando/logbook/pull/376) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring.version from 5.1.1.RELEASE to 5.1.2.RELEASE [\#375](https://github.com/zalando/logbook/pull/375) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump dependency-check-maven from 3.3.2 to 3.3.4 [\#374](https://github.com/zalando/logbook/pull/374) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring-boot-dependencies from 2.0.5.RELEASE to 2.0.6.RELEASE [\#369](https://github.com/zalando/logbook/pull/369) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))

## [1.11.0](https://github.com/zalando/logbook/tree/1.11.0) (2018-10-16)

[Full Changelog](https://github.com/zalando/logbook/compare/1.10.0...1.11.0)

**Merged pull requests:**

- Bump spring.version from 5.1.0.RELEASE to 5.1.1.RELEASE [\#368](https://github.com/zalando/logbook/pull/368) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- add remove query filter [\#367](https://github.com/zalando/logbook/pull/367) ([nsmolenskii](https://github.com/nsmolenskii))
- add form-url-encoded body filter [\#366](https://github.com/zalando/logbook/pull/366) ([nsmolenskii](https://github.com/nsmolenskii))
- Bump maven-surefire-plugin from 2.22.0 to 2.22.1 [\#365](https://github.com/zalando/logbook/pull/365) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))

## [1.10.0](https://github.com/zalando/logbook/tree/1.10.0) (2018-10-11)

[Full Changelog](https://github.com/zalando/logbook/compare/1.9.1...1.10.0)

**Closed issues:**

- Formatting JSON logs with logstash [\#356](https://github.com/zalando/logbook/issues/356)
- Unable to print body of application/json post request  [\#353](https://github.com/zalando/logbook/issues/353)
- configure logbook with NoSql appenders [\#339](https://github.com/zalando/logbook/issues/339)

**Merged pull requests:**

- add include configuration options [\#364](https://github.com/zalando/logbook/pull/364) ([nsmolenskii](https://github.com/nsmolenskii))
- simplify spring boot tests setup [\#363](https://github.com/zalando/logbook/pull/363) ([nsmolenskii](https://github.com/nsmolenskii))
- Bump mockito-junit-jupiter from 2.22.0 to 2.23.0 [\#360](https://github.com/zalando/logbook/pull/360) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito-core from 2.22.0 to 2.23.0 [\#359](https://github.com/zalando/logbook/pull/359) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- add key value http logger [\#358](https://github.com/zalando/logbook/pull/358) ([nsmolenskii](https://github.com/nsmolenskii))
- Add compacting body filters [\#357](https://github.com/zalando/logbook/pull/357) ([nsmolenskii](https://github.com/nsmolenskii))
- Bump spring.version from 5.0.9.RELEASE to 5.1.0.RELEASE [\#354](https://github.com/zalando/logbook/pull/354) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump jackson.version from 2.9.6 to 2.9.7 [\#352](https://github.com/zalando/logbook/pull/352) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))

## [1.9.1](https://github.com/zalando/logbook/tree/1.9.1) (2018-09-18)

[Full Changelog](https://github.com/zalando/logbook/compare/1.9.0...1.9.1)

**Closed issues:**

- How to disable not OK request? [\#336](https://github.com/zalando/logbook/issues/336)
- ClassNotFoundException: org.zalando.fauxpas.ThrowingConsumer [\#333](https://github.com/zalando/logbook/issues/333)
- Getting going with Spring Boot 2 [\#316](https://github.com/zalando/logbook/issues/316)
- Configurable correlation id [\#311](https://github.com/zalando/logbook/issues/311)
- Annotation-driven query filtering [\#309](https://github.com/zalando/logbook/issues/309)
- failing using LogbookProperties with Spring Cloud @RefreshScope [\#299](https://github.com/zalando/logbook/issues/299)

**Merged pull requests:**

- Bump dependency-check-maven from 3.3.1 to 3.3.2 [\#351](https://github.com/zalando/logbook/pull/351) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring-boot-dependencies from 2.0.4.RELEASE to 2.0.5.RELEASE [\#350](https://github.com/zalando/logbook/pull/350) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump junit-jupiter.version from 5.3.0 to 5.3.1 [\#349](https://github.com/zalando/logbook/pull/349) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump junit-platform-commons from 1.3.0 to 1.3.1 [\#348](https://github.com/zalando/logbook/pull/348) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- fix BodyFilter.replaceJsonStringProperty issue with empty values [\#347](https://github.com/zalando/logbook/pull/347) ([nsmolenskii](https://github.com/nsmolenskii))
- Bump versions-maven-plugin from 2.6 to 2.7 [\#346](https://github.com/zalando/logbook/pull/346) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump javax.ws.rs-api from 2.1 to 2.1.1 [\#345](https://github.com/zalando/logbook/pull/345) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito-core from 2.21.0 to 2.22.0 [\#344](https://github.com/zalando/logbook/pull/344) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring.version from 5.0.8.RELEASE to 5.0.9.RELEASE [\#343](https://github.com/zalando/logbook/pull/343) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito-junit-jupiter from 2.21.0 to 2.22.0 [\#342](https://github.com/zalando/logbook/pull/342) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump junit-jupiter.version from 5.2.0 to 5.3.0 [\#341](https://github.com/zalando/logbook/pull/341) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump junit-platform.version from 1.2.0 to 1.3.0 [\#340](https://github.com/zalando/logbook/pull/340) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump versions-maven-plugin from 2.5 to 2.6 [\#338](https://github.com/zalando/logbook/pull/338) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump jacoco-maven-plugin from 0.8.1 to 0.8.2 [\#337](https://github.com/zalando/logbook/pull/337) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Added stability marker [\#335](https://github.com/zalando/logbook/pull/335) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump dependency-check-maven from 3.3.0 to 3.3.1 [\#332](https://github.com/zalando/logbook/pull/332) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito-junit-jupiter from 2.20.1 to 2.21.0 [\#330](https://github.com/zalando/logbook/pull/330) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito-core from 2.20.1 to 2.21.0 [\#329](https://github.com/zalando/logbook/pull/329) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring-boot-dependencies from 2.0.3.RELEASE to 2.0.4.RELEASE [\#328](https://github.com/zalando/logbook/pull/328) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump maven-compiler-plugin from 3.7.0 to 3.8.0 [\#327](https://github.com/zalando/logbook/pull/327) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring.version from 5.0.7.RELEASE to 5.0.8.RELEASE [\#326](https://github.com/zalando/logbook/pull/326) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump lombok from 1.18.0 to 1.18.2 [\#325](https://github.com/zalando/logbook/pull/325) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito-core from 2.20.0 to 2.20.1 [\#324](https://github.com/zalando/logbook/pull/324) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito-junit-jupiter from 2.19.1 to 2.20.1 [\#323](https://github.com/zalando/logbook/pull/323) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito-core from 2.19.1 to 2.20.0 [\#320](https://github.com/zalando/logbook/pull/320) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump httpasyncclient from 4.1.3 to 4.1.4 [\#319](https://github.com/zalando/logbook/pull/319) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump dependency-check-maven from 3.2.1 to 3.3.0 [\#318](https://github.com/zalando/logbook/pull/318) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Configure logger to trace [\#317](https://github.com/zalando/logbook/pull/317) ([softwaregravy](https://github.com/softwaregravy))
- Bump okhttp from 3.10.0 to 3.11.0 [\#314](https://github.com/zalando/logbook/pull/314) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito-junit-jupiter from 2.19.0 to 2.19.1 [\#313](https://github.com/zalando/logbook/pull/313) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito-core from 2.19.0 to 2.19.1 [\#312](https://github.com/zalando/logbook/pull/312) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump httpclient from 4.5.5 to 4.5.6 [\#310](https://github.com/zalando/logbook/pull/310) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump faux-pas from 0.7.1 to 0.8.0 [\#308](https://github.com/zalando/logbook/pull/308) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))

## [1.9.0](https://github.com/zalando/logbook/tree/1.9.0) (2018-07-05)

[Full Changelog](https://github.com/zalando/logbook/compare/1.8.1...1.9.0)

**Fixed bugs:**

- Spring logbook.exclude doesn't work [\#282](https://github.com/zalando/logbook/issues/282)

**Closed issues:**

- Make Spring 5/Spring Boot 2 the default [\#303](https://github.com/zalando/logbook/issues/303)
- Setup "replaceJsonStringProperty" body filter with Spring Boot Auto-configuration [\#298](https://github.com/zalando/logbook/issues/298)
- Allow to limit the body length of request/response logging [\#289](https://github.com/zalando/logbook/issues/289)
- Custom HttpLogWriter - access to HttpServletRequest and HttpServletResponse [\#283](https://github.com/zalando/logbook/issues/283)
- How to replace body message "\<skipped\>"? [\#275](https://github.com/zalando/logbook/issues/275)
- Delayed logging [\#227](https://github.com/zalando/logbook/issues/227)

**Merged pull requests:**

- Made JSON heuristic handle whitespace around structural characters correctly [\#307](https://github.com/zalando/logbook/pull/307) ([whiskeysierra](https://github.com/whiskeysierra))
- Made Spring 5 the default [\#306](https://github.com/zalando/logbook/pull/306) ([whiskeysierra](https://github.com/whiskeysierra))
- Added spring configuration metadata file [\#305](https://github.com/zalando/logbook/pull/305) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump httpcore from 4.4.9 to 4.4.10 [\#302](https://github.com/zalando/logbook/pull/302) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Refactored jaxrs module code [\#301](https://github.com/zalando/logbook/pull/301) ([whiskeysierra](https://github.com/whiskeysierra))
- add JAX-RS support \(on client and server\) for logbook [\#300](https://github.com/zalando/logbook/pull/300) ([davidwcarlson](https://github.com/davidwcarlson))
- Bump faux-pas from 0.7.0 to 0.7.1 [\#297](https://github.com/zalando/logbook/pull/297) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump mockito-core from 2.18.3 to 2.19.0 [\#294](https://github.com/zalando/logbook/pull/294) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump maven-surefire-plugin from 2.21.0 to 2.22.0 [\#293](https://github.com/zalando/logbook/pull/293) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump maven-enforcer-plugin from 3.0.0-M1 to 3.0.0-M2 [\#292](https://github.com/zalando/logbook/pull/292) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- fix README [\#291](https://github.com/zalando/logbook/pull/291) ([finnjohnsen](https://github.com/finnjohnsen))
- Bump spring-boot-dependencies from 1.5.13.RELEASE to 2.0.3.RELEASE [\#290](https://github.com/zalando/logbook/pull/290) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Added bill of materials [\#288](https://github.com/zalando/logbook/pull/288) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump spring.version from 4.3.14.RELEASE to 5.0.7.RELEASE [\#286](https://github.com/zalando/logbook/pull/286) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump spring-security-bom from 4.2.6.RELEASE to 4.2.7.RELEASE [\#285](https://github.com/zalando/logbook/pull/285) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump jackson.version from 2.9.5 to 2.9.6 [\#284](https://github.com/zalando/logbook/pull/284) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump lombok from 1.16.20 to 1.18.0 [\#281](https://github.com/zalando/logbook/pull/281) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))

## [1.8.1](https://github.com/zalando/logbook/tree/1.8.1) (2018-05-31)

[Full Changelog](https://github.com/zalando/logbook/compare/1.8.0...1.8.1)

**Closed issues:**

- JSON formatted logs with truncated body are not valid JSON anymore [\#279](https://github.com/zalando/logbook/issues/279)
- How to log request body instead of \<multipart\>? [\#273](https://github.com/zalando/logbook/issues/273)
- Add correlationId as configurable HTTP header in request and response [\#271](https://github.com/zalando/logbook/issues/271)
- Add logbook.write.body\_max\_size as configuration parameters [\#267](https://github.com/zalando/logbook/issues/267)
- How to log access token? [\#266](https://github.com/zalando/logbook/issues/266)

**Merged pull requests:**

- Added JSON heuristic [\#280](https://github.com/zalando/logbook/pull/280) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump lombok from 1.16.20 to 1.16.22 [\#278](https://github.com/zalando/logbook/pull/278) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump maven-javadoc-plugin from 3.0.0 to 3.0.1 [\#277](https://github.com/zalando/logbook/pull/277) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump dependency-check-maven from 3.2.0 to 3.2.1 [\#276](https://github.com/zalando/logbook/pull/276) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Renamed configuration key, refactored and reformatted some parts [\#274](https://github.com/zalando/logbook/pull/274) ([whiskeysierra](https://github.com/whiskeysierra))
- add a body max size config for Spring [\#272](https://github.com/zalando/logbook/pull/272) ([boure](https://github.com/boure))
- Bump spring-test-junit5 from 1.0.3 to 1.2.0 [\#270](https://github.com/zalando/logbook/pull/270) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump dependency-check-maven from 3.1.2 to 3.2.0 [\#269](https://github.com/zalando/logbook/pull/269) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- JUnit Upgrade [\#262](https://github.com/zalando/logbook/pull/262) ([whiskeysierra](https://github.com/whiskeysierra))

## [1.8.0](https://github.com/zalando/logbook/tree/1.8.0) (2018-05-17)

[Full Changelog](https://github.com/zalando/logbook/compare/1.7.3...1.8.0)

**Closed issues:**

- How to log spring principal in request? [\#236](https://github.com/zalando/logbook/issues/236)
- How to forward logbook logs to a different file? [\#235](https://github.com/zalando/logbook/issues/235)
- Include a list of paths in the logging process [\#234](https://github.com/zalando/logbook/issues/234)
- Enable HTTP Response Logging only for Error responses [\#231](https://github.com/zalando/logbook/issues/231)
- Extract specific fields to put into database [\#229](https://github.com/zalando/logbook/issues/229)
- Obfuscate JSON Request Body  [\#226](https://github.com/zalando/logbook/issues/226)
- Spring boot 2.0.1 and logbook 1.7.1 nothing in the log [\#218](https://github.com/zalando/logbook/issues/218)
- Log requests/responses not showing up [\#216](https://github.com/zalando/logbook/issues/216)
- Support Spring 5 and Spring Boot 2 [\#194](https://github.com/zalando/logbook/issues/194)

**Merged pull requests:**

- Bump org.springframework.security:spring-security-bom from 4.2.4.RELEASE to 4.2.6.RELEASE [\#265](https://github.com/zalando/logbook/pull/265) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Made spring scope provided [\#264](https://github.com/zalando/logbook/pull/264) ([whiskeysierra](https://github.com/whiskeysierra))
- Updated README and renamed spring5 profiles [\#263](https://github.com/zalando/logbook/pull/263) ([whiskeysierra](https://github.com/whiskeysierra))
- Bump org.apache.maven.plugins:maven-resources-plugin from 2.7 to 3.1.0 [\#261](https://github.com/zalando/logbook/pull/261) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump org.codehaus.mojo:versions-maven-plugin from 2.2 to 2.5 [\#260](https://github.com/zalando/logbook/pull/260) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump org.apache.maven.plugins:maven-compiler-plugin from 3.5.1 to 3.7.0 [\#259](https://github.com/zalando/logbook/pull/259) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump org.hobsoft.hamcrest:hamcrest-compose from 0.3.0 to 0.4.0 [\#258](https://github.com/zalando/logbook/pull/258) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump org.owasp:dependency-check-maven from 3.1.1 to 3.1.2 [\#256](https://github.com/zalando/logbook/pull/256) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump com.squareup.okhttp3:okhttp from 3.9.1 to 3.10.0 [\#255](https://github.com/zalando/logbook/pull/255) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump org.apache.maven.plugins:maven-source-plugin from 3.0.0 to 3.0.1 [\#253](https://github.com/zalando/logbook/pull/253) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump com.jayway.jsonpath:json-path-assert from 2.2.0 to 2.4.0 [\#252](https://github.com/zalando/logbook/pull/252) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump org.apache.maven.plugins:maven-javadoc-plugin from 2.10.3 to 3.0.0 [\#250](https://github.com/zalando/logbook/pull/250) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump org.eluder.coveralls:coveralls-maven-plugin from 4.1.0 to 4.3.0 [\#249](https://github.com/zalando/logbook/pull/249) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump org.glassfish:javax.servlet from 3.0 to 3.1.1 [\#248](https://github.com/zalando/logbook/pull/248) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump com.github.sbrannen:spring-test-junit5 from 1.0.0 to 1.0.3 [\#247](https://github.com/zalando/logbook/pull/247) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump org.mockito:mockito-core from 2.9.0 to 2.18.3 [\#244](https://github.com/zalando/logbook/pull/244) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump org.apache.httpcomponents:httpclient from 4.5.3 to 4.5.5 [\#243](https://github.com/zalando/logbook/pull/243) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump org.apache.httpcomponents:httpcore from 4.4.6 to 4.4.9 [\#242](https://github.com/zalando/logbook/pull/242) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump org.sonatype.plugins:nexus-staging-maven-plugin from 1.6.7 to 1.6.8 [\#240](https://github.com/zalando/logbook/pull/240) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump org.projectlombok:lombok from 1.16.18 to 1.16.20 [\#239](https://github.com/zalando/logbook/pull/239) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Bump org.zalando:faux-pas from 0.3.1 to 0.7.0 [\#238](https://github.com/zalando/logbook/pull/238) ([dependabot-preview[bot]](https://github.com/apps/dependabot-preview))
- Fixed some issues [\#237](https://github.com/zalando/logbook/pull/237) ([whiskeysierra](https://github.com/whiskeysierra))
- Dropped private constructor tests [\#233](https://github.com/zalando/logbook/pull/233) ([whiskeysierra](https://github.com/whiskeysierra))
- Added SECURITY file [\#230](https://github.com/zalando/logbook/pull/230) ([whiskeysierra](https://github.com/whiskeysierra))
- Made surefire print test failures to console [\#228](https://github.com/zalando/logbook/pull/228) ([whiskeysierra](https://github.com/whiskeysierra))

## [1.7.3](https://github.com/zalando/logbook/tree/1.7.3) (2018-04-23)

[Full Changelog](https://github.com/zalando/logbook/compare/1.7.2...1.7.3)

**Fixed bugs:**

- Bumped spring 5 and spring boot 2 versions [\#224](https://github.com/zalando/logbook/pull/224) ([whiskeysierra](https://github.com/whiskeysierra))

**Closed issues:**

- Use ThreadLocalRandom to generate correlation id [\#223](https://github.com/zalando/logbook/issues/223)
- SecurityStrategy supporting 403 [\#219](https://github.com/zalando/logbook/issues/219)
- Introduce API Guardian [\#217](https://github.com/zalando/logbook/issues/217)

**Merged pull requests:**

- Replaced Random with ThreadLocalRandom [\#225](https://github.com/zalando/logbook/pull/225) ([whiskeysierra](https://github.com/whiskeysierra))
- Annotated public API with @API [\#221](https://github.com/zalando/logbook/pull/221) ([whiskeysierra](https://github.com/whiskeysierra))

## [1.7.2](https://github.com/zalando/logbook/tree/1.7.2) (2018-04-20)

[Full Changelog](https://github.com/zalando/logbook/compare/1.7.1...1.7.2)

**Merged pull requests:**

- SecurityStrategy supporting 403 \(\#219\) [\#222](https://github.com/zalando/logbook/pull/222) ([fschlesinger](https://github.com/fschlesinger))
- Moved to semantic release script [\#220](https://github.com/zalando/logbook/pull/220) ([whiskeysierra](https://github.com/whiskeysierra))

## [1.7.1](https://github.com/zalando/logbook/tree/1.7.1) (2018-04-11)

[Full Changelog](https://github.com/zalando/logbook/compare/1.7.0...1.7.1)

**Merged pull requests:**

- Bumped Jackson version due to CVE-2018-7489 [\#215](https://github.com/zalando/logbook/pull/215) ([whiskeysierra](https://github.com/whiskeysierra))

## [1.7.0](https://github.com/zalando/logbook/tree/1.7.0) (2018-03-21)

[Full Changelog](https://github.com/zalando/logbook/compare/1.6.1...1.7.0)

**Closed issues:**

- Can "Incoming Request" log under info-level and "Outgoing Request" log under debug-level [\#212](https://github.com/zalando/logbook/issues/212)

**Merged pull requests:**

- Added original request to Precorrelation [\#214](https://github.com/zalando/logbook/pull/214) ([whiskeysierra](https://github.com/whiskeysierra))

## [1.6.1](https://github.com/zalando/logbook/tree/1.6.1) (2018-03-08)

[Full Changelog](https://github.com/zalando/logbook/compare/1.6.0...1.6.1)

**Merged pull requests:**

- Bump spring boot version [\#213](https://github.com/zalando/logbook/pull/213) ([wreulicke](https://github.com/wreulicke))
- Enabled CVE check [\#210](https://github.com/zalando/logbook/pull/210) ([whiskeysierra](https://github.com/whiskeysierra))

## [1.6.0](https://github.com/zalando/logbook/tree/1.6.0) (2018-02-26)

[Full Changelog](https://github.com/zalando/logbook/compare/1.5.5...1.6.0)

**Closed issues:**

- Add custom request info [\#207](https://github.com/zalando/logbook/issues/207)
- get correlation id in other logger [\#205](https://github.com/zalando/logbook/issues/205)
- Add support for OkHttp [\#204](https://github.com/zalando/logbook/issues/204)

**Merged pull requests:**

-  Update isFormRequest condition [\#209](https://github.com/zalando/logbook/pull/209) ([whiskeysierra](https://github.com/whiskeysierra))
- Added support for OkHttp [\#206](https://github.com/zalando/logbook/pull/206) ([whiskeysierra](https://github.com/whiskeysierra))

## [1.5.5](https://github.com/zalando/logbook/tree/1.5.5) (2018-02-09)

[Full Changelog](https://github.com/zalando/logbook/compare/1.5.4...1.5.5)

**Merged pull requests:**

- Bump dependencies for Jackson and Spring [\#202](https://github.com/zalando/logbook/pull/202) ([skjolber](https://github.com/skjolber))

## [1.5.4](https://github.com/zalando/logbook/tree/1.5.4) (2018-02-07)

[Full Changelog](https://github.com/zalando/logbook/compare/1.5.3...1.5.4)

**Closed issues:**

- Logbook resolves localhost address via InetAddress.getLocalHost\(\) [\#200](https://github.com/zalando/logbook/issues/200)

**Merged pull requests:**

- Removed Localhost interface for resolving host address [\#201](https://github.com/zalando/logbook/pull/201) ([whiskeysierra](https://github.com/whiskeysierra))

## [1.5.3](https://github.com/zalando/logbook/tree/1.5.3) (2018-01-26)

[Full Changelog](https://github.com/zalando/logbook/compare/1.5.2...1.5.3)

**Fixed bugs:**

- getOutputStream\(\) has already been called for this response - still not completely fixed [\#155](https://github.com/zalando/logbook/issues/155)

**Closed issues:**

- It is slow when generate correlation Id [\#198](https://github.com/zalando/logbook/issues/198)

**Merged pull requests:**

- GH-198 Uses pseudo-random generator to create correlation ids [\#199](https://github.com/zalando/logbook/pull/199) ([AlexanderYastrebov](https://github.com/AlexanderYastrebov))

## [1.5.2](https://github.com/zalando/logbook/tree/1.5.2) (2018-01-22)

[Full Changelog](https://github.com/zalando/logbook/compare/1.5.1...1.5.2)

**Merged pull requests:**

- Update slf4j [\#197](https://github.com/zalando/logbook/pull/197) ([wreulicke](https://github.com/wreulicke))

## [1.5.1](https://github.com/zalando/logbook/tree/1.5.1) (2018-01-19)

[Full Changelog](https://github.com/zalando/logbook/compare/1.5.0...1.5.1)

**Closed issues:**

- Support other streaming media types [\#190](https://github.com/zalando/logbook/issues/190)

**Merged pull requests:**

- Update JUnit5 dependencies [\#196](https://github.com/zalando/logbook/pull/196) ([wreulicke](https://github.com/wreulicke))
- Update spring version [\#195](https://github.com/zalando/logbook/pull/195) ([wreulicke](https://github.com/wreulicke))
- Update spring dependencies [\#193](https://github.com/zalando/logbook/pull/193) ([wreulicke](https://github.com/wreulicke))
- Update JUnit5 dependencies [\#192](https://github.com/zalando/logbook/pull/192) ([wreulicke](https://github.com/wreulicke))
- Added support for more streaming media types [\#191](https://github.com/zalando/logbook/pull/191) ([whiskeysierra](https://github.com/whiskeysierra))

## [1.5.0](https://github.com/zalando/logbook/tree/1.5.0) (2017-11-22)

[Full Changelog](https://github.com/zalando/logbook/compare/1.4.1...1.5.0)

**Fixed bugs:**

- There is no Content-Type header in response with async requests [\#143](https://github.com/zalando/logbook/issues/143)

**Closed issues:**

- Question : is it possible to change the log level dynamically in spring apps [\#187](https://github.com/zalando/logbook/issues/187)
- Do not log response [\#186](https://github.com/zalando/logbook/issues/186)
- Request fails when using a streaming response in spring boot apps [\#185](https://github.com/zalando/logbook/issues/185)
- Condition on responses [\#183](https://github.com/zalando/logbook/issues/183)
- Not able to remove headers [\#181](https://github.com/zalando/logbook/issues/181)
- Add support for Servlet API 2.5  [\#172](https://github.com/zalando/logbook/issues/172)
- logbook does not print request body when method=POST and header=x-www-form-urlencoded [\#169](https://github.com/zalando/logbook/issues/169)

**Merged pull requests:**

- Moved flushing of async response to correct branch [\#189](https://github.com/zalando/logbook/pull/189) ([whiskeysierra](https://github.com/whiskeysierra))
- Added support to query for absent content type [\#188](https://github.com/zalando/logbook/pull/188) ([whiskeysierra](https://github.com/whiskeysierra))
- Handling of form requests is now configurable [\#184](https://github.com/zalando/logbook/pull/184) ([whiskeysierra](https://github.com/whiskeysierra))

## [1.4.1](https://github.com/zalando/logbook/tree/1.4.1) (2017-10-10)

[Full Changelog](https://github.com/zalando/logbook/compare/1.4.0...1.4.1)

**Closed issues:**

- correlation id in response header [\#177](https://github.com/zalando/logbook/issues/177)
- Spring Boot 2.0.0 \(milestone\) incompatibility [\#176](https://github.com/zalando/logbook/issues/176)

**Merged pull requests:**

- GH-179 Fixes NPE in header\(String, Predicate\) [\#180](https://github.com/zalando/logbook/pull/180) ([AlexanderYastrebov](https://github.com/AlexanderYastrebov))
- Added Spring Boot 2.0 build profile [\#178](https://github.com/zalando/logbook/pull/178) ([whiskeysierra](https://github.com/whiskeysierra))
- Switched to JUnit 5 [\#171](https://github.com/zalando/logbook/pull/171) ([whiskeysierra](https://github.com/whiskeysierra))

## [1.4.0](https://github.com/zalando/logbook/tree/1.4.0) (2017-07-20)

[Full Changelog](https://github.com/zalando/logbook/compare/1.3.1...1.4.0)

**Closed issues:**

- How to log request/response based on response status [\#165](https://github.com/zalando/logbook/issues/165)

**Merged pull requests:**

- Added possibility to delay logging of requests until response can be inspected [\#167](https://github.com/zalando/logbook/pull/167) ([whiskeysierra](https://github.com/whiskeysierra))

## [1.3.1](https://github.com/zalando/logbook/tree/1.3.1) (2017-07-03)

[Full Changelog](https://github.com/zalando/logbook/compare/1.3.0...1.3.1)

**Closed issues:**

- Support for JDK9 [\#161](https://github.com/zalando/logbook/issues/161)
- logbook support log http request in CURL format  [\#160](https://github.com/zalando/logbook/issues/160)
- Log duration of response [\#116](https://github.com/zalando/logbook/issues/116)

**Merged pull requests:**

- Added correlation id to curl log [\#170](https://github.com/zalando/logbook/pull/170) ([whiskeysierra](https://github.com/whiskeysierra))
- Added logging of durations [\#168](https://github.com/zalando/logbook/pull/168) ([whiskeysierra](https://github.com/whiskeysierra))

## [1.3.0](https://github.com/zalando/logbook/tree/1.3.0) (2017-06-27)

[Full Changelog](https://github.com/zalando/logbook/compare/1.2.5...1.3.0)

**Closed issues:**

- spring boot 1.5.3 + freemaker Chinese characters messy code [\#162](https://github.com/zalando/logbook/issues/162)

**Merged pull requests:**

- Added curl request formatter [\#166](https://github.com/zalando/logbook/pull/166) ([whiskeysierra](https://github.com/whiskeysierra))

## [1.2.5](https://github.com/zalando/logbook/tree/1.2.5) (2017-06-15)

[Full Changelog](https://github.com/zalando/logbook/compare/1.2.4...1.2.5)

**Closed issues:**

- logbook messy code  [\#163](https://github.com/zalando/logbook/issues/163)

**Merged pull requests:**

- Fixed missing content-type [\#164](https://github.com/zalando/logbook/pull/164) ([whiskeysierra](https://github.com/whiskeysierra))

## [1.2.4](https://github.com/zalando/logbook/tree/1.2.4) (2017-05-24)

[Full Changelog](https://github.com/zalando/logbook/compare/1.2.3...1.2.4)

**Closed issues:**

- Consider removing 'com.google.gag' from dependencies [\#156](https://github.com/zalando/logbook/issues/156)

**Merged pull requests:**

- Cleaned up dependencies [\#159](https://github.com/zalando/logbook/pull/159) ([whiskeysierra](https://github.com/whiskeysierra))

## [1.2.3](https://github.com/zalando/logbook/tree/1.2.3) (2017-05-24)

[Full Changelog](https://github.com/zalando/logbook/compare/1.2.1...1.2.3)

**Closed issues:**

- LogbookAutoConfiguration require javax.servlet.Filter result  [\#157](https://github.com/zalando/logbook/issues/157)

**Merged pull requests:**

- Split up auto configuration cleanly [\#158](https://github.com/zalando/logbook/pull/158) ([whiskeysierra](https://github.com/whiskeysierra))

## [1.2.1](https://github.com/zalando/logbook/tree/1.2.1) (2017-05-09)

[Full Changelog](https://github.com/zalando/logbook/compare/1.2.0...1.2.1)

**Closed issues:**

- getOutputStream\(\) has already been called for this response [\#146](https://github.com/zalando/logbook/issues/146)

**Merged pull requests:**

- GH-146 Delegates getOutputStream and getWriter after withoutBody was called [\#154](https://github.com/zalando/logbook/pull/154) ([AlexanderYastrebov](https://github.com/AlexanderYastrebov))

## [1.2.0](https://github.com/zalando/logbook/tree/1.2.0) (2017-04-24)

[Full Changelog](https://github.com/zalando/logbook/compare/1.1.1...1.2.0)

**Closed issues:**

- Exclude some headers from output [\#151](https://github.com/zalando/logbook/issues/151)
- Split long lines on whitespace [\#148](https://github.com/zalando/logbook/issues/148)
- Support chunking of large log messages [\#142](https://github.com/zalando/logbook/issues/142)

**Merged pull requests:**

- GH-151 Allows removal of headers [\#152](https://github.com/zalando/logbook/pull/152) ([AlexanderYastrebov](https://github.com/AlexanderYastrebov))
- GH-148 Increases ChunkingHttpLogWriter min/max chunk size delta [\#150](https://github.com/zalando/logbook/pull/150) ([AlexanderYastrebov](https://github.com/AlexanderYastrebov))
- GH-148 Splits chunks on whitespace or punctuation [\#149](https://github.com/zalando/logbook/pull/149) ([AlexanderYastrebov](https://github.com/AlexanderYastrebov))
- Added chunking support [\#147](https://github.com/zalando/logbook/pull/147) ([whiskeysierra](https://github.com/whiskeysierra))

## [1.1.1](https://github.com/zalando/logbook/tree/1.1.1) (2017-03-20)

[Full Changelog](https://github.com/zalando/logbook/compare/1.1.0...1.1.1)

**Closed issues:**

- Unable to exclude logbook-httpclient from Spring Boot starter [\#141](https://github.com/zalando/logbook/issues/141)
- How to implement Custom Formatter? [\#140](https://github.com/zalando/logbook/issues/140)

**Merged pull requests:**

- Fixed tests that failed on Windows due to line separator [\#145](https://github.com/zalando/logbook/pull/145) ([Infeligo](https://github.com/Infeligo))
- Added support to exclude http client [\#144](https://github.com/zalando/logbook/pull/144) ([whiskeysierra](https://github.com/whiskeysierra))

## [1.1.0](https://github.com/zalando/logbook/tree/1.1.0) (2016-12-15)

[Full Changelog](https://github.com/zalando/logbook/compare/1.0.1...1.1.0)

**Closed issues:**

- JsonHttpLogFormatter failed to render JSON log when request / response body is not met JSON spec [\#139](https://github.com/zalando/logbook/issues/139)
- Spring auto configuration failed when the container has multiple ObjectMapper beans [\#138](https://github.com/zalando/logbook/issues/138)
- \[httpclient\] RemoteResponse.withBody\(\) breaks gzip decompression [\#135](https://github.com/zalando/logbook/issues/135)

**Merged pull requests:**

- Feature - Filter body [\#137](https://github.com/zalando/logbook/pull/137) ([rubenjgarcia](https://github.com/rubenjgarcia))

## [1.0.1](https://github.com/zalando/logbook/tree/1.0.1) (2016-11-11)

[Full Changelog](https://github.com/zalando/logbook/compare/1.0.0...1.0.1)

**Closed issues:**

- Compare logbook to similar technologies [\#100](https://github.com/zalando/logbook/issues/100)

**Merged pull requests:**

- Issue \#135: Copy the chunked flag and the ContentEncoding/ContentType… [\#136](https://github.com/zalando/logbook/pull/136) ([BGehrels](https://github.com/BGehrels))

## [1.0.0](https://github.com/zalando/logbook/tree/1.0.0) (2016-10-07)

[Full Changelog](https://github.com/zalando/logbook/compare/1.0.0-RC5...1.0.0)

**Fixed bugs:**

- TypeNotPresentException when SecurityFilterAutoConfiguration is not on the classpath [\#108](https://github.com/zalando/logbook/issues/108)
- How to log POST parameters? [\#94](https://github.com/zalando/logbook/issues/94)
- Header map in requests/responses should be case-insensitive [\#67](https://github.com/zalando/logbook/issues/67)

**Closed issues:**

- Invalid JSON due to body filter [\#133](https://github.com/zalando/logbook/issues/133)
- Expose http client request/response interceptors in LogbookAutoConfiguration [\#107](https://github.com/zalando/logbook/issues/107)
- Remove dependency on core [\#87](https://github.com/zalando/logbook/issues/87)
- Change Obfuscator APIs to allow access to request/response [\#80](https://github.com/zalando/logbook/issues/80)
- URI obfuscation [\#79](https://github.com/zalando/logbook/issues/79)
- Release First Major Version [\#78](https://github.com/zalando/logbook/issues/78)
- Detect correct protocol version [\#75](https://github.com/zalando/logbook/issues/75)
- Should requestTo\(String\) treat its argument as a pattern or as a string? [\#70](https://github.com/zalando/logbook/issues/70)
- Remove Guava to make logbook lightweight [\#59](https://github.com/zalando/logbook/issues/59)
- Add Spring Boot support for predicate [\#57](https://github.com/zalando/logbook/issues/57)
- Implement query parameter obfuscation in a less invasive way [\#50](https://github.com/zalando/logbook/issues/50)

**Merged pull requests:**

- Added interceptors to AutoConfiguration [\#109](https://github.com/zalando/logbook/pull/109) ([whiskeysierra](https://github.com/whiskeysierra))
- Removed params from JSON example [\#104](https://github.com/zalando/logbook/pull/104) ([whiskeysierra](https://github.com/whiskeysierra))
- Avoid array copy on LocalResponse.getBodyAsString [\#93](https://github.com/zalando/logbook/pull/93) ([AlexanderYastrebov](https://github.com/AlexanderYastrebov))
- Removed servlet request double buffering [\#92](https://github.com/zalando/logbook/pull/92) ([AlexanderYastrebov](https://github.com/AlexanderYastrebov))
- Removed runtime dependency from httpclient+servlet to core [\#91](https://github.com/zalando/logbook/pull/91) ([whiskeysierra](https://github.com/whiskeysierra))
- Disabled some email notifications [\#90](https://github.com/zalando/logbook/pull/90) ([whiskeysierra](https://github.com/whiskeysierra))
- Release 1.0.0-RC1 [\#85](https://github.com/zalando/logbook/pull/85) ([whiskeysierra](https://github.com/whiskeysierra))
- Remove obsolete TODOs [\#84](https://github.com/zalando/logbook/pull/84) ([whiskeysierra](https://github.com/whiskeysierra))
- Extracted query parameters from request uri [\#74](https://github.com/zalando/logbook/pull/74) ([whiskeysierra](https://github.com/whiskeysierra))

## [1.0.0-RC5](https://github.com/zalando/logbook/tree/1.0.0-RC5) (2016-09-21)

[Full Changelog](https://github.com/zalando/logbook/compare/1.0.0-RC4...1.0.0-RC5)

**Fixed bugs:**

- Check how logbook handles HTTP streams [\#118](https://github.com/zalando/logbook/issues/118)
- Logging unauthorized request \(error chain\) [\#32](https://github.com/zalando/logbook/issues/32)

**Closed issues:**

- GET request contains empty body property in json [\#127](https://github.com/zalando/logbook/issues/127)
- Allow to ignore body for certain messages [\#123](https://github.com/zalando/logbook/issues/123)
- Split AutoConfiguration into parts [\#122](https://github.com/zalando/logbook/issues/122)
- Un-finalize formatting class [\#114](https://github.com/zalando/logbook/issues/114)
- Use UTF-8 in logbook-servlet [\#48](https://github.com/zalando/logbook/issues/48)

**Merged pull requests:**

- Requests in error dispatch are now logged without a body [\#132](https://github.com/zalando/logbook/pull/132) ([whiskeysierra](https://github.com/whiskeysierra))
- Customization of HTTP/JSON output [\#131](https://github.com/zalando/logbook/pull/131) ([whiskeysierra](https://github.com/whiskeysierra))
- Switched to UTF-8 [\#130](https://github.com/zalando/logbook/pull/130) ([whiskeysierra](https://github.com/whiskeysierra))
- Filtering [\#129](https://github.com/zalando/logbook/pull/129) ([whiskeysierra](https://github.com/whiskeysierra))
- Switched to MIT license [\#128](https://github.com/zalando/logbook/pull/128) ([whiskeysierra](https://github.com/whiskeysierra))

## [1.0.0-RC4](https://github.com/zalando/logbook/tree/1.0.0-RC4) (2016-08-15)

[Full Changelog](https://github.com/zalando/logbook/compare/1.0.0-RC3...1.0.0-RC4)

**Fixed bugs:**

- Incompatibility with Apache's Async HTTP Client [\#124](https://github.com/zalando/logbook/issues/124)
- @AutoConfigureAfter\(JacksonAutoConfiguration.class\) [\#119](https://github.com/zalando/logbook/issues/119)
- Put POST parameter issue to Known Issues section in the README [\#111](https://github.com/zalando/logbook/issues/111)

**Closed issues:**

- Enhance Auto Configuration to allow multiple body obfuscators [\#120](https://github.com/zalando/logbook/issues/120)
- Order of fields in json log format [\#96](https://github.com/zalando/logbook/issues/96)

**Merged pull requests:**

- unknown host exception in local request [\#126](https://github.com/zalando/logbook/pull/126) ([tkrop](https://github.com/tkrop))
- GH-124 Fixed async support for http client [\#125](https://github.com/zalando/logbook/pull/125) ([whiskeysierra](https://github.com/whiskeysierra))
- Multiple obfuscators [\#121](https://github.com/zalando/logbook/pull/121) ([whiskeysierra](https://github.com/whiskeysierra))
- Allow passing of URIs without defined port [\#115](https://github.com/zalando/logbook/pull/115) ([lukasniemeier-zalando](https://github.com/lukasniemeier-zalando))
- Update README.md [\#113](https://github.com/zalando/logbook/pull/113) ([whiskeysierra](https://github.com/whiskeysierra))
- Added known issue to README [\#112](https://github.com/zalando/logbook/pull/112) ([whiskeysierra](https://github.com/whiskeysierra))
- Release 1.0.0-RC3 [\#110](https://github.com/zalando/logbook/pull/110) ([whiskeysierra](https://github.com/whiskeysierra))

## [1.0.0-RC3](https://github.com/zalando/logbook/tree/1.0.0-RC3) (2016-05-27)

[Full Changelog](https://github.com/zalando/logbook/compare/1.0.0-RC2...1.0.0-RC3)

**Merged pull requests:**

- Updated contribution guidelines [\#106](https://github.com/zalando/logbook/pull/106) ([whiskeysierra](https://github.com/whiskeysierra))
- Release 1.0.0-RC2 [\#105](https://github.com/zalando/logbook/pull/105) ([whiskeysierra](https://github.com/whiskeysierra))

## [1.0.0-RC2](https://github.com/zalando/logbook/tree/1.0.0-RC2) (2016-05-25)

[Full Changelog](https://github.com/zalando/logbook/compare/1.0.0-RC1...1.0.0-RC2)

**Fixed bugs:**

- HeadersBuilder builds a mutable data structure [\#97](https://github.com/zalando/logbook/issues/97)
- Remove params from example JSON in README [\#95](https://github.com/zalando/logbook/issues/95)

**Closed issues:**

- Fix zappr configuration [\#101](https://github.com/zalando/logbook/issues/101)

**Merged pull requests:**

- Do you mean 'Logbook' instead of 'Logback' here? [\#103](https://github.com/zalando/logbook/pull/103) ([jbellmann](https://github.com/jbellmann))
- Update and rename .zappr.yml to .zappr.yaml [\#102](https://github.com/zalando/logbook/pull/102) ([whiskeysierra](https://github.com/whiskeysierra))
- Make headers immutable [\#98](https://github.com/zalando/logbook/pull/98) ([AlexanderYastrebov](https://github.com/AlexanderYastrebov))
- Updated zappr config [\#89](https://github.com/zalando/logbook/pull/89) ([whiskeysierra](https://github.com/whiskeysierra))
- Remove guava dependency [\#88](https://github.com/zalando/logbook/pull/88) ([AlexanderYastrebov](https://github.com/AlexanderYastrebov))

## [1.0.0-RC1](https://github.com/zalando/logbook/tree/1.0.0-RC1) (2016-05-02)

[Full Changelog](https://github.com/zalando/logbook/compare/0.14.0...1.0.0-RC1)

**Fixed bugs:**

- Made headers case insensitive [\#73](https://github.com/zalando/logbook/pull/73) ([whiskeysierra](https://github.com/whiskeysierra))
- Fixed typo [\#72](https://github.com/zalando/logbook/pull/72) ([whiskeysierra](https://github.com/whiskeysierra))

**Closed issues:**

- Obfuscate Authorization header by default [\#65](https://github.com/zalando/logbook/issues/65)
- Add predefined, reusable request predicates [\#64](https://github.com/zalando/logbook/issues/64)
- Extract testing utilities into own module [\#55](https://github.com/zalando/logbook/issues/55)

**Merged pull requests:**

- Removed nested class in Logbook [\#83](https://github.com/zalando/logbook/pull/83) ([whiskeysierra](https://github.com/whiskeysierra))
- Feature/request obfuscator [\#82](https://github.com/zalando/logbook/pull/82) ([whiskeysierra](https://github.com/whiskeysierra))
- Fixed examples in readme [\#81](https://github.com/zalando/logbook/pull/81) ([whiskeysierra](https://github.com/whiskeysierra))
- Added support for excludes in Spring Boot [\#77](https://github.com/zalando/logbook/pull/77) ([whiskeysierra](https://github.com/whiskeysierra))
- Added support for protocol version [\#76](https://github.com/zalando/logbook/pull/76) ([whiskeysierra](https://github.com/whiskeysierra))
- Changed semantics of requestTo\(String\) [\#71](https://github.com/zalando/logbook/pull/71) ([whiskeysierra](https://github.com/whiskeysierra))
- Extracted API and Test modules [\#69](https://github.com/zalando/logbook/pull/69) ([whiskeysierra](https://github.com/whiskeysierra))
- Added predefined/reusable request predicates [\#68](https://github.com/zalando/logbook/pull/68) ([whiskeysierra](https://github.com/whiskeysierra))
- Enabled authorization header obfuscation by default [\#66](https://github.com/zalando/logbook/pull/66) ([whiskeysierra](https://github.com/whiskeysierra))
- Fix headlines [\#63](https://github.com/zalando/logbook/pull/63) ([whiskeysierra](https://github.com/whiskeysierra))
- Update README.md [\#62](https://github.com/zalando/logbook/pull/62) ([LappleApple](https://github.com/LappleApple))
- Release 0.14.0 [\#61](https://github.com/zalando/logbook/pull/61) ([whiskeysierra](https://github.com/whiskeysierra))

## [0.14.0](https://github.com/zalando/logbook/tree/0.14.0) (2016-04-14)

[Full Changelog](https://github.com/zalando/logbook/compare/0.13.0...0.14.0)

**Fixed bugs:**

- logbook-spring-boot-starter dependency on spring-security-web [\#46](https://github.com/zalando/logbook/issues/46)
- Extracted security aspect of LogbookAutoConfiguration [\#54](https://github.com/zalando/logbook/pull/54) ([whiskeysierra](https://github.com/whiskeysierra))

**Closed issues:**

- Provide a way to distinguish between incoming and outgoing request/responses [\#47](https://github.com/zalando/logbook/issues/47)
- Provide a way to disable logging of requests/responses on certain urls [\#45](https://github.com/zalando/logbook/issues/45)
- Document correlation id in readme [\#37](https://github.com/zalando/logbook/issues/37)

**Merged pull requests:**

- Refined readme [\#58](https://github.com/zalando/logbook/pull/58) ([whiskeysierra](https://github.com/whiskeysierra))
- Conditional logging [\#56](https://github.com/zalando/logbook/pull/56) ([whiskeysierra](https://github.com/whiskeysierra))
- Added origin property to requests and responses [\#53](https://github.com/zalando/logbook/pull/53) ([whiskeysierra](https://github.com/whiskeysierra))
- Added correlation section to readme [\#52](https://github.com/zalando/logbook/pull/52) ([whiskeysierra](https://github.com/whiskeysierra))

## [0.13.0](https://github.com/zalando/logbook/tree/0.13.0) (2016-02-23)

[Full Changelog](https://github.com/zalando/logbook/compare/0.12.0...0.13.0)

**Merged pull requests:**

- Don't fail on invalid HTTP request targets [\#49](https://github.com/zalando/logbook/pull/49) ([twz123](https://github.com/twz123))
- Upstream/master [\#44](https://github.com/zalando/logbook/pull/44) ([lukasniemeier-zalando](https://github.com/lukasniemeier-zalando))

## [0.12.0](https://github.com/zalando/logbook/tree/0.12.0) (2016-01-21)

[Full Changelog](https://github.com/zalando/logbook/compare/0.11.0...0.12.0)

**Closed issues:**

- Duplicate key exception when logging response headers in spring boot application [\#42](https://github.com/zalando/logbook/issues/42)

**Merged pull requests:**

- Fix logging of duplicate header names [\#43](https://github.com/zalando/logbook/pull/43) ([jhorstmann](https://github.com/jhorstmann))

## [0.11.0](https://github.com/zalando/logbook/tree/0.11.0) (2016-01-04)

[Full Changelog](https://github.com/zalando/logbook/compare/0.10.0...0.11.0)

**Fixed bugs:**

- JsonHttpLogFormatter should be resilient against malformed JSON [\#38](https://github.com/zalando/logbook/issues/38)

**Closed issues:**

- Provide SPI to body embedding [\#41](https://github.com/zalando/logbook/issues/41)
- logbook-spring-boot-starter missing in maven-central [\#40](https://github.com/zalando/logbook/issues/40)
- Consider having a consistent naming of http headers [\#35](https://github.com/zalando/logbook/issues/35)
- Add classifier to JSON formatted messages [\#31](https://github.com/zalando/logbook/issues/31)
- Log full URI on request [\#30](https://github.com/zalando/logbook/issues/30)
- Provide a Spring Boot AutoConfiguration [\#25](https://github.com/zalando/logbook/issues/25)
- Add test for error dispatch [\#10](https://github.com/zalando/logbook/issues/10)

**Merged pull requests:**

- Fallback to simple String for invalid JSON body [\#39](https://github.com/zalando/logbook/pull/39) ([lukasniemeier-zalando](https://github.com/lukasniemeier-zalando))
- Requests will now contain an absolute request uri [\#34](https://github.com/zalando/logbook/pull/34) ([whiskeysierra](https://github.com/whiskeysierra))
- Added classifier to JSON output [\#33](https://github.com/zalando/logbook/pull/33) ([whiskeysierra](https://github.com/whiskeysierra))
- LogbookAutoConfiguration [\#28](https://github.com/zalando/logbook/pull/28) ([whiskeysierra](https://github.com/whiskeysierra))

## [0.10.0](https://github.com/zalando/logbook/tree/0.10.0) (2015-12-02)

[Full Changelog](https://github.com/zalando/logbook/compare/0.9.0...0.10.0)

**Fixed bugs:**

- System.out tests a running in a race condition [\#29](https://github.com/zalando/logbook/issues/29)

**Closed issues:**

- Remove "buffer once" semantic [\#24](https://github.com/zalando/logbook/issues/24)
- Add support for security setups to logbook-servlet [\#17](https://github.com/zalando/logbook/issues/17)

**Merged pull requests:**

- Removed buffer-once semantic in multi-filter setups [\#27](https://github.com/zalando/logbook/pull/27) ([whiskeysierra](https://github.com/whiskeysierra))
- Added banner to readme [\#26](https://github.com/zalando/logbook/pull/26) ([whiskeysierra](https://github.com/whiskeysierra))
- Added security strategy support [\#23](https://github.com/zalando/logbook/pull/23) ([whiskeysierra](https://github.com/whiskeysierra))

## [0.9.0](https://github.com/zalando/logbook/tree/0.9.0) (2015-11-25)

[Full Changelog](https://github.com/zalando/logbook/compare/0.8.0...0.9.0)

**Closed issues:**

- Add support for CXF interceptors [\#16](https://github.com/zalando/logbook/issues/16)
- Add low-level support for Apache HttpComponents [\#15](https://github.com/zalando/logbook/issues/15)

**Merged pull requests:**

- Added support for Apache HttpClient [\#22](https://github.com/zalando/logbook/pull/22) ([whiskeysierra](https://github.com/whiskeysierra))

## [0.8.0](https://github.com/zalando/logbook/tree/0.8.0) (2015-11-23)

[Full Changelog](https://github.com/zalando/logbook/compare/0.7.0...0.8.0)

**Fixed bugs:**

- QueryParameters\#splitEntries fails with java.util.NoSuchElementException [\#20](https://github.com/zalando/logbook/issues/20)
- Bugfix/query parameters without values [\#21](https://github.com/zalando/logbook/pull/21) ([whiskeysierra](https://github.com/whiskeysierra))

## [0.7.0](https://github.com/zalando/logbook/tree/0.7.0) (2015-11-04)

[Full Changelog](https://github.com/zalando/logbook/compare/0.6.0...0.7.0)

**Fixed bugs:**

- Servlet 3 asynchronous requests not correctly handled [\#2](https://github.com/zalando/logbook/issues/2)

**Closed issues:**

- Servlet filter is swallowing response body [\#18](https://github.com/zalando/logbook/issues/18)

**Merged pull requests:**

- Fixed flush issue with buffering print writers [\#19](https://github.com/zalando/logbook/pull/19) ([whiskeysierra](https://github.com/whiskeysierra))

## [0.6.0](https://github.com/zalando/logbook/tree/0.6.0) (2015-10-14)

[Full Changelog](https://github.com/zalando/logbook/compare/0.5.0...0.6.0)

**Closed issues:**

- Support empty bodies for application/json messages [\#13](https://github.com/zalando/logbook/issues/13)

**Merged pull requests:**

- Fixed invalid json in case of empty json body [\#14](https://github.com/zalando/logbook/pull/14) ([whiskeysierra](https://github.com/whiskeysierra))

## [0.5.0](https://github.com/zalando/logbook/tree/0.5.0) (2015-10-12)

[Full Changelog](https://github.com/zalando/logbook/compare/0.4.0...0.5.0)

## [0.4.0](https://github.com/zalando/logbook/tree/0.4.0) (2015-10-09)

[Full Changelog](https://github.com/zalando/logbook/compare/0.3.0...0.4.0)

**Merged pull requests:**

- Fixed IllegalStateException when accessing AsyncContext [\#12](https://github.com/zalando/logbook/pull/12) ([whiskeysierra](https://github.com/whiskeysierra))

## [0.3.0](https://github.com/zalando/logbook/tree/0.3.0) (2015-10-08)

[Full Changelog](https://github.com/zalando/logbook/compare/0.2.0...0.3.0)

**Merged pull requests:**

- Added support for embedded+compacted json bodies [\#11](https://github.com/zalando/logbook/pull/11) ([whiskeysierra](https://github.com/whiskeysierra))

## [0.2.0](https://github.com/zalando/logbook/tree/0.2.0) (2015-10-07)

[Full Changelog](https://github.com/zalando/logbook/compare/0.1.0...0.2.0)

**Closed issues:**

- Logger config info [\#7](https://github.com/zalando/logbook/issues/7)
- Split up formatting and writing [\#5](https://github.com/zalando/logbook/issues/5)
- Get rid of Spring dependency [\#4](https://github.com/zalando/logbook/issues/4)
- Correlate request and response [\#1](https://github.com/zalando/logbook/issues/1)

**Merged pull requests:**

- Fixed NPE on null response body [\#9](https://github.com/zalando/logbook/pull/9) ([whiskeysierra](https://github.com/whiskeysierra))
- Update README.md [\#8](https://github.com/zalando/logbook/pull/8) ([bocytko](https://github.com/bocytko))
- Refactored library completely [\#6](https://github.com/zalando/logbook/pull/6) ([whiskeysierra](https://github.com/whiskeysierra))
- Added support for JsonHttpLogger [\#3](https://github.com/zalando/logbook/pull/3) ([whiskeysierra](https://github.com/whiskeysierra))

## [0.1.0](https://github.com/zalando/logbook/tree/0.1.0) (2015-10-06)

[Full Changelog](https://github.com/zalando/logbook/compare/a0dcb5b3ac0eb2ff374f6aa6bb4c91c2b56fafd5...0.1.0)



\* *This Changelog was automatically generated by [github_changelog_generator](https://github.com/github-changelog-generator/github-changelog-generator)*
