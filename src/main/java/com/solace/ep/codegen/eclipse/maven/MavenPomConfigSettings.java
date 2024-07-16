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

package com.solace.ep.codegen.eclipse.maven;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Class to parse YAML file containing Maven POM configuration settings
 * The YAML config file contains the settings for dependencies to be
 * included in the pom.xml for generated projects.
 */
@Data
@NoArgsConstructor
public class MavenPomConfigSettings {
    
    protected List<DependencyConfig> dependencies;

    protected List<RepositoryConfig> repositories;

    @Data
    @NoArgsConstructor
    public static class DependencyConfig {

        @JsonProperty
        @NonNull
        protected ModuleProtocolType appliesTo;

        @JsonProperty
        @NonNull
        protected String groupId;

        @JsonProperty
        @NonNull
        protected String artifactId;

        @JsonProperty
        @NonNull
        protected String version;
    }

    @Data
    @NoArgsConstructor
    public static class RepositoryConfig {

        @JsonProperty
        @NonNull
        protected String id;

        @JsonProperty
        @NonNull
        protected String name;

        @JsonProperty
        @NonNull
        protected String url;

        @JsonProperty
        protected Boolean pluginRepository;
    }
}
