/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.solace.ep.muleflow.boomi;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Scanner;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class BoomiProcessGenerator {
    // store token for calling a rest api
    
    private static final String token = System.getenv("EP2BoomiToken");

    static ObjectMapper JsonMapper = new ObjectMapper();
    static Scanner scanner = new Scanner(System.in);
    static HttpClient client = HttpClient.newHttpClient();
    static XmlMapper xmlMapper = new XmlMapper();


    public static void main(String[] args) {
        generateBoomiProcess();
        String selected_domain = getAppDomains();
        String selected_app = getAppInDomain(selected_domain);
        String app_ver_id = getAppVersions(selected_app);
        JsonNode app_consumers = getAppVersionSpec(app_ver_id);
        System.out.println(app_consumers);
    }

    static void generateBoomiProcess() {
        System.out.println("Generating Boomi Process");
        System.out.println("Token: " + token);
    }

    static String getAppDomains() {
        String selected_domain = null;
        try {
            // create a new http request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.solace.cloud/api/v2/architecture/applicationDomains"))
                    .header("accept", "application/json;charset=UTF-8")
                    .header("authorization", "Bearer " + token)
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();
            // send the request and get the response
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            // parse the JSON response
            JsonNode jsonResponse = JsonMapper.readTree(response.body());

            // get the array of name objects
            JsonNode dataNode = jsonResponse.get("data");
            // create a HashMap to store the names and ids

            HashMap<String, String> app_domains = new HashMap<>();

            // loop through the objects in data
            for (int i = 0; i < dataNode.size(); i++) {
                JsonNode obj = dataNode.get(i);
                // access the properties of each object
                String name = obj.get("name").asText();
                String id = obj.get("id").asText();
                // store the name and id as key-value pairs
                app_domains.put(name, id);
            }
            // print the key from the hashmap with a number in front of it
            int count = 1;
            for (String key : app_domains.keySet()) {
                System.out.println(count + ". " + key);
                count++;
            }

            // prompt for input from the user to select a number
            System.out.print("Enter a number: ");
            int selection = scanner.nextInt();

            // retrieve the corresponding key from the hashmap
            String selected = null;
            count = 1;
            for (String key : app_domains.keySet()) {
                if (count == selection) {
                    selected = key;
                    break;
                }
                count++;
            }

            // handle the selected key
            if (selected != null) {
                System.out.println("Selected key: " + selected + " with id: " + app_domains.get(selected));
                selected_domain = app_domains.get(selected);

            } else {
                System.out.println("Invalid app domain selected.");
            }

        } catch (Exception e) {
            System.out.println(e);
        }
        return selected_domain;
    }

    static String getAppInDomain(String app_domain) {
        String selected_app = null;
        try {
            // Get the application for the selected domain
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(
                            "https://api.solace.cloud/api/v2/architecture/applications?pageSize=20&pageNumber=1&applicationDomainId="
                                    + app_domain))
                    .header("accept", "application/json;charset=UTF-8")
                    .header("authorization", "Bearer " + token)
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request,
                    HttpResponse.BodyHandlers.ofString());
            // parse json response
            JsonNode rootNode = JsonMapper.readTree(response.body());
            JsonNode dataNode = rootNode.get("data");
            HashMap<String, String> app_map = new HashMap<>();
            for (int i = 0; i < dataNode.size(); i++) {
                JsonNode obj = dataNode.get(i);
                String name = obj.get("name").asText();
                String id = obj.get("id").asText();
                app_map.put(name, id);
            }

            int count = 1;
            for (String key : app_map.keySet()) {
                System.out.println(count + ". " + key);
                count++;
            }

            // prompt for input from the user to select a number
            System.out.print("Enter a number: ");
            int selection = scanner.nextInt();
            String selected = null;
            count = 1;
            for (String key : app_map.keySet()) {
                if (count == selection) {
                    selected = key;
                    break;
                }
                count++;
            }

            if (selected != null) {
                System.out.println("Selected app: " + selected + " with id: " + app_map.get(selected));
                selected_app = app_map.get(selected);

            } else {
                System.out.println("Invalid app selected.");
            }
        } catch (Exception e) {
            System.out.println(e);
        }

        return selected_app;
    }

    static String getAppVersions(String app_id) {
        // Get the application version for the selected application
        String selected_app_version = null;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(
                            "https://api.solace.cloud/api/v2/architecture/applicationVersions?pageSize=20&pageNumber=1&includeWarnings=false&applicationIds="
                                    + app_id))
                    .header("accept", "application/json;charset=UTF-8")
                    .header("authorization",
                            "Bearer eyJhbGciOiJSUzI1NiIsImtpZCI6Im1hYXNfcHJvZF8yMDIwMDMyNiIsInR5cCI6IkpXVCJ9.eyJvcmciOiJzb2xhY2VzZTEiLCJvcmdUeXBlIjoiRU5URVJQUklTRSIsInN1YiI6Im9kYWZkcDFmbjMyIiwicGVybWlzc2lvbnMiOiJBQUFBQUFBQUFBQUFRQUFBSUFBQUFBQUFBQUFBQUFBQUFBQVFBSUVBSUFBQUpBS3JnWlFmQVJ3QVdFSUJ3b0lxSUFFQkVBQkMiLCJhcGlUb2tlbklkIjoiMWprdGtmYXVjaXEiLCJpc3MiOiJTb2xhY2UgQ29ycG9yYXRpb24iLCJpYXQiOjE3MTQwNzc2NjF9.Op5VbSDtFXPaaoWfv_F49ip-Yv_p0qBREjovKXMKRbU2ixcf-nvzuso1l_bvBt8NApdxELwJ_513GU0uVl-70IxUAlVCeykMLGzGRLH_NS2e6lHj9uGqBYEKVDHmRe2bZHQrHCzJbXt2uHdROeL-C32TIkF2kjVWdOKi4TJwmHWQwiiq56Poc7eS8CZcLyNGTfnlfrnFAE-jUOAFfUktMzM3K8YCXf9sNbCSErcptsvsgIzYH-uTAxBKyHSBuKxkSxv1Tb-K5eO-8ldfUhiKeYfYbBvLF28Ew9utgrIArIbR7woOAywR1jlr0Ydzik-pADRa5hqGLv1Vab5gdxnXoA")
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request,
                    HttpResponse.BodyHandlers.ofString());
            // parse json response
            JsonNode rootNode = JsonMapper.readTree(response.body());
            JsonNode dataNode = rootNode.get("data");
            HashMap<String, String> app_ver_map = new HashMap<>();
            for (int i = 0; i < dataNode.size(); i++) {
                JsonNode obj = dataNode.get(i);
                String name = obj.get("version").asText();
                String id = obj.get("id").asText();
                app_ver_map.put(name, id);
            }

            int count = 1;
            for (String key : app_ver_map.keySet()) {
                System.out.println(count + ". " + key);
                count++;
            }

            // prompt for input from the user to select a number
            System.out.print("Enter a number: ");
            int selection = scanner.nextInt();
            String selected = null;
            count = 1;
            for (String key : app_ver_map.keySet()) {
                if (count == selection) {
                    selected = key;
                    break;
                }
                count++;
            }
            if (selected != null) {
                System.out.println("Selected app version: " + selected + " with id: " + app_ver_map.get(selected));
                selected_app_version = app_ver_map.get(selected);

            } else {
                System.out.println("Invalid app version selected.");
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return selected_app_version;

    }

    static JsonNode getAppVersionSpec(String app_ver_id) {
        // Gather the required data to build a boomi process
        JsonNode asyncFile = null;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.solace.cloud/api/v2/architecture/applicationVersions/" + app_ver_id + "/asyncApi?format=json&showVersioning=false&includedExtensions=all&asyncApiVersion=2.5.0&environmentOptions=include_attracted_events_only"))
                    .header("accept", "application/json;charset=UTF-8")
                    .header("authorization", "Bearer " + token)
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request,
                    HttpResponse.BodyHandlers.ofString());
            // System.out.println("Data for the app version: " + ver_spec_response.body());
            JsonNode rootNode = JsonMapper.readTree(response.body());
            System.out.println(rootNode);
            asyncFile = rootNode;
        } catch (Exception e) {
            System.out.println(e);
        }

        return asyncFile;
    }


    
}
