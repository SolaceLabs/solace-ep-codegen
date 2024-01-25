package com.solace.ep.muleflow.eclipse.maven;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

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
