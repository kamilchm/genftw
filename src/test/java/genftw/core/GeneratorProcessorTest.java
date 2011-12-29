/*
 * Copyright 2011 GenFTW contributors
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

package genftw.core;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import genftw.api.Generator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GeneratorProcessorTest {

    @Mock
    ProcessorLogger logger;

    @Mock
    RoundEnvironment roundEnv;

    @InjectMocks
    GeneratorProcessor tested;

    @Test
    public void getGeneratorElements_elementWithKindClass() {
        stubRoundEnvironment(mockElement(Element.class, ElementKind.CLASS));

        Set<TypeElement> result = tested.getGeneratorElements(roundEnv);

        assertThat(result.isEmpty(), equalTo(true));
    }

    @Test
    public void getGeneratorElements_typeElementWithKindClass() {
        stubRoundEnvironment(mockElement(TypeElement.class, ElementKind.CLASS));

        Set<TypeElement> result = tested.getGeneratorElements(roundEnv);

        assertThat(result.isEmpty(), equalTo(true));
        verify(logger).error(anyString(), any(Element.class));
    }

    @Test
    public void getGeneratorElements_typeElementWithKindInterface() {
        TypeElement typeElement = mockElement(TypeElement.class, ElementKind.INTERFACE);
        stubRoundEnvironment(typeElement);

        Set<TypeElement> result = tested.getGeneratorElements(roundEnv);

        assertThat(result.size(), equalTo(1));
        assertThat(result.contains(typeElement), equalTo(true));
    }

    void stubRoundEnvironment(Element... generatorElements) {
        Set<Element> annotatedElements = new HashSet<Element>();
        annotatedElements.addAll(Arrays.asList(generatorElements));
        doReturn(annotatedElements).when(roundEnv).getElementsAnnotatedWith(Generator.class);
    }

    @SuppressWarnings("unchecked")
    <T extends Element> T mockElement(Class<T> elementClass, ElementKind elementKind) {
        return (T) when(mock(elementClass).getKind()).thenReturn(elementKind).getMock();
    }

}
