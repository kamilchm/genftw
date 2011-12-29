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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import genftw.api.ForAllElements;
import genftw.api.ForEachElement;
import genftw.api.Produces;
import genftw.core.match.ElementFinder;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GeneratorMethodFinderTest {

    @Mock
    Elements elementUtils;

    @Mock
    ElementFinder elementFinder;

    @Mock
    ProcessorLogger logger;

    @InjectMocks
    GeneratorMethodFinder tested;

    @Test
    public void visitExecutable_elementWithKindOtherThanMethod() {
        ExecutableElement executableElement = mockExecutableElement(ElementKind.CONSTRUCTOR);

        tested.visitExecutable(executableElement, null);

        assertThat(tested.getMethodsFound().length, equalTo(0));
    }

    @Test
    public void visitExecutable_elementWithKindMethod_withoutProducesAnnotation() {
        ExecutableElement executableElement = mockExecutableElement(ElementKind.METHOD);
        stubGeneratorMethodAnnotations(executableElement, false, false, false);

        tested.visitExecutable(executableElement, null);

        assertThat(tested.getMethodsFound().length, equalTo(0));
        verify(logger).warning(anyString(), any(Element.class));
    }

    @Test
    public void visitExecutable_elementWithKindMethod_withMultipleMatchingAnnotations() {
        ExecutableElement executableElement = mockExecutableElement(ElementKind.METHOD);
        stubGeneratorMethodAnnotations(executableElement, true, true, true);

        tested.visitExecutable(executableElement, null);

        assertThat(tested.getMethodsFound().length, equalTo(0));
        verify(logger).error(anyString(), any(Element.class));
    }

    @Test
    public void visitExecutable_elementWithKindMethod_withExtendedSignature() {
        ExecutableElement executableElement = mockExecutableElement(ElementKind.METHOD);
        stubGeneratorMethodAnnotations(executableElement, true, true, false);

        TypeMirror returnTypeBoolean = mockTypeMirror(TypeKind.BOOLEAN);
        when(executableElement.getReturnType()).thenReturn(returnTypeBoolean);

        tested.visitExecutable(executableElement, null);

        assertThat(tested.getMethodsFound().length, equalTo(0));
        verify(logger).warning(anyString(), any(Element.class));
    }

    @Test
    public void visitExecutable_elementWithKindMethod_expectedBehavior() {
        ExecutableElement executableElement = mockExecutableElement(ElementKind.METHOD);
        stubGeneratorMethodAnnotations(executableElement, true, true, false);

        TypeMirror returnTypeVoid = mockTypeMirror(TypeKind.VOID);
        when(executableElement.getReturnType()).thenReturn(returnTypeVoid);

        tested.visitExecutable(executableElement, null);

        assertThat(tested.getMethodsFound().length, equalTo(1));
        verify(logger, never()).warning(anyString(), any(Element.class));
        verify(logger, never()).error(anyString(), any(Element.class));
    }

    void stubGeneratorMethodAnnotations(ExecutableElement element, boolean hasProducesAnnotation,
            boolean hasForAllElementsAnnotation, boolean hasForEachElementAnnotation) {
        when(element.getAnnotation(Produces.class)).thenReturn(
                hasProducesAnnotation ? mock(Produces.class) : null);
        when(element.getAnnotation(ForAllElements.class)).thenReturn(
                hasForAllElementsAnnotation ? mock(ForAllElements.class) : null);
        when(element.getAnnotation(ForEachElement.class)).thenReturn(
                hasForEachElementAnnotation ? mock(ForEachElement.class) : null);
    }

    ExecutableElement mockExecutableElement(ElementKind elementKind) {
        return when(mock(ExecutableElement.class).getKind()).thenReturn(elementKind).getMock();
    }

    TypeMirror mockTypeMirror(TypeKind typeKind) {
        return when(mock(TypeMirror.class).getKind()).thenReturn(typeKind).getMock();
    }

}
