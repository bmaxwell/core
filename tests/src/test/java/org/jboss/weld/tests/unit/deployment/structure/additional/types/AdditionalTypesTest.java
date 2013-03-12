/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.tests.unit.deployment.structure.additional.types;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;

import org.jboss.arquillian.container.weld.ee.embedded_1_1.mock.AbstractDeployment;
import org.jboss.weld.bootstrap.WeldBootstrap;
import org.jboss.weld.bootstrap.api.Environments;
import org.jboss.weld.bootstrap.api.TypeDiscoveryConfiguration;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.CDI11BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.tests.unit.deployment.structure.EmptyBeanDeploymentArchive;
import org.testng.annotations.Test;

public class AdditionalTypesTest {

    @Test
    public void testArchiveWithAdditionalTypes() {

        final VerifyingExtension observer = new VerifyingExtension();
        final CDI11BeanDeploymentArchive archive = new EmptyBeanDeploymentArchive() {
            @Override
            public Collection<String> getAdditionalTypes() {
                return Collections.singleton(AdditionalType.class.getName());
            }
        };

        Deployment deployment = new AbstractDeployment(archive, observer) {
            @Override
            public BeanDeploymentArchive loadBeanDeploymentArchive(Class<?> beanClass) {
                return archive;
            }
        };

        WeldBootstrap bootstrap = new WeldBootstrap();
        TypeDiscoveryConfiguration configuration = bootstrap.startExtensions(deployment.getExtensions());

        assertTrue(configuration.getAdditionalTypeMarkerAnnotations().contains(RequiredAnnotation.class));

        bootstrap.startContainer(Environments.EE_INJECT, deployment).startInitialization().deployBeans().validateBeans().endInitialization();

        assertNotNull(observer.getObservedType());
        assertEquals(observer.getObservedType().getJavaClass(), AdditionalType.class);
        assertNotNull(observer.getFinalType());
        assertEquals(observer.getFinalType().getJavaClass(), AdditionalType.class);

        // make sure a bean is not created for an additional type
        assertFalse(observer.isProcessBeanObserved());
    }
}
