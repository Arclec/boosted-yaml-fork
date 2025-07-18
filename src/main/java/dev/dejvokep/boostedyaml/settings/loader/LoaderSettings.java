/*
 * Copyright 2024 https://dejvokep.dev/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.dejvokep.boostedyaml.settings.loader;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.settings.Settings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.snakeyaml.engine.v2.api.ConstructNode;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.api.LoadSettingsBuilder;
import org.snakeyaml.engine.v2.env.EnvConfig;
import org.snakeyaml.engine.v2.nodes.Tag;
import org.snakeyaml.engine.v2.schema.Schema;

import java.util.Map;
import java.util.Optional;

/**
 * Loader settings cover all options related explicitly (only) to file loading.
 * <p>
 * Settings introduced by BoostedYAML follow builder design pattern, e.g. you may build your own settings using
 * <code>LoaderSettings.builder() //configure// .build()</code>
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public class LoaderSettings implements Settings {

    /**
     * Default loader settings.
     */
    public static final LoaderSettings DEFAULT = builder().build();

    //SnakeYAML Engine load settings builder
    private final LoadSettingsBuilder builder;
    //If to automatically update and create file if absent
    private final boolean createFileIfAbsent, autoUpdate;

    /**
     * Creates final, immutable loader settings from the given builder.
     *
     * @param builder the builder
     */
    private LoaderSettings(Builder builder) {
        this.builder = builder.builder;
        this.autoUpdate = builder.autoUpdate;
        this.createFileIfAbsent = builder.createFileIfAbsent;
    }

    /**
     * Returns if to automatically call {@link YamlDocument#update()} after the document has been loaded.
     *
     * @return if to automatically update after the document has been loaded
     */
    public boolean isAutoUpdate() {
        return autoUpdate;
    }

    /**
     * Returns if to create a new file and save it if it does not exist automatically.
     *
     * @return if to create a new file if absent
     */
    public boolean isCreateFileIfAbsent() {
        return createFileIfAbsent;
    }

    /**
     * Builds the SnakeYAML Engine settings.
     *
     * @param generalSettings settings used to get defaults (list, set, map) from
     * @return the new settings
     */
    public LoadSettings buildEngineSettings(GeneralSettings generalSettings) {
        return this.builder.setParseComments(true).setDefaultList(generalSettings::getDefaultList).setDefaultSet(generalSettings::getDefaultSet).setDefaultMap(generalSettings::getDefaultMap).build();
    }

    /**
     * Returns a new builder.
     *
     * @return the new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates and returns a new builder from the given, already created SnakeYAML Engine settings builder.
     * <p>
     * <b>Note that the given builder is not cloned, so it is in the caller's best interest to never change it's
     * settings from now on.</b>
     * <p>
     * Please note that {@link Builder#setCreateFileIfAbsent(boolean)} and {@link Builder#setAutoUpdate(boolean)} still have to be
     * called (if you want to alter the default), as they are not part of the Engine's settings.
     *
     * @param builder the underlying builder
     * @return the new builder
     */
    public static Builder builder(LoadSettingsBuilder builder) {
        return new Builder(builder);
    }

    /**
     * Returns a new builder with the same configuration as the given settings.
     *
     * @param settings preset settings
     * @return the new builder
     */
    public static Builder builder(LoaderSettings settings) {
        return builder(settings.builder)
                .setAutoUpdate(settings.autoUpdate)
                .setCreateFileIfAbsent(settings.createFileIfAbsent);
    }

    /**
     * Builder for loader settings; wrapper for SnakeYAML Engine's {@link LoadSettingsBuilder} class which is more
     * detailed, provides more options and possibilities, hides options which should not be configured.
     */
    public static class Builder {

        /**
         * If to automatically create a new file if absent by default.
         */
        public static final boolean DEFAULT_CREATE_FILE_IF_ABSENT = true;
        /**
         * If to automatically update the file after load by default.
         */
        public static final boolean DEFAULT_AUTO_UPDATE = false;
        /**
         * If to print detailed error messages by default.
         */
        public static final boolean DEFAULT_DETAILED_ERRORS = true;
        /**
         * If to allow duplicate keys by default.
         */
        public static final boolean DEFAULT_ALLOW_DUPLICATE_KEYS = true;

        //Underlying SnakeYAML Engine settings builder
        private final LoadSettingsBuilder builder;
        //If to automatically update and create file if absent
        private boolean autoUpdate = DEFAULT_AUTO_UPDATE, createFileIfAbsent = DEFAULT_CREATE_FILE_IF_ABSENT;

        /**
         * Creates a new builder from the given, already created SnakeYAML Engine settings builder.
         * <p>
         * Please note that {@link #setCreateFileIfAbsent(boolean)} and {@link #setAutoUpdate(boolean)} still have to be
         * called (if you want to alter the default), as they are not part of the Engine's settings.
         *
         * @param builder the underlying builder
         */
        private Builder(LoadSettingsBuilder builder) {
            this.builder = builder;
        }

        /**
         * Creates a new builder. Automatically applies the defaults, compatible with Spigot/BungeeCord API.
         */
        private Builder() {
            //Create
            this.builder = LoadSettings.builder();
            //Set defaults
            setDetailedErrors(DEFAULT_DETAILED_ERRORS);
            setAllowDuplicateKeys(DEFAULT_ALLOW_DUPLICATE_KEYS);
        }

        /**
         * Sets if to create a new file and save it if it does not exist automatically.
         * <p>
         * Not effective if there is no {@link YamlDocument#getFile() file associated} with the document.
         * <p>
         * <b>Default: </b>{@link #DEFAULT_CREATE_FILE_IF_ABSENT}
         *
         * @param createFileIfAbsent if to create a new file if absent
         * @return the builder
         */
        public Builder setCreateFileIfAbsent(boolean createFileIfAbsent) {
            this.createFileIfAbsent = createFileIfAbsent;
            return this;
        }

        /**
         * If enabled, automatically calls {@link YamlDocument#update()} after the document has been loaded.
         * <p>
         * Not effective if there are no {@link YamlDocument#getDefaults() defaults associated} with the document.
         * <p>
         * <b>Default: </b>{@link #DEFAULT_AUTO_UPDATE}
         *
         * @param autoUpdate if to automatically update after loading
         * @return the builder
         */
        public Builder setAutoUpdate(boolean autoUpdate) {
            this.autoUpdate = autoUpdate;
            return this;
        }

        /**
         * Sets custom label for error messages.
         * <p>
         * For additional information, please refer to documentation of the parent method listed below.
         * <p>
         * <b>Default: </b> defined by the parent method<br>
         * <b>Parent method: </b> {@link LoadSettingsBuilder#setLabel(String)}<br>
         * <b>Parent method docs (v2.3): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/LoadSettingsBuilder.html#setLabel(java.lang.String)">click</a><br>
         * <b>Related YAML spec (v1.2.2): </b>-
         *
         * @param label the label
         * @return the builder
         */
        public Builder setErrorLabel(@NotNull String label) {
            builder.setLabel(label);
            return this;
        }

        /**
         * Sets if to print detailed error messages.
         * <p>
         * For additional information, please refer to documentation of the parent method listed below.
         * <p>
         * <b>Default: </b> {@link #DEFAULT_DETAILED_ERRORS}<br>
         * <b>Parent method: </b> {@link LoadSettingsBuilder#setUseMarks(boolean)}<br>
         * <b>Parent method docs (v2.3): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/LoadSettingsBuilder.html#setUseMarks(boolean)">click</a><br>
         * <b>Related YAML spec (v1.2.2): </b>-
         *
         * @param detailedErrors if to print detailed errors
         * @return the builder
         */
        public Builder setDetailedErrors(boolean detailedErrors) {
            builder.setUseMarks(detailedErrors);
            return this;
        }

        /**
         * Sets if to allow duplicate keys in sections (last key wins when loading).
         * <p>
         * For additional information, please refer to documentation of the parent method listed below.
         * <p>
         * <b>Default: </b> {@link #DEFAULT_ALLOW_DUPLICATE_KEYS}<br>
         * <b>Parent method: </b> {@link LoadSettingsBuilder#setAllowDuplicateKeys(boolean)}<br>
         * <b>Parent method docs (v2.3): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/LoadSettingsBuilder.html#setAllowDuplicateKeys(boolean)">click</a><br>
         * <b>Related YAML spec (v1.2.2): </b>-
         *
         * @param allowDuplicateKeys if to allow duplicate keys
         * @return the builder
         */
        public Builder setAllowDuplicateKeys(boolean allowDuplicateKeys) {
            builder.setAllowDuplicateKeys(allowDuplicateKeys);
            return this;
        }

        /**
         * Sets maximum aliases a collection can have to prevent memory leaks (see
         * <a href="https://en.wikipedia.org/wiki/Billion_laughs_attack">Billion laughs attack</a>).
         * <p>
         * For additional information, please refer to documentation of the parent method listed below.
         * <p>
         * <b>Default: </b> defined by the parent method<br>
         * <b>Parent method: </b> {@link LoadSettingsBuilder#setMaxAliasesForCollections(int)}<br>
         * <b>Parent method docs (v2.3): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/LoadSettingsBuilder.html#setMaxAliasesForCollections(int)">click</a><br>
         * <b>Related YAML spec (v1.2.2): </b>-
         *
         * @param maxCollectionAliases maximum aliases for collections
         * @return the builder
         */
        public Builder setMaxCollectionAliases(int maxCollectionAliases) {
            builder.setMaxAliasesForCollections(maxCollectionAliases);
            return this;
        }

        /**
         * Sets custom node to Java object constructors, per YAML tag.
         * <p>
         * For additional information, please refer to documentation of the parent method listed below.
         * <p>
         * <b>Default: </b> defined by the parent method<br>
         * <b>Parent method: </b> {@link LoadSettingsBuilder#setTagConstructors(Map)} (int)}<br>
         * <b>Parent method docs (v2.3): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/LoadSettingsBuilder.html#setTagConstructors(java.util.Map)">click</a><br>
         * <b>Related YAML spec (v1.2.2): </b><a href="https://yaml.org/spec/1.2.2/#1021-tags">JSON schema tags</a>, <a href="https://yaml.org/spec/1.2.2/#failsafe-schema">failsafe schema tags</a>
         *
         * @param constructors constructor map
         * @return the builder
         */
        public Builder setTagConstructors(@NotNull Map<Tag, ConstructNode> constructors) {
            builder.setTagConstructors(constructors);
            return this;
        }

        /**
         * Sets custom schema to use. Schemas are used to resolve and determine object tags contained within a document.
         * <p>
         * For additional information, please refer to documentation of the parent method listed below.
         * <p>
         * <b>Default: </b> defined by the parent method<br>
         * <b>Parent method: </b> {@link LoadSettingsBuilder#setSchema(Schema)}<br>
         * <b>Parent method docs (v2.7): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.7/org/snakeyaml/engine/v2/api/LoadSettingsBuilder.html#setSchema(org.snakeyaml.engine.v2.schema.Schema)">click</a><br>
         * <b>Related YAML spec (v1.2.2): </b><a href="https://yaml.org/spec/1.2.2/#1021-tags">JSON schema tags</a>, <a href="https://yaml.org/spec/1.2.2/#failsafe-schema">failsafe schema tags</a>
         *
         * @param schema the schema to set
         * @return the builder
         * @see LoadSettingsBuilder#setSchema(Schema)
         */
        public Builder setSchema(@NotNull Schema schema) {
            builder.setSchema(schema);
            return this;
        }

        /**
         * Sets custom environment variable config.
         * <p>
         * For additional information, please refer to documentation of the parent method listed below.
         * <p>
         * <b>Default: </b> defined by the parent method<br>
         * <b>Parent method: </b> {@link LoadSettingsBuilder#setEnvConfig(Optional)}<br>
         * <b>Parent method docs (v2.3): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.3/org/snakeyaml/engine/v2/api/LoadSettingsBuilder.html#setEnvConfig(java.util.Optional)">click</a><br>
         * <b>Related YAML spec (v1.2.2): </b>-
         *
         * @param envConfig the config to set
         * @return the builder
         * @see LoadSettingsBuilder#setEnvConfig(Optional)
         */
        public Builder setEnvironmentConfig(@Nullable EnvConfig envConfig) {
            builder.setEnvConfig(Optional.ofNullable(envConfig));
            return this;
        }

        /**
         * Sets the limit on the code point length of the incoming document. Please note that this is <b>not</b> the
         * actual size of the document in bytes - that depends on the used encoding. Should a document exceed the limit,
         * an {@link org.snakeyaml.engine.v2.exceptions.YamlEngineException} will be thrown while loading the document.
         * <p>
         * For additional information, please refer to documentation of the parent method listed below.
         * <p>
         * <b>Default: </b> defined by the parent method<br>
         * <b>Parent method: </b> {@link LoadSettingsBuilder#setCodePointLimit(int)}<br>
         * <b>Parent method docs (v2.9): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.9/org/snakeyaml/engine/v2/api/LoadSettingsBuilder.html#setCodePointLimit(int)">click</a><br>
         * <b>Related YAML spec (v1.2.2): </b>-
         *
         * @param limit the code point limit
         * @return the builder
         * @see LoadSettingsBuilder#setCodePointLimit(int)
         */
        public Builder setCodePointLimit(int limit) {
            builder.setCodePointLimit(limit);
            return this;
        }

        /**
         * Sets the buffer size in bytes for passed input streams. Alternatively, make sure that the passed streams are
         * already buffered, when this setting rarely has any impact.
         * <p>
         * For additional information, please refer to documentation of the parent method listed below.
         * <p>
         * <b>Default: </b> defined by the parent method<br>
         * <b>Parent method: </b> {@link LoadSettingsBuilder#setBufferSize(Integer)}<br>
         * <b>Parent method docs (v2.9): </b><a href="https://javadoc.io/static/org.snakeyaml/snakeyaml-engine/2.9/org/snakeyaml/engine/v2/api/LoadSettingsBuilder.html#setBufferSize(Integer)">click</a><br>
         * <b>Related YAML spec (v1.2.2): </b>-
         *
         * @param size the buffer size in bytes
         * @return the builder
         */
        public Builder setBufferSize(int size) {
            builder.setBufferSize(size);
            return this;
        }

        /**
         * Builds the settings.
         *
         * @return the settings
         */
        public LoaderSettings build() {
            return new LoaderSettings(this);
        }
    }

}