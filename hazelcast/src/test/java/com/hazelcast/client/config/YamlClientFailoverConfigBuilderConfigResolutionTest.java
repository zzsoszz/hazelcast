/*
 * Copyright (c) 2008-2025, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.client.config;

import com.hazelcast.config.helpers.DeclarativeConfigFileHelper;
import com.hazelcast.core.HazelcastException;
import com.hazelcast.test.HazelcastSerialClassRunner;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.io.File;

import static com.hazelcast.internal.config.DeclarativeConfigUtil.SYSPROP_CLIENT_FAILOVER_CONFIG;
import static com.hazelcast.internal.config.DeclarativeConfigUtil.YAML_ACCEPTED_SUFFIXES_STRING;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;

@RunWith(HazelcastSerialClassRunner.class)
@Category(QuickTest.class)
public class YamlClientFailoverConfigBuilderConfigResolutionTest {


    private final DeclarativeConfigFileHelper helper = new DeclarativeConfigFileHelper();

    @Before
    @After
    public void tearDown() throws Exception {
        System.clearProperty(SYSPROP_CLIENT_FAILOVER_CONFIG);
        helper.ensureTestConfigDeleted();
    }

    @Test
    public void testResolveSystemProperty_file_yaml() throws Exception {
        helper.givenYamlClientFailoverConfigFileInWorkDir("foo.yaml", 42);
        System.setProperty(SYSPROP_CLIENT_FAILOVER_CONFIG, "foo.yaml");

        ClientFailoverConfig config = new YamlClientFailoverConfigBuilder().build();
        assertEquals(42, config.getTryCount());
    }

    @Test
    public void testResolveSystemProperty_classpath_yaml() throws Exception {
        helper.givenYamlClientFailoverConfigFileOnClasspath("foo.yaml", 42);
        System.setProperty(SYSPROP_CLIENT_FAILOVER_CONFIG, "classpath:foo.yaml");

        ClientFailoverConfig config = new YamlClientFailoverConfigBuilder().build();
        assertEquals(42, config.getTryCount());
    }

    @Test
    public void testResolveSystemProperty_file_yml() throws Exception {
        helper.givenYamlClientFailoverConfigFileInWorkDir("foo.yml", 42);
        System.setProperty(SYSPROP_CLIENT_FAILOVER_CONFIG, "foo.yml");

        ClientFailoverConfig config = new YamlClientFailoverConfigBuilder().build();
        assertEquals(42, config.getTryCount());
    }

    @Test
    public void testResolveSystemProperty_classpath_yml() throws Exception {
        helper.givenYamlClientFailoverConfigFileOnClasspath("foo.yml", 42);
        System.setProperty(SYSPROP_CLIENT_FAILOVER_CONFIG, "classpath:foo.yml");

        ClientFailoverConfig config = new YamlClientFailoverConfigBuilder().build();
        assertEquals(42, config.getTryCount());
    }

    @Test
    public void testResolveSystemProperty_classpath_nonExistentYaml_throws() {
        System.setProperty(SYSPROP_CLIENT_FAILOVER_CONFIG, "classpath:idontexist.yaml");

        assertThatThrownBy(YamlClientFailoverConfigBuilder::new)
                .isInstanceOf(HazelcastException.class)
                .hasMessageContaining("classpath")
                .hasMessageContaining("idontexist.yaml");
    }

    @Test
    public void testResolveSystemProperty_file_nonExistentYaml_throws() {
        System.setProperty(SYSPROP_CLIENT_FAILOVER_CONFIG, "idontexist.yaml");

        assertThatThrownBy(YamlClientFailoverConfigBuilder::new)
                .isInstanceOf(HazelcastException.class)
                .hasMessageContaining("idontexist.yaml");
    }

    @Test
    public void testResolveSystemProperty_file_nonYaml_throws() throws Exception {
        File file = helper.givenYamlClientFailoverConfigFileInWorkDir("foo.xml", 42);
        System.setProperty(SYSPROP_CLIENT_FAILOVER_CONFIG, file.getAbsolutePath());

        assertThatThrownBy(YamlClientFailoverConfigBuilder::new)
                .isInstanceOf(HazelcastException.class)
                .hasMessageContaining(SYSPROP_CLIENT_FAILOVER_CONFIG)
                .hasMessageContaining("suffix")
                .hasMessageContaining("foo.xml")
                .hasMessageContaining(YAML_ACCEPTED_SUFFIXES_STRING);
    }

    @Test
    public void testResolveSystemProperty_classpath_nonYaml_throws() throws Exception {
        helper.givenYamlClientFailoverConfigFileOnClasspath("foo.xml", 42);
        System.setProperty(SYSPROP_CLIENT_FAILOVER_CONFIG, "classpath:foo.xml");

        assertThatThrownBy(YamlClientFailoverConfigBuilder::new)
                .isInstanceOf(HazelcastException.class)
                .hasMessageContaining(SYSPROP_CLIENT_FAILOVER_CONFIG)
                .hasMessageContaining("suffix")
                .hasMessageContaining("foo.xml")
                .hasMessageContaining(YAML_ACCEPTED_SUFFIXES_STRING);
    }

    @Test
    public void testResolveSystemProperty_file_nonExistentNonYaml_throws() {
        System.setProperty(SYSPROP_CLIENT_FAILOVER_CONFIG, "foo.xml");

        assertThatThrownBy(YamlClientFailoverConfigBuilder::new)
                .isInstanceOf(HazelcastException.class)
                .hasMessageContaining(SYSPROP_CLIENT_FAILOVER_CONFIG)
                .hasMessageContaining("foo.xml")
                .hasMessageContaining(YAML_ACCEPTED_SUFFIXES_STRING);
    }

    @Test
    public void testResolveSystemProperty_classpath_nonExistentNonYaml_throws() {
        System.setProperty(SYSPROP_CLIENT_FAILOVER_CONFIG, "classpath:idontexist.xml");

        assertThatThrownBy(YamlClientFailoverConfigBuilder::new)
                .isInstanceOf(HazelcastException.class)
                .hasMessageContaining(SYSPROP_CLIENT_FAILOVER_CONFIG)
                .hasMessageContaining("classpath")
                .hasMessageContaining("idontexist.xml")
                .hasMessageContaining(YAML_ACCEPTED_SUFFIXES_STRING);

    }

    @Test
    public void testResolveSystemProperty_file_noSuffix_throws() throws Exception {
        File file = helper.givenYamlClientFailoverConfigFileInWorkDir("foo", 42);
        System.setProperty(SYSPROP_CLIENT_FAILOVER_CONFIG, file.getAbsolutePath());

        assertThatThrownBy(YamlClientFailoverConfigBuilder::new)
                .isInstanceOf(HazelcastException.class)
                .hasMessageContaining(SYSPROP_CLIENT_FAILOVER_CONFIG)
                .hasMessageContaining("suffix")
                .hasMessageContaining("foo")
                .hasMessageContaining(YAML_ACCEPTED_SUFFIXES_STRING);
    }

    @Test
    public void testResolveSystemProperty_classpath_noSuffix_throws() throws Exception {
        helper.givenYamlClientFailoverConfigFileOnClasspath("foo", 42);
        System.setProperty(SYSPROP_CLIENT_FAILOVER_CONFIG, "classpath:foo");

        assertThatThrownBy(YamlClientFailoverConfigBuilder::new)
                .isInstanceOf(HazelcastException.class)
                .hasMessageContaining(SYSPROP_CLIENT_FAILOVER_CONFIG)
                .hasMessageContaining("suffix")
                .hasMessageContaining("foo")
                .hasMessageContaining(YAML_ACCEPTED_SUFFIXES_STRING);
    }

    @Test
    public void testResolveFromWorkDirYamlButNotYml() throws Exception {
        helper.givenYamlClientFailoverConfigFileInWorkDir(42);

        ClientFailoverConfig config = new YamlClientFailoverConfigBuilder().build();

        assertEquals(42, config.getTryCount());
    }

    @Test
    public void testResolveFromWorkDirYmlButNotYaml() throws Exception {
        helper.givenYmlClientFailoverConfigFileInWorkDir(42);

        ClientFailoverConfig config = new YamlClientFailoverConfigBuilder().build();

        assertEquals(42, config.getTryCount());
    }

    @Test
    public void testResolveFromWorkDirYamlAndYaml() throws Exception {
        helper.givenYamlClientFailoverConfigFileInWorkDir(42);
        helper.givenYmlClientFailoverConfigFileInWorkDir(24);

        ClientFailoverConfig config = new YamlClientFailoverConfigBuilder().build();

        assertEquals(42, config.getTryCount());
    }

    @Test
    public void testResolveFromClasspathYamlButNotYml() throws Exception {
        helper.givenYamlClientFailoverConfigFileOnClasspath(42);

        ClientFailoverConfig config = new YamlClientFailoverConfigBuilder().build();

        assertEquals(42, config.getTryCount());
    }

    @Test
    public void testResolveFromClasspathYmlButNotYaml() throws Exception {
        helper.givenYmlClientFailoverConfigFileOnClasspath(42);

        ClientFailoverConfig config = new YamlClientFailoverConfigBuilder().build();

        assertEquals(42, config.getTryCount());
    }

    @Test
    public void testResolveFromClasspathYamlAndYml() throws Exception {
        helper.givenYamlClientFailoverConfigFileOnClasspath(42);
        helper.givenYmlClientFailoverConfigFileOnClasspath(24);

        ClientFailoverConfig config = new YamlClientFailoverConfigBuilder().build();

        assertEquals(42, config.getTryCount());
    }

    @Test
    public void testResolveDefault() {
        assertThatThrownBy(YamlClientFailoverConfigBuilder::new)
                .isInstanceOf(HazelcastException.class)
                .hasMessageContaining("Failed to load ClientFailoverConfig");
    }

}
